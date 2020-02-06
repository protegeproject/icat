package edu.stanford.bmir.protege.web.server.icd.proposals.logDef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.OWLExistentialRestriction;
import edu.stanford.smi.protegex.owl.model.OWLHasValue;
import edu.stanford.smi.protegex.owl.model.OWLIntersectionClass;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLSomeValuesFrom;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;


public class LogicalDefinitionUtils {
	
	private static transient Logger log = Logger.getLogger(LogicalDefinitionUtils.class);
			
	public static final String LOG_DEF_MISSING = "MISSING_LOG_DEF";
	public static final String LOG_DEF_MATCH_NS = "MATCH_NS";
	public static final String LOG_DEF_MATCH_N = "MATCH_N";
	public static final String LOG_DEF_MISSING_FILLER = "MISSING_FILLER";
	public static final String LOG_DEF_NO_MATCH_FILLER = "NO_MATCH_FILLER";
	public static final String LOG_DEF_NO_MATCH_SUPER = "NO_MATCH_SUPER";

	private enum CheckFillerResults {
		EMPTY_FILLERS, FOUND, NOT_FOUND;
	}
	
	
	private OWLModel owlModel;
	private ICDContentModel cm;
	
	
	public LogicalDefinitionUtils(OWLModel owlModel) {
		this.owlModel = owlModel;
		this.cm = new ICDContentModel(owlModel);
	}


	public String checkLogicalDefinition(RDFSNamedClass childCls, RDFProperty prop, 
			RDFResource filler, RDFSNamedClass parentCls) {
		//check parent
		RDFSNamedClass preCoordSupercls = cm.getPreecoordinationSuperclass(childCls);
		if (preCoordSupercls == null) {
			return LOG_DEF_MISSING;
		}
		
		if (parentCls.equals(preCoordSupercls) == false) {
			 return LOG_DEF_NO_MATCH_SUPER; 
		}
		
		Collection<RDFSClass> eqClses = childCls.getEquivalentClasses();
		CheckFillerResults checkEqFillers = checkFillers(parentCls, prop, filler, eqClses);
		if (checkEqFillers == CheckFillerResults.FOUND) {
			return LOG_DEF_MATCH_NS;
		}
		
		Collection<RDFSClass> superClses = childCls.getSuperclasses(false);
		CheckFillerResults checkSuperFillers = checkFillers(parentCls, prop, filler, superClses);
		if (checkSuperFillers == CheckFillerResults.FOUND) {
			return LOG_DEF_MATCH_N;
		}
		
		return (checkEqFillers == CheckFillerResults.EMPTY_FILLERS && checkSuperFillers == CheckFillerResults.EMPTY_FILLERS) ?
					LOG_DEF_MISSING_FILLER : LOG_DEF_NO_MATCH_FILLER;
	}


	private CheckFillerResults checkFillers(RDFSNamedClass parentCls, RDFProperty prop, RDFResource filler, Collection<RDFSClass> clses) {
		boolean fillersFound = false;
		
		for (RDFSClass cls : clses) {
			if (cls instanceof OWLIntersectionClass) {
				Collection<RDFResource> fillers = getFillers(parentCls, (OWLIntersectionClass) cls, prop);
				if (fillers.isEmpty() == false) {
					fillersFound = true;
				}
				if (fillers.contains(filler)) {
					return CheckFillerResults.FOUND;
				}
			}
		}
		return fillersFound ? CheckFillerResults.NOT_FOUND : CheckFillerResults.EMPTY_FILLERS;
	}
	
	
	private OWLIntersectionClass getLogDefIntersectionClass(RDFSNamedClass cls, RDFSNamedClass parentCls,
			RDFProperty prop, RDFResource filler) {
		Collection<RDFSClass> eqClses = cls.getEquivalentClasses();
		OWLIntersectionClass intCls = getLogDefIntersectionClass(parentCls, prop, filler, eqClses);
		if (intCls != null) {
			return intCls;
		}
		
		Collection<RDFSClass> superClses = cls.getSuperclasses(false);
		intCls = getLogDefIntersectionClass(parentCls, prop, filler, superClses);
		
		return intCls;
	}
	
	private OWLIntersectionClass getLogDefIntersectionClass(RDFSNamedClass parentCls, RDFProperty prop, 
			RDFResource filler, Collection<RDFSClass> clses) {
		for (RDFSClass cls : clses) {
			if (cls instanceof OWLIntersectionClass) {
				Collection<RDFResource> fillers = getFillers(parentCls, (OWLIntersectionClass) cls, prop);
				
				if (fillers.contains(filler)) {
					return (OWLIntersectionClass) cls;
				}
			}
		}
		return null;
	}
	
	private Collection<RDFResource> getFillers(RDFSNamedClass parentCls, OWLIntersectionClass intCls, 
			RDFProperty prop) {
		Set<RDFResource> fillers = new HashSet<RDFResource>();
		
		boolean parentMatches = false;
		
		Collection<RDFSClass> ops = intCls.getOperands();
		for (RDFSClass op : ops) {
			if (op instanceof OWLHasValue || op instanceof OWLSomeValuesFrom) { //that's how log defs are encoded
				RDFProperty opProp = getPropFromExpr(op);
				
				if (opProp != null && opProp.equals(prop)) {
					Object filler = getFillerFromExpr(op);
					if (filler instanceof RDFResource) {
						fillers.add((RDFResource) filler);
					}
				}
			} else if (op instanceof RDFSNamedClass) {
				if ( ((RDFSNamedClass)op).equals(parentCls) ) {
					parentMatches = true;
				}
			}
		}
		
		return parentMatches == true ? fillers : new HashSet<RDFResource>();
	}
	

	private RDFProperty getPropFromExpr(RDFSClass expr) {
		return ((OWLExistentialRestriction)expr).getOnProperty();
	}

	private Object getFillerFromExpr(RDFSClass expr) {
		return expr instanceof OWLHasValue ? ((OWLHasValue) expr).getHasValue() : ((OWLSomeValuesFrom)expr).getFiller();
	}
	
	
	public void createLogicalDefinition(RDFSNamedClass cls, RDFSNamedClass parentCls, 
			RDFProperty prop, RDFResource filler, 
			boolean isHasValue, boolean isDefining) {
		
		Collection<RDFResource> operands = new ArrayList<RDFResource>();
		operands.add(parentCls);
		operands.add(createLogDefClassExpr(prop, filler, isHasValue));
		
		OWLIntersectionClass intCls = owlModel.createOWLIntersectionClass(operands);
		
		if (isDefining == true) {
			((OWLNamedClass)cls).addEquivalentClass(intCls);
		} else {
			cls.addSuperclass(intCls);
		}
	}
	
	
	private RDFResource createLogDefClassExpr(RDFProperty prop, RDFResource filler, boolean isHasValue) {
		return isHasValue == true ? owlModel.createOWLHasValue(prop, filler) :
									owlModel.createOWLSomeValuesFrom(prop, filler);
	}
	

	public boolean deleteLogicalDefinition(RDFSNamedClass cls, RDFSNamedClass parentCls, RDFProperty property,
			RDFResource filler) {
		OWLIntersectionClass intCls = getLogDefIntersectionClass(cls, parentCls, property, filler);
		
		if (intCls == null) {
			return false;
		}
		
		intCls.delete();
		
		return true;
	}


	public boolean editLogicalDefinition(RDFSNamedClass cls, RDFSNamedClass parentCls, RDFProperty prop,
			RDFResource oldFiller, RDFResource newFiller, boolean isHasValue, boolean isDefining) {
		boolean res = deleteLogicalDefinition(cls, parentCls, prop, oldFiller);
		
		if (res == false) {
			return false;
		}
		
		createLogicalDefinition(cls, parentCls, prop, newFiller, isHasValue, isDefining);
		return true;
	}

	
}
