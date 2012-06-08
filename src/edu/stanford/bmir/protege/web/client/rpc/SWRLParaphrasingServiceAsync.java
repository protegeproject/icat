package edu.stanford.bmir.protege.web.client.rpc;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.ParaphraseObject;
import edu.stanford.bmir.protege.web.client.rpc.data.Rule;
import edu.stanford.bmir.protege.web.client.rpc.data.SWRLNames;

public interface SWRLParaphrasingServiceAsync {

	void getSWRLParaphrases(String projectName,
			AsyncCallback<ParaphraseObject> callback);

	void getSWRLRuleGroups(String projectName,
			AsyncCallback<HashMap<Integer, List<Rule>>> callback);

	void getSWRLNames(String projectName, AsyncCallback<SWRLNames> callback);

	void addSWRLRule(String projectName, String rule, String name, String sig,
			AsyncCallback<String> callback);

}
