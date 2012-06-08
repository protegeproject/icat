package edu.stanford.bmir.protege.web.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
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
		String xml = "";
		ProjectConfiguration config = null;
		List<TabConfiguration> tabs = null;

		File f = getConfigurationFile(projectName, userName);

		if (!f.exists()) {
		    Log.getLogger().severe("Installation misconfigured: Default project configuration file missing: " + f);
		    throw new IllegalStateException("Misconfiguration");
		}
		try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while (line != null) {
            	xml = xml + "\n" + line;
            	line = br.readLine();
            }
            br.close();
        } catch (java.io.FileNotFoundException e) {
        	config = new ProjectConfiguration();
        } catch (java.io.IOException e) {
        	Log.getLogger().log(Level.WARNING, "Failed to read from config file at server. ", e);
        }

		config = convertXMLToConfiguration(xml);
        config.setOntologyName(projectName);

		return config;
	}


	public Boolean saveProjectConfiguration(String projectName, String userName, ProjectConfiguration config) {
		String xml = convertConfigDetailsToXML(config.getTabs());
        File f = getProjectAndUserConfigurationFile(projectName, userName);

		try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            bw.write(xml);
            bw.close();
        } catch (IOException e) {
        	Log.getLogger().log(Level.WARNING, "Failed to write to config file. ", e);
        	return false;
        }
		return true;
	}


	public String convertConfigDetailsToXML(List<TabConfiguration> tabs) {
		XStream xstream = new XStream();
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
		xstream.alias("project", ProjectConfiguration.class);
		return xstream.toXML(tabs);
	}

	public ProjectConfiguration convertXMLToConfiguration(String xml) {
		XStream xstream = new XStream();
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
		xstream.alias("project", ProjectConfiguration.class);
        xstream.alias("map", LinkedHashMap.class);
		ProjectConfiguration config = (ProjectConfiguration)xstream.fromXML(xml);
		return config;
	}

}
