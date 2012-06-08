package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protegex.bp.ref.Constants;
import edu.stanford.bmir.protege.web.client.event.AbstractEvent;
import edu.stanford.bmir.protege.web.client.rpc.OntologyService;
import edu.stanford.bmir.protege.web.client.rpc.data.AnnotationData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalReferenceData;
import edu.stanford.bmir.protege.web.client.rpc.data.BioPortalSearchData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ImportsData;
import edu.stanford.bmir.protege.web.client.rpc.data.MetricData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyType;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protegex.bp.ref.ReferenceModel;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameCounts;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.server.RemoteClientProject;
import edu.stanford.smi.protege.server.RemoteServer;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.metaproject.MetaProjectConstants;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.Tree;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.OWLOntology;
import edu.stanford.smi.protegex.owl.model.ProtegeNames;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.XSPNames;
import edu.stanford.smi.protegex.owl.model.impl.DefaultRDFSLiteral;
import edu.stanford.smi.protegex.owl.model.impl.OWLSystemFrames;
import edu.stanford.smi.protegex.owl.model.impl.OWLUtil;
import edu.stanford.smi.protegex.owl.model.util.DLExpressivityChecker;
import edu.stanford.smi.protegex.owl.model.util.ModelMetrics;

/**
 * @author Tania Tudorache <tudorache@stanford.edu>
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class OntologyServiceImpl extends RemoteServiceServlet implements OntologyService {
	private static final long serialVersionUID = -4229789001933130232L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if (Log.getLogger().isLoggable(Level.FINE)) {
			Log.getLogger().fine("In init of OntologyServiceImp");
		}

		ServletContext context = config.getServletContext();
		ProjectManager.getProjectManager().setRealPath(context.getRealPath("/"));		
	}


	protected Project getProject(String projectName) {
		return ProjectManager.getProjectManager().getProject(projectName);
	}

	public Integer loadProject(String projectName) {      
		ServerProject serverProject = ProjectManager.getProjectManager().getServerProject(projectName);
		if (serverProject == null) {
			return null;
		} 
		//TODO: not clear it is needed...
		Project prj = serverProject.getProject();
		if (prj != null) {
			HasAnnotationCache.fillHasAnnotationCache(prj.getKnowledgeBase());
		}
		return Integer.valueOf(serverProject.getServerVersion()); 

	}


	public String getOntologyURI(String projectName) {
		Project project = getProject(projectName);
		RDFResource owlOntology = getOWLOntologyObject(project);
		return owlOntology.getURI();
	}


	public ArrayList getAnnotationProperties(String projectName, String entityName) {
		ArrayList<AnnotationData> annotations = new ArrayList<AnnotationData>();
		Project project = getProject(projectName);

		// Frames ontologies don't have annotations.
		if (!isOWLOntology(project)) {
			return annotations;
		}

		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		RDFResource owlOntology = owlModel.getOWLOntologyByURI(URIUtilities.createURI(entityName));

		// Loop through RDF properties for the ontology object.
		for (Object o : owlOntology.getRDFProperties()) {
			RDFProperty property = (RDFProperty) o;

			// Filter on annotation properties.
			if (property.isAnnotationProperty()) {

				// Loop through property values for each property.
				for (Object value : owlOntology.getPropertyValues(property)) {
					String propName = property.getBrowserText();
					AnnotationData annotation = new AnnotationData(propName); 

					/*
					 * TODO: This code needs to be fixed later.  It makes 
					 * the assumption that all values are strings, but this 
					 * will not be true going forward.
					 */
					if (value instanceof RDFSLiteral) {
						RDFSLiteral literal = (RDFSLiteral) value;
						annotation.setLang(literal.getLanguage());
						annotation.setValue(literal.getString());
					} else {
						annotation.setValue(value.toString());
					}

					annotations.add(annotation);
				}
			}
		}

		Collections.sort(annotations, new AnnotationDataComparator());

		return annotations;
	}

	class AnnotationDataComparator implements Comparator<AnnotationData> {
		public int compare(AnnotationData ad1, AnnotationData ad2) {
			return ad1.getName().compareTo(ad2.getName());
		}
	}

	private RDFResource getOWLOntologyObject(Project project) {
		RDFResource owlOntology = null;

		if (project != null) {
			OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
			owlOntology = owlModel.getDefaultOWLOntology();
		}

		return owlOntology;
	}

	public ArrayList<SubclassEntityData> getSubclasses(String projectName, String className) {
		ArrayList<SubclassEntityData> subclassesData = new ArrayList<SubclassEntityData>();

		Project project = getProject(projectName);
		if (project != null) {
			KnowledgeBase kb = project.getKnowledgeBase();

			Cls superCls = kb.getCls(className);
			if (superCls == null) {
				return subclassesData;
			}

			ArrayList subclasses = new ArrayList(superCls.getVisibleDirectSubclasses());
			Collections.sort(subclasses, new FrameComparator());						

			for (Iterator iterator = subclasses.iterator(); iterator.hasNext();) {
				Cls subcls = (Cls) iterator.next();

				if (!subcls.isSystem()) {
					subclassesData.add(
							new SubclassEntityData(subcls.getName(), 
									getBrowserText(subcls), null, 
									ChAOUtil.hasAnnotations(subcls),
									subcls.getVisibleDirectSubclassCount()));
				}
			}
		}
		//Log.getLogger().info("Server: Get subclasses of " + className + " count: " + subclassesData.size() + " " + (new Date()));
		return subclassesData;
	}

	private static String getBrowserText(Frame frame) {
		String bt = frame.getBrowserText();
		if (bt.startsWith("'")) {
			bt = bt.substring(1, bt.length() - 1);
		}
		return bt;
	}

	public EntityData getRootEntity(String projectName) {
		Project project = getProject(projectName);

		if (project != null) {
			KnowledgeBase kb = project.getKnowledgeBase();
			Cls root = kb.getRootCls();

			if (kb instanceof OWLModel) {				
				root = ((OWLModel)kb).getOWLThingClass();
			}
			return new EntityData(root.getName(), root.getBrowserText());
		}
		return new EntityData("Root", "Root");
	}


	public ArrayList<EntityData> getSubproperties(String projectName, String propertyName) {
		ArrayList<EntityData> subpropertyData = new ArrayList<EntityData>();

		Project project = getProject(projectName);
		if (project != null) {
			KnowledgeBase kb = project.getKnowledgeBase();

			ArrayList subproperties = new ArrayList();
			if (propertyName == null) { //property root case
				subproperties.addAll(getRootProperties(kb));
			} else {
				Slot superProperty = kb.getSlot(propertyName);
				if (superProperty == null) {
					return subpropertyData;
				}
				subproperties.addAll(superProperty.getDirectSubslots());
			}

			Collections.sort(subproperties, new FrameComparator());

			for (Iterator iterator = subproperties.iterator(); iterator.hasNext();) {
				Slot subprop = (Slot) iterator.next();

				if (!subprop.isSystem() && subprop.isVisible()) {
					PropertyEntityData entityData = new PropertyEntityData(subprop.getName(), subprop.getBrowserText(), null, 
							ChAOUtil.hasAnnotations(subprop));
					entityData.setPropertyType(getPropertyType(subprop));					
					subpropertyData.add(entityData);				
				}
			}
		}

		return subpropertyData;
	}

	
	public static PropertyType getPropertyType(Slot slot) {
		if (slot instanceof OWLObjectProperty) {
			return PropertyType.OBJECT;
		} else if (slot instanceof OWLDatatypeProperty) {
			return PropertyType.DATATYPE;
		} else if (slot instanceof RDFProperty && ((RDFProperty) slot).isAnnotationProperty()) {
			return PropertyType.ANNOTATION;
		}
		return null;
	}
	

	public void addPropertyValue(String projectName, String entityName,
			PropertyEntityData propertyEntity, EntityData valueEntityData) {
		Project project = getProject(projectName);
		if (project == null) {throw new IllegalArgumentException("Add operation failed. Unknown project: " + projectName);}			
		KnowledgeBase kb = project.getKnowledgeBase();
		Instance subject = kb.getInstance(entityName);
		if (subject == null) {throw new IllegalArgumentException("Add operation failed. Unknown subject: " + entityName); }
		Slot property = kb.getSlot(propertyEntity.getName());
		if (property == null) { new IllegalArgumentException("Add operation failed. Unknown property: " + propertyEntity.getName()); }
		if (valueEntityData.getValueType() == null) { //TODO: maybe move in getProtegeObject
			ValueType propValueType = property.getValueType();
			valueEntityData.setValueType(propValueType == null ? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.String :
				edu.stanford.bmir.protege.web.client.rpc.data.ValueType.valueOf(propValueType.toString()));
		}
		Object value = getProtegeObject(kb, valueEntityData);
		if (value != null) {
			subject.addOwnSlotValue(property, value);
		} else {
			throw new IllegalArgumentException("Add operation failed. Invalid value: " + valueEntityData.getName());
		}
	}


	public void removePropertyValue(String projectName, String entityName,
			PropertyEntityData propertyEntity, EntityData valueEntityData) {	
		Project project = getProject(projectName);
		if (project == null) {return ;}			
		KnowledgeBase kb = project.getKnowledgeBase();
		Instance subject = kb.getInstance(entityName);
		if (subject == null) { return; }
		Slot property = kb.getSlot(propertyEntity.getName());
		if (property == null) { return; }
		Object value = getProtegeObject(kb, valueEntityData);
		if (value != null) {
			if (value instanceof RDFSLiteral) {
				value = value.toString();
			}
			subject.removeOwnSlotValue(property, value);
		}
	}

	public void replacePropertyValue(String projectName, String entityName,
			PropertyEntityData propertyEntity, EntityData oldValue, EntityData newValue) { 
		//TODO: make into a transaction?
		addPropertyValue(projectName, entityName, propertyEntity, newValue);
		removePropertyValue(projectName, entityName, propertyEntity, oldValue);		
	}

	private static Object getProtegeObject(KnowledgeBase kb, EntityData entityData) {
		if (entityData == null) { return null; }
		edu.stanford.bmir.protege.web.client.rpc.data.ValueType valueType = entityData.getValueType();
		if (valueType == null) { valueType = edu.stanford.bmir.protege.web.client.rpc.data.ValueType.String; }
		String value = entityData.getName();
		switch (valueType) {
		case String:
			return value;
		case Symbol:
			return value;
		case Instance:
			return kb.getInstance(value);
		case Integer:
			try {
				return new Integer(value);
			} catch (NumberFormatException e) {
				return null;
			}
		case Float: 
			try {
				return new Float(value);
			} catch (NumberFormatException e) {
				return null;
			}	
		case Date: 
			return value; //FIXME for OWL
		case Literal:
		{
			if (!(kb instanceof OWLModel)) { return value; }
			OWLModel owlModel = (OWLModel) kb;
			//FIXME: should work with all datatypes
			RDFSLiteral lit = DefaultRDFSLiteral.create(owlModel, value, owlModel.getRDFSDatatypeByName("xsd:string"));
			return lit;

		}
		default:
			return null;
		}		
	}


	protected Collection getRootProperties(KnowledgeBase kb) {
		List results = new ArrayList(kb.getSlots());
		Iterator i = results.iterator();
		while (i.hasNext()) {
			Slot slot = (Slot) i.next();
			if (slot.getDirectSuperslotCount() > 0) {
				i.remove();
			}
		}      
		Collections.sort(results, new FrameComparator());      
		return results;
	}


	public ArrayList<Triple> getEntityTriples(String projectName, String entityName) {
		ArrayList<Triple> triples = new ArrayList<Triple>();

		Project project = getProject(projectName);
		if (project == null) {
			return triples;
		}

		KnowledgeBase kb = project.getKnowledgeBase();
		Instance inst = kb.getInstance(entityName);
		if (inst == null) {
			return triples;
		}

		for (Iterator iterator = inst.getOwnSlots().iterator(); iterator.hasNext();) {
			Slot slot = (Slot) iterator.next();		
			if (!slot.isSystem()) { 
				triples.addAll(getTriples(inst, slot));
			}
		}

		//add rdfs:comment and rdfs:label
		if (kb instanceof OWLModel) {
			triples.addAll(getTriples(inst, ((OWLModel)kb).getRDFSCommentProperty()));
			triples.addAll(getTriples(inst, ((OWLModel)kb).getRDFSLabelProperty()));
			//domain and range should not be necessarily retrieved
			triples.addAll(getTriples(inst, ((OWLModel)kb).getRDFSDomainProperty()));
			triples.addAll(getTriples(inst, ((OWLModel)kb).getRDFSRangeProperty()));
		}

		if (inst instanceof Cls) {
			triples.addAll(getPropertiesInDomain((Cls)inst));
		}

		return triples;
	}

	public ArrayList<Triple> getEntityTriples(String projectName, List<String> entities, List<String> properties) {
		ArrayList<Triple> triples = new ArrayList<Triple>();
		Project project = getProject(projectName);
		if (project == null) { return triples; }

		KnowledgeBase kb = project.getKnowledgeBase();

		for (String entityName : entities) {
			Instance inst = kb.getInstance(entityName);
			if (inst != null) {
				for (Iterator<String> iterator = properties.iterator(); iterator.hasNext();) {
					String propName = iterator.next();
					Slot slot = kb.getSlot(propName);			
					if (slot != null) { 
						triples.addAll(getTriples(inst, slot));
					}
				}
			}
		}
		return triples;
	}


	private ArrayList<Triple> getPropertiesInDomain(Cls cls) {
		ArrayList<Triple> triples = new ArrayList<Triple>();
		KnowledgeBase kb = cls.getKnowledgeBase();

		for (Iterator iterator = cls.getTemplateSlots().iterator(); iterator.hasNext();) {
			Slot slot = (Slot) iterator.next();

			//hack
			if (slot.isSystem() || slot.getName().startsWith(ProtegeNames.PROTEGE_OWL_NAMESPACE) ||
					slot.getName().startsWith(XSPNames.NS)) {
				continue;
			}
			//TODO: refactor this code 

			if (slot.getValueType() == ValueType.INSTANCE) {
				Collection directDomain = slot.getDirectDomain();
				Collection allowedClses = slot.getAllowedClses();
				if (kb instanceof OWLModel) {
					if (directDomain != null && directDomain.size() > 0 && !directDomain.contains(kb.getRootCls())) {
						if (allowedClses.size() == 0) {
							allowedClses.add(kb.getRootCls());
						}
						triples.addAll(getTriples(cls, slot, allowedClses));
					}
				} else {
					triples.addAll(getTriples(cls, slot, allowedClses));
				}
			} else	if (slot.getValueType() == ValueType.CLS) {
				Collection directDomain = slot.getDirectDomain();
				Collection allowedParents = slot.getAllowedParents();
				if (kb instanceof OWLModel) {
					if (directDomain != null && directDomain.size() > 0 && !directDomain.contains(kb.getRootCls())) {
						if (allowedParents.size() == 0) {
							allowedParents.add(kb.getRootCls());
						}
						triples.addAll(getTriples(cls, slot, allowedParents));
					}
				} else {
					triples.addAll(getTriples(cls, slot, allowedParents));
				}
			} else  {
				//TODO
				Collection directDomain = slot.getDirectDomain();				
				if (kb instanceof OWLModel) {
					if (directDomain != null && directDomain.size() > 0 && !directDomain.contains(kb.getRootCls())) {
						triples.add(createTriple(cls, slot, slot.getValueType().toString()));
					}
				} else {
					triples.add(createTriple(cls, slot, slot.getValueType().toString()));
				}
			}
		}

		return triples;
	}


	protected Collection<Triple> getTriples(Instance inst, Slot slot) {
		return getTriples(inst, slot, inst.getOwnSlotValues(slot));
	}

	protected Collection<Triple> getTriples(Instance inst, Slot slot, Collection values) {
		ArrayList<Triple> triples = new ArrayList<Triple>();

		for (Iterator iterator2 = values.iterator(); iterator2.hasNext();) {
			Object object = (Object) iterator2.next();			
			triples.add(createTriple(inst, slot, object));			
		}

		return triples;
	}

	protected Triple createTriple(Instance instance, Slot slot, Object object) {
		EntityData subj = createEntityData(instance);
		PropertyEntityData pred = createPropertyEntityData(slot, instance.getDirectType(), false);
		EntityData obj = createEntityData(object, false);				
		return new Triple(subj, pred, obj);
	}


	public static EntityData createEntityData(Object object) {
		return createEntityData(object, true);
	}

	public static List<EntityData> createEntityList(List<Object> list) {
		ArrayList<EntityData> edList = new ArrayList<EntityData>();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Object obj = (Object) iterator.next();
			edList.add(createEntityData(obj));			
		}
		return edList;
	}

	public static EntityData createEntityData(Object object, boolean computeAnnotations) {
		if (object == null) { return null;	}

		if (object instanceof Frame) {
			Frame objFrame = (Frame) object;
			EntityData entityData;
			if (objFrame instanceof Slot) {
				entityData = new PropertyEntityData(objFrame.getName(), getBrowserText(objFrame), null,
						(computeAnnotations) ? ChAOUtil.hasAnnotations(objFrame) : false);
				((PropertyEntityData)entityData).setPropertyType(OntologyServiceImpl.getPropertyType((Slot) objFrame));
			} else {
				entityData = new EntityData(objFrame.getName(), getBrowserText(objFrame), null,
					(computeAnnotations) ? ChAOUtil.hasAnnotations(objFrame) : false);
			}
			return entityData;
		} else {
			return new EntityData(object.toString());
		}
	}

	public static PropertyEntityData createPropertyEntityData(Slot property, Cls cls, boolean computeAnnotations) {
		if (property == null) { return null; }
		PropertyEntityData ped = new PropertyEntityData(property.getName(), getBrowserText(property),
				null, computeAnnotations ? ChAOUtil.hasAnnotations(property) : false);
		ped.setMinCardinality(cls == null ?
				property.getMinimumCardinality() : 
					cls.getTemplateSlotMinimumCardinality(property));
		ped.setMaxCardinality(cls == null ? 
				property.getMaximumCardinality() :
					cls.getTemplateSlotMaximumCardinality(property));
		if (property.getValueType() == ValueType.STRING) {
			ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.String);
		} else if (property.getValueType() == ValueType.INSTANCE || property.getValueType() == ValueType.CLS) {
			ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance);
			ped.setAllowedValues(createEntityList((List)property.getAllowedClses())); //TODO: fix me
		} else if (property.getValueType() == ValueType.BOOLEAN) {
			ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Boolean);
		} else if (property.getValueType() == ValueType.SYMBOL) {
			ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Symbol);
			ped.setAllowedValues(createEntityList((List<Object>) property.getAllowedValues()));
		} else {
			ped.setValueType(edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Any);
		}
		return ped;
	}

	public ArrayList getIndividuals(String projectName, String className) {
		ArrayList<EntityData> instancesData = new ArrayList<EntityData>();

		Project project = getProject(projectName);

		if (project == null) {
			return instancesData;
		}

		KnowledgeBase kb = project.getKnowledgeBase();
		Cls cls = kb.getCls(className);

		if (cls == null) {
			return instancesData;
		}

		ArrayList instances = new ArrayList(cls.getDirectInstances());
		Collections.sort(instances, new FrameComparator());

		for (Iterator iterator = instances.iterator(); iterator.hasNext();) {
			Instance inst = (Instance) iterator.next();

			if (inst.isVisible()) {
				instancesData.add(createEntityData(inst));
			}
		}

		return instancesData;
	}

	public ArrayList<NotesData> getNotes(String projectName, String entityName, boolean topLevelOnly) {
		ArrayList<NotesData> notes = new ArrayList<NotesData>();
		Project project = getProject(projectName);

		// Shortcut
		if (project == null) {
			return notes;
		}		

		KnowledgeBase kb = project.getKnowledgeBase();
		Frame frame = kb.getFrame(entityName);
		if (frame == null) {
			return notes;
		}

		Collection<Annotation> annotations = new ArrayList<Annotation>();
		if (topLevelOnly) {
			annotations = ChAOUtil.getTopLevelDiscussionThreads(kb);
		} else {
			Ontology_Component ontologyComponent = ChAOUtil.getOntologyComponent(frame, true);
			if (ontologyComponent == null) {
				return notes;
			}			
			annotations = new ArrayList<Annotation>(ontologyComponent.getAssociatedAnnotations());			
		}

		for (Annotation annotation : annotations) {
			notes.add(getDiscussionThread(annotation, ChAOKbManager.getChAOKb(kb)));
		}
		return notes;
	}


	public boolean hasNotes(String projectName, String entityName) {
		Project project = getProject(projectName);

		if (project == null) {
			return false;
		}

		Frame frame = project.getKnowledgeBase().getFrame(entityName);

		if (frame == null) {
			return false;
		}

		return ChAOUtil.hasAnnotations(frame);		
	}


	private NotesData getDiscussionThread(Annotation annotation, KnowledgeBase changesKb) {
		NotesData note = createNoteData(annotation, changesKb);

		for (Iterator<Annotation> iterator = annotation.getAssociatedAnnotations().iterator(); iterator.hasNext();) {
			Annotation reply = (Annotation) iterator.next();			
			note.addReply(getDiscussionThread(reply, changesKb));
		}

		return note;
	}

	private NotesData createNoteData(Annotation annotation, KnowledgeBase changesKb) {
		NotesData note = new NotesData();
		AnnotationFactory factory = new AnnotationFactory(changesKb);		
		note.setEntity(new EntityData(factory.getProtegeName(annotation)));
		note.setBody(annotation.getBody());
		note.setAuthor(annotation.getAuthor());
		note.setSubject(annotation.getSubject());
		Timestamp ts = (Timestamp)annotation.getCreated();
		if (ts != null) {
			note.setDate(ts.getDate());
		}		
		note.setType(factory.getAnnotationType(annotation));
		return note;
	}

	public ImportsData getImportedOntologies(String projectName) {
		Project project = getProject(projectName);
		ImportsData id = new ImportsData();

		if (isOWLOntology(project)) {
			// OWL ontology
			OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
			id = copyOWLTree(id, owlModel.getDefaultOWLOntology());
		} else {
			// Frames ontology
			Tree<URI> tree = project.getProjectTree();
			id = copyTree(id, tree, tree.getRoot());
		}

		return id;
	}

	public NotesData createNote(String projectName, NotesData newNote, boolean topLevel) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();		
		KnowledgeBase changeKb = ChAOUtil.getChangesKb(kb);
		if (changeKb == null) {
			return null; 
		}

		String type = newNote.getType();
		if (type == null || type.length() == 0) {
			type = "Comment";
		}
		AnnotationFactory factory = new AnnotationFactory(changeKb);	
		Annotation annotation = null;
		String annotatedEntityName = newNote.getAnnotatedEntity().getName();
		Instance annotatedInst = null;
		if (!topLevel && annotatedEntityName != null) {
			annotatedInst = kb.getInstance(annotatedEntityName);
			if (annotatedInst == null) {
				annotatedInst = changeKb.getInstance(annotatedEntityName);
			}
		}
		Cls annotationCls = changeKb.getCls(type);
		if (annotationCls == null) {
			annotationCls = factory.getCommentClass();
		}
		annotation = ChAOUtil.createAnnotationOnAnnotation(kb, annotatedInst, annotationCls);

		factory.fillDefaultValues(annotation);
		annotation.setAuthor(newNote.getAuthor());
		annotation.setBody(newNote.getBody());
		annotation.setSubject(newNote.getSubject());
		
		return createNoteData(annotation, changeKb);
	}


	private ImportsData copyTree(ImportsData data, Tree<URI> tree, URI node) {
		ImportsData id = data;
		id.setName(URIUtilities.getBaseName(node));

		Set<URI> children = tree.getChildren(node);
		for (URI childNode : children) {
			ImportsData childID = new ImportsData();
			childID = copyTree(childID, tree, childNode);
			id.addImport(childID);
		}

		return id;
	}

	private ImportsData copyOWLTree(ImportsData data, OWLOntology ontology) {
		ImportsData id = data;
		id.setName(ontology.getURI());

		Collection imports = ontology.getImportResources();
		for (Object o : imports) {
			OWLOntology childOnt = (OWLOntology) o;
			ImportsData childID = new ImportsData(childOnt.getURI());
			childID = copyOWLTree(childID, childOnt);
			id.addImport(childID);
		}

		return id;
	}

	public ArrayList getMetrics(String projectName) {
		ArrayList<MetricData> metrics = new ArrayList<MetricData>();
		Project project = getProject(projectName);

		if (isOWLOntology(project)) {
			// OWL ontology
			OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
			ModelMetrics modelMetrics = new ModelMetrics(owlModel);
			modelMetrics.calculateMetrics();

			Integer count = new Integer(modelMetrics.getNamedClassCount());
			metrics.add(new MetricData("Class count", count.toString()));

			count = new Integer(modelMetrics.getDatatypePropertyCount());
			metrics.add(new MetricData("Datatype property count", count.toString()));

			count = new Integer(modelMetrics.getObjectPropertyCount());
			metrics.add(new MetricData("Object property count", count.toString()));

			count = new Integer(modelMetrics.getAnnotationPropertyCount());
			metrics.add(new MetricData("Annotation property count", count.toString()));

			count = new Integer(modelMetrics.getOwlIndividualCount());
			metrics.add(new MetricData("Individual count", count.toString()));

			DLExpressivityChecker checker = new DLExpressivityChecker(owlModel);
			checker.check();
			metrics.add(new MetricData("DL Expressivity", checker.getDLName()));

		} else {
			// Frames ontology
			FrameCounts frameCounts = project.getKnowledgeBase().getFrameCounts();

			Integer count = new Integer(frameCounts.getTotalClsCount());
			metrics.add(new MetricData("Class count", count.toString()));

			count = new Integer(frameCounts.getTotalSlotCount());
			metrics.add(new MetricData("Slot count", count.toString()));

			count = new Integer(frameCounts.getTotalFacetCount());
			metrics.add(new MetricData("Facet count", count.toString()));

			count = new Integer(frameCounts.getTotalSimpleInstanceCount());
			metrics.add(new MetricData("Instance count", count.toString()));

			count = new Integer(frameCounts.getTotalFrameCount());
			metrics.add(new MetricData("Frame count", count.toString()));
		}

		return metrics;
	}

	private boolean isOWLOntology(Project project) {
		KnowledgeBase kb = project.getKnowledgeBase();
		return (kb instanceof OWLModel) ? true : false;
	}


	public ArrayList<AbstractEvent> getEvents(String projectName, long fromVersion) {
		ServerProject serverProject = ProjectManager.getProjectManager().getServerProject(projectName, false);
		if (serverProject == null) {
			throw new RuntimeException("Ontology " + projectName + " is not available from the server. Probably it is closed for maintenance.");
		}
		return serverProject.getEvents(fromVersion);
	}

	public EntityData createCls(String projectName, String clsName, String superClsName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Cls superCls = superClsName == null ?
				kb.getRootCls() : kb.getCls(superClsName);

				//set user
				EntityData clsEntity = null;
				Cls cls = null;
				try {
					if (isOWLOntology(project)) {
						if (superCls != null && superCls instanceof OWLNamedClass) {
							cls = ((OWLModel)kb).createOWLNamedSubclass(clsName, (OWLNamedClass)superCls);
						} else {
							cls = ((OWLModel)kb).createOWLNamedClass(clsName);
						}
					} else {
						cls = kb.createCls(clsName, 
								superCls == null ? kb.getRootClses() : CollectionUtilities.createCollection(superCls));
					}

					if (cls != null) {
						clsEntity = createEntityData(cls, false);
					}
				} catch (Exception e) {
					Log.getLogger().log(Level.WARNING, "Error at creating class " + clsName, e);
				}	
				return clsEntity;
	}

	public void deleteCls(String projectName, String clsName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Cls cls = kb.getCls(clsName);
		if (cls == null) {
			return;
		}

		cls.delete();
	}

	public void addSuperCls(String projectName, String clsName,
			String superClsName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Cls cls = kb.getCls(clsName);
		Cls superCls = kb.getCls(superClsName);
		if (cls == null || superCls == null) {
			return;
		}

		cls.addDirectSuperclass(superCls);		
	}

	public void removeSuperCls(String projectName, String clsName,
			String superClsName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Cls cls = kb.getCls(clsName);
		Cls superCls = kb.getCls(superClsName);
		if (cls == null || superCls == null) {
			return;
		}

		cls.removeDirectSuperclass(superCls);		
	}

	public void moveCls(String projectName, String clsName, String oldParentName, String newParentName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Cls cls = kb.getCls(clsName);
		Cls oldParent = kb.getCls(oldParentName);
		Cls newParent = kb.getCls(newParentName);

		if (cls == null || oldParent == null || newParent == null) {
			return;
		}

		cls.addDirectSuperclass(newParent);
		cls.removeDirectSuperclass(oldParent);		
	}

	
	public EntityData createProperty(String projectName,
			String propertyName, String superPropName, PropertyType propertyType) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		if (kb.getFrame(propertyName) != null) {
			throw new RuntimeException("An entity with the same name already exists!");
		}
		
		if (isOWLOntology(project)) { //OWL
			OWLModel owlModel = (OWLModel) kb;			
			RDFProperty property = null;
			if (propertyType == PropertyType.OBJECT) {
				property = owlModel.createOWLObjectProperty(propertyName);
			} else if (propertyType == PropertyType.DATATYPE) {
				property = owlModel.createOWLDatatypeProperty(propertyName);
			} else if (propertyType == PropertyType.ANNOTATION) {
				property = owlModel.createAnnotationProperty(propertyName);
			}			
				
			if (superPropName != null) {
				RDFProperty superProp = owlModel.getRDFProperty(superPropName);
				if (superProp != null) {
					property.addSuperproperty(superProp);
				}
			}
			return createEntityData(property, false);
		} else { //Frames
			Slot slot = kb.createSlot(propertyName);
			slot.setValueType(propertyType == PropertyType.OBJECT ? ValueType.INSTANCE : ValueType.STRING);
			if (superPropName != null) {
				Slot superSlot = kb.getSlot(superPropName);
				if (superSlot != null) {
					slot.addDirectSuperslot(superSlot);
				}
			}
			return createEntityData(slot, false);
		}		
	}
	
	public EntityData createDatatypeProperty(String projectName,
			String propertyName, String superPropName) {
		return createProperty(projectName, propertyName, superPropName, PropertyType.DATATYPE);
	}


	public EntityData createObjectProperty(String projectName,
			String propertyName, String superPropName) {
		return createProperty(projectName, propertyName, superPropName, PropertyType.OBJECT);
	}


	public EntityData createAnnotationProperty(String projectName,
			String propertyName, String superPropName) {
		return createProperty(projectName, propertyName, superPropName, PropertyType.ANNOTATION);
	}


	public void deleteProperty(String projectName, String propertyName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Slot slot = kb.getSlot(propertyName);
		if (slot == null) {
			return;
		}

		slot.delete();
		
	}
	
	
	
	public EntityData createInstance(String projectName, String instName, String typeName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		
		Cls type = null;
		if (typeName == null) {
			type = kb.getRootCls();
		} else {
			type = kb.getCls(typeName);	
		}		
		
		if (type == null) {
			Log.getLogger().warning("Could not create instance " + instName + " of type " + typeName + ". Null type");
			throw new IllegalArgumentException("Could not create instance " + instName + " of type " + typeName + ". Null type");			
		}
		
		Instance inst = null;
		try {
			inst = type.createDirectInstance(instName);
		} catch (Exception e) {
			Log.getLogger().warning("Could not create instance " + instName + " of type " + typeName);
			throw new IllegalArgumentException("Could not create instance " + instName + " of type " + typeName);			
		}
		
		return createEntityData(inst);
	}

	public EntityData createInstanceValue(String projectName, String instName, String typeName, String subjectEntity, String propertyEntity) {
		EntityData valueData = createInstance(projectName, instName, typeName);
		if (valueData == null) { return null; }
		
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		Slot slot = kb.getSlot(propertyEntity);
		
		if (slot != null) {		
			PropertyEntityData propEntityData = createPropertyEntityData(slot, null, false);
			addPropertyValue(projectName, subjectEntity, propEntityData, valueData);
		}
		return valueData;
	}
	

	public EntityData renameEntity(String projectName, String oldName, String newName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		if (isOWLOntology(project)) {
			OWLModel owlModel = (OWLModel) kb;
			oldName = OWLUtil.getInternalFullName(owlModel, oldName);
			newName = OWLUtil.getInternalFullName(owlModel, newName);						
		}

		Frame oldFrame = kb.getFrame(oldName);
		Frame newFrame = kb.getFrame(newName);

		if (oldFrame == null || newFrame != null) {
			return null; //TODO: throw exception
		}

		newFrame = oldFrame.rename(newName);

		return createEntityData(newFrame);
	}


	public String getRestrictionHtml(String projectName, String className) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		if (!(kb instanceof OWLModel)) {
			return "";
		}
		OWLModel owlModel = (OWLModel) kb;
		OWLNamedClass cls = owlModel.getOWLNamedClass(className);
		if (cls == null) {
			return "";
		}

		StringBuffer buffer = new StringBuffer();				
		buffer.append(getEquivalentClassesHtml(cls));		
		buffer.append(getSuperClassesHtml(cls));
		return buffer.toString();	
	}

	private StringBuffer getEquivalentClassesHtml(OWLNamedClass cls) {
		StringBuffer buffer = new StringBuffer();		
		buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");		

		List<RDFSClass> equivClasses = (List<RDFSClass>) cls.getEquivalentClasses();
		if (equivClasses.size() > 0) {
			buffer.append("<div class=\"restiction_title\">Equivalent classes (Necessary and Sufficient conditions)</div>");
		}
		Collections.sort(equivClasses, new RestrictionComparator());

		for (RDFSClass equivClass : equivClasses) {

			if (equivClass instanceof OWLIntersectionClass) {
				List<RDFSClass> operands = (List<RDFSClass>) ((OWLIntersectionClass)equivClass).getOperands();
				Collections.sort(operands, new RestrictionComparator());
				for (RDFSClass operand : operands) {
					buffer.append("<tr><td>");
					buffer.append(getConditionHtmlString(operand));
					buffer.append("</td></tr>");
				}
			} else {
				buffer.append("<tr><td>");
				buffer.append(getConditionHtmlString(equivClass));
				buffer.append("</td></tr>");				
			}			
		}

		buffer.append("</table>");
		return buffer;		
	}


	private StringBuffer getSuperClassesHtml(OWLNamedClass cls) {
		StringBuffer buffer = new StringBuffer();		
		buffer.append("<table width=\"100%\" border=\"0\" cellspacing=\"3\"  class=\"restriction_table\">");		

		List<RDFSClass> superClasses = new ArrayList<RDFSClass>(cls.getPropertyValues(cls.getOWLModel().getRDFSSubClassOfProperty()));
		if (superClasses.size() > 0) {
			buffer.append("<hr>");
			buffer.append("<div class=\"restiction_title\">Superclasses (Necessary conditions)</div>");
		}
		Collections.sort(superClasses, new RestrictionComparator());

		for (RDFSClass superCls : superClasses) {			
			buffer.append("<tr><td>");
			buffer.append(getConditionHtmlString(superCls));
			buffer.append("</td></tr>");
		}

		buffer.append("</table>");
		return buffer;		
	}


	private static final String delimsStrs[] = {"and", "or", "not", "some", "only", "has", "min", "exactly", "max"};
	private static List<String> delims = Arrays.asList(delimsStrs);

	private String getConditionHtmlString(RDFSClass cls) {
		StringBuffer buffer = new StringBuffer();
		StringTokenizer st = new StringTokenizer(cls.getBrowserText(), " \t\n\r\f", true);
		while(st.hasMoreTokens()) {
			final String token = st.nextToken();
			if (delims.contains(token)) {
				buffer.append("<span class=\"restriction_delim\">");
				buffer.append(token);
				buffer.append("</span>");
			} else {
				buffer.append(token);
			}
		}

		return buffer.toString();
	}

	class RestrictionComparator implements Comparator<RDFSClass> {
		public int compare(RDFSClass cls1, RDFSClass cls2) {
			if (cls1 instanceof RDFSNamedClass && cls2 instanceof RDFSNamedClass) {
				return cls1.getBrowserText().compareTo(cls2.getBrowserText());
			}			
			if (cls1 instanceof RDFSNamedClass && !(cls2 instanceof RDFSNamedClass)) {
				return -1;
			}			
			if (!(cls1 instanceof RDFSNamedClass) && cls2 instanceof RDFSNamedClass) {
				return 1;
			}
			//for all other cases
			return cls1.getBrowserText().compareTo(cls2.getBrowserText());
		}		
	}

	public Boolean hasWritePermission(String projectName, String userName) {
		Project project = getProject(projectName);		
		if (project == null) {	return Boolean.FALSE; }
		if (project instanceof RemoteClientProject) {//is remote project
			RemoteClientProject remoteClientProject = (RemoteClientProject) project;
			RemoteServer server = remoteClientProject.getServer();
			boolean allowed = false;
			try {
				//using a bogus session with the correct user name
				allowed = server.isOperationAllowed(new Session(userName, "(from web protege)"), MetaProjectConstants.OPERATION_WRITE, projectName);	
			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Error at remote call: isOperationAllowed for " + projectName, e);
			}
			return allowed;
		}
		//TODO: in standalone it always returns true - make it work with the metaproject
		return true;
	}

	public ArrayList<EntityData> search(String projectName, String searchString) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		if (!searchString.startsWith("*")) { searchString = "*" + searchString;}

		Collection<Frame> frames = new HashSet<Frame>(kb.getMatchingFrames(kb.getSystemFrames().getNameSlot(), null, false, searchString, -1));
		if (isOWLOntology(project)) {
			frames.addAll(kb.getMatchingFrames(((OWLSystemFrames)kb.getSystemFrames()).getRdfsLabelProperty(), null, false, searchString, -1));
		}

		ArrayList<Frame> sortedFrame = new ArrayList<Frame>(frames);
		Collections.sort(sortedFrame, new FrameComparator());

		ArrayList<EntityData> results = new ArrayList<EntityData>();
		for (Frame frame : sortedFrame) {
			if (!frame.isSystem()) {
				results.add(createEntityData(frame));
			}
		}

		return results;
	}

	public ArrayList<EntityData> getPathToRoot(String projectName, String entityName) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		ArrayList<EntityData> path = new ArrayList<EntityData>();

		//for now it works only with classes 
		Cls entity = kb.getCls(entityName);

		if (entity == null) {
			return path;
		}

		List clsPath = ModelUtilities.getPathToRoot(entity);

		for (Iterator iterator = clsPath.iterator(); iterator.hasNext();) {
			Cls cls = (Cls) iterator.next();
			path.add(createEntityData(cls, false));
		}

		return path;
	}

	private static String PREFERRED_NAME_PROP = "Preferred_Name";

	//TODO: search methods to be refactored in another class/interface

	public String getBioPortalSearchContent(String projectName,
			String entityName, BioPortalSearchData bpSearchData) {
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		Frame frame = kb.getFrame(entityName);
		if (frame == null) {
			return URLUtil.getURLContent(getBioPortalSearchUrl(entityName, bpSearchData));
		}	
		//try to search the preferred name, if exists
		if (kb instanceof OWLModel) {
			RDFProperty preferredNameProp = ((OWLModel)kb).getRDFProperty(PREFERRED_NAME_PROP);
			if (preferredNameProp != null) {
				try {
					String preferredName = (String) frame.getOwnSlotValue(preferredNameProp);
					String prefNameSearch = URLUtil.getURLContent(getBioPortalSearchUrl(preferredName, bpSearchData));
					if (prefNameSearch != null && prefNameSearch.indexOf("searchBean") > 0) {
						return prefNameSearch;
					}					
				} catch (Exception e) {}
			}
		}
		//try to search the browser text
		String nameSearch = URLUtil.getURLContent(getBioPortalSearchUrl(getBrowserText(frame), bpSearchData));
		if (nameSearch != null && nameSearch.indexOf("searchBean") > 0) {
			return nameSearch;
		}
		//search first rdfs:label if exist.. 
		if (frame instanceof RDFResource) {
			RDFProperty rdfsLabelProp = ((OWLModel)kb).getSystemFrames().getRdfsLabelProperty();
			Object rdfsLabelO = kb.getOwnSlotValue(frame, rdfsLabelProp);
			if (rdfsLabelO != null) {
				String rdfsLabelString = "";
				rdfsLabelString = rdfsLabelO instanceof RDFSLiteral ?
						((RDFSLiteral) rdfsLabelO).getString() : rdfsLabelO.toString();
						return URLUtil.getURLContent(getBioPortalSearchUrl(rdfsLabelString, bpSearchData));
			}
		}
		return "";	
	}

	private static String getBioPortalSearchUrl(String text, BioPortalSearchData bpSearchData) {
		text = text.replaceAll(" ", "%20");
		return bpSearchData.getBpSearchUrl() + text + createSearchUrlQueryString(bpSearchData);
	}
	
	private static String createSearchUrlQueryString(BioPortalSearchData bpSearchData) {
		String res = "";
		String ontIds = bpSearchData.getSearchOntologyIds();
		String pgOpt = bpSearchData.getSearchPageOption();
		boolean firstSep = true;
		if (ontIds != null ) {
			res += (firstSep ? "?" : "&") + "ontologyids=" + ontIds;
			firstSep = false;
		}
		if (pgOpt != null) {
			res += (firstSep ? "?" : "&") + pgOpt;
			firstSep = false;
		}
		return res;
	}


	public boolean importBioPortalConcept(String projectName, String entityName, BioPortalReferenceData bpRefData) {
		//System.out.println("importBioPortalConcept " + projectName + " - " +entityName + " - " + bpRefData);
		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		// magic constants will go away  with the merge from the who branch. temporary hack so things will compile.
		ReferenceModel referenceModel = new ReferenceModel(kb, bpRefData.createAsClass(), bpRefData.getReferenceClassName(),
		                                                   "url", "ontologyName", null, 
		                                                   "ontologyId", "conceptId", null,
		                                                   "preferredTerm");
		Instance refInstance = referenceModel.createReference(
				bpRefData.getBpBaseUrl(), bpRefData.getConceptId(), bpRefData.getOntologyVersionId(), 
				bpRefData.getPreferredName(), bpRefData.getOntologyName());
		if (refInstance == null) {
			return false;
		}
		
		Instance instance = kb instanceof OWLModel ? ((OWLModel)kb).getRDFResource(entityName) : kb.getInstance(entityName);
		if (instance == null ) {
			return false;
		}
		String referenceProperty = bpRefData.getReferencePropertyName();
		Slot slot = kb instanceof OWLModel ? ((OWLModel)kb).getRDFProperty(referenceProperty) : kb.getSlot(referenceProperty);
		if (slot == null ) {
			//TODO check if we want to keep this code or not. If not, what should we do when referenceProprerty is not specified or does not exist in the ontology
			if (kb instanceof OWLModel) {
				slot = ((OWLModel)kb).createRDFProperty(referenceProperty);
			}
			else {
				slot = kb.createSlot(referenceProperty);
			}
		}
		
		instance.addOwnSlotValue(slot, refInstance);
		return true;
	}

}
