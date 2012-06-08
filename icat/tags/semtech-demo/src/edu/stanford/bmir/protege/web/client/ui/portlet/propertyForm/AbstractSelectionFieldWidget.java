package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;

public abstract class AbstractSelectionFieldWidget extends TextFieldWidget {

    public AbstractSelectionFieldWidget(Project project) {
        super(project);
        getField().setDisabled(true);
    }

    @Override
    protected Collection<Widget> createSuffixComponents() {
        ArrayList<Widget> list = new ArrayList<Widget>();
        list.add(createAddEntityLink());
        return list;
    }

    protected Anchor createAddEntityLink() {
        Anchor deleteLink = new Anchor("&nbsp<img src=\"images/add.png\"></img>", true, "");
        deleteLink.setWidth("20px");
        deleteLink.setTitle("Add new value");
        deleteLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                onSelectEntity();
            }
        });
        return deleteLink;
    }

    protected void onSelectEntity() {
        final Window window = new Window();
        window.setTitle("Select value");
        window.setClosable(true);
        window.setPaddings(7);
        window.setWidth(250);
        window.setHeight(350);
        window.setLayout(new FitLayout());
        //window.setCloseAction(Window.HIDE);
        window.add(new SelectionDialog(window, createSelectable()));
        window.show();
    }

    public abstract Selectable createSelectable();

    /*
     * Internal class
     */

    class SelectionDialog extends Panel {
        private Window parent;
        private Selectable selectable;

        public SelectionDialog(Window parent, Selectable selectable) {
            super();
            this.parent = parent;
            this.selectable = selectable;

            //setPaddings(15);

            Button selectButton = new Button("Select", new ButtonListenerAdapter() {
                @Override
                public void onClick(Button button, EventObject e) {
                    Collection<EntityData> selection = SelectionDialog.this.selectable.getSelection();
                    if (selection != null && selection.size() > 0) {
                        EntityData singleSelection = selection.iterator().next();
                        SelectionDialog.this.parent.close();
                        onChangeValue(getSingleValue(), singleSelection);
                    }
                }
            });

            setLayout(new FitLayout());
            add((Widget) selectable);
            addButton(selectButton);
        }
    }

}
