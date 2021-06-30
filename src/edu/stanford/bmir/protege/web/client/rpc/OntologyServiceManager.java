package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.AnnotationData;
import edu.stanford.bmir.protege.web.client.rpc.data.ConditionItem;
import edu.stanford.bmir.protege.web.client.rpc.data.ConditionSuggestion;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.rpc.data.PaginationData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyPairs;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;

public class OntologyServiceManager {

    private static OntologyServiceAsync proxy;
    static OntologyServiceManager instance;

    public static OntologyServiceManager getInstance() {
        if (instance == null) {
            instance = new OntologyServiceManager();
        }
        return instance;
    }

    private OntologyServiceManager() {
        proxy = (OntologyServiceAsync) GWT.create(OntologyService.class);
    }

    /*
     * Project management methods
     */

    public void loadProject(String projectName, AsyncCallback<Integer> cb) {
        proxy.loadProject(projectName, cb);
    }


    public void hasWritePermission(String projectName, String userName, AsyncCallback<Boolean> cb) {
        proxy.hasWritePermission(projectName, userName, cb);
    }

    /*
     * Ontology methods
     */

    public void getAnnotationProperties(String projectName, String entityName, AsyncCallback<List<AnnotationData>> cb) {
        proxy.getAnnotationProperties(projectName, entityName, cb);
    }

    public void getImportedOntologies(String projectName, AsyncCallback<ImportsData> cb) {
        proxy.getImportedOntologies(projectName, cb);
    }

    public void getMetrics(String projectName, AsyncCallback<List<MetricData>> cb) {
        proxy.getMetrics(projectName, cb);
    }

    /*
     * Entity methods
     */

    public void getEntityTriples(String projectName, String entityName, AsyncCallback<List<Triple>> cb) {
        proxy.getEntityTriples(projectName, entityName, cb);
    }

    public void getEntityTriples(String projectName, List<String> entities, List<String> properties,
            AsyncCallback<List<Triple>> cb) {
        proxy.getEntityTriples(projectName, entities, properties, cb);
    }

    public void getEntityTriples(String projectName, List<String> entities, List<String> properties, List<String> reifiedProperties,
            AsyncCallback<List<Triple>> cb) {
        proxy.getEntityTriples(projectName, entities, properties, reifiedProperties, cb);
    }

    public void getEntityTriples(String projectName, List<String> entities, Map<String, List<String>> id2simpleTriples, Map<String, PropertyPairs> id2reifiedPropPairs,
    		AsyncCallback<Map<String, List<Triple>>> cb) {
    	proxy.getEntityTriples(projectName, entities, id2simpleTriples, id2reifiedPropPairs, cb);
    }
    
    
    public void getEntityPropertyValues(String projectName, List<String> entities, List<String> properties, List<String> reifiedProps,
            AsyncCallback<List<EntityPropertyValues>> cb) {
        proxy.getEntityPropertyValues(projectName, entities, properties, reifiedProps, cb);
    }
    
    public void getEntityPropertyValues(String prjName, List<String> subjects, Map<String, PropertyPairs> reifiedPropMap,
			AsyncCallback<Map<String, List<EntityPropertyValues>>> cb) {
		proxy.getEntityPropertyValues(prjName, subjects, reifiedPropMap, cb);
	}

	public void getMultilevelEntityPropertyValues(String projectName,
			List<String> entities, String property, List<String> reifiedProperties,
			int[] subjectEntityIndexes, AsyncCallback<List<EntityPropertyValuesList>> cb) {
		proxy.getMultilevelEntityPropertyValues(projectName,
				entities, property, reifiedProperties, subjectEntityIndexes, cb);
	}

    public void getRootEntity(String projectName, AsyncCallback<EntityData> cb) {
        proxy.getRootEntity(projectName, cb);
    }

    public void renameEntity(String projectName, String oldName, String newName, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.renameEntity(projectName, oldName, newName, user, operationDescription, cb);
    }

    public void getEntity(String projectName, String entityName, AsyncCallback<EntityData> cb) {
        proxy.getEntity(projectName, entityName, cb);
    }

    public void deleteEntity(String projectName, String entityName, String user, String operationDescription,
            AsyncCallback<Void> cb) {
        proxy.deleteEntity(projectName, entityName, user, operationDescription, cb);
    }

    /*
     * Class methods
     */

    public void getSubclasses(String projectName, String className, AsyncCallback<List<SubclassEntityData>> cb) {
        proxy.getSubclasses(projectName, className, cb);
    }

    public void getIndividuals(String projectName, String className, AsyncCallback<List<EntityData>> cb) {
        proxy.getIndividuals(projectName, className, cb);
    }

    public void getIndividuals(String projectName, String className, int start, int limit, String sort, String dir,
            AsyncCallback<PaginationData<EntityData>> cb) {
        proxy.getIndividuals(projectName, className, start, limit, sort, dir, cb);
    }

    public void createCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createCls(projectName, clsName, superClsName, user, operationDescription, cb);
    }

    public void createCls(String projectName, String clsName, String superClsName, boolean createMetaClses, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createCls(projectName, clsName, superClsName,createMetaClses, user, operationDescription, cb);
    }

    public void createClsWithProperty(String projectName, String clsName, String superClsName, String propertyName,
            EntityData propertyValue, String user, String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createClsWithProperty(projectName, clsName, superClsName, propertyName, propertyValue, user, operationDescription, cb);
    }

    public void addSuperCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription, AsyncCallback<Void> cb) {
        proxy.addSuperCls(projectName, clsName, superClsName, user, operationDescription, cb);
    }

    public void removeSuperCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription, AsyncCallback<Void> cb) {
        proxy.removeSuperCls(projectName, clsName, superClsName, user, operationDescription, cb);
    }

    public void moveCls(String projectName, String clsName, String oldParentName, String newParentName, boolean checkForCycles,
            String user, String operationDescription, AsyncCallback<List<EntityData>> cb) {
        proxy.moveCls(projectName, clsName, oldParentName, newParentName, checkForCycles, user, operationDescription, cb);
    }

    public void getRestrictionHtml(String projectName, String className, AsyncCallback<String> cb) {
        proxy.getRestrictionHtml(projectName, className, cb);
    }

    public void getClassConditions(String projectName, String className, AsyncCallback<List<ConditionItem>> cb) {
        proxy.getClassConditions(projectName, className, cb);
    }

    public void deleteCondition(String projectName, String className, ConditionItem conditionItem, int row, String operationDescription,
            AsyncCallback<List<ConditionItem>> cb) {
        proxy.deleteCondition(projectName, className, conditionItem, row, operationDescription, cb);
    }

    public  void replaceCondition(String projectName, String className, ConditionItem conditionItem, int row,
            String newCondition, String operationDescription, AsyncCallback<List<ConditionItem>> callback) {
        proxy.replaceCondition(projectName, className, conditionItem, row, newCondition, operationDescription, callback);
    }

    public void addCondition(String projectName, String className, int row, String newCondition, boolean isNS,
            String operationDescription, AsyncCallback<List<ConditionItem>> callback) {
        proxy.addCondition(projectName, className, row, newCondition, isNS, operationDescription, callback);
    }

    public void getConditionAutocompleteSuggestions(String projectName, String condition, int cursorPosition,
            AsyncCallback<ConditionSuggestion> callback) {
        proxy.getConditionAutocompleteSuggestions(projectName, condition, cursorPosition, callback);
    }

    public void getParents(String projectName, String className, boolean direct, AsyncCallback<List<EntityData>> cb) {
        proxy.getParents(projectName, className, direct, cb);
    }

    public void getParentsHtml(String projectName, String className, boolean direct, AsyncCallback<String> cb) {
        proxy.getParentsHtml(projectName, className, direct, cb);
    }

    public void getRelatedProperties(String projectName, String className, AsyncCallback<List<Triple>> callback) {
        proxy.getRelatedProperties(projectName, className, callback);
    }

    /*
     * Properties methods
     */

    public void createObjectProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createObjectProperty(projectName, propertyName, superPropName, user, operationDescription, cb);
    }

    public void createDatatypeProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createDatatypeProperty(projectName, propertyName, superPropName, user, operationDescription, cb);
    }

    public void createAnnotationProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createAnnotationProperty(projectName, propertyName, superPropName, user, operationDescription, cb);
    }

    public void getSubproperties(String projectName, String propertyName, AsyncCallback<List<EntityData>> cb) {
        proxy.getSubproperties(projectName, propertyName, cb);
    }

	public void getProperties(String projectName, List<String> props, AsyncCallback<List<EntityData>> cb) {
		proxy.getProperties(projectName, props, cb);
	}

    public void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
            EntityData value, boolean copyIfTemplate, String user, String operationDescription, AsyncCallback<Void> cb) {
        proxy.addPropertyValue(projectName, entityName, propertyEntity, value, copyIfTemplate, user, operationDescription, cb);
    }

    public void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
            EntityData value, boolean deleteIfFromTemplate, String user, String operationDescription, AsyncCallback<Void> cb) {
        proxy.removePropertyValue(projectName, entityName, propertyEntity, value, deleteIfFromTemplate, 
        		user, operationDescription, cb);
    }

    public void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
            EntityData oldValue, EntityData newValue, boolean copyIfTemplate, 
            String user, String operationDescription, AsyncCallback<Void> cb) {
        proxy.replacePropertyValue(projectName, entityName, propertyEntity, oldValue, newValue, copyIfTemplate, user,
                operationDescription, cb);
    }

    public void setPropertyValues(String projectName, String entityName,  PropertyEntityData propertyEntity,
            List<EntityData> values, String user, String operationDescription, AsyncCallback<Void> cb) {
        proxy.setPropertyValues(projectName, entityName, propertyEntity, values, user, operationDescription, cb);
    }

    /*
     * Instance methods
     */

    public void createInstance(String projectName, String instName, String typeName, String user,
            String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createInstance(projectName, instName, typeName, user, operationDescription, cb);
    }

    public void createInstanceValue(String projectName, String instName, String typeName, String subjectEntity,
            String propertyEntity, String user, String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createInstanceValue(projectName, instName, typeName, subjectEntity, propertyEntity, user,
                operationDescription, cb);
    }

    public void createInstanceValueWithPropertyValue(String projectName, String instName, String typeName,
            String subjectEntity, String propertyEntity, PropertyEntityData instancePropertyEntity,
            EntityData valueEntityData, String user, String operationDescription, AsyncCallback<EntityData> cb) {
        proxy.createInstanceValueWithPropertyValue(projectName, instName, typeName, subjectEntity, propertyEntity,
                instancePropertyEntity, valueEntityData, user, operationDescription, cb);
    }

	public void createPropertyValueInstances(String projectName, EntityData rootSubject, String[] properties,
			String[] types, String user, String operationDescription, AbstractAsyncHandler<EntityData[]> cb) {
		proxy.createPropertyValueInstances(projectName, rootSubject, properties, types, user, operationDescription, cb);
	}

    /*
     * Search
     */

    public void search(String projectName, String searchString, AsyncCallback<List<EntityData>> cb) {
        proxy.search(projectName, searchString, cb);
    }

    public void search(String projectName, String searchString, ValueType valueType, AsyncCallback<List<EntityData>> cb) {
        proxy.search(projectName, searchString, valueType, cb);
    }

    public void search(String projectName, String searchString, ValueType valueType, 
    		int start, int limit, String sort, String dir, AsyncCallback<PaginationData<EntityData>> cb) {
        proxy.search(projectName, searchString, valueType, start, limit, sort, dir, cb);
    }

    public void getPathToRoot(String projectName, String entityName, AsyncCallback<List<EntityData>> cb) {
        proxy.getPathToRoot(projectName, entityName, cb);
    }
    
    public void getPathToSupercls(String projectName, String entityName, String superclsName, AsyncCallback<List<EntityData>> cb) {
        proxy.getPathToSupercls(projectName, entityName, superclsName, cb);
    }

    public void getDirectTypes(String projectName, String instanceName, AsyncCallback<List<EntityData>> cb){
        proxy.getDirectTypes(projectName, instanceName, cb);
    }


}
