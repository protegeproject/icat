package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Snippet implements Serializable {

	public int startingIndex, endingIndex;
	public List<Occurrence> properties;
	public List<Occurrence> instruments;
	public List<Occurrence> phenotypes;
	public String snippetTxt;
	public int lambda; // //

	// int minL = 4;

	public Snippet() {
	};

	public Snippet(int startingIndex, int endingIndex, String snippetTxt) {
		this.startingIndex = startingIndex;
		this.endingIndex = endingIndex;
		this.snippetTxt = snippetTxt;
		instruments = new ArrayList<Occurrence>();
		properties = new ArrayList<Occurrence>();
		phenotypes = new ArrayList<Occurrence>();
		lambda = 280; // 56
	}

	public Snippet(int startingIndex, int endingIndex,
			List<Occurrence> properties, String snippetTxt) {
		this.startingIndex = startingIndex;
		this.endingIndex = endingIndex;
		this.properties = properties;
		this.snippetTxt = snippetTxt;
		instruments = new ArrayList<Occurrence>();
		phenotypes = new ArrayList<Occurrence>();
		lambda = 280; // 56
	}

	public List<Snippet> makeASnippet(String txt,
			List<Occurrence> propertyOccurrences) {
		lambda = averageSentenceLength(txt) * 3;
		List<Snippet> snippets = new ArrayList<Snippet>();
		this.properties = propertyOccurrences;
		Collections.sort(propertyOccurrences);
		List<Integer> dotDifferences = new ArrayList<Integer>();
		int preDotIndex = 0;
		for (Occurrence currDot : propertyOccurrences) {
			dotDifferences.add(currDot.index - preDotIndex);
			preDotIndex = currDot.index;
		}

		List<List<Occurrence>> lines = new ArrayList<List<Occurrence>>();
		List<Occurrence> line = new ArrayList<Occurrence>();
		if (!propertyOccurrences.isEmpty()) {
			line.add(propertyOccurrences.get(0));
		}
		// ////////////////////////////////////////////////CLUSTERING PART
		for (int k = 1; k < propertyOccurrences.size(); k++) {
			if (dotDifferences.get(k) <= lambda) {
				line.add(propertyOccurrences.get(k));
			} else {
				lines.add(line);
				line = new ArrayList<Occurrence>();
				line.add(propertyOccurrences.get(k));
			}
		}
		lines.add(line);
		// //////////////////////////////////////////////////////////
		for (List<Occurrence> l : lines) {
			if (l.isEmpty()) {
				continue;
			}
			int firstIndex = txt.lastIndexOf('.', l.get(0).index);
			if (firstIndex == -1) {
				firstIndex = l.get(0).index;
			} else {
				++firstIndex;
			}
			int lastIndex = txt.indexOf('.', l.get(l.size() - 1).index);
			if (lastIndex == -1) {
				lastIndex = l.get(l.size() - 1).index;
			} else {
				++lastIndex;
			}

			String cluster = txt.substring(firstIndex, lastIndex);
			snippets.add(new Snippet(firstIndex, lastIndex, l, cluster));
			// System.out.println(cluster);
			// System.out.println("--------------------------");
		}
		return snippets;

	}

	public int averageSentenceLength(String txt) {
		String[] sentences = txt.split("[.]");
		int sum = 0;
		for (int i = 0; i < sentences.length; ++i) {
			sum += sentences[i].length();
		}
		int average = sum / sentences.length;
		return average;
	}

}
