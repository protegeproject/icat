package edu.stanford.bmir.protege.web.client.ui.icd;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;


public class IcdApiSearchManager {
	
	//just as workaround for resetting the subtree filter to the top level
	public static String TOP_LEVEL_SUBTREE_FILTER = 
		      "http://id.who.int/icd/entity/448895267,"   // ICD Entity
			+ "http://id.who.int/icd/entity/1405434703,"  // ICF Entity
			+ "http://id.who.int/icd/entity/60347385,"    // ICHI Entity
			+ "http://id.who.int/icd/entity/1320036174";  // Extension Entities
	
	
	public static String ICD_API_SERVER_URL_PROP = "icd.api.server.url";
	public static String ICD_API_SERVER_URL_DEFAULT = "https://icdapilive.azurewebsites.net";
	
	
	private static IcdApiSearchManager instance;
	
	private Map<String, SearchComponent> iNo2Field = new HashMap<String, SearchComponent>();
	
	private static int next_INo = 0;
	
	
	private IcdApiSearchManager() {}

	public static IcdApiSearchManager getInstance() {
		if (instance == null) {
			instance = new IcdApiSearchManager();
			
			instance.exportSetSelection();
			instance.init(getIcdApiUrl());
		}
		return instance;
	}
	
	private static String getIcdApiUrl() {
		return ClientApplicationPropertiesCache.getStringProperty(ICD_API_SERVER_URL_PROP, ICD_API_SERVER_URL_DEFAULT);
	}
	
	public void setSelection(String iNo, String uri) {
		SearchComponent field = iNo2Field.get(iNo);
		if (field == null) {
			GWT.log("Could not find ICD search field with iNo: " + iNo);
			return;
		}
		
		field.setSelection(uri);
	}


	public native void exportSetSelection() /*-{
		var that = this;
		$wnd.setSelection = $entry(function(iNo,uri) {
			that.@edu.stanford.bmir.protege.web.client.ui.icd.IcdApiSearchManager::setSelection(Ljava/lang/String;Ljava/lang/String;)(iNo,uri);
		});
	}-*/;

	public native void init(String icdApiUrl) /*-{
		mySettings = {
			apiServerUrl: icdApiUrl,
			simplifiedMode: false,
			popupMode: false,
			icdLinearization: "foundation",
			autoBind: false,
			apiSecured: false
		};
		
		selectedEntityFunct = function(selectedEntity)  { 
			console.log("Selected iNo: " + selectedEntity.iNo + " selectedEntity: " + selectedEntity.title);
			$wnd.setSelection(selectedEntity.iNo, selectedEntity.uri);
		};
		
		myCallbacks = {
			selectedEntityFunction: selectedEntityFunct,
		};
		
		// configure the ECT Handler
		$wnd.ECT.Handler.configure(mySettings, myCallbacks);
		
	}-*/;

	public SearchComponent createSearchComponent(Project project, Selectable selectable) {
		String iNo = getNextINo();
		SearchComponent searchComp = new SearchComponent(project, iNo, selectable); 
		
		iNo2Field.put(iNo, searchComp);
		return searchComp;
	}
	
	public void bind(SearchComponent field) {
		String iNo = field.getiNo();
		bind(iNo);
	}
	
	public native void bind(String iNo) /*-{
		$wnd.ECT.Handler.bind(iNo);
	}-*/;

	public void setSubtreeFilter(SearchComponent field, String icdSearchFilter) {
		if (icdSearchFilter == null || icdSearchFilter.length() == 0) {
			icdSearchFilter = TOP_LEVEL_SUBTREE_FILTER;
		}
		setSubtreeFilter(field.getiNo(), icdSearchFilter);
		bind(field.getiNo());
	}

	private native void setSubtreeFilter(String iNo, String icdSearchFilter) /*-{
		$wnd.ECT.Handler.overwriteConfiguration(iNo, {subtreesFilter: icdSearchFilter});
	}-*/;
		
	private static String getNextINo() {
		next_INo++;
		return new Integer(next_INo).toString();
	}
}
