package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.Field;

import edu.stanford.bmir.protege.web.client.model.Project;


public class CheckBoxWidget extends AbstractFieldWidget {

    public CheckBoxWidget(Project project) {
        super(project);
    }


    @Override
    protected Field createFieldComponent() {
       return new Checkbox();
    }

}
