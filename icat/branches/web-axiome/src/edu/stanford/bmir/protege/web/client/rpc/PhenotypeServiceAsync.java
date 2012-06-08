package edu.stanford.bmir.protege.web.client.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.Snippet;

public interface PhenotypeServiceAsync {

	void getPhenotypes(String projectName, String fileName,
			AsyncCallback<HashMap<Snippet, Set<String>>> callback);
	
	void getRules(String projectName,
			AsyncCallback<HashMap<String, String>> callback);
	
	void getSWRLParaphrases(String projectName,
			AsyncCallback<HashMap<String, String>> callback);
	
	void getPapers(String projectName,
			AsyncCallback<HashMap<String, String>> callback);
	
	void getRelevantRule(String projectName, Snippet snippet, 
			AsyncCallback<List<String>> callback);

}
