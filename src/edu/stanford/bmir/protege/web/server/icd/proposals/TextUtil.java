package edu.stanford.bmir.protege.web.server.icd.proposals;

import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;

public class TextUtil {

	public static final String getPropertyName(OWLModel owlModel, String propertyId) {
		RDFProperty prop = owlModel.getRDFProperty(propertyId);
		return prop == null ? "(property not found)" : prop.getBrowserText(); 		
	}
	
}
