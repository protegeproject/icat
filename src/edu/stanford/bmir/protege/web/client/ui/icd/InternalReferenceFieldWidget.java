package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceGridWidgetConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.ReferenceFieldWidget;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

/**
 * Creates an internal reference to an entity in the same ontology.
 * The selection of values (in case of classes) are children in a
 * ontology tree starting with top_class.
 * Creates an instance of a reference term (e.g., InteralReferenceTerm),
 * and sets a reference value property to the entity itself. 
 * Does not set label or other properties.
 * 
 * In the forms configuration: "internalreference"
 * 
 */
public class InternalReferenceFieldWidget extends ReferenceFieldWidget {
	private Window selectWindow;
	private Selectable selectable;
	private String topClass;
	private String termClass;
	private String referencedValueProperty;


	public InternalReferenceFieldWidget(Project project) {
		super(project);
	}

	@Override
	public Anchor createAddNewValueHyperlink() {
		return null;
	}

	@Override
	protected Anchor createReplaceNewValueHyperlink() {
		return null;
	}

	@Override
	protected Anchor createReplaceExistingHyperlink() {
		return null;
	}

	@Override
	protected Anchor createAddExistingHyperlink() {
		final Map<String, Object> widgetConfiguration = getWidgetConfiguration();
		final ProjectConfiguration projectConfiguration = getProject().getProjectConfiguration();
		Anchor addNewLink = new Anchor(
				InstanceGridWidgetConstants.getIconLink(
						InstanceGridWidgetConstants.getAddExistingValueActionDesc(widgetConfiguration, projectConfiguration, "Select value"),
						InstanceGridWidgetConstants.getAddIcon(widgetConfiguration, projectConfiguration)), true);
		addNewLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (isWriteOperationAllowed()){
					PropertyEntityData prop = getProperty();
					onAddNewReference(prop == null ? "" : UIUtil.getDisplayText(prop));
				}
			}
		});

		return addNewLink;
	}


	@Override
	public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
		super.setup(widgetConfiguration, propertyEntityData);
		this.topClass = (String) widgetConfiguration.get(FormConstants.TOP_CLASS);
		this.termClass = (String)widgetConfiguration.get(FormConstants.ONT_TYPE);
		this.referencedValueProperty = UIUtil.getStringConfigurationProperty(widgetConfiguration, 
				getProject().getProjectConfiguration(), FormConstants.REFERENCED_VALUE_PROP, null);
	}

	private void onAddNewReference(String string) {
		Window window = getSelectionWindow();
		window.show();
	}

	protected Window getSelectionWindow() {
		if (selectWindow == null) {
			selectWindow = new Window();
			selectWindow.setTitle("Select values");
			selectWindow.setWidth(600);
			selectWindow.setHeight(480);
			selectWindow.setMinWidth(300);
			selectWindow.setMinHeight(350);
			selectWindow.setLayout(new FitLayout());
			selectWindow.setPaddings(5);
			selectWindow.setButtonAlign(Position.CENTER);

			selectWindow.setCloseAction(Window.HIDE);
			selectWindow.setPlain(true);

			Button cancelButton = new Button("Cancel");
			cancelButton.addListener(new ButtonListenerAdapter() {
				@Override
				public void onClick(Button button, EventObject e) {
					selectWindow.hide();
				}
			});

			Button selectButton = new Button("Select");
			selectButton.addListener(new ButtonListenerAdapter() {
				@Override
				public void onClick(Button button, EventObject e) {
					Collection<EntityData> selection = getSelectable().getSelection();
					if (selection == null || selection.size() == 0) {
						MessageBox.alert("No selection", "No class selected. Please select a class from the tree.");
						return;
					}
					final Collection<EntityData> selected = selectable.getSelection();

					for (EntityData data : selected) {
						ICDServiceManager.getInstance().createInternalReference(getProject().getProjectName(),
								getSubject(), 
								InternalReferenceFieldWidget.this.termClass,
								InternalReferenceFieldWidget.this.getProperty().getName(),
								InternalReferenceFieldWidget.this.referencedValueProperty, 
								data, 
								GlobalSettings.getGlobalSettings().getUserName(),
								getTransactionString(), new ImportInternalReferenceHandler(selectWindow));
					}

					selectWindow.hide();
				}
			});

			selectWindow.add((Component) getSelectable());
			selectWindow.addButton(selectButton);
			selectWindow.addButton(cancelButton);
		}
		return selectWindow;
	}

	protected String getTransactionString() {
		return UIUtil.getAppliedToTransactionString("Added internal reference " + UIUtil.getDisplayText(getSubject())
				+ ", for property: " + getWidgetConfiguration().get(FormConstants.LABEL) + " and class: " + getSubject().getBrowserText(),
				getSubject().getName());
	}
	
	
	public Selectable getSelectable() {
		if (selectable == null) {
			ClassTreePortlet selectableTree = new ICDClassTreePortlet(getProject(), true, false, false, true, topClass);
			selectableTree.disableCreate();
			selectableTree.setDraggable(false);
			selectableTree.setClosable(false);
			selectableTree.setCollapsible(false);
			selectableTree.setHeight(300);
			selectableTree.setWidth(450);
			selectable = selectableTree;
		}
		return selectable;
	}
	
	/*
	 * Remote calls
	 */

	class ImportInternalReferenceHandler extends AbstractAsyncHandler<EntityData> {
		private Window selectWindow;

		public ImportInternalReferenceHandler(Window selectWindow) {
			this.selectWindow = selectWindow;
		}

		@Override
		public void handleFailure(Throwable caught) {
			selectWindow.getEl().unmask();
			GWT.log("Could not create internal reference ", null);
			MessageBox.alert("Internal reference generation failed!");
		}

		@Override
		public void handleSuccess(EntityData refInstance) {
			selectWindow.getEl().unmask();
			getProject().forceGetEvents();
			if (refInstance != null) {
				refresh();
			} else {
				MessageBox.alert("Internal reference operation DID NOT SUCCEED!");
			}
		}
	}

}
