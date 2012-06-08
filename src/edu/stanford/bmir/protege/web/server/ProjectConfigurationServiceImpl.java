package edu.stanford.bmir.protege.web.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.thoughtworks.xstream.XStream;

import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationService;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabColumnConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabConfiguration;
import edu.stanford.smi.protege.util.Log;

public class ProjectConfigurationServiceImpl extends RemoteServiceServlet implements ProjectConfigurationService {
    private static final Logger log = Log.getLogger(ProjectConfigurationServiceImpl.class);
	
	private static final long serialVersionUID = -2875415014621934377L;
	
	private static final String PROJECT_CONFIG_DIR = "projectConfigurations";
	private static String configurationFilePrefix = FileUtil.getRealPath() + PROJECT_CONFIG_DIR + File.separator + "configuration";

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		if (log.isLoggable(Level.FINE)) {
			log.fine("In init of ProjectConfigurationImpl");
		}
		
		ServletContext context = config.getServletContext();
		FileUtil.init(context.getRealPath("/")); //TODO: check if necessary
		
		checkConfigDirExists();
	}

	
	private void checkConfigDirExists() {
		String path = FileUtil.getRealPath() + PROJECT_CONFIG_DIR;
		File file = new File(path);
		if (!file.exists()) {
			boolean success = false;
			try {
				success = file.mkdir();
			} catch (Exception e) {
				log.log(Level.WARNING, "Could not create project configuration directory in " + path, e);
				return;
			}
			if (success) {
				log.log(Level.WARNING, "Created project configuration directory in " + path);
			} else {
				log.log(Level.WARNING, "Could not create project configuration directory in " + path);
			}
		}
	}
    
    // TODO: default configuration is different for owl and frames projects.
	public static File getConfigurationFile(String projectName, String userName) {
	    File configFile = getProjectAndUserConfigurationFile(projectName, userName);
	    if (!configFile.exists()) {
	        configFile = new File(configurationFilePrefix + "_" + projectName + ".xml");
	    }
	    if (!configFile.exists()) {
	        configFile = new File(configurationFilePrefix + ".xml");
	    }
	    if (log.isLoggable(Level.FINE)) {
	    	log.fine("Path to project configuration file: " + configFile);
	    }
	    return configFile;
	}
	
	public static File getProjectAndUserConfigurationFile(String projectName, String userName) {
	    return new File(configurationFilePrefix + "_" + projectName + "_" + userName + ".xml");
	}
	

	public ProjectConfiguration getProjectConfiguration(String projectName, String userName) { 
		String xml = "";
		ProjectConfiguration config = null;
		List<TabConfiguration> tabs = null;		

		File f = getConfigurationFile(projectName, userName);
		
		if (!f.exists()) {
		    log.severe("Installation misconfigured: Default project configuration file missing: " + f);
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
        	log.log(Level.WARNING, "Failed to read from config file at server. ", e);
        }
        
        tabs = convertXMLToConfiguration(xml);
        config = new ProjectConfiguration();
        config.setTabs(tabs);
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
        	log.log(Level.WARNING, "Failed to write to config file. ", e);
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
	
	public List<TabConfiguration> convertXMLToConfiguration(String xml) {
		XStream xstream = new XStream();
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
		xstream.alias("project", ProjectConfiguration.class);
		List<TabConfiguration> tabs = (List<TabConfiguration>)xstream.fromXML(xml);		
		return tabs;		
	}
	
}
