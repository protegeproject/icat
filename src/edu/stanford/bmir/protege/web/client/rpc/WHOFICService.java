package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Collection;
import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.AllowedPostcoordinationValuesData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.PrecoordinationClassExpressionData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;

@RemoteServiceRelativePath("who-fic")
public interface WHOFICService extends RemoteService  {

    public List<EntityPropertyValuesList> getEntityPropertyValuesForLinearization(String projectName, List<String> entities, String property,
            List<String> reifiedProps, int[] subjectEntityColumns);

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

    public List<EntityData> getAllSuperEntities(String projectName, EntityData entity);

    public List<PrecoordinationClassExpressionData> getPreCoordinationClassExpressions(
			String projectName, String entity, List<String> properties);

	public List<AllowedPostcoordinationValuesData> getAllowedPostCoordinationValues(
			String projectName, String entity,
			List<String> customScaleProperties,
			List<String> treeValueProperties,
			List<String> fixedScaleProperties);

	public boolean setPrecoordinationPropertyValue(String projectName, String entity,
			String property, EntityData oldValue, EntityData newValue);

	public boolean changeIsDefinitionalFlag(String projectName, String entity,
			String property, boolean isDefinitionalFlag);

    public boolean reorderSiblings(String projectName, String movedClass, String targetClass, boolean isBelow, String parent);
    
    public EntityData createInternalReference(String projectName, 
			EntityData entity, String referenceClassName,
			String referencePropertyName, String referencedValuePropertyName,
			EntityData referencedEntity,
			String user, String operationDescription);

}
