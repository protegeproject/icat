package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;

public class ClassProfile implements Comparable<ClassProfile>,Serializable{
	public String className, label, mathcedString;
	public float score;
	float ep = (float) 0.0001;

	public ClassProfile(String className, String label, float score, String mathcedString) {
		if (className == null || label == null)
			throw new NullPointerException();
		this.className = className;
		this.label = label;
		this.score = score;
		this.mathcedString = mathcedString;
	}

	public String className() {
		return className;
	}

	public String label() {
		return label;
	}

	public float score() {
		return score;
	}

	public boolean equals(Object o) {
		if (!(o instanceof ClassProfile))
			return false;
		ClassProfile n = (ClassProfile) o;
		return (Math.abs(n.score - score) < ep);
	}

	public int hashCode() {
		return  63*className.hashCode() + 31*label.hashCode() + Float.toString(score).hashCode();
	}

	public String toString() {
		return className + " - " + label + " - " + score + " - " + mathcedString.trim();
		//return score + "";
	}

	public int compareTo(ClassProfile n) {
		if((Math.abs(n.score - score) < ep))
			return 0;
		else if((score-n.score)<0){
			return (int) Math.floor(score-n.score);
			
		}else{
			return (int) Math.ceil(score-n.score);
		}
	}
}
