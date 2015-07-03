package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;

public class ImportProposalsUtil {
	
	private static final String SEPARATOR_INPUT_PROP = "icd.proposals.separator.input";
	private static final String SEPARATOR_INPUT_DEFAULT = "\\|";
	private static final String SEPARATOR_OUTPUT_PROP = "icd.proposals.separator.output";
	private static final String SEPARATOR_OUTPUT_DEFAULT = "|";
	
	private static String RESULT_OUTPUT_FILE_PATH_PROP = "icd.proposals.output.path";
	private static String RESULT_OUTPUT_FILE_PATH_DEFAULT = "icd.proposals.import.result";
	private static String RESULT_OUTPUT_FILE_MAX_SIZE_PROP = "icd.proposals.output.maxsize.in.bytes";
	private static int RESULT_OUTPUT_FILE_MAX_SIZE_DEFAULT = 10 * 1000000; //10 MB

	public static final String getPropertyName(OWLModel owlModel, String propertyId) {
		RDFProperty prop = owlModel.getRDFProperty(propertyId);
		return prop == null ? "(property not found)" : prop.getBrowserText(); 		
	}
	
	public static String getInputSeparator() {
		return ApplicationProperties.getString(SEPARATOR_INPUT_PROP, SEPARATOR_INPUT_DEFAULT);
	}
	
	public static String getOutputSeparator() {
		return ApplicationProperties.getString(SEPARATOR_OUTPUT_PROP, SEPARATOR_OUTPUT_DEFAULT);
	}
	
	public static String getResultOutputPath() {
		return ApplicationProperties.getString(RESULT_OUTPUT_FILE_PATH_PROP, RESULT_OUTPUT_FILE_PATH_DEFAULT);
	}
	
	public static int getResultOutputFileMaxSize() {
		return ApplicationProperties.getIntegerProperty(RESULT_OUTPUT_FILE_MAX_SIZE_PROP, RESULT_OUTPUT_FILE_MAX_SIZE_DEFAULT);
	}
	
}
