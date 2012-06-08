package edu.stanford.bmir.protege.web.client.ui.ontology.properties;

import java.util.ArrayList;
import java.util.Iterator;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Node;
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.tree.TreeNode;
import com.gwtext.client.widgets.tree.TreePanel;
import com.gwtext.client.widgets.tree.event.TreePanelListenerAdapter;

import edu.stanford.bmir.protege.web.client.event.EntityCreateEvent;
import edu.stanford.bmir.protege.web.client.event.EntityDeleteEvent;
import edu.stanford.bmir.protege.web.client.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.event.EventType;
import edu.stanford.bmir.protege.web.client.event.OntologyListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyType;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.util.Project;
import edu.stanford.bmir.protege.web.client.util.SelectionEvent;

public class PropertiesTreePortlet extends AbstractEntityPortlet {

	protected TreePanel treePanel;
	
	protected ArrayList<EntityData> currentSelection;
	private Button createButton;
	private Button createSubButton;
	private Button deleteButton;

	public PropertiesTreePortlet(Project project) {
		super(project);		
	}
	
	public void reload() {	
		Node[] nodes = treePanel.getRootNode().getChildNodes();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].remove();
		}
		getSubProperties(null, true);
	}
	
	public void intialize() {
		setLayout(new FitLayout());
		setTitle("Properties Tree");
						
		treePanel = new TreePanel();		
		treePanel.setHeight(500);		
		treePanel.setAutoWidth(true);		
		treePanel.setAnimate(true);
		treePanel.setAutoScroll(true);
		
		treePanel.addListener(new TreePanelListenerAdapter() {
			public void onClick(TreeNode node, EventObject e) {
				currentSelection = new ArrayList<EntityData>();
				currentSelection.add((EntityData)node.getUserObject());
				notifySelectionListeners(new SelectionEvent(PropertiesTreePortlet.this));
			}

			public void onExpandNode(TreeNode node) {
				getSubProperties(node.getId(), true);
			}
		});

		// Temporary - to be replaced when ontology is loaded.
		TreeNode root = new TreeNode((String)null);
		root.setId("RootPropertyNode");
		root.setHref("");
		root.setUserObject(new PropertyEntityData("RootPropertyNode", "RootPropertyNode", null, false));
		setTreeNodeIcon(root);
	
		treePanel.setRootNode(root);
		treePanel.setRootVisible(false);
		
		addToolbarButtons();
		addProjectListeners();
		add(treePanel);
	}
	
	
	protected void addProjectListeners() {
		project.addOntologyListener(new OntologyListenerAdapter() {			
			public void entityCreated(EntityCreateEvent ontologyEvent) {				
				if (ontologyEvent.getType() == EventType.PROPERTY_CREATED) {
					//GWT.log("Created", null);
					onClassCreated(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities());
				} else if (ontologyEvent.getType() == EventType.SUBPROPERTY_ADDED) {
					//GWT.log("Sub addded", null);
					onSubclassAdded(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities(), false);
				}  else if (ontologyEvent.getType() == EventType.SUBPROPERTY_REMOVED) {
					//GWT.log("Sub removed", null);
					onSubclassRemoved(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities());
				} 
			}
			
			public void entityRenamed(EntityRenameEvent renameEvent) {
				onClassRename(renameEvent.getEntity(), renameEvent.getOldName());
			}
			
			@Override
			public void entityDeleted(EntityDeleteEvent ontologyEvent) {
				//GWT.log("Property deleted", null);
				onPropertyDeleted(ontologyEvent.getEntity());					
			}
			
		});
	}
		

	protected void onClassCreated(EntityData entity, ArrayList<EntityData> superEntities) {
		if (superEntities == null) {
			GWT.log("Entity created: " + entity.getName() + " but unknown superEntities", null);
			return;
		}
		
		if (superEntities.size() == 0) {
			TreeNode newNode = createTreeNode(entity);
			Node firstNode = treePanel.getRootNode().getFirstChild();
			if (firstNode != null) {
				treePanel.getRootNode().insertBefore(newNode, firstNode);
			} else {			
				treePanel.getRootNode().appendChild(newNode);
			}
			treePanel.doLayout();
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
	
	
	protected void insertNodeInTree(TreeNode parentNode, EntityData child) {
		TreeNode treeNode = createTreeNode(child);
		parentNode.appendChild(treeNode);
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

	
	protected void onPropertyDeleted(EntityData entity) {
		TreeNode propNode = findTreeNode(entity.getName());
		if (propNode != null) {
			Node parentNode = propNode.getParentNode();
			if (parentNode != null) {
				parentNode.removeChild(propNode);
			} else {
				propNode.remove();
			}
		}		
	}
	
	protected void onClassRename(EntityData entity, String oldName) {
		TreeNode oldNode = findTreeNode(oldName);		
		if (oldNode == null) {return;}		
		TreeNode newNode = createTreeNode(entity);		
		oldNode.getParentNode().replaceChild(newNode, oldNode);		
	}
	
	private boolean hasChild(TreeNode parentNode, String childId) {		
		return getDirectChild(parentNode, childId) != null;
	}
	
	
	protected void addToolbarButtons() {
		createButton = new Button("Create");		
		createButton.setCls("toolbar-button");
		createButton.addListener(new ButtonListenerAdapter() {			
			public void onClick(Button button, EventObject e) {
				onCreateProperty(false);
			}
		});
		
		if (!project.hasWritePermission()) {
			createButton.setDisabled(true);
		}
		
		createSubButton = new Button("Create subproperty");		
		createSubButton.setCls("toolbar-button");
		createSubButton.addListener(new ButtonListenerAdapter() {			
			public void onClick(Button button, EventObject e) {
				onCreateProperty(true);
			}
		});
		
		if (!project.hasWritePermission()) {
			createSubButton.setDisabled(true);	
		}		
		
		deleteButton = new Button("Delete");
		//deleteButton.setIconCls("protege-class-delete-icon");
		deleteButton.setCls("toolbar-button");
		deleteButton.addListener(new ButtonListenerAdapter() {			
			public void onClick(Button button, EventObject e) {			
				onDeleteProperty();
			}
		});
		
		if (!project.hasWritePermission()) {
			deleteButton.setDisabled(true);
		}
				
		setTopToolbar(new Button[]{createButton, createSubButton, deleteButton});
	}
	
	protected void onCreateProperty(final boolean createSub) {
		final Window window = new Window();  
		window.setTitle("Create property");		
		window.setClosable(true);				
		window.setPaddings(7);
		window.setCloseAction(Window.HIDE);
		window.add(new CreatePropertyPanel(window, createSub));
		window.show();		
	}	
	
	protected void onDeleteProperty() {
		if (currentSelection == null || currentSelection.size() == 0) {
			MessageBox.alert("Please select first a property to delete.");
			return; 
		}
		
		final String clsName = ((EntityData)currentSelection.get(0)).getName();
		
		MessageBox.confirm("Confirm", "Are you sure you want to delete property <br> " + clsName + " ?",  
				new MessageBox.ConfirmCallback() {  
			public void execute(String btnID) { 
				if (btnID.equals("yes")) {									
					deleteProperty(clsName);
				}
			}  
		});
	}
	
	protected void createProperty(final String className, boolean createSub, PropertyType propertyType) {
		String superClsName = null;
		if (currentSelection != null && currentSelection.size() > 0 && createSub) {
			superClsName = ((EntityData)currentSelection.get(0)).getName(); 
		}
		
		//TODO: make this better
		if (propertyType == PropertyType.OBJECT) {
			OntologyServiceManager.getInstance().createObjectProperty(project.getProjectName(),
					className, superClsName, new CreatePropertyHandler());				
		} else if (propertyType == PropertyType.DATATYPE) {
			OntologyServiceManager.getInstance().createDatatypeProperty(project.getProjectName(),
					className, superClsName, new CreatePropertyHandler());
			 
		} else if (propertyType == PropertyType.ANNOTATION) {
			OntologyServiceManager.getInstance().createAnnotationProperty(project.getProjectName(),
					className, superClsName, new CreatePropertyHandler());
			
		} else {
			OntologyServiceManager.getInstance().createDatatypeProperty(project.getProjectName(),
					className, superClsName, new CreatePropertyHandler());			
		}
				
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
	
	
	protected void deleteProperty(String className) {		
		GWT.log("Should delete property with name: " + className, null);		
		if (className == null) {return;	}
		
		OntologyServiceManager.getInstance().deleteProperty(project.getProjectName(),
				className, new DeletePropertyHandler());
		
		//try to refresh the tree earlier
		refreshFromServer();		
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
	
	public String getNodeClsName(Node node) {
		EntityData data = (EntityData) node.getUserObject();
		return data.getName();
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
	
	@Override
	public void onLogin(String userName) {
		updateButtonStates();
	}
	
	@Override
	public void onLogout(String userName) {
		updateButtonStates();
	}
	
	public void updateButtonStates() {		
		if (project.hasWritePermission()) {
			createButton.enable();
			deleteButton.enable();
			createSubButton.enable();
		} else {
			createButton.disable();
			deleteButton.disable();
			createSubButton.disable();
		}		
	}	
	
	
	public TreePanel getTreePanel() {
		return treePanel;
	}
	
	protected void afterRender() {
		getSubProperties(null, true);
		super.afterRender();
	}
	
	public void setTreeNodeIcon(TreeNode node) {
		PropertyEntityData entityData = (PropertyEntityData) node.getUserObject();
		PropertyType type = entityData.getPropertyType();
		if (type == PropertyType.OBJECT) {
			node.setIconCls("protege-object-property-icon");
		} else if (type == PropertyType.DATATYPE) {
			node.setIconCls("protege-datatype-property-icon");
		}  else if (type == PropertyType.ANNOTATION) {
			node.setIconCls("protege-annotation-property-icon");
		} else {
			node.setIconCls("protege-slot-icon");
		}
	}

	public void getSubProperties(final String propName, final boolean getSubpropertiesOfSubproperties) {
		OntologyServiceManager.getInstance().getSubproperties(project.getProjectName(), propName, 
				new GetSubproperties(propName, getSubpropertiesOfSubproperties));
	}


	public ArrayList getSelection() {		
		return currentSelection;
	}
	
	protected TreeNode createTreeNode(EntityData entityData) {
		TreeNode node = new TreeNode(entityData.getBrowserText());		
		node.setId(entityData.getName());
		node.setHref(null);
		node.setUserObject(entityData);
		setTreeNodeIcon(node);
		
		if(entityData.hasAnnotation()) {
			//TODO: add a css for this
			node.setText(entityData.getBrowserText()+ " <img src=\"images/comment.gif\" />");
		}
		
		return node;
	}
	
	/*
	 * Remote procedure calls
	 */	
	class GetSubproperties extends AbstractAsyncHandler {

		private String propName;
		private boolean getSubpropertiesOfSubproperties;

		public GetSubproperties(String className, boolean getSubpropertiesOfSubproperties) {
			super();
			this.propName = className;
			this.getSubpropertiesOfSubproperties = getSubpropertiesOfSubproperties;
		}

		public void handleFailure(Throwable caught) {
			GWT.log("RPC error at getting subproperties of " + propName, caught);			
		}

		public void handleSuccess(Object result) {			
			ArrayList children = (ArrayList) result;
			
			if (propName == null) {
				propName = "RootPropertyNode";
			}
			TreeNode parentNode = treePanel.getNodeById(propName);

			if (parentNode == null) {
				GWT.log("Cannot find node: " + propName, null);
				return;
			}

			//already loaded?
			//TODO - not a very good method
			if (parentNode.getFirstChild() != null) {
				//children are already fetched? Maybe?
				//TODO: think about this situation

				if (getSubpropertiesOfSubproperties) {
					for (Iterator iterator = children.iterator(); iterator.hasNext();) {
						EntityData child = (EntityData) iterator.next();
						getSubProperties(child.getName(), false);
					}
				}
				return;
			}

			for (Iterator iterator = children.iterator(); iterator.hasNext();) {
				EntityData childData = (EntityData) iterator.next();

				//if (!hasChild(clsName, childName)) {
				parentNode.appendChild(createTreeNode(childData));
				//}
			}

			if (getSubpropertiesOfSubproperties) {
				for (Iterator iterator = children.iterator(); iterator.hasNext();) {
					EntityData childName = (EntityData) iterator.next();
					getSubProperties(childName.getName(), false);
				}
			}
		}

		private boolean hasChild(String parentId, String childId) {
			TreeNode parentNode = treePanel.getNodeById(propName);

			Node[] children = parentNode.getChildNodes();

			for (int i = 0; i < children.length; i++) {
				Node child = children[i];

				if (child.getId().equals(childId)) {
					return true;
				}
			}		
			return false;
		}				
	}
	
	
	class CreatePropertyHandler extends AbstractAsyncHandler<EntityData> {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at creating class", caught);
			 MessageBox.alert("There were errors at creating the property.<br>" +
			 		" Message: " + caught.getMessage());
		}

		public void handleSuccess(EntityData entityData) {			
			if (entityData != null) {
				GWT.log("Created successfully property " + entityData.getName(), null);
			} else {
				GWT.log("Problem at creating property", null);
				MessageBox.alert("Property creation failed.");
			}
		}		
	}

	
	class DeletePropertyHandler extends AbstractAsyncHandler {

		public void handleFailure(Throwable caught) {
			GWT.log("Error at deleting class", caught);
			 MessageBox.alert("There were errors at deleting property.<br>" +
			 		" Message: " + caught.getMessage());
		}

		public void handleSuccess(Object result) {			
			GWT.log("Delete successfully property ", null);		
		}		
	}
	
	/*
	 * Create property panel
	 */
	
	class CreatePropertyPanel extends FormPanel {		
		private boolean createSub;
		private Window parent;
		private TextField propertyNameTextField;
		private ComboBox propertyTypeComboBox;
	
		public CreatePropertyPanel(Window parent, boolean createSub) {
			super();
			this.parent = parent;
			this.createSub = createSub;
			
			setWidth(450);			
			setLabelWidth(150);
			setPaddings(15);

			propertyNameTextField = new TextField("Property name", "propName");
			propertyNameTextField.setAllowBlank(false);
			add(propertyNameTextField);
			
			Button createButton = new Button("Create", new ButtonListenerAdapter() {
				public void onClick(Button button, EventObject e) {	
					String text = propertyNameTextField.getValueAsString().trim();					
					PropertyType propertyType = getPropertyType();
					CreatePropertyPanel.this.parent.close();
					createProperty(text, CreatePropertyPanel.this.createSub, propertyType);					
				}
			});
			
			propertyTypeComboBox = createTypeComboBox();
			add(propertyTypeComboBox);
			
			addButton(createButton);
		}
		
		private ComboBox createTypeComboBox() {
			Store cbstore = new SimpleStore(new String[]{"propType"}, getOWLPropertyTypes() );   
	        cbstore.load();   
	        ComboBox cb = new ComboBox();        
	        cb.setStore(cbstore);
	        cb.setForceSelection(true);   
		    cb.setMinChars(1);   
		    cb.setFieldLabel("Type");   
		    cb.setDisplayField("propType");   
		    cb.setMode(ComboBox.LOCAL);   
		    cb.setTriggerAction(ComboBox.ALL);   
		    cb.setEmptyText("Select Type");   
		    cb.setTypeAhead(true);   
		    cb.setSelectOnFocus(true);
		    cb.setWidth(200);   
		    cb.setHideTrigger(false);
		    cb.setValue("Datatype");		    
	        return cb;
		}
		
		private String[][] getOWLPropertyTypes() {
			String[][]   commentTypes = new String[][]{   
					new String[]{"Object"},   
					new String[]{"Datatype"},
					new String[]{"Annotation"},
			}; 
			return commentTypes;
		} 
		
		private PropertyType getPropertyType() {
			String type = propertyTypeComboBox.getValueAsString();
			//GWT.log("Prop type combo: " + type, null);
			if (type.equals("Object")) {
				return PropertyType.OBJECT;
			} else if (type.equals("Datatype")) {
				return PropertyType.DATATYPE;
			} else if (type.equals("Annotation")) {
				return PropertyType.ANNOTATION;
			}
			return null;
		}
		
		
	}
	
}
