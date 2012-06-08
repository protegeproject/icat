package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;

public class ICDServiceManager {

    private static ICDServiceAsync proxy;
    static ICDServiceManager instance;

    public static ICDServiceManager getInstance() {
        if (instance == null) {
            instance = new ICDServiceManager();
        }
        return instance;
    }

    private ICDServiceManager() {
        proxy = (ICDServiceAsync) GWT.create(ICDService.class);
    }

    public void createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String sortingLabel,
               boolean createICDSpecificEntities, String user, String operationDescription, String reasonForChange, AsyncCallback<EntityData> cb) {
        proxy.createICDCls(projectName, clsName, superClsNames, title, sortingLabel, createICDSpecificEntities, user, operationDescription, reasonForChange, cb);
    }

    public void getEntityPropertyValuesForLinearization(String projectName, List<String> entities, List<String> properties,
            List<String> reifiedProps, AsyncCallback<List<EntityPropertyValues>> cb) {
        proxy.getEntityPropertyValuesForLinearization(projectName, entities, properties, reifiedProps, cb);
    }

    public void exportICDBranch(String projectName, String topNode, String userName,  AsyncCallback<String> cb){
        proxy.exportICDBranch(projectName, topNode, userName, cb);
    }

}
