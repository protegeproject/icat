package edu.stanford.bmir.protege.web.server;

import edu.stanford.bmir.protegex.chao.annotation.api.Annotation;
import edu.stanford.bmir.protegex.chao.annotation.api.impl.DefaultAnnotation;
import edu.stanford.bmir.protegex.chao.ontologycomp.api.Ontology_Component;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protegex.server_changes.ServerChangesUtil;

import java.util.*;

public class AnnotationCache {
    private static Map<String, Map<Instance, Date>> projectToAnnotation = new HashMap<String, Map<Instance, Date>>();
    private static Map<Instance, Ontology_Component> annotationToRootNode = new HashMap<Instance, Ontology_Component>();

    public static synchronized void removeAnnotation(String name, Instance inst) {
        Map<Instance, Date> map = projectToAnnotation.get(name);
        if (map == null) {
            map = new HashMap<Instance, Date>();
        }
        map.remove(inst);
        projectToAnnotation.put(name, map);
    }

    public static synchronized void removeAnnotations(String name, Collection<Instance> inst) {
        Map<Instance, Date> map = projectToAnnotation.get(name);
        if (map == null) {
            map = new HashMap<Instance, Date>();
        }
        for (Instance instance : inst) {
            map.remove(instance);
            annotationToRootNode.remove(instance);
        }
        projectToAnnotation.put(name, map);
    }

    public static synchronized void addAnnotation(String name, final Instance inst) {
        Map<Instance, Date> map = projectToAnnotation.get(name);
        if (map == null) {
            map = new HashMap<Instance, Date>();
        }
        map.remove(inst);
        map.put(inst, new Date());
        projectToAnnotation.put(name, map);
        Annotation annotation = new DefaultAnnotation(inst);
        final Collection<Ontology_Component> components = ServerChangesUtil.getAnnotatedOntologyComponents(annotation);
        for (Ontology_Component component : components) {
            annotationToRootNode.put(inst, component);
        }
    }

    public static synchronized Ontology_Component getRootNode(Instance instance) {
        return annotationToRootNode.get(instance);
    }

    public static synchronized Date getChangeDate(String projectName, Instance instance) {
        return projectToAnnotation.get(projectName).get(instance);
    }

    public static synchronized Collection<Instance> getAnnotations(String project, Date from, Date to) {
        final Map<Instance, Date> instances = projectToAnnotation.get(project);
        if (instances == null) {
            return new ArrayList<Instance>();
        }
        Collection<Instance> results = new ArrayList<Instance>();
        for (Map.Entry<Instance, Date> instanceDateEntry : instances.entrySet()) {
            if (instanceDateEntry.getValue().after(from) && instanceDateEntry.getValue().before(to)) {
                results.add(instanceDateEntry.getKey());
            }
        }
        return results;
    }

    public static synchronized void purge(Date from, Date to) {
        for (Map.Entry<String, Map<Instance, Date>> projectsToInstances : projectToAnnotation.entrySet()) {
            final Map<Instance, Date> instances = projectsToInstances.getValue();
            if (instances == null) {
                continue;
            }
            Collection<Instance> results = new ArrayList<Instance>();
            for (Map.Entry<Instance, Date> instanceDateEntry : instances.entrySet()) {
                if (instanceDateEntry.getValue().after(from) && instanceDateEntry.getValue().before(to)) {
                    results.add(instanceDateEntry.getKey());
                }
            }
            removeAnnotations(projectsToInstances.getKey(), results);
        }
    }
}