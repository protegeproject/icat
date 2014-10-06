package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.ui.Anchor;
import com.gwtext.client.widgets.MessageBox;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.InstanceGridWidgetConstants;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.MultilevelInstanceGridWidget;

public class ScaleValueEditorWidget extends MultilevelInstanceGridWidget {

    private static int OFFSET_DELETE_COLUMN = -1;//-1
    private static int OFFSET_COMMENT_COLUMN = 1;//1
    private static int OFFSET_MAX_COLUMN = OFFSET_COMMENT_COLUMN;

	public ScaleValueEditorWidget(Project project) {
		super(project);
	}

	@Override
	public void setup(Map<String, Object> widgetConfiguration,
			PropertyEntityData propertyEntityData) {
		widgetConfiguration.put(InstanceGridWidgetConstants.ADD_EXISTING_VALUE, true);
		widgetConfiguration.put(InstanceGridWidgetConstants.REPLACE_WITH_EXISTING_VALUE, true);
		widgetConfiguration.put(InstanceGridWidgetConstants.ADD_NEW_VALUE, false);
		widgetConfiguration.put(InstanceGridWidgetConstants.REPLACE_WITH_NEW_VALUE, true);
		
		super.setup(widgetConfiguration, propertyEntityData);
		
	}

	
	@Override
	protected Anchor createAddNewValueHyperlink() {
		return null;
	}
	
	@Override
	protected Anchor createAddExistingHyperlink() {
        final Anchor addExistingLink = super.createAddExistingHyperlink();

        final Map<String, Object> widgetConfiguration = getWidgetConfiguration();
        final ProjectConfiguration projectConfiguration = getProject().getProjectConfiguration();
        addExistingLink.setHTML(
                InstanceGridWidgetConstants.getIconLink(
                        InstanceGridWidgetConstants.getAddExistingValueActionDesc(widgetConfiguration, projectConfiguration, "Select scale"),
                        InstanceGridWidgetConstants.getAddIcon(widgetConfiguration, projectConfiguration)));

        return addExistingLink;
	}
	
	@Override
	protected Anchor createReplaceNewValueHyperlink() {
        final Anchor replaceNewValueHyperlink = super.createReplaceNewValueHyperlink();

        final Map<String, Object> widgetConfiguration = getWidgetConfiguration();
        final ProjectConfiguration projectConfiguration = getProject().getProjectConfiguration();
        replaceNewValueHyperlink.setHTML(
                InstanceGridWidgetConstants.getIconLink(
                        InstanceGridWidgetConstants.getReplaceNewValueActionDesc(widgetConfiguration, projectConfiguration, "Delete scale"),
                        InstanceGridWidgetConstants.getReplaceIcon(widgetConfiguration, projectConfiguration)));
        return replaceNewValueHyperlink;
	}
	
	@Override
	protected Anchor createReplaceExistingHyperlink() {
        final Anchor replaceExistingLink = super.createReplaceExistingHyperlink();

        final Map<String, Object> widgetConfiguration = getWidgetConfiguration();
        final ProjectConfiguration projectConfiguration = getProject().getProjectConfiguration();
        replaceExistingLink.setHTML(
                InstanceGridWidgetConstants.getIconLink(
                        InstanceGridWidgetConstants.getReplaceExistingValueActionDesc(widgetConfiguration, projectConfiguration, "Select another scale"),
                        InstanceGridWidgetConstants.getReplaceIcon(widgetConfiguration, projectConfiguration)));

        return replaceExistingLink;
	}
	
    @Override
    protected int getOffsetDeleteColumn() {
        return OFFSET_DELETE_COLUMN;
    }

    @Override
    protected int getOffsetCommentColumn() {
        return OFFSET_COMMENT_COLUMN;
    }

    @Override
    protected int getMaxColumnOffset() {
        return OFFSET_MAX_COLUMN;
    }

    @Override
    protected int getExtraColumnCount() {
        return OFFSET_MAX_COLUMN + 1;   //1 for the instance field
    }
    
    @Override
    protected String getAddExistingOperationDescription(EntityData value) {
        return super.getAddExistingOperationDescription(value).replace("Added ", "Added (a copy of) ");
    }


    @Override
    protected void onAddExistingValue() {
    	if (isLoading()) {
    		MessageBox.alert("Warning", "Server is still busy. Try again soon.");
    	}
    	else {
    		super.onAddExistingValue();
    	}
    };
    
    @Override
    protected void addExistingValues(Collection<EntityData> values) {
    	setLoadingStatus(true);
        //TODO: later optimize this in a single remote call
        for (EntityData value : values) {
            OntologyServiceManager.getInstance().addPropertyValue(
            		getProject().getProjectName(), getSubject().getName(), getProperty(), value, true, 
                    GlobalSettings.getGlobalSettings().getUserName(), getAddExistingOperationDescription(value), 
                    new AddExistingValueHandler(getSubject()));
        }
    }

    @Override
    protected void onReplaceNewValue() {
    	//Hijacking onReplaceNewValue for deletion
    	super.onDelete(0);
    }

    @Override
    protected void removeRowFromStore(int removeInd) {
    	getStore().removeAll();
		updateActionLinks(isReplace());
    	getShadowStore().removeAll();
	}
}
