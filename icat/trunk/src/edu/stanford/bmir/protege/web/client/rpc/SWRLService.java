package edu.stanford.bmir.protege.web.client.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.SWRLData;

/**
 * A service for accessing SWRL rules.
 * 
 * @author Mike Uehara-Bingen <mike.bingen@stanford.edu>
 */
@RemoteServiceRelativePath("swrl")
public interface SWRLService
        extends RemoteService {

	List<SWRLData> getData(String projectName);

}
