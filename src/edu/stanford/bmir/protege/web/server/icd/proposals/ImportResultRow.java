package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.Date;

public class ImportResultRow {
	
	private String contributionId;
	private Date date;
	private ImportRowStatus status;
	private String comment;

	
	public ImportResultRow(String contributionId, Date date,
			ImportRowStatus status, String comment) {
		super();
		this.contributionId = contributionId;
		this.date = date;
		this.status = status;
		this.comment = comment;
	}
	
	public String getContributionId() {
		return contributionId;
	}


	public void setContributionId(String contributionId) {
		this.contributionId = contributionId;
	}


	public Date getDate() {
		return date;
	}


	public void setDate(Date date) {
		this.date = date;
	}


	public ImportRowStatus getStatus() {
		return status;
	}


	public void setStatus(ImportRowStatus status) {
		this.status = status;
	}


	public String getComment() {
		return comment;
	}


	public void setComment(String comment) {
		this.comment = comment;
	}	
	
}
