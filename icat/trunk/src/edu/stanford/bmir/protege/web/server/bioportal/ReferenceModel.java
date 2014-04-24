package edu.stanford.bmir.protege.web.server.bioportal;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.ncbo.stanford.util.HTMLUtil;

import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class ReferenceModel {
	private KnowledgeBase kb;
	private Cls reference_cls;
	private Slot url_slot;
	private Slot ontologyName_slot;
	private Slot ontologyName2_slot = null;
	private Slot preferredTerm_slot;
	private Cls referenceTerm_supercls;
	private Slot conceptId_slot;
	private Slot conceptIdShort_slot = null;
	private Slot ontologyId_slot;

	private boolean importFromOriginalOntology;
	private boolean createAsClass;
	private String referenceClassName;


	public ReferenceModel(KnowledgeBase kb, boolean importFromOriginalOntology, boolean createAsClass, String referenceClassName,
			String url_slot_name, String ontologyName_slot_name,
			String ontologyId_slot_name, String conceptId_slot_name,
			String preferredTerm_slot_name) {

		this.kb = kb;
		this.importFromOriginalOntology = importFromOriginalOntology;
		this.createAsClass = createAsClass;
		this.referenceClassName = referenceClassName;
		init(url_slot_name, ontologyName_slot_name, ontologyId_slot_name, conceptId_slot_name, preferredTerm_slot_name);
		initBpPrefix();
	}

	public ReferenceModel(KnowledgeBase kb, boolean importFromOriginalOntology, boolean createAsClass, String referenceClassName,
			String url_slot_name, String ontologyName_slot_name, String ontologyName2_slot_name,
			String ontologyId_slot_name, String conceptId_slot_name, String conceptId2_slot_name,
			String preferredTerm_slot_name) {

		this(kb, importFromOriginalOntology, createAsClass, referenceClassName,
				url_slot_name, ontologyName_slot_name,
				ontologyId_slot_name, conceptId_slot_name,
				preferredTerm_slot_name);
		setAlternativeSlotNames(ontologyName2_slot_name, conceptId2_slot_name);
	}

	private void initBpPrefix() {
	    if (kb instanceof OWLModel && !kb.getProject().isMultiUserClient()) { //setting prefixes in the client does not work
	        OWLModel owlModel = (OWLModel) kb;
	        String prefix = owlModel.getNamespaceManager().getPrefix(OntologyEntityConstants.BP_NAMESPACE);
	        if (prefix == null) {
	            owlModel.getNamespaceManager().setPrefix(OntologyEntityConstants.BP_NAMESPACE, OntologyEntityConstants.BP_PREFIX);
	        }
	    }
	}

	//ugly implementation - should be split in two classes: frames and owl
	private void init(String url_slot_name, String ontologyName_slot_name,
			String ontology_id_slot_name, String concept_id_slot_name, String preferredTerm_slot_name) {
		if (kb instanceof OWLModel) {
			url_slot = getOrCreateSlot(
					(url_slot_name==null || url_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_URL) :
							url_slot_name);
			ontologyName_slot = getOrCreateSlot(
					(ontologyName_slot_name==null || ontologyName_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_ONTOLOGY_NAME) :
							ontologyName_slot_name);
			ontologyId_slot = getOrCreateSlot(
					(ontology_id_slot_name==null || ontology_id_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_ONTOLOGY_ID) :
							ontology_id_slot_name);
			conceptId_slot = getOrCreateSlot(
					(concept_id_slot_name==null || concept_id_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_CONCEPT_ID) :
							concept_id_slot_name);
			preferredTerm_slot = getOrCreateSlot(
					(preferredTerm_slot_name==null || preferredTerm_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_PREFERRED_TERM) :
							preferredTerm_slot_name);
		}
		else {
			url_slot = getOrCreateSlot(
					(url_slot_name==null || url_slot_name.length()==0) ? OntologyEntityConstants.SLOT_URL : url_slot_name);
			ontologyName_slot = getOrCreateSlot(
					(ontologyName_slot_name==null || ontologyName_slot_name.length()==0) ? OntologyEntityConstants.SLOT_ONTOLOGY_NAME : ontologyName_slot_name);
			ontologyId_slot = getOrCreateSlot(
					(ontology_id_slot_name==null || ontology_id_slot_name.length()==0) ? OntologyEntityConstants.SLOT_ONTOLOGY_ID : ontology_id_slot_name);
			conceptId_slot = getOrCreateSlot(
					(concept_id_slot_name==null || concept_id_slot_name.length()==0) ? OntologyEntityConstants.SLOT_CONCEPT_ID : concept_id_slot_name);
			preferredTerm_slot = getOrCreateSlot(
					(preferredTerm_slot_name==null || preferredTerm_slot_name.length()==0) ? OntologyEntityConstants.SLOT_PREFERRED_TERM : preferredTerm_slot_name);
		}

		reference_cls = createReferenceClass();
	}

	public void setAlternativeSlotNames(String ontologyName2_slot_name, String conceptId2_slot_name) {
		if (kb instanceof OWLModel) {
			ontologyName2_slot = getOrCreateSlot(
					(ontologyName2_slot_name==null || ontologyName2_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_ONTOLOGY_NAME2) :
							ontologyName2_slot_name);
			conceptIdShort_slot = getOrCreateSlot(
					(conceptId2_slot_name==null || conceptId2_slot_name.length()==0) ?
							ProtegeUtil.fixNamespace((OWLModel)kb, OntologyEntityConstants.PROPERTY_CONCEPT_ID_SHORT) :
							conceptId2_slot_name);
		}
		else {
			ontologyName2_slot = getOrCreateSlot(
					(ontologyName2_slot_name==null || ontologyName2_slot_name.length()==0) ? OntologyEntityConstants.SLOT_ONTOLOGY_NAME2 : ontologyName2_slot_name);
			conceptIdShort_slot = getOrCreateSlot(
					(conceptId2_slot_name==null || conceptId2_slot_name.length()==0) ? OntologyEntityConstants.SLOT_CONCEPT_ID2_SHORT : conceptId2_slot_name);
		}
	}


	private Slot getOrCreateSlot(String name) {
		if (name == null || name.length() == 0) {
			return null;
		}

		Slot slot = kb instanceof OWLModel ?
				((OWLModel)kb).getRDFProperty(name) :
				kb.getSlot(name);
		if (slot == null) {
			slot = kb instanceof OWLModel ?
					((OWLModel)kb).createAnnotationProperty(name) :
					kb.createSlot(name);
		}
		return slot;
	}

	private Cls createReferenceClass() {
		reference_cls = kb instanceof OWLModel ?
				((OWLModel)kb).getRDFSNamedClass(referenceClassName) :
				kb.getCls(referenceClassName);
		if (reference_cls == null) {
			if (!createAsClass) {
				reference_cls = kb instanceof OWLModel ?
						((OWLModel)kb).createOWLNamedClass(referenceClassName) :
						kb.createCls(referenceClassName, kb.getRootClses());
			} else {
				if (kb instanceof OWLModel) {
					reference_cls = ((OWLModel)kb).createOWLNamedClass(referenceClassName);
				} else {
					reference_cls = kb.createCls(referenceClassName, CollectionUtilities.createCollection(kb.getSystemFrames().getStandardClsMetaCls()));
				}
			}
			reference_cls.addDirectTemplateSlot(url_slot);
			reference_cls.addDirectTemplateSlot(ontologyName_slot);
			reference_cls.addDirectTemplateSlot(conceptId_slot);
			reference_cls.addDirectTemplateSlot(ontologyId_slot);
			reference_cls.addDirectTemplateSlot(preferredTerm_slot);

			ArrayList<Object> browserSlotElems = new ArrayList<Object>();
			browserSlotElems.add(preferredTerm_slot);
			browserSlotElems.add(" from ");
			browserSlotElems.add(ontologyName_slot);
			reference_cls.setDirectBrowserSlotPattern(new BrowserSlotPattern(browserSlotElems));

		}

		if (createAsClass && !(kb instanceof OWLModel)) {
			Cls superMetaCls = kb.getSystemFrames().getStandardClsMetaCls();
			if (!reference_cls.hasSuperclass(superMetaCls)) {
				reference_cls.addDirectSuperclass(superMetaCls);
				reference_cls.removeDirectSuperclass(kb.getRootCls());
			}
		}

		if (createAsClass && !(kb instanceof OWLModel)) {
			referenceTerm_supercls = kb instanceof OWLModel ?
					((OWLModel)kb).getOWLNamedClass(OntologyEntityConstants.CLASS_REFRERENCE_TERM_SUPERCLASS):
					kb.getCls(OntologyEntityConstants.CLS_REFRERENCE_TERM_SUPERCLASS);
			if (referenceTerm_supercls == null) {
				referenceTerm_supercls = kb instanceof OWLModel ?
					((OWLModel)kb).createOWLNamedClass(OntologyEntityConstants.CLASS_REFRERENCE_TERM_SUPERCLASS) :
					kb.createCls(OntologyEntityConstants.CLS_REFRERENCE_TERM_SUPERCLASS, kb.getRootClses());
			}
		}

		return reference_cls;
	}


	public KnowledgeBase getKb() {
		return kb;
	}

	public Cls getReference_cls() {
		return reference_cls;
	}

	public Slot getUrl_slot() {
		return url_slot;
	}

	public Slot getOntologyName_slot() {
		return ontologyName_slot;
	}

	public Slot getOntologyName2_slot() {
		return ontologyName2_slot;
	}

	public Slot getPreferredTerm_slot() {
		return preferredTerm_slot;
	}

	public Cls getReferenceTerm_supercls() {
		return referenceTerm_supercls;
	}

	public Slot getConceptId_slot() {
		return conceptId_slot;
	}

	public Slot getConceptIdShort_slot() {
		return conceptIdShort_slot;
	}

	public Slot getOntologyId_slot() {
		return ontologyId_slot;
	}

	public void setReference_cls(String className) {
		if (reference_cls.getName().equals(className)) { return; }
		if (reference_cls.getInstanceCount() == 0) {
			reference_cls.delete();
		}
		referenceClassName = className;
		this.reference_cls = createReferenceClass();
	}

	public Instance createReference(String bpUrl, String conceptId, String conceptIdShort,
			String ontologyVersionId, String viewOnOntologyVersionId,
			String preferredName, String ontologyName, String viewOnOntologyName) {
		Cls referenceCls = getReference_cls();
		if (referenceCls == null) { return null; }
		Instance inst = null;
		if (referenceCls instanceof RDFSNamedClass) {
			if (referenceCls.isMetaCls()) {
				inst = ((OWLModel)kb).createOWLNamedClass(null);
				((Cls)inst).addDirectSuperclass(referenceCls);
				((Cls)inst).removeDirectSuperclass(kb.getRootCls());
			} else {
				inst = ((RDFSNamedClass)referenceCls).createRDFIndividual(null);
			}
		} else {
			inst = referenceCls.createDirectInstance(null);
		}
		//update method arguments if necessary
		String localConceptIdShort = extractNumericLocalConceptId(conceptIdShort);
		if (importFromOriginalOntology && viewOnOntologyVersionId != null) {
			String localConceptId = extractNumericLocalConceptId(conceptId);
			try {
				bpUrl = bpUrl.replace(HTMLUtil.encodeURI(conceptId), HTMLUtil.encodeURI(localConceptId));
			} catch (UnsupportedEncodingException e) {
			} finally {
				bpUrl = bpUrl.replace(conceptId, localConceptId);
			}
			try {
				bpUrl = bpUrl.replace(HTMLUtil.encodeURI(conceptIdShort), HTMLUtil.encodeURI(localConceptIdShort));
			} catch (UnsupportedEncodingException e) {
			} finally {
				bpUrl = bpUrl.replace(conceptIdShort, localConceptIdShort);
			}
			bpUrl = bpUrl.replace(ontologyVersionId, viewOnOntologyVersionId);
			conceptId = localConceptId;
			ontologyVersionId = viewOnOntologyVersionId;
			if (viewOnOntologyName != null && viewOnOntologyName.length() > 0) {
				ontologyName = viewOnOntologyName;
			}
		}

		inst.setOwnSlotValue(getConceptId_slot(), conceptId);
		if (getConceptIdShort_slot() != null) {
			inst.setOwnSlotValue(getConceptIdShort_slot(), localConceptIdShort);
		}
		inst.setOwnSlotValue(getOntologyId_slot(), ontologyVersionId);
		inst.setOwnSlotValue(getUrl_slot(), bpUrl);
		inst.setOwnSlotValue(getPreferredTerm_slot(), preferredName);
		inst.setOwnSlotValue(getOntologyName_slot(), ontologyName);
		if (getOntologyName2_slot() != null) {
			inst.setOwnSlotValue(getOntologyName2_slot(), ontologyName);
		}
		if (inst instanceof Cls && getReferenceTerm_supercls() != null) {
			Cls cls = (Cls) inst;
			cls.addDirectSuperclass(getReferenceTerm_supercls());
		}
		return inst;
	}

	/**
	 * Extracts the short concept ID (i.e. the local name) from a RDF ID
	 * that has "digits only" local name.
	 *
	 * @param conceptId an arbitrary BP conceptId
	 * @return
	 */
	private String extractNumericLocalConceptId(String conceptId) {
        int indexSep = conceptId.lastIndexOf("#");
        if (indexSep >= 0) {
            String localName = conceptId.substring(indexSep + 1);
            if (localName.matches("\\d+")) {
                 return localName;
            }
        }

        indexSep = conceptId.lastIndexOf("/");
        if (indexSep >= 0) {
            String localName = conceptId.substring(indexSep + 1);
            if (localName.matches("\\d+")) {
                return localName;
            }
        }

        return conceptId;
	}

}
