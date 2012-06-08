package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.HashMap;

public class ParaphraseObject implements Serializable {
	public String text;
	public HashMap<String, String> RuleNames;
	public HashMap<String, String> RuleSignatures;

	public ParaphraseObject() {
	}
}
