package edu.stanford.bmir.protege.web.server.icd.proposals.util;

import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class TermTitle2IDMap extends EntityTitle2IDMap {


	private static final long serialVersionUID = 5090953870850475907L;

	public TermTitle2IDMap(ICDContentModel cm, boolean buildFullMap) {
		super(cm, buildFullMap);
	}

	public TermTitle2IDMap(EntityID2TitlesMap id2titlesMap) {
		super(id2titlesMap);
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
