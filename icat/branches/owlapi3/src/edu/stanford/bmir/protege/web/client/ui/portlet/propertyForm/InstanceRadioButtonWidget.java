package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Collection;
import java.util.Collections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.RadioButton;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class InstanceRadioButtonWidget extends InstanceCheckBoxWidget {

    public InstanceRadioButtonWidget(Project project) {
        super(project);
    }

    @Override
    protected CheckBox createCheckComponent(String name, String label) {
        return new RadioButton(name, label);
    }

    @Override
    protected void valueChanged(CheckBox radioButton) {
        if (UIUtil.confirmOperationAllowed(getProject())) {
            String newValue = radioButton.getElement().getId();
            setPropertyValues(getSubject().getName(), getProperty().getName(), ValueType.Instance, new EntityData(newValue),
                    getSetValuesOperationDescription(values, newValue));
        } else {
            refresh();
        }
    }
    
    protected String getSetValuesOperationDescription(Collection<EntityData> oldValue, Object newValue) {
        return UIUtil.getAppliedToTransactionString("Set " + UIUtil.getShortName(getProperty().getBrowserText())
                + " of " + getSubject().getBrowserText() + ". Old value: "
                + (oldValue == null || oldValue.size() == 0|| oldValue.toString().length() == 0 ? "(empty)" : oldValue) + " "
                + ". New value: " + (newValue == null || newValue.toString().length() == 0 ? "(empty)" : newValue),
                getSubject().getName());
    }

    protected void setPropertyValues(String entityName, String propName, ValueType propValueType,
            EntityData newEntityData, String operationDescription) {
        propertyValueUtil.setPropertyValues(getProject().getProjectName(), entityName, propName, propValueType,
                Collections.singleton(newEntityData.getName()), GlobalSettings
                        .getGlobalSettings().getUserName(), operationDescription, 
                        new SetPropertyValuesHandler(newEntityData));
    }

    /*
     * Remote calls
     */
    
    class SetPropertyValuesHandler extends AbstractAsyncHandler<Void> {
        
        private Collection<EntityData> newValues;
        
        public SetPropertyValuesHandler(EntityData newEntityData) {
            this.newValues = Collections.singleton(newEntityData);
        }
        
        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at setting property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            Window.alert("There was an error at setting the property value for " + getSubject().getBrowserText() + ".");
            refresh();
        }
        
        @Override
        public void handleSuccess(Void result) {
            values = newValues;
        }
    }

}
