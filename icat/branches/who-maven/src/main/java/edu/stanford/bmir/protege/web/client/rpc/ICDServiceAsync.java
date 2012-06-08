package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;

public interface ICDServiceAsync {

    void createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String user,
            String operationDescription, String reasonForChange, AsyncCallback<EntityData> cb);

    void getEntityPropertyValuesForLinearization(String projectName, List<String> entities, List<String> properties, 
            List<String> reifiedProps, AsyncCallback<List<EntityPropertyValues>> cb);

}
