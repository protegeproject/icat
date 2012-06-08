package edu.stanford.bmir.protege.web.server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.logging.Level;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.thoughtworks.xstream.XStream;

import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationService;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabColumnConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabConfiguration;
import edu.stanford.smi.protege.util.Log;

public class ProjectConfigurationServiceImpl extends RemoteServiceServlet implements ProjectConfigurationService {
    //private static final Logger log = Log.getLogger(ProjectConfigurationServiceImpl.class);

	private static final long serialVersionUID = -2875415014621934377L;

	private static final String PROJECT_CONFIG_DIR = "projectConfigurations";
	private static String configurationFilePrefix = null;


	private static String getProjectConfigPrefix() {
		if (configurationFilePrefix == null) {
			configurationFilePrefix = FileUtil.getRealPath() + PROJECT_CONFIG_DIR + File.separator + "configuration";
		}
		return configurationFilePrefix;
	}

    // TODO: default configuration is different for owl and frames projects.
	public static File getConfigurationFile(String projectName, String userName) {
	    File configFile = getProjectAndUserConfigurationFile(projectName, userName);
	    if (!configFile.exists()) {
	        configFile = new File(getProjectConfigPrefix() + "_" + projectName + ".xml");
	    }
	    if (!configFile.exists()) {
	        configFile = new File(getProjectConfigPrefix() + ".xml");
	    }
	    if (Log.getLogger().isLoggable(Level.FINE)) {
	    	Log.getLogger().fine("Path to project configuration file: " + configFile);
	    }
	    return configFile;
	}

	public static File getProjectAndUserConfigurationFile(String projectName, String userName) {
	    return new File(getProjectConfigPrefix() + "_" + projectName + "_" + userName + ".xml");
	}


	public ProjectConfiguration getProjectConfiguration(String projectName, String userName) {
		ProjectConfiguration config = null;

		File f = getConfigurationFile(projectName, userName);

		if (!f.exists()) {
		    Log.getLogger().severe("Installation misconfigured: Default project configuration file missing: " + f);
		    throw new IllegalStateException("Misconfiguration");
		}
		try {
		    FileReader fileReader = new FileReader(f);
            config = convertXMLToConfiguration(fileReader);
            fileReader.close();
        } catch (java.io.FileNotFoundException e) {
        	config = new ProjectConfiguration();
        } catch (java.io.IOException e) {
        	Log.getLogger().log(Level.WARNING, "Failed to read from config file at server. ", e);
        }

        config.setOntologyName(projectName);

		return config;
	}


	public void saveProjectConfiguration(String projectName, String userName, ProjectConfiguration config) {
		String xml = convertConfigDetailsToXML(config);
        File f = getProjectAndUserConfigurationFile(projectName, userName);

		try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(xml);
            bw.close();
        } catch (IOException e) {
        	Log.getLogger().log(Level.WARNING, "Failed to write to config file. ", e);
        }

	}


	public String convertConfigDetailsToXML(ProjectConfiguration config) {
		XStream xstream = new XStream();
		xstream.alias("project", ProjectConfiguration.class);
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
		xstream.alias("project", ProjectConfiguration.class);
		return xstream.toXML(config);
	}

	public ProjectConfiguration convertXMLToConfiguration(Reader reader) {
		XStream xstream = new XStream();
		xstream.alias("project", ProjectConfiguration.class);
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
        xstream.alias("map", LinkedHashMap.class);
		ProjectConfiguration config = (ProjectConfiguration)xstream.fromXML(reader);
		return config;
	}

}
