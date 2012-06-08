package edu.stanford.bmir.protege.web.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.event.AbstractEvent;
import edu.stanford.bmir.protege.web.client.rpc.data.AnnotationData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public interface OntologyServiceAsync {
	
	/*
	 * Project management methods
	 */
	
	void loadProject(String projectName, AsyncCallback<Integer> cb);	
	
	void getEvents(String projectName, long fromVersion, AsyncCallback<ArrayList<AbstractEvent>> cb);
	
	void hasWritePermission(String projectName, String userName, AsyncCallback<Boolean> cb);
	
	
	/*
	 * Ontology methods
	 */	
	
	void getOntologyURI(String projectName, AsyncCallback<String> cb);
	
	void getAnnotationProperties(String projectName, String entityName, AsyncCallback<ArrayList<AnnotationData>> cb);
	
	void getImportedOntologies(String projectName, AsyncCallback<ImportsData> cb);

	void getMetrics(String projectName, AsyncCallback<ArrayList<MetricData>> cb);


	/*
	 * Entity methods
	 */
	
	void getEntityTriples(String projectName, String entityName, AsyncCallback<ArrayList<Triple>> cb);
	
	void getEntityTriples(String projectName, List<String> entities, List<String> properties, AsyncCallback<ArrayList<Triple>> cb);
	
	void getRootEntity(String projectName, AsyncCallback<EntityData> cb);

	void renameEntity(String projectName, String oldName, String newName, AsyncCallback<EntityData> cb);
	
	/*
	 * Class methods
	 */
	
	void getSubclasses(String projectName, String className, AsyncCallback<ArrayList<SubclassEntityData>> cb);	
	
	void getIndividuals(String projectName, String className, AsyncCallback<ArrayList<EntityData>> cb);
	
	void createCls(String projectName, String clsName, String superClsName, AsyncCallback<EntityData> cb);
	
	void deleteCls(String projectName, String clsName, AsyncCallback<Void> cb);
	
	void addSuperCls(String projectName, String clsName, String superClsName, AsyncCallback<Void> cb);
	
	void removeSuperCls(String projectName, String clsName, String superClsName, AsyncCallback<Void> cb);
	
	void moveCls(String projectName, String clsName, String oldParentName, String newParentName, AsyncCallback<Void> cb);
	
	void getRestrictionHtml(String projectName, String className, AsyncCallback<String> cb);
	

	/*
	 * Properties methods
	 */
	
	void createObjectProperty(String projectName, String propertyName, String superPropName, AsyncCallback<EntityData> cb);
	
	void createDatatypeProperty(String projectName, String propertyName, String superPropName, AsyncCallback<EntityData> cb);
	
	void createAnnotationProperty(String projectName, String propertyName, String superPropName, AsyncCallback<EntityData> cb);
	
	void deleteProperty(String projectName, String propertyName, AsyncCallback<Void> cb);
	
	void getSubproperties(String projectName, String propertyName, AsyncCallback<ArrayList<EntityData>> cb);
	
	void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData value, AsyncCallback<Void> cb);
	
	void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData value, AsyncCallback<Void> cb);
	
	void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData oldValue, EntityData newValue, AsyncCallback<Void> cb);
	
	/*
	 * Instance methods
	 */
	
	void createInstance(String projectName, String instName, String typeName, AsyncCallback<EntityData> cb);
	
	void createInstanceValue(String projectName, String instName, String typeName, String subjectEntity, String propertyEntity, AsyncCallback<EntityData> cb);
	
	
	/*
	 * Notes methods
	 */
	
	void getNotes(String projectName, String entityName, boolean topLevel, AsyncCallback<ArrayList<NotesData>> cb);
	
	void createNote(String projectName, NotesData newNote, boolean topLevel,  AsyncCallback<NotesData> cb);
	
	/*
	 * Search
	 */

	void search(String projectName, String searchString, AsyncCallback<ArrayList<EntityData>> cb);
		
	void getPathToRoot(String projectName, String entityName, AsyncCallback<ArrayList<EntityData>> cb );
	
	
	/*
	 * Util methods
	 */

	void getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData, AsyncCallback<String> cb);

	void importBioPortalConcept(String projectName, String entityName, BioPortalReferenceData bpRefData, AsyncCallback<Boolean> cb);
		
	
}
