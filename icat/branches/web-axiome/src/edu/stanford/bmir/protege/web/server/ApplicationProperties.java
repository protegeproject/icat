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
	
	static String getProtegeServerUser() {
		return edu.stanford.smi.protege.util.ApplicationProperties.getString(PROTEGE_SERVER_USER_PROP, PROTEGE_SERVER_USER_DEFAULT);
	}
	
	static String getProtegeServerPassword() {
		return edu.stanford.smi.protege.util.ApplicationProperties.getString(PROTEGE_SERVER_PASSWORD_PROP, PROTEGE_SERVER_PASSWORD_DEFAULT);
	}
	
	public static boolean getLoadOntologiesFromServer() {
		return edu.stanford.smi.protege.util.ApplicationProperties.getBooleanProperty(LOAD_ONTOLOGIES_FROM_PROTEGE_SERVER_PROP, LOAD_ONTOLOGIES_FROM_PROTEGE_SERVER_DEFAULT);
	}

	public static int getLocalProjectSaveInterval() {
		return edu.stanford.smi.protege.util.ApplicationProperties.getIntegerProperty(SAVE_INTERVAL_PROP, SAVE_INTERVAL_DEFAULT);
	}
	
}
