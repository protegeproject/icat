package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public class ICDServiceManager {

    private static ICDServiceAsync proxy;
    private static ICDServiceManager instance;

    public static ICDServiceManager getInstance() {
        if (instance == null) {
            instance = new ICDServiceManager();
        }
        return instance;
    }

    private ICDServiceManager() {
        proxy = (ICDServiceAsync) GWT.create(ICDService.class);
    }

    public void createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String user,
            String operationDescription, String reasonForChange, AsyncCallback<EntityData> cb) {
        proxy.createICDCls(projectName, clsName, superClsNames, title, user, operationDescription, reasonForChange, cb);
    }

}
