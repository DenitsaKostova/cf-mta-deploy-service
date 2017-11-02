package com.sap.cloud.lm.sl.cf.web.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.web.api.FilesApiService;
import com.sap.cloud.lm.sl.cf.web.api.model.FileMetadata;
import com.sap.cloud.lm.sl.cf.web.message.Messages;
import com.sap.cloud.lm.sl.common.SLException;
import com.sap.cloud.lm.sl.persistence.model.FileEntry;
import com.sap.cloud.lm.sl.persistence.services.AbstractFileService;
import com.sap.cloud.lm.sl.persistence.services.DatabaseFileService;
import com.sap.cloud.lm.sl.persistence.services.FileStorageException;
import com.sap.cloud.lm.sl.persistence.util.Configuration;
import com.sap.cloud.lm.sl.persistence.util.DefaultConfiguration;

@RequestScoped
@Component
public class FilesApiServiceImpl implements FilesApiService {

    private static final String DEFAULT_FILE_NAMESPACE = "deploy";
    @Inject
    // The @Named annotation is needed for the Jersey-Spring integration in order to identify which
    // FileService managed instance (FileService or ProgressMessageService) to inject.
    @Named("fileService")
    private AbstractFileService fileService;

    public Response getMtaFiles(SecurityContext securityContext, String spaceGuid) {
        try {
            List<FileEntry> entries = fileService.listFiles(spaceGuid, DEFAULT_FILE_NAMESPACE);
            List<FileMetadata> files = entries.stream().map(entry -> parseFileEntry(entry)).collect(Collectors.toList());
            return Response.ok().entity(files).build();

        } catch (FileStorageException e) {
            throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    public Response uploadMtaFile(HttpServletRequest request, SecurityContext securityContext, String spaceGuid) {
        try {
            FileEntry fileEntry = uploadFiles(request, spaceGuid).get(0);
            FileMetadata fileMetadata = parseFileEntry(fileEntry);
            return Response.status(Status.CREATED).entity(fileMetadata).build();

        } catch (Exception e) {
            throw new WebApplicationException(e, Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
    }

    private List<FileEntry> uploadFiles(HttpServletRequest request, String spaceGuid)
        throws FileUploadException, IOException, FileStorageException, SLException {
        ServletFileUpload upload = getFileUploadServlet();
        long maxUploadSize = getConfiguration().getMaxUploadSize();
        upload.setSizeMax(maxUploadSize);

        List<FileEntry> uploadedFiles = new ArrayList<FileEntry>();
        FileItemIterator fileItemIterator = null;
        try {
            fileItemIterator = upload.getItemIterator(request);
        } catch (SizeLimitExceededException ex) {
            throw new SLException(MessageFormat.format(Messages.MAX_UPLOAD_SIZE_EXCEEDED, maxUploadSize));
        }
        while (fileItemIterator.hasNext()) {
            FileItemStream item = fileItemIterator.next();
            if (item.isFormField()) {
                continue; // ignore simple (non-file) form fields
            }

            InputStream in = null;
            try {
                in = item.openStream();
                FileEntry entry = getFileService().addFile(spaceGuid, DEFAULT_FILE_NAMESPACE, item.getName(),
                    getConfiguration().getFileUploadProcessor(), in);
                uploadedFiles.add(entry);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
        return uploadedFiles;
    }

    protected ServletFileUpload getFileUploadServlet() {
        return new ServletFileUpload();
    }

    public AbstractFileService getFileService() {
        if (fileService == null) {
            fileService = DatabaseFileService.getInstance();
        }
        return fileService;
    }

    protected Configuration getConfiguration() {
        return new DefaultConfiguration();
    }

    private FileMetadata parseFileEntry(FileEntry fileEntry) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(fileEntry.getId());
        fileMetadata.setDigest(fileEntry.getDigest());
        fileMetadata.setDigestAlgorithm(fileEntry.getDigestAlgorithm());
        fileMetadata.setName(fileEntry.getName());
        fileMetadata.setSize(fileEntry.getSize());
        fileMetadata.setSpace(fileEntry.getSpace());
        return fileMetadata;
    }
}