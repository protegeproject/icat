package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.SWRLData;

/**
 * @author Mike Uehara-Bingen <mike.bingen@stanford.edu>
 */
public class SWRLServiceManager {

	private static SWRLServiceAsync proxy;

	private static SWRLServiceManager instance;

	private SWRLServiceManager() {
		proxy = (SWRLServiceAsync) GWT.create(SWRLService.class);
	}

	public static SWRLServiceManager getInstance() {
		if (instance == null) {
			instance = new SWRLServiceManager();
		}
		return instance;
	}

	public void getData(String projectName, AsyncCallback<List<SWRLData>> cb) {
		proxy.getData(projectName, cb);
	}
	
	

}
