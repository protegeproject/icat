package edu.stanford.bmir.protege.web.server.icd.proposals;


public class UploadProposalsResponse {

	private int httpCode;
	private String message;
		

	public UploadProposalsResponse() {
		this(200, "Nothing imported so far.");
	}
	
	public UploadProposalsResponse(int httpCode, String message) {
		this.httpCode = httpCode;
		this.message = message;		
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
