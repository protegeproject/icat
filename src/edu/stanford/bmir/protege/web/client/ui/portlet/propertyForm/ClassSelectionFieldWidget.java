package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Map;

import com.gwtext.client.widgets.Window;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.generated.ClassTreeFactory;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class ClassSelectionFieldWidget extends AbstractSelectionFieldWidget {

	private String topClass;

	public ClassSelectionFieldWidget(Project project) {
		super(project);		
	}

	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {
		super.setup(widgetConfiguration, propertyEntityData);
        resetTopClass();
	}

	public void resetTopClass() {
		this.topClass = UIUtil.getStringConfigurationProperty(getWidgetConfiguration(), 
				getProject().getProjectConfiguration(), FormConstants.TOP_CLASS, null);
	}

	public void setTopClass(String topClass) {
		this.topClass = topClass;
	}

	@Override
	public void setWindowSize(Window window) {
        window.setWidth(450);
        window.setHeight(300);
    }
	
	public Selectable createSelectable() {
		ClassTreePortlet classTreePortlet = ClassTreeFactory.getICDClassTreePortlet(getProject(), true, false, false, false, topClass);

		classTreePortlet.disableCreate();
		classTreePortlet.setDraggable(false);
		classTreePortlet.setClosable(false);
		classTreePortlet.setCollapsible(false);

		return classTreePortlet;
	}
	
	
}
