package edu.stanford.bmir.protege.web.client.ui.ontology.notes;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.Window;
import com.gwtext.client.widgets.layout.FitLayout;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.ChAOServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;

public class NoteInputHandler {

    private Project project = null;
    private EntityData currentEntity = null;
    private NotesData record = null;
    private AsyncCallback<NotesData> cb = null;
    private boolean topLevel = false;
    
    public NoteInputHandler(Project project, EntityData currentEntity, AsyncCallback<NotesData> cb){
        this.project = project;
        this.currentEntity = currentEntity;
        this.cb = cb;
    }
    
    public boolean getTopLevel() {
        return topLevel;
    }

    public void setTopLevel(boolean topLevel) {
        this.topLevel = topLevel;
    }

    public Project getProject() {
        return project;
    }
    public void setProject(Project project) {
        this.project = project;
    }
    public EntityData getCurrentEntity() {
        return currentEntity;
    }
    public void setCurrentEntity(EntityData currentEntity) {
        this.currentEntity = currentEntity;
    }
    public NotesData getRecord() {
        return record;
    }
    public void setRecord(NotesData record) {
        this.record = record;
    }
    public AsyncCallback<NotesData> getCb() {
        return cb;
    }
    public void setCb(AsyncCallback<NotesData> cb) {
        this.cb = cb;
    }

    public void showInWindow(final boolean isReply, String subject, String text) {
        final Window window = new Window();
        window.setTitle(isReply ? "Reply" : "New topic");
        window.setWidth(600);
        window.setHeight(450);
        window.setMinWidth(300);
        window.setMinHeight(200);
        window.setLayout(new FitLayout());
        window.setPaddings(5);
        window.setButtonAlign(Position.CENTER);
        window.setPlain(true);

        NoteInputPanel nip = new NoteInputPanel(this.project, "Please enter your note:", false, subject, text,
                getAnnotatedEntity(isReply), window, new AsyncCallback<NotesData>() {
                    public void onFailure(Throwable caught) {
                        if(caught != null){
                            cb.onFailure(caught);
                        }
                        closeWindow();
                    }
                    public void onSuccess(NotesData result) {
                        if(result != null){
                            if (isReply) {
                                onReplyButton(result);
                            } else  {
                                onPostButton(result);
                            }
                        }
                        closeWindow();
                    }
                    
                    public void closeWindow(){
                        window.close();
                    }
                });
        window.add(nip);

        window.show();
        nip.getMainComponentForFocus().focus();
    }
    
    public void editNote(){
        final Window window = new Window();
        window.setTitle("Edit Note");
        window.setWidth(600);
        window.setHeight(450);
        window.setMinWidth(300);
        window.setMinHeight(200);
        window.setLayout(new FitLayout());
        window.setPaddings(5);
        window.setButtonAlign(Position.CENTER);
        window.setPlain(true);

        NoteInputPanel nip = new NoteInputPanel(this.project, "Please edit your note:", false, this.record,
                getAnnotatedEntity(true), new AsyncCallback<NotesData>() {
                    public void onFailure(Throwable caught) {
                        if(caught != null){
                            cb.onFailure(caught);
                        }
                        window.close();
                    }
                    public void onSuccess(NotesData result) {
                        onEditButton(result);
                        window.close();
                    }
                });
        window.add(nip);
        window.show();
        nip.getMainComponentForFocus().focus();
    }
    
    private void onEditButton(NotesData note){
        
        String subject = note.getSubject();
        subject = (subject == null || subject.length() == 0 ? "(no subject)" : subject);
        note.setSubject(subject);
        ChAOServiceManager.getInstance().editNote(project.getProjectName(), note, this.record.getEntity().getName(), cb);
    }
    
    private void onReplyButton(NotesData note){
        if (GlobalSettings.getGlobalSettings().getUserName() == null) {
            MessageBox.alert("To post a message you need to be logged in.");
            return;
        }
        String subject = note.getSubject();
        subject = (subject == null || subject.length() == 0 ? "(no subject)" : subject);
        note.setSubject(subject);
        
        ChAOServiceManager.getInstance().createNote(project.getProjectName(), note, false, cb);
    }
    
    private void onPostButton(NotesData note){
        if (GlobalSettings.getGlobalSettings().getUserName() == null) {
            MessageBox.alert("To post a message you need to be logged in.");
            return;
        }
        String subject = note.getSubject();
        if (subject == null || subject.length() == 0) {
            subject = "(no subject)";
        }
        note.setSubject(subject);
        ChAOServiceManager.getInstance().createNote(project.getProjectName(), note, topLevel, cb);
    }
    
    private EntityData getAnnotatedEntity(boolean isReply) {
        EntityData annotatedEntity = null;
        if (isReply) {
            if (this.record != null) {
                String annotatedEntityId = this.record.getEntity().toString();
                if (annotatedEntityId != null) {
                    annotatedEntity = new EntityData(annotatedEntityId);
                }
            }
            if (annotatedEntity == null) {
                annotatedEntity = new EntityData(currentEntity != null ? currentEntity.getName() : null);
            }
        } else {
            if (currentEntity != null) {
                annotatedEntity = new EntityData(currentEntity.getName());
            }
        }
        return annotatedEntity;
    }
}
