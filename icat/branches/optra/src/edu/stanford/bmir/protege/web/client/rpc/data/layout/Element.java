package edu.stanford.bmir.protege.web.client.rpc.data.layout;

import java.io.Serializable;

public class Element implements Serializable {

	private String name;
	private int columnNumber;
	private int positionInColumn;
	private int height;
	private int width;
	
	public Element () {
		name = "dummy";
		columnNumber = -1;
		positionInColumn = -1;
		height = 0;
		width = 0;
	}
	
	public Element(String e, int col, int pos) {
		//System.out.println("elem created: "+e);
		name = e;
		columnNumber = col;
		positionInColumn = pos;
		height = 0;
		width = 0;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int h) {
		this.height = h;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int w) {
		this.width = w;
	}
	
	public int getPos() {
		return this.positionInColumn;
	}
	
	public void setPos(int p) {
		this.positionInColumn = p;
	}
	
	public int getColumnNumber() {
		return this.columnNumber;
	}
	
	public void setColumnNumber(int c) {
		this.columnNumber = c;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String n) {
		this.name = n;
	}
}
