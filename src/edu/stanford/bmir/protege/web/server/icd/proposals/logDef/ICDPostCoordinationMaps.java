package edu.stanford.bmir.protege.web.server.icd.proposals.logDef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;

public class ICDPostCoordinationMaps {
	
	//TODO: External causes properties not treated currently
	
	public static final List<String> fixedScalePCPropIds = new ArrayList<>(
		Arrays.asList(
				ICDContentModelConstants.PC_AXIS_ETIOLOGY_CAUSALITY,
				ICDContentModelConstants.PC_AXIS_TOPOLOGY_LATERALITY,
				ICDContentModelConstants.PC_AXIS_TOPOLOGY_RELATIONAL, 
				ICDContentModelConstants.PC_AXIS_TOPOLOGY_DISTRIBUTION,
				ICDContentModelConstants.PC_AXIS_TOPOLOGY_REGIONAL,
				ICDContentModelConstants.PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_FRACTURE_SUBTYPE, 
				ICDContentModelConstants.PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_OPEN_OR_CLOSED, 
				ICDContentModelConstants.PC_AXIS_INJURY_QUALIFIER_FRACTURE_QUALIFIER_JOINT_INVOLVEMENT_IN_FRACTURE_SUBTYPE,
				ICDContentModelConstants.PC_AXIS_INJURY_QUALIFIER_TYPE_OF_INJURY,
				ICDContentModelConstants.PC_AXIS_INJURY_QUALIFIER_BURN_QUALIFIER,
				ICDContentModelConstants.PC_AXIS_CONSCIOUSNESS_MEASURE_DURATION_OF_COMA,
				ICDContentModelConstants.PC_AXIS_LEVEL_OF_CONSCIOUSNESS
		)
	);
	
	
	public static final List<String> hierarchicalPCPropIds = new ArrayList<>(
		Arrays.asList(
				ICDContentModelConstants.PC_AXIS_TEMPORALITY_TIME_IN_LIFE,
				ICDContentModelConstants.PC_AXIS_ETIOLOGY_INFECTIOUS_AGENT,
				ICDContentModelConstants.PC_AXIS_ETIOLOGY_CHEMICAL_AGENT, 
				ICDContentModelConstants.PC_AXIS_ETIOLOGY_MEDICATION,
				ICDContentModelConstants.PC_AXIS_HISTOPATHOLOGY,
				ICDContentModelConstants.PC_AXIS_SPECIFIC_ANATOMY
		)
	);
	
	
	public static final List<String> scalePCPropIds = new ArrayList<>(
		Arrays.asList(
				ICDContentModelConstants.PC_AXIS_HAS_SEVERITY,
				ICDContentModelConstants.PC_AXIS_HAS_ALT_SEVERITY1,
				ICDContentModelConstants.PC_AXIS_HAS_ALT_SEVERITY2,
				ICDContentModelConstants.PC_AXIS_TEMPORALITY_COURSE,
				ICDContentModelConstants.PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET
		)
	);
	
	

	public static boolean isFixedScalePCProp(String propName) {
		return fixedScalePCPropIds.contains(propName);
	}
	
	public static boolean isScalePCProp(String propName) {
		return scalePCPropIds.contains(propName);
	}
	
	public static boolean isHierarchicalPCProp(String propName) {
		return hierarchicalPCPropIds.contains(propName);
	}
	
	public static boolean isHasValueRestriction(String propName) {
		return isFixedScalePCProp(propName) || isScalePCProp(propName);
	}

}
