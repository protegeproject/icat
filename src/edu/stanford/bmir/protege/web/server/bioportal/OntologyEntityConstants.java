package edu.stanford.bmir.protege.web.server.bioportal;

import org.ncbo.stanford.util.ProvenanceConstants;

public class OntologyEntityConstants {

    /*
     * Frame ontologies
     */

	public final static String CLS_REFERENCE ="ExternalReference";
	public final static String SLOT_URL ="url";
	public final static String SLOT_ONTOLOGY_NAME ="ontologyName";
	public final static String SLOT_ONTOLOGY_NAME2 = null;
	public final static String SLOT_PREFERRED_TERM = "preferredTerm";
	public final static String SLOT_CONCEPT_ID ="conceptId";
	public final static String SLOT_CONCEPT_ID2_SHORT = null;
	public final static String SLOT_ONTOLOGY_ID ="ontologyId";
	public final static String SLOT_VIEW_ON_ONTOLOGY_ID ="viewOnOntologyId";

	public final static String CLS_REFRERENCE_TERM_SUPERCLASS ="ExternalReferenceTerm";

	/*
	 * OWL ontologies
	 */

	public final static String BP_NAMESPACE = ProvenanceConstants.BP_NS;
	public final static String BP_PREFIX = ProvenanceConstants.BP_PREFIX;

	public final static String CLASS_REFERENCE = CLS_REFERENCE;
	public final static String PROPERTY_URL ="url";

	public final static String PROPERTY_ONTOLOGY_NAME2 ="ontologyId";
	public final static String PROPERTY_PREFERRED_TERM = "label";
	public final static String PROPERTY_CONCEPT_ID ="termId";

	public final static String PROPERTY_ONTOLOGY_NAME = ProvenanceConstants.BP_ONT_LABEL_PROP;
	public final static String PROPERTY_CONCEPT_ID_SHORT = ProvenanceConstants.BP_SHORT_TERM_ID_PROP;
	public final static String PROPERTY_ONTOLOGY_ID = ProvenanceConstants.BP_ONT_ID_PROP;
	public final static String PROPERTY_VIEW_ON_ONTOLOGY_ID = ProvenanceConstants.BP_VIEW_ON_ONT_ID_PROP;

	public final static String CLASS_REFRERENCE_TERM_SUPERCLASS = CLS_REFRERENCE_TERM_SUPERCLASS;

}
