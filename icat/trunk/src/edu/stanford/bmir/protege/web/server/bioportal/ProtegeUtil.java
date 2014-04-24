package edu.stanford.bmir.protege.web.server.bioportal;

import java.util.Collection;
import java.util.Iterator;

import org.ncbo.stanford.util.HTMLUtil;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protegex.owl.model.OWLModel;

public class ProtegeUtil {

    public static String getDisplayText(Object value) {
        if (value instanceof Frame) {
            return ((Frame) value).getBrowserText();
        } else {
            return HTMLUtil.makeHTMLLinks(value.toString());
        }
    }

    public static String getDisplayText(Object value, int maxLength) {
        if (value instanceof Frame) {
            return ((Frame) value).getBrowserText();
        } else {
            return HTMLUtil.makeHTMLLinks(value.toString(), maxLength);
        }
    }

    public static String getDisplayText(Collection<?> values) {
        StringBuffer buffer = new StringBuffer();
        if (values == null) {
            return buffer.toString();
        }
        for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            buffer.append(getDisplayText(object));
            buffer.append(", ");
        }
        if (buffer.length() > 2) {
            return buffer.substring(0, buffer.length() - 2);
        }
        return buffer.toString();
    }

    public static String getDisplayText(Collection<?> values, int maxLength) {
        StringBuffer buffer = new StringBuffer();
        if (values == null) {
            return buffer.toString();
        }
        for (Iterator<?> iterator = values.iterator(); iterator.hasNext();) {
            Object object = iterator.next();
            buffer.append(getDisplayText(object, maxLength));
            buffer.append(", ");
        }
        if (buffer.length() > 2) {
            return buffer.substring(0, buffer.length() - 2);
        }
        return buffer.toString();
    }

    public static String fixNamespace(OWLModel owlModel, String className) {
        if (owlModel == null || className == null) {
            return className;
        }
        if (owlModel.getNamespaceManager().getDefaultNamespace() == null && (!className.contains(":"))
                && (!className.contains("#"))) {
            return owlModel.getDefaultOWLOntology().getName() + "#" + className;
        } else {
            return className;
        }
    }
}
