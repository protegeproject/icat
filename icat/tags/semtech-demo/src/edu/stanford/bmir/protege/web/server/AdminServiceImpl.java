package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.AdminService;
import edu.stanford.bmir.protege.web.client.rpc.data.ProjectData;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.smi.protege.server.metaproject.Operation;
import edu.stanford.smi.protege.util.Log;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Administrative services for user management
 *
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class AdminServiceImpl extends RemoteServiceServlet
implements AdminService {

    private static final long serialVersionUID = 7616699639338297327L;
    private static final String USER_DATA_PARAMETER = "user.name";

    public UserData validateUser(String name, String password) {
        HttpServletRequest request = this.getThreadLocalRequest();
        final HttpSession session = request.getSession();
        final UserData currentUserData = (UserData) session.getAttribute(USER_DATA_PARAMETER);
        if (currentUserData != null && currentUserData.getName().equals(name)){
            return currentUserData;
        }
        if (!Protege3ProjectManager.getProjectManager().getMetaProjectManager().hasValidCredentials(name, password)) {
            return null;
        }
        Log.getLogger().info("User " + name + " logged in at: " + new Date());
        final UserData userData = new UserData(name, password);
        session.setAttribute(USER_DATA_PARAMETER, userData);
        return userData;
    }


    public String getCurrentUserInSession() {
        HttpServletRequest request = getThreadLocalRequest();
        final HttpSession session = request.getSession();
        final UserData userData = (UserData) session.getAttribute(USER_DATA_PARAMETER);
        return userData == null ? null: userData.getName();
    }

    public void logout() {
        HttpServletRequest request = getThreadLocalRequest();
        final HttpSession session = request.getSession();
        session.setAttribute(USER_DATA_PARAMETER, null);
    }

    public UserData registerUser(String userName, String password) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().registerUser(userName, password);
    }

    public void changePassword(String userName, String password) {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().changePassword(userName, password);
    }

    public String getUserEmail(String userName) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
    }

    public void setUserEmail(String userName, String email) {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().setUserEmail(userName, email);
    }

    public ArrayList<ProjectData> getProjects(String user) {
        return Protege3ProjectManager.getProjectManager().getMetaProjectManager().getProjectsData(user);
    }

    public Collection<String> getAllowedOperations(String project, String user) {
        Collection<Operation> ops = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getAllowedOperations(project, user);
        Collection<String> opsAsString = new ArrayList<String>();
        for (Operation op : ops) {
            opsAsString.add(op.getName());
        }
        return opsAsString;
    }

    public Collection<String> getAllowedServerOperations(String userName) {
        Collection<Operation> ops = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getAllowedServerOperations(userName);
        Collection<String> opsAsString = new ArrayList<String>();
        for (Operation op : ops) {
            opsAsString.add(op.getName());
        }
        return opsAsString;
    }

    public void refreshMetaproject() {
        Protege3ProjectManager.getProjectManager().getMetaProjectManager().reloadMetaProject();
    }

	public void sendPasswordReminder(String userName) {
	    String email = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getUserEmail(userName);
	    if (email == null) {
	        throw new IllegalArgumentException("User " + userName + " does not have an email configured.");
	    }
	    changePassword(userName, EmailConstants.RESET_PASSWORD);
		EmailUtil.sendEmail(email, EmailConstants.FORGOT_PASSWORD_SUBJECT, EmailConstants.FORGOT_PASSWORD_EMAIL_BODY,
		        ApplicationProperties.getEmailAccount());
	}
}
