package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;

import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;

@RemoteServiceRelativePath("icd")
public interface ICDService extends WHOFICService  {

    public EntityData createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String sortingLabel,
            boolean createICDSpecificEntities, String user, String operationDescription, String reasonForChange);

    public String exportICDBranch(String projectName, String topNode, String userName);


}
