package edu.stanford.bmir.protege.web.client.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.Snippet;

public class PhenotypesServiceManager {

	private static PhenotypeServiceAsync proxy;

	private static PhenotypesServiceManager instance;

	private PhenotypesServiceManager() {
		proxy = (PhenotypeServiceAsync) GWT.create(PhenotypeService.class);
	}

	public static PhenotypesServiceManager getInstance() {
		if (instance == null) {
			instance = new PhenotypesServiceManager();
		}
		return instance;
	}

	public void getPhenotypes(String projectName, String fileName,
			AsyncCallback<HashMap<Snippet, Set<String>>> cb) {
		proxy.getPhenotypes(projectName, fileName, cb);
	}
	
	public void getRules(String projectName,
			AsyncCallback<HashMap<String, String>> cb) {
		proxy.getRules(projectName, cb);
	}
	
	public void getSWRLParaphrases(String projectName,
			AsyncCallback<HashMap<String, String>> cb) {
		proxy.getSWRLParaphrases(projectName, cb);
	}
	
	public void getPapers(String projectName,
			AsyncCallback<HashMap<String, String>> cb) {
		proxy.getPapers(projectName, cb);
	}
	
	public void getRelevantRule(String projectName, Snippet snippet,
			AsyncCallback<List<String>> cb) {
		proxy.getRelevantRule(projectName, snippet, cb);
	}
	

}
