package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

/**
 * @author Csongor Nyulas
 */

public class BioPortalReferenceData implements Serializable {
	private boolean createAsClass; 
	private String referenceClassName;
	private String referencePropertyName;
	
	private String bpBaseUrl;
	private String conceptId;
	private String ontologyVersionId;
	private String preferredName;
	private String ontologyName;
	
	
	@Override
	public String toString() {
		return "BioPortalReferenceData [bpBaseUrl=" + bpBaseUrl
				+ ", conceptId=" + conceptId + ", createAsClass="
				+ createAsClass + ", ontologyName=" + ontologyName
				+ ", ontologyVersionId=" + ontologyVersionId
				+ ", preferredName=" + preferredName + ", referenceClassName="
				+ referenceClassName + ", referencePropertyName="
				+ referencePropertyName + "]";
	}
	
	public boolean createAsClass() {
		return createAsClass;
	}
	public void setCreateAsClass(boolean createAsClass) {
		this.createAsClass = createAsClass;
	}
	public String getReferenceClassName() {
		return referenceClassName;
	}
	public void setReferenceClassName(String referenceClassName) {
		this.referenceClassName = referenceClassName;
	}
	public String getReferencePropertyName() {
		return referencePropertyName;
	}
	public void setReferencePropertyName(String referencePropertyName) {
		this.referencePropertyName = referencePropertyName;
	}
	public String getBpBaseUrl() {
		return bpBaseUrl;
	}
	public void setBpBaseUrl(String bpBaseUrl) {
		this.bpBaseUrl = bpBaseUrl;
	}
	public String getConceptId() {
		return conceptId;
	}
	public void setConceptId(String conceptId) {
		this.conceptId = conceptId;
	}
	public String getOntologyVersionId() {
		return ontologyVersionId;
	}
	public void setOntologyVersionId(String ontologyVersionId) {
		this.ontologyVersionId = ontologyVersionId;
	}
	public String getPreferredName() {
		return preferredName;
	}
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}
	public String getOntologyName() {
		return ontologyName;
	}
	public void setOntologyName(String ontologyName) {
		this.ontologyName = ontologyName;
	}
	
}
