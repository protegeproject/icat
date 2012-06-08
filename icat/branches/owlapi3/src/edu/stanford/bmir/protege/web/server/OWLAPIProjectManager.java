package edu.stanford.bmir.protege.web.server;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.stanford.smi.protege.server.metaproject.ProjectInstance;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protege.util.URIUtilities;

//TODO: Jack, please check if it needs syncronization
public class OWLAPIProjectManager extends AbstractProjectManager<OWLOntology> implements ProjectManager {

    private static OWLAPIProjectManager owlApiProjectManager;

    private OWLOntologyManager owlOntologyManager;

    private OWLAPIProjectManager() {
        owlOntologyManager = OWLManager.createOWLOntologyManager();
    }

    public static OWLAPIProjectManager getProjectManager() {
        if (owlApiProjectManager == null) {
            owlApiProjectManager = new OWLAPIProjectManager();
        }
        return owlApiProjectManager;
    }

    public OWLOntologyManager getOwlOntologyManager() {
        return owlOntologyManager;
    }

    @Override
    protected void ensureProjectOpen(String projectName, ServerProject<OWLOntology> serverProject) {
        synchronized (serverProject) {
            if (serverProject.getProject() == null) {
                File file = new File(getProjectURI(projectName));
                OWLOntology onto = null;
                try {
                    owlOntologyManager.setSilentMissingImportsHandling(true);
                    onto = owlOntologyManager.loadOntologyFromOntologyDocument(file);
                } catch (OWLOntologyCreationException e) {
                    e.printStackTrace(); //TODO: use logger
                }

                if (onto == null) {
                    throw new RuntimeException("Cannot open project " + projectName);
                }
                //TODO: load also ChAO KB if available - use the NotesAPI
                serverProject.setProject(onto);
                serverProject.setProjectName(projectName) ;
            }
        }
    }

    //copied from LocalMetaProjectManager
    public URI getProjectURI(String projectName) {
        for (ProjectInstance projectInstance : getMetaProjectManager().getMetaProject().getProjects()) {
            String name = projectInstance.getName();
            if (name.equals(projectName)) {
                String path = projectInstance.getLocation();
                URL url = URIUtilities.toURL(path, ApplicationProperties.getWeprotegeDirectory());
                URI uri = null;
                try {
                    uri = url.toURI();
                } catch (URISyntaxException e) {
                    Log.getLogger().log(Level.SEVERE,
                            "Error at getting path for project " + projectName + ". Computed path: " + url, e);
                }
                return uri;
            }
        }
        return null;
    }


    @Override
    public void dispose() {
        // TODO Auto-generated method stub
    }

}
