package edu.stanford.bmir.protege.web.server.icd.proposals;

import java.util.ArrayList;
import java.util.List;

public class UploadProposalsResponse {

	private int httpCode;
	private String message;
	private List<ICDProposalResponse> rowErrors;
	

	public UploadProposalsResponse() {
		this(200, "Nothing imported so far.");
	}
	
	public UploadProposalsResponse(int httpCode, String message) {
		this.httpCode = httpCode;
		this.message = message;
		this.rowErrors = new ArrayList<ICDProposalResponse>();
	}

	public void addRowError(ICDProposalResponse response) {
		rowErrors.add(response);
	}
	
	public int getHttpCode() {
		return httpCode;
	}

	public void setHttpCode(int httpCode) {
		this.httpCode = httpCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

}
