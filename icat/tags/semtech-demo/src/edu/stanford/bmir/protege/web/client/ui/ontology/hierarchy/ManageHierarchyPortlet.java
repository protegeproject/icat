package edu.stanford.bmir.protege.web.client.ui.ontology.hierarchy;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.layout.ColumnLayout;
import com.gwtext.client.widgets.layout.ColumnLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractEntityPortlet;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class ManageHierarchyPortlet extends AbstractEntityPortlet {

    private static final String TOP_CLASS_PROP = "topClass";

    private Panel wrappingPanel;
    private Selectable selectable;
    private String topClass;

    public ManageHierarchyPortlet(Project project) {
        super(project);
    }

    @Override
    public void initialize() {
        setTitle("Manage hierarchy");
        setPaddings(5);
        setLayout(new ColumnLayout());

        topClass = getRootClsName();

        Panel rightPanel = new Panel();

        rightPanel.add(createCreateClsHtml()); //TODO: commented out because it invoked ICD class creation

        rightPanel.add(createMoveClsHtml());

        rightPanel.add(new HTML("<br />"));
        rightPanel.add(createRetireClsHtml());

        rightPanel.add(createAdditionalHtml());

        wrappingPanel = new Panel();
        wrappingPanel.setAutoScroll(true);
        wrappingPanel.setPaddings(5);
        wrappingPanel.setLayout(new FitLayout());

        add(rightPanel, new ColumnLayoutData(.35));
        add(wrappingPanel, new ColumnLayoutData(.65));
        add(rightPanel);
        add(wrappingPanel);
    }

    @Override
    public void setPortletConfiguration(PortletConfiguration portletConfiguration) {
        super.setPortletConfiguration(portletConfiguration);
        topClass = getRootClsName();
    }


    private String getRootClsName() {
        PortletConfiguration portletConfiguration = getPortletConfiguration();
        if (portletConfiguration == null) {
            return topClass;
        }
        Map<String, Object> props = portletConfiguration.getProperties();
        if (props == null) {
            return topClass;
        }
        if (topClass == null) {
            topClass = (String) props.get(TOP_CLASS_PROP);
        }
        return topClass ;
    }

    /*
     * HTML code
     */

    protected HTML createCreateClsHtml() {
        HTML createClassHtml = new HTML("CREATE NEW CLASS", true);
        createClassHtml.setStylePrimaryName("manage-button");
        createClassHtml.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (UIUtil.confirmOperationAllowed(getProject())) {
                    onCreateCls();
                }
            }
        });
        return createClassHtml;
    }

    protected HTML createRetireClsHtml() {
        HTML retireClassHtml = new HTML("RETIRE CLASS", true);
        retireClassHtml.setStylePrimaryName("manage-button");
        retireClassHtml.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (UIUtil.confirmOperationAllowed(getProject())) {
                    onRetireCls();
                }
            }
        });
        return retireClassHtml;
    }

    protected HTML createMoveClsHtml() {
        HTML moveHtml = new HTML("CHANGE PARENTS<br />(add or remove parents, move in hierarchy)", true);
        moveHtml.setStylePrimaryName("manage-button");
        moveHtml.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (UIUtil.confirmOperationAllowed(getProject())) {
                    onChangeParents();
                }
            }
        });
        return moveHtml;
    }


    protected HTML createAdditionalHtml() {
        return new HTML("");
    }

    /*
     * Functionality
     */

    protected void onCreateCls() {
        wrappingPanel.clear();
        CreateClassPanel createClassPanel = new CreateClassPanel(project);
        createClassPanel.setTopClass(topClass);
        createClassPanel.setParentsClses(UIUtil.createCollection(getEntity()));
        wrappingPanel.add(createClassPanel);
        selectable = createClassPanel;
        doLayout();
    }

    protected void onRetireCls() {
        wrappingPanel.clear();
        RetireClassPanel retireClassPanel = new RetireClassPanel(project);
        retireClassPanel.setTopClass(topClass);
        retireClassPanel.setParentsClses(UIUtil.createCollection(getEntity()));
        wrappingPanel.add(retireClassPanel);
        selectable = retireClassPanel;
        doLayout();
    }


    protected void onChangeParents() {
        wrappingPanel.clear();
        ChangeParentPanel changeParentPanel = new ChangeParentPanel(project);
        changeParentPanel.setTopClass(topClass);
        changeParentPanel.setParentsClses(UIUtil.createCollection(getEntity()));
        wrappingPanel.add(changeParentPanel);
        selectable = changeParentPanel;
        doLayout();
    }


    /*
     * Misc
     */

    @Override
    public void reload() {
        EntityData entityData = getEntity();
        if (selectable != null) {
            selectable.setSelection(UIUtil.createCollection(entityData));
        }
    }

    public Collection<EntityData> getSelection() {
       return (selectable != null) ? selectable.getSelection() : null;
    }

}
