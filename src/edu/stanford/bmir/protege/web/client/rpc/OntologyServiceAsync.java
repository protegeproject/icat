package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.model.event.OntologyEvent;
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
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;


/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public interface OntologyServiceAsync {

    /*
     * Project management methods
     */

    void loadProject(String projectName, AsyncCallback<Integer> cb);

    void getEvents(String projectName, long fromVersion, AsyncCallback<List<OntologyEvent>> cb);

    void hasWritePermission(String projectName, String userName, AsyncCallback<Boolean> cb);

    /*
     * Ontology methods
     */

    void getOntologyURI(String projectName, AsyncCallback<String> cb);

    void getAnnotationProperties(String projectName, String entityName, AsyncCallback<List<AnnotationData>> cb);

    void getImportedOntologies(String projectName, AsyncCallback<ImportsData> cb);

    void getMetrics(String projectName, AsyncCallback<List<MetricData>> cb);

    /*
     * Entity methods
     */

    void getEntityTriples(String projectName, String entityName, AsyncCallback<List<Triple>> cb);

    void getEntityTriples(String projectName, List<String> entities, List<String> properties, AsyncCallback<List<Triple>> cb);

    void getEntityTriples(String projectName, List<String> entities, List<String> properties, List<String> reifiedProps, AsyncCallback<List<Triple>> cb);

    void getEntityPropertyValues(String projectName, List<String> entities, List<String> properties, List<String> reifiedProps, AsyncCallback<List<EntityPropertyValues>> cb);

	void getMultilevelEntityPropertyValues(String projectName,
			List<String> entities, String property,
			List<String> reifiedProperties, int[] subjectEntityIndexes,
			AsyncCallback<List<EntityPropertyValuesList>> cb);

    void getRootEntity(String projectName, AsyncCallback<EntityData> cb);

    void renameEntity(String projectName, String oldName, String newName, String user, String operationDescription, AsyncCallback<EntityData> cb);

    void getEntity(String projectName, String entityName, AsyncCallback<EntityData> cb);

    void deleteEntity(String projectName, String entityName, String user, String operationDescription, AsyncCallback<Void> cb);

    /*
     * Class methods
     */

    void getSubclasses(String projectName, String className, AsyncCallback<List<SubclassEntityData>> cb);

    void getIndividuals(String projectName, String className, AsyncCallback<List<EntityData>> cb);

    void getIndividuals(String projectName, String className, int start, int limit, String sort, String dir,
            AsyncCallback<PaginationData<EntityData>> cb);

    void createCls(String projectName, String clsName, String superClsName, String user, String operationDescription,
            AsyncCallback<EntityData> cb);

    void createCls(String projectName, String clsName, String superClsName, boolean createMetaClses, String user, String operationDescription,
            AsyncCallback<EntityData> cb);

    void createClsWithProperty(String projectName, String clsName, String superClsName, String propertyName, EntityData propertyValue, String user, String operationDescription,
            AsyncCallback<EntityData> cb);

    void addSuperCls(String projectName, String clsName, String superClsName, String user, String operationDescription,
            AsyncCallback<Void> cb);

    void removeSuperCls(String projectName, String clsName, String superClsName, String user,
            String operationDescription, AsyncCallback<Void> cb);

    void moveCls(String projectName, String clsName, String oldParentName, String newParentName, boolean checkForCycles,
            String user,  String operationDescription, AsyncCallback<List<EntityData>> cb);

    void getRestrictionHtml(String projectName, String className, AsyncCallback<String> cb);

    void getClassConditions(String projectName, String className, AsyncCallback<List<ConditionItem>> cb);

    void deleteCondition(String projectName, String className, ConditionItem conditionItem, int row, String operationDescription,
            AsyncCallback<List<ConditionItem>> callback);

    void replaceCondition(String projectName, String className, ConditionItem conditionItem, int row,
            String newCondition, String operationDescription, AsyncCallback<List<ConditionItem>> callback);

    void addCondition(String projectName, String className, int row, String newCondition, boolean isNS,
            String operationDescription, AsyncCallback<List<ConditionItem>> callback);

    void getConditionAutocompleteSuggestions(String projectName, String condition, int cursorPosition,  AsyncCallback<ConditionSuggestion> callback);

    void getParents(String projectName, String className, boolean direct, AsyncCallback<List<EntityData>> callback);

    void getParentsHtml(String projectName, String className, boolean direct, AsyncCallback<String> callback);

    void getRelatedProperties(String projectName, String className, AsyncCallback<List<Triple>> callback);

    /*
     * Properties methods
     */

    void createObjectProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription, AsyncCallback<EntityData> cb);

    void createDatatypeProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription, AsyncCallback<EntityData> cb);

    void createAnnotationProperty(String projectName, String propertyName, String superPropName, String user,
            String operationDescription, AsyncCallback<EntityData> cb);

    void getSubproperties(String projectName, String propertyName, AsyncCallback<List<EntityData>> cb);

	void getProperties(String projectName, List<String> props, AsyncCallback<List<EntityData>> cb);

    void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData value,
            boolean copyIfTemplate, String user, String operationDescription, AsyncCallback<Void> cb);

    void removePropertyValue(String projectName, String entityName,
			PropertyEntityData propertyEntity, EntityData value,
			boolean deleteIfFromTemplate, String user,
			String operationDescription, AsyncCallback<Void> cb);

    void replacePropertyValue(String projectName, String entityName,
			PropertyEntityData propertyEntity, EntityData oldValue,
			EntityData newValue, boolean copyIfTemplate, String user,
			String operationDescription, AsyncCallback<Void> cb);

    void setPropertyValues(String projectName, String entityName, PropertyEntityData propertyEntity,
            List<EntityData> values, String user, String operationDescription, AsyncCallback<Void> cb);

    /*
     * Instance methods
     */

    void createInstance(String projectName, String instName, String typeName, String user, String operationDescription,
            AsyncCallback<EntityData> cb);

    void createInstanceValue(String projectName, String instName, String typeName, String subjectEntity,
            String propertyEntity, String user, String operationDescription, AsyncCallback<EntityData> cb);

    void createInstanceValueWithPropertyValue(String projectName, String instName, String typeName, String subjectEntity,
    		String propertyEntity, PropertyEntityData instancePropertyEntity, EntityData valueEntityData, String user,
    		String operationDescription, AsyncCallback<EntityData> callback);

    /*
     * Search
     */

    void search(String projectName, String searchString, ValueType valueType, int start, int limit, String sort,
            String dir, AsyncCallback<PaginationData<EntityData>> cb);

    void search(String projectName, String searchString, AsyncCallback<List<EntityData>> cb);

    void search(String projectName, String searchString, ValueType valueType, AsyncCallback<List<EntityData>> cb);

    void getPathToRoot(String projectName, String entityName, AsyncCallback<List<EntityData>> cb);


    void getDirectTypes(String projectName, String instanceName, AsyncCallback<List<EntityData>> cb);


   }
