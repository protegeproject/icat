package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

public interface ICDServiceAsync {

    void createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String user,
            String operationDescription, String reasonForChange, AsyncCallback<EntityData> cb);

}
