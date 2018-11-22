package edu.stanford.bmir.protege.web.server.icd.proposals.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public abstract class EntityID2TitlesMap extends Hashtable<String, Collection<String>> {

	private static final long serialVersionUID = -1494358855025657114L;
	private boolean isFullMap;

	public EntityID2TitlesMap(ICDContentModel cm, boolean buildFullMap) {
		if (!buildFullMap) {
			System.out.println("Creating empty EntityID2TitlesMap...");
			isFullMap = false;
		}
		else {
			buildFullMap(cm);
		}
	}

	public void buildFullMap(ICDContentModel cm) {
		System.out.println("Building EntityID2TitlesMap from content model...");
		long startTimeMS = System.currentTimeMillis();
		this.clear();
		Collection<RDFSNamedClass> icdCategories = getEntities(cm);
		for (RDFSNamedClass icdCat : icdCategories) {
			String catID = icdCat.getURI();
			ArrayList<String> titles = new ArrayList<String>(1);
			this.put(catID, titles);
			Collection<RDFResource> titleTerms = getTitles(cm, icdCat);
			for (RDFResource icdTitleTerm : titleTerms) {
				String title = (String) icdTitleTerm.getPropertyValue(cm.getLabelProperty());
				if (title != null) {
					titles.add(title);
				}
			}
		}
		System.out.println("EntityID2TitlesMap built in " + new DecimalFormat("#.##").format((System.currentTimeMillis()-startTimeMS)/1000) + " seconds");
		isFullMap = true;
	}

	public EntityID2TitlesMap(EntityTitle2IDMap title2idMap) {
		System.out.println("Building EntityID2TitlesMap from Title2ID map...");
		long startTimeMS = System.currentTimeMillis();
		this.clear();
		for (String title : title2idMap.keySet()) {
			String catID = title2idMap.get(title);
			addIdTitle(catID, title);
		}
		System.out.println("EntityID2TitlesMap built in " + new DecimalFormat("#.##").format((System.currentTimeMillis()-startTimeMS)/1000) + " seconds");
		isFullMap = title2idMap.isFullMap();
	}

	public boolean isFullMap() {
		return isFullMap;
	}

	abstract protected Collection<RDFSNamedClass> getEntities(ICDContentModel cm);


	abstract protected Collection<RDFResource> getTitles(ICDContentModel cm, RDFSNamedClass cls);

	
	public void addIdTitle(String catID, String title) {
		Collection<String> titles = this.get(catID);
		if (titles == null) {
			titles = new ArrayList<String>(1);
			this.put(catID, titles);
		}
		titles.add(title);
	}

	
}
