package edu.stanford.bmir.protege.web.client.ui.ontology.phenologue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Node;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Item;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;
import com.gwtext.client.widgets.tree.TreeNode;
import com.gwtext.client.widgets.tree.TreePanel;
import com.gwtext.client.widgets.tree.event.TreePanelListenerAdapter;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.SWRLParaphrasingServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Rule;
import edu.stanford.bmir.protege.web.client.ui.generated.UIFactory;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.portlet.EntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;
import edu.stanford.bmir.protege.web.client.util.Project;

/**
 * @author Saeed
 * 
 */
public class RuleTreePortlet extends AbstractEntityPortlet {

	protected TreePanel treePanel;
	public static HashMap<String, String> nameExp;
	public static HashMap<String, String> sigExp;
	boolean[] open;

	protected ArrayList<EntityData> currentSelection;

	private Menu menu;
	private MyBaseItemListenerAdapter listener;

	public RuleTreePortlet(Project project) {
		super(project);
	}

	public void reload() {
		TreeNode root = treePanel.getRootNode();
		Node[] children = root.getChildNodes();
		open = new boolean[children.length];

		for (int i = 0; i < children.length; i++) {
			TreeNode tmp = (TreeNode) children[i];
			if (tmp.isExpanded())
				open[i] = true;
			else
				open[i] = false;
		}

		removeNodes();
		getSWRLRuleGroups(project.getProjectName());

		// System.out.println("Updated");
	}

	private void removeNodes() {
		TreeNode root = treePanel.getRootNode();
		Node[] children = root.getChildNodes();
		for (int i = 0; i < children.length; i++) {
			children[i].remove();
		}
	}

	public void intialize() {
		setLayout(new FitLayout());
		setTitle("Rule Tree");

		treePanel = new TreePanel();
		treePanel.setHeight(500);
		treePanel.setAutoWidth(true);
		treePanel.setAnimate(true);
		treePanel.setAutoScroll(true);

		treePanel.addListener(new TreePanelListenerAdapter() {
			public void onContextMenu(TreeNode node, EventObject e) {
				int[] xy = e.getXY();
				showContextMenu(node, e);
			}
		});

		TreeNode root = new TreeNode((String) null);
		root.setId("RootPropertyNode");
		root.setHref("");
		root.setUserObject(new PropertyEntityData("RootPropertyNode",
				"RootPropertyNode", null, false));

		treePanel.setRootNode(root);
		treePanel.setRootVisible(false);
		add(treePanel);
	}

	public TreePanel getTreePanel() {
		return treePanel;
	}

	protected void afterRender() {
		getSWRLRuleGroups(project.getProjectName());
		super.afterRender();
	}

	public ArrayList getSelection() {
		return currentSelection;
	}

	private void showContextMenu(TreeNode node, EventObject e) {
		if (menu == null) {
			menu = new Menu();
			listener = new MyBaseItemListenerAdapter();
			Item elicItem = new Item("Generate a rule from this group",
					listener);
			elicItem.setId("elic-item");
			menu.addItem(elicItem);
		}
		listener.setNode(node);
		menu.showAt(e.getXY());
	}

	private class MyBaseItemListenerAdapter extends BaseItemListenerAdapter {
		private TreeNode node;
		String txt, sig;

		public void onClick(BaseItem item, EventObject e) {

			System.out.println("RULE = " + node.getText());

			if (nameExp != null)

				if ((node.getId()).startsWith("folder")) {
					System.out.println("clicked on a folder");
					txt = nameExp.get((node.getChildNodes())[0].getId());
					txt = preProc(txt);

				} else if (nameExp.containsKey(node.getId())) {
					txt = nameExp.get(node.getId());
					txt = preProc(txt);

				} else
					System.out.println("The Rule Not Found");

			else
				System.out.println("Hash Map is null!");

			if (sigExp != null)

				if ((node.getId()).startsWith("folder")) {
					System.out.println("clicked on a folder");
					sig = sigExp.get((node.getChildNodes())[0].getId());
					System.out.println(sig);

				} else if (sigExp.containsKey(node.getId())) {
					sig = sigExp.get(node.getId());
					System.out.println(sig);
				} else
					System.out.println("The Signature Not Found");
			else
				System.out.println("Signature map is null!");

			ElicitationPortlet.txt = txt;
			ElicitationPortlet.sig = sig;
			EntityPortlet portlet = UIFactory.createPortlet(project,
					ElicitationPortlet.class.getName(), txt);
			if (portlet == null) {
				System.out.println("Portlet is null");
				return;
			}

			AbstractTab activeTab = getTab();
			activeTab.addPortlet(portlet, activeTab.getColumnCount() - 1);
			activeTab.setControllingPortlet(portlet);
			activeTab.doLayout();
		}

		private void setNode(TreeNode node) {
			this.node = node;
		}

		private String preProc(String txt) {
			txt = txt.replaceAll("<br>", "#");
			txt = txt.replaceAll("&nbsp;&nbsp;&nbsp;", "@");
			return txt;
		}

	}

	// //////////////////////////////////////////////////////////

	public void getSWRLRuleGroups(String projectNam) {
		SWRLParaphrasingServiceManager.getInstance().getSWRLRuleGroups(
				projectNam, new GetSWRLRuleGroups());
	}

	/*
	 * Remote procedure calls
	 */
	class GetSWRLRuleGroups extends AbstractAsyncHandler {
		public void handleFailure(Throwable caught) {
			System.out.println("ERROR" + caught);
			GWT.log("RPC error getting imported ontologies ", caught);
		}

		public void handleSuccess(Object result) {
			HashMap<Integer, List<Rule>> rules = (HashMap<Integer, List<Rule>>) result;
			TreeNode root = treePanel.getRootNode();
			addRules(rules, root);
			root.select();
			root.expand();

			root = treePanel.getRootNode();
			Node[] newChildren = root.getChildNodes();

			if (open != null)
				for (int i = 0; i < open.length; i++) {
					if (open[i]) {
						TreeNode tmp = (TreeNode) newChildren[i];
						tmp.expand();
					}
				}
		}

		/**
		 * @param rulesmap
		 * @param node
		 */
		public void addRules(HashMap<Integer, List<Rule>> rulesmap,
				TreeNode node) {
			List<Rule> ruleslist;
			int key;
			Iterator iterator = rulesmap.keySet().iterator();
			int i = 1;
			while (iterator.hasNext()) {
				key = (Integer) iterator.next();
				TreeNode gnode = new TreeNode();
				gnode.setId("folder" + Math.random());
				gnode.setText("Group " + i);
				gnode.setHref(null);
				gnode.setUserObject(key);

				ruleslist = rulesmap.get(key);
				for (Iterator ir = ruleslist.iterator(); ir.hasNext();) {
					Rule rule = (Rule) ir.next();

					TreeNode rnode = new TreeNode();
					rnode.setId(rule.ruleName);
					rnode.setText(rule.ruleName);
					rnode.setHref(null);
					rnode.setUserObject(rule.ruleText);
					gnode.appendChild(rnode);
				}
				node.appendChild(gnode);
				i++;
			}
		}

	}

}
