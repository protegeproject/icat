package edu.stanford.bmir.protege.web.server.icd.proposals.util;

import java.util.Collection;

import edu.stanford.bmir.whofic.icd.ICDContentModel;

public class LookupUtil {

	private ICDContentModel cm;
	private boolean buildFullMap;
	private EntityID2TitlesMap icdCatId2titlesMap = null;
	private EntityTitle2IDMap icdCatTitle2idMap = null;
	
	
	public LookupUtil(ICDContentModel cm, boolean buildFullMap) {
		this.cm = cm;
		this.buildFullMap = buildFullMap;
	}
	
	public EntityID2TitlesMap getICDCategoryID2TitlesMap() {
		if (icdCatId2titlesMap == null) {
			if (icdCatTitle2idMap == null) {
				icdCatId2titlesMap = new ICDCategoryID2TitlesMap(cm, buildFullMap);
			}
			else {
				icdCatId2titlesMap = new ICDCategoryID2TitlesMap(icdCatTitle2idMap);
			}
		}
		
		if ( icdCatId2titlesMap.isFullMap() == false && 
				(icdCatTitle2idMap != null && icdCatTitle2idMap.isFullMap())) {
			//rebuild icdCatId2titlesMap from full icdCatTitle2idMap
			icdCatId2titlesMap = new ICDCategoryID2TitlesMap(icdCatTitle2idMap);
		}
		
		return icdCatId2titlesMap;
	}

	public EntityTitle2IDMap getICDCategoryTitle2IDMap() {
		if (icdCatTitle2idMap == null) {
			if (icdCatTitle2idMap == null) {
				icdCatTitle2idMap = new ICDCategoryTitle2IDMap(cm, buildFullMap);
			}
			else {
				icdCatTitle2idMap = new ICDCategoryTitle2IDMap(icdCatId2titlesMap);
			}
		}
		
		if ( icdCatTitle2idMap.isFullMap() == false && 
				(icdCatId2titlesMap != null && icdCatId2titlesMap.isFullMap())) {
			//rebuild icdCatTitle2idMap from full icdCatId2titlesMap
			icdCatTitle2idMap = new ICDCategoryTitle2IDMap(icdCatId2titlesMap);
		}

		return icdCatTitle2idMap;

	}

	/**
	 * Regenerates the ICDCategoryTitle2ID and ICDCategoryID2Titles maps
	 * if they are not full maps already
	 * @param force if true the maps will be recreated even if they were full maps already 
	 */
	public void rebuildFullICDCategoryTitleMaps(boolean force) {
		if (force || (icdCatId2titlesMap == null && icdCatTitle2idMap == null)) {
			rebuildFullICDCategoryTitleMaps();
		}
		else {
			if (icdCatId2titlesMap == null) {
				if (icdCatTitle2idMap.isFullMap()) {
					icdCatId2titlesMap = new ICDCategoryID2TitlesMap(icdCatTitle2idMap);
				}
				else {
					rebuildFullICDCategoryTitleMaps();
				}
			}
			else if (icdCatTitle2idMap == null) {
				if (icdCatId2titlesMap.isFullMap()) {
					icdCatTitle2idMap = new ICDCategoryTitle2IDMap(icdCatId2titlesMap);
				}
				else {
					rebuildFullICDCategoryTitleMaps();
				}
			}
			else {  // icdCatId2titlesMap != null && icdCatTitle2idMap != null)
				if (icdCatId2titlesMap.isFullMap()) {
					if (icdCatTitle2idMap.isFullMap() == false) {
						icdCatTitle2idMap = new ICDCategoryTitle2IDMap(icdCatId2titlesMap);
					}
				}
				else {
					if (icdCatTitle2idMap.isFullMap()) {
						icdCatId2titlesMap = new ICDCategoryID2TitlesMap(icdCatTitle2idMap);
					}
					else {
						rebuildFullICDCategoryTitleMaps();
					}
				}
			}
		}
	}

	private void rebuildFullICDCategoryTitleMaps() {
		icdCatId2titlesMap = new ICDCategoryID2TitlesMap(cm, true);
		icdCatTitle2idMap = new ICDCategoryTitle2IDMap(icdCatId2titlesMap);
	}
	
	public String getCategoryIDForTitle(String title) {
		return getICDCategoryTitle2IDMap().get(title);
	}
	
	public Collection<String> getCategoryTitlesForID(String categoryID) {
		return getICDCategoryID2TitlesMap().get(categoryID);
	}
	
	public void addCategoryIDTitlePair(String categoryID, String title) {
		getICDCategoryID2TitlesMap().addIdTitle(categoryID, title);
		getICDCategoryTitle2IDMap().addTitleId(title, categoryID);
	}
	

	public String getScaleIDForTitle(String title) {
		return getICDCategoryTitle2IDMap().get(title);
	}
	
	public Collection<String> getScaleTitlesForID(String categoryID) {
		return getICDCategoryID2TitlesMap().get(categoryID);
	}

	public boolean isPostCoordinationAxis(String property) {
		return cm.getPostcoordinationAxesPropertyList().contains(property);
	}

	public boolean hasFullEntityTitleMaps() {
		return getICDCategoryID2TitlesMap().isFullMap() && getICDCategoryTitle2IDMap().isFullMap();
	}

}
