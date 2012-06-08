package edu.stanford.bmir.protege.web.server.phenotypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.PhenotypeService;
import edu.stanford.bmir.protege.web.client.rpc.data.ClassProfile;
import edu.stanford.bmir.protege.web.client.rpc.data.Occurrence;
import edu.stanford.bmir.protege.web.client.rpc.data.Snippet;
import edu.stanford.bmir.protege.web.server.ProjectManager;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFList;
import edu.stanford.smi.protegex.owl.model.RDFObject;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSLiteral;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLBuiltinAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLClassAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDataRangeAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDatavaluedPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLDifferentIndividualsAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLImp;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLIndividualPropertyAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLSameIndividualAtom;
import edu.stanford.smi.protegex.owl.swrl.model.SWRLVariable;

public class PhenotypeImpl extends RemoteServiceServlet implements
		PhenotypeService {

	String sig = "";

	@Override
	public HashMap<String, String> getPapers(String projectName) {
		HashMap<String, String> papers = new HashMap<String, String>();
		papers
				.put(
						"Using the autism diagnostic interview--revised to increase phenotypic homogeneity in genetic studies of autism",
						"http://www.ncbi.nlm.nih.gov/pubmed/17276746");
		papers.put("shorter paper",
				"http://www.ncbi.nlm.nih.gov/pubmed/17276746");
		return papers;
	}

	@Override
	public HashMap<Snippet, Set<String>> getPhenotypes(String projectName,
			String fileName) {
		Project project = ProjectManager.getProjectManager().getProject(
				projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		HashMap<Snippet, Set<String>> phenotypeSnippet = new HashMap<Snippet, Set<String>>();
		String txt = new String();
		File file = new File(fileName + ".txt");
		StringBuffer contents = new StringBuffer();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				contents.append(text).append(
						System.getProperty("line.separator"));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		txt = contents.toString();
		txt = txt.toLowerCase();
		txt = txt.replace('\n', ' ');
		List<Snippet> candidateSnippets = new ArrayList<Snippet>();

		Collection<RDFResource> phenotypeDomain = buildDomainForClasses(
				owlModel,
				"http://purl.org/autism-ontology/1.0/autism-core.owl#AUTISMC1000015",
				true); // Autism Spectrum Disorder related phenotype
		Collection<RDFResource> instruments = buildDomainForClasses(
				owlModel,
				"http://purl.org/autism-ontology/1.0/assessment-result.owl#AUTISMC000002",
				false); // Assessment result

		Snippet snippetGenerator = new Snippet();
		// Snippet snippetGenerator = new Snippet();
		for (RDFResource instrument : instruments) {
			// System.out.println(">>>>>" + instrument.getLocalName());
			Collection<RDFResource> SingleInstrumentDomain = new ArrayList<RDFResource>();
			SingleInstrumentDomain.add(instrument);
			Collection<RDFResource> DTPwithInstrumentDomain = buildDomainForInstruments(
					owlModel, instrument.getName());
			List<Occurrence> instrumentOccurrences = findExactOccurrences(txt,
					SingleInstrumentDomain);
			List<Snippet> snippets = snippetGenerator.makeASnippet(txt,
					instrumentOccurrences);

			for (Snippet snippet : snippets) {
				int fi = txt.lastIndexOf('.', Math.max(0, snippet.startingIndex
						- snippetGenerator.lambda));
				if (fi < 0) {
					fi = snippet.startingIndex;
				} else {
					++fi;
				}
				int li = txt.indexOf('.', Math.min(snippet.endingIndex
						+ snippetGenerator.lambda, txt.length() - 2));
				if (li < 0) {
					li = snippet.endingIndex;
				} else {
					++li;
				}
				String vicinity = txt.substring(fi, li);
				snippet.snippetTxt = vicinity;

				List<Occurrence> phenotypeOccurrences = findOccurrences(
						vicinity, phenotypeDomain);
				List<Occurrence> PropertyOccurrences = findOccurrences(
						vicinity, DTPwithInstrumentDomain);

				if (!phenotypeOccurrences.isEmpty()) {
					// || !PropertyOccurrences.isEmpty()) {
					snippet.instruments.addAll(snippet.properties);
					snippet.properties.clear();
					for (Occurrence phenotypeOccurrence : phenotypeOccurrences) {
						snippet.phenotypes.add(phenotypeOccurrence);
					}

					for (Occurrence PropertyOccurrence : PropertyOccurrences) {
						snippet.properties.add(PropertyOccurrence);
					}
					candidateSnippets.add(snippet);
				}
			}
		}

		for (Snippet candidateSnippet : candidateSnippets) {

			if (phenotypeSnippet.keySet().contains(candidateSnippet)) {
				for (Occurrence snippetPhenotype : candidateSnippet.phenotypes) {

					phenotypeSnippet.get(candidateSnippet).add(snippetPhenotype.label);
				}
			} else {
				Set<String> newPhenotypes = new HashSet<String>();
				for (Occurrence snippetPhenotype : candidateSnippet.phenotypes) {
					newPhenotypes.add(snippetPhenotype.label);
				}
				phenotypeSnippet.put(candidateSnippet, newPhenotypes);
			}

		}

		return phenotypeSnippet;
	}

	public Collection<RDFResource> buildDomainForInstruments(OWLModel owlModel,
			String className) {
		Collection<RDFResource> domain = new ArrayList<RDFResource>();
		Collection<OWLDatatypeProperty> dpList = owlModel
				.getUserDefinedOWLDatatypeProperties();
		for (OWLDatatypeProperty dp : dpList) {
			if (dp.isDomainDefined(false)
					&& dp.getDomain(false).getName().equals(className)) {
				domain.add(dp);
			}
		}
		return domain;
	}

	public Collection<RDFResource> buildDomainForClasses(OWLModel owlModel,
			String className, boolean descendants) {
		OWLNamedClass root = owlModel.getOWLNamedClass(className);
		Collection<RDFResource> domain = root.getNamedSubclasses(descendants);
		domain.add(root);
		return domain;
	}

	public List<Occurrence> findOccurrences(String txt,
			Collection<RDFResource> domain) {
		List<Occurrence> occurrences = new ArrayList<Occurrence>();
		DomainNameAndIDs nameAndIDs = new DomainNameAndIDs();
		nameAndIDs.buildDomainNameAndIDs(domain);
		StringMatching stringMatching = new StringMatching();

		ArrayList<ClassProfile> ClassProfileList = new ArrayList<ClassProfile>();
		for (String domainTerm : nameAndIDs.domainTerms.keySet()) {
			// System.out.println("@-" + domainTerm+"-");
			float score = stringMatching.align(domainTerm, txt);
			ClassProfileList.add(new ClassProfile(nameAndIDs.domainTerms
					.get(domainTerm), domainTerm, score,
					stringMatching.matchedString));
		}
		Collections.sort(ClassProfileList);
		Collections.reverse(ClassProfileList);
		for (ClassProfile cp : ClassProfileList) {
			if (cp.score >= 0.55) {
				int index = txt.indexOf(cp.mathcedString);
				while (index >= 0) {
					occurrences.add(new Occurrence(cp, index));
					index = txt.indexOf(cp.mathcedString, index + 1);
				}
			}
		}
		return occurrences;
	}

	public List<Occurrence> findExactOccurrences(String txt,
			Collection<RDFResource> domain) {
		List<Occurrence> occurrences = new ArrayList<Occurrence>();
		DomainNameAndIDs nameAndIDs = new DomainNameAndIDs();
		nameAndIDs.buildDomainNameAndIDs(domain);

		ArrayList<ClassProfile> ClassProfileList = new ArrayList<ClassProfile>();
		for (String domainTerm : nameAndIDs.domainTerms.keySet()) {
			// System.out.println("@-" + domainTerm.trim()+"-");
			int startIndex = txt.indexOf(domainTerm.trim());
			while (startIndex >= 0) {
				ClassProfileList.add(new ClassProfile(nameAndIDs.domainTerms
						.get(domainTerm), domainTerm, 1, domainTerm.trim()));
				// System.out.println(domainTerm);
				startIndex = txt.indexOf(domainTerm.trim(), startIndex + 1);
			}
		}
		Collections.sort(ClassProfileList);
		Collections.reverse(ClassProfileList);
		for (ClassProfile cp : ClassProfileList) {
			if (cp.score >= 0.55) {
				int index = txt.indexOf(cp.mathcedString);
				while (index >= 0) {
					occurrences.add(new Occurrence(cp, index));
					index = txt.indexOf(cp.mathcedString, index + 1);
				}
			}
		}
		return occurrences;
	}

	public HashMap<String, String> getRules(String projectName) {
		Project project = ProjectManager.getProjectManager().getProject(
				projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		SWRLFactory factory = new SWRLFactory(owlModel);
		Collection rules = factory.getImps(); // Get rules
		HashMap<String, String> PhenotypeRule = new HashMap<String, String>();

		for (Iterator it = rules.iterator(); it.hasNext();) {
			SWRLImp imp = (SWRLImp) it.next(); // Get a rule
			String ruleText = imp.getBrowserText();
			// ///////////////////////////////////////////////////////
			ArrayList<SWRLAtom> swrlHeadAtoms = (ArrayList<SWRLAtom>) imp
					.getHead().getValues(); // Get_rule's_head_atoms

			for (Iterator ruleit = swrlHeadAtoms.iterator(); ruleit.hasNext();) {
				SWRLAtom atom = (SWRLAtom) ruleit.next();
				if ("swrl:ClassAtom".equals(atom.getRDFType().getBrowserText())) {
					List<String> labels = (List) ((((SWRLClassAtom) atom)
							.getClassPredicate()).getLabels());
					String label = labels.get(0);
					if (label.equals("")) {
						continue;
					}
					String trimedLabel = label;
					trimedLabel = trimedLabel.replace('"', ' ');
					PhenotypeRule.put(" " + trimedLabel.toLowerCase().trim()
							+ " ", imp.getLocalName());
				}
			}
		}

		return PhenotypeRule;
	}

	public List<String> getRelevantRule(String projectName, Snippet snippet) {
		Project project = ProjectManager.getProjectManager().getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		RuleModeling ruleBase = new RuleModeling(owlModel);
		float[][] ruleVectors = ruleBase.vectorize();
		List<String> rules = ruleBase.findClosestRulesToTexts(snippet, ruleVectors);
		return rules;
	}

	public HashMap<String, String> getSWRLParaphrases(String projectName) {
		Project project = ProjectManager.getProjectManager().getProject(
				projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		SWRLFactory factory = new SWRLFactory(owlModel);
		RDFProperty bph = owlModel
				.getRDFProperty("http://swrl.stanford.edu/ontologies/3.3/swrla.owl#hasBuiltInPhrase");
		RDFProperty hrg = owlModel
				.getRDFProperty("http://swrl.stanford.edu/ontologies/3.3/swrla.owl#hasRuleCategory");

		Collection rules = factory.getImps();
		Collection vars = factory.getVariables();
		Collection props = owlModel.getUserDefinedOWLObjectProperties();
		Collection classes = owlModel.getUserDefinedOWLNamedClasses();
		Collection dataTypes = owlModel.getUserDefinedOWLDatatypeProperties();

		HashMap<String, String> ruleParaphrase = new HashMap<String, String>();
		String fsig = "";
		// String sig = "";
		String Result = new String();
		String OneResult = new String();
		String SResult = new String();
		String myResult = new String();
		String[] ResultArray = new String[2];
		SWRLImp element;
		List head, body;
		HashMap<String, List> map;
		HashMap<String, List> cmap = new HashMap<String, List>();
		HashMap<String, List> ccmap = new HashMap<String, List>();

		String[] order;
		String root;
		List<String> CREATEOWLTHING, p, CREATEOWLTHING2, p2;

		String signature = null;
		HashMap<String, Integer> sigMap = new HashMap<String, Integer>();
		HashMap<String, String> sigNameMap = new HashMap<String, String>();
		String[] tmp = new String[2];
		boolean test = true;

		for (Iterator it = rules.iterator(); it.hasNext();) {
			OneResult = "";
			CREATEOWLTHING = new ArrayList();
			p = new ArrayList();
			CREATEOWLTHING2 = null;
			p2 = null;
			element = (SWRLImp) it.next();
			// OneResult = "<b>- Rule " + element.getLocalName()
			// + " : </b><br><br>";
			Result += "- Rule " + element.getLocalName() + " : <br><br>";
			myResult += "- Rule " + element.getLocalName() + " : <br><br>";
			// System.out.println("IMMMMMMMMP= " + element.getLocalName());
			head = element.getHead().getValues();
			body = element.getBody().getValues();
			List<Pair> aliases = new ArrayList<Pair>();
			List<Pair> aliases2 = new ArrayList<Pair>();
			List<String> sequence = new ArrayList<String>();

			ExtendedMap xmap = ScanRules(body, aliases, CREATEOWLTHING, p,
					sequence); // Scan the rules & Generate the hashmap
			map = xmap.map;
			// printMap(map);
			CREATEOWLTHING = xmap.CREATEOWLTHING;
			p = xmap.p;
			collapse(aliases, map);
			transAliases(aliases);
			cmap.putAll(map); // Generate a new copy of the rules
			ccmap.putAll(map);
			// /printMap(map);
			order = reorderVar(map); // Sort the Hashmap
			reorderAtoms(cmap); // Sort the Atom list for each Hashmap element
			reorderAtoms(ccmap);
			boolean first = true;

			fsig = "";
			while (!cmap.isEmpty()) {
				if (first)
					first = false;
				else {
					// System.out.print("AND ");
					Result += "AND ";
					OneResult += "AND ";
					myResult += "AND ";
				}

				// System.out.print("IF<br>");
				Result += "IF<br>";
				OneResult += "IF<br>";
				myResult += "IF<br>";
				root = ChooseRoot(cmap, body, order, sequence);
				sig = "";
				tmp = DFSJoin(root, cmap, body, "&nbsp;&nbsp;&nbsp;", false,
						false, bph, aliases);
				Result += tmp[0];
				OneResult += tmp[0];
				myResult += tmp[1];

				// System.out.println();
				Result += "<br>";
				OneResult += "<br>";
				myResult += "<br>";
				fsig += sig + "^";

			}
			fsig = fsig.substring(0, fsig.length() - 1);
			fsig += "-";
			// System.out.println("IF signiture = " + fsig);

			xmap = ScanRules(head, aliases2, CREATEOWLTHING2, p2, sequence); // Scan
			// the
			// rules
			// &
			// Generate
			// the
			// hashmap
			map = xmap.map;
			collapse(aliases, map);
			cmap.putAll(map); // Generate a new copy of the rules
			// printMap(map);
			order = reorderVar(map); // Sort the Hashmap
			reorderAtoms(cmap); // Sort the Atom list for each Hashmap element

			// System.out.print("THEN<br>");
			Result += "THEN<br>";
			OneResult += "THEN<br>";
			myResult += "THEN<br>";

			while (!cmap.isEmpty()) {

				if (!p.isEmpty()) {
					root = p.get(0);
					Result += "&nbsp;&nbsp;&nbsp;" + CREATEOWLTHING.get(0);
					OneResult += "&nbsp;&nbsp;&nbsp;" + CREATEOWLTHING.get(0);
					myResult += "&nbsp;&nbsp;&nbsp;" + CREATEOWLTHING.get(0);
					p.remove(0);
					CREATEOWLTHING.remove(0);

					if (cmap.containsKey(root)) {
						sig = "";
						tmp = DFSJoin(root, cmap, head, "&nbsp;&nbsp;&nbsp;",
								true, false, bph, aliases);
						Result += tmp[0];
						OneResult += tmp[0];
						myResult += tmp[1];
					}

				} else {
					root = ChooseRoot(cmap, head, order, sequence);
					sig = "";
					tmp = DFSJoin(root, cmap, head, "&nbsp;&nbsp;&nbsp;",
							false, false, bph, aliases);
					Result += tmp[0];
					OneResult += tmp[0];
					myResult += tmp[1];
				}

				Result += "<br>";
				OneResult += "<br>";
				myResult += "<br>";
				fsig += sig + "^";
			}

			while (!p.isEmpty()) {
				root = p.get(0);
				Result += "&nbsp;&nbsp;&nbsp;" + CREATEOWLTHING.get(0) + " A "
						+ p.get(0);
				OneResult += "&nbsp;&nbsp;&nbsp;" + CREATEOWLTHING.get(0)
						+ " A " + p.get(0);
				;
				myResult += "&nbsp;&nbsp;&nbsp;" + CREATEOWLTHING.get(0)
						+ " A " + p.get(0);
				;
				p.remove(0);
				CREATEOWLTHING.remove(0);
			}

			fsig = fsig.substring(0, fsig.length() - 1);

			signature = moreCompressString(compressString(fsig)).trim();
			// signature = fsig;

			String ab = "";
			for (int i = 0; i < signature.length(); i++) {
				if (signature.charAt(i) != '+')
					ab += signature.charAt(i);
			}

			if (!sigNameMap.containsKey(ab)) {
				sigNameMap.put(ab, signature);
				sigMap.put(signature, 1);
			} else {
				String mtmp = sigNameMap.get(ab);
				if (signature.length() > mtmp.length()) {
					sigNameMap.put(ab, signature);
					sigMap.put(signature, sigMap.get(mtmp));
					sigMap.remove(mtmp);
				} else {
					signature = mtmp;
				}
				sigMap.put(signature, sigMap.get(signature) + 1);

			}

			// if(!sigMap.containsKey(signature)){
			// sigMap.put(signature, 1);
			// }else{
			// sigMap.put(signature, sigMap.get(signature)+1);
			// }

			element.setPropertyValue(hrg, signature);
			// System.out.println("Group Signiture" + signature);
			// System.out.println("Group Property Value" +
			// element.getPropertyValue(hrg));

			// System.out.print("*********************************************************************************<br>");
			// Result +=
			// "*********************************************************************************<br>";
			// myResult +=
			// "*********************************************************************************<br>";
			Result += "<hr>";
			myResult += "<hr>";
			// System.out.println("OneResult =<br>" + OneResult);
			ruleParaphrase.put(element.getLocalName(), OneResult);
			// sigExp.put(element.getLocalName(), signature);

		}

		// System.out.println("+++++++++++++++++++++++++++++++++++++++");
		// System.out.println(Result);
		// printSig(sigMap);
		// ///////////////////////////////////SResult = printSig(sigMap);

		// ResultArray[0] = Result;
		// ResultArray[1] = SResult;

		// System.out.println("++++++++++++++++++++++++++++++++++++++++");
		// System.out.println(myResult);
		// System.out.println(Result);
		// ParaphraseObject obj = new ParaphraseObject();
		// obj.text = Result;
		// obj.RuleNames = nameExp;
		// obj.RuleSignatures = sigExp;
		return ruleParaphrase;
	}

	public String[] DFSJoin(String root, HashMap<String, List> map, List bList,
			String prefix, boolean OWLTH, boolean WR, RDFProperty bph,
			List aliases) {
		Argument arg;
		SWRLAtom atom;
		int Rtype, ARtype;
		String argName2, prefix2;
		List list = map.remove(root);
		boolean first = true;
		String Result2 = new String();
		String myResult2 = new String();
		String[] ResultArray2 = new String[2];
		String[] tmp = new String[2];

		sig += "(";
		// Special case for the first rule
		if (((Argument) list.get(0)).ruleType == 1 && OWLTH) {
			if (startsWithVowel(root)) {
				// System.out.println(" AN \"" + root + "\" SUCH THAT");
				Result2 += " AN \"" + root + "\" SUCH THAT<br>";
			} else {
				// System.out.println(" A \"" + root + "\" SUCH THAT");
				Result2 += " A \"" + root + "\" SUCH THAT<br>";
			}

			prefix += "&nbsp;&nbsp;&nbsp;"; // The second line starts after a
			// tab
			sig += "@";
		}

		// Start checking Atoms
		for (Iterator it = list.iterator(); it.hasNext();) {
			arg = (Argument) it.next();
			atom = (SWRLAtom) bList.get(arg.value);

			if (first) {
				if (!WR) {
					// System.out.print(prefix);
					Result2 += prefix;
					myResult2 += prefix;
				}

				first = false;

			} else {
				// System.out.print(prefix + "AND ");
				Result2 += prefix + "AND ";
				myResult2 += prefix + "AND ";
			}

			Result2 += parseAnAtom(atom, bph, aliases);
			myResult2 += atom.getBrowserText();

			Rtype = arg.ruleType;
			sig += Rtype;
			prefix2 = prefix;

			if (Rtype == 2) {
				argName2 = stringPruning(((((SWRLIndividualPropertyAtom) atom)
						.getArgument2()).getBrowserText()));
				if (map.containsKey(argName2)) {
					if (((Argument) (map.get(argName2).get(0))).ruleType != 5) {
						// System.out.println();
						Result2 += "<br>";
						myResult2 += "<br>";
						prefix2 += "&nbsp;&nbsp;&nbsp;";
						// System.out.print(prefix2);
						Result2 += prefix2;
						myResult2 += prefix2;
					}

					// System.out.print(" WHERE ");
					Result2 += " WHERE ";
					myResult2 += " WHERE ";
					sig += "#";
					tmp = DFSJoin(argName2, map, bList, prefix2, false, true,
							bph, aliases);
					Result2 += tmp[0];
					myResult2 += tmp[1];
				} else {
					// System.out.println();
					Result2 += "<br>";
					myResult2 += "<br>";
				}

			} else if (Rtype == 4) {
				argName2 = stringPruning(((((SWRLDatavaluedPropertyAtom) atom)
						.getArgument2()).getBrowserText()));
				if (map.containsKey(argName2)) {
					if (((Argument) (map.get(argName2).get(0))).ruleType != 5) {
						// System.out.println();
						Result2 += "<br>";
						myResult2 += "<br>";
						prefix2 += "&nbsp;&nbsp;&nbsp;";
						// System.out.print(prefix2);
						Result2 += prefix2;
						myResult2 += prefix2;
					}

					// System.out.print(" WHERE ");
					Result2 += " WHERE ";
					myResult2 += " WHERE ";
					sig += "#";
					tmp = DFSJoin(argName2, map, bList, prefix2, false, true,
							bph, aliases);
					Result2 += tmp[0];
					myResult2 += tmp[1];
				} else {
					// System.out.println();
					Result2 += "<br>";
					myResult2 += "<br>";
				}
			} else {
				// System.out.println();
				Result2 += "<br>";
				myResult2 += "<br>";
			}

		}
		sig += ")";

		ResultArray2[0] = Result2;
		ResultArray2[1] = myResult2;

		return ResultArray2;
	}

	public String ChooseRoot(HashMap<String, List> cmap, List atomList,
			String[] order, List sequence) {
		List list;
		Argument arg;

		if (cmap.size() == order.length) {
			for (int i = 0; i < order.length; i++) {
				list = cmap.get(order[i]);
				arg = (Argument) list.get(0);

				if (arg.ruleType == 1)
					return order[i];
			}

			return (String) sequence.get(0);
		} else {
			String key;
			Iterator it = cmap.keySet().iterator();
			while (it.hasNext()) {
				key = (String) it.next();
				list = cmap.get(key);

				if (((Argument) list.get(0)).ruleType == 1) {
					return key;

				}
			}

			it = cmap.keySet().iterator();
			return (String) it.next();
		}
	}

	public void collapse(List aliases, HashMap<String, List> map) {
		Pair p;
		List<Argument> l1, l2;
		for (Iterator ait = aliases.iterator(); ait.hasNext();) {
			p = (Pair) ait.next();
			if (map.containsKey(p.a) && map.containsKey(p.b)) {
				l1 = map.get(p.a);
				l2 = map.get(p.b);

				for (int i = 0; i < l2.size(); i++) {
					l1.add(l2.get(i));
				}

				map.put(p.a, l1);
				map.remove(p.b);
			}
		}
	}

	public void reorderAtoms(HashMap<String, List> cmap) {
		List list;
		String key;
		ArgComparator comparator = new ArgComparator();
		Iterator it = cmap.keySet().iterator();
		while (it.hasNext()) {
			key = (String) it.next();
			list = cmap.get(key);
			Collections.sort(list, comparator); // Sort the atom list
		}

	}

	public String[] reorderVar(HashMap<String, List> map) {
		int n = map.size();
		int counter = 0;
		String[] result = new String[n];

		String key;
		List list;
		String maxKey = null;
		int max;

		Iterator it;

		while (!map.isEmpty()) // if Map is not empty
		{
			max = 0;
			it = map.keySet().iterator();
			while (it.hasNext()) {
				key = (String) it.next();
				list = map.get(key);

				if (list.size() > max) {
					max = list.size();
					maxKey = key;
				}
			}
			map.remove(maxKey);
			result[counter] = maxKey;
			counter++;
			// System.out.println(maxKey);

		}
		return result;
	}

	// Scan the rules and generate the HashMap
	public ExtendedMap ScanRules(List list, List aliases, List CREATEOWLTHING,
			List p, List<String> sequence) {
		SWRLAtom atom;
		RDFList argus;
		RDFSClass ruleType;
		String S, ruleTypeName;
		sequence.clear();

		HashMap<String, List> varMap = new HashMap<String, List>();
		int atomCounter = 0;
		String argName, argName2;

		for (Iterator It = list.iterator(); It.hasNext();) {
			atom = (SWRLAtom) It.next();
			S = atom.getBrowserText();
			ruleType = atom.getRDFType();
			ruleTypeName = ruleType.getBrowserText();

			if (ruleTypeName.equals("swrl:ClassAtom")) {
				argName = stringPruning(((((SWRLClassAtom) atom).getArgument1())
						.getBrowserText()));
				// System.out.print(argName);
				sequence.add(argName);
				varMap = insertMap(varMap, argName, atomCounter, 1);

			} else if (ruleTypeName.equals("swrl:IndividualPropertyAtom")) {
				argName = stringPruning((((SWRLIndividualPropertyAtom) atom)
						.getArgument1()).getBrowserText());
				// System.out.println(" insert " + argName);
				sequence.add(argName);
				varMap = insertMap(varMap, argName, atomCounter, 2);

			} else if (ruleTypeName.equals("swrl:DatavaluedPropertyAtom")) {
				argName = stringPruning(((((SWRLDatavaluedPropertyAtom) atom)
						.getArgument1()).getBrowserText()));
				// System.out.print(argName);
				sequence.add(argName);
				varMap = insertMap(varMap, argName, atomCounter, 4);

			} else if (ruleTypeName.equals("swrl:BuiltinAtom")) {
				argus = ((SWRLBuiltinAtom) atom).getArguments();
				argName = stringPruning(((RDFResource) argus.getFirst())
						.getBrowserText());

				// System.out.println( ((SWRLBuiltinAtom)
				// atom).getBrowserText());
				// System.out.println((((SWRLBuiltinAtom)
				// atom).getBuiltin()).getBrowserText());

				S = stringPruning((((SWRLBuiltinAtom) atom).getBuiltin())
						.getBrowserText());
				// System.out.print(argName);

				if (S.equals("createOWLThing")) {
					p.add(argName);
					argus = argus.getRest();
					argName = stringPruning(((RDFResource) argus.getFirst())
							.getBrowserText());
					;
					CREATEOWLTHING.add("FOR EACH \"" + argName + "\" THERE IS");
				} else {
					sequence.add(argName);
					varMap = insertMap(varMap, argName, atomCounter, 5);

				}

			} else if (ruleTypeName.equals("swrl:SameIndividualAtom")) {
				argName = stringPruning(((((SWRLSameIndividualAtom) atom)
						.getArgument1()).getBrowserText()));
				argName2 = stringPruning(((((SWRLSameIndividualAtom) atom)
						.getArgument2()).getBrowserText()));
				aliases.add(new Pair(argName, argName2));
				// System.out.print(argName);
				// varMap = insertMap(varMap, argName, atomCounter, 3);

			} else if (ruleTypeName.equals("swrl:DifferentIndividualsAtom")) {
				argName = stringPruning(((((SWRLDifferentIndividualsAtom) atom)
						.getArgument1()).getBrowserText()));
				// System.out.print(argName);
				// varMap = insertMap(varMap, argName, atomCounter, 3);

			} else if (ruleTypeName.equals("swrl:DataRangeAtom")) {
				argName = stringPruning(((((SWRLDataRangeAtom) atom)
						.getArgument1()).getBrowserText()));
				// System.out.print(argName);
				sequence.add(argName);
				varMap = insertMap(varMap, argName, atomCounter, 6);

			} else {
				System.out.print("Error in atom type");
			}
			atomCounter++;
		}
		return (new ExtendedMap(varMap, CREATEOWLTHING, p));
	}

	public void transAliases(List<Pair> aliases) {
		String m;
		Pair p = null;
		for (int i = 0; i < aliases.size(); i++) {
			m = aliases.get(i).b;
			for (int j = i + 1; j < aliases.size(); j++) {
				if (m.equals(aliases.get(j).a)) {
					p = new Pair(aliases.get(i).a, aliases.get(j).b);
					aliases.set(j, p);
				}
			}

		}
	}

	public String compressString(String S) {
		char[] stringS = S.toCharArray();
		char[] NS = new char[S.length()];
		int j = 0;
		boolean star = false;
		for (int i = 0; i < (stringS.length);) {
			if (i == (stringS.length) - 1) {
				if (stringS[i] == stringS[i - 1] && stringS[i] != '('
						&& stringS[i] != ')') {
					NS[j] = '+';
					break;
				} else {
					if (star) {
						NS[j] = '+';
						j++;
					}
					NS[j] = stringS[i];
					break;
				}
			} else {
				star = false;
				while (i < (stringS.length) - 1 && stringS[i] == stringS[i + 1]
						&& stringS[i] != '(' && stringS[i] != ')') {
					if (!star)
						star = true;
					i++;
				}
				// System.out.println(" i = " + i + " j = " + j);
				NS[j] = stringS[i];
				i++;
				j++;
				if (i != (stringS.length) - 1 && star) {
					NS[j] = '+';
					j++;
				}
			}
		}

		return String.valueOf(NS);
	}

	public String moreCompressString(String S) {
		char[] stringS = S.toCharArray();
		List<Character> VS = new ArrayList<Character>();
		char[] NS = new char[S.length()];

		for (int i = 0; i < stringS.length; i++)
			VS.add(stringS[i]);

		int j;

		for (int i = VS.size() - 2; i >= 0; i--) {
			if (VS.get(i + 1).equals('#')
					|| (VS.get(i + 1).equals('+') && VS.get(i + 2).equals('#'))) {
				j = i + 1;
				while (!VS.get(j).equals(')'))
					j++;

				if (VS.get(j + 1).equals(VS.get(i))) {
					VS.remove(j + 1);

					if (VS.get(j + 1).equals('+'))
						VS.remove(j + 1);

					if (!VS.get(i + 1).equals('+'))
						VS.add(i + 1, '+');

				}
			}
		}

		// NS = VS.toArray();
		// System.out.println(VS);
		for (int i = 0; i < VS.size(); i++) {
			// System.out.print((VS.get(i)));
			NS[i] = VS.get(i);
		}

		return String.valueOf(NS);
		// return null;
	}

	public boolean startsWithVowel(String S) {
		S = S.toLowerCase();
		if (S.startsWith("a")
				|| S.startsWith("e")
				|| S.startsWith("i")
				|| S.startsWith("o")
				|| (S.startsWith("u") && !(S.startsWith("univers") || S
						.startsWith("unit"))) || S.equals("f") || S.equals("h")
				|| S.equals("l") || S.equals("m") || S.equals("n")
				|| S.equals("r") || S.equals("s") || S.equals("x")
				|| S.startsWith("hour") || S.startsWith("honor"))
			return true;
		else
			return false;
	}

	public String stringPruning(String S) {
		// if (S.startsWith("?"))
		// S = S.substring(1);

		// else if (S.startsWith("swrlb:"))
		// S = S.substring(6);

		// else
		if (S.startsWith("swrlx:"))
			S = S.substring(6);

		// else if (S.startsWith("adi-2003:"))
		// S = S.substring(9);

		// else if (S.startsWith("temporal:"))
		// S = S.substring(9);

		// S = S.replace('_', ' ');

		else if (S.startsWith("_"))
			S = S.substring(1);

		return S;

	}

	public String parseAnAtom(SWRLAtom atom, RDFProperty bph, List aliases) {

		RDFList argus;
		RDFSClass ruleType;
		String S, ruleTypeName, argName, predicate;
		RDFObject a2;
		String Result1 = new String();

		S = atom.getBrowserText();
		ruleType = atom.getRDFType();
		ruleTypeName = ruleType.getBrowserText();
		// System.out.println("Name = " + S + "Type = " + ruleTypeName);
		boolean second = true;

		if (ruleTypeName.equals("swrl:ClassAtom")) {
			argName = stringPruning(((((SWRLClassAtom) atom).getArgument1())
					.getBrowserText()));
			argName = uniqueArg(argName, aliases);
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";
			// printMap(varMap);
			S = "!1!"
					+ stringPruning((((SWRLClassAtom) atom).getClassPredicate())
							.getBrowserText());

			if (startsWithVowel(S)) {
				// System.out.print("IS AN " + S);
				Result1 += "IS AN " + S;
			} else {
				// System.out.print("IS A " + S);
				Result1 += "IS A " + S;
			}

		} else if (ruleTypeName.equals("swrl:IndividualPropertyAtom")) {
			argName = stringPruning((((SWRLIndividualPropertyAtom) atom)
					.getArgument1()).getBrowserText());
			argName = uniqueArg(argName, aliases);
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";

			// predicate = (((SWRLIndividualPropertyAtom)
			// atom).getPropertyPredicate()).getBrowserText();
			predicate = "!2!"
					+ stringPruning((((SWRLIndividualPropertyAtom) atom)
							.getPropertyPredicate()).getBrowserText());
			// predicate = S;

			if (predicate.toLowerCase().startsWith("has ")) {
				// System.out.print("HAS " + predicate.substring(4) + " ");
				Result1 += "HAS " + predicate.substring(4) + " ";

			} else if (predicate.toLowerCase().startsWith("has")) {
				// System.out.print("HAS " + predicate.substring(3) + " ");
				Result1 += "HAS " + predicate.substring(3) + " ";
			} else {
				// System.out.print(predicate + " ");
				Result1 += predicate + " ";
			}

			argName = stringPruning(((((SWRLIndividualPropertyAtom) atom)
					.getArgument2()).getBrowserText()));
			argName = uniqueArg(argName, aliases);
			// System.out.print("\"" + argName + "\"");
			Result1 += "\"" + argName + "\"";

		} else if (ruleTypeName.equals("swrl:DatavaluedPropertyAtom")) {
			argName = stringPruning(((((SWRLDatavaluedPropertyAtom) atom)
					.getArgument1()).getBrowserText()));
			argName = uniqueArg(argName, aliases);
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";

			predicate = "!4!"
					+ stringPruning((((SWRLDatavaluedPropertyAtom) atom)
							.getPropertyPredicate()).getBrowserText());

			Object o = ((((SWRLDatavaluedPropertyAtom) atom).getArgument2()));
			if (!(o instanceof SWRLVariable)
					&& !(o instanceof RDFSLiteral && ((RDFSLiteral) o)
							.getDatatype().isNumericDatatype())) {
				argName = stringPruning((((SWRLDatavaluedPropertyAtom) atom)
						.getArgument2()).getBrowserText());
				argName = "\'" + argName + "\'";

			} else {
				argName = stringPruning((((SWRLDatavaluedPropertyAtom) atom)
						.getArgument2()).getBrowserText());
				argName = "\"" + argName + "\"";
				// System.out.println(data[row][col]);
			}
			// a2 = ((SWRLDatavaluedPropertyAtom) atom).getArgument2();

			argName = uniqueArg(argName, aliases);
			Result1 += "HAS VALUE " + argName + " FOR " + predicate;

		} else if (ruleTypeName.equals("swrl:BuiltinAtom")) {
			argus = ((SWRLBuiltinAtom) atom).getArguments();
			argName = stringPruning(((RDFResource) argus.getFirst())
					.getBrowserText());
			argName = uniqueArg(argName, aliases);
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";

			argus = argus.getRest();
			S = 5 + stringPruning((((SWRLBuiltinAtom) atom).getBuiltin())
					.getBrowserText());
			String mytmp = "!5!"
					+ (String) (((SWRLBuiltinAtom) atom).getBuiltin())
							.getBrowserText();
			// .getPropertyValue(bph); //This line is commented for simplicity

			if (mytmp != null) {
				// System.out.print(mytmp + " ");
				Result1 += mytmp + " ";
			} else {
				// System.out.print(S + " ");
				Result1 += S + " ";
			}

			while (argus.isClosed()) {
				Object j = argus.getFirst();

				if (j instanceof RDFResource) {
					argName = stringPruning(((RDFResource) argus.getFirst())
							.getBrowserText());
					argName = uniqueArg(argName, aliases);
					// System.out.print("1 \"" + argName + "\"");
					Result1 += "\"" + argName + "\" ";

				} else if (j instanceof String) {
					// System.out.print("2 \"" + j + "\"");
					Result1 += "\"" + j + "\" ";

				} else {
					// System.out.print("3 "+j);
					Result1 += j;
				}

				argus = argus.getRest();
			}

		} else if (ruleTypeName.equals("swrl:SameIndividualAtom")) {
			argName = stringPruning(((((SWRLSameIndividualAtom) atom)
					.getArgument1()).getBrowserText()));
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";

			// System.out.print("IS THE SAME AS ");
			Result1 += "IS THE SAME AS ";

			argName = stringPruning(((((SWRLSameIndividualAtom) atom)
					.getArgument2()).getBrowserText()));
			// System.out.print("\""+ argName + "\"");
			Result1 += "\"" + argName + "\"";

		} else if (ruleTypeName.equals("swrl:DifferentIndividualsAtom")) {
			argName = stringPruning(((((SWRLDifferentIndividualsAtom) atom)
					.getArgument1()).getBrowserText()));
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";

			// System.out.print("IS DIFFERENT FROM ");
			Result1 += "IS DIFFERENT FROM ";

			argName = stringPruning(((((SWRLDifferentIndividualsAtom) atom)
					.getArgument2()).getBrowserText()));
			// System.out.print("\"" + argName + "\"");
			Result1 += "\"" + argName + "\"";

		} else if (ruleTypeName.equals("swrl:DataRangeAtom")) {
			argName = stringPruning(((((SWRLDataRangeAtom) atom).getArgument1())
					.getBrowserText()));
			// System.out.print("\"" + argName + "\" ");
			Result1 += "\"" + argName + "\" ";

			// System.out.print("HAS RNAGE ");
			Result1 += "HAS RNAGE ";

			// System.out.print("\"" + stringPruning(((((SWRLDataRangeAtom)
			// atom).getDataRange()).getBrowserText())) + "\"");
			Result1 += "\""
					+ stringPruning(((((SWRLDataRangeAtom) atom).getDataRange())
							.getBrowserText())) + "\"";

		} else {
			// System.out.print(S);
			Result1 += S;
			// System.out.print("["+ruleTypeName+"]");
			Result1 += "[" + ruleTypeName + "]";
		}

		return Result1;

	}

	HashMap insertMap(HashMap<String, List> map, String key, int value,
			int ruleType) {
		Argument newArg = new Argument(value, ruleType);

		if (map.containsKey(key)) {
			map.get(key).add(newArg);

		} else {
			List list = new ArrayList();
			list.add(newArg);
			map.put(key, list);
		}

		return map;
	}

	public String uniqueArg(String arg2, List aliases) {
		Pair t;
		for (Iterator It = aliases.iterator(); It.hasNext();) {
			t = (Pair) It.next();
			if (arg2.equals(t.b))
				return t.a;
		}
		return arg2;
	}

	class Pair {
		public String a;
		public String b;

		public Pair(String a, String b) {
			this.a = a;
			this.b = b;
		}

	}

	class ExtendedMap {

		public List<String> CREATEOWLTHING;
		public List<String> p;
		public HashMap<String, List> map;

		public ExtendedMap(HashMap<String, List> map, List CREATEOWLTHING,
				List p) {
			this.CREATEOWLTHING = CREATEOWLTHING;
			this.p = p;
			this.map = map;
		}

	}

	class Argument {

		public int ruleType;
		public String var;
		public int value;

		Argument() {
			ruleType = 0;
			value = -1;
		}

		public Argument(int value, int ruleType) {
			this.value = value;
			this.ruleType = ruleType;
		}

	}

	class ArgComparator implements Comparator { // ArgComparator
		public int compare(Object o1, Object o2) {

			int r1 = ((Argument) o1).ruleType;
			int r2 = ((Argument) o2).ruleType;

			if (r1 > r2)
				return 1;
			else if (r1 < r2)
				return -1;
			else
				return 0;
		}
	}

}
