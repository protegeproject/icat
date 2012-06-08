package edu.stanford.bmir.protege.web.client.ui.ontology.phenologue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.PhenotypesServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Snippet;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Saeed
 * 
 */
public class PhenotypeTreePortlet extends AbstractEntityPortlet {

	protected ListGrid paperGrid, snippetGrid, phenotypeGrid,ruleRelevantGrid, ruleGrid;
	//protected TextArea textArea;
	protected HashMap<String, Set<String>> phenotypeText;
	protected HashMap<String, Snippet> phenotypeSnippetText;
	protected HashMap<String, String> phenotypeRule, ruleParaphrase, papers;

	public PhenotypeTreePortlet(Project project) {
		super(project);
	}

	@Override
	public void reload() {
		// getSWRLParaphrases(project.getProjectName());
		getPapers(project.getProjectName());
		getSWRLParaphrases(project.getProjectName());
		getRules(project.getProjectName());
		// getPhenotypes(project.getProjectName());
	}

	@Override
	public Collection<EntityData> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void intialize() {

		setTitle("Phenologue");
		// setLayout(new AnchorLayout());
		// setLayout(new FitLayout());
		// setLayout(new VerticalLayout(15));
		// setLayout(new HorizontalLayout(15));
		// setLayout(new TableLayout(2));
		setLayout(new ColumnLayout());

		setAutoScroll(true);
		setAutoWidth(true);
		setPaddings(5, 5, 5, 0);
		// setMargins(15);
		// setAutoHeight(true);
		// setWidth(10000);

		paperGrid = new ListGrid();
		paperGrid.setWidth("20%");
		paperGrid.setHeight100();
		paperGrid.setShowAllRecords(true);
		paperGrid.setSelectionType(SelectionStyle.SINGLE);
		paperGrid.setVisible(true);
		paperGrid.setWrapCells(true);
		paperGrid.setFixedRecordHeights(false);
		ListGridField paperTitleField = new ListGridField("Paper", "Paper");
		ListGridField urlField = new ListGridField("URL", "URL");
		urlField.setType(ListGridFieldType.LINK);
		paperGrid.setFields(new ListGridField[] { paperTitleField, urlField });

		snippetGrid = new ListGrid();
		snippetGrid.setWidth("20%");
		// snippetGrid.setHeight(500);
		snippetGrid.setHeight100();
		snippetGrid.setShowAllRecords(true);
		snippetGrid.setSelectionType(SelectionStyle.SINGLE);
		snippetGrid.setVisible(true);
		snippetGrid.setWrapCells(true);
		snippetGrid.setFixedRecordHeights(false);
		ListGridField snippetField = new ListGridField("Snippet", "Snippet");
		snippetGrid.setFields(snippetField);

		phenotypeGrid = new ListGrid();
		phenotypeGrid.setWidth("20%");
		phenotypeGrid.setHeight100();
		phenotypeGrid.setShowAllRecords(true);
		phenotypeGrid.setVisible(true);
		phenotypeGrid.setWrapCells(true);
		phenotypeGrid.setFixedRecordHeights(false);
		ListGridField phenotypeField = new ListGridField("Phenotype",
				"Phenotype");
		phenotypeGrid.setFields(phenotypeField);

		
		ruleRelevantGrid = new ListGrid();
		ruleRelevantGrid.setWidth("20%");
		ruleRelevantGrid.setHeight100();
		ruleRelevantGrid.setShowAllRecords(true);
		ruleRelevantGrid.setVisible(true);
		ruleRelevantGrid.setWrapCells(true);
		ruleRelevantGrid.setFixedRecordHeights(false);
		ListGridField ruleRelevantField = new ListGridField("Rule",
				"Rule");
		ruleRelevantGrid.setFields(ruleRelevantField);
		
		ruleGrid = new ListGrid();
		ruleGrid.setWidth("20%");
		ruleGrid.setHeight100();
		ruleGrid.setShowAllRecords(true);
		ruleGrid.setVisible(true);
		ruleGrid.setWrapCells(true);
		ruleGrid.setFixedRecordHeights(false);
		ListGridField ruleField = new ListGridField("RuleText",
				"RuleText");
		ruleGrid.setFields(ruleField);
//		textArea = new TextArea();
//		textArea.setTitle("Rule");
//		// textArea.setHeight("100px");
//		textArea.setHeight("50em");
//		textArea.setText("No rule exists");
//		textArea.setReadOnly(true);

		paperGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(
					com.smartgwt.client.widgets.grid.events.SelectionEvent event) {
				ListGridRecord[] selectedRecords = paperGrid.getSelection();
				if (selectedRecords.length == 1) {

					getPhenotypes(project.getProjectName(), selectedRecords[0]
							.getAttribute("Paper"));
					//textArea.setText("No rule exists");
					ListGridRecord[] allRecods = ruleGrid.getRecords();
					for (int i = 0; i < allRecods.length; ++i) {
						ruleGrid.removeData(allRecods[i]);
					}
					allRecods = phenotypeGrid.getRecords();
					for (int i = 0; i < allRecods.length; ++i) {
						phenotypeGrid.removeData(allRecods[i]);
					}
					
					allRecods = ruleRelevantGrid.getRecords();
					for (int i = 0; i < allRecods.length; ++i) {
						ruleRelevantGrid.removeData(allRecods[i]);
					}
				}
			}
		});

		snippetGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(
					com.smartgwt.client.widgets.grid.events.SelectionEvent event) {
				ListGridRecord[] selectedRecords = snippetGrid.getSelection();
				if (selectedRecords.length == 1) {
					phenotypeGrid.setData(getRecords("Phenotype", phenotypeText
							.get(selectedRecords[0].getAttribute("Snippet"))));
					getRelevantRule(project.getProjectName(), phenotypeSnippetText.get(selectedRecords[0].getAttribute("Snippet")));
				}
			}
		});

		phenotypeGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(
					com.smartgwt.client.widgets.grid.events.SelectionEvent event) {
				ListGridRecord[] selectedRecords = phenotypeGrid.getSelection();
				
				if (selectedRecords.length == 1) {
					List<String> tmp = new ArrayList<String>();
					if (phenotypeRule.get(selectedRecords[0].getAttribute("Phenotype")) != null) {
						tmp.add(ruleParaphrase.get(phenotypeRule.get(selectedRecords[0].getAttribute("Phenotype"))));
					} else {
						tmp.add(selectedRecords[0].getAttribute("Phenotype") + " has no definition in the rule base.");
					}
					ruleGrid.setData(getRecords("RuleText", tmp));
				}
			}
		});
		
		ruleRelevantGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			public void onSelectionChanged(
					com.smartgwt.client.widgets.grid.events.SelectionEvent event) {
				ListGridRecord[] selectedRecords = ruleRelevantGrid.getSelection();
				
				if (selectedRecords.length == 1) {
					List<String> tmp = new ArrayList<String>();
					if (ruleParaphrase.get(selectedRecords[0].getAttribute("Rule")) != null) {
						tmp.add(ruleParaphrase.get(selectedRecords[0].getAttribute("Rule")));
					} else {
						tmp.add(selectedRecords[0].getAttribute("Rule") + " has no definition in the rule base.");
					}
					ruleGrid.setData(getRecords("RuleText", tmp));
				}
			}
		});

		// System.out.println("@@@@@@@@@@ " + RuleTreePortlet.nameExp.keySet());
		getPapers(project.getProjectName());
		getSWRLParaphrases(project.getProjectName());
		getRules(project.getProjectName());
		// getPhenotypes(project.getProjectName());

		add(paperGrid, new ColumnLayoutData(.20));
		add(snippetGrid, new ColumnLayoutData(.20));
		add(ruleRelevantGrid, new ColumnLayoutData(.20));
		add(phenotypeGrid, new ColumnLayoutData(.20));
		add(ruleGrid, new ColumnLayoutData(.20));

	}

	// // ////////////////////////////////////////////////////////////

	public MyRecord[] getRecords(String attribute, Collection<String> data) {
		MyRecord[] records = new MyRecord[data.size()];
		int i = 0;
		for (String item : data) {
			records[i++] = new MyRecord(attribute, item);
		}
		return records;
	}

	public MyPaperRecord[] getPaperRecords(HashMap<String, String> data) {
		MyPaperRecord[] records = new MyPaperRecord[data.keySet().size()];
		int i = 0;
		for (String title : data.keySet()) {
			records[i++] = new MyPaperRecord(title, data.get(title));
		}
		return records;
	}

	public void getPapers(String projectNam) {
		PhenotypesServiceManager.getInstance().getPapers(projectNam,
				new GetPapers());
	}

	public void getPhenotypes(String projectNam, String fileName) {
		PhenotypesServiceManager.getInstance().getPhenotypes(projectNam,
				fileName, new GetPhenotypes());
	}

	public void getRules(String projectNam) {
		PhenotypesServiceManager.getInstance().getRules(projectNam,
				new GetRules());
	}
	
	public void getRelevantRule(String projectName, Snippet snippet) {
		PhenotypesServiceManager.getInstance().getRelevantRule(projectName, snippet, new GetRelevantRule());
	}

	public void getSWRLParaphrases(String projectNam) {
		PhenotypesServiceManager.getInstance().getSWRLParaphrases(projectNam,
				new GetSWRLParaphrases());
	}

	/*
	 * Remote procedure calls
	 */

	class GetPapers extends AbstractAsyncHandler {
		public void handleFailure(Throwable caught) {
			System.out.println("ERROR" + caught);
			GWT.log("RPC error getting imported ontologies ", caught);
		}

		public void handleSuccess(Object result) {
			papers = (HashMap<String, String>) result;
			paperGrid.setData(getPaperRecords(papers));
		}

	}

	class GetPhenotypes extends AbstractAsyncHandler {
		public void handleFailure(Throwable caught) {
			System.out.println("ERROR" + caught);
			GWT.log("RPC error getting imported ontologies ", caught);
		}

		public void handleSuccess(Object result) {
			
			HashMap<Snippet, Set<String>> phenotypeSnippets = (HashMap<Snippet, Set<String>>) result;
			phenotypeText = new HashMap<String, Set<String>>();
			phenotypeSnippetText = new HashMap<String, Snippet>();
			for(Snippet phenotypeSnippet : phenotypeSnippets.keySet()){
				phenotypeText.put(phenotypeSnippet.snippetTxt, phenotypeSnippets.get(phenotypeSnippet));
				phenotypeSnippetText.put(phenotypeSnippet.snippetTxt, phenotypeSnippet);
			}
			
			snippetGrid.setData(getRecords("Snippet", phenotypeText.keySet()));
		}

	}

	class GetRules extends AbstractAsyncHandler {
		public void handleFailure(Throwable caught) {
			System.out.println("ERROR" + caught);
			GWT.log("RPC error getting imported ontologies ", caught);
		}

		public void handleSuccess(Object result) {
			phenotypeRule = (HashMap<String, String>) result;

		}

	}
	
	class GetRelevantRule extends AbstractAsyncHandler {
		public void handleFailure(Throwable caught) {
			System.out.println("ERROR" + caught);
			GWT.log("RPC error getting imported ontologies ", caught);
		}

		public void handleSuccess(Object result) {
			List<String> listOfRelevantRules = (List<String>) result;
			ruleRelevantGrid.setData(getRecords("Rule", listOfRelevantRules));

		}

	}

	class GetSWRLParaphrases extends AbstractAsyncHandler {
		public void handleFailure(Throwable caught) {
			System.out.println("ERROR" + caught);
			GWT.log("Error at getting SWRL Data from server", caught);
		}

		public void handleSuccess(Object result) {
			ruleParaphrase = (HashMap<String, String>) result;
		}
	}
}

class MyRecord extends ListGridRecord {

	public MyRecord(String attribute, String phenotype) {
		setAttribute(attribute, phenotype);
	}
}

class MyPaperRecord extends ListGridRecord {

	public MyPaperRecord(String title, String url) {
		setAttribute("Paper", title);
		setAttribute("URL", url);

	}
}
