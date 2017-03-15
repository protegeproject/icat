package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.AllowedPostcoordinationValuesData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.PrecoordinationClassExpressionData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;

public class WHOFICServiceManager {

    private static WHOFICServiceAsync proxy;
    static WHOFICServiceManager instance;

    public static WHOFICServiceManager getInstance() {
        if (instance == null) {
            instance = new WHOFICServiceManager();
        }
        return instance;
    }

    protected WHOFICServiceManager() {
        proxy = GWT.create(WHOFICService.class);
    }

    public void getEntityPropertyValuesForLinearization(String projectName, List<String> entities, String property,
            List<String> reifiedProps, int[] subjectEntityColumns, AsyncCallback<List<EntityPropertyValuesList>> cb) {
        proxy.getEntityPropertyValuesForLinearization(projectName, entities, property, reifiedProps, subjectEntityColumns, cb);
    }

    public void getSecondaryAndInheritedTags(String projectName, String clsName, AsyncCallback<List<EntityPropertyValues>> cb) {
        proxy.getSecondaryAndInheritedTags(projectName, clsName, cb);
    }

    public void getSubclasses(String projectName, String className, AsyncCallback<List<SubclassEntityData>> cb) {
        proxy.getSubclasses(projectName, className, cb);
    }

    public void changeIndexType(String projectName, String subject, String indexEntity, List<String> reifiedProps, String indexType, AsyncCallback<EntityPropertyValues> cb) {
        proxy.changeIndexType(projectName, subject, indexEntity, reifiedProps, indexType, cb);
    }

    public void changeInclusionFlagForIndex(String projectName, String subject, String indexEntity, List<String> reifiedProps, boolean isInclusionFlag, AsyncCallback<EntityPropertyValues> cb) {
        proxy.changeInclusionFlagForIndex(projectName, subject, indexEntity, reifiedProps, isInclusionFlag, cb);
    }

    public void removeBaseIndexTerm(String projectName, String entityName,
            String value, String user, String operationDescription, AsyncCallback<Void> cb) {
        proxy.removeBaseIndexTerm(projectName, entityName, value, user, operationDescription, cb);
    }

    public void getListOfSelectedPostCoordinationAxes(String projectName, String entity,
    		List<String> reifiedProps, AsyncCallback<List<String>> cb) {
    	proxy.getListOfSelectedPostCoordinationAxes(projectName, entity, reifiedProps, cb);
    }

	public void getEntityPropertyValuesForPostCoordinationAxes(String projectName, List<String> entities, List<String> properties,
			List<String> reifiedProps, AsyncCallback<List<EntityPropertyValues>> cb) {
		proxy.getEntityPropertyValuesForPostCoordinationAxes(projectName, entities, properties, reifiedProps, cb);
	}

    public void addAllowedPostCoordinationAxis(String projectName, String subject,
			String postcoordinationEntity, String postcoordinationProperty,
			boolean isRequiredFlag, AsyncCallback<Boolean> cb) {
    	proxy.addAllowedPostCoordinationAxis(projectName, subject,
    			postcoordinationEntity, postcoordinationProperty, isRequiredFlag, cb);
    }

    public void removeAllowedPostCoordinationAxis(String projectName, String subject,
    		String postcoordinationEntity, String postcoordinationProperty, AsyncCallback<Boolean> cb) {
    	proxy.removeAllowedPostCoordinationAxis(projectName, subject,
    			postcoordinationEntity, postcoordinationProperty, cb);
    }

    public void getPostCoordinationAxesScales(String projectName,
    		List<String> properties, AsyncCallback<List<ScaleInfoData>> cb) {
    	proxy.getPostCoordinationAxesScales(projectName, properties, cb);
    }

	public void getAllSuperEntities(String projectName, EntityData entity, AsyncCallback<List<EntityData>> cb) {
		proxy.getAllSuperEntities(projectName, entity, cb);
	}
	
	public void getPreCoordinationClassExpressions(String projectName, String entity, 
			List<String> properties, AsyncCallback<List<PrecoordinationClassExpressionData>> cb) {
		proxy.getPreCoordinationClassExpressions(projectName, entity, properties, cb);
	}
	
	public void getAllowedPostCoordinationValues(String projectName, String entity, 			 
			List<String> customScaleProperties, List<String> treeValueProperties, List<String> fixedScaleProperties, 
			AsyncCallback<List<AllowedPostcoordinationValuesData>> cb) {
		proxy.getAllowedPostCoordinationValues(projectName, entity, 
				customScaleProperties, treeValueProperties, fixedScaleProperties, cb);
	}
	
	public void setPrecoordinationPropertyValue(String projectName,
			String entity, String property, 
			EntityData oldValue, EntityData newValue, AsyncCallback<Boolean> cb) {
		proxy.setPrecoordinationPropertyValue(projectName, entity, property, oldValue, newValue, cb);
	}

	public void changeIsDefinitionalFlag(String projectName, String entity,
			String property, boolean isDefinitionalFlag, AsyncCallback<Boolean> cb) {
		proxy.changeIsDefinitionalFlag(projectName, entity, property, isDefinitionalFlag, cb);
	}

	public void reorderSiblings(String projectName, String movedClass, String targetClass, boolean isBelow, String parent,
            AsyncCallback<Boolean> cb) {
        proxy.reorderSiblings(projectName, movedClass, targetClass, isBelow, parent, cb);
    }

	public void createInternalReference(String projectName, EntityData entity,
			String referenceClassName,
			String referencePropertyName,
			String referencedValuePropertyName,
			EntityData referencedEntity, String user,
			String operationDescription, AsyncCallback<EntityData> callback) {
		proxy.createInternalReference(projectName, entity, referenceClassName, referencePropertyName,
				referencedValuePropertyName, referencedEntity, user, operationDescription, callback);
	}
}
