package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public abstract class AbstractSelectionFieldWidget extends TextFieldWidget {

    protected static final String ADD_ICON_STYLE_STRING = DELETE_ICON_STYLE_STRING;

    public AbstractSelectionFieldWidget(Project project) {
        super(project);
    }

    @Override
    protected Collection<Widget> createSuffixComponents() {
        ArrayList<Widget> list = new ArrayList<Widget>();
        list.add(createAddEntityLink());
        return list;
    }

    protected Anchor createAddEntityLink() {
    	boolean enabled = ( !isReadOnly() && !isDisabled());
        Anchor addLink = ( enabled ?
        		new Anchor("&nbsp<img src=\"images/add.png\" " + ADD_ICON_STYLE_STRING + "></img>", true) :
        		new Anchor("&nbsp<img src=\"images/add_grey.png\" " + ADD_ICON_STYLE_STRING + "></img>", true) );
        addLink.setWidth("22px");
        addLink.setHeight("22px");
        addLink.setTitle(enabled ? "Add new value" : "Add value is not allowed");
        addLink.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
            	if (!isReadOnly() && !isDisabled() && isWriteOperationAllowed()) {
            		onSelectEntity();
            	}
            }
        });
        return addLink;
    }

    protected void onSelectEntity() {
        final Window window = new Window();
        window.setTitle("Select value");
        window.setClosable(true);
        window.setPaddings(7);
        setWindowSize(window);
        window.setLayout(new FitLayout());
        //window.setCloseAction(Window.HIDE);
        window.add(new SelectionDialog(window, createSelectable()));
        window.show();
    }

    public abstract Selectable createSelectable();

    public void setWindowSize(Window window) {
        window.setWidth(250);
        window.setHeight(350);
    }

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
                        onChangeValue(getSubject(), UIUtil.getFirstItem(getValues()), singleSelection);
                    }
                    else {
                        SelectionDialog.this.parent.close();
                        onChangeValue(getSubject(), UIUtil.getFirstItem(getValues()), null);
                    }
                }
            });

            setLayout(new FitLayout());
            add((Widget) selectable);
            addButton(selectButton);
        }
    }

    @Override
    public void setup(Map<String, Object> widgetConfiguration, PropertyEntityData propertyEntityData) {
        super.setup(widgetConfiguration, propertyEntityData);
        getField().setReadOnly(true);
    }

    //testing
//    @Override
//    protected void displayValues() {
//    	GWT.log("called AbstractSelectionFieldWidget.displayValues");
//    	Field f = getField();
//    	GWT.log("Field: " + f + "; isVisible: " + f.isVisible());
//    	
//    	super.displayValues();
//    }
}
