package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;

public class ICDServiceManager {

    private static ICDServiceAsync proxy;
    static ICDServiceManager instance;

    public static ICDServiceManager getInstance() {
        if (instance == null) {
            instance = new ICDServiceManager();
        }
        return instance;
    }

    private ICDServiceManager() {
        proxy = (ICDServiceAsync) GWT.create(ICDService.class);
    }

    public void createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String sortingLabel,
               boolean createICDSpecificEntities, String user, String operationDescription, String reasonForChange, AsyncCallback<EntityData> cb) {
        proxy.createICDCls(projectName, clsName, superClsNames, title, sortingLabel, createICDSpecificEntities, user, operationDescription, reasonForChange, cb);
    }

    public void getEntityPropertyValuesForLinearization(String projectName, List<String> entities, List<String> properties,
            List<String> reifiedProps, AsyncCallback<List<EntityPropertyValues>> cb) {
        proxy.getEntityPropertyValuesForLinearization(projectName, entities, properties, reifiedProps, cb);
    }

    public void exportICDBranch(String projectName, String topNode, String userName,  AsyncCallback<String> cb){
        proxy.exportICDBranch(projectName, topNode, userName, cb);
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

    public void reorderSiblings(String projectName, String movedClass, String targetClass, boolean isBelow, String parent,
            AsyncCallback<Boolean> cb) {
        proxy.reorderSiblings(projectName, movedClass, targetClass, isBelow, parent, cb);
    }
}
