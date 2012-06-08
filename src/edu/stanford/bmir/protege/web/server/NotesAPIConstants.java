package edu.stanford.bmir.protege.web.server;

import java.io.File;

import org.semanticweb.owlapi.model.IRI;

public class NotesAPIConstants {

    public static String CHAO_DOCUMENT_IRI = IRI.create(new File("resources/changes.owl")).toString();
    public static String NOTESKB_ONTOLOGY_PREFIX = "http://protege.stanford.edu/noteskb/";
    public static String NOTESKB_DOCUMENT_PREFIX = "projects/";


    public static String getChAODocumentIRI() {
        if(CHAO_DOCUMENT_IRI == null){
            CHAO_DOCUMENT_IRI = IRI.create(new File("resources/changes.owl")).toString();
        }
        return CHAO_DOCUMENT_IRI;
    }

    public static String getNotesKbOntologyIRI(String projectName) {
        String key = getKeyFromProjectName(projectName);
        return NOTESKB_ONTOLOGY_PREFIX+key;
    }

    public static String getNotesKbDocumentIRI(String projectName) {
        String key = getKeyFromProjectName(projectName);
        return IRI.create(new File("projects/ChAO_annotation_" + key + ".owl")).toString();
    }

    public static String getKeyFromProjectName(String projectName){
        String key = projectName.trim().replaceAll("\\W", "_");
        return key;
    }
}
