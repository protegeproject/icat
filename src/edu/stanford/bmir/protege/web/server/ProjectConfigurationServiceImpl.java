package edu.stanford.bmir.protege.web.server;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.thoughtworks.xstream.XStream;

import edu.stanford.bmir.protege.web.client.rpc.ProjectConfigurationService;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabColumnConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.TabConfiguration;
import edu.stanford.smi.protege.server.metaproject.Group;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;

public class ProjectConfigurationServiceImpl extends RemoteServiceServlet implements ProjectConfigurationService {

	private static final long serialVersionUID = -2875415014621934377L;

	private final String PROJECT_CONFIG_DIR = "projectConfigurations";
	private String configurationFilePrefix = null;

	private String getProjectConfigPrefix() {
		if (configurationFilePrefix == null) {
			configurationFilePrefix = FileUtil.getRealPath() + PROJECT_CONFIG_DIR + File.separator + "configuration";
		}
		return configurationFilePrefix;
	}

	// TODO: default configuration is different for owl and frames projects.
	private File getConfigurationFile(String projectName, String userName) {
		if (userName == null) { // happens if the user is not logged in
			return getDefaultConfigurationFile(projectName);
		}

		File configFile = getProjectAndUserConfigurationFile(projectName, userName);

		if (configFile.exists() == false) {
			Iterator<File> it = getProjectAndUserGroupConfigurationFiles(projectName, userName).iterator();
			while (!configFile.exists() && it.hasNext()) {
				configFile = it.next();
			}
		}

		if (configFile.exists() == false) {
			return getDefaultConfigurationFile(projectName);
		}

		return configFile;
	}

	private File getDefaultConfigurationFile(String projectName) {
		File configFile = new File(getProjectConfigPrefix() + "_" + projectName + ".xml");
		if (!configFile.exists()) {
			configFile = getDefaultConfigurationFile();
		}
		return configFile;
	}

	private File getDefaultConfigurationFile() {
		return new File(getProjectConfigPrefix() + ".xml");
	}

	private File getProjectAndUserConfigurationFile(String projectName, String userName) {
		return new File(getProjectConfigPrefix() + "_" + projectName + "_" + userName + ".xml");
	}

	private Set<File> getProjectAndUserGroupConfigurationFiles(String projectName, String userName) {
		Set<File> res = new TreeSet<File>(new Comparator<File>() {
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		User user = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUser(userName);

		if (user == null) {
			return res;
		}

		Set<Group> groups = user.getGroups();

		if (groups == null) {
			return res;
		}

		for (Group g : groups) {
			String groupName = g.getName();
			if (groupName != null) {
				res.add(new File(getProjectConfigPrefix() + "_" + projectName + "_" + groupName + ".xml"));
			}
		}
		
		return res;
	}

	public ProjectConfiguration getProjectConfiguration(String projectName, String userName) {
		ProjectConfiguration config = null;

		File f = getConfigurationFile(projectName, userName);
		Log.getLogger().info("Opening project configuration file for " + projectName + 
				" and user: " + userName + ": " + f.getAbsolutePath());

		if (!f.exists()) {
			Log.getLogger().severe("Installation misconfigured: Default project configuration file missing: " + f);
			throw new IllegalStateException("Misconfiguration");
		}

		
		try {
			Reader configReader = getXMLConfigReader(f);
			config = convertXMLToConfiguration(configReader);
			configReader.close();
		} catch (java.io.FileNotFoundException e) {
			config = new ProjectConfiguration();
		} catch (Exception e) {
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

	private String convertConfigDetailsToXML(ProjectConfiguration config) {
		XStream xstream = new XStream();
		xstream.alias("project", ProjectConfiguration.class);
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
		xstream.alias("project", ProjectConfiguration.class);
		return xstream.toXML(config);
	}

	private ProjectConfiguration convertXMLToConfiguration(Reader reader) {
		XStream xstream = new XStream();
		xstream.alias("project", ProjectConfiguration.class);
		xstream.alias("tab", TabConfiguration.class);
		xstream.alias("portlet", PortletConfiguration.class);
		xstream.alias("column", TabColumnConfiguration.class);
		xstream.alias("map", LinkedHashMap.class);
		ProjectConfiguration config = (ProjectConfiguration) xstream.fromXML(reader);
		return config;
	}

	
	/************* File utilities *****************/
	

	/**
	 * This method will merge all the included XML file with xinclude from the main 
	 * XML file (xmlFile).
	 * 
	 * @param xmlFile
	 * @return an InputStreamReader for the merged XML file
	 * 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws FileNotFoundException 
	 * @throws ParserConfigurationException 
	 * @throws TransformerException 
	 */
	private Reader getXMLConfigReader(File xmlFile) throws FileNotFoundException, SAXException, 
									IOException, ParserConfigurationException, TransformerException {
	    // document parser
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setXIncludeAware(true);
        factory.setNamespaceAware(true);
        
        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document doc = docBuilder.parse(new FileInputStream(xmlFile));
        
        // print result in output stream
        DOMSource source = new DOMSource(doc);
        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamResult result = new StreamResult(os);
        
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
        
        InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
        return new InputStreamReader(inputStream);
	}
	
}
