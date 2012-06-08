package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.SWRLData;

/**
 * @author Mike Uehara-Bingen <mike.bingen@stanford.edu>
 */
public interface SWRLServiceAsync {

	void getData(String projectName, AsyncCallback<List<SWRLData>> callback);

}
