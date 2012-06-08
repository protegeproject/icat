package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class ImportsData implements Serializable {

	private String name;
	private ArrayList<ImportsData> imports = new ArrayList<ImportsData>();

	public ImportsData() {
	}

	public ImportsData(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList getImports() {
		return imports;
	}

	public void setImports(ArrayList imports) {
		this.imports = imports;
	}
	
	public void addImport(ImportsData data) {
		this.imports.add(data);
	}
}
