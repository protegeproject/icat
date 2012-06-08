package edu.stanford.bmir.protege.web.client.ui.ontology.phenologue;

import java.util.Collection;

import com.gwtext.client.core.Connection;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.tree.AsyncTreeNode;
import com.gwtext.client.widgets.tree.ColumnNodeUI;
import com.gwtext.client.widgets.tree.ColumnTree;
import com.gwtext.client.widgets.tree.ColumnTreeEditor;
import com.gwtext.client.widgets.tree.TreeLoader;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;

public class ParaphrasePortlet extends AbstractEntityPortlet {
	Object hours[][] = null;

	public ParaphrasePortlet(Project project) {
		super(project);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

//	@Override
//	public Collection<EntityData> getSelection() {
//		throw new RuntimeException("Under construction");
//	}

	@Override
	public void intialize() {

		final ColumnTree coltree = new ColumnTree();
		coltree.setWidth(552);
		coltree.setHeight(400);
		coltree.setAutoHeight(true);
		coltree.setRootVisible(false);
		coltree.setAutoScroll(true);
		coltree.setTitle("my Sample");

		ColumnTree.Column cols[] = new ColumnTree.Column[3];
		cols[0] = coltree.new Column("Task", 350, "task");
		cols[1] = coltree.new Column("Duration", 100, "duration");
		cols[2] = coltree.new Column("Assigned To", 100, "user");

		coltree.setColumns(cols);

		TreeLoader loader = new TreeLoader();
		loader.setPreloadChildren(true);
		loader.setDataUrl("data/column-data.json");
		loader.setMethod(Connection.GET);
		loader.setUiProviders(ColumnNodeUI.getUiProvider());

		AsyncTreeNode root = new AsyncTreeNode("Tasks", loader);
		coltree.setRootNode(root);

		final Store store = new SimpleStore(
				new String[] { "abbr", "duration" }, getCompanyData());
		store.load();

		ComboBox cb = new ComboBox();
		cb.setMinChars(1);
		cb.setFieldLabel("Duration");
		cb.setStore(store);
		cb.setDisplayField("duration");
		cb.setMode(ComboBox.LOCAL);
		cb.setTriggerAction(ComboBox.ALL);
		cb.setEmptyText("Select Duration");
		cb.setTypeAhead(true);
		cb.setSelectOnFocus(true);
		cb.setWidth(110);
		cb.setResizable(true);
		cb.setTitle("Duration");
		cb.setAllowBlank(false);
		ColumnTreeEditor treeEditorDuration = new ColumnTreeEditor(coltree,
				"duration", cb);
		ColumnTreeEditor treeEditorTask = new ColumnTreeEditor(coltree, "task",
				new TextField());
		ColumnTreeEditor treeEditorUser = new ColumnTreeEditor(coltree, "user",
				new TextField());

		cb.getStore().getById(cb.getValue());

		this.add(coltree);
	}

	private Object[][] getCompanyData() {
		if (hours == null) {
			hours = new Object[][] { new Object[] { "1h", "1 Hour" },
					new Object[] { "2h", "2 Hour" },
					new Object[] { "3h", "3 Hour" },
					new Object[] { "4h", "4 Hour" },
					new Object[] { "5h", "5 Hour" },
					new Object[] { "6h", "6 Hour" },
					new Object[] { "7h", "7 Hour" } };
		}
		return hours;
	}

	@Override
	public Collection<EntityData> getSelection() {
		// TODO Auto-generated method stub
		return null;
	}

}
