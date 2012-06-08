package edu.stanford.bmir.protege.web.client.rpc;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.Snippet;

@RemoteServiceRelativePath("phenotypes")
public interface PhenotypeService extends RemoteService {

	public HashMap<Snippet, Set<String>> getPhenotypes(String projectName, String fileName);
	public HashMap<String, String> getRules(String projectName);
	public HashMap<String, String> getSWRLParaphrases(String projectName);
	public HashMap<String, String> getPapers(String projectName);
	public List<String> getRelevantRule(String projectName, Snippet snippet);

}
