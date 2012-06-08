package edu.stanford.bmir.protege.web.server.phenotypes;
import java.util.Collection;
import java.util.Hashtable;

import edu.stanford.smi.protegex.owl.model.RDFResource;

public class DomainNameAndIDs {
	Hashtable<String, String> domainTerms;
	Hashtable<String, Integer> domainIDs;

	public DomainNameAndIDs() {
		domainTerms = new Hashtable<String, String>();
		domainIDs = new Hashtable<String, Integer>();
	}

	public void buildDomainNameAndIDs(Collection<RDFResource> domain) {
		int id = 0;
		for (RDFResource resource : domain) {

			String name = resource.getLocalName();
			String longName = resource.getName();
			Collection<String> labels = (Collection<String>) resource
					.getLabels();
			Collection<String> comments = (Collection<String>) resource
					.getComments();

			if (domainIDs.keySet().contains(name)
					|| !longName.contains("autism-ontology")) {
				System.out.println("<<<< " + name + " = " + longName);
				continue;
			}

			domainTerms.put(" " + name.toLowerCase().trim() + " ", name);
			domainIDs.put(name, id);
			// System.out.println(name);

			if (!labels.isEmpty() && !labels.equals(" ")) {
				for (String label : labels) {
					// System.out.println(label);
					if(label.equals("")){continue;}
					String trimedLabel = label;
					trimedLabel = trimedLabel.replace('"', ' ');
					domainTerms.put(" " + trimedLabel.toLowerCase().trim() + " ", name);
				}
			}

			if (!comments.isEmpty()) {
				for (String comment : comments) {
					// System.out.println(comment);
					if(comment.equals("")){continue;}
					String trimedComment = comment;
					trimedComment = trimedComment.replace('"', ' ');
					while(trimedComment.contains("(") && trimedComment.contains(")")){
						int fp =  trimedComment.indexOf('(');
						trimedComment = trimedComment.substring(0, fp) + trimedComment.substring(trimedComment.indexOf(")", fp)+1);
					}
					if(trimedComment.contains("-")){
						trimedComment = trimedComment.substring(0, trimedComment.indexOf('-'));
					}
					if(trimedComment.contains("(")){
						trimedComment = trimedComment.substring(0, trimedComment.indexOf('('));
					}
					domainTerms.put(" " + trimedComment.toLowerCase().trim() + " ", name);
				}
			}
			++id;
		}
		//System.out.println("Domain size : " + domainIDs.keySet().size());
	}
}
