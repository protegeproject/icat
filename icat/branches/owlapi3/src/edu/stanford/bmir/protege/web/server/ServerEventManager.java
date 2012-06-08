package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protege.web.client.model.event.*;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.event.*;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.CollectionUtilities;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.FilteringOWLOntologyChangeListener;

import java.util.*;

public class ServerEventManager {
    static OWLOntologyManager owlOntologyManager;


    /*
     * TODO: this is a memory leak: we keep all the change events
     * since the server was started.. this set can get veeery big.
     * We need to serialize it somehow.
     */
    private List<OntologyEvent> events;
    private ServerProject<Project> serverProject;
    private FilteringOWLOntologyChangeListener changeListener;

    /*
     * TODO: commenting out the frame listener for now -> no widget uses it so far
     * and it generates too many events and the clients do not need them for now.
     * Think about an alternative way of handling property value events - they are too verbose..
     */

    // TODO: We should be able to start with a different version number
    public ServerEventManager(ServerProject serverProject) {
        this.serverProject = serverProject;
        this.events = new ArrayList<OntologyEvent>();
        owlOntologyManager = ProjectManagerFactory.getOWLAPIProjectManager().getOwlOntologyManager();
        createListeners();
        addListeners();
    }

    private void createListeners() {

        if (changeListener == null){
        changeListener = new FilteringOWLOntologyChangeListener() {
            @Override
            public void visit(OWLSubClassOfAxiom axiom) {
                if (isAdd() ) {
                    events.add(createEvent(axiom, EventType.SUBCLASS_ADDED));
                }
            }

        };
        owlOntologyManager.addOntologyChangeListener(changeListener);
        }
    }

    private OWLOntology getOntology(String project) {
        OWLOntology ont = ProjectManagerFactory.getOWLAPIProjectManager().getProject(project);
        return ont;
    }

    public void startListening() {

    }

    private void addListeners() {


    }

    public int getServerRevision() {
        return events.size();
    }

    public ArrayList<OntologyEvent> getEvents(long fromVersion) {
        return getEvents(fromVersion, events.size());
    }

    public ArrayList<OntologyEvent> getEvents(long fromVersion, long toVersion) {
        ArrayList<OntologyEvent> fromToEvents = new ArrayList<OntologyEvent>();

        //TODO: check these conditions
        if (fromVersion < 0) {
            fromVersion = 0;
        }
        if (toVersion > events.size()) {
            toVersion = events.size();
        }
        for (long i = fromVersion; i < toVersion; i++) {
            fromToEvents.add(events.get((int) i)); //fishy
        }

        //Log.getLogger().info("SERVER: GetEvents from: " + fromVersion + " to: " + toVersion + " events size: " + events.size() + " Events: " + fromToEvents);

        return fromToEvents.size() == 0 ? null : fromToEvents;
    }

    public void dispose() {
        owlOntologyManager.removeOntologyChangeListener(this.changeListener);
    }


    private OntologyEvent createEvent(OWLSubClassOfAxiom event, int type) {
        String name = event.getSuperClass().asOWLClass().getIRI().toString();
        if(name == null){
            name = event.getSuperClass().asOWLClass().getIRI().toString(); 
        }
        EntityData superClass = new EntityData(name, event.toString());
        EntityData subClass = new EntityData(event.getSubClass().asOWLClass().getIRI().toString(), event.getSubClass().asOWLClass().getIRI().toString());
        return new EntityCreateEvent(superClass, type, "getUser not supported by owl ontologies", Arrays.asList(subClass), events.size());
    }


}
