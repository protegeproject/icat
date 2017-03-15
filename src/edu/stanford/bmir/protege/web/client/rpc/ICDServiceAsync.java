package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public interface ICDServiceAsync extends WHOFICServiceAsync {

    void createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String sortingLabel,
            boolean createICDSpecificEntities, String user, String operationDescription, String reasonForChange, AsyncCallback<EntityData> cb);

    void exportICDBranch(String projectName, String topNode, String userName, AsyncCallback<String> cb);


}
