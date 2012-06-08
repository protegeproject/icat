package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.TextFieldListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.UIUtil;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.util.Project;

public abstract class AbstractFieldWidget extends AbstractPropertyWidget {
	
	private Panel wrappingPanel;
	private Field field;
	private Hyperlink deleteLink;
	private Hyperlink commentLink;
	private Collection<EntityData> values;

	public AbstractFieldWidget(Project project) {
		super(project);	
			
		wrappingPanel = new Panel();
		wrappingPanel.setAutoScroll(true);
		wrappingPanel.setPaddings(5);
		wrappingPanel.setLayout(new ColumnLayout());			
		//wrappingPanel.setStyle("background-color:#AABBCC;");
		
		field = createField();
		deleteLink = createAddHyperlink();		
		commentLink = createCommentHyperLink();
		
		FormPanel formPanel = new FormPanel();
		//formPanel.setStyle("background-color:#BBBBFF;");
		//formPanel.add(field, new AnchorLayoutData("70%"));
		formPanel.add(field, new AnchorLayoutData("100% - 53"));
		
		wrappingPanel.add(formPanel, new ColumnLayoutData(1));
		
		Collection<Widget> suffixComponents = createSuffixComponents();
		for (Iterator<Widget> iterator = suffixComponents.iterator(); iterator.hasNext();) {
			Widget widget = iterator.next();
			wrappingPanel.add(widget);
		}			
		
		wrappingPanel.add(deleteLink);
		wrappingPanel.add(commentLink);			
	}
	
	protected Collection<Widget> createSuffixComponents() { //TODO: might need better name
		return new ArrayList<Widget>();
	}
	
	protected Hyperlink createAddHyperlink () {
		deleteLink = new Hyperlink("&nbsp<img src=\"images/delete.png\"></img>", true, "");		
		deleteLink.setWidth("20px");
		deleteLink.setTitle("Delete this value");
		deleteLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				deletePropertyValue(getSubject().getName(), 
						getProperty().getName(), getProperty().getValueType(), 
						getSingleValue());
			}
		});
		return deleteLink;
	}
	
	protected Field createField() {
		field = createFieldComponent();	
		field.addListener(new TextFieldListenerAdapter() {
			public void onChange(Field field, Object newVal, Object oldVal) {
				onChangeValue(oldVal, newVal);
			}					
		});
		field.setLabelStyle("width:100px;");
		return field;
	}
	
	
	protected EntityData getSingleValue() {
		Collection<EntityData> vals = getValues();
		if (vals == null) { return null;}
		if (vals.size() > 0) {
			return vals.iterator().next();		
		}
		return null;
	}
	
	protected void onChangeValue(Object oldVal, Object newVal) {
		if (getProject().getUserName().equals("No user")) {
			MessageBox.alert("Please login", "Please login to edit.");
			field.setValue(oldVal == null ? "" : oldVal.toString());
			return;
		}
		GWT.log("on change before invoke " + field.getFieldLabel(), null);
		if (!(newVal instanceof EntityData)) {
			newVal = new EntityData(newVal.toString());
		}
		if (oldVal == null || oldVal.toString().length() == 0) {			
			addPropertyValue(getSubject().getName(), getProperty().getName(), getProperty().getValueType(), (EntityData) newVal);
		} else {
			//TODO: !!!! not robust, store previous value
			if (!(oldVal instanceof EntityData)) {
				oldVal = new EntityData(oldVal.toString());
			}
			replacePropertyValue(getSubject().getName(), getProperty().getName(), getProperty().getValueType(), (EntityData) oldVal, (EntityData) newVal);
		}
	}
	
	protected Hyperlink createCommentHyperLink() {
		commentLink = new Hyperlink("<img src=\"images/comment.gif\"></img>", true, "");		
		commentLink.setWidth("20px");
		commentLink.setTitle("Add a comment on this value");
		commentLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				 MessageBox.show(new MessageBoxConfig() {
                     {
                         setTitle("Comment");
                         setMsg("Please enter a comment on this property value:");
                         setWidth(400);
                         setButtons(MessageBox.OKCANCEL);
                         setMultiline(true);
                         setCallback(new MessageBox.PromptCallback() {
                             public void execute(String btnID, String text) {
                                 
                             }
                         });
                         setAnimEl(field.getId());
                     }
                 });
			}			
		});
		return commentLink;
	}
	
	
	protected Field createFieldComponent() {
		TextField textField = new TextField();
		return textField;
	}

	
	public void setLabel(String label) {
		field.setLabel(label);		
	}
	
	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {	
		super.setup(widgetConfiguration, propertyEntityData);
		String label = (String) widgetConfiguration.get(FormConstants.LABEL);
		setLabel(label == null ? propertyEntityData.getBrowserText() : label);
	}
	
	@Override
	public Collection<EntityData> getValues() {		
		return values;
	}
	
	
	public void setValues(Collection<EntityData> values) {		
		this.values = values;
		if (values == null || values.size() == 0) {		
			field.setValue("");
		} else {
			if (values.size() == 1) {
				EntityData value = values.iterator().next();				
				String displayText = UIUtil.getDisplayText(value);				
				field.setValue(displayText);				
			} else {
				String valuesText = new String();
				for (Iterator<EntityData> iterator = values.iterator(); iterator.hasNext();) {
					EntityData object = iterator.next();
					String displayText = UIUtil.getDisplayText(object);
					valuesText = displayText + ", ";
				}
				valuesText = valuesText.substring(0, valuesText.length() - 1);
				field.setValue(valuesText);
			}
		}		
	}

	public Field getField() {
		return field;
	}
	
	@Override
	public Component getComponent() {
		return getWrappingPanel();
	}
	
	public Panel getWrappingPanel() {
		return wrappingPanel;
	}
	
	protected void deletePropertyValue(String entityName, String propName, ValueType propValueType, EntityData oldEntityData) {
		if (propValueType == null) { propValueType = ValueType.String; }
		//EntityData oldEntityData = new EntityData(value);
		oldEntityData.setValueType(propValueType);
		OntologyServiceManager.getInstance().removePropertyValue(getProject().getProjectName(), entityName,
				new PropertyEntityData(propName), oldEntityData, new RemovePropertyValueHandler());
	}
	
	//TODO: assume value value type is the same as the property value type, fix later
	protected void replacePropertyValue(String entityName, String propName, ValueType propValueType, EntityData oldEntityData, EntityData newEntityData) {
		if (propValueType == null) { propValueType = ValueType.String; }
		//EntityData oldEntityData = new EntityData(oldValue);
		oldEntityData.setValueType(propValueType);
		//EntityData newEntityData = new EntityData(newValue);
		newEntityData.setValueType(propValueType);
		OntologyServiceManager.getInstance().replacePropertyValue(getProject().getProjectName(), entityName,
				new PropertyEntityData(propName), oldEntityData, newEntityData, new ReplacePropertyValueHandler(newEntityData));
	}	
	
	//TODO: assume value value type is the same as the property value type, fix later
	protected void addPropertyValue(String entityName, String propName, ValueType propValueType, EntityData newEntityData) {				
		//EntityData newEntityData = new EntityData(newValue);
		newEntityData.setValueType(propValueType);
		OntologyServiceManager.getInstance().addPropertyValue(getProject().getProjectName(), entityName,
				new PropertyEntityData(propName), newEntityData, new AddPropertyValueHandler(newEntityData));
	}	
	
	
	/*
	 * Remote calls
 	 */
	class RemovePropertyValueHandler extends AbstractAsyncHandler<Void> {

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at removing value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), caught);
			Window.alert("There was an error at removing the property value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText() + ".");
			setValues(values);
		}

		@Override
		public void handleSuccess(Void result) {
			GWT.log("Success at removing value for "  + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
			setValues(null);
		}
		
	}
	
	class ReplacePropertyValueHandler extends AbstractAsyncHandler<Void> {

		private EntityData newEntityData;
		
		public ReplacePropertyValueHandler(EntityData newEntityData) {
			this.newEntityData = newEntityData;
		}

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at replace property for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), caught);
			Window.alert("There was an error at setting the property value for " + getSubject().getBrowserText() + ".");
			setValues(values);
		}

		@Override
		public void handleSuccess(Void result) {		
			GWT.log("Success at setting value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
			//FIXME: we need a reload method
			Collection<EntityData> ed = new ArrayList<EntityData>();
			ed.add(newEntityData);
			setValues(ed);
		}
	}
	
	class AddPropertyValueHandler extends AbstractAsyncHandler<Void> {
		
		private EntityData newEntityData;
		
		public AddPropertyValueHandler(EntityData newEntityData) {
			this.newEntityData = newEntityData;
		}

		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at add property for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), caught);
			Window.alert("There was an error at adding the property value for " + getSubject().getBrowserText() + ".");
			setValues(values);
		}

		@Override
		public void handleSuccess(Void result) {		
			GWT.log("Success at adding value for " + getProperty().getBrowserText() + " and " + getSubject().getBrowserText(), null);
			//FIXME: we need a reload method
			Collection<EntityData> ed = new ArrayList<EntityData>();
			ed.add(newEntityData);
			setValues(ed);
		}
	}

}
