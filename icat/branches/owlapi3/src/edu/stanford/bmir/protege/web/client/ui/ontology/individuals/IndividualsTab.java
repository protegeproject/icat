package edu.stanford.bmir.protege.web.client.ui.ontology.individuals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.Store;
import com.gwtext.client.data.event.StoreListener;
import com.gwtext.client.data.event.StoreListenerAdapter;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Container;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.event.ContainerListener;
import com.gwtext.client.widgets.event.ContainerListenerAdapter;
import com.gwtext.client.widgets.event.PanelListener;
import com.gwtext.client.widgets.event.PanelListenerAdapter;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.portlet.EntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionListener;
import edu.stanford.bmir.protege.web.client.ui.tab.AbstractTab;

/**
 * A single view that shows the classes in an ontology.
 *
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class IndividualsTab extends AbstractTab {

    private ClassTreePortlet clsTreePortlet;
    private IndividualsListPortlet indListPorlet;

    public IndividualsTab(Project project) {
        super(project);
    }

    @Override
    public void setup() {
        super.setup();

        clsTreePortlet = (ClassTreePortlet) getPortletByClassName(ClassTreePortlet.class.getName());
        indListPorlet = (IndividualsListPortlet) getPortletByClassName(IndividualsListPortlet.class.getName());

        setControllingPortlet(indListPorlet);

        if (clsTreePortlet != null && indListPorlet != null) {
            clsTreePortlet.addSelectionListener(new SelectionListener() {
                public void selectionChanged(SelectionEvent event) {
                    EntityData selection = clsTreePortlet.getSelection().get(0);
                    Collection<EntityPortlet> portlets = getPortlets();
                    for (EntityPortlet portlet : portlets) {
                        portlet.setEntity(selection);
                    }

                }
            });
        }
    }

    @Override
    public void setSelection(final String selectionName) {
        final EntityData individual = new EntityData(selectionName);
        OntologyServiceManager.getInstance().getAllImplementedClasses(this.project.getProjectName(), individual, new AbstractAsyncHandler<List<EntityData>>() {
            @Override
            public void handleFailure(Throwable caught) {
                GWT.log("Could not select " + selectionName);
            }

            @Override
            public void handleSuccess(final List<EntityData> refInstance) {
                clsTreePortlet.setSelection(refInstance);
                indListPorlet.addStoreListener(new StoreListenerAdapter(){
                    @Override
                    public void onAdd(Store store, Record[] records, int index) {

                        indListPorlet.setSelection(Arrays.asList(individual));
                    }
                });

            }
        });
    }
}
