package edu.stanford.bmir.protege.web.server.icd.proposals.util;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Hashtable;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public abstract class EntityTitle2IDMap extends Hashtable<String, String> {

	private static final long serialVersionUID = -1254813982589520851L;
	private boolean isFullMap;

	public EntityTitle2IDMap(ICDContentModel cm, boolean buildFullMap) {
		if (!buildFullMap) {
			System.out.println("Creating empty EntityTitle2IDMap...");
			isFullMap = false;
		}
		else {
			buildFullMap(cm);
		}
	}


	private void buildFullMap(ICDContentModel cm) {
		System.out.println("Building EntityTitle2IDMap from content model...");
		long startTimeMS = System.currentTimeMillis();
		this.clear();
		Collection<RDFSNamedClass> icdCategories = getEntities(cm);
		for (RDFSNamedClass icdCat : icdCategories) {
			String catID = icdCat.getURI();
			Collection<RDFResource> titleTerms = getTitles(cm, icdCat);
			for (RDFResource icdTitleTerm : titleTerms) {
				String title = (String) icdTitleTerm.getPropertyValue(cm.getLabelProperty());
				if (title != null) {
					addTitleId(title, catID);
				}
			}
		}
		System.out.println("EntityTitle2IDMap built in " + new DecimalFormat("#.##").format((System.currentTimeMillis()-startTimeMS)/1000) + " seconds");
		isFullMap = true;
	}


	public EntityTitle2IDMap(EntityID2TitlesMap id2titlesMap) {
		System.out.println("Building EntityTitle2IDMap from ID2Titles...");
		long startTimeMS = System.currentTimeMillis();
		this.clear();
		for (String catID : id2titlesMap.keySet()) {
			Collection<String> titles = id2titlesMap.get(catID);
			for (String title : titles) {
				addTitleId(title, catID);
			}
		}
		System.out.println("EntityTitle2IDMap built in " + new DecimalFormat("#.##").format((System.currentTimeMillis()-startTimeMS)/1000) + " seconds");
		isFullMap = id2titlesMap.isFullMap();
	}


	public boolean isFullMap() {
		return isFullMap;
	}

	
	abstract protected Collection<RDFSNamedClass> getEntities(ICDContentModel cm);


	abstract protected Collection<RDFResource> getTitles(ICDContentModel cm, RDFSNamedClass cls);
	
	public void addTitleId(String title, String catID) {
		this.put(title, catID);
	}

}
