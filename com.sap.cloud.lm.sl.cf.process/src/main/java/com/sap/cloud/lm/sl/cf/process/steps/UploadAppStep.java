package com.sap.cloud.lm.sl.cf.process.steps;

import static java.text.MessageFormat.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.activiti.engine.delegate.DelegateExecution;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sap.activiti.common.ExecutionStatus;
import com.sap.cloud.lm.sl.cf.client.ClientExtensions;
import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.UploadStatusCallbackExtended;
import com.sap.cloud.lm.sl.cf.client.util.InputStreamProducer;
import com.sap.cloud.lm.sl.cf.client.util.StreamUtil;
import com.sap.cloud.lm.sl.cf.core.activiti.ActivitiFacade;
import com.sap.cloud.lm.sl.cf.core.helpers.ApplicationEnvironmentUpdater;
import com.sap.cloud.lm.sl.cf.core.helpers.ApplicationFileDigestDetector;
import com.sap.cloud.lm.sl.cf.core.util.Configuration;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.message.Messages;
import com.sap.cloud.lm.sl.common.ContentException;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.persistence.processors.DefaultFileDownloadProcessor;
import com.sap.cloud.lm.sl.persistence.processors.FileDownloadProcessor;
import com.sap.cloud.lm.sl.persistence.services.FileStorageException;

@Component("uploadAppStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UploadAppStep extends AbstractProcessStep {

    private static final String WAIT_TILL_UPLOAD_START_TASK_ID = "waitTillUploadStartTask";
    private static final String ARCHIVE_FILE_SEPARATOR = "/";

    @Inject
    protected ScheduledExecutorService asyncTaskExecutor;
    @Inject
    protected ActivitiFacade activitiFacade;
    @Inject
    protected Configuration configuration;

    @Override
    protected ExecutionStatus executeStepInternal(DelegateExecution context) throws FileStorageException, SLException {
        getStepLogger().logActivitiTask();

        CloudApplicationExtended app = StepsUtil.getApp(context);

        try {
            getStepLogger().info(Messages.UPLOADING_APP, app.getName());

            int uploadAppTimeoutSeconds = configuration.getUploadAppTimeout();
            getStepLogger().debug(Messages.UPLOAD_APP_TIMEOUT, uploadAppTimeoutSeconds);

            CloudFoundryOperations client = getCloudFoundryClient(context);
            ClientExtensions clientExtensions = getClientExtensions(context);

            Future<?> future = asyncTaskExecutor.submit(getUploadAppStepRunnable(context, app, client, clientExtensions));
            asyncTaskExecutor.schedule(getUploadAppStepRunnableKiller(context, future), uploadAppTimeoutSeconds, TimeUnit.SECONDS);
        } catch (SLException e) {
            getStepLogger().error(e, Messages.ERROR_UPLOADING_APP, app.getName());
            throw e;
        }
        return ExecutionStatus.RUNNING;
    }

    private Runnable getUploadAppStepRunnableKiller(DelegateExecution context, Future<?> future) {
        return () -> {
            LOGGER.getLoggerImpl().warn(format(Messages.CANCELING_UPLOAD_ASYNC_THREAD, context.getProcessInstanceId()));
            if (future.cancel(true)) {
                LOGGER.getLoggerImpl().warn(format(Messages.ASYNC_THREAD_CANCELLED, context.getProcessInstanceId()));
            } else {
                LOGGER.getLoggerImpl().warn(format(Messages.ASYNC_THREAD_COMPLETED, context.getProcessInstanceId()));
            }
        };
    }

    private String asyncUploadFiles(DelegateExecution context, ClientExtensions clientExtensions, CloudFoundryOperations client,
        CloudApplication app, String appArchiveId, String fileName) throws FileStorageException, SLException {
        final StringBuilder uploadTokenBuilder = new StringBuilder();
        FileDownloadProcessor uploadFileToControllerProcessor = new DefaultFileDownloadProcessor(StepsUtil.getSpaceId(context),
            appArchiveId, (appArchiveStream) -> {
                File file = null;
                long maxStreamSize = Configuration.getInstance().getMaxResourceFileSize();
                try (InputStreamProducer streamProducer = getInputStreamProducer(appArchiveStream, fileName, maxStreamSize)) {
                    // Start uploading application content
                    file = saveToFile(fileName, streamProducer);
                    detectApplicationFileDigestChanges(context, app, file, client);
                    String uploadToken = clientExtensions.asynchUploadApplication(app.getName(), file,
                        getMonitorUploadStatusCallback(context, app, file));
                    uploadTokenBuilder.append(uploadToken);
                } catch (IOException e) {
                    cleanUpTempFile(context, file);
                    throw new SLException(e, Messages.ERROR_RETRIEVING_MTA_MODULE_CONTENT, fileName);
                } catch (CloudFoundryException e) {
                    cleanUpTempFile(context, file);
                    throw e;
                }
            });
        fileService.processFileContent(uploadFileToControllerProcessor);
        return uploadTokenBuilder.toString();
    }

    private void uploadFiles(DelegateExecution context, final CloudFoundryOperations client, final CloudApplication app,
        final String appArchiveId, final String fileName) throws FileStorageException, SLException {
        FileDownloadProcessor uploadFileToControllerProcessor = new DefaultFileDownloadProcessor(StepsUtil.getSpaceId(context),
            appArchiveId, (appArchiveStream) -> {
                File file = null;
                long maxStreamSize = Configuration.getInstance().getMaxResourceFileSize();
                try (InputStreamProducer streamProducer = getInputStreamProducer(appArchiveStream, fileName, maxStreamSize)) {
                    // Upload application content
                    file = saveToFile(fileName, streamProducer);
                    detectApplicationFileDigestChanges(context, app, file, client);
                    client.uploadApplication(app.getName(), file, getMonitorUploadStatusCallback(context, app, file));
                } catch (IOException e) {
                    throw new SLException(e, Messages.ERROR_RETRIEVING_MTA_MODULE_CONTENT, fileName);
                } catch (CloudFoundryException e) {
                    throw e;
                } finally {
                    cleanUpTempFile(context, file);
                }
            });
        fileService.processFileContent(uploadFileToControllerProcessor);

    }

    private void detectApplicationFileDigestChanges(DelegateExecution context, CloudApplication app, File applicationFile,
        CloudFoundryOperations client) {
        CloudApplication existingApp = client.getApplication(app.getName());
        ApplicationFileDigestDetector applicationFileDigestDetector = new ApplicationFileDigestDetector(existingApp);
        String appNewFileDigest = applicationFileDigestDetector.detectNewAppFileDigest(applicationFile);
        String currentFileDigest = applicationFileDigestDetector.detectCurrentAppFileDigest();
        attemptToUpdateApplicationDigest(client, app, appNewFileDigest, currentFileDigest);
        updateContextExtension(context, hasAppFileDigestChanged(appNewFileDigest, currentFileDigest));
    }

    private void attemptToUpdateApplicationDigest(CloudFoundryOperations client, CloudApplication app, String newFileDigest,
        String currentFileDigest) {
        if (!hasAppFileDigestChanged(newFileDigest, currentFileDigest)) {
            return;
        }
        new ApplicationEnvironmentUpdater(app, client).updateApplicationEnvironment(
            com.sap.cloud.lm.sl.cf.core.Constants.ENV_DEPLOY_ATTRIBUTES, com.sap.cloud.lm.sl.cf.core.Constants.ATTR_APP_CONTENT_DIGEST,
            newFileDigest);
    }

    private boolean hasAppFileDigestChanged(String newFileDigest, String currentFileDigest) {
        return !newFileDigest.equals(currentFileDigest);
    }

    private void updateContextExtension(DelegateExecution context, boolean appContentChanged) throws SLException {
        // boolean appPropertiesChanged = StepsUtil.getAppPropertiesChanged(context);
        // boolean hasAppChanged = appPropertiesChanged || appContentChanged;
        contextExtensionDao.addOrUpdate(context.getProcessInstanceId(), Constants.VAR_HAS_APP_CONTENT_CHANGED,
            Boolean.toString(appContentChanged));
    }

    MonitorUploadStatusCallback getMonitorUploadStatusCallback(DelegateExecution context, CloudApplication app, File file) {
        return new MonitorUploadStatusCallback(context, app, file);
    }

    InputStreamProducer getInputStreamProducer(InputStream appArchiveStream, String fileName, long maxStreamSize) throws SLException {
        return new InputStreamProducer(appArchiveStream, fileName, maxStreamSize);
    }

    @SuppressWarnings("resource")
    protected File saveToFile(String fileName, InputStreamProducer streamProducer) throws IOException {
        InputStream stream = streamProducer.getNextInputStream();
        if (stream == null) {
            throw new ContentException(com.sap.cloud.lm.sl.mta.message.Messages.CANNOT_FIND_ARCHIVE_ENTRY, fileName);
        }

        String entryName = streamProducer.getStreamEntryName();
        StreamUtil streamUtil = new StreamUtil(stream);
        if (isFile(fileName)) {
            return streamUtil.saveStreamToFile(entryName);
        }

        if (entryName.equals(fileName)) {
            return streamUtil.saveZipStreamToDirectory(fileName, Configuration.getInstance().getMaxResourceFileSize());
        }
        Path destinationDirectory = StreamUtil.getTempDirectory(fileName);
        while (stream != null) {
            if (!entryName.endsWith(ARCHIVE_FILE_SEPARATOR)) {
                streamUtil.saveStreamToDirectory(entryName, fileName, destinationDirectory);
            }
            // no need to close this stream because no new stream object is created
            stream = streamProducer.getNextInputStream();
            streamUtil.setInputStream(stream);
            entryName = streamProducer.getStreamEntryName();
        }
        return destinationDirectory.toFile();
    }

    void cleanUpTempFile(DelegateExecution context, File file) {
        if (file != null) {
            try {
                StreamUtil.deleteFile(file);
            } catch (IOException e) {
                getStepLogger().warn(Messages.ERROR_DELETING_APP_TEMP_FILE, file.getAbsolutePath());
            }
        }
    }

    private boolean isFile(String fileName) {
        return !fileName.endsWith(ARCHIVE_FILE_SEPARATOR);
    }

    class MonitorUploadStatusCallback implements UploadStatusCallbackExtended {

        static final String FINISHED_STATUS = "finished";

        private final DelegateExecution context;
        private final CloudApplication app;
        private final File file;

        public MonitorUploadStatusCallback(DelegateExecution context, CloudApplication app, File file) {
            this.context = context;
            this.app = app;
            this.file = file;
        }

        @Override
        public void onCheckResources() {
            getStepLogger().debug("Resources checked");
        }

        @Override
        public void onMatchedFileNames(Set<String> matchedFileNames) {
            getStepLogger().info("Matched files count: {0}", matchedFileNames.size());
        }

        @Override
        public void onProcessMatchedResources(int length) {
            getStepLogger().info("Matched resources processed, total size is {0}", length);
        }

        @Override
        public boolean onProgress(String status) {
            getStepLogger().info("Upload status: {0}", status);
            if (status.equals(FINISHED_STATUS)) {
                cleanUpTempFile(context, file);
            }
            return false;
        }

        @Override
        public void onError(Exception e) {
            getStepLogger().error(e, Messages.ERROR_UPLOADING_APP, app.getName());
            cleanUpTempFile(context, file);
        }

    }

    protected Runnable getUploadAppStepRunnable(DelegateExecution context, CloudApplicationExtended app, CloudFoundryOperations client,
        ClientExtensions clientExtensions) {
        return () -> {
            String processId = context.getProcessInstanceId();
            getStepLogger().trace("Started upload app step runnable for process \"{0}\"", processId);
            ExecutionStatus status = ExecutionStatus.FAILED;
            Map<String, Object> outputVariables = new HashMap<>();
            try {
                String appArchiveId = StepsUtil.getRequiredStringParameter(context, Constants.PARAM_APP_ARCHIVE_ID);
                String fileName = StepsUtil.getModuleFileName(context, app.getModuleName());
                getStepLogger().debug("Uploading file \"{0}\" for application \"{1}\"", fileName, app.getName());
                if (clientExtensions != null) {
                    String uploadToken = asyncUploadFiles(context, clientExtensions, client, app, appArchiveId, fileName);
                    outputVariables.put(Constants.VAR_UPLOAD_TOKEN, uploadToken);
                    getStepLogger().debug("Started async upload of application \"{0}\"", fileName, app.getName());
                    status = ExecutionStatus.RUNNING;
                } else {
                    uploadFiles(context, client, app, appArchiveId, fileName);
                    getStepLogger().debug(Messages.APP_UPLOADED, app.getName());
                    status = ExecutionStatus.SUCCESS;
                }
            } catch (SLException | FileStorageException e) {
                getStepLogger().error(e, Messages.ERROR_UPLOADING_APP, app.getName());
                logException(context, e);
                throw new SLException(e.getMessage(), e);
            } catch (CloudFoundryException cfe) {
                SLException e = StepsUtil.createException(cfe);
                getStepLogger().error(e, Messages.ERROR_UPLOADING_APP, app.getName());
                logException(context, e);
                throw e;
            } catch (Throwable e) {
                Throwable eWithMessage = getWithProperMessage(e);
                logException(context, eWithMessage);
                if (e instanceof Exception) {
                    // only wrap Runtime & checked exceptions as Runtime ones
                    throw new RuntimeException(eWithMessage.getMessage(), eWithMessage);
                } else {
                    // Errors and other should be handled elsewhere
                    throw e;
                }
            } finally {
                outputVariables.put(getStatusVariable(), status.name());
                LOGGER.getLoggerImpl()
                    .info(format("Attempting to signal process with id:{0} with variables : {1}", processId, outputVariables));
                signalWaitTask(context.getProcessInstanceId(), outputVariables, configuration.getUploadAppTimeout() * 1000);
            }
            getStepLogger().trace("Upload app step runnable for process \"{0}\" finished", context.getProcessInstanceId());
        };
    }

    protected void signalWaitTask(String processId, Map<String, Object> outputVariables, int timeout) {
        activitiFacade.signal(null, processId, WAIT_TILL_UPLOAD_START_TASK_ID, outputVariables, timeout);
    }

    @Override
    protected String getIndexVariable() {
        return Constants.VAR_APPS_INDEX;
    }

}
