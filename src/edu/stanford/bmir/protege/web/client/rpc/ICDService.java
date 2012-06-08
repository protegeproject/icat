package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

@RemoteServiceRelativePath("icd")
public interface ICDService extends RemoteService  {

    public EntityData createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String user,
            String operationDescription, String reasonForChange);

}
