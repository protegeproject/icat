package edu.stanford.bmir.protege.web.server;

import java.util.HashMap;
import java.util.Map;

import org.protege.notesapi.NotesException;
import org.protege.notesapi.NotesManager;
import org.protege.notesapi.NotesManagerPool;
import org.protege.notesapi.db.DatabaseBackedOntologyHandler;
import org.protege.notesapi.db.DatabaseBackedOntologyHandlerPool;
import org.semanticweb.owlapi.model.IRI;

public class NotesAPIUtil {

    public static Map<String, NotesManager> notesManagerMap = new HashMap<String, NotesManager>();

    public static NotesManager getNotesManagerForDB(String projectName){
        
        String key = NotesAPIConstants.getKeyFromProjectName(projectName);
        
        NotesManager notesManager = null;
        notesManager = notesManagerMap.get(key);
        DatabaseBackedOntologyHandler dbOntologyHandler = DatabaseBackedOntologyHandlerPool.mysqlDbBackedOntologyHandler_localhost;
        
        if(notesManager == null && !notesManagerMap.containsKey(key)){
            NotesManagerPool notesManagerPool = new NotesManagerPool();
            notesManagerPool.setChaoDocumentIRI(NotesAPIConstants.getChAODocumentIRI());
            
            String projectNameKey = NotesAPIConstants.getKeyFromProjectName(projectName);
            String namespace = NotesAPIConstants.getNotesKbOntologyIRI(projectNameKey);
            
            try {
                notesManager = notesManagerPool.getNotesManagerForDatabase(dbOntologyHandler, projectNameKey, IRI.create(namespace));
            } catch (NotesException e) {
                e.printStackTrace();
            }
            notesManagerMap.put(key, notesManager);
        }
        
        return notesManager;
    }
    
    
    public static NotesManager getNotesManager(String projectName){
        
        //Uncomment this line for using a DB backend
        //return getNotesManagerForDB(projectName);
        return getNotesManagerForFile(projectName);
    }
    
    
    private static NotesManager getNotesManagerForFile(String projectName){
        String key = NotesAPIConstants.getKeyFromProjectName(projectName);
        
        NotesManager notesManager = null;
        notesManager = notesManagerMap.get(key);
        
        if(notesManager == null && !notesManagerMap.containsKey(key)){
            NotesManagerPool notesManagerPool = new NotesManagerPool();
            notesManagerPool.setChaoDocumentIRI(NotesAPIConstants.getChAODocumentIRI());
            
            String notesKbOntologyIRI = NotesAPIConstants.getNotesKbOntologyIRI(projectName);
            String notesKbDocumentIRI = NotesAPIConstants.getNotesKbDocumentIRI(projectName);
            
            try {
                notesManager = notesManagerPool.getNotesManagerForFile(notesKbOntologyIRI, notesKbDocumentIRI);
                notesManager.getAllNotes();
            } catch (NotesException e) {
                e.printStackTrace();
            }
            notesManagerMap.put(key, notesManager);
        }
        
        return notesManager;
    }


}
