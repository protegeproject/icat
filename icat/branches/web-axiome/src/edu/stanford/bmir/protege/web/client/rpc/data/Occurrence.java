package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;


public class Occurrence implements Comparable<Occurrence>,Serializable {

	public String className, label, mathcedString;
	public float score;
	public int index;
	
	public Occurrence(){
		
	}
	
	public Occurrence(ClassProfile oc, int index){
		this.className = oc.className;
		this.label = oc.label;
		this.mathcedString = oc.mathcedString;
		this.score = oc.score;
		this.index = index;
	}
	


	public String toString() {
		return className + " - " + label + " - " + score + " - " + mathcedString + " - " + index;
		//return score + "";
	}
	
	@Override
	public int compareTo(Occurrence n) {
		if(n.index == index)
			return 0;
		else{
			return index - n.index;
		}
	}

}
