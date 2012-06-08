package edu.stanford.bmir.protege.web.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
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

public class OntologyServiceManager {
	
	private static OntologyServiceAsync proxy;
	private static OntologyServiceManager instance;
	
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
		
	public void getEvents(String projectName, long fromVersion, 
			AsyncCallback<ArrayList<AbstractEvent>> cb) {
		proxy.getEvents(projectName, fromVersion, cb);
	}
	
	public void hasWritePermission(String projectName, String userName, AsyncCallback<Boolean> cb) {
		proxy.hasWritePermission(projectName, userName, cb);
	}
	
	
	/*
	 * Ontology methods
	 */
		
	public void getAnnotationProperties(String projectName, String entityName,
			AsyncCallback<ArrayList<AnnotationData>> cb) {
		proxy.getAnnotationProperties(projectName, entityName, cb);
	}

	public void getImportedOntologies(String projectName, AsyncCallback<ImportsData> cb) {
		proxy.getImportedOntologies(projectName, cb);
	}

	public void getMetrics(String projectName, AsyncCallback<ArrayList<MetricData>> cb) {
		proxy.getMetrics(projectName, cb);
	}

	
	/*
	 * Entity methods
	 */
	
	public void getEntityTriples(String projectName, String entityName,
			AsyncCallback<ArrayList<Triple>> cb) {
		proxy.getEntityTriples(projectName, entityName, cb);
	}
	
	public void getEntityTriples(String projectName, List<String> entities, List<String> properties,
			AsyncCallback<ArrayList<Triple>> cb) {
		proxy.getEntityTriples(projectName, entities, properties, cb);
	}
	
	public void getRootEntity(String projectName, AsyncCallback<EntityData> cb) {
		proxy.getRootEntity(projectName, cb);
	}

	public void renameEntity(String projectName, String oldName, String newName, AsyncCallback<EntityData> cb)  {
		proxy.renameEntity(projectName, oldName, newName, cb);
	}
	
	
	/*
	 * Class methods
	 */
	
	public void getSubclasses(String projectName, String className,
			AsyncCallback<ArrayList<SubclassEntityData>> cb) {
		proxy.getSubclasses(projectName, className, cb);
	}

	public void getIndividuals(String projectName, String className,
			AsyncCallback<ArrayList<EntityData>> cb) {
		proxy.getIndividuals(projectName, className, cb);
	}
	
	public void createCls(String projectName, String clsName, String superClsName,
			AsyncCallback<EntityData> cb) {
		proxy.createCls(projectName, clsName, superClsName, cb);
	}
	
	public void deleteCls(String projectName, String clsName,
			AsyncCallback<Void> cb) {
		proxy.deleteCls(projectName, clsName, cb);
	}
	
	public void addSuperCls(String projectName, String clsName, String superClsName, AsyncCallback<Void> cb) {
		proxy.addSuperCls(projectName, clsName, superClsName, cb);
	}
	
	public void removeSuperCls(String projectName, String clsName, String superClsName, AsyncCallback<Void> cb){
		proxy.removeSuperCls(projectName, clsName, superClsName, cb);
	}

	public void moveCls(String projectName, String clsName, String oldParentName, String newParentName, AsyncCallback<Void> cb) {
		proxy.moveCls(projectName, clsName, oldParentName, newParentName, cb);
	}
	
	public void getRestrictionHtml(String projectName, String className, AsyncCallback<String> cb) {
		proxy.getRestrictionHtml(projectName, className, cb);
	}
	
	
	/*
	 * Properties methods
	 */
	
	public void createObjectProperty(String projectName, String propertyName,
			String superPropName, AsyncCallback<EntityData> cb) {
		proxy.createObjectProperty(projectName, propertyName, superPropName, cb);
	}
	
	public void createDatatypeProperty(String projectName, String propertyName, 
			String superPropName, AsyncCallback<EntityData> cb) {
		proxy.createDatatypeProperty(projectName, propertyName, superPropName, cb);
	}
	
	public void createAnnotationProperty(String projectName, String propertyName, 
			String superPropName, AsyncCallback<EntityData> cb) {
		proxy.createAnnotationProperty(projectName, propertyName, superPropName, cb);
	}
	
	public void deleteProperty(String projectName, String propertyName, AsyncCallback<Void> cb) {
		proxy.deleteProperty(projectName, propertyName, cb);
	}
	
	public void getSubproperties(String projectName, String propertyName,
			AsyncCallback<ArrayList<EntityData>> cb) {
		proxy.getSubproperties(projectName, propertyName, cb);
	}
	
	public void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
			EntityData value, AsyncCallback<Void> cb) {
		proxy.addPropertyValue(projectName, entityName, propertyEntity, value, cb);
	}
	
	public void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
			EntityData value, AsyncCallback<Void> cb) {
		proxy.removePropertyValue(projectName, entityName, propertyEntity, value, cb);
	}
	
	public void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity,
			EntityData oldValue, EntityData newValue, AsyncCallback<Void> cb) {
		proxy.replacePropertyValue(projectName, entityName, propertyEntity, oldValue, newValue, cb);
	}
	
	
	/*
	 * Instance methods
	 */
	
	public void createInstance(String projectName, String instName, String typeName, AsyncCallback<EntityData> cb) {
		proxy.createInstance(projectName, instName, typeName, cb);
	}
	
	public void createInstanceValue(String projectName, String instName, String typeName, 
			String subjectEntity, String propertyEntity, AsyncCallback<EntityData> cb) {
		proxy.createInstanceValue(projectName, instName, typeName, subjectEntity, propertyEntity, cb);
	}

	
	/*
	 * Notes methods
	 */
	
	public void getNotes(String projectName, String entityName, boolean topLevel,
			AsyncCallback<ArrayList<NotesData>> cb) {
		proxy.getNotes(projectName, entityName, topLevel, cb);
	}
	
	public void createNote(String projectName, NotesData newNote, boolean topLevel, 
			AsyncCallback<NotesData> cb) {
		proxy.createNote(projectName, newNote, topLevel, cb);
	}
	
	/*
	 * Search 
	 */
	
	public void search(String projectName, String searchString, AsyncCallback<ArrayList<EntityData>> cb) {
		proxy.search(projectName, searchString, cb);
	}	
	
	public void getPathToRoot(String projectName, String entityName, AsyncCallback<ArrayList<EntityData>> cb) {
		proxy.getPathToRoot(projectName, entityName, cb);
	}
	
	
	/* 
	 * Util methods
	 */
	
	public void getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData, AsyncCallback<String> cb) {
		proxy.getBioPortalSearchContent(projectName, entityName, bpSearchData, cb);
	}
	
	public void importBioPortalConcept(String projectName, String entityName,  
			BioPortalReferenceData bpRefData, AsyncCallback<Boolean> cb) {
		proxy.importBioPortalConcept(projectName, entityName, bpRefData, cb);
	}
}
