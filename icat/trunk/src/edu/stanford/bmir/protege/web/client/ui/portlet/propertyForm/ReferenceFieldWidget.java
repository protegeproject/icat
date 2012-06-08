package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Hyperlink;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.grid.GridEditor;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.ui.ontology.search.BioPortalSearchComponent;
import edu.stanford.bmir.protege.web.client.util.Project;

public class ReferenceFieldWidget extends InstanceGridWidget {


	private Window window = null;
	private BioPortalSearchComponent bpSearchComponent = null;
	
	public ReferenceFieldWidget(Project project) {
		super(project);
	}

	protected GridEditor getGridEditor() {
		return null;
	}

	public String preRenderColumnContent(String content) {
		if (content.startsWith("http://")) {
			content = "<a href= \"" + content + "\" target=_blank>" + content + "</a>";
		}
		return content;
	}

	
	@Override
	public Hyperlink createAddNewHyperlink() {
		Hyperlink addNewLink = new Hyperlink("<br><img src=\"images/add.png\"></img>&nbsp Add new reference", true, "");
		//Hyperlink addNewLink = new Hyperlink(getProperty().getBrowserText() + ": <br><br><img src=\"images/add.png\"></img>&nbsp Add new value", true, "");			
		addNewLink.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				onAddNewReference();							
			}
		});
		
		return addNewLink;
	}

	private void onAddNewReference() {
        window = new Window();
        window.setTitle("BioPortal Search");       
        window.setMinWidth(500);
        window.setWidth(500);
        window.setMinHeight(300);
        window.setLayout(new FitLayout());

        FormPanel panel = new FormPanel();
        panel.setLayout(new FitLayout());
        
//        if (bpSearchComponent == null) {
	        bpSearchComponent = new BioPortalSearchComponent(getProject()) {
	        	@Override
	        	protected AbstractAsyncHandler<Boolean> getImportBioPortalConceptHandler() {
	        		return new ImportBioPortalConceptHandler(this);
	        	}
	        };
//        }
        
        Map<String, Object> widgetConfig = getWidgetConfiguration();
        Map<String, Object> bpSearchProperties = (Map<String, Object>) widgetConfig.get(FormConstants.BP_SEARCH_PROPERTIES);
        bpSearchComponent.setConfigProperties(bpSearchProperties);
		
        panel.add(bpSearchComponent);//, new BorderLayoutData(RegionPosition.CENTER));
//        panel.addButton(showInTreeButton); //n, new BorderLayoutData(RegionPosition.SOUTH));
//        panel.addButton(closeButton);
        
        window.add(panel);
        window.show();
        
//        if (bpSearchComponent.getEntity() == null) {
        	bpSearchComponent.setEntity(getSubject());
//        }
	}							

//	protected void attachListeners() {
//		//TODO: may not works so well.. - check indexes
//		getGridPanel().addGridCellListener(new GridCellListenerAdapter() {
//			 public void onCellClick(GridPanel grid, int rowIndex, int colindex, EventObject e) {
//				 if (colindex == properties.size())  {//FIXME!!!!
//					 //should be delete - do nothing
//					 Record record = store.getAt(rowIndex);
//					 if (record != null) {						 
//						 onDelete(rowIndex);
//					 }
//				 } else if (colindex == properties.size() + 1) {
//					 Record record = store.getAt(rowIndex);
//					 record.getAs
//				 }
//			 }						
//		});		
//		
//	}

	
	class ImportBioPortalConceptHandler extends AbstractAsyncHandler<Boolean> {
		private BioPortalSearchComponent bpSearchComponent;

		public ImportBioPortalConceptHandler(BioPortalSearchComponent bioPortalSearchComponent) {
			this.bpSearchComponent = bioPortalSearchComponent;
		}

		@Override
		public void handleFailure(Throwable caught) {
			bpSearchComponent.getEl().unmask();
			GWT.log("Could not import BioPortal concept " , null);			
			MessageBox.alert("Import operation failed!");
		}
		
		@Override
		public void handleSuccess(Boolean success) {
			bpSearchComponent.getEl().unmask();			
			refresh();
			if (!success) {
				MessageBox.alert("Import operation DID NOT SUCCEDED!");
			}
		}
	}
	
}
