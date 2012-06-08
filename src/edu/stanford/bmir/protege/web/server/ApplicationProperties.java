package edu.stanford.bmir.protege.web.server;

import java.io.File;
import java.net.URI;

import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;

/**
 * Provides static methods for accessing WebProtege
 * configuration setting. For example, accessing the properties
 * stored in protege.properties.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ApplicationProperties {

    /*
     * Paths
     */
    private final static String PROJECTS_DIR = "projects"; //not important - just for the default location of the metaproject
    private final static String METAPROJECT_FILE = "metaproject" + File.separator + "metaproject.pprj";
    private static final String LOCAL_METAPROJECT_PATH_PROP = "local.metaproject.path";
    private static final String LOCAL_METAPROJECT_PATH_DEFAULT = PROJECTS_DIR + File.separator + METAPROJECT_FILE;

    /*
     * Application settings
     */

    private static final String APPLICATION_NAME_PROP = "application.name";
    private static final String APPLICATION_NAME_DEFAULT = "WebProtege";

    private static final String APPLICATION_URL_PROP = "application.url";
    private static final String APPLICATION_URL_DEFAULT = "localhost";

    private static final String APPLICATION_PORT_PROP = "application.port";
    private static final String APPLICATION_PORT_DEFAULT = "8080";


    private static final String ENABLE_IMMEDIATE_NOTIFICATION = "enable.immediate.notification";
    private static final Boolean ENABLE_IMMEDIATE_NOTIFICATION_DEFAULT = Boolean.FALSE;

    private static final String ENABLE_ALL_NOTIFICATION = "enable.all.notification";
    private static final Boolean ENABLE_ALL_NOTIFICATION_DEFAULT = Boolean.TRUE;

    /*
     * Protege server settings
     */
    private static final String PROTEGE_SERVER_HOSTNAME_PROP = "protege.server.hostname";
    private static final String PROTEGE_SERVER_HOSTNAME_DEFAULT = "localhost";

    private final static String PROTEGE_SERVER_USER_PROP = "webprotege.user";
    private final static String PROTEGE_SERVER_USER_DEFAULT = "webprotege";

    private final static String PROTEGE_SERVER_PASSWORD_PROP = "webprotege.password";
    private final static String PROTEGE_SERVER_PASSWORD_DEFAULT = "webprotege";

    private static final String LOAD_ONTOLOGIES_FROM_PROTEGE_SERVER_PROP = "load.ontologies.from.protege.server";
    private static final boolean LOAD_ONTOLOGIES_FROM_PROTEGE_SERVER_DEFAULT = false;

    /*
     * Email settings
     */

    private static final String EMAIL_SMTP_HOST_NAME_PROP = "email.smtp.host.name";
    private static final String EMAIL_SMTP_PORT_PROP = "email.smtp.port";
    private static final String EMAIL_SSL_FACTORY_PROP = "email.ssl.factory";

    private static final String EMAIL_ACCOUNT_PROP = "email.account";
    private static final String EMAIL_PASSWORD_PROP = "email.password";

    /*
     * Automatic save for local projects
     */
    private static final String SAVE_INTERVAL_PROP = "server.save.interval.sec";
    private static final int SAVE_INTERVAL_DEFAULT = 120;
    public static final int NO_SAVE = -1;


    public static URI getWeprotegeDirectory() {
        String uri = FileUtil.getRealPath();
        return URIUtilities.createURI(uri);
    }

    public static URI getLocalMetaprojectURI() {
        String path = edu.stanford.smi.protege.util.ApplicationProperties.getString(LOCAL_METAPROJECT_PATH_PROP);
        if (path == null) {
            path = FileUtil.getRealPath() + LOCAL_METAPROJECT_PATH_DEFAULT;
        }
        Log.getLogger().info("Path to local metaproject: " + path);
        return URIUtilities.createURI(path);
    }

    public static String getProtegeServerHostName() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(PROTEGE_SERVER_HOSTNAME_PROP, PROTEGE_SERVER_HOSTNAME_DEFAULT);
    }

    public static String getProtegeServerUser() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(PROTEGE_SERVER_USER_PROP, PROTEGE_SERVER_USER_DEFAULT);
    }

    public static String getProtegeServerPassword() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(PROTEGE_SERVER_PASSWORD_PROP, PROTEGE_SERVER_PASSWORD_DEFAULT);
    }

    public static boolean getLoadOntologiesFromServer() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getBooleanProperty(LOAD_ONTOLOGIES_FROM_PROTEGE_SERVER_PROP, LOAD_ONTOLOGIES_FROM_PROTEGE_SERVER_DEFAULT);
    }

    public static int getLocalProjectSaveInterval() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getIntegerProperty(SAVE_INTERVAL_PROP, SAVE_INTERVAL_DEFAULT);
    }

    public static String getSmtpHostName() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(EMAIL_SMTP_HOST_NAME_PROP, "");
    }

    public static String getSmtpPort() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(EMAIL_SMTP_PORT_PROP, "");
    }

    public static String getSslFactory() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(EMAIL_SSL_FACTORY_PROP, "javax.net.ssl.SSLSocketFactory");
    }

    public static String getEmailAccount() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(EMAIL_ACCOUNT_PROP, "");
    }

    public static String getEmailPassword() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(EMAIL_PASSWORD_PROP, "");
    }

    public static String getApplicationName() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(APPLICATION_NAME_PROP, APPLICATION_NAME_DEFAULT);
    }

    public static String getApplicationPort() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(APPLICATION_PORT_PROP, APPLICATION_PORT_DEFAULT);
    }

    public static String getApplicationUrl() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getString(APPLICATION_URL_PROP, APPLICATION_URL_DEFAULT);
    }

    public static Boolean getImmediateThreadsEnabled() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getBooleanProperty(ENABLE_IMMEDIATE_NOTIFICATION, ENABLE_IMMEDIATE_NOTIFICATION_DEFAULT);
    }

    public static Boolean getAllNotificationEnabled() {
        return edu.stanford.smi.protege.util.ApplicationProperties.getBooleanProperty(ENABLE_ALL_NOTIFICATION, ENABLE_ALL_NOTIFICATION_DEFAULT);
    }
}
