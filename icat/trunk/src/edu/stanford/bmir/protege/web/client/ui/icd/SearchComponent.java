package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.Label;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.event.CheckboxListenerAdapter;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.HorizontalLayout;
import com.gwtext.client.widgets.layout.VerticalLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.search.SearchGridPanel;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;

public class SearchComponent extends Panel {
	
	private Project project;

	private TabPanel tabPanel;
	private TextField searchField;
	private Checkbox searchSelectedSubtreeCheckBox;
	
	private Selectable selectable;
	private String subtreeSearchFilter;
	
	private String publicIdSelection;

	private String iNo;
	
	private AsyncCallback<EntityData> onSelectCallback;
	

	public SearchComponent(Project project, String iNo, Selectable selectable) {
		this.project = project;
		this.iNo = iNo;
		this.selectable = selectable;
		
		initUI();
	}

	private void initUI() {
		setAutoScroll(true);
		
		setLayout(new VerticalLayout(7));
		setBorder(false);
		
		searchField = createSearchField();
		
		searchSelectedSubtreeCheckBox = createSearchCheckBox();
		
		tabPanel = createTabPanel();
        tabPanel.add(createIcdApiPanel());
        tabPanel.add(createiCATPanel());
        
        Panel searchFieldPanel = new Panel();  
        searchFieldPanel.setBorder(false);  
        searchFieldPanel.setPaddings(5);  
  
        searchFieldPanel.setLayout(new HorizontalLayout(1)); 
        searchFieldPanel.add(new Label("Search: "));
        searchFieldPanel.add(searchField);
        
        add(searchFieldPanel);
        add(searchSelectedSubtreeCheckBox);
        add(tabPanel);
	}

	private TabPanel createTabPanel() {
		TabPanel tabPanel = new TabPanel();
		tabPanel.setResizeTabs(true);  
        tabPanel.setTabWidth(135);  
        tabPanel.setActiveTab(0);
        tabPanel.setWidth(560); //not ideal
        tabPanel.setBodyBorder(false);
        tabPanel.setBorder(false);
    
        return tabPanel;
	}
	
	private TextField createSearchField() {
		TextField searchField = new TextField("Search:");
		searchField.setFieldLabel("Search");
		searchField.setCls("ctw-input");
		searchField.getElement().setAttribute("data-ctw-ino", iNo);
		searchField.setWidth(500);
		searchField.setValidateOnBlur(false);
		return searchField;
	}
	
	private Checkbox createSearchCheckBox() {
		Checkbox checkbox = new Checkbox("Search only in selected subtree (works only in Browser search)");
		checkbox.setCls("subtreeCheckbox"); 
		
		checkbox.addListener(new CheckboxListenerAdapter() {
			@Override
			public void onCheck(Checkbox field, boolean checked) {
				String query = searchField.getText();

				setSearchSubtreeFilter();
				
				triggerSearch(iNo, query);
			}
		});
		return checkbox;
	}
	
	
	private void setSearchSubtreeFilter() {
		IcdApiSearchManager icdSearchManager = IcdApiSearchManager.getInstance();
		
		if (searchOnlyInSelectedSubtree() == true && selectable != null) {
			Collection<EntityData> selection = (List<EntityData>)selectable.getSelection();
			
			if (selection != null && selection.size() > 0) {
				String selectedPublicId = selection.iterator().next().getProperty(ICDClassTreePortlet.PUBLIC_ID_PROP);
				if (selectedPublicId != null) {
					icdSearchManager.setSubtreeFilter(this, selectedPublicId);
					return;
				}
			}
		} 
		
		icdSearchManager.setSubtreeFilter(this, subtreeSearchFilter);
	}
	
	
	private native void triggerSearch(String iNo, String query) /*-{
		$wnd.ECT.Handler.search(iNo, query);
	}-*/;
	
	private Panel createIcdApiPanel() {
		Panel icdApiPanel = new Panel();
		icdApiPanel.setBorder(false);
		icdApiPanel.setTitle("Browser search");
		icdApiPanel.setAutoScroll(true);
		icdApiPanel.setCls("ctw-window");
		icdApiPanel.getElement().setAttribute("data-ctw-ino", iNo);
		return icdApiPanel;
	}
	
	private Panel createiCATPanel() {
		Panel iCatPanel = new Panel();
		iCatPanel.setTitle("iCAT search");
		iCatPanel.setAutoScroll(true);
		iCatPanel.setBorder(false);
		
		SearchGridPanel searchGridPanel = new SearchGridPanel() {
			@Override
			protected void addSearchToolbar() {
				//does nothing
			}
			
			@Override
			protected void onEntityClick() {
				if (selectable != null) {
					EntityData selection = getSelection();
					GWT.log("Selecting in tree: " + selection);
					
					Collection<EntityData> entities = new ArrayList<EntityData>();
					entities.add(selection);
					
					selectable.setSelection(entities);
					
					invokeSelectCallback(selection);
				}
			}
		};
		
		searchGridPanel.setAutoScroll(true);
		searchGridPanel.setStripeRows(true);
		searchGridPanel.setProjectName(project.getProjectName());
		
		searchGridPanel.getProxy().setValueType(ValueType.Cls);
		searchGridPanel.setSearchField(searchField);
		//searchGridPanel.setBusyComponent(searchGridPanel);
		
		searchGridPanel.setHeight(330);
		
		iCatPanel.add(searchGridPanel, new AnchorLayoutData("100% 100%"));
		
		return iCatPanel;
	}
	
	void setSelection(String publicIdSelection) {
		this.publicIdSelection = publicIdSelection;
		selectClsWithPublicId(publicIdSelection);
	}

	public String getiNo() {
		return iNo;
	}

	public String getPublicIdSelection() {
		return publicIdSelection;
	}
	
	public TextField getSearchField() {
		return searchField;
	}
	
	public boolean searchOnlyInSelectedSubtree() {
		return searchSelectedSubtreeCheckBox.getValue();
	}
	
	public void setSubtreeSearchFilter(String filter) {
		this.subtreeSearchFilter = filter;
		
		IcdApiSearchManager.getInstance().setSubtreeFilter(this, filter);
	}
	
    private void selectClsWithPublicId(String publicId) {
    	ICDServiceManager.getInstance().getFrameForPublicId(project.getProjectName(), publicId, 
    			new AsyncCallback<EntityData>() {
					@Override
					public void onFailure(Throwable caught) {
						GWT.log("Could not get frame for public id: " + publicId, caught);
					}

					@Override
					public void onSuccess(EntityData frame) {
						GWT.log("Selecting in cls tree: " + frame + " with public id: " + publicId);
						Collection<EntityData> selection = new ArrayList<EntityData>();
						selection.add(frame);
						
						if (selectable != null) {
							selectable.setSelection(selection);
						}
						
						invokeSelectCallback(frame);
					}
		});
	}
	
    public void setOnSelectCallback(AsyncCallback<EntityData> callback) {
    	this.onSelectCallback = callback;
    }
    
    private void invokeSelectCallback(EntityData selection) {
    	if (onSelectCallback == null) {
    		return;
    	}
    	
    	onSelectCallback.onSuccess(selection);
    }
    
}
