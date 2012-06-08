package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

public class SubclassEntityData extends EntityData implements Serializable {

	private int subclassCount;
	
	public SubclassEntityData() {
		super();
	};
			
	public SubclassEntityData(String name, String browserText, EntityData type, boolean hasAnnotation, int subclassCount) {
		super(name, browserText, type, hasAnnotation);
		this.subclassCount = subclassCount;				
	}
	
	public void setSubclassCount(int subclassCount) {
		this.subclassCount = subclassCount;
	}

	public int getSubclassCount() {
		return subclassCount;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("SubclassEntity(");
		buffer.append(this.getName());
		buffer.append(", ");
		buffer.append(this.getSubclassCount());
		buffer.append(")");
		return buffer.toString();
	}
	
}
