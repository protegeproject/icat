package edu.stanford.bmir.protege.web.client.ui.ontology.hierarchy;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.FormPanel;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.layout.AnchorLayoutData;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ChAOServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;
import edu.stanford.bmir.protege.web.client.ui.selection.Selectable;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionEvent;
import edu.stanford.bmir.protege.web.client.ui.selection.SelectionListener;
import edu.stanford.bmir.protege.web.client.ui.util.ClassSelectionField;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;
import edu.stanford.bmir.protege.web.client.ui.util.field.TextAreaField;

public class CreateClassPanel extends FormPanel implements Selectable {

    private final Project project;
    private ClassSelectionField parentsField;
    private TextField nameField;
    //private TextField codeField;
    private TextAreaField reasonField;
    private AsyncCallback<EntityData> asyncCallback;
    private String topClass;

    public CreateClassPanel(Project project) {
        this(project, null, null);
    }

    public CreateClassPanel(Project project, AsyncCallback<EntityData> asyncCallback, String topClass) {
        this.project = project;
        this.asyncCallback = asyncCallback;
        this.topClass = topClass;
        buildUI();
    }

    private void buildUI() {
        setHeight(350);

        Label title = new Label("CREATE NEW CLASS");
        title.setStylePrimaryName("hierarchy-title");
        add(title, new AnchorLayoutData("100% - 53"));

        HTML explanationHtml = new HTML("Please enter a <b>name</b> for the new class.<br />" +
                "Select one or more <b>parents</b> for the category by clicking on the &nbsp <img src=\"../images/add.png\"></img> &nbsp icon in the <i>Parents</i> field.<br />" +
                "Operations are performed only after clicking on the <i>Create</i> button.");
        explanationHtml.setStylePrimaryName("explanation");
        add(explanationHtml);

        nameField = new TextField("Name", "name", 300);
        nameField.setAllowBlank(false);
        add(nameField, new AnchorLayoutData("100% - 53"));
/*
        codeField = new TextField("Code", "code",  300);
        codeField.setAllowBlank(false);
        add(codeField, new AnchorLayoutData("100% - 53"));
*/
        parentsField = new ClassSelectionField(project, "Parent(s)", true, topClass);
        add(parentsField, new AnchorLayoutData("100% - 53"));

        reasonField = new TextAreaField();
        reasonField.setLabel("Reason for change:");
        ((TextArea)reasonField.getFieldComponent()).setHeight(120);
        add(reasonField, new AnchorLayoutData("100% - 53"));

        Button createButton = new Button("Create");
        createButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                if (UIUtil.confirmOperationAllowed(project)) {
                    onCreate();
                }
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.addListener(new ButtonListenerAdapter() {
            @Override
            public void onClick(Button button, EventObject e) {
                clear();
                destroy();
                if (asyncCallback != null) {
                    asyncCallback.onFailure(null);
                }
            }
        });

        addButton(createButton);
        addButton(cancelButton);
    }


    public void setParentsClses(Collection<EntityData> parents) {
        parentsField.setClsValues(parents);
    }

    public Collection<EntityData> getParentsClses() {
       return parentsField.getClsValues();
    }

    private void onCreate() {
        final String clsName = nameField.getValueAsString();
        //final String code = codeField.getValueAsString();
        final String reasonForChange = reasonField.getValueAsString();

        if (clsName == null || clsName.length() == 0) {
            MessageBox.alert("Empty class name", "Please enter a class name.");
            return;
        }

        /*
        if (code == null || code.length() == 0) {
            MessageBox.alert("Empty code", "Please enter a code.");
            return;
        }
        */

        if (reasonForChange == null || reasonForChange.length() == 0) {
            MessageBox.alert("No reason for change", "A reason for the change was not provided.<br />" +
                    "Please fill in the <i>Reason for change</i> field.");
            return;
        }

        UIUtil.mask(getEl(), "Class " + clsName + " is being created...", true, 10);
        performCreate();
    }

    protected void performCreate() {
        ICDServiceManager.getInstance().createICDCls(project.getProjectName(), null,
                UIUtil.getStringCollection(parentsField.getClsValues()), nameField.getValueAsString(),
                GlobalSettings.getGlobalSettings().getUserName(), getOperationDescription(),
                "reason for change", new CreateClassHandler()); //TODO: remove the unneeded args
    }

    protected void createNote(final EntityData newClass, String opDesc, String reasonForChange) {
        NotesData noteData = new NotesData();
        noteData.setAuthor(GlobalSettings.getGlobalSettings().getUserName());
        noteData.setSubject("[Reason for change]: " + opDesc);
        noteData.setBody(reasonForChange);
        noteData.setAnnotatedEntity(newClass);
        ChAOServiceManager.getInstance().createNote(project.getProjectName(), noteData, false, new AbstractAsyncHandler<NotesData>() {
            @Override
            public void handleFailure(Throwable caught) {
                GWT.log("Could not create note for " + newClass);
            }
            @Override
            public void handleSuccess(NotesData result) {
                //TODO: maybe update notes count?
            }
        });
    }

    public String getOperationDescription() {
       return "Create class with name: " + nameField.getValueAsString() +
        ", parents: " + UIUtil.commaSeparatedList(parentsField.getClsValues());
    }

    public String getReasonForChange() {
       return reasonField.getValueAsString();
    }

    public void setAsyncCallback(AsyncCallback<EntityData> asyncCallback) {
        this.asyncCallback = asyncCallback;
    }

    public String getTopClass() {
        return topClass;
    }

    public void setTopClass(String topClass) {
        this.topClass = topClass;
        parentsField.setTopClass(topClass);
    }

    protected void refreshFromServer() {
        Timer timer = new Timer() {
            @Override
            public void run() {
                project.forceGetEvents();
            }
        };
        timer.schedule(500);
    }

    public void addSelectionListener(SelectionListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Collection<EntityData> getSelection() {
        return parentsField.getClsValues();
    }

    public void notifySelectionListeners(SelectionEvent selectionEvent) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void removeSelectionListener(SelectionListener listener) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void setSelection(Collection<EntityData> selection) {
        parentsField.setClsValues(selection);
    }

    /*
     * Remote calls
     */

    class CreateClassHandler extends AbstractAsyncHandler<EntityData> {

        @Override
        public void handleFailure(Throwable caught) {
            getEl().unmask();
            MessageBox.alert("Errot at creating new class", "There was an error at creating the new class.<br />" +
            		"Message: " + caught.getMessage());
            GWT.log("There was an error at creating the new class." + caught.getMessage(), caught);
            if (asyncCallback != null) {
                asyncCallback.onFailure(caught);
            }
        }

        @Override
        public void handleSuccess(EntityData entityData) {
            getEl().unmask();
            if (entityData != null) {
                nameField.reset();

                //create note
                createNote(entityData, getOperationDescription(), getReasonForChange());

            } else {
                GWT.log("Problem at creating class", null);
                MessageBox.alert("Class creation failed.");
            }
            if (asyncCallback != null) {
                asyncCallback.onSuccess(entityData);
            }
            refreshFromServer();
        }

    }
}
