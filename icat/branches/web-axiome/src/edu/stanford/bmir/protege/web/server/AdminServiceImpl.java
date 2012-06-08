package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.AdminService;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.smi.protege.util.Log;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class AdminServiceImpl extends RemoteServiceServlet 
							  implements AdminService {
	
	private static final long serialVersionUID = 7616699639338297327L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		ServletContext context = config.getServletContext();
		String webappRoot = context.getRealPath("/");
		try {
			/*
			 * Set the protege.dir to the webapp root, 
			 * so that protege.properties will be read
			 * from the webapp root, rather than tomcat root.
			 */
			System.setProperty("protege.dir", webappRoot);	
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		Log.getLogger().info("WebProtege running in: " + webappRoot); //this line has to be after setting the system property		
		FileUtil.init(webappRoot); //needed
		ProjectManager.getProjectManager().setRealPath(webappRoot);	
	}
	
	public UserData validateUser(String name, String password) {
		if (!ProjectManager.getProjectManager().getMetaProjectManager().hasValidCredentials(name, password)) {
			return null;
		}
		
		Log.getLogger().info("User " + name + " logged in at: " + new Date());		
		return new UserData(name, password);
	}	

	
	public UserData registerUser(String name, String password) {
		throw new UnsupportedOperationException("Operation not implemented yet"); //TODO - implement
	}
	
	public ArrayList<ProjectData> getProjects(String user) {
		return ProjectManager.getProjectManager().getMetaProjectManager().getProjectsData(user);
	}

	public void refreshMetaproject() {
		ProjectManager.getProjectManager().getMetaProjectManager().reloadMetaProject();		
	}

	
}
