package edu.stanford.bmir.protege.web.client.ui.generated;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.ui.ClientApplicationPropertiesCache;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDClassTreePortlet;
import edu.stanford.bmir.protege.web.client.ui.ontology.classes.ClassTreePortlet;


public class ClassTreeFactory {

    public static  final String DEFAULT_CLASS_TREE_PROP = "default.class.tree";

    public static ClassTreePortlet getDefaultClassTreePortlet(final Project project, final boolean showToolbar, final boolean showTitle,
            final boolean showTools, final boolean allowsMultiSelection, final String topClass) {

        String defaultClassName = getDefaultClassTreeName();
        if (defaultClassName.equals(ClassTreePortlet.class.getName())) {
            return getClassTreePortlet(project, showToolbar, showTitle, showTools, allowsMultiSelection, topClass);
        } else if (defaultClassName.equals(ICDClassTreePortlet.class.getName())) {
            return getICDClassTreePortlet(project, showToolbar, showTitle, showTools, allowsMultiSelection, topClass);
        }

        return null;
    }

    public static ClassTreePortlet getClassTreePortlet(final Project project, final boolean showToolbar, final boolean showTitle,
            final boolean showTools, final boolean allowsMultiSelection, final String topClass) {
        return new ClassTreePortlet(project, showToolbar, showTitle, showTools, allowsMultiSelection, topClass);
    }

    public static ICDClassTreePortlet getICDClassTreePortlet(final Project project, final boolean showToolbar, final boolean showTitle,
            final boolean showTools, final boolean allowsMultiSelection, final String topClass) {
        return new ICDClassTreePortlet(project, showToolbar, showTitle, showTools, allowsMultiSelection, topClass);
    }

    private static String getDefaultClassTreeName() {
        return ClientApplicationPropertiesCache.getStringProperty(DEFAULT_CLASS_TREE_PROP, ClassTreePortlet.class.getName());
    }

}
