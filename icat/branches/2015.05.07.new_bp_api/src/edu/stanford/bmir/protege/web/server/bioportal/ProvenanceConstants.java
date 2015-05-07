package edu.stanford.bmir.protege.web.server.bioportal;

public class ProvenanceConstants {

    /*
     * General Provenance
     */

    public final static String PROVENANCE_ONT_NAME = "http://protege.stanford.edu/ontologies/metadata/provenance.owl";
    public final static String PROVENANCE_NS = PROVENANCE_ONT_NAME + "#";
    public final static String PROVENANCE_PREFIX = "prov";


    public final static String PROVENANCE_IMPORT_CREATOR_PROP = PROVENANCE_NS + "importCreator";
    public final static String PROVENANCE_IMPORT_DATE_PROP = PROVENANCE_NS + "importDate";

    public final static String PROVENANCE_ONT_NAME_PROP = PROVENANCE_NS + "ontologyName";
    public final static String PROVENANCE_ONT_VERSION_PROP = PROVENANCE_NS + "ontologyVersion";

    public final static String PROVENANCE_TERM_LABEL_PROP = PROVENANCE_NS + "termLabel";
    public final static String PROVENANCE_TERM_ID_PROP = PROVENANCE_NS + "termId";
    public final static String PROVENANCE_TERM_URL_PROP = PROVENANCE_NS + "termUrl";


    /*
     * BioPortal
     */

    public final static String BP_NS = "http://bioportal.bioontology.org#";
    public final static String BP_PREFIX = "bp";

    public final static String BP_ONT_ID_PROP = BP_NS + "ontologyId";
    //sup-property of prov:ontologyName
    public final static String BP_ONT_LABEL_PROP = BP_NS + "ontologyLabel";

    //not needed anymore
    public final static String BP_SHORT_TERM_ID_PROP = BP_NS + "shortTermId";

    public final static String BP_VIEW_ON_ONT_ID_PROP = BP_NS + "viewOnOntologyId";

}
