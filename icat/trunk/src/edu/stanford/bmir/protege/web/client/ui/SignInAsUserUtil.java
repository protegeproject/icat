package edu.stanford.bmir.protege.web.client.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.core.RegionPosition;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.WindowListenerAdapter;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.BorderLayoutData;
import com.gwtext.client.widgets.layout.VerticalLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.rpc.AdminServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.UserData;

public class SignInAsUserUtil {

	private Set<HandlerRegistration> clickHandlers = new HashSet<HandlerRegistration>();
	
	
	public void signInAs() {
		getUsers();
	}

	
	private void getUsers() {
		String userName = GlobalSettings.getGlobalSettings().getUserName();
		AdminServiceManager.getInstance().getUsers(userName, 
				getUserAsynCall());
		
	}


	private AsyncCallback<List<UserData>> getUserAsynCall() {
		return new AsyncCallback<List<UserData>>() {

			@Override
			public void onFailure(Throwable t) {
				MessageBox.alert("Error at retrieving users", t.getMessage());
			}

			@Override
			public void onSuccess(List<UserData> users) {
				showUsers(users);
			}
		};
	}

	private void showUsers(List<UserData> users) {
		Panel usersPanel = createUsersPanel();
		
		for (UserData user : users) {
			usersPanel.add(createAnchor(user));
		}
	
		Window win = createWindow();
		win.add(usersPanel, new BorderLayoutData(RegionPosition.CENTER));
		win.show();
	}
	

	private Anchor createAnchor(final UserData userData) {
		Anchor anchor = new Anchor(userData.getName(), true);
		// anchor.setStylePrimaryName("export_import_anchor");
		HandlerRegistration reg = anchor.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				signInAsUser(userData);
			}
		});
		clickHandlers.add(reg);
		return anchor;
	}

	private void signInAsUser(UserData userData) {
		GWT.log("Sign in as: " + userData.getName());
		signInAs(userData);
	}


	private Window createWindow() {
		final Window win = new Window();

		win.setLayout(new BorderLayout());
		win.setTitle("Pick a user to sign in as ...");
		win.setClosable(true);
		win.setWidth(300);
		win.setHeight(500);
		win.setPaddings(7);
		win.setCloseAction(Window.HIDE);
		
		win.addListener(new WindowListenerAdapter() {
			@Override
			public void onHide(Component component) {
				removeClickHandlers(win);
				super.onHide(component);
			}
		});
		
		return win;
	}
	
	private Panel createUsersPanel() {
		Panel panel = new Panel();
		panel.setBorder(false);
		panel.setPaddings(15);
		panel.setAutoScroll(true);
		panel.setLayout(new VerticalLayout(10));

		return panel;

	}

	private void removeClickHandlers(Window win) {
		for (HandlerRegistration handler : clickHandlers) {
			handler.removeHandler();
		}
	}
	
	
	/***************** Sign in as ************/
	 
	

	private void signInAs(UserData userData) {
		String currentUser = GlobalSettings.getGlobalSettings().getUserName();
		AdminServiceManager.getInstance().switchUser(currentUser, userData.getName(), 
				new AsyncCallback<UserData>() {

					@Override
					public void onFailure(Throwable caught) {
						MessageBox.alert("Failed to switch user. Message: <br />"
								+ caught.getMessage());
						
					}

					@Override
					public void onSuccess(UserData newUserData) {
						GlobalSettings.getGlobalSettings().setUser(newUserData);
					}
		});
	}
	
	
}
