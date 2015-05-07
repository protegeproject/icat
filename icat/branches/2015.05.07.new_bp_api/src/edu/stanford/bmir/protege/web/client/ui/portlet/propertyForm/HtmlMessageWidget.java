package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Collection;

import com.gwtext.client.widgets.Component;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.html.HtmlTextComponent;

public class HtmlMessageWidget extends AbstractPropertyWidget {

	private HtmlTextComponent htmlTextComponent;

    public HtmlMessageWidget(Project project) {
		super(project);		
	}

    @Override
    public Component createComponent() {
        htmlTextComponent = new HtmlTextComponent();
        htmlTextComponent.setConfigProperties(getWidgetConfiguration());
        return htmlTextComponent;
    }

    @Override
    public Component getComponent() {
        return htmlTextComponent;
    }
    
    @Override
    public void setValues(Collection<EntityData> values) {
    	// Do nothing.
    	// This widget is not intended to have values set dynamically    	
    }

}
