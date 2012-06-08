package edu.stanford.bmir.protege.web.server;

import edu.stanford.smi.protege.collab.util.HasAnnotationCache;
import edu.stanford.smi.protege.model.Project;

/**
 * Main class for managing Protege 3 projects on the server side. It has support for:
 * <ul>
 * <li> loading a local or remote project</li>
 * <li> get the remote Protege server </li>
 * <li> caches opened projects </li>
 * <li> has a thread for automatically saving local projects at a set interval </li>
 * </ul>
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 */
public class Protege3ProjectManager extends AbstractProjectManager<Project> implements ProjectManager {

    private static Protege3ProjectManager projectManager;

    private Protege3ProjectManager() {
        super();
    }

    public static synchronized Protege3ProjectManager getProjectManager() {
        if (projectManager == null) {
            projectManager = new Protege3ProjectManager();
        }
        return projectManager;
    }


    @Override
    protected void ensureProjectOpen(String projectName, ServerProject<Project> serverProject) {
        synchronized (serverProject) {
            if (serverProject.getProject() == null) {
                Project project = getMetaProjectManager().openProject(projectName);
                if (project == null) {
                    throw new RuntimeException("Cannot open project " + projectName);
                }
                //load also ChAO KB if available

                HasAnnotationCache.fillHasAnnotationCache(project.getKnowledgeBase());
                serverProject.setProject(project);
                serverProject.setProjectName(projectName);

                //TODO: Jack, please refactor
                //TODO: this should be refactored to a different class
                /*
                final KnowledgeBase chaoKb = ChAOKbManager.getChAOKb(project.getKnowledgeBase());
                if (chaoKb != null) {
                    final AnnotationFactory annotationFactory = new AnnotationFactory(chaoKb);
                    chaoKb.addKnowledgeBaseListener(new KnowledgeBaseAdapter() {
                        @Override
                        public void instanceDeleted(KnowledgeBaseEvent event) {
                            Cls annotationCls = annotationFactory.getAnnotationClass();
                            Instance inst = (Instance) event.getFrame();
                            if (!inst.hasType(annotationCls)) {
                                return;
                            }
                            AnnotationCache.removeAnnotation(event.getFrame().getProject().getName(), inst);
                        }
                    });
                    chaoKb.addFrameListener(new FrameAdapter(){
                        @Override
                        public void ownSlotValueChanged(FrameEvent event) {
                            if (!event.getSlot().equals(annotationFactory.getAnnotatesSlot())){
                                return;
                            }
                            Cls annotationCls = annotationFactory.getAnnotationClass();

                            Instance inst = (Instance) event.getFrame();
                            if (!inst.hasType(annotationCls)) {
                                return;
                            }

                            AnnotationCache.addAnnotation(event.getFrame().getProject().getName(), inst);
                        }
                    });
                }
                */
            }
        }
    }

}
