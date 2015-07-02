package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *
 * @author Jack Elliott <jack.elliott@stanford.edu>
 */
@RemoteServiceRelativePath("applicationProperties")
public interface ApplicationPropertiesService extends RemoteService {

    Map<String, String> initialize();    

}