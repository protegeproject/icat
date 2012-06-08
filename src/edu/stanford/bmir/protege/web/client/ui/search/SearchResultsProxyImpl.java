package edu.stanford.bmir.protege.web.client.ui.search;

import java.util.List;

import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PaginationData;
import edu.stanford.bmir.protege.web.client.ui.util.GWTProxy;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.UrlParam;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.MessageBox;

import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;

public class SearchResultsProxyImpl extends GWTProxy {

    private String projectName = null;
    private String searchText = null;
    private ValueType valueType = null;
    private Component busyComponent = null;
    
    public Component getBusyComponent() {
        return busyComponent;
    }

    public void setBusyComponent(Component busyComponent) {
        this.busyComponent = busyComponent;
    }
    
    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }
    
    public void resetParams(){
        this.projectName = null;
        this.searchText = null;
        this.valueType = null;
    }

    public SearchResultsProxyImpl() {

    }

    @Override
    public void load(int start, int limit, String sort, String dir, final JavaScriptObject o, UrlParam[] baseParams) {

        if (busyComponent != null) {
            busyComponent.getEl().mask("Searching", true);
         }

        OntologyServiceManager.getInstance().search(projectName, searchText, valueType, start, limit, sort, dir,
                new AsyncCallback<PaginationData<EntityData>>() {
                    public void onFailure(Throwable caught) {
                        if (busyComponent != null) {
                            UIUtil.unmask(busyComponent.getEl());
                        }
                        GWT.log("Error at search", caught);
                        MessageBox.alert("There were problems at search. Please try again later");
                        loadResponse(o, false, 0, (JavaScriptObject) null);
                    }

                    public void onSuccess(PaginationData<EntityData> result) {

                        if (busyComponent != null) {
                            UIUtil.unmask(busyComponent.getEl());
                        }
                        
                        if (result != null && result.getTotalRecords() != 0) {

                            loadResponse(o, true, result.getTotalRecords(), getRecords(result));
                        } else {
                            MessageBox.alert("No results", "Empty search results for: '" + searchText + "'. Please try a different query. <BR>" +
                                    "<BR>" +
                                    "<B>Hint:</B> You may use wildcards (*) in your search query. <BR>" +
                                    "&nbsp;&nbsp;&nbsp;&nbsp;(Wildcards are automatically added before and after query strings that&nbsp;&nbsp;&nbsp;&nbsp;<BR>" +
                                    "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;have at least 3 characters AND do not already start or end with a wildcard.)&nbsp;&nbsp;&nbsp;&nbsp;");
                            loadResponse(o, false, 0, (JavaScriptObject) null);
                        }
                    }

                    private Object[][] getRecords(PaginationData<EntityData> result) {

                        List<EntityData> records = result.getData();
                        Object[][] resultAsObjects = new Object[records.size()][1];

                        int i = 0;
                        for (EntityData record : records) {
                            Object[] obj = getRow(record);
                            resultAsObjects[i++] = obj;
                        }
                        return resultAsObjects;
                    }

                    private Object[] getRow(Object o1) {
                        return new Object[] { o1 };
                    }
                });
    }
}
