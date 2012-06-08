package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.layout.ColumnLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.PropertyValueUtil;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class InstanceCheckBoxWidget extends AbstractPropertyWidget {

    private Map<String, String> label2valuesMap = new LinkedHashMap<String, String>();
    private Map<String, CheckBox> value2checkBox = new LinkedHashMap<String, CheckBox>();

    private Panel wrappingPanel;
    private HTML loadingIcon;

    private ValueChangeHandler<Boolean> valueChangedHandler;

    protected PropertyValueUtil propertyValueUtil;

    protected Collection<EntityData> values;

    public InstanceCheckBoxWidget(Project project) {
        super(project);
    }

    @Override
    public Component createComponent() {
        propertyValueUtil = new PropertyValueUtil();
        valueChangedHandler = getValueChangeHandler();
        wrappingPanel = createWrappingPanel();

        label2valuesMap = (Map<String, String>) getWidgetConfiguration().get(FormConstants.ALLOWED_VALUES);

        if (label2valuesMap == null) { return wrappingPanel; }

        VerticalPanel verticalRadioPanel = new VerticalPanel();
        verticalRadioPanel.setSpacing(5);
        for (String label : label2valuesMap.keySet()) {
            CheckBox checkBox = createCheckComponent(getProperty().getName(), label);
            String value = label2valuesMap.get(label);

            checkBox.getElement().setId(value);
            checkBox.setStyleName("instance-checkbox");
            checkBox.addValueChangeHandler(valueChangedHandler);
            verticalRadioPanel.add(checkBox);
            value2checkBox.put(value, checkBox);
        }

        wrappingPanel.add(verticalRadioPanel);
        return wrappingPanel;
    }

    protected CheckBox createCheckComponent(String name, String label) {
        return new CheckBox(label);
    }

    protected Panel createWrappingPanel() {
        wrappingPanel = new Panel();
        wrappingPanel.setLayout(new ColumnLayout());
        wrappingPanel.setPaddings(5);
        int height = UIUtil.getIntegerConfigurationProperty(getWidgetConfiguration(), FormConstants.HEIGHT, 120);
        wrappingPanel.setHeight(height);

        loadingIcon = new HTML("<img src=\"images/loading.gif\"/>");
        loadingIcon.setStyleName("loading-img");
        loadingIcon.setVisible(false);

        String labelText = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), FormConstants.LABEL, getProperty().getBrowserText());
        Label helpLabel = new Label();
        helpLabel.setHtml(getLabelHtml("", getHelpURL(), getTooltipText()) + AbstractFieldWidget.LABEL_SEPARATOR);

        HorizontalPanel horizLabelPanel = new HorizontalPanel();
        horizLabelPanel.add(new Label(labelText));
        horizLabelPanel.add(loadingIcon);
        horizLabelPanel.add(helpLabel);
        horizLabelPanel.setStyleName("form_label");

        wrappingPanel.add(horizLabelPanel);

        return wrappingPanel;
    }

    protected ValueChangeHandler<Boolean> getValueChangeHandler() {
        if (valueChangedHandler == null) {
            valueChangedHandler = new ValueChangeHandler<Boolean>() {
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    valueChanged((CheckBox)event.getSource());
                }
            };
        }
        return valueChangedHandler;
    }

    protected void valueChanged(CheckBox checkBox) {
        if (UIUtil.confirmOperationAllowed(getProject())) {
            String newValue = checkBox.getElement().getId();
            if (checkBox.getValue() == true) {
                addPropertyValue(getSubject().getName(), getProperty().getName(), ValueType.Instance, new EntityData(newValue),
                    getAddValueOperationDescription(values, newValue));
            }
            else {
                removePropertyValue(getSubject().getName(), getProperty().getName(), ValueType.Instance, new EntityData(newValue),
                        getRemoveValueOperationDescription(values, newValue));
            }

        } else {
            refresh();
        }
    }

    @Override
    public Component getComponent() {
        return wrappingPanel;
    }



    @Override
    protected void fillValues(List<String> subjects, List<String> props) {
        setLoadingStatus(true);
        super.fillValues(subjects, props);
    }

    @Override
    public void setLoadingStatus(boolean loading) {
        super.setLoadingStatus(loading);
        loadingIcon.setVisible(loading);
    }

    @Override
    public void setValues(Collection<EntityData> values) {
        uncheckAll();
        //take only first value
        if (values == null) {
            this.values = null;
            setLoadingStatus(false);
            return;
        }
        for (EntityData value : values) {
            if (value == null) { continue; }

            CheckBox rb = value2checkBox.get(value.getName());
            if (rb != null) {
                rb.setValue(true);
            }
        }
        setLoadingStatus(false);
    }

    protected void uncheckAll() {
        for (CheckBox rb : value2checkBox.values()) {
            rb.setValue(false);
        }
    }

    protected void addPropertyValue(String entityName, String propName, ValueType propValueType,
            EntityData newEntityData, String operationDescription) {
        propertyValueUtil.addPropertyValue(getProject().getProjectName(), entityName, propName, propValueType,
                newEntityData.getName(), GlobalSettings.getGlobalSettings().getUserName(),
                operationDescription, new AddPropertyValueHandler(newEntityData));
    }

    protected void removePropertyValue(String entityName, String propName, ValueType propValueType,
            EntityData newEntityData, String operationDescription) {
        propertyValueUtil.deletePropertyValue(getProject().getProjectName(), entityName, propName, propValueType,
                null, newEntityData.getName(), GlobalSettings.getGlobalSettings().getUserName(),
                        operationDescription, new RemovePropertyValueHandler(newEntityData));
    }


    protected String getAddValueOperationDescription(Object oldValue, Object newValue) {
        return UIUtil.getAppliedToTransactionString("Added " + UIUtil.getShortName(getProperty().getBrowserText())
                + " of " + getSubject().getBrowserText() + ": " +
                (newValue == null || newValue.toString().length() == 0 ? "(nothing)" : newValue),
                getSubject().getName());
    }

    protected String getRemoveValueOperationDescription(Object oldValue, Object newValue) {
        return UIUtil.getAppliedToTransactionString("Removed " + UIUtil.getShortName(getProperty().getBrowserText())
                + " of " + getSubject().getBrowserText() + ": " +
                (newValue == null || newValue.toString().length() == 0 ? "(nothing)" : newValue),
                getSubject().getName());
    }

    /*
     * Remote calls
     */

    class AddPropertyValueHandler extends AbstractAsyncHandler<Void> {

        private EntityData newEntityData;

        public AddPropertyValueHandler(EntityData newEntityData) {
            this.newEntityData = newEntityData;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at adding property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            Window.alert("There was an error at adding the property value for " + getSubject().getBrowserText() + ".");
            refresh();
        }

        @Override
        public void handleSuccess(Void result) {
            if (values == null) {
                values = new ArrayList<EntityData>();
            }
            values.add(newEntityData);
        }
    }

    class RemovePropertyValueHandler extends AbstractAsyncHandler<Void> {

        private EntityData newEntityData;

        public RemovePropertyValueHandler(EntityData newEntityData) {
            this.newEntityData = newEntityData;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at remove property for " + getProperty().getBrowserText() + " and "
                    + getSubject().getBrowserText(), caught);
            Window.alert("There was an error at removing the property value for " + getSubject().getBrowserText() + ".");
            refresh();
        }

        @Override
        public void handleSuccess(Void result) {
            if (values != null) {
                values.remove(newEntityData);
            }
        }
    }

}
