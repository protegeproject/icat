package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;

@RemoteServiceRelativePath("icd")
public interface ICDService extends RemoteService  {

    public EntityData createICDCls(String projectName, String clsName, Collection<String> superClsNames, String title, String sortingLabel,
            boolean createICDSpecificEntities, String user, String operationDescription, String reasonForChange);

    public List<EntityPropertyValues> getEntityPropertyValuesForLinearization(String projectName, List<String> entities, List<String> properties,
            List<String> reifiedProps);

    public String exportICDBranch(String projectName, String topNode, String userName);

    public List<EntityPropertyValues> getSecondaryAndInheritedTags(String projectName, String clsName);

    public List<SubclassEntityData> getSubclasses(String projectName, String className);

    public EntityPropertyValues changeIndexType(String projectName, String subject, String indexEntity,
            List<String> reifiedProps, String indexType);

    public EntityPropertyValues changeInclusionFlagForIndex(String projectName, String subject, String indexEntity,
            List<String> reifiedProps, boolean isInclusionFlag);

    public void removeBaseIndexTerm(String projectName, String entityName,
            String value, String user, String operationDescription);

    public List<String> getListOfSelectedPostCoordinationAxes(
    		String projectName, String entity, List<String> reifiedProps);

	public List<EntityPropertyValues> getEntityPropertyValuesForPostCoordinationAxes(
			String projectName, List<String> entities, List<String> properties,
			List<String> reifiedProps);

    public boolean addAllowedPostCoordinationAxis(String projectName, String subject,
   		 	String postcoordinationEntity, String postcoordinationProperty, boolean isRequiredFlag);

    public boolean removeAllowedPostCoordinationAxis(String projectName, String subject,
    		String postcoordinationEntity, String postcoordinationProperty);

	public List<ScaleInfoData> getPostCoordinationAxesScales(String projectName,
			List<String> properties);

    public boolean reorderSiblings(String projectName, String movedClass, String targetClass, boolean isBelow, String parent);

}
