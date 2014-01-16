package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.ui.Anchor;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
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
		//TODO remove this!!!!!!!
		//widgetConfiguration.put(FormConstants.INSERT_COPY, true);
		
		super.setup(widgetConfiguration, propertyEntityData);
		
	}

	
	@Override
	protected Anchor createAddNewValueHyperlink() {
		return null;
	}
	
	@Override
	protected Anchor createAddExistingHyperlink() {
		return super.createAddExistingHyperlink();
	}
	
	@Override
	protected Anchor createReplaceNewValueHyperlink() {
		return null;
	}
	
	@Override
	protected Anchor createReplaceExistingHyperlink() {
		// TODO Auto-generated method stub
		return super.createReplaceExistingHyperlink();
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



}
