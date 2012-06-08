package edu.stanford.bmir.protege.web.client.ui.login;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.KeyListener;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.TextField;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.bmir.protege.web.client.util.Project;


/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class LoginFormPanel extends FormPanel {
	private Panel parent;
	private TextField userID;
	private TextField password;	
	private Project project;
	private Window window;
	private TextField newUserID;
	private TextField newUserPassword;   
	private TextField newUserPassword2;
	private UserData userData = null;
	
	public LoginFormPanel(Panel parent, Project project) {
		this.parent = parent;
		this.project = project;
		init();
	}
	
	private void init() {
		setFrame(true);
		this.parent.setWidth(450);
		//setTitle("Sign in to WebProt\u00E9g\u00E9");
		
		setWidth(400);
		
		Label label = new Label();
		label.setHtml("<br />Welcome! Don't have a User ID? Register as a new user to get a User ID or Enter " +
				"&quot;Guest&quot; and leave the password field blank to " +
				"sign in as a guest user.<br /><br /><br />");
		label.setCls("login-welcome-msg");
		add(label);
		
		userID = new TextField("User ID", "id", 250);  
		userID.setAllowBlank(false);
		add(userID);  

		password = new TextField("Password", "password", 250);
		password.setPassword(true);
		password.setAllowBlank(false);
		// 13 is Javascript Char Code for Enter key
		password.addKeyListener(13, new KeyListener() { 
			public void onKey(int key, EventObject e) {
				performSignIn();
			}
		});
		add(password);  

		Button signInButton = new Button("Sign In", 
				new ButtonListenerAdapter() {
			public void onClick(Button button, EventObject e) {
				performSignIn();
			}
		});
		addButton(signInButton);
		/*
		Button newUserButton = new Button("New User", 
				new ButtonListenerAdapter() {
			public void onClick(Button button, EventObject e) {
				createNewUser();
			}
		});
		addButton(newUserButton);
		
		//TODO: Uncomment when register user works right
		/*
		ClickListener hyperlinkClickListener = new ClickListener() {
			public void onClick(Widget sender) {				
				MessageBox.alert("Please email tudorache _at_ stanford.edu to reset the password.");
			}
		};
		
		Hyperlink forgotPasswordLink = new Hyperlink("Forgot Password", "");
		forgotPasswordLink.setStyleName("discussion-link");
		forgotPasswordLink.addClickListener(hyperlinkClickListener);		
		add(forgotPasswordLink);		 
		 */
		
	}
	
	public String getUserID() {
		return userID.getText();
	}

	public void setUserID(String userID) {
		this.userID.setValue(userID);
	}

	public String getPassword() {
		return password.getText();
	}

	public void setPassword(String password) {
		this.password.setValue(password);
	}

	private void performSignIn() {
		//mask the display during the login period
		parent.getEl().mask("Logging in...", true);
		AdminServiceManager.getInstance().validateUser(getUserID(), 
				getPassword(), new SignIn());
	}
	
	private void createNewUser() {
		
	    final FormPanel formPanel = new FormPanel();
	    
	    window = new Window();
	    window.setTitle("New User Registration");
	    formPanel.setFrame(true);
	    formPanel.setWidth(400);
	    newUserID = new TextField("User ID", "id", 250);  
	    newUserID.setAllowBlank(false);
		formPanel.add(newUserID);  

		newUserPassword = new TextField("Password", "password", 250);
		newUserPassword.setPassword(true);
		newUserPassword.setAllowBlank(false);
		formPanel.add(newUserPassword);  
		
		newUserPassword2 = new TextField("Confirm Password", "password", 250);
		newUserPassword2.setPassword(true);
		newUserPassword2.setAllowBlank(false);
		// 13 is Javascript Char Code for Enter key
		newUserPassword2.addKeyListener(13, new KeyListener() { 
			public void onKey(int key, EventObject e) {
				newUser(newUserID.getText(), newUserPassword.getText(), newUserPassword2.getText());
			}
		});
		formPanel.add(newUserPassword2);  
	    

		//TODO: Commented code because it does not work right.
		//The user is created in the server metaproject, but the validation of user
		//is made against the local metaproject -- need to fix this
/*	    	    
		Button register = new Button("Register", new ButtonListenerAdapter() {   
	        public void onClick(Button button, EventObject e) {
	        	newUser(newUserID.getText(), newUserPassword.getText(), newUserPassword2.getText());        	
	         }   
	    });   
		formPanel.addButton(register);
*/   
	    
	    Button cancel = new Button("Cancel", new ButtonListenerAdapter() {   
	        public void onClick(Button button, EventObject e) {   
	        	window.close();
	         }   
	    });   
	    formPanel.addButton(cancel);
	    
	    window.add(formPanel);
	    window.setWidth(410);
	    window.show();
	    	
	}

	protected void newUser(String name, String password1, String password2) {
				
		if(password1.contentEquals(password2)) {
			AdminServiceManager.getInstance().registerUser(name, password1, new Register());
		} else {
			MessageBox.alert("Passwords dont match. Please try again.");
			newUserPassword.setValue("");
			newUserPassword2.setValue("");
		}	
				
	}
	
	protected void afterRender() {
		super.afterRender();
		userID.focus();
	}
	
	public void getWritePermission(Project project, String userName) {		
		if (project == null || project.getProjectName() == null) { return; }
		OntologyServiceManager.getInstance().hasWritePermission(project.getProjectName(), 
				userName, new GetWritePermission());		
	}


	/*
	 * Remote calls
	 */
	
	
	class SignIn extends AbstractAsyncHandler<UserData> {

		public void handleFailure(Throwable caught) {
			GWT.log("RPC error in LoginFormPanel", caught);
			MessageBox.alert("Cannot login. Please try again.");
			userID.setValue("");
			password.setValue("");
			parent.getEl().unmask();
		}

		public void handleSuccess(UserData ud) {
			parent.getEl().unmask();
			userData = ud;
			if (userData != null) {
				getWritePermission(project, userData.getName());
			} else {				
				MessageBox.alert("Invalid user name or password. Please try again.");
				userID.setValue("");
				password.setValue("");
			}
		}		
	}
	
	class Register extends AbstractAsyncHandler<UserData> {
		public void handleFailure(Throwable caught) {
			GWT.log("RPC error in LoginFormPanel", caught);
			MessageBox.alert("Failed: Server denied the registration request.");
			newUserID.setValue("");
			newUserPassword.setValue("");
			newUserPassword2.setValue("");
			parent.getEl().unmask();
		}

		public void handleSuccess(UserData userData) {
			parent.getEl().unmask();			
			if (userData != null) {
				userID.setValue(userData.getName());
				password.setValue(userData.getPassword());					
				window.close();
			} else {	
				MessageBox.alert("Failed: New user registration could not be completed. Please try again.");					
				newUserID.setValue("");
				newUserPassword.setValue("");
				newUserPassword2.setValue("");
			}
		}
	}	
	
	class GetWritePermission extends AbstractAsyncHandler<Boolean> {
			
		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at getting write permission flag from server", caught);			
		}

		@Override
		public void handleSuccess(Boolean result) {
			GWT.log("Get write permission for " + project.getUserName() + " result: " + result, null);			
			project.setHasWritePermission(result.booleanValue());
			if (userData != null) {
				project.setUserName(userData.getName());
			}
			parent.hide();
		}		
	}
	
	
}
