package edu.stanford.bmir.protege.web.client.rpc.data;

public class SWRLData
        extends EntityData {

	private static final long serialVersionUID = -2831052580176421595L;

	private boolean isEnabled;

	public SWRLData() {
	};

	public SWRLData(String name, String rule, boolean isEnabled) {
		super(name, rule);
		this.isEnabled = isEnabled;
	}

	public String getRule() {
		return getBrowserText();
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}

}
