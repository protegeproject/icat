package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

/**
 * @author Csongor Nyulas
 */

public class BioPortalSearchData implements Serializable {
	
	private String bpSearchUrl;
	private String searchOntologyIds;
	private String searchPageOption;
	
	
	@Override
	public String toString() {
		return "BioPortalSearchData [bpSearchUrl=" + bpSearchUrl
				+ ", searchOntologyIds=" + searchOntologyIds + 
				", searchPageOption=" + searchPageOption + "]";
	}

	public String getBpSearchUrl() {
		return bpSearchUrl;
	}
	public void setBpSearchUrl(String bpSearchUrl) {
		this.bpSearchUrl = bpSearchUrl;
	}
	public String getSearchOntologyIds() {
		return searchOntologyIds;
	}
	public void setSearchOntologyIds(String searchOntologyIds) {
		this.searchOntologyIds = searchOntologyIds;
	}
	public String getSearchPageOption() {
		return searchPageOption;
	}
	public void setSearchPageOption(String searchPageOption) {
		this.searchPageOption = searchPageOption;
	}

}
