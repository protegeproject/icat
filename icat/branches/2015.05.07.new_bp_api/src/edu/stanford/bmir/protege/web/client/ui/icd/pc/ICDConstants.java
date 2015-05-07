package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.HashMap;

public class ICDConstants {
    public final static String NS = "http://who.int/icd#";

    public final static String PC_AXIS_HAS_SEVERITY = NS + "hasSeverity";	//replicated from ICDContentModelConstants
	public final static String PC_AXIS_TEMPORALITY_COURSE = NS + "course";	//replicated in ICDContentModelConstants
	public final static String PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET = NS + "temporalPatternAndOnset";	//replicated in ICDContentModelConstants

	public final static String PC_SCALE_SEVERITY = NS + "hasSeverityScale";	//replicated in ICDContentModelConstants
	public final static String PC_SCALE_COURSE = NS + "hasCourseScale";	//replicated in ICDContentModelConstants
	public final static String PC_SCALE_PATTERN_AND_ONSET = NS + "hasPatternActivityClinicalStatusScale";	//replicated in ICDContentModelConstants

	@SuppressWarnings("serial")	//replicated in ICDContentModelConstants
	public	static final HashMap<String, String> PC_AXIS_PROP_TO_VALUE_SET_PROP = new HashMap<String, String>(){
		{
			put(PC_AXIS_HAS_SEVERITY, PC_SCALE_SEVERITY);
			put(PC_AXIS_TEMPORALITY_COURSE, PC_SCALE_COURSE);
	    	put(PC_AXIS_TEMPORALITY_PATTERN_AND_ONSET, PC_SCALE_PATTERN_AND_ONSET);
	    }
	
		@Override
		public String get(Object key){
			String res = super.get(key);
			if (res == null) {
				return (String)key;
			}
			return res;
		}
	};


}
