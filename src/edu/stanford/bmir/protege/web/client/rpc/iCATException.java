package edu.stanford.bmir.protege.web.client.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;

public class iCATException extends Exception implements IsSerializable {
	
	public iCATException() {}
	
	public iCATException(String message) {
		super(message);
	}

	public iCATException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
