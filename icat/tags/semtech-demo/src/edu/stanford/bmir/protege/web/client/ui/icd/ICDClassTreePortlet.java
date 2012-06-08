package edu.stanford.bmir.protege.web.client.ui.icd;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.ToolbarButton;
import com.gwtext.client.widgets.tree.TreeNode;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.ontology.hierarchy.CreateClassPanel;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class ICDClassTreePortlet extends ClassTreePortlet {

    public ICDClassTreePortlet(Project project) {
        super(project);
    }

    @Override
    protected void onCreateCls() {
        final com.gwtext.client.widgets.Window selectWindow = new com.gwtext.client.widgets.Window();

        selectWindow.setBorder(false);
        selectWindow.setHeight(380);
        selectWindow.setPaddings(5);
        selectWindow.setPlain(true);
        selectWindow.setBodyBorder(false);

        final CreateClassPanel createClassPanel = new CreateClassPanel(project, null, getRootClsName());

        createClassPanel.setAsyncCallback(new AsyncCallback<EntityData>() {

            public void onFailure(Throwable caught) {
                selectWindow.hide();
                MessageBox.alert("Error", "There was a problem at creating class.<br />Message: " + caught.getMessage());
            }

            public void onSuccess(EntityData newClass) {
                selectWindow.hide();

                if (newClass == null) {
                    MessageBox.alert("Error", "There was a problem at creating class.");
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
    protected ToolbarButton createDeletButton() {
        return null;
    }

}
