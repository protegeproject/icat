package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protegex.chao.ChAOKbManager;
import edu.stanford.bmir.protegex.chao.annotation.api.AnnotationFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.OntologyComponentFactory;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.User;
import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseAdapter;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class WatchedEntitiesCache {
    private static Map<String, List<User>> watchedBranches = new HashMap<String, List<User>>();
    private static Map<String, Map<String, Set<String>>> projectsToUsersToBranches = new HashMap<String, Map<String, Set<String>>>();
    private static Map<String, List<User>> watchedEntities = new HashMap<String, List<User>>();
    private static Map<String, Map<String, Set<String>>> projectsToUsersToEntities = new HashMap<String, Map<String, Set<String>>>();
    private static ReadWriteLock lock = new ReentrantReadWriteLock();

    public static void init(final ServerProject<Project> project) {
        try {
            lock.writeLock().lock();
            KnowledgeBase kb = project.getProject().getKnowledgeBase();
            final KnowledgeBase changesKb = ChAOKbManager.getChAOKb(kb);

            if (changesKb == null) {
                return;
            }

            OntologyComponentFactory factory = new OntologyComponentFactory(changesKb);
            final Collection<User> allUserObjects = factory
                    .getAllUserObjects();
            for (User user : allUserObjects) {
                if (user.getWatchedBranch() != null || !user.getWatchedBranch().isEmpty()) {
                    final Collection<Ontology_Component> branches = user.getWatchedBranch();
                    for (Ontology_Component branch : branches) {
                        List<User> users = watchedBranches.get(branch.getCurrentName());
                        if (users == null) {
                            users = new ArrayList<User>();
                        }
                        users.add(user);
                        watchedBranches.put(branch.getCurrentName(), users);
                    }
                }
                if (user.getWatchedEntity() != null || !user.getWatchedEntity().isEmpty()) {
                    final Collection<Ontology_Component> entities = user.getWatchedEntity();
                    for (Ontology_Component entity : entities) {
                        List<User> users = watchedEntities.get(entity.getCurrentName());
                        if (users == null) {
                            users = new ArrayList<User>();
                        }
                        users.add(user);
                        watchedEntities.put(entity.getCurrentName(), users);
                    }
                }
            }
            projectsToUsersToBranches.put(project.getProjectName(), new HashMap<String, Set<String>>());
            projectsToUsersToEntities.put(project.getProjectName(), new HashMap<String, Set<String>>());
            buildInverseMap(project.getProjectName(), projectsToUsersToBranches, watchedBranches);
            buildInverseMap(project.getProjectName(), projectsToUsersToEntities, watchedEntities);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void buildInverseMap(String projectName, Map<String, Map<String, Set<String>>> projects,
                                        final Map<String, List<User>> originalWatches) {
        Map<String, Set<String>> map = projects.get(projectName);
        if (map == null) {
            map = new HashMap<String, Set<String>>();
        }
        for (Map.Entry<String, List<User>> stringListEntry : originalWatches.entrySet()) {
            for (User user : stringListEntry.getValue()) {

                Set<String> entities = map.get(user.getName());
                if (entities == null) {
                    entities = new HashSet<String>();
                }
                entities.add(stringListEntry.getKey());
                map.put(user.getName(), entities);
            }
        }
        projects.put(projectName, map);
    }

    public static Map<String, List<User>> getWatchedBranches() {
        try {
            lock.readLock().lock();
            final HashMap<String, List<User>> collector = new HashMap<String, List<User>>();
            for (Map.Entry<String, List<User>> entry : watchedBranches.entrySet()) {
                if (entry.getValue() != null){
                    collector.put(entry.getKey(), new ArrayList<User>(entry.getValue()));
                }
            }
            return collector;
        } finally {
            lock.readLock().unlock();
        }
    }

    public static void addEntityWatch(final String projectName, String key, User value) {
        try {
            lock.writeLock().lock();
            List<User> users = watchedEntities.get(key);
            if (users == null) {
                users = new ArrayList<User>();
            }
            users.add(value);
            watchedEntities.put(key, users);
            addToProjectsMap(key, value, projectName, projectsToUsersToEntities);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void addBranchWatch(final String projectName, String branchToWatch, User value) {
        try {
            lock.writeLock().lock();
            List<User> users = watchedBranches.get(branchToWatch);
            if (users == null) {
                users = new ArrayList<User>();
            }
            users.add(value);
            watchedBranches.put(branchToWatch, users);
            addToProjectsMap(branchToWatch, value, projectName, projectsToUsersToBranches);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void addToProjectsMap(String key, User value, String projectName,
                                         final Map<String, Map<String, Set<String>>> projectsMap) {
        Map<String, Set<String>> map = projectsMap.get(projectName);
        if (map == null) {
            map = new HashMap<String, Set<String>>();
        }
        Set<String> branches = map.get(value.getName());
        if (branches == null) {
            branches = new HashSet<String>();
        }
        branches.add(key);
        map.put(value.getName(), branches);
        projectsMap.put(projectName, map);
    }

    private static void removeFromProjectsMap(String key, User value, String projectName,
                                              final Map<String, Map<String, Set<String>>> projectsMap) {
        Map<String, Set<String>> map = projectsMap.get(projectName);
        if (map == null) {
            return;
        }
        Set<String> branches = map.get(value.getName());
        if (branches == null) {
            return;
        }
        branches.remove(key);
        map.put(value.getName(), branches);
        projectsMap.put(projectName, map);
    }

    public static void removeBranchWatch(final String projectName, String key, User value) {
        try {
            lock.writeLock().lock();
            List<User> users = watchedBranches.get(key);
            if (users == null || users.isEmpty()) {
                return;
            }
            users.remove(value);
            removeFromProjectsMap(key, value, projectName, projectsToUsersToBranches);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void removeEntityWatch(final String projectName, String key, User value) {
        try {
            lock.writeLock().lock();
            List<User> users = watchedEntities.get(key);
            if (users == null || users.isEmpty()) {
                return;
            }
            users.remove(value);
            removeFromProjectsMap(key, value, projectName, projectsToUsersToEntities);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static void registerListeners(Project project) {
        try {
            lock.writeLock().lock();
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
                chaoKb.addFrameListener(new FrameAdapter() {
                    @Override
                    public void ownSlotValueChanged(FrameEvent event) {
                        if (!event.getSlot().getName().equals("watchedEntity")
                                && !event.getSlot().getName().equals("watchedBranch")) {
                            return;
                        }
                    }
                });
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    static void purgeCache() {
        try {
            lock.writeLock().lock();
            watchedBranches = new HashMap<String, List<User>>();
            projectsToUsersToBranches = new HashMap<String, Map<String, Set<String>>>();
            watchedEntities = new HashMap<String, List<User>>();
            projectsToUsersToEntities = new HashMap<String, Map<String, Set<String>>>();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean isWatchedEntity(String projectName, String userName, String entityName){
        try {
            lock.readLock().lock();
            final Map<String, Set<String>> usersToEntities = projectsToUsersToEntities.get(projectName);
            if (usersToEntities == null ){
                return false;
            }
            final Set<String> watches = usersToEntities.get(userName);
            return watches != null && watches.contains(entityName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public static boolean isWatchedBranch(String projectName, String userName, String entityName){
        try {
            lock.readLock().lock();
            final Map<String, Set<String>> usersToEntities = projectsToUsersToBranches.get(projectName);
            if (usersToEntities == null ){
                return false;
            }
            final Set<String> watches = usersToEntities.get(userName);
            return watches != null && watches.contains(entityName);
        } finally {
            lock.readLock().unlock();
        }
    }

}