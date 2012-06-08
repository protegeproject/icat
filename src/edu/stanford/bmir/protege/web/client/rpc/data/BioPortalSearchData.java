package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

/**
 * @author Csongor Nyulas
 */

public class BioPortalSearchData implements Serializable {
	
    private String bpRestBaseUrl;
	private String bpSearchUrl;
	private String searchOntologyIds;
	private String searchOptions;
	private String searchPageOption;
	
	
	@Override
	public String toString() {
		return "BioPortalSearchData [bpSearchUrl=" + bpSearchUrl
				+ ", searchOntologyIds=" + searchOntologyIds + 
				", searchOptions=" + searchOptions + 
				", searchPageOption=" + searchPageOption + 
				"bpRestBaseUrl=" + bpRestBaseUrl + "]";
	}

	public String getBpRestBaseUrl() {
        return bpRestBaseUrl;
    }
    public void setBpRestBaseUrl(String bpRestBaseUrl) {
        this.bpRestBaseUrl = bpRestBaseUrl;
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
	public String getSearchOptions() {
	    return searchOptions;
	}
	public void setSearchOptions(String searchOptions) {
	    this.searchOptions = searchOptions;
	}
	public String getSearchPageOption() {
		return searchPageOption;
	}
	public void setSearchPageOption(String searchPageOption) {
		this.searchPageOption = searchPageOption;
	}

}
