package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.List;

import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Node;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Toolbar;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.WindowListenerAdapter;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.layout.FitLayout;
import com.gwtext.client.widgets.menu.BaseItem;
import com.gwtext.client.widgets.menu.Menu;
import com.gwtext.client.widgets.menu.MenuItem;
import com.gwtext.client.widgets.menu.event.BaseItemListenerAdapter;
import com.gwtext.client.widgets.tree.TreeNode;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.HierarchyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.ontology.hierarchy.CreateClassPanel;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class ICDClassTreePortlet extends ClassTreePortlet {
	
    public static final String CREATE_ICD_SPECIFIC_ENTITES_PROP = "create_icd_specific_entities";
    public static final boolean CREATE_ICD_SPECIFIC_ENTITES_DEFAULT = true;
    
    public static final String PUBLIC_ID_PROP ="publicId";
    
    public static final String ICD_SEARCH_SUBTREE_FILTER_PROP = "icd_search_subtree_filter";

    private ComboBox searchCb;
    
    private Window searchWindow;

    public ICDClassTreePortlet(Project project) {
        super(project);
    }

    public ICDClassTreePortlet(final Project project, final boolean showToolbar, final boolean showTitle,
            final boolean showTools, final boolean allowsMultiSelection, final String topClass) {
        super(project, showToolbar, showTitle, showTools, allowsMultiSelection, topClass);
    }

    @Override
    public void initialize() {
        super.initialize();
        
        initSearchWindow();
        addSearchInWindowButton();
    }


	protected String getICDSearchFilter() {
	        return UIUtil.getStringConfigurationProperty(getPortletConfiguration(), ICD_SEARCH_SUBTREE_FILTER_PROP, null);
	}

	@Override
    protected void createContextMenu(final Node node, EventObject e) {
		getTreePanel().getSelectionModel().select((TreeNode) node);
        Menu contextMenu = new Menu();
        contextMenu.setWidth("200px");
        EntityData entity = (EntityData) node.getUserObject();

        addMenuItemShowPublicId(entity, contextMenu);
        addMenuItemShowInternalId(entity, contextMenu);
        addMenuItemShowDirectLink(entity, contextMenu);
        addMenuItemShowExternalLink(entity, contextMenu);

        contextMenu.showAt(e.getXY()[0]+5, e.getXY()[1]+5);
	}


	protected void addMenuItemShowExternalLink(final EntityData entity, Menu contextMenu) {
		MenuItem menuExternalLink = new MenuItem();
        //TODO load text from configuration
        menuExternalLink.setText("Link to ICD-11 Public Browser");
        menuExternalLink.setCls("hyperlink");
        menuExternalLink.addListener(new BaseItemListenerAdapter() {
            @Override
            public void onClick(BaseItem item, EventObject event) {
                openExternalLink(entity);
            }
        });
        contextMenu.addItem(menuExternalLink);
	}

    private void openExternalLink(EntityData entity) {
        String publicId = getPublicId(entity);
        //TODO get URL base from configuration
        String baseUrl = "https://icd.who.int/dev11/f/en#/";
        String url = baseUrl + (publicId != null && publicId.length() > 0 ? URL.encodePathSegment(publicId) : "");
        com.google.gwt.user.client.Window.open(url,  "_blank", "");
    }

    protected void addMenuItemShowPublicId(final EntityData entity, Menu contextMenu) {
        MenuItem menuShowPublicID = new MenuItem();
        menuShowPublicID.setText("Show Public ID");
        menuShowPublicID.addListener(new BaseItemListenerAdapter() {
            @Override
            public void onClick(BaseItem item, EventObject event) {
                showPublicID(entity);
            }
        });
        contextMenu.addItem(menuShowPublicID);
    }


    private void showPublicID(EntityData entity) {
        String classDisplayName = entity.getBrowserText();
        String publicID = getPublicId(entity);
        String message = "The public ID of the class ";
        message += "<BR>&nbsp;&nbsp;&nbsp;&nbsp;<I>" + classDisplayName + "</I>";
        message += "<BR>is:";
        message += "<BR>&nbsp;&nbsp;&nbsp;&nbsp;<B>" + (publicID == null || publicID.length() == 0 ? "--- not set ---" : publicID) + "</B>";
        MessageBox.alert("Public ID", message);
    }

	private String getPublicId(EntityData entity) {
		return entity.getProperty(PUBLIC_ID_PROP);
	}
	
    @Override
    protected void onCreateCls() {
        final com.gwtext.client.widgets.Window selectWindow = new com.gwtext.client.widgets.Window();

        selectWindow.setBorder(false);
        selectWindow.setWidth(500);
        selectWindow.setBodyBorder(false);

        final CreateClassPanel createClassPanel = new CreateClassPanel(project, getCreateICDSpecificEntities(), null, getRootClsName(), ICDClassTreePortlet.this);

        createClassPanel.setAsyncCallback(new AsyncCallback<EntityData>() {

            public void onFailure(Throwable caught) {
                selectWindow.hide();
                MessageBox.alert("Error", "There was a problem at creating class. Please try again later.");
            }

            public void onSuccess(EntityData newClass) {
                selectWindow.hide();

                if (newClass == null) { //probably the user just pressed cancel
                    return;
                }

                MessageBox.alert("Success", "Created class successfully.");

                EntityData supercls = UIUtil.getFirstItem(createClassPanel.getParentsClses());

                if (supercls != null) {
                    TreeNode parentNode = findTreeNode(supercls.getName());
                    if (parentNode != null) {
                        parentNode.expand();

                        TreeNode newNode = getDirectChild(parentNode, newClass.getName());
                        if (newNode != null) {
                            newNode.select();
                        } else {
                            newNode = createTreeNode(newClass);
                            parentNode.appendChild(newNode);
                            newNode.select();
                        }
                    }
                }
            }
        });

        EntityData currentSelection = getSingleSelection();

        createClassPanel.setParentsClses(UIUtil.createCollection(currentSelection));
        selectWindow.add(createClassPanel);
        selectWindow.show();
        selectWindow.center();
    }

    @Override
    protected void onMoveClass(TreeNode target, TreeNode dropNode) {
    	EntityData cls = (EntityData) dropNode.getUserObject();
    	EntityData oldParent = (EntityData) dropNode.getParentNode().getUserObject();
    	EntityData newParent = (EntityData) target.getUserObject();
    	
    	if (oldParent.equals(newParent)) {
            return;
        }
    	
    	 HierarchyServiceManager.getInstance().changeParent(
                 project.getProjectName(),
                 cls.getName(),
                 UIUtil.createCollection(newParent.getName()),
                 UIUtil.createCollection(oldParent.getName()),
                 GlobalSettings.getGlobalSettings().getUserName(),
                 UIUtil.getAppliedToTransactionString(getMoveClsOperationDescription(cls, oldParent, newParent), cls.getName()),
                 (String) null,
                 new MoveClassHandler(target, dropNode, (TreeNode) dropNode.getParentNode(),
                		 cls.getName(), oldParent.getName(), newParent.getName()));
    	
    }
    

    @Override
    protected void onReorderNode(TreeNode movedNode, TreeNode targetNode, boolean isBelow) {
        String parentNode = getNodeClsName(movedNode.getParentNode());
        ICDServiceManager.getInstance().reorderSiblings(getProject().getProjectName(),
                getNodeClsName(movedNode), getNodeClsName(targetNode), isBelow, parentNode,
                new AsyncCallback<Boolean>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        MessageBox.alert("Error", "There was an error at reordering the classes. Please try again later.");
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                       // MessageBox.alert("Success at reordering!");
                    }
                });
    }

    @Override
    protected ToolbarButton createDeleteButton() {
        return null;
    }

    private boolean getCreateICDSpecificEntities() {
        return UIUtil.getBooleanConfigurationProperty(getPortletConfiguration(), CREATE_ICD_SPECIFIC_ENTITES_PROP, CREATE_ICD_SPECIFIC_ENTITES_DEFAULT);
    }

    @Override
    protected Element createSearchField() {
    	return null;
    }


    protected void addSearchInWindowButton() {
        ToolbarButton searchInWindowButton = new ToolbarButton();
        searchInWindowButton.setCls("toolbar-button");
        searchInWindowButton.setIcon("images/magnifier.png");
        searchInWindowButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                onShowSearchWindow();
            }
        });
        Toolbar topToolbar = getTopToolbar();
        if (topToolbar != null) {
        	topToolbar.addButton(searchInWindowButton);
        }
    }

	protected void onShowSearchWindow() {
		searchWindow.setTitle(getCustomTitle() + " search results");
		searchWindow.show();
	}

	private void initSearchWindow() {
		searchWindow = new com.gwtext.client.widgets.Window();
    	
        searchWindow.setTitle("Search results");
        searchWindow.setWidth(600);
        searchWindow.setHeight(500);
        searchWindow.setLayout(new FitLayout());
        
        FormPanel panel = new FormPanel();
        panel.setAutoScroll(true);
        
        IcdApiSearchManager icdSearchManager = IcdApiSearchManager.getInstance();
        
		final SearchComponent searchComponent = icdSearchManager.createSearchComponent(getProject(), this);
        searchComponent.setSubtreeSearchFilter(getICDSearchFilter()); //does not work here,because portlet config not loaded yet
        searchComponent.setOnSelectCallback(new AsyncCallback<EntityData>() {
        	@Override
			public void onFailure(Throwable caught) {}
        	@Override
        	public void onSuccess(EntityData result) {
        		searchWindow.hide();
        	}
		});
		
        Button closeButton = new Button("Close", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                searchWindow.hide();
            }
        });

        panel.add(searchComponent);
        
        panel.addButton(closeButton);

        searchWindow.addListener(new WindowListenerAdapter() {
        	@Override
        	public void onShow(Component component) {
        		searchComponent.setSubtreeSearchFilter(getICDSearchFilter());
        		
        		icdSearchManager.bind(searchComponent);
        		searchComponent.getSearchField().focus(true, 100);
        	}
        });
        
        searchWindow.add(panel);
	}
    

	@Override
    public void setTreeNodeIcon(TreeNode node, EntityData entityData) {
    	if (entityData.isReleased() == true) {
    		node.setIconCls("released-icon");
    		return;
    	}
    	
        String status = entityData.getProperty("status");
        if (status == null) {
            node.setIconCls(null);
        } else if (status.equals(DisplayStatus.B.toString())) {
            node.setIconCls( "blue-display-status-icon");
        } else if (status.equals(DisplayStatus.Y.toString())) {
            node.setIconCls( "yellow-display-status-icon");
        } else if (status.equals(DisplayStatus.R.toString())) {
            node.setIconCls( "red-display-status-icon");
        }
    }

    @Override
    protected String createNodeText(EntityData entityData) {
        String browserText = super.createNodeText(entityData);
        
        if ("true".equals(entityData.getProperty("obsolete"))) {
           browserText = "<span style=\"font-style:italic; text-decoration: line-through;\">" + browserText + "</span>";
        }
        return browserText;
    }

    @Override
    public void setTreeNodeTooltip(TreeNode node, EntityData entityData) {
        String tooltip = UIUtil.getDisplayText(entityData);
        
        if ("true".equals(entityData.getProperty("obsolete"))) {
            tooltip = tooltip + " is obsolete.";
        } 
        
        if (entityData.isReleased() == true) {
        	tooltip = tooltip + " is released.";
        }
        
        node.setTooltip(tooltip);
    }

    @Override
    protected void invokeGetSubclassesRemoteCall(String parentClsName, AsyncCallback<List<SubclassEntityData>> callback) {
        ICDServiceManager.getInstance().getSubclasses(project.getProjectName(), parentClsName, callback);
    }

}
