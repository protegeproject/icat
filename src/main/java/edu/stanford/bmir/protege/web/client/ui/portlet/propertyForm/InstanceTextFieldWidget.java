package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.TextField;
import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.PropertyValueUtil;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

import java.util.*;

public class InstanceTextFieldWidget extends TextFieldWidget {
	private String property;

	public InstanceTextFieldWidget(Project project) {
		super(project);
	}

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        String instProperty = (String) widgetConfiguration.get(FormConstants.PROPERTY);
        if (instProperty == null) {
            GWT.log("Wrong widget configuration for " + propertyEntityData.getName()
            		+ ". Property \"" + FormConstants.PROPERTY + "\" must be specified!", null);
            //FIXME this is only for experimenting purposes.
            //See if we can set a good default value on the client or we deal with this case on the server
            instProperty = ":NAME";
        }
        property = instProperty;
    }

    @Override
    public Field createFieldComponent() {
    	TextField textfield = new TextField();
    	return textfield;
    }

    protected void displayValue(Collection<EntityData> value) {
        Field field = getField();
        if (value == null || value.size() == 0) {
            field.setValue("");
        } else if (value.size() == 1) {
            field.setValue(UIUtil.getDisplayText(UIUtil.getFirstItem(value)));
        } else {
            String displayText = UIUtil.commaSeparatedList(value);
            if (displayText == null){
                displayText = "";
            }
            field.setValue(displayText);
        }
    }

    @Override
    protected void displayValues() { //do nothing

    }

    @Override
    protected void fillValues(List<String> subjects, List<String> props) {
        displayValue(null);
        List<String> reifiedProps = new ArrayList<String>();
        reifiedProps.add(property);
        OntologyServiceManager.getInstance().getEntityTriples(getProject().getProjectName(), subjects, props, reifiedProps,
                new GetTriplesHandler());
    }


    /*
     * Remote calls
     */

    class GetTriplesHandler extends AbstractAsyncHandler<List<Triple>> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Instance Text Field Widget: Error at getting triples for " + getSubject(), caught);
        }

        @Override
        public void handleSuccess(List<Triple> triples) {
            List<EntityData> dispValues = null;
            Set<EntityData> subjects = null;
            if (triples != null) {
                dispValues = new ArrayList<EntityData>();
                subjects = new HashSet<EntityData>();
                for (Triple triple : triples) {
                    dispValues.add(triple.getValue());
                    subjects.add(triple.getEntity());
                }
            }
            setOldDisplayedSubject(getSubject());
            setValues(subjects);
            displayValue(dispValues);
            setLoadingStatus(false);
        }
    }


    @Override
    protected void deletePropertyValue(String entityName, String propName, ValueType propValueType,
            EntityData oldEntityData, String operationDescription) {
        propertyValueUtil.deletePropertyValue(getProject().getProjectName(), entityName, propName, propValueType,
                oldEntityData.getName(), GlobalSettings.getGlobalSettings().getUserName(), operationDescription,
                new RemoveInstancePropertyValueHandler());
    }

    @Override
    protected void replacePropertyValue(String entityName, String propName, ValueType propValueType,
            EntityData oldEntityData, EntityData newEntityData, String operationDescription) {
        String selSubject = null;
        Collection<EntityData> values = getValues();
        if (values != null && ! values.isEmpty()) {
        	selSubject = values.toArray(new EntityData[0])[0].getName();
        }
        if (selSubject != null) {
            new PropertyValueUtil().replacePropertyValue(getProject().getProjectName(), selSubject,
                    property, null, oldEntityData.toString(), newEntityData
                            .toString(), GlobalSettings.getGlobalSettings().getUserName(),
                    //getReplaceValueOperationDescription(properties.get(colIndex), oldValue, newValue),
                    operationDescription,
                    new ReplacePropertyValueHandler(
                            new EntityData(selSubject)));
        }
    }

    @Override
    //TODO: assume value value type is the same as the property value type, fix later
    protected void addPropertyValue(String entityName, String propName, ValueType propValueType,
            EntityData newEntityData, String operationDescription) {
        List<EntityData> allowedValues = getProperty().getAllowedValues();
        String type = null;
        if (allowedValues != null && !allowedValues.isEmpty()) {
            type = allowedValues.iterator().next().getName();
        }

        OntologyServiceManager.getInstance().createInstanceValueWithPropertyValue(getProject().getProjectName(), null, type,
        		getSubject().getName(), propName, new PropertyEntityData(property), newEntityData,
        		GlobalSettings.getGlobalSettings().getUserName(),
        		operationDescription, new AddInstanceValueWithPropertyValueHandler());
    }

    /*
     * Remote calls
     */
    class RemoveInstancePropertyValueHandler extends RemovePropertyValueHandler {

        @Override
        public void handleSuccess(Void result) {
            GWT.log("Success at removing value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);
            setValues(new ArrayList<EntityData>());
            displayValue(new ArrayList<EntityData>());
        }

    }

    class ReplaceInstancePropertyValueHandler extends ReplacePropertyValueHandler {

        private EntityData newEntityData;

        public ReplaceInstancePropertyValueHandler(EntityData newEntityData) {
            super(newEntityData);
        }

        @Override
        public void handleSuccess(Void result) {
            GWT.log("Success at setting value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);
            //FIXME: we need a reload method
            Collection<EntityData> ed = new ArrayList<EntityData>();
            ed.add(newEntityData);
            setValues(ed);
        }
    }

    class AddInstanceValueWithPropertyValueHandler extends AbstractAsyncHandler<EntityData> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at adding instance property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            MessageBox.alert("There was an error at adding the instance property value for " + getSubject().getBrowserText()
                    + ".");
        }

        @Override
        public void handleSuccess(EntityData newEntityData) {
            if (newEntityData == null) {
                GWT.log("Error at adding instance property for " + getProperty().getBrowserText() + " and "
                        + getSubject().getBrowserText(), null);
                return;
            }

            GWT.log("Success at adding value for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), null);
            //FIXME: we need a reload method
            Collection<EntityData> ed = new ArrayList<EntityData>();
            ed.add(newEntityData);
            setValues(ed);
        }
    }

}
