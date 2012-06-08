package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.protege.notesapi.NotesException;
import org.protege.notesapi.NotesManager;
import org.protege.notesapi.notes.AnnotatableThing;
import org.protege.notesapi.notes.NoteType;
import org.protege.notesapi.oc.Ontology;
import org.protege.notesapi.oc.OntologyClass;
import org.semanticweb.owlapi.model.IRI;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.ChAOService;
import edu.stanford.bmir.protege.web.client.rpc.data.ChangeData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.NotesData;
import edu.stanford.bmir.protege.web.client.rpc.data.PaginationData;
import edu.stanford.bmir.protege.web.client.rpc.data.ReviewData;
import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.Review;
import edu.stanford.bmir.protegex.chao.change.api.Change;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Reviewer;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Timestamp;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.User;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.impl.DefaultTimestamp;
import edu.stanford.bmir.protegex.chao.util.interval.TimeIntervalCalculator;
import edu.stanford.smi.protege.collab.changes.ChAOUtil;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Transaction;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

/**
 * @author Csongor Nyulas <csongor.nyulas@stanford.edu>
 * @author Tania Tudorache <tudorache@stanford.edu>
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */
public class ChAOServiceImpl extends RemoteServiceServlet implements ChAOService {
    private static final long serialVersionUID = -6201244374065093182L;

    public KnowledgeBase getChAOKb(String projectName) {
        Project prj = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
        if (prj == null) {
            return null;
        }
        return ChAOKbManager.getChAOKb(prj.getKnowledgeBase());
    }

    protected Project getProject(String projectName) {
        return ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
    }

    /*
     * Notes methods
     */

    public Boolean archiveNote(String projectName, String noteId, boolean archive){
        NotesManager notesManager = NotesAPIUtil.getNotesManager(projectName);
        if(notesManager == null){
            return false;
        }

        org.protege.notesapi.notes.Annotation annotation = notesManager.getNote(noteId);
        annotation.setArchived(archive);

        saveTransaction(notesManager);

        return true;
    }

    public List<NotesData> getReplies(String projectName, String noteId, boolean topLevelOnly){
        NotesManager notesManager = NotesAPIUtil.getNotesManager(projectName);
        if(notesManager == null){
            return new ArrayList<NotesData>();
        }

        org.protege.notesapi.notes.Annotation annotation = notesManager.getNote(noteId);

        NotesData note = getDiscussionThread(annotation, false);
        if(topLevelOnly && note.getReplies() != null){
            for(NotesData reply : note.getReplies()){
                reply.setReplies(null);
            }
        }

        return note.getReplies();
    }

    public NotesData editNote(String projectName, NotesData note, String noteId) {
        NotesManager notesManager = NotesAPIUtil.getNotesManager(projectName);
        if(notesManager == null){
            return note;
        }

        org.protege.notesapi.notes.Annotation annotation = notesManager.getNote(noteId);

        annotation.setBody(note.getBody());
        annotation.setSubject(note.getSubject());
        annotation.setModifiedAt(System.currentTimeMillis());
        annotation.setArchived(note.isArchived());

        //TODO : look at how change the type
        //annotation.Type = NoteType.valueOf(note.getType());

        for(AnnotatableThing at : annotation.getAnnotates()){
            note.setAnnotatedEntity(new EntityData(at.getId()));
            break;
        }

        note.setBody(annotation.getBody());
        note.setAuthor(annotation.getAuthor());
        note.setSubject(annotation.getSubject());
        note.setEntity(new EntityData(annotation.getId()));
        note.setType(annotation.getType().name());


        Long doc = annotation.getCreatedAt();
        Date d = new Date(doc);

        note.setCreationDate(DefaultTimestamp.DATE_FORMAT.format(d));
        Date latestModificationAt = new Date(annotation.getModifiedAt());
        note.setLatestUpdate(latestModificationAt);

        saveTransaction(notesManager);

        return note;
    }

    public List<NotesData> getNotes(String projectName, String entityName, boolean ontologyLevelNotes, boolean topLevelNotesOnly){
        ArrayList<NotesData> notes = new ArrayList<NotesData>();
        Collection<org.protege.notesapi.notes.Annotation> annotations = null;

        NotesManager notesManager = NotesAPIUtil.getNotesManager(projectName);
        if(notesManager == null){
            return notes;
        }

        if(ontologyLevelNotes){
            // TODO: IMP
            Project prj = getProject(projectName);
            if (prj == null) {
                return notes;
            }
            KnowledgeBase kb = prj.getKnowledgeBase();
            String domainOntName = "";
            if (kb instanceof OWLModel) {
                domainOntName = ((OWLModel)kb).getDefaultOWLOntology().getName();
            } else {
                //TODO: fix me for Frames ontologies
            }
            Ontology ontology = notesManager.getOntology(IRI.create(domainOntName).toString());
            annotations = ontology.getAssociatedAnnotations();

        } else {

            // Need to migrate completely - use notesManager.getOWLOntology().containsClassInSignature(parentEntityName)

            Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
            KnowledgeBase kb = project.getKnowledgeBase();

            Instance annotatedInst = null;

            annotatedInst = kb.getInstance(entityName);
            if (annotatedInst == null) {
                org.protege.notesapi.notes.Annotation parent = notesManager.getNote(entityName);
                annotations = parent.getAssociatedAnnotations();
            } else {
                OntologyClass parentClass = notesManager.getOntologyClass(entityName);
                annotations = parentClass.getAssociatedAnnotations();
            }

            for(org.protege.notesapi.notes.Annotation ann : annotations){
                if(ann == null){
                    continue;
                }
                NotesData note = getDiscussionThread(ann, topLevelNotesOnly);
                if(note != null){
                    notes.add(note);
                }
            }

        }
        Collections.sort(notes, new NotesDataComparator());
        return notes;
    }

    public List<NotesData> getNotes(String projectName, String entityName, boolean ontologyLevelNotes) {
        return getNotes(projectName, entityName, ontologyLevelNotes, false);
    }

    private NotesData getDiscussionThread(org.protege.notesapi.notes.Annotation annotation, boolean topLevelOnly) {
        NotesData note = getNoteFromAnnotation(annotation);
        try {
            note.setArchived(annotation.hasArchived() ? annotation.getArchived() : false);
        } catch (Exception e) {
            if (Log.getLogger().getLevel().equals(Level.FINE)) {
                Log.getLogger().fine("Could not set archived flag on NotesData. Most likely ChAO does not contain a 'archived' slot");
            }
        }
        String d = note.getCreationDate();

        Date updateDate = DefaultTimestamp.getDateParsed(d);
        if(updateDate != null){
            note.setLatestUpdate(DefaultTimestamp.getDateParsed(d));
        }

        NotesData tempNote = topLevelOnly ? new NotesData(): note;

        for (org.protege.notesapi.notes.Annotation annotation2 : annotation.getAssociatedAnnotations()) {
            org.protege.notesapi.notes.Annotation reply = annotation2;
            tempNote.addReply(getDiscussionThread(reply, topLevelOnly));
        }

        if(tempNote.getReplies()!= null && tempNote.getReplies().size() > 0){
            NotesDataComparator comparator = new NotesDataComparator();
            Collections.sort(tempNote.getReplies(), comparator);

            int val = comparator.compare(note, tempNote.getReplies().get(0));
            if(val > 0){
                note.setLatestUpdate(tempNote.getReplies().get(0).getLatestUpdate());
            }
        }

        note.setNumOfReplies(tempNote.getNumOfReplies());

        return note;
    }

    public Collection<EntityData> getAvailableNoteTypes(String projectName) {

        Collection<EntityData> allAnnotationTypes = new ArrayList<EntityData>();
        for(NoteType t : NoteType.values()){
            allAnnotationTypes.add(OntologyServiceImpl.createEntityData(t.name(), false));
        }
        return allAnnotationTypes;
    }

    public NotesData createNote(String projectName, NotesData newNote, boolean topLevel) {
        NotesManager notesManager = NotesAPIUtil.getNotesManager(projectName);
        NotesData toReturn = null;

        Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
        KnowledgeBase kb = project.getKnowledgeBase();

        Instance annotatedInst = null;

        try {
            org.protege.notesapi.notes.Annotation n = notesManager.createSimpleNote(NoteType.valueOf(newNote.getType()), newNote.getSubject(), newNote.getBody(), newNote.getAuthor());
            n.setArchived(newNote.isArchived());
            String parentEntityName = newNote.getAnnotatedEntity() == null ? null : newNote.getAnnotatedEntity().getName();

            // Need to migrate completely - use notesManager.getOWLOntology().containsClassInSignature(parentEntityName)
            if (!topLevel && parentEntityName != null) {
                annotatedInst = kb.getInstance(parentEntityName);
                if (annotatedInst == null) {
                    org.protege.notesapi.notes.Annotation parent = notesManager.getNote(parentEntityName);
                    n.addAnnotates(parent);
                } else {
                    OntologyClass parentClass = notesManager.getOntologyClass(parentEntityName);
                    n.addAnnotates(parentClass);
                }
            }

            toReturn = getNoteFromAnnotation(n);
            saveTransaction(notesManager);

        } catch (NotesException e) {
            e.printStackTrace();
        }

        return toReturn;
    }


    private void saveTransaction(NotesManager notesManager) {
        //TODO: Need to change accordingly for saving an operation
        /*try {
            OWLOntology notesKb = notesManager.getOWLOntology();
            OWLOntologyManager manager = notesKb.getOWLOntologyManager();
            manager.saveOntology(notesKb, new RDFXMLOntologyFormat(), manager.getOntologyDocumentIRI(notesKb));

        }
        catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        }*/
    }

    private NotesData getNoteFromAnnotation(org.protege.notesapi.notes.Annotation n) {
        NotesData note = new NotesData();

        note.setBody(n.getBody());
        note.setAuthor(n.getAuthor());
        note.setSubject(n.getSubject());
        note.setType(n.getType().name());
        try { //backwards compatibility - archive was not there in previous versions
            note.setArchived(n.hasArchived() && n.getArchived());
        } catch (Exception e) {
            Log.getLogger().warning("Did not set archived status for note. Most likely archive slot is not present in Chao.");
        }
        note.setEntity(new EntityData(n.getId()));
        note.setEntity(new EntityData(n.getId()));
        for(AnnotatableThing at : n.getAnnotates()){
            note.setAnnotatedEntity(new EntityData(at.getId()));
            break;
        }
        Long doc = n.getCreatedAt();
        Date d = new Date(doc);
        note.setCreationDate(DefaultTimestamp.DATE_FORMAT.format(d));
        note.setLatestUpdate(d);

        return note;
    }

    public void deleteNote(String projectName, String noteId) {

        NotesManager notesManager = NotesAPIUtil.getNotesManager(projectName);
        if(notesManager == null){
            return;
        }

        notesManager.deleteNote(noteId);
        saveTransaction(notesManager);
    }

    /*
     * Change methods
     */

    public Collection<ChangeData> getChanges(String projectName, Date start, Date end) {
        ArrayList<ChangeData> changeData = new ArrayList<ChangeData>();
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return changeData;
        }

        TimeIntervalCalculator tic = TimeIntervalCalculator.get(chaoKb);
        Collection<Change> changes = tic.getTopLevelChanges(start, end);

        return getChangeData(changes);
    }

    public PaginationData<ChangeData> getChanges(String projectName, Date startDate, Date endDate, int start, int limit,
            String sort, String dir) {

        // TODO Need to find a way to just retrieve only 'limit' # of records
        // from database

        PaginationData<ChangeData> wrapper = new PaginationData<ChangeData>();
        ArrayList<ChangeData> changeData = (ArrayList<ChangeData>) getChanges(projectName, startDate, endDate);

        int totalRecords = changeData.size();
        wrapper.setTotalRecords(totalRecords);

        if (start >= totalRecords) {
            return wrapper;
        }

        int end = start + limit - 1;
        int lastIndex = totalRecords - 1;
        if (lastIndex >= start && lastIndex <= end) {
            end = lastIndex;
        }

        for (int i = start, k = 0; i <= end; i++, k++) {
            ChangeData record = changeData.get(i);
            wrapper.getData().add(record);
        }

        return wrapper;
    }

    public PaginationData<ChangeData> getChanges(String projectName, String entityName, int start, int limit, String sort,
            String dir) {

        // TODO Need to find a way to just retrieve only 'limit' # of records
        // from database

        PaginationData<ChangeData> wrapper = new PaginationData<ChangeData>();
        ArrayList<ChangeData> changeData = (ArrayList<ChangeData>) getChanges(projectName, entityName);

        int totalRecords = changeData.size();
        wrapper.setTotalRecords(totalRecords);

        if (start >= totalRecords) {
            return wrapper;
        }

        int end = start + limit - 1;
        int lastIndex = totalRecords - 1;
        if (lastIndex >= start && lastIndex <= end) {
            end = lastIndex;
        }

        for (int i = start, k = 0; i <= end; i++, k++) {
            ChangeData record = changeData.get(i);
            wrapper.getData().add(record);

        }

        return wrapper;
    }

    public Collection<ChangeData> getChanges(String projectName, String entityName) {
        ArrayList<ChangeData> changeData = new ArrayList<ChangeData>();
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return changeData;
        }
        Frame frame = getProject(projectName).getKnowledgeBase().getFrame(entityName);
        if (frame == null) {
            return changeData; // TODO: rather throw exception?
        }
        return getChangeData(ChAOUtil.getTopLevelChanges(frame));
    }

    protected Collection<ChangeData> getChangeData(Collection<Change> changes) {
        ArrayList<ChangeData> changeData = new ArrayList<ChangeData>();
        if (changes != null) {
            for (Change change : changes) {
                ChangeData data = new ChangeData();
                data.setAuthor(change.getAuthor());
                data.setDescription(getChangeDescription(change.getContext()));
                data.setTimestamp(change.getTimestamp().getDateParsed());
                changeData.add(data);
            }
        }
        return changeData;
    }

    private String getChangeDescription(String text) {
        if (text == null) {
            return "No details";
        }
        int index = text.indexOf(Transaction.APPLY_TO_TRAILER_STRING);
        if (index > 0) {
            return text.substring(0, index);
        }
        return text;
    }

    public Integer getNumChanges(String projectName, Date start, Date end) {
        Integer retval = 0;
        KnowledgeBase changesKB = getChAOKb(projectName);
        if (changesKB == null) {
            return retval;
        }
        TimeIntervalCalculator tic = TimeIntervalCalculator.get(changesKB);
        Collection<Change> changes = tic.getTopLevelChanges(start, end);
        if (changes != null) {
            retval = changes.size();
        }
        return new Integer(retval);
    }

    public PaginationData<ChangeData> getWatchedEntities(String projectName, String userName, int start, int limit,
            String sort, String dir) {
        // TODO Auto-generated method stub
        PaginationData<ChangeData> wrapper = new PaginationData<ChangeData>();
        ArrayList<ChangeData> watchedEntities = (ArrayList<ChangeData>) getWatchedEntities(projectName, userName);

        int totalRecords = watchedEntities.size();
        wrapper.setTotalRecords(totalRecords);

        if (start >= totalRecords) {
            return wrapper;
        }

        int end = start + limit - 1;
        int lastIndex = totalRecords - 1;
        if (lastIndex >= start && lastIndex <= end) {
            end = lastIndex;
        }

        for (int i = start, k = 0; i <= end; i++, k++) {
            ChangeData record = watchedEntities.get(i);
            wrapper.getData().add(record);
        }

        return wrapper;
    }

    /*
     * Watched entities methods
     */

    public Collection<ChangeData> getWatchedEntities(String projectName, String userName) {

        ArrayList<ChangeData> changeData = new ArrayList<ChangeData>();
        if (userName == null) {
            return changeData;
        }
        Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);

        // Get ChAO knowledge base
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return changeData;
        }

        // Get ChAO user
        User user = ServerChangesUtil.getUser(chaoKb, userName);
        if (user == null) {
            return changeData;
        }

        // Get watched entities for ChAO user
        Collection<Ontology_Component> watchedEntities = user.getWatchedEntity();
        for (Ontology_Component entity : watchedEntities) {
            // Get the name and browser text of the entity
            String name = entity.getCurrentName();
            EntityData entityData = null;
            if (name == null) { //delete frame
                entityData = new EntityData("Deleted_entity", "Deleted entity"); //TODO: try to get the deletion name
            } else {
                Frame frame = project.getKnowledgeBase().getFrame(name);
                String browserText = frame.getBrowserText();
                entityData = new EntityData(name, browserText);
            }

            // Get changes for entity
            Collection<Change> changes = entity.getChanges();
            for (Change change : changes) {
                if (change.getPartOfCompositeChange() == null) { // get only top level changes
                    ChangeData data = new ChangeData();
                    data.setEntityData(entityData);
                    data.setAuthor(change.getAuthor());
                    data.setDescription(change.getContext());
                    data.setTimestamp(change.getTimestamp().getDateParsed());
                    changeData.add(data);
                }
            }
            // this is a hack, so that the watched entity portlet can use the
            // same grid as the change history
            if (changes == null || changes.size() == 0) {
                ChangeData data = new ChangeData();
                data.setEntityData(entityData);
                changeData.add(data);
            }
        }

        return changeData;

        /*
         * This code doesn't work, but it needs to be debugged and added to the
         * logic above.
         */
        // last login
        /*
         * RemoteMetaProjectManager manager = (RemoteMetaProjectManager)
         * ProjectManagerFactory.getProtege3ProjectManager().getMetaProjectManager(); Date date
         * = manager.getLastLogin(projectName, userName);
         */
    }

    public EntityData addWatchedEntity(String projectName, String userName, String watchedEntityName) {
        if (userName == null) {
            return null;
        }
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return null;
        }
        OntologyComponentFactory factory = new OntologyComponentFactory(chaoKb);
        User user = ServerChangesUtil.getUser(chaoKb, userName);
        if (user == null) {
            user = factory.createUser(null);
            user.setName(userName);
        }
        Frame frame = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName).getKnowledgeBase().getFrame(
                watchedEntityName);
        Ontology_Component oc = ServerChangesUtil.getOntologyComponent(frame, true);
        user.addWatchedEntity(oc);

        return OntologyServiceImpl.createEntityData(frame, true);
    }

   public EntityData addWatchedBranch(String projectName, String userName, String watchedEntityName) {
        if (userName == null) {
            return null;
        }
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return null;
        }
        OntologyComponentFactory factory = new OntologyComponentFactory(chaoKb);
        User user = ServerChangesUtil.getUser(chaoKb, userName);
        if (user == null) {
            user = factory.createUser(null);
            user.setName(userName);
        }
        Frame frame = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName).getKnowledgeBase().getFrame(
                watchedEntityName);
        Ontology_Component oc = ServerChangesUtil.getOntologyComponent(frame, true);
       user.addWatchedBranch(oc);

        return OntologyServiceImpl.createEntityData(frame, true);
    }

    public void removeWatchedEntity(String projectName, String userName, String watchedEntityName) {
        if (userName == null) {
            return;
        }
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return;
        }
        User user = ServerChangesUtil.getUser(chaoKb, userName);
        if (user == null) {
            return;
        }
        Frame frame = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName).getKnowledgeBase().getFrame(
                watchedEntityName);
        Ontology_Component oc = ServerChangesUtil.getOntologyComponent(frame, false);
        if (oc != null) {
            user.removeWatchedEntity(oc);
        }
    }

    /*
     * Review methods
     */

    public ArrayList<String> getReviewers(String projectName) {
        ArrayList<String> list = new ArrayList<String>();
        KnowledgeBase chaoKb = getChAOKb(projectName);

        // Shortcut
        if (chaoKb == null) {
            return null;
        }

        OntologyComponentFactory factory = new OntologyComponentFactory(chaoKb);
        Collection<Reviewer> reviewers = factory.getAllReviewerObjects(true);
        for (Reviewer reviewer : reviewers) {
            list.add(reviewer.getName());
        }

        return list;
    }

    public ArrayList<ReviewData> getReviews(String projectName, String entityName) {
        ArrayList<ReviewData> reviews = new ArrayList<ReviewData>();

        // Shortcut
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return null;
        }

        Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
        Frame frame = project.getKnowledgeBase().getFrame(entityName);
        Ontology_Component oc = ServerChangesUtil.getOntologyComponent(frame, true);

        Collection<Annotation> annotations = oc.getAssociatedAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation.canAs(Review.class)) {
                ReviewData data = new ReviewData();
                data.setAuthor(annotation.getAuthor());
                data.setBody(annotation.getBody());

                Timestamp timestamp = annotation.getCreated();
                if (timestamp != null) {
                    Date date = timestamp.getDateParsed();
                    data.setCreated(date);
                }

                data.setSubject(annotation.getSubject());
                reviews.add(data);
            }
        }

        return reviews;
    }

    public void requestReview(String projectName, String entityName, List<String> reviewerNames) {
        // Shortcut
        if (reviewerNames == null) {
            return;

        }

        // Shortcut
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return;
        }

        for (String name : reviewerNames) {
            Reviewer reviewer = (Reviewer) ServerChangesUtil.getUser(chaoKb, name);
            Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);
            Frame frame = project.getKnowledgeBase().getFrame(entityName);
            Ontology_Component oc = ServerChangesUtil.getOntologyComponent(frame, true);
            reviewer.addPendingReview(oc);
        }
    }

    public void addReview(String projectName, String userName, NotesData data) {
        // Shortcut
        KnowledgeBase chaoKb = getChAOKb(projectName);
        if (chaoKb == null) {
            return;
        }

        Project project = ProjectManagerFactory.getProtege3ProjectManager().getProject(projectName);

        // Get the entity in the ontology that we want to annotate.
        String name = data.getAnnotatedEntity().getName();
        Frame frame = project.getKnowledgeBase().getFrame(name);

        // Create a new annotation (type "Review")
        Cls annotationCls = chaoKb.getCls(data.getType());
        Annotation annotation = ChAOUtil.createAnnotationOnAnnotation(project.getKnowledgeBase(), frame, annotationCls);
        annotation.setAuthor(data.getAuthor());
        annotation.setSubject(data.getSubject());
        annotation.setBody(data.getBody());
        annotation.setCreated(DefaultTimestamp.getTimestamp(chaoKb));

        /*
         * Now that review for this entity is added/completed, remove it as
         * pending from current user.
         */
        User user = ServerChangesUtil.getUser(chaoKb, userName);
        if (user != null) { // Need to check for this right? Because user might
            // not be in chao ontology?
            if (user instanceof Reviewer) {
                // Get the entity from the changes & annotations ontology.
                Ontology_Component oc = ServerChangesUtil.getOntologyComponent(frame, true);
                ((Reviewer) user).removePendingReview(oc);
            }
        }
    }

}
