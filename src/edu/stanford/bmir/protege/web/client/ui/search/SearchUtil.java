package edu.stanford.bmir.protege.web.client.ui.search;

import java.util.ArrayList;
import java.util.Collection;

import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.layout.AnchorLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;

public class SearchUtil {

    private Project project;
    private Selectable selectable;
    private ValueType searchedValueType;
    private SearchGridPanel searchGrid;
    
    private String lastSearchString;

    public SearchUtil(Project project, Selectable selectable) {
        this.project = project;
        this.selectable = selectable;
        init();
    }
    
    private void init()
    {
        searchGrid = new SearchGridPanel() {
            @Override
            protected void onEntityDblClick() {
                doSelect(getSelection());
            }
        };
        searchGrid.setAutoScroll(true);
        searchGrid.setStripeRows(true);
        
    }

    public void search(String text) {
        lastSearchString = text;
        showSearchResults(text);
    }

    public void setSearchedValueType(ValueType searchedValueType) {
        this.searchedValueType = searchedValueType;
    }

    public ValueType getSearchedValueType() {
        return searchedValueType;
    }

    public void setBusyComponent(Component busyComponent) {
        this.searchGrid.setBusyComponent(busyComponent);
    }

    /*
     * Remote calls
     */

    private void showSearchResults(String searchText) {

        final Window window = new Window();
        window.setTitle("Search results for '" + lastSearchString + "'");
        window.setWidth(500);
        window.setHeight(365);
        window.setLayout(new FitLayout());

        FormPanel panel = new FormPanel();

        Button showInTreeButton = new Button("Select in tree", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
            	doSelect(searchGrid.getSelection());
            }
        });

        Button closeButton = new Button("Close", new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                window.close(); // TODO: cancel existing search
                window.destroy();
            }
        });
        
        panel.add(searchGrid, new AnchorLayoutData("100% 100%"));
        panel.addButton(showInTreeButton);
        panel.addButton(closeButton);

        window.add(panel);
        
        this.searchGrid.reload(project.getProjectName(), searchText, searchedValueType, window);
    }

	private void doSelect(final EntityData selection) {
        if (selection == null) {
            return;
        }
        if (selectable != null) {
            Collection<EntityData> selectionCollection = new ArrayList<EntityData>();
            selectionCollection.add(selection);
            selectable.setSelection(selectionCollection);
        }
	}

}
