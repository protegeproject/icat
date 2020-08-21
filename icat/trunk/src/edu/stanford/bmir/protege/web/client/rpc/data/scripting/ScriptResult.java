package edu.stanford.bmir.protege.web.client.rpc.data.scripting;

import java.io.Serializable;

public class ScriptResult implements Serializable {
	
	private String result;
	private String error;

	public ScriptResult() {
	}

	public ScriptResult(String result, String error) {
		super();
		this.result = result;
		this.error = error;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean hasError() {
		return error != null && error.trim().length() > 0;
	}
	
	public boolean hasResult() {
		return result != null && result.trim().length() > 0;
	}
}
