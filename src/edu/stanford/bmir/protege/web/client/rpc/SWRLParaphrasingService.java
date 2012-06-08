package edu.stanford.bmir.protege.web.client.rpc;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.ParaphraseObject;
import edu.stanford.bmir.protege.web.client.rpc.data.Rule;
import edu.stanford.bmir.protege.web.client.rpc.data.SWRLNames;


@RemoteServiceRelativePath("paraphrasing")
public interface SWRLParaphrasingService
					extends RemoteService {

	public ParaphraseObject getSWRLParaphrases(String projectName);
	public SWRLNames getSWRLNames(String projectName);
	public HashMap<Integer, List<Rule>> getSWRLRuleGroups(String projectName);
	public String addSWRLRule(String projectName, String rule, String name, String sig);

}
