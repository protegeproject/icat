package edu.stanford.bmir.protege.web.server.phenotypes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.data.Occurrence;
import edu.stanford.bmir.protege.web.client.rpc.data.Snippet;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLClassAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDatavaluedPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLIndividualPropertyAtom;

public class RuleModeling {

	OWLModel owlModel;
	Hashtable<String, Integer> resourceIDs;
	String[] ruleNames;

	public RuleModeling(OWLModel owlModel) {
		this.owlModel = owlModel;
		this.resourceIDs = new Hashtable<String, Integer>();

		ArrayList<RDFResource> resource = new ArrayList<RDFResource>(); // Class&PropertiesList
		resource.addAll(owlModel.getUserDefinedOWLNamedClasses()); // GetClasses
		resource.addAll(owlModel.getUserDefinedOWLObjectProperties()); // GetObjectProperties
		resource.addAll(owlModel.getUserDefinedOWLDatatypeProperties()); // GetDataValuedProperties
		int id = 0;
		for (Iterator it = resource.iterator(); it.hasNext();) {
			RDFResource r = (RDFResource) it.next();
			String name = r.getLocalName();
			String longName = r.getName();
			if (resourceIDs.keySet().contains(name)
					|| !longName.contains("autism-ontology")) {
				// System.out.println("##### " + name + " = " + longName);
				continue;
			}
			resourceIDs.put(name, id);
			++id;
		}
	}

	public float[][] vectorize() {
		ArrayList<RDFResource> resource = new ArrayList<RDFResource>(); // Class&PropertiesList
		HashMap<String, ArrayList> nameAncestor = new HashMap<String, ArrayList>(); // AncestorMap
		HashMap<String, ArrayList> nameDescendant = new HashMap<String, ArrayList>(); // DescendantMap

		Collection sup;
		Collection sub;

		resource.addAll(owlModel.getUserDefinedOWLNamedClasses()); // GetClasses

		for (Iterator it = resource.iterator(); it.hasNext();) {
			ArrayList directAncestor = new ArrayList();
			ArrayList directDescendant = new ArrayList();

			RDFSClass gclass = (RDFSClass) it.next();
			sup = gclass.getNamedSuperclasses(false); // Get SuperClasses
			sub = gclass.getNamedSubclasses(false); // Get SubClasses

			for (Iterator sit = sup.iterator(); sit.hasNext();) {
				RDFSClass tmp = (RDFSClass) sit.next();
				if (!(tmp.getLocalName().equals(gclass.getLocalName()))) {
					directAncestor.add(tmp);
				}
			}

			for (Iterator sit = sub.iterator(); sit.hasNext();) {
				RDFSClass tmp = (RDFSClass) sit.next();
				if (!(tmp.getLocalName().equals(gclass.getLocalName()))) {
					directDescendant.add(tmp);
				}
			}

			if (!directAncestor.isEmpty())
				nameAncestor.put(gclass.getLocalName(), directAncestor);

			if (!directDescendant.isEmpty())
				nameDescendant.put(gclass.getLocalName(), directDescendant);
		}

		resource.clear();
		resource.addAll(owlModel.getUserDefinedOWLObjectProperties()); // GetObjectProperties
		resource.addAll(owlModel.getUserDefinedOWLDatatypeProperties()); // GetDataValuedProperties

		for (Iterator it = resource.iterator(); it.hasNext();) {
			ArrayList directAncestor = new ArrayList();
			ArrayList directDescendant = new ArrayList();

			RDFProperty gprop = (RDFProperty) it.next();
			sup = gprop.getSuperproperties(false); // Get SuperProperties
			sub = gprop.getSubproperties(false); // Get SubProperites

			for (Iterator sit = sup.iterator(); sit.hasNext();) {
				RDFProperty tmp = (RDFProperty) sit.next();
				if (!(tmp.getLocalName().equals(gprop.getLocalName()))) {
					directAncestor.add(tmp);
				}
			}

			for (Iterator sit = sub.iterator(); sit.hasNext();) {
				RDFProperty tmp = (RDFProperty) sit.next();
				if (!(tmp.getLocalName().equals(gprop.getLocalName()))) {
					directDescendant.add(tmp);
				}
			}
			if (!directAncestor.isEmpty())
				nameAncestor.put(gprop.getLocalName(), directAncestor);

			if (!directDescendant.isEmpty())
				nameDescendant.put(gprop.getLocalName(), directDescendant);
		}

		// ////////////////////////////////////////////////////////////////////////

		SWRLFactory factory = new SWRLFactory(owlModel);
		Collection rules = factory.getImps(); // Get rules
		int dimension = resourceIDs.keySet().size();
		System.out.println("Dimension of space = " + dimension);

		int n = rules.size();
		ruleNames = new String[n];
		SWRLImp imp;
		SWRLAtom atom;
		String name;
		int alpha = 1;
		int beta = 1;

		ArrayList<SWRLAtom> swrlAtoms = new ArrayList<SWRLAtom>();
		float[][] w = new float[n][dimension];

		int ruleCounter = 0;
		int flag = -1;

		for (Iterator it = rules.iterator(); it.hasNext();) {
			imp = (SWRLImp) it.next(); // Get a rule
			ruleNames[ruleCounter] = imp.getLocalName();
			swrlAtoms.clear();

			// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
			swrlAtoms.addAll(imp.getHead().getValues()); // Get_rule's_head_atoms
			swrlAtoms.addAll(imp.getBody().getValues()); // Get_rule's_body_atoms
			// ////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for (Iterator ruleit = swrlAtoms.iterator(); ruleit.hasNext();) {
				flag = -1;
				atom = (SWRLAtom) ruleit.next();
				name = "";
				if ("swrl:ClassAtom".equals(atom.getRDFType().getBrowserText())) {
					flag = 0;
					name = (((SWRLClassAtom) atom).getClassPredicate())
							.getLocalName();
				} else if ("swrl:IndividualPropertyAtom".equals(atom
						.getRDFType().getBrowserText())) {
					flag = 1;
					name = (((SWRLIndividualPropertyAtom) atom)
							.getPropertyPredicate()).getLocalName();
				} else if ("swrl:DatavaluedPropertyAtom".equals(atom
						.getRDFType().getBrowserText())) {
					flag = 1;
					name = (((SWRLDatavaluedPropertyAtom) atom)
							.getPropertyPredicate()).getLocalName();
				}

				if (name != "") {
					// ////////////////////////////////////////////SUPER

					String[] ANCnameArray = new String[dimension];
					int[] ANCd = new int[dimension];
					for (int i = 0; i < dimension; i++)
						ANCd[i] = -1;

					int start = 0;
					int end = 0;
					int ancd = 0;
					ANCd[end] = ancd;
					ANCnameArray[end++] = name;
					String lname;

					while (ANCnameArray[start] != null) {
						lname = ANCnameArray[start];
						// System.out.println(lname);
						start++;
						if (start == 1 || (ANCd[start - 2] != ANCd[start - 1])) {
							ancd++;
							// System.out.println(ancd);
						}

						if (nameAncestor.containsKey(lname)) {
							ArrayList anc = nameAncestor.get(lname);
							for (Iterator fit = anc.iterator(); fit.hasNext();) {
								String oneName = ((RDFResource) fit.next())
										.getLocalName();
								if (resourceIDs.containsKey(oneName)) {
									ANCd[end] = ancd;
									ANCnameArray[end++] = oneName;

								}
							}
						}
					}

					// ////////////////////////////////////////////SUB

					String[] DESnameArray = new String[dimension];
					int[] DESd = new int[dimension];
					for (int i = 0; i < dimension; i++)
						DESd[i] = -1;

					start = 0;
					end = 0;
					int desd = 0;
					DESd[end] = desd;
					DESnameArray[end++] = name;

					while (DESnameArray[start] != null) {
						lname = DESnameArray[start];
						start++;
						if (start == 1 || (DESd[start - 2] != DESd[start - 1]))
							++desd;

						if (nameDescendant.containsKey(lname)) {
							ArrayList dec = nameDescendant.get(lname);
							for (Iterator fit = dec.iterator(); fit.hasNext();) {
								String oneName = ((RDFResource) fit.next())
										.getLocalName();
								if (resourceIDs.containsKey(oneName)) {
									DESd[end] = desd;
									DESnameArray[end++] = oneName;
								}
							}
						}

					} // for Atom and its subClasses

					float eps = (float) 0.001;
					for (int i = 0; ANCnameArray[i] != null; ++i) {
						String newname = ANCnameArray[i];
						if (resourceIDs.containsKey(newname)) {
							int newid = resourceIDs.get(newname);
							w[ruleCounter][newid] += 1 / ((Math.log(Math.pow(
									ANCd[i], alpha)
									* Math.pow(ancd - ANCd[i], beta) + 1.01)));

						}
					}
					// //////////////////////////////////////////
					for (int i = 1; DESnameArray[i] != null; ++i) {
						String newname = DESnameArray[i];
						if (resourceIDs.containsKey(newname)) {
							int newid = resourceIDs.get(newname);
							w[ruleCounter][newid] += 1 / ((Math.log(Math.pow(
									DESd[i], alpha)
									* Math.pow(ancd, beta) + 1.01)));
						}
					}

				} // if it is a valid Atom
			} // for each Atom in a rule

			ruleCounter++;
		}
		return w;

	}

	public List<String> findClosestRulesToTexts(Snippet snippet,
			float[][] ruleVectors) {
		float[] textVector = new float[resourceIDs.keySet().size()];
		ArrayList<Occurrence> candidateInformation = new ArrayList<Occurrence>();
		candidateInformation.addAll(snippet.phenotypes);
		candidateInformation.addAll(snippet.properties);
		candidateInformation.addAll(snippet.instruments);
		for (Occurrence ocl : candidateInformation) {
			// System.out.println(ocl);
			if (resourceIDs.keySet().contains(ocl.className)) {
				textVector[resourceIDs.get(ocl.className)] += ocl.score;
			}
		}
//		candidateInformation.clear();
//		candidateInformation.addAll(snippet.phenotypes);
//		for (Occurrence ocl : candidateInformation) {
//			if (resourceIDs.keySet().contains(ocl.className)) {
//				textVector[resourceIDs.get(ocl.className)] += 10*(ocl.score);
//			}
//		}

		float[] ruleSimilarities = new float[ruleVectors.length];

		for (int i = 0; i < ruleVectors.length; ++i) {
			ruleSimilarities[i] = sim(ruleVectors[i], textVector);
		}
		// /////////////// Sort
		for (int pass = 1; pass < ruleVectors.length; pass++) {
			for (int k = 0; k < ruleVectors.length - pass; k++) {
				if (ruleSimilarities[k] < ruleSimilarities[k + 1]) {
					// exchange elements
					float temp = ruleSimilarities[k];
					ruleSimilarities[k] = ruleSimilarities[k + 1];
					ruleSimilarities[k + 1] = temp;
					String tempString = ruleNames[k];
					ruleNames[k] = ruleNames[k + 1];
					ruleNames[k + 1] = tempString;
				}
			}
		}

		List<String> resultRules = new ArrayList<String>();
		for (int i = 0; i < 10; ++i) {
			resultRules.add(ruleNames[i]);
			System.out.println("Rule: " + ruleNames[i] + " : "
					+ ruleSimilarities[i]);
		}
		return resultRules;
		// ////////////////////
	}

	public float sim(float[] a, float[] b) {
		return innerProduct(a, b) / (mag(a) * mag(b));
	}

	public float mag(float[] a) {
		double r = 0;
		for (int i = 0; i < a.length; i++)
			r += Math.pow(a[i], 2);
		return (float) Math.sqrt(r);
	}

	public float innerProduct(float[] a, float[] b) {
		float r = 0;
		for (int i = 0; i < a.length; i++)
			r += a[i] * b[i];
		return r;
	}

}
