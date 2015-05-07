package edu.stanford.bmir.protege.web.server.bioportal;

public class BioPortalServerConstants {

    public static final String BP_BASE = "http://bioportal.bioontology.org/";
    public static final String BP_REST_BASE = "http://rest.bioontology.org/bioportal/";

    public static final String CONCEPTS_REST = "concepts";
    public static final String ROOTS_REST = "root";
    public static String PATH_REST = "path";
    public static final String ONTOLOGIES_REST = "ontologies";
    public static final String VISUALIZE_REST = "visualize";
    public static final String SEARCH_REST = "search";

    public final static String CONCEPT_ID_PARAM = "conceptid";
    public final static String API_KEY_PARAM = "apikey";

    public final static String BP_PRODUCTION_PROTEGE_API_KEY_VALUE = "8fadfa2c-47de-4487-a1f5-b7af7378d693";

    public static final String BP_PRODUCTION_PROTEGE_API_KEY = API_KEY_PARAM + "=" + BP_PRODUCTION_PROTEGE_API_KEY_VALUE;

    public static final String RECORD_TYPE_PREFERRED_NAME = "apreferredname";
    public static final String RECORD_TYPE_CONCEPT_ID = "bconceptid";
    public static final String RECORD_TYPE_SYNONYM = "csynonym";
    public static final String RECORD_TYPE_PROPERTY = "dproperty";

}
