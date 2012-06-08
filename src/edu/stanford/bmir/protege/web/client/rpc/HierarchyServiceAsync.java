package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HierarchyServiceAsync {

    void changeParent(String project, String className, Collection<String> parentsToAdd,
            Collection<String> parentsToRemove, String user, String operationDescription, String reasonForChange,
            AsyncCallback<Void> callback);

    void retireClasses(String project, Collection<String> classesToRetireNames, boolean retireChildren,
            String newParent, String reasonForChange, String operationDescription, String user,
            AsyncCallback<Void> callback);

}
