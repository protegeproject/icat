package edu.stanford.bmir.protege.web.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

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
 * A service for accessing ontology data.
 * <p />
 * 
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
@RemoteServiceRelativePath("ontology")
public interface OntologyService extends RemoteService {

	/*
	 * Project management methods
	 */
		
	public Integer loadProject(String projectName);	
	
	public ArrayList<AbstractEvent> getEvents(String projectName, long fromVersion);
	
	public Boolean hasWritePermission(String projectName, String userName);
	
	
	/*
	 * Ontology methods
	 */
	
	public String getOntologyURI(String projectName);	

	public ArrayList<AnnotationData> getAnnotationProperties(String projectName, String entityName);	

	public ImportsData getImportedOntologies(String projectName);

	public ArrayList<MetricData> getMetrics(String projectName);
	
	
	/*
	 * Entity methods
	 */
	
	public ArrayList<Triple> getEntityTriples(String projectName, String entityName);
	
	public ArrayList<Triple> getEntityTriples(String projectName, List<String> entities, List<String> properties);
	
	public EntityData renameEntity(String projectName, String oldName, String newName);
	
	public EntityData getRootEntity(String projectName);
	
	
	/*
	 * Class methods
	 */
	
	public EntityData createCls(String projectName, String clsName, String superClsName);
	
	public void deleteCls(String projectName, String clsName);
	
	public void addSuperCls(String projectName, String clsName, String superClsName);
	
	public ArrayList<SubclassEntityData> getSubclasses(String projectName, String className);
	
	public void removeSuperCls(String projectName, String clsName, String superClsName);
	
	public void moveCls(String projectName, String clsName, String oldParentName, String newParentName);
	
	public ArrayList<EntityData> getIndividuals(String projectName, String className);
	
	public String getRestrictionHtml(String projectName, String className);
	
	
	/*
	 * Properties methods
	 */
	
	public EntityData createObjectProperty(String projectName, String propertyName, String superPropName);
	
	public EntityData createDatatypeProperty(String projectName, String propertyName, String superPropName);
	
	public EntityData createAnnotationProperty(String projectName, String propertyName, String superPropName);
	
	public void deleteProperty(String projectName, String propertyName);
	
	public ArrayList<EntityData> getSubproperties(String projectName, String propertyName);
	
	public void addPropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData value);
	
	public void removePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData value);
	
	public void replacePropertyValue(String projectName, String entityName, PropertyEntityData propertyEntity, EntityData oldValue, EntityData newValue);
	
	
	/*
	 * Instance methods
	 */
	
	public EntityData createInstance(String projectName, String instName, String typeName);
	
	public EntityData createInstanceValue(String projectName, String instName, String typeName, String subjectEntity, String propertyEntity);
	
	/*
	 * Notes methods
	 */
	
	public ArrayList<NotesData> getNotes(String projectName, String entityName, boolean topLevel);
	
	public NotesData createNote(String projectName, NotesData newNote, boolean topLevel);
	
	/*
	 * Search
	 */
	
	public ArrayList<EntityData> search(String projectName, String searchString);		
	
	public ArrayList<EntityData> getPathToRoot(String projectName, String entityName);
	
	/*
	 * Util methods
	 */
	
	public String getBioPortalSearchContent(String projectName, String entityName, BioPortalSearchData bpSearchData);
	
	public boolean importBioPortalConcept(String projectName, String entityName, BioPortalReferenceData bpRefData);
}
