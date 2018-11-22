package edu.stanford.bmir.protege.web.server.icd.proposals.util;

import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class TermID2TitlesMap extends EntityID2TitlesMap {

	private static final long serialVersionUID = 954684727887607070L;

	
	public TermID2TitlesMap(ICDContentModel cm, boolean buildFullMap) {
		super(cm, buildFullMap);
	}

	public TermID2TitlesMap(EntityTitle2IDMap title2idMap) {
		super(title2idMap);
	}

	@Override
	protected Collection<RDFSNamedClass> getEntities(ICDContentModel cm) {
		return cm.getICDCategories();
	}

	@Override
	protected Collection<RDFResource> getTitles(ICDContentModel cm, RDFSNamedClass cls) {
		return cm.getTerms(cls, cm.getIcdTitleProperty());
	}

}
