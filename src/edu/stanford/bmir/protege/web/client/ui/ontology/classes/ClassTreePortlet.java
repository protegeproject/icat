package edu.stanford.bmir.protege.web.client.ui.ontology.classes;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Position;
import com.gwtext.client.data.Node;
import com.gwtext.client.dd.DragData;
import com.gwtext.client.dd.DragDrop;
import com.gwtext.client.widgets.*;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.SplitButtonListenerAdapter;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.TextFieldListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.menu.*;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;
import com.gwtext.client.widgets.menu.event.CheckItemListenerAdapter;
import com.gwtext.client.widgets.tree.*;
import com.gwtext.client.widgets.tree.event.DefaultSelectionModelListenerAdapter;
import com.gwtext.client.widgets.tree.event.MultiSelectionModelListener;
import com.gwtext.client.widgets.tree.event.TreeNodeListenerAdapter;
import com.gwtext.client.widgets.tree.event.TreePanelListenerAdapter;
import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.model.event.EntityCreateEvent;
import edu.stanford.bmir.protege.web.client.model.event.EntityRenameEvent;
import edu.stanford.bmir.protege.web.client.model.event.EventType;
import edu.stanford.bmir.protege.web.client.model.event.PropertyValueEvent;
import edu.stanford.bmir.protege.web.client.model.listener.OntologyListenerAdapter;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ChAOServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.Triple;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.ontology.notes.NoteInputPanel;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.search.SearchUtil;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Portlet for displaying class trees. It can be configured to show only a
 * subtree of an ontology, by setting the portlet property <code>topClass</code>
 * to the name of the top class to show. <br>
 * Also supports creating and editing classes.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class ClassTreePortlet extends AbstractEntityPortlet {

    private static final String SUFFIX_ID_LOCAL_ANNOTATION_COUNT = "_locAnnCnt";
    private static final String SUFFIX_ID_LOCAL_ANNOTATION_IMG = "_locAnnImg";
    /*
     * Configuration constants and defaults
     */
    private static final String CREATE_ACTION_DESC_PROP = "create_action_desc";
    private static final String CREATE_ACTION_DESC_DEFAULT = "Created class";

    private static final String DELETE_ACTION_DESC_PROP = "delete_action_desc";
    private static final String DELETE_ACTION_DESC_DEFAULT = "Deleted class";

    private static final String RENAME_ACTION_DESC_PROP = "rename_action_desc";
    private static final String RENAME_ACTION_DESC_DEFAULT = "Renamed class";

    private static final String MOVE_ACTION_DESC_PROP = "move_action_desc";
    private static final String MOVE_ACTION_DESC_DEFAULT = "Moved class";

    private static final String CREATE_LABEL_PROP = "create_label";
    private static final String CREATE_LABEL_DEFAULT = "Create";

    private static final String DELETE_LABEL_PROP = "delete_label";
    private static final String DELETE_LABEL_DEFAULT = "Delete";

    private static final String WATCH_LABEL_PROP = "watch_label";
    private static final String WATCH_LABEL_DEFAULT = "Watch";

    private static final String WATCH_BRANCH_LABEL_PROP = "watch_branch_label";
    private static final String WATCH_BRANCH_LABEL_DEFAULT = "Watch Branch";

    private static final String PLACE_HOLDER_PANEL = "placeHolderPanel";
    private static final String TOP_CLASS_PROP = "topClass";

    private TreePanel treePanel;

    private ToolbarButton createButton;
    private ToolbarButton deleteButton;
    private ToolbarMenuButton watchButton;

    private boolean expandDisabled = false;
    private boolean showToolbar = true;
    private boolean showTitle = true;
    private boolean showTools = true;

    private boolean allowsMultiSelection = false;
    private String hierarchyProperty = null;
    private String topClass = null;

    public ClassTreePortlet(Project project) {
        this(project, true, true, true, false, null);
    }

    public ClassTreePortlet(Project project, boolean showToolbar, boolean showTitle, boolean showTools, boolean allowsMultiSelection, String topClass) {
        super(project, false);
        this.showToolbar = showToolbar;
        this.showTitle = showTitle;
        this.showTools = showTools;
        this.allowsMultiSelection = allowsMultiSelection;
        this.topClass = topClass;
        initialize();
    }

    protected String getCreateClsDescription() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), CREATE_ACTION_DESC_PROP,
                CREATE_ACTION_DESC_DEFAULT);
    }

    protected String getDeleteClsDescription() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), DELETE_ACTION_DESC_PROP,
                DELETE_ACTION_DESC_DEFAULT);
    }

    protected String getRenameClsDescription() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), RENAME_ACTION_DESC_PROP,
                RENAME_ACTION_DESC_DEFAULT);
    }

    protected String getMoveClsDescription() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), MOVE_ACTION_DESC_PROP,
                MOVE_ACTION_DESC_DEFAULT);
    }

    protected String getCreateClsLabel() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), CREATE_LABEL_PROP, CREATE_LABEL_DEFAULT);
    }

    protected String getDeleteClsLabel() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), DELETE_LABEL_PROP, DELETE_LABEL_DEFAULT);
    }

    protected String getWatchClsLabel() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), WATCH_LABEL_PROP, WATCH_LABEL_DEFAULT);
    }

    protected String getWatchBranchClsLabel() {
        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), WATCH_BRANCH_LABEL_PROP, WATCH_BRANCH_LABEL_DEFAULT);
    }

    @Override
    public void reload() {
    }

    @Override
    public void initialize() {
        setLayout(new FitLayout());

        setTools(getTools());

        if (showTitle) {
            setTitle("Classes");
        }

        if (showToolbar) {
            addToolbarButtons();
        }

        Panel bogusPanel = new Panel();
        bogusPanel.setId(PLACE_HOLDER_PANEL);
        bogusPanel.setHeight(560);
        add(bogusPanel);
        updateButtonStates();
    }

    public TreePanel createTreePanel() {
        treePanel = new TreePanel();
        treePanel.setHeight(560);
        treePanel.setAutoWidth(true);
        treePanel.setAnimate(true);
        treePanel.setAutoScroll(true);
        treePanel.setEnableDD(true);

        if (allowsMultiSelection) {
            treePanel.setSelectionModel(new MultiSelectionModel());
        }

        treePanel.addListener(new TreePanelListenerAdapter() {
            @Override
            public void onExpandNode(TreeNode node) {
                if (!expandDisabled && !treePanel.getRootNode().equals(node)) {
                    GWT.log("Expand node " + node.getUserObject(), null);
                    getSubclasses(((EntityData) node.getUserObject()).getName(), node);
                }
            }
        });


        addDragAndDropSupport();
        addProjectListeners();

        return treePanel;
    }


    protected void createSelectionListener() {
        TreeSelectionModel selModel = treePanel.getSelectionModel();
        if (selModel instanceof DefaultSelectionModel) {
            ((DefaultSelectionModel) selModel).addSelectionModelListener(new DefaultSelectionModelListenerAdapter() {
                @Override
                public void onSelectionChange(DefaultSelectionModel sm, TreeNode node) {
                    notifySelectionListeners(new SelectionEvent(ClassTreePortlet.this));
                }
            });
        } else if (selModel instanceof MultiSelectionModel) {
            ((MultiSelectionModel) selModel).addSelectionModelListener(new MultiSelectionModelListener() {
                public void onSelectionChange(MultiSelectionModel sm, TreeNode[] nodes) {
                    notifySelectionListeners(new SelectionEvent(ClassTreePortlet.this));
                }
            });
        } else {
            GWT.log("Unknown tree selection model for class tree: " + selModel, null);
        }
    }

    protected void addProjectListeners() {
        if (hierarchyProperty != null) { // hierarchy of property
            project.addOntologyListener(new OntologyListenerAdapter() {
                @Override
                public void entityRenamed(EntityRenameEvent renameEvent) {
                    onClassRename(renameEvent.getEntity(), renameEvent.getOldName());
                }

                @Override
                public void propertyValueAdded(PropertyValueEvent propertyValueEvent) {
                    GWT.log("Property value added event: " + propertyValueEvent.getEntity() + " "
                            + propertyValueEvent.getProperty() + " " + propertyValueEvent.getAddedValues(), null);
                    if (propertyValueEvent.getProperty().getName().equals(hierarchyProperty)) {
                        onSubclassAdded(propertyValueEvent.getEntity(), propertyValueEvent.getAddedValues(), false);
                    }
                }

                @Override
                public void propertyValueRemoved(PropertyValueEvent propertyValueEvent) {
                    GWT.log("Property value removed event: " + propertyValueEvent.getEntity() + " "
                            + propertyValueEvent.getProperty() + " " + propertyValueEvent.getRemovedValues(), null);
                    if (propertyValueEvent.getProperty().getName().equals(hierarchyProperty)) {
                        onSubclassRemoved(propertyValueEvent.getEntity(), propertyValueEvent.getRemovedValues());
                    }
                }

                @Override
                public void propertyValueChanged(PropertyValueEvent propertyValueEvent) {
                    GWT.log("Property value changed event: " + propertyValueEvent.getEntity() + " "
                            + propertyValueEvent.getProperty() + " " + propertyValueEvent.getAddedValues() + "  "
                            + propertyValueEvent.getRemovedValues(), null);
                }

            });
        } else { // subclass of

            project.addOntologyListener(new OntologyListenerAdapter() {
                @Override
                public void entityCreated(EntityCreateEvent ontologyEvent) {
                    if (ontologyEvent.getType() == EventType.SUBCLASS_ADDED) {
                        onSubclassAdded(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities(), false);
                    } else if (ontologyEvent.getType() == EventType.SUBCLASS_REMOVED) {
                        onSubclassRemoved(ontologyEvent.getEntity(), ontologyEvent.getSuperEntities());
                    }
                }

                @Override
                public void entityRenamed(EntityRenameEvent renameEvent) {
                    onClassRename(renameEvent.getEntity(), renameEvent.getOldName());
                }
            });
        }
    }

    @Override
    protected Tool[] getTools() {
        return showTools ? super.getTools() : new Tool[]{};
    }


    protected void addToolbarButtons() {
        setTopToolbar(new Toolbar());
        Toolbar toolbar = getTopToolbar();

        createButton = createCreateButton();
        if (createButton != null) {
            toolbar.addButton(createButton);
        }

        deleteButton = createDeletButton();
        if (deleteButton != null) {
            toolbar.addButton(deleteButton);
        }

        watchButton = createWatchButton();
        if (watchButton != null) {
            toolbar.addButton(watchButton);
        }

        Component searchField = createSearchField();
        if (searchField != null) {
            toolbar.addFill();
            toolbar.addSeparator();
            toolbar.addText("&nbsp<i>Search</i>:&nbsp&nbsp");
            toolbar.addElement(searchField.getElement());
        }
    }

    protected ToolbarButton createCreateButton() {
        createButton = new ToolbarButton(getCreateClsLabel());
        createButton.setCls("toolbar-button");
        createButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                onCreateCls();
            }
        });
        createButton.setDisabled(!project.hasWritePermission(GlobalSettings.getGlobalSettings().getUserName()));
        return createButton;
    }

    protected ToolbarButton createDeletButton() {
        deleteButton = new ToolbarButton(getDeleteClsLabel());
        deleteButton.setCls("toolbar-button");
        deleteButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                onDeleteCls();
            }
        });
        deleteButton.setDisabled(!project.hasWritePermission(GlobalSettings.getGlobalSettings().getUserName()));
        return deleteButton;
    }

    protected ToolbarMenuButton createWatchButton() {
        Menu menu = new Menu();

         final CheckItemListenerAdapter listener = new CheckItemListenerAdapter() {
             public void onCheckChange(CheckItem item, boolean checked) {
                 watchButton.setText(item.getText());
             }
         };
         menu.setShadow(true);
         menu.setMinWidth(10);

         CheckItem watchItem = new CheckItem();
         watchItem.setText(getWatchClsLabel());
         watchItem.setChecked(true);
        watchItem.setGroup("theme");
         watchItem.addListener(listener);
        watchItem.setCls("toolbar-button");
         menu.addItem(watchItem);

        CheckItem watchBranchItem = new CheckItem();
        watchBranchItem.setText(getWatchBranchClsLabel());
        watchBranchItem.setGroup("theme");
        watchBranchItem.setCls("toolbar-button");
        watchBranchItem.addListener(listener);
         menu.addItem(watchBranchItem);


//                         new SplitButton();
        watchButton = new ToolbarMenuButton(getWatchClsLabel());
        watchButton.setMenu(menu);

        watchButton.setCls("toolbar-button");
        watchButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                if (button.getText().equals(getWatchClsLabel())){
                    onWatchCls();
                } else if (button.getText().equals(getWatchBranchClsLabel())){
                    onWatchBranchCls();
                }
            }
        });
        watchButton.setDisabled(!GlobalSettings.getGlobalSettings().isLoggedIn());
        return watchButton;
    }

    protected Component createSearchField() {
        final TextField searchField = new TextField("Search: ", "search");
        searchField.setAutoWidth(true);
        searchField.setEmptyText("Type search string");
        searchField.addListener(new TextFieldListenerAdapter() {
            @Override
            public void onSpecialKey(Field field, EventObject e) {
                if (e.getKey() == EventObject.ENTER) {
                    SearchUtil su = new SearchUtil(project, ClassTreePortlet.this);
                    //su.setBusyComponent(searchField);  //this does not seem to work
                    su.setBusyComponent(getTopToolbar());
                    su.setSearchedValueType(ValueType.Cls);
                    su.search(searchField.getText());
                }
            }
        });
        return searchField;
    }

    protected void addDragAndDropSupport() {
        treePanel.addListener(new TreePanelListenerAdapter() {
            @Override
            public boolean doBeforeNodeDrop(TreePanel treePanel, final TreeNode target, DragData dragData, String point,
                                            DragDrop source, final TreeNode dropNode, DropNodeCallback dropNodeCallback) {
                if (project.hasWritePermission()) {
                    boolean success = Window.confirm("Are you sure you want to move " + getNodeBrowserText(dropNode) +
                            " from parent " + getNodeBrowserText(dropNode.getParentNode()) + " to parent " + getNodeBrowserText(target) + " ?");
                    if (success) {
                        moveClass(getNodeClsName(dropNode), getNodeClsName(dropNode.getParentNode()), getNodeClsName(target));
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        });
    }

    protected void onSubclassAdded(EntityData entity, Collection<EntityData> subclasses, boolean selectNewNode) {
        if (subclasses == null || subclasses.size() == 0) {
            return;
        }

        EntityData subclassEntity = ((List<EntityData>) subclasses).get(0); //there is always just one
        TreeNode parentNode = findTreeNode(entity.getName());

        if (parentNode == null) {
            return; // nothing to be done
        }

        TreeNode subclassNode = findTreeNode(subclassEntity.getName());
        if (subclassNode == null) {
            subclassNode = createTreeNode(subclassEntity);
            parentNode.appendChild(subclassNode);
            getSubclasses(subclassEntity.getName(), parentNode);
        } else { // tricky if it already exists
            if (!hasChild(parentNode, subclassEntity.getName())) { //multiple parents
                subclassNode = createTreeNode(subclassEntity);
                if (subclassEntity instanceof SubclassEntityData) {
                    int childrenCount = ((SubclassEntityData) subclassEntity).getSubclassCount();
                    if (childrenCount > 0) {
                        subclassNode.setExpandable(true);
                    }
                }
                parentNode.appendChild(subclassNode);
            }
        }

    }

    protected TreeNode findTreeNode(String id) {
        TreeNode root = treePanel.getRootNode();
        TreeNode node = findTreeNode(root, id, new ArrayList<TreeNode>());
        return node;
    }

    protected TreeNode findTreeNode(TreeNode node, String id, ArrayList<TreeNode> visited) {
        if (getNodeClsName(node).equals(id)) {
            return node;
        } else {
            visited.add(node);
            Node[] children = node.getChildNodes();
            for (Node element2 : children) {
                TreeNode n = findTreeNode((TreeNode) element2, id, visited);
                if (n != null) {
                    return n;
                }
            }
            return null;
        }
    }

    protected void onSubclassRemoved(EntityData entity, Collection<EntityData> subclasses) {
        if (subclasses == null || subclasses.size() == 0) {
            return;
        }

        EntityData subclass = ((List<EntityData>) subclasses).get(0);
        TreeNode parentNode = findTreeNode(entity.getName());

        if (parentNode == null) {
            return;
        }

        TreeNode subclassNode = findTreeNode(parentNode, subclass.getName(), new ArrayList<TreeNode>());
        if (subclassNode == null) {
            return;
        }

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
        for (EntityData entityData : superEntities) {
            EntityData superEntity = entityData;
            TreeNode parentNode = findTreeNode(superEntity.getName());
            if (parentNode != null) {
                insertNodeInTree(parentNode, entity);
            }
        }
    }

    protected void onClassRename(EntityData entity, String oldName) {
        TreeNode oldNode = findTreeNode(oldName);
        if (oldNode == null) {
            return;
        }
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
        EntityData currentSelection = getSingleSelection();
        if (currentSelection != null) {
            superClsName = currentSelection.getName();
        }

        OntologyServiceManager.getInstance().createCls(project.getProjectName(), className, superClsName,
                GlobalSettings.getGlobalSettings().getUserName(), getCreateClsDescription() + " " + className,
                getCreateClassAsyncHandler(superClsName, className));
    }

    protected AbstractAsyncHandler<EntityData> getCreateClassAsyncHandler(String superClsName, String className) {
        return new CreateClassHandler(superClsName, className);
    }

    protected void onDeleteCls() {
        EntityData currentSelection = getSingleSelection();
        if (currentSelection == null) {
            Window.alert("Please select first a class to delete.");
            return;
        }

        final String clsName = currentSelection.getName();

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
        if (className == null) {
            return;
        }

        OntologyServiceManager.getInstance().deleteEntity(project.getProjectName(), className,
                GlobalSettings.getGlobalSettings().getUserName(), getDeleteClsDescription() + " " + className,
                new DeleteClassHandler());

        refreshFromServer(500);
    }

    protected void onWatchCls() {
        EntityData currentSelection = getSingleSelection();
        TreeNode selectedNode = getSingleSelectedTreeNode();

        if (currentSelection == null || selectedNode == null || (selectedNode != null && isWatched(selectedNode) )) {
            return;
        }
        ChAOServiceManager.getInstance().addWatchedEntity(project.getProjectName(),
                GlobalSettings.getGlobalSettings().getUserName(), currentSelection.getName(),
                new AddWatchedCls(selectedNode));
    }

    protected void onWatchBranchCls() {
        EntityData currentSelection = getSingleSelection();
        TreeNode selectedNode = getSingleSelectedTreeNode();

        if (currentSelection == null || selectedNode == null || (selectedNode != null && isWatched(selectedNode))) {
            return;
        }
        ChAOServiceManager.getInstance().addWatchedBranchEntity(project.getProjectName(),
                GlobalSettings.getGlobalSettings().getUserName(), currentSelection.getName(),
                new AddWatchedCls(selectedNode));
    }

    protected void renameClass(String oldName, String newName) {
        GWT.log("Should rename class from " + oldName + " to " + newName, null);
        if (oldName.equals(newName) || newName == null || newName.length() == 0) {
            return;
        }

        OntologyServiceManager.getInstance().renameEntity(project.getProjectName(), oldName, newName,
                GlobalSettings.getGlobalSettings().getUserName(),
                getRenameClsDescription() + " " + "Old name: " + oldName + ", New name: " + newName,
                new RenameClassHandler());
    }

    public TreePanel getTreePanel() {
        return treePanel;
    }

    @Override
    protected void afterRender() {
        getRootCls();
    }

    public void setTreeNodeIcon(TreeNode node) {
        node.setIconCls("protege-class-icon");
    }

    public void getSubclasses(final String clsName, TreeNode parentNode) {
        if (isSubclassesLoaded(parentNode)) {
            return;
        }
        if (hierarchyProperty == null) {
            OntologyServiceManager.getInstance().getSubclasses(project.getProjectName(), clsName,
                    new GetSubclassesOfClassHandler(clsName, parentNode, null));
        } else {
            List<String> subjects = new ArrayList<String>();
            subjects.add(clsName);
            List<String> props = new ArrayList<String>();
            props.add(hierarchyProperty);
            OntologyServiceManager.getInstance().getEntityTriples(project.getProjectName(), subjects, props,
                    new GetPropertyHierarchySubclassesOfClassHandler(clsName, parentNode));
        }
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

    public void setWatched(TreeNode node, boolean watched) {
        node.setAttribute("watched", watched ? "true" : "false");
    }

    public void setBranchWatched(TreeNode node, boolean watched) {
        node.setAttribute("branchWatched", watched ? "true" : "false");
    }

    public boolean isWatched(TreeNode node) {
        String val = node.getAttribute("watched");
        return val != null && val.equals("true");
    }


    public void getRootCls() {
        String rootClsName = getRootClsName();
        if (rootClsName != null) {
            OntologyServiceManager.getInstance().getEntity(project.getProjectName(), rootClsName,
                    new GetRootClassHandler());
        } else {
            OntologyServiceManager.getInstance().getRootEntity(project.getProjectName(), new GetRootClassHandler());
        }
    }


    protected String getRootClsName() {
        PortletConfiguration portletConfiguration = getPortletConfiguration();
        if (portletConfiguration == null) {
            return topClass;
        }
        Map<String, Object> props = portletConfiguration.getProperties();
        if (props == null) {
            return topClass;
        }
        // TODO: move from here
        String title = (String) props.get("label");
        setTitle(title == null ? "Class Tree" : title);
        hierarchyProperty = (String) props.get("hierarchyProperty");

        //cache it so that we can set it
        if (topClass == null) {
            topClass = (String) props.get(TOP_CLASS_PROP);
        }
        return topClass;
    }


    /**
     * To take effect, it has to be called before {@link #afterRender()}.
     *
     * @param topClass
     */
    public void setTopClass(String topClass) {
        this.topClass = topClass;
    }

    protected void moveClass(String clsName, String oldParentName, String newParentName) {
        if (oldParentName.equals(newParentName)) {
            return;
        }
        OntologyServiceManager.getInstance().moveCls(
                project.getProjectName(),
                clsName,
                oldParentName,
                newParentName,
                GlobalSettings.getGlobalSettings().getUserName(),
                getMoveClsDescription() + " " + clsName + ". Old parent: " + oldParentName + ", new parent: "
                        + newParentName, new MoveClassHandler(clsName, oldParentName, newParentName));
    }

    public void getPathToRoot(EntityData entity) {
        OntologyServiceManager.getInstance().getPathToRoot(project.getProjectName(), entity.getName(),
                new GetPathToRootHandler());
    }

    public List<EntityData> getSelection() {
        List<EntityData> selections = new ArrayList<EntityData>();
        TreeSelectionModel selectionModel = treePanel.getSelectionModel();
        if (selectionModel instanceof MultiSelectionModel) {
            TreeNode[] selection = ((MultiSelectionModel) selectionModel).getSelectedNodes();
            for (TreeNode node : selection) {
                EntityData ed = (EntityData) node.getUserObject();
                selections.add(ed);
            }
        } else if (selectionModel instanceof DefaultSelectionModel) {
            TreeNode node = ((DefaultSelectionModel) selectionModel).getSelectedNode();
            if (node != null) {
                selections.add((EntityData) node.getUserObject());
            }
        }
        return selections;
    }


    public List<TreeNode> getSelectedTreeNodes() {
        List<TreeNode> selections = new ArrayList<TreeNode>();
        TreeSelectionModel selectionModel = treePanel.getSelectionModel();
        if (selectionModel instanceof MultiSelectionModel) {
            TreeNode[] selection = ((MultiSelectionModel) selectionModel).getSelectedNodes();
            for (TreeNode node : selection) {
                selections.add(node);
            }
        } else if (selectionModel instanceof DefaultSelectionModel) {
            TreeNode node = ((DefaultSelectionModel) selectionModel).getSelectedNode();
            selections.add(node);
        }
        return selections;
    }


    public TreeNode getSingleSelectedTreeNode() {
        return UIUtil.getFirstItem(getSelectedTreeNodes());
    }

    public EntityData getSingleSelection() {
        return UIUtil.getFirstItem(getSelection());
    }

    @Override
    public void setSelection(Collection<EntityData> selection) {
        if (selection == null || selection.isEmpty()) {
            return;
        } // TODO: how to clear selection?
        GWT.log("Select in class tree: " + selection, null);
        getPathToRoot(selection.iterator().next()); // FIXME: just take first element in selection for now
    }

    public void selectPathInTree(List<EntityData> path) {
        int topIndex = -1;
        if (topClass != null) { //adjust path
            for (int i = 0; i < path.size() && topIndex == -1; i++) {
                if (path.get(i).getName().equals(topClass)) {
                    topIndex = i;
                }
            }
            if (topIndex != -1) {
                path = path.subList(topIndex, path.size());
            }
        }

        selectPathInTree(path, treePanel.getRootNode(), 1);
    }

    private void selectPathInTree(List<EntityData> path, TreeNode parentNode, int index) {
        for (int i = index; i < path.size(); i++) {
            String clsName = path.get(i).getName();
            TreeNode node = findTreeNode(clsName);
            if (node == null) {
                EntityData parentEntityData = (EntityData) parentNode.getUserObject();
                OntologyServiceManager.getInstance().getSubclasses(project.getProjectName(),
                        parentEntityData.getName(), new SelectInTreeHandler(parentNode, path, i));
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
        TreeNode node = new TreeNode(UIUtil.getDisplayText(entityData));
        node.setHref(null);
        node.setUserObject(entityData);
        node.setAllowDrag(true);
        node.setAllowDrop(true);
        setTreeNodeIcon(node);

        //vertical-align:super;

        // TODO: add a css for this
        String text = entityData.getBrowserText();
        int localAnnotationsCount = entityData.getLocalAnnotationsCount();
        if (localAnnotationsCount > 0) {
            String idLocalAnnotationImg = node.getId() + SUFFIX_ID_LOCAL_ANNOTATION_IMG;
            String idLocalAnnotationCnt = node.getId() + SUFFIX_ID_LOCAL_ANNOTATION_COUNT;

            text = text + "<img id=\"" + idLocalAnnotationImg + "\" src=\"images/comment.gif\" title=\""
                    + UIUtil.getNiceNoteCountText(localAnnotationsCount) + " on this category. \nClick on the icon to see and edit the notes\" />"
                    + "<span id=\"" + idLocalAnnotationCnt + "\" style=\"font-size:95%;color:#15428B;font-weight:bold;\">"
                    + localAnnotationsCount + "</span>";

            node.addListener(new TreeNodeListenerAdapter() {
                @Override
                public boolean doBeforeClick(Node node, EventObject e) {
                    Element target = e.getTarget();
                    if (target != null) {
                        String tgtId = target.getId();
                        if (tgtId.endsWith(SUFFIX_ID_LOCAL_ANNOTATION_IMG)
                                || tgtId.endsWith(SUFFIX_ID_LOCAL_ANNOTATION_COUNT)) {
                            showClassNotes(node);
                        }
                    }
                    return true;
                }
            });
        }
        int childrenAnnotationsCount = entityData.getChildrenAnnotationsCount();
        if (childrenAnnotationsCount > 0) {
            text = text + " <img src=\"images/comment_small.gif\" title=\"" + UIUtil.getNiceNoteCountText(childrenAnnotationsCount)
                    + " on the children of this category\" />" + "<span style=\"font-size:90%;color:#999999;\">"
                    + childrenAnnotationsCount + "</span>";
        }
        node.setText(text);
        //node.setIcon(null);

        return node;
    }

    private void showClassNotes(Node node) {
        EntityData entity = (EntityData) node.getUserObject();

        final com.gwtext.client.widgets.Window window = new com.gwtext.client.widgets.Window();
        window.setTitle("View/Edit Notes on " + entity.getBrowserText());
        window.setWidth(600);
        window.setHeight(480);
        window.setMinWidth(300);
        window.setMinHeight(350);
        window.setLayout(new FitLayout());
        window.setPaddings(5);
        window.setButtonAlign(Position.CENTER);

        //window.setCloseAction(Window.HIDE);
        window.setPlain(true);

        NoteInputPanel nip = new NoteInputPanel(getProject(), "Please enter your note:", true,
                entity, window);

        window.add(nip);
        window.show();
    }

    private boolean hasChild(TreeNode parentNode, String childId) {
        return getDirectChild(parentNode, childId) != null;
    }

    protected void createRoot(EntityData rootEnitity) {
        if (rootEnitity == null) {
            GWT.log("Root entity is null", null);
            rootEnitity = new EntityData("Root", "Root node is not defined");
        }
        remove(PLACE_HOLDER_PANEL);
        // ClassTreePortlet.this.doLayout();

        treePanel = createTreePanel();
        TreeNode root = createTreeNode(rootEnitity);
        treePanel.setRootNode(root);
        add(treePanel);
        createSelectionListener();

        try {
            // TODO: could not figure out why it throws exceptions sometimes, not elegant but it works
            doLayout();
        } catch (Exception e) {
            GWT.log("Error at doLayout in class tree", e);
        }

        root.select();
        getSubclasses(rootEnitity.getName(), root);
        root.expand(); // TODO: does not seem to work always
    }

    protected TreeNode getDirectChild(TreeNode parentNode, String childId) {
        Node[] children = parentNode.getChildNodes();
        for (Node child : children) {
            if (getNodeClsName(child).equals(childId)) {
                return (TreeNode) child;
            }
        }
        return null;
    }

    @Override
    protected void onRefresh() {
        Collection<EntityData> initialSelection = getSelection();
        // TODO: not ideal
        TreeNode root = treePanel.getRootNode();

        Node[] children = root.getChildNodes();
        if (children != null) {
            for (Node element2 : children) {
                TreeNode child = (TreeNode) element2;
                root.removeChild(child);
                setSubclassesLoaded(child, false);
            }
        }
        EntityData rootEntity = (EntityData) root.getUserObject();

        setSubclassesLoaded(root, false);
        root.select();
        getSubclasses(rootEntity.getName(), root);
        doLayout();
        root.expand();

        if (initialSelection != null && initialSelection.size() > 0) {
            setSelection(initialSelection);
        }
    }

    public void updateButtonStates() {
        if (project.hasWritePermission(GlobalSettings.getGlobalSettings().getUserName())) {
            if (createButton != null) {
                createButton.enable();
            }
            if (deleteButton != null) {
                deleteButton.enable();
            }
        } else {
            if (createButton != null) {
                createButton.disable();
            }
            if (deleteButton != null) {
                deleteButton.disable();
            }
        }

        if (watchButton != null) {
            watchButton.setDisabled(!GlobalSettings.getGlobalSettings().isLoggedIn());
        }
    }

    public String getNodeClsName(Node node) {
        EntityData data = (EntityData) node.getUserObject();
        return data.getName();
    }

    public String getNodeBrowserText(Node node) {
        EntityData data = (EntityData) node.getUserObject();
        return data.getBrowserText();
    }

    @Override
    public void onPermissionsChanged(Collection<String> permissions) {
        updateButtonStates();
    }


    /*
     * ************ Remote procedure calls *****************
     */

    class GetRootClassHandler extends AbstractAsyncHandler<EntityData> {

        @Override
        public void handleFailure(Throwable caught) {
            getEl().unmask();
            GWT.log("RPC error at getting classes root ", caught);
        }

        @Override
        public void handleSuccess(EntityData rootEnitity) {
            getEl().unmask();
            createRoot(rootEnitity);
        }
    }

    class GetSubclassesOfClassHandler extends AbstractAsyncHandler<List<SubclassEntityData>> {

        private String clsName;
        private TreeNode parentNode;
        private AsyncCallback<Object> endCallback;

        public GetSubclassesOfClassHandler(String className, TreeNode parentNode, AsyncCallback<Object> endCallback) {
            super();
            this.clsName = className;
            this.parentNode = parentNode;
            this.endCallback = endCallback;
        }

        @Override
        public void handleFailure(Throwable caught) {
            getEl().unmask();
            GWT.log("RPC error at getting subclasses of " + clsName, caught);
            if (endCallback != null) {
                endCallback.onFailure(caught);
            }
        }

        @Override
        public void handleSuccess(List<SubclassEntityData> children) {
            getEl().unmask();

            for (SubclassEntityData subclassEntityData : children) {
                SubclassEntityData childData = subclassEntityData;
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
                endCallback.onSuccess(children);
            }
        }
    }

    class GetPropertyHierarchySubclassesOfClassHandler extends AbstractAsyncHandler<List<Triple>> {

        private String clsName;
        private TreeNode parentNode;

        public GetPropertyHierarchySubclassesOfClassHandler(String className, TreeNode parentNode) {
            super();
            this.clsName = className;
            this.parentNode = parentNode;
        }

        @Override
        public void handleFailure(Throwable caught) {
            getEl().unmask();
            GWT.log("RPC error at getting subclasses of " + clsName, caught);
        }

        @Override
        public void handleSuccess(List<Triple> childTriples) {
            getEl().unmask();
            if (childTriples != null) {
                for (Triple childTriple : childTriples) {
                    EntityData childData = childTriple.getValue();
                    if (!hasChild(parentNode, childData.getName())) {
                        TreeNode childNode = createTreeNode(childData);
                        childNode.setExpandable(true); // TODO - we need to get the
                        // own slot values count
                        parentNode.appendChild(childNode);
                    }
                }
            }

            setSubclassesLoaded(parentNode, true);
        }
    }

    class CreateClassHandler extends AbstractAsyncHandler<EntityData> {

        private String superClsName;
        private String className;

        public CreateClassHandler(String superClsName, String className) {
            this.superClsName = superClsName;
            this.className = className;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at creating class", caught);
            MessageBox.alert("There were errors at creating class.<br>" + " Message: " + caught.getMessage());
        }

        @Override
        public void handleSuccess(EntityData entityData) {
            if (entityData != null) {
                GWT.log("Created successfully class " + entityData.getName(), null);

                project.forceGetEvents();

                Timer timer = new Timer() {
                    @Override
                    public void run() {
                        TreeNode parentNode = findTreeNode(superClsName);
                        if (parentNode != null) {
                            parentNode.expand();
                        }

                        TreeNode newNode = getDirectChild(parentNode, className);
                        if (newNode != null) {
                            newNode.select();
                        }
                    }
                };
                timer.schedule(1000);
            } else {
                GWT.log("Problem at creating class", null);
                MessageBox.alert("Class creation failed.");
            }
        }
    }

    class DeleteClassHandler extends AbstractAsyncHandler<Void> {

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at deleting class", caught);
            MessageBox.alert("There were errors at deleting class.<br>" + " Message: " + caught.getMessage());
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

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at moving class", caught);
            MessageBox.alert("There were errors at moving class.<br>" + " Message: " + caught.getMessage());
            // TODO: refresh oldParent and newParent
        }

        @Override
        public void handleSuccess(Void result) {
            GWT.log("Moved successfully class " + clsName, null);
        }
    }

    class RenameClassHandler extends AbstractAsyncHandler<EntityData> {
        @Override
        public void handleFailure(Throwable caught) {
            MessageBox.alert("Class rename failed.<br>" + "Message: " + caught.getMessage());
        }

        @Override
        public void handleSuccess(EntityData result) {
            GWT.log("Rename succeded!", null);
        }
    }

    // TODO: not used - will be used for the selection in tree

    class GetPathToRootHandler extends AbstractAsyncHandler<List<EntityData>> {
        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at finding path to root", caught);
        }

        @Override
        public void handleSuccess(List<EntityData> result) {
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

    class SelectInTreeHandler extends AbstractAsyncHandler<List<SubclassEntityData>> {

        private TreeNode parentNode;
        private List<EntityData> path;
        private int index;

        public SelectInTreeHandler(TreeNode parentNode, List<EntityData> path, int index) {
            super();
            this.parentNode = parentNode;
            this.index = index;
            this.path = path;
        }

        @Override
        public void handleFailure(Throwable caught) {
            getEl().unmask();
            GWT.log("RPC error at select in tree for " + parentNode.getUserObject(), caught);
        }

        @Override
        public void handleSuccess(List<SubclassEntityData> children) {
            getEl().unmask();

            TreeNode pathTreeNode = null;

            EntityData nextParent = path.get(index);

            for (SubclassEntityData subclassEntityData : children) {
                SubclassEntityData childData = subclassEntityData;
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
                    EntityData entityData = (EntityData) pathTreeNode.getUserObject();
                    setEntity(entityData);
//                    notifySelectionListeners(new SelectionEvent(ClassTreePortlet.this));
                } else {
                    selectPathInTree(path, pathTreeNode, index + 1);
                }
            } else {
                GWT.log("Error at select in tree: could not find child " + nextParent + " of "
                        + parentNode.getUserObject(), null);
            }
        }
    }

    class AddWatchedCls extends AbstractAsyncHandler<EntityData> {

        private TreeNode node;

        public AddWatchedCls(TreeNode node) {
            this.node = node;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Error at add watched entity", caught);
            MessageBox.alert("Error", "There was an error at adding the new watched entity. Pleas try again later.");
        }

        @Override
        public void handleSuccess(EntityData result) {
            setWatched(node, true);
            node.setText(node.getText() + " " + "(W)");
        }

    }

}
