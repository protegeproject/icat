package edu.stanford.bmir.protege.web.client.ui.ontology.classes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Node;
import com.gwtext.client.dd.DragData;
import com.gwtext.client.dd.DragDrop;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.tree.DropNodeCallback;
import com.gwtext.client.widgets.tree.TreeNode;
import com.gwtext.client.widgets.tree.TreePanel;
import com.gwtext.client.widgets.tree.event.TreePanelListenerAdapter;

import edu.stanford.bmir.protege.web.client.event.EntityCreateEvent;
import edu.stanford.bmir.protege.web.client.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.event.EventType;
import edu.stanford.bmir.protege.web.client.event.OntologyListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.SelectionEvent;

public class ClassTreePortlet extends AbstractEntityPortlet {

	private static final String PLACE_HOLDER_PANEL = "placeHolderPanel";
	private static final String TOP_CLASS_PROP = "topClass"; //TODO move to another class
	
	protected TreePanel treePanel;	
	protected ArrayList<EntityData> currentSelection;	
	private Button createButton;
	private Button deleteButton;	

	private boolean expandDisabled = false;
	
	public ClassTreePortlet(Project project) {
		super(project);		
	}

	public void reload() {		
	}
	
	public void intialize() {
		setLayout(new FitLayout());
		setTitle("Class Tree");
		addToolbarButtons();
		
		Panel bogusPanel = new Panel();
		bogusPanel.setId(PLACE_HOLDER_PANEL);
		bogusPanel.setHeight(560);
		add(bogusPanel);
	}

	
	protected TreePanel createTreePanel() {
		treePanel = new TreePanel();	
		treePanel.setHeight(560);		
		treePanel.setAutoWidth(true);		
		treePanel.setAnimate(true);
		treePanel.setAutoScroll(true);
		treePanel.setEnableDD(true);
		
		treePanel.addListener(new TreePanelListenerAdapter() {        	
			public void onClick(TreeNode node, EventObject e) {
				currentSelection = new ArrayList<EntityData>();
				currentSelection.add((EntityData) node.getUserObject());
				notifySelectionListeners(new SelectionEvent(ClassTreePortlet.this));
			}

			public void onExpandNode(TreeNode node) {				
				if (!expandDisabled && !treePanel.getRootNode().equals(node)) {
					GWT.log("Expand node " + node.getUserObject(), null);
					getSubclasses(((EntityData)node.getUserObject()).getName(), node);
				}
			}		
		});
		
		addDragAndDropSupport();
		addProjectListeners();
		addRenameListener();
		return treePanel;
	}
	
	protected void addProjectListeners() {
		project.addOntologyListener(new OntologyListenerAdapter() {			
			public void entityCreated(EntityCreateEvent ontologyEvent) {
				//GWT.log("Entity created: " + ontologyEvent.getEntity().getName(), null);
				if (ontologyEvent.getType() == EventType.CLASS_CREATED) {
					//onClassCreated(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities());
				} else if (ontologyEvent.getType() == EventType.SUBCLASS_ADDED) {
					onSubclassAdded(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities(), false);
				}  else if (ontologyEvent.getType() == EventType.SUBCLASS_REMOVED) {
					onSubclassRemoved(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities());
				}
			}
			
			public void entityRenamed(EntityRenameEvent renameEvent) {
				onClassRename(renameEvent.getEntity(), renameEvent.getOldName());
			}
			
		});
	}
	
	protected void addRenameListener() {
		//TODO - rename does not work right - disable for now
		/*
		final TextField editorTextField = new TextField("editorTF", "editorTF", 50);
		editorTextField.setValue("InitValue");
		
		TreeEditor treeEditor = new TreeEditor(treePanel, editorTextField);		
		treeEditor.addListener(new EditorListenerAdapter() {			
			public boolean doBeforeStartEdit(Editor source, ExtElement boundEl,
					Object value) {
				editorTextField.setValue("MyText");
				source.setValue("Pizza");
				value = "SomeValue";
				return true;
			}
			
			public boolean doBeforeComplete(Editor source, Object value,
					Object startValue) {
				GWT.log("Before complete rename: old: " + startValue + " new: " + value + " editor: " + source, null);
				renameClass((String)startValue, (String)value);
				return true;
			}
		});		
		*/
	}


	
	protected void addToolbarButtons() {
		createButton = new Button("Create");		
		createButton.setCls("toolbar-button");
		createButton.addListener(new ButtonListenerAdapter() {			
			public void onClick(Button button, EventObject e) {
				onCreateCls();
			}
		});
		createButton.setDisabled(!project.hasWritePermission());
		
		deleteButton = new Button("Delete");
		//deleteButton.setIconCls("protege-class-delete-icon");
		deleteButton.setCls("toolbar-button");
		deleteButton.addListener(new ButtonListenerAdapter() {			
			public void onClick(Button button, EventObject e) {			
				onDeleteCls();
			}
		});
		deleteButton.setDisabled(!project.hasWritePermission());
				
		setTopToolbar(new Button[]{createButton, deleteButton});
	}

	
	protected void addDragAndDropSupport() {
		treePanel.addListener(new TreePanelListenerAdapter() {			
					
			public boolean doBeforeNodeDrop(TreePanel treePanel,
					TreeNode target, DragData dragData, String point,
					DragDrop source, TreeNode dropNode,
					DropNodeCallback dropNodeCallback) {
				//GWT.log("Node before dropped: target: " + target.getId() + " dropNode: " + dropNode.getId() + " from: " + dropNode.getParentNode().getId(), null);
				moveClass(getNodeClsName(dropNode), getNodeClsName(dropNode.getParentNode()), getNodeClsName(target));
				return true;
			}
		});		
	}


	protected void onSubclassAdded(EntityData entity, ArrayList<EntityData> subclasses, boolean selectNewNode) {
		if (subclasses == null || subclasses.size() == 0) {
			return;
		}
		
		EntityData subclassEntity = (EntityData) subclasses.get(0); //there is always just one		
		TreeNode parentNode = findTreeNode(entity.getName());
			
		if (parentNode == null) {
			return; //nothing to be done
		}		
	
		TreeNode subclassNode = findTreeNode(subclassEntity.getName());
		if (subclassNode == null) {
			subclassNode = createTreeNode(subclassEntity);
			parentNode.appendChild(subclassNode);
		}  else { //tricky if it already exists			
			if (!hasChild(parentNode, subclassEntity.getName())) {
				parentNode.appendChild(subclassNode);							
			}			
		}	
	}

	protected TreeNode findTreeNode(String id) {
		TreeNode root = treePanel.getRootNode();
		TreeNode node = findTreeNode(root, id, new ArrayList<TreeNode>());		
		return node;
	}
	
	protected TreeNode findTreeNode(TreeNode node, String id, ArrayList<TreeNode>  visited) {
		if (getNodeClsName(node).equals(id)) {
			return node;
		} else  {
			visited.add(node);
			Node[] children = node.getChildNodes();
			for (int i = 0; i < children.length; i++) {
				TreeNode n = findTreeNode((TreeNode) children[i], id, visited);
				if (n != null) { return n; }
			}
			return null;
		}
	}
	
	
	protected void onSubclassRemoved(EntityData entity, ArrayList<EntityData> subclasses) {
		if (subclasses == null || subclasses.size() == 0) {	return;	}
		
		EntityData subclass = (EntityData) subclasses.get(0); //there is always just one
		//TreeNode parentNode = treePanel.getNodeById(entity.getName());
		TreeNode parentNode = findTreeNode(entity.getName());
		
		if (parentNode == null) { return;}
		
		//TreeNode subclassNode = treePanel.getNodeById(subclass.getName());
		TreeNode subclassNode = findTreeNode(subclass.getName());
		if (subclassNode == null) {	return;	}
		
		if (subclassNode.getParentNode().equals(parentNode)) {
			parentNode.removeChild(subclassNode);
			if (parentNode.getChildNodes().length < 1) {
				parentNode.setExpandable(false);
			}
		}
	}
	
	protected void onClassCreated(EntityData entity, ArrayList<EntityData> superEntities) {
		if (superEntities == null) {
			GWT.log("Entity created: " + entity.getName() + " but unknown superEntities", null);
			return;
		}
		for (Iterator<EntityData> iterator = superEntities.iterator(); iterator.hasNext();) {
			EntityData superEntity = (EntityData) iterator.next();
			TreeNode parentNode = findTreeNode(superEntity.getName());
			if (parentNode != null) {
				insertNodeInTree(parentNode, entity);
			}
		}		
	}
	
	protected void onClassRename(EntityData entity, String oldName) {
		TreeNode oldNode = findTreeNode(oldName);		
		if (oldNode == null) {return;}		
		TreeNode newNode = createTreeNode(entity);		
		oldNode.getParentNode().replaceChild(newNode, oldNode);		
	}
	
	
	protected void insertNodeInTree(TreeNode parentNode, EntityData child) {
		parentNode.appendChild(createTreeNode(child));
	}
	
	protected void onCreateCls() {
		MessageBox.prompt("Name", "Please enter a class name:",
				new MessageBox.PromptCallback() {  
			public void execute(String btnID, String text) {
				createCls(text);
			}	
		});
	}	
	
	protected void createCls(final String className) {
		String superClsName = null;
		if (currentSelection != null && currentSelection.size() > 0) {
			superClsName = ((EntityData)currentSelection.get(0)).getName(); 
		}
		
		OntologyServiceManager.getInstance().createCls(project.getProjectName(),
				className, superClsName, new CreateClassHandler());
		
		refreshFromServer();
		TreeNode parentNode = findTreeNode(superClsName);
		if (parentNode != null) {
			parentNode.expand();
		}
		
		TreeNode newNode = getDirectChild(parentNode, className);
		if (newNode != null) {
			newNode.select();
		}	
	}  
		
	
	protected void onDeleteCls() {
		if (currentSelection == null || currentSelection.size() == 0) {
			Window.alert("Please select first a class to delete.");
			return; 
		}
		
		final String clsName = ((EntityData)currentSelection.get(0)).getName();
		
		MessageBox.confirm("Confirm", "Are you sure you want to delete class <br> " + clsName + " ?",  
				new MessageBox.ConfirmCallback() {  
			public void execute(String btnID) { 
				if (btnID.equals("yes")) {									
					deleteCls(clsName);
				}
			}  
		});
	}
	
	protected void deleteCls(String className) {		
		GWT.log("Should delete class with name: " + className, null);		
		if (className == null) {return;	}
		
		OntologyServiceManager.getInstance().deleteCls(project.getProjectName(),
				className, new DeleteClassHandler());
		
		//try to refresh the tree earlier
		refreshFromServer();		
	} 

	protected void renameClass(String oldName, String newName) {
		GWT.log("Should rename class from " + oldName + " to " + newName, null);
		if (oldName.equals(newName)  || newName == null || newName.length() == 0) {
			return;
		}
		
		OntologyServiceManager.getInstance().renameEntity(project.getProjectName(), oldName, newName,
				new RenameClassHandler());
	}
	
	
	public TreePanel getTreePanel() {
		return treePanel;
	}
	
	protected void afterRender() {			
		getRootCls();		
	}
	
	public void setTreeNodeIcon(TreeNode node) {
		node.setIconCls("protege-class-icon");
	}
	
	public void getSubclasses(final String clsName, TreeNode parentNode) {
		if (isSubclassesLoaded(parentNode)) { return; }		
		OntologyServiceManager.getInstance().getSubclasses(project.getProjectName(), clsName, 
				new GetSubclassesOfClassHandler(clsName, parentNode, null));
	}	

	
	protected String getStoredSubclassCount(TreeNode node) {
		return node.getAttribute("scc");		
	}
	

	public boolean isSubclassesLoaded(TreeNode node) {
		String val = node.getAttribute("subclassesLoaded");
		return val != null && val.equals("true");
	}
	
	public void setSubclassesLoaded(TreeNode node, boolean loaded) {		
		node.setAttribute("subclassesLoaded", loaded ? "true" : "false");
	}

	public void getRootCls() {
		String rootClsName = getRootClsName();
		if (rootClsName != null) {
			createRoot(new EntityData(rootClsName)); //TODO - get the real one from the server..
		} else {
			OntologyServiceManager.getInstance().getRootEntity(project.getProjectName(), new GetRootClassHandler());
		}
	}

	private String getRootClsName() {		
		PortletConfiguration portletConfiguration = getPortletConfiguration();
		if (portletConfiguration == null) { return null; }
		Map<String, Object> props = portletConfiguration.getProperties();
		if (props == null) { return null; }
		return (String) props.get(TOP_CLASS_PROP);		
	}
	
	
	protected void moveClass(String clsName, String oldParentName, String newParentName) {
		if (oldParentName.equals(newParentName)) {
			return;
		}
		OntologyServiceManager.getInstance().moveCls(project.getProjectName(), clsName, oldParentName, newParentName,
				new MoveClassHandler(clsName, oldParentName, newParentName));		
	}

	
	public void getPathToRoot(EntityData entity) {
		OntologyServiceManager.getInstance().getPathToRoot(project.getProjectName(), entity.getName(), new GetPathToRootHandler());
	}

	
	public ArrayList<EntityData> getSelection() {		
		return currentSelection;
	}
	
	@Override
	public void setSelection(Collection<EntityData> selection) {
		if (selection == null || selection.isEmpty()) { return; } //TODO: how to clear selection?
		GWT.log("Select in class tree: " + selection, null);
		getPathToRoot(selection.iterator().next()); //FIXME: just take first elemtne in selection for now
	}

	
	public void selectPathInTree(List<EntityData> path) {		
		selectPathInTree(path, treePanel.getRootNode(), 1);		
	}	
	
	private void selectPathInTree(List<EntityData> path, TreeNode parentNode, int index) {
		for (int i = index; i < path.size(); i++) {
			String clsName = path.get(i).getName();
			TreeNode node = findTreeNode(clsName);
			if (node == null) {				
				EntityData parentEntityData = (EntityData)parentNode.getUserObject();
				OntologyServiceManager.getInstance().getSubclasses(project.getProjectName(), parentEntityData.getName(), 
						new SelectInTreeHandler(parentNode, path, i));				
				return;			
			} else {
				parentNode = node;
				if (i == path.size() - 1) {
					node.select();
				} else {
					expandDisabled = true;
					node.expand();
					expandDisabled = false;
				}
			}
		}
	}
	
	
	protected TreeNode createTreeNode(EntityData entityData) {
		TreeNode node = new TreeNode(entityData.getBrowserText());	
		node.setHref(null);
		node.setUserObject(entityData);
		node.setAllowDrag(true);
		node.setAllowDrop(true);
		setTreeNodeIcon(node);
		
		if(entityData.hasAnnotation()) {
			//TODO: add a css for this
			node.setText(entityData.getBrowserText()+ " <img src=\"images/comment.gif\" />");
		}
		
		return node;
	}
	
	
	private boolean hasChild(TreeNode parentNode, String childId) {		
		return getDirectChild(parentNode, childId) != null;
	}
	
	protected void createRoot(EntityData rootEnitity) {
		remove(PLACE_HOLDER_PANEL);			
		//ClassTreePortlet.this.doLayout();
			
		treePanel = createTreePanel();			
		TreeNode root = createTreeNode(rootEnitity);				
		treePanel.setRootNode(root);
		add(treePanel);	
		
		try { //TODO: could not figure out why it throws exceptions sometimes, not elegant but it works
			doLayout();	
		} catch (Exception e) {
			GWT.log("Error at doLayout in class tree", e);
		}
		
		root.select();
		getSubclasses(rootEnitity.getName(), root);		
		root.expand(); //TODO: does not seem to work
	}
	
	
	private TreeNode getDirectChild(TreeNode parentNode, String childId) {
		Node[] children = parentNode.getChildNodes();
		for (int i = 0; i < children.length; i++) {
			Node child = children[i];
			if (getNodeClsName(child).equals(childId)) {
				return (TreeNode)child;
			}
		}		
		return null;
	}
	
	
	protected void onRefresh() {		
		//TODO: not ideal
		TreeNode root = treePanel.getRootNode();
		
		Node[] children = root.getChildNodes();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				TreeNode child = (TreeNode) children[i];
				root.removeChild(child);
				setSubclassesLoaded(child, false);
			}
		}
		EntityData rootEntity = (EntityData)root.getUserObject(); 
			
		setSubclassesLoaded(root, false);
		root.select();
		getSubclasses(rootEntity.getName(), root);			
		doLayout();
		root.expand();
	}	
	

	public void updateButtonStates() {		
		if (project.hasWritePermission()) {
			createButton.enable();
			deleteButton.enable();
		} else {
			createButton.disable();
			deleteButton.disable();
		}		
	}	
	
	public String getNodeClsName(Node node) {
		EntityData data = (EntityData) node.getUserObject();
		return data.getName();
	}
	
	
	@Override
	public void onLogin(String userName) {
		updateButtonStates();
	}
	
	@Override
	public void onLogout(String userName) {
		updateButtonStates();
	}
	
	
	/*
	 *************  Remote procedure calls *****************
	 */
	
	class GetRootClassHandler extends AbstractAsyncHandler<EntityData> {

		public void handleFailure(Throwable caught) {
			getEl().unmask();
			GWT.log("RPC error at getting classes root ", caught);			
		}

		public void handleSuccess(EntityData rootEnitity) {		
			getEl().unmask();		
			createRoot(rootEnitity);
		}		
	}
		
	
	class GetSubclassesOfClassHandler extends AbstractAsyncHandler<ArrayList<SubclassEntityData>> {

		private String clsName;		
		private TreeNode parentNode;
		private AsyncCallback<Object> endCallback;

		public GetSubclassesOfClassHandler(String className, TreeNode parentNode, AsyncCallback<Object> endCallback) {
			super();
			this.clsName = className;		
			this.parentNode = parentNode;
			this.endCallback = endCallback;
		}

		public void handleFailure(Throwable caught) {
			getEl().unmask();			
			GWT.log("RPC error at getting subclasses of " + clsName, caught);
			if (endCallback != null) {
				endCallback.onFailure(caught);
			}
		}

		public void handleSuccess(ArrayList<SubclassEntityData> result) {
			getEl().unmask();
			ArrayList<SubclassEntityData> children = result;			
			
			for (Iterator<SubclassEntityData> iterator = children.iterator(); iterator.hasNext();) {
				SubclassEntityData childData = (SubclassEntityData) iterator.next();
				if (!hasChild(parentNode, childData.getName())) {
					TreeNode childNode = createTreeNode(childData);
					if (childData.getSubclassCount() > 0) {
						childNode.setExpandable(true);
					}
					parentNode.appendChild(childNode);
				}
			}
			
			setSubclassesLoaded(parentNode, true);
			if (endCallback != null) {
				endCallback.onSuccess(result);
			}
		}
	}

	
	class CreateClassHandler extends AbstractAsyncHandler<EntityData> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at creating class", caught);
			 MessageBox.alert("There were errors at creating class.<br>" +
			 		" Message: " + caught.getMessage());
		}

		public void handleSuccess(EntityData entityData) {			
			if (entityData != null) {
				GWT.log("Created successfully class " + entityData.getName(), null);
			} else {
				GWT.log("Problem at creating class", null);
				MessageBox.alert("Class creation failed.");
			}
		}		
	}

	
	class DeleteClassHandler extends AbstractAsyncHandler<Void> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at deleting class", caught);
			 MessageBox.alert("There were errors at deleting class.<br>" +
			 		" Message: " + caught.getMessage());
		}

		@Override
		public void handleSuccess(Void result) {
			GWT.log("Delete successfully class ", null);
			
		}		
	}
	
	class MoveClassHandler extends AbstractAsyncHandler<Void> {
		private String clsName;
		private String oldParentName;
		private String newParentName;

		public MoveClassHandler(String clsName, String oldParentName, String newParentName) {
			this.clsName = clsName;
			this.oldParentName = oldParentName;
			this.newParentName = newParentName;
		}
		
		public void handleFailure(Throwable caught) {
			GWT.log("Error at moving class", caught);
			 MessageBox.alert("There were errors at moving class.<br>" +
			 		" Message: " + caught.getMessage());
			 //TODO: refresh oldParent and newParent
		}

		public void handleSuccess(Void result) {			
			GWT.log("Moved successfully class " + clsName, null);		
		}		
	}
	
	class RenameClassHandler extends AbstractAsyncHandler<EntityData> {
		public void handleFailure(Throwable caught) {
			MessageBox.alert("Class rename failed.<br>" +
					"Message: " + caught.getMessage());			
		}

		public void handleSuccess(EntityData result) {
			GWT.log("Rename succeded!", null);			
		}		
	}	

	
	//TODO: not used - will be used for the selection in tree
	class GetPathToRootHandler extends AbstractAsyncHandler<ArrayList<EntityData>> {		
		@Override
		public void handleFailure(Throwable caught) {
			GWT.log("Error at finding path to root", caught);			
		}

		@Override
		public void handleSuccess(ArrayList<EntityData> result) {
			GWT.log(result.toString(), null);
			if (result == null || result.size() == 0) {
				GWT.log("Could not find path in the tree", null);
				return;
			}
			String path = "";
			for (EntityData entity : result) {
				path = path + entity.getBrowserText() + " --> <br/>";				
			}
			path = path.substring(0, path.length() - 10);
			GWT.log("Selection path in tree: " + path, null);
			selectPathInTree(result);
			
		}		
	}
	
	class SelectInTreeHandler extends AbstractAsyncHandler<ArrayList<SubclassEntityData>> {
		
		private TreeNode parentNode;	
		private List<EntityData> path;
		private int index;

		public SelectInTreeHandler(TreeNode parentNode, List<EntityData> path, int index) {
			super();		
			this.parentNode = parentNode;			
			this.index = index;
			this.path = path;
		}

		public void handleFailure(Throwable caught) {
			getEl().unmask();
			GWT.log("RPC error at select in tree for " + parentNode.getUserObject(), caught);			
		}

		public void handleSuccess(ArrayList<SubclassEntityData> children) {
			getEl().unmask();			
			
			TreeNode pathTreeNode = null;
			
			EntityData nextParent = path.get(index);
			
			for (Iterator<SubclassEntityData> iterator = children.iterator(); iterator.hasNext();) {
				SubclassEntityData childData = (SubclassEntityData) iterator.next();
				if (!hasChild(parentNode, childData.getName())) {
					TreeNode childNode = createTreeNode(childData);
					if (childData.getSubclassCount() > 0) {
						childNode.setExpandable(true);
					}
					parentNode.appendChild(childNode);
				}
				if (childData.equals(nextParent)) {
					pathTreeNode = getDirectChild(parentNode, childData.getName());
				}				
			}
			
			setSubclassesLoaded(parentNode, true);
			
			if (pathTreeNode != null) {
				expandDisabled = true;
				pathTreeNode.expand();
				expandDisabled = false;
				if (path.size() - 1 == index) {
					pathTreeNode.select();
					EntityData entityData = (EntityData)pathTreeNode.getUserObject();
					setEntity(entityData);
					currentSelection = new ArrayList<EntityData>();
					currentSelection.add(entityData);
					notifySelectionListeners(new SelectionEvent(ClassTreePortlet.this));
				} else {
					selectPathInTree(path, pathTreeNode, index + 1);
				}
			} else {
				GWT.log("Error at select in tree: could not find child " + nextParent + " of " + parentNode.getUserObject(), null);
			}
		}
	}

	
}
