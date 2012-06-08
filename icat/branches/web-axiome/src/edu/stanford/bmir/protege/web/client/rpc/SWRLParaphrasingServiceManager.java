package edu.stanford.bmir.protege.web.client.rpc;

import java.util.HashMap;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.ParaphraseObject;
import edu.stanford.bmir.protege.web.client.rpc.data.Rule;
import edu.stanford.bmir.protege.web.client.rpc.data.SWRLNames;

public class SWRLParaphrasingServiceManager {

	private static SWRLParaphrasingServiceAsync proxy;

	private static SWRLParaphrasingServiceManager instance;

	private SWRLParaphrasingServiceManager() {
		proxy = (SWRLParaphrasingServiceAsync) GWT
				.create(SWRLParaphrasingService.class);
	}

	public static SWRLParaphrasingServiceManager getInstance() {
		if (instance == null) {
			instance = new SWRLParaphrasingServiceManager();
		}
		return instance;
	}

	public void getSWRLParaphrases(String projectName,
			AsyncCallback<ParaphraseObject> cb) {
		proxy.getSWRLParaphrases(projectName, cb);
	}

	public void getSWRLNames(String projectName, AsyncCallback<SWRLNames> cb) {
		proxy.getSWRLNames(projectName, cb);
	}

	public void getSWRLRuleGroups(String projectName,
			AsyncCallback<HashMap<Integer, List<Rule>>> cb) {
		proxy.getSWRLRuleGroups(projectName, cb);
	}

	public void addSWRLRule(String projectName, String rule, String name, String sig,
			AsyncCallback<String> cb) {
		proxy.addSWRLRule(projectName, rule, name, sig, cb);

	}

}
