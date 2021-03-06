package com.sap.cloud.lm.sl.cf.client.message;

public class Messages {
    public static final String CANT_CREATE_CLIENT = "Could not create XsClient instance for URL \"{0}\" with org \"{1}\" and space \"{2}\"";
    public static final String CANT_GET_XS_INFO = "Could not get XS info";
    public static final String CANT_GET_ORGS = "Could not get organizations";
    public static final String CANT_GET_ORG = "Could not get organization \"{0}\"";
    public static final String CANT_GET_SPACES = "Could not get spaces";
    public static final String CANT_GET_SPACE = "Could not get space \"{0}\"";
    public static final String CANT_GET_APPS = "Could not get applications";
    public static final String CANT_GET_APP = "Could not get application \"{0}\"";
    public static final String CANT_GET_APP_INSTANCES = "Could not get application instances";
    public static final String CANT_CREATE_APP = "Could not create application \"{0}\"";
    public static final String CANT_UPLOAD_APP = "Could not upload file {1} of application \"{0}\"";
    public static final String CANT_ASYNC_UPLOAD_APP = "Could not asynchronously upload file {1} of application \"{0}\"";
    public static final String CANT_GET_UPLOAD_INFO = "Could not get upload info for token \"{0}\"";
    public static final String CANT_UPDATE_APP_DISK = "Could not update disk quota of application \"{0}\" to {1}";
    public static final String CANT_UPDATE_APP_MEMORY = "Could not update memory of application \"{0}\" to {1}";
    public static final String CANT_UPDATE_APP_INSTANCES = "Could not update instances of application \"{0}\" to {1}";
    public static final String CANT_UPDATE_APP_SERVICES = "Could not update services of application \"{0}\" to {1}";
    public static final String CANT_UPDATE_APP_STAGING = "Could not update staging of application \"{0}\"";
    public static final String CANT_UPDATE_APP_URIS = "Could not update URIs of application \"{0}\" to {1}";
    public static final String CANT_UPDATE_APP_ENV = "Could not update environment of application \"{0}\"";
    public static final String CANT_START_APP = "Could not start application \"{0}\"";
    public static final String CANT_STAGE_APP = "Could not stage application \"{0}\"";
    public static final String CANT_STOP_APP = "Could not stop application \"{0}\"";
    public static final String CANT_DELETE_APP = "Could not delete application \"{0}\"";
    public static final String CANT_GET_SERVICES = "Could not get services";
    public static final String CANT_GET_SERVICE_KEYS = "Could not get service keys for service \"{0}\"";
    public static final String CANT_GET_SERVICE = "Could not get service \"{0}\"";
    public static final String CANT_GET_SERVICE_INSTANCE = "Could not get instance of service \"{0}\"";
    public static final String CANT_CREATE_SERVICE = "Could not create service \"{0}\"";
    public static final String CANT_CREATE_SERVICE_KEY = "Could not create service key \"{0}\" for service \"{1}\"";
    public static final String CANT_DELETE_SERVICE_KEY = "Could not delete service key \"{0}\" for service \"{1}\"";
    public static final String CANT_CREATE_USER_PROVIDED_SERVICE = "Could not create user-provided service \"{0}\"";
    public static final String CANT_BIND_SERVICE = "Could not bind application \"{0}\" to service \"{1}\"";
    public static final String CANT_UNBIND_SERVICE = "Could not unbind application \"{0}\" from service \"{1}\"";
    public static final String CANT_DELETE_SERVICE = "Could not delete service \"{0}\"";
    public static final String CANT_GET_SERVICE_OFFERINGS = "Could not get service offerings";
    public static final String CANT_STREAM_APP_LOGS = "Could not stream logs of application \"{0}\"";
    public static final String CANT_GET_RECENT_LOGS = "Could not get recent logs of application \"{0}\"";
    public static final String CANT_GET_STAGING_LOGS = "Could not get staging logs of application \"{0}\"";
    public static final String CANT_GET_SERVICE_BROKERS = "Could not get service brokers";
    public static final String CANT_GET_SERVICE_BROKER = "Could not get service broker \"{0}\"";
    public static final String CANT_CREATE_SERVICE_BROKER = "Could not create service broker \"{0}\"";
    public static final String CANT_UPDATE_SERVICE_BROKER = "Could not update service broker \"{0}\"";
    public static final String CANT_DELETE_SERVICE_BROKER = "Could not delete service broker \"{0}\"";
    public static final String CANT_GET_DOMAINS = "Could not get domains";
    public static final String CANT_GET_DOMAINS_FOR_ORG = "Could not get domains for the current org";
    public static final String CANT_GET_SHARED_DOMAINS = "Could not get shared domains";
    public static final String CANT_GET_DEFAULT_DOMAIN = "Could not get default domain";
    public static final String CANT_ADD_DOMAIN = "Could not add domain \"{0}\"";
    public static final String CANT_DELETE_DOMAIN = "Could not delete domain \"{0}\"";
    public static final String CANT_GET_ROUTES = "Could not get routes";
    public static final String CANT_ADD_ROUTE = "Could not add route for domain \"{0}\" with host / port \"{1}\"";
    public static final String CANT_DELETE_ROUTE = "Could not delete route for domain \"{0}\" with host / port \"{1}\"";
    public static final String CANT_DELETE_ORPHANED_ROUTES = "Could not delete orphaned routes";
    public static final String CANT_LOGIN = "Could not login";
    public static final String CANT_GET_SPACE_MANAGERS = "Could not get space managers for space \"{0}\"";
    public static final String CANT_GET_SPACE_DEVELOPERS = "Could not get space developers for space \"{0}\"";
    public static final String CANT_GET_SPACE_AUDITORS = "Could not get space auditors for space \"{0}\"";
    public static final String CANT_GET_SPACE_MANAGERS2 = "Could not get space managers for org \"{0}\" and space \"{1}\"";
    public static final String CANT_GET_SPACE_DEVELOPERS2 = "Could not get space developers for org \"{0}\" and space \"{1}\"";
    public static final String CANT_GET_SPACE_AUDITORS2 = "Could not get space auditors for org \"{0}\" and space {1}";
    public static final String CANT_REGISTER_SERVICE_URL = "Could not register service URL for service \"{0}\" and URL \"{1}\"";
    public static final String CANT_UNREGISTER_SERVICE_URL = "Could not unregister service URL for service \"{0}\"";
    public static final String CANT_UPDATE_USER_PROVIDED_SERVICE_CREDENTIALS = "Could not update credentials of user-provided service \"{0}\" to \"{1}\"";
    public static final String CANT_UPDATE_SERVICE_TAGS = "Could not update tags of service \"{0}\" to \"{1}\"";
    public static final String CANT_UPDATE_SERVICE_PARAMETERS = "Could not update parameters of service \"{0}\" to \"{1}\"";
    public static final String CANT_GET_TOKEN = "Could not get token";
    public static final String CANT_GET_CONTROLLER_URL = "Could not get controller URL";
    public static final String CANT_RESERVE_PORT = "Could not reserve port for domain \"{0}\"";
    public static final String CANT_GET_SERVICE_TAGS = "Cloud not retrieve tags for service \"{0}\"";
    public static final String CLIENT_NOT_SET_UP = "Client is not set up";
    public static final String CANT_GET_TASKS_FOR_APP = "Could not get tasks for application \"{0}\"";
    public static final String CANT_RUN_TASK_ON_APP = "Could not run task \"{0}\" on application \"{1}\"";
    public static final String CANT_CANCEL_TASK = "Could not cancel task with GUID \"{0}\"";
    public static final String CANT_UPDATE_SERVICE_PLAN = "Could not update service plan of service \"{0}\": \"{1}\"";
    public static final String ERROR_SIZE_OF_UNCOMPRESSED_FILE_EXCEEDS_MAX_SIZE_LIMIT = "The size \"{0}\" of file \"{1}\" exceeds max size limit \"{2}\"";
    public static final String SAVING_INPUT_STREAM_TEMP_FILE = "Saving input stream to temporary file \"{0}\"...";
    public static final String INPUT_STREAM_SAVED_IN_TEMP_FILE = "Input stream saved to temporary file \"{0}\"";
    public static final String SAVING_INPUT_STREAM_TO_TMP_DIR = "Saving input stream to temporary directory \"{0}\"...";
    public static final String INPUT_STREAM_SAVED_TO_TMP_DIR = "Input stream saved to temporary directory \"{0}\"";
}
