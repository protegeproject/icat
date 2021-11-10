package edu.stanford.bmir.protege.web.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import edu.stanford.bmir.protege.web.client.rpc.WHOFICService;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.AllowedPostcoordinationValuesData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.PrecoordinationClassExpressionData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;
import edu.stanford.bmir.protege.web.client.ui.icd.DisplayStatus;
import edu.stanford.bmir.protege.web.client.ui.icd.ICDClassTreePortlet;
import edu.stanford.bmir.whofic.IcdIdGenerator;
import edu.stanford.bmir.whofic.PrecoordinationDefinitionComponent;
import edu.stanford.bmir.whofic.WHOFICContentModel;
import edu.stanford.bmir.whofic.WHOFICContentModelConstants;
import edu.stanford.bmir.whofic.icd.ICDContentModel;
import edu.stanford.bmir.whofic.icd.ICDContentModelConstants;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.Log;
import edu.stanford.smi.protegex.owl.model.OWLClass;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFResource;
import edu.stanford.smi.protegex.owl.model.RDFSClass;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;

public class WHOFICServiceImpl extends OntologyServiceImpl implements WHOFICService {

	private static final long serialVersionUID = 5554025235067233870L;

	// TODO: Event generation is currently disabled for class creation, because
	// it generates too many events and slows down significantly the class creation.
	// The effect is that some other class trees do not get the event, and will not
	// update. We will keep this limitation for now, given the increase in
	// performance.

	public List<EntityPropertyValuesList> getEntityPropertyValuesForLinearization(String projectName,
			List<String> entities, String property, List<String> reifiedProps, int[] subjectEntityColumns) {
		List<EntityPropertyValuesList> entityPropertyValues = getMultilevelEntityPropertyValues(projectName, entities,
				property, reifiedProps, subjectEntityColumns);
		return prepareLinearizationEntityPropertyValues(projectName, entities, reifiedProps, entityPropertyValues);
	}

	private List<EntityPropertyValues> prepareLinearizationEntityPropertyValues(String projectName,
			List<String> entities, List<EntityPropertyValues> entityPropertyValues, boolean fixLinearizationParent) {
		if (entityPropertyValues == null) {
			entityPropertyValues = new ArrayList<EntityPropertyValues>();
		}
		ArrayList<EntityPropertyValues> result = new ArrayList<EntityPropertyValues>(entityPropertyValues);

		if (fixLinearizationParent) {
			// update display label of linearization parent to default parent, if it makes
			// sense
			if (entities != null && entities.size() == 1) {
				List<EntityData> directParents = getParents(projectName, entities.get(0), true);
				if (directParents.size() == 1) {
					String parentBrowserText = directParents.get(0).getBrowserText();
					PropertyEntityData propLinParentED = new PropertyEntityData(
							WHOFICContentModelConstants.LINEARIZATION_PARENT_PROP);
					for (EntityPropertyValues entityPV : result) {
						List<EntityData> values = entityPV.getPropertyValues(propLinParentED);
						if (values == null) {
							values = new ArrayList<EntityData>();
						}
						if (values.isEmpty()) {
							values.add(new EntityData());
						}
						String linParentName = values.get(0).getName();
						if (linParentName == null || "".equals(linParentName)) {
							ArrayList<EntityData> newValues = new ArrayList<EntityData>();
							for (EntityData value : values) {
								value.setName(null);
								value.setBrowserText("[" + parentBrowserText + "]");
								newValues.add(value);
							}
							entityPV.setPropertyValues(propLinParentED, newValues);
						}
					}
				}
			}
		}
		Collections.sort(result, new LinearizationEPVComparator());
		return result;
	}

	private List<EntityPropertyValuesList> prepareLinearizationEntityPropertyValues(String projectName,
			List<String> entities, List<String> reifiedProperties,
			List<EntityPropertyValuesList> entityPropertyValues) {
		if (entityPropertyValues == null) {
			entityPropertyValues = new ArrayList<EntityPropertyValuesList>();
		}
		ArrayList<EntityPropertyValuesList> result = new ArrayList<EntityPropertyValuesList>(entityPropertyValues);

		if (true) {
			// update display label of linearization parent to default parent, if it makes
			// sense
			if (entities != null && entities.size() == 1) {
				List<EntityData> directParents = getParents(projectName, entities.get(0), true);
				if (directParents.size() == 1) {
					String parentBrowserText = directParents.get(0).getBrowserText();
					int linParentPropertyIndex = getPropertyIndex(reifiedProperties,
							WHOFICContentModelConstants.LINEARIZATION_PARENT_PROP);
					for (EntityPropertyValuesList entityPV : result) {
						List<EntityData> values = entityPV.getPropertyValues(linParentPropertyIndex);
						if (values == null) {
							values = new ArrayList<EntityData>();
						}
						if (values.isEmpty()) {
							values.add(new EntityData());
						}
						String linParentName = values.get(0).getName();
						if (linParentName == null || "".equals(linParentName)) {
							ArrayList<EntityData> newValues = new ArrayList<EntityData>();
							for (EntityData value : values) {
								value.setName(null);
								value.setBrowserText("[" + parentBrowserText + "]");
								newValues.add(value);
							}
							entityPV.setPropertyValues(linParentPropertyIndex, newValues);
						}
					}
				}
			}
		}

		int linViewPropertyIndex = getPropertyIndex(reifiedProperties,
				WHOFICContentModelConstants.LINEARIZATION_VIEW_PROP);
		Collections.sort(result, new LinearizationEPVLComparator(linViewPropertyIndex));
		return result;
	}

	private int getPropertyIndex(List<String> properties, String propertyName) {
		int propIndex = -1;

		if (properties != null) {
			propIndex = properties.indexOf(propertyName);
		}
		return propIndex;
	}

	private static class LinearizationEPVComparator implements Comparator<EntityPropertyValues> {

		private static final PropertyEntityData linearizationViewPED = new PropertyEntityData(
				WHOFICContentModelConstants.LINEARIZATION_VIEW_PROP);

		public int compare(EntityPropertyValues epv1, EntityPropertyValues epv2) {
			// We should not have null values, but if we happen to have we wish to leave
			// them at the end
			if (epv1 == null) {
				return 1;
			}
			if (epv2 == null) {
				return -1;
			}
			EntityData lin1 = null;
			EntityData lin2 = null;
			List<EntityData> lins;
			lins = epv1.getPropertyValues(linearizationViewPED);
			if (lins != null && lins.size() > 0) {
				lin1 = lins.get(0);
			}
			lins = epv2.getPropertyValues(linearizationViewPED);
			if (lins != null && lins.size() > 0) {
				lin2 = lins.get(0);
			}
			// We should not have null values, but if we happen to have we wish to leave
			// them at the end
			if (lin1 == null) {
				return 1;
			}
			if (lin2 == null) {
				return -1;
			}

			return lin1.getBrowserText().compareTo(lin2.getBrowserText());
		}
	}

	private static class LinearizationEPVLComparator implements Comparator<EntityPropertyValuesList> {
		private final int linearizationViewPropIndex;

		public LinearizationEPVLComparator(int linearizationViewPropertyIndex) {
			this.linearizationViewPropIndex = linearizationViewPropertyIndex;
		}

		public int compare(EntityPropertyValuesList epvl1, EntityPropertyValuesList epvl2) {
			// We should not have null values, but if we happen to have we wish to leave
			// them at the end
			if (epvl1 == null) {
				return 1;
			}
			if (epvl2 == null) {
				return -1;
			}
			// We should always have values for the "linearizationView" property, but in
			// case don't, we can't compare them meaningfully
			if (linearizationViewPropIndex == -1) {
				return 0;
			}
			EntityData lin1 = null;
			EntityData lin2 = null;
			List<EntityData> lins;
			lins = epvl1.getPropertyValues(linearizationViewPropIndex);
			if (lins != null && lins.size() > 0) {
				lin1 = lins.get(0);
			}
			lins = epvl2.getPropertyValues(linearizationViewPropIndex);
			if (lins != null && lins.size() > 0) {
				lin2 = lins.get(0);
			}
			// We should not have null values, but if we happen to have we wish to leave
			// them at the end
			if (lin1 == null) {
				return 1;
			}
			if (lin2 == null) {
				return -1;
			}

			return lin1.getBrowserText().compareTo(lin2.getBrowserText());
		}
	}

	// have to correspond to the ones in InheritedTagsGrid
	private final static String COL_TAG = "tag";
	private final static String COL_INH_FROM = "inheritedFrom";

	public List<EntityPropertyValues> getSecondaryAndInheritedTags(String projectName, String clsName) {
		Project project = getProject(projectName);
		if (project == null) {
			return null;
		}

		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		RDFSNamedClass cls = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, clsName);
		if (cls == null) {
			return null;
		}

		// set browserText for values that come from primary

		List<EntityPropertyValues> inhTagsList = new ArrayList<EntityPropertyValues>();

		WHOFICContentModel cm = getContentModel(owlModel);
		Map<RDFResource, List<RDFSNamedClass>> tag2inhFrom = cm.getInvolvedTags(cls);

		RDFResource localPrimaryTag = cm.getAssignedPrimaryTag(cls);
		Collection<RDFResource> localSecondaryTags = cm.getAssignedSecondaryTags(cls);

		PropertyEntityData tagEdProp = new PropertyEntityData(COL_TAG);
		PropertyEntityData inhFromEdProp = new PropertyEntityData(COL_INH_FROM);

		for (Iterator<RDFResource> iteratorTags = tag2inhFrom.keySet().iterator(); iteratorTags.hasNext();) {
			RDFResource tagInst = iteratorTags.next();

			EntityData tag = createEntityData(tagInst);
			List<RDFSNamedClass> inhFromClses = tag2inhFrom.get(tagInst);

			EntityPropertyValues epv = new EntityPropertyValues();
			epv.setSubject(tag);

			EntityPropertyValues suplEpv = null;

			epv.addPropertyValue(tagEdProp, tag);
			List<RDFSNamedClass> realInheritedClasses = new ArrayList<RDFSNamedClass>();
			realInheritedClasses.addAll(inhFromClses);
			// remove local primary TAG
			if (tagInst.equals(localPrimaryTag)) {
				realInheritedClasses.remove(cls);
			}

			// split value list, if it contains local secondary TAG
			if (localSecondaryTags != null && localSecondaryTags.contains(tagInst)) {
				realInheritedClasses.remove(cls); // TODO see if we need to remove multiple appearances of cls in case a
													// tag is added as secondary tag multiple times

				suplEpv = new EntityPropertyValues();
				suplEpv.setSubject(tag); // not ideal - we need a subject for the values, ideally should be null
				suplEpv.addPropertyValue(tagEdProp, tag);
				suplEpv.addPropertyValues(inhFromEdProp, new ArrayList<EntityData>());
				inhTagsList.add(suplEpv);
			}

			// create value list for inherited classes; add "(P)" if inherited from primary
			// TAG of superclass
			if (!realInheritedClasses.isEmpty()) {
				List<EntityData> inheritedClsesEdList = new ArrayList<EntityData>();
				for (RDFSNamedClass inhCls : realInheritedClasses) {
					EntityData ed = createEntityData(inhCls);
					if (tagInst.equals(cm.getAssignedPrimaryTag(inhCls))) {
						ed.setBrowserText(ed.getBrowserText() + " (P)");
					}
					inheritedClsesEdList.add(ed);
				}
				epv.addPropertyValues(inhFromEdProp, inheritedClsesEdList);
				inhTagsList.add(epv);
			}
		}
		Collections.sort(inhTagsList, new InheritedTagEpvComparator());
		return inhTagsList;
	}

	@SuppressWarnings("deprecation")
	@Override
	public List<SubclassEntityData> getSubclasses(String projectName, String className) {
		Project project = getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();

		ArrayList<SubclassEntityData> subclsesData = new ArrayList<SubclassEntityData>();

		RDFSNamedClass superCls = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, className);
		if (superCls == null) {
			return subclsesData;
		}

		WHOFICContentModel cm = getContentModel(owlModel);
		
		List<RDFSNamedClass> subclasses = cm.getOrderedChildren(superCls);

		for (RDFSNamedClass subcls : subclasses) {
			if (subcls.isSystem() == true) {
				continue;
			}
			
			SubclassEntityData entity = new SubclassEntityData(subcls.getName(), getBrowserText(subcls),
					createEntityList(subcls.getDirectTypes()), subcls.getVisibleDirectSubclassCount());
			
			//subclassEntityData.setLocalAnnotationsCount(HasAnnotationCache.getAnnotationCount(subcls));
			//subclassEntityData.setChildrenAnnotationsCount(HasAnnotationCache.getChildrenAnnotationCount(subcls));
			
			setObsoleteStatus(subcls, entity, cm);
			boolean isReleased = setReleasedStatus(subcls, entity, cm);
			
			if (isReleased == false) { //optimization
				setDisplayStatus(subcls, entity, cm);
			}
			
			entity.setProperty(ICDClassTreePortlet.PUBLIC_ID_PROP, cm.getPublicId(subcls));

			String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
			if (user != null) {
				entity.setWatch(WatchedEntitiesCache.getCache(project).getWatchType(user, subcls.getName()));
			}
			
			subclsesData.add(entity);
		
		}
		return subclsesData;
	}


	private void setDisplayStatus(RDFSNamedClass cls, EntityData entity, WHOFICContentModel cm) {
		RDFResource status = cm.getDisplayStatus(cls);
		
		if (status == null) {
			return;
		}
		
		String prop = "status";
		if (status.equals(cm.getDisplayStatusBlueInst())) {
			entity.setProperty(prop, DisplayStatus.B.toString());
		} else if (status.equals(cm.getDisplayStatusYellowInst())) {
			entity.setProperty(prop, DisplayStatus.Y.toString());
		} else if (status.equals(cm.getDisplayStatusRedInst())) {
			entity.setProperty(prop, DisplayStatus.R.toString());
		}
	}

	private void setObsoleteStatus(RDFSNamedClass cls, EntityData entity, WHOFICContentModel cm) {
		boolean isObsolete = cm.isObsoleteCls(cls);
		if (isObsolete == true) {
			entity.setProperty("obsolete", "true");
		}
	}
	
	private boolean setReleasedStatus(RDFSNamedClass cls, EntityData entity, WHOFICContentModel cm) {
		boolean isReleased = cm.isReleased(cls);
		if (isReleased == true) {
			entity.setReleased(true);
		}
		return isReleased;
	}

	class InheritedTagEpvComparator implements Comparator<EntityPropertyValues> {
		public int compare(EntityPropertyValues epv1, EntityPropertyValues epv2) {
			Collection<EntityData> inhTags1 = epv1.getPropertyValues(new PropertyEntityData(COL_INH_FROM));
			Collection<EntityData> inhTags2 = epv2.getPropertyValues(new PropertyEntityData(COL_INH_FROM));
			if (inhTags1.size() == inhTags2.size()) {
				EntityData tag1 = CollectionUtilities
						.getFirstItem(epv1.getPropertyValues(new PropertyEntityData(COL_TAG)));
				EntityData tag2 = CollectionUtilities
						.getFirstItem(epv2.getPropertyValues(new PropertyEntityData(COL_TAG)));
				return tag2.getBrowserText().compareTo(tag1.getBrowserText());
			}
			return inhTags1.size() - inhTags2.size();
		}

	}

	public EntityPropertyValues changeIndexType(String projectName, String subject, String indexEntity,
			List<String> reifiedProps, String indexType) {
		Project project = getProject(projectName);
		if (project == null) {
			return null;
		}
		KnowledgeBase kb = project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel((OWLModel) kb);

		Cls cls = edu.stanford.bmir.whofic.KBUtil.getCls(kb, subject);
		if (cls == null || (!(cls instanceof RDFResource))) {
			return null;
		}
		RDFResource subjResource = (RDFResource) cls;

		Instance indexInst = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, indexEntity);
		if (indexInst == null) {
			return null;
		}

		boolean convertToSynonym = WHOFICContentModelConstants.TERM_SYNONYM_CLASS.equals(indexType);
		boolean convertToNarrower = WHOFICContentModelConstants.TERM_NARROWER_CLASS.equals(indexType);
		boolean convertToBaseIndex = !(convertToSynonym || convertToNarrower);

		RDFSNamedClass termSynonymClass = cm.getTermSynonymClass();
		RDFSNamedClass termNarrowerClass = cm.getTermNarrowerClass();
		RDFSNamedClass termBaseIndexClass = cm.getTermBaseIndexClass();

		String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
		String operationDescription = "Made base index term '" + indexInst.getBrowserText() + "' a "
				+ (convertToSynonym ? "synonym" : convertToNarrower ? "narrower term" : "(unspecified) base index");

		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {

				kb.beginTransaction(operationDescription, subject);

				if (termSynonymClass != null && termNarrowerClass != null && termBaseIndexClass != null) {
					if (convertToSynonym && (!indexInst.hasDirectType(termSynonymClass))) {
						indexInst.addDirectType(termSynonymClass);
					} else if (convertToNarrower && (!indexInst.hasDirectType(termNarrowerClass))) {
						indexInst.addDirectType(termNarrowerClass);
					} else if (convertToBaseIndex && (!indexInst.hasDirectType(termBaseIndexClass))) {
						indexInst.addDirectType(termBaseIndexClass);
					}

					if ((convertToNarrower || convertToBaseIndex) && indexInst.hasDirectType(termSynonymClass)) {
						indexInst.removeDirectType(termSynonymClass);
					}
					if ((convertToSynonym || convertToBaseIndex) && indexInst.hasDirectType(termNarrowerClass)) {
						indexInst.removeDirectType(termNarrowerClass);
					}
					if ((convertToSynonym || convertToNarrower) && indexInst.hasDirectType(termBaseIndexClass)) {
						indexInst.removeDirectType(termBaseIndexClass);
					}
				}

				RDFProperty synonymProperty = cm.getSynonymProperty();
				if (synonymProperty != null) {
					if (convertToSynonym && (!subjResource.hasPropertyValue(synonymProperty, indexInst))) {
						subjResource.addPropertyValue(synonymProperty, indexInst);
					} else if ((convertToNarrower || convertToBaseIndex)
							&& subjResource.hasPropertyValue(synonymProperty, indexInst)) {
						subjResource.removePropertyValue(synonymProperty, indexInst);
					}
				}
				RDFProperty narrowerProperty = cm.getNarrowerProperty();
				if (narrowerProperty != null) {
					if (convertToNarrower && (!subjResource.hasPropertyValue(narrowerProperty, indexInst))) {
						subjResource.addPropertyValue(narrowerProperty, indexInst);
					} else if ((convertToSynonym || convertToBaseIndex)
							&& subjResource.hasPropertyValue(narrowerProperty, indexInst)) {
						subjResource.removePropertyValue(narrowerProperty, indexInst);
					}
				}
				RDFProperty baseIndexProperty = cm.getBaseIndexProperty();
				if (baseIndexProperty != null) {
					if ((convertToBaseIndex) && (!subjResource.hasPropertyValue(baseIndexProperty, indexInst))) {
						subjResource.addPropertyValue(baseIndexProperty, indexInst);
					} else if ((convertToSynonym || convertToNarrower)
							&& subjResource.hasPropertyValue(baseIndexProperty, indexInst)) {
						subjResource.removePropertyValue(baseIndexProperty, indexInst);
					}
				}

				kb.commitTransaction();

			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at changing index type for: " + subject + " and index entity: " + indexEntity, e);
				kb.rollbackTransaction();
				throw new RuntimeException("Error at changing index type for: " + subject + " and index entity: "
						+ indexEntity + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
			}
		}

		EntityPropertyValues res = new EntityPropertyValues(new EntityData(subject));
		for (String reifiedPropName : reifiedProps) {
			Slot reifiedSlot = edu.stanford.bmir.whofic.KBUtil.getSlot(kb, reifiedPropName);
			if (reifiedSlot != null) {
				res.addPropertyValues(new PropertyEntityData(reifiedSlot.getName()),
						createEntityList(indexInst.getOwnSlotValues(reifiedSlot)));
			}
		}

		return res;
	}

	public EntityPropertyValues changeInclusionFlagForIndex(String projectName, String subject, String indexEntity,
			List<String> reifiedProps, boolean isInclusionFlag) {
		Project project = getProject(projectName);
		if (project == null) {
			return null;
		}
		KnowledgeBase kb = project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel((OWLModel) kb);

		Cls cls = edu.stanford.bmir.whofic.KBUtil.getCls(kb, subject);
		if (cls == null || (!(cls instanceof RDFResource))) {
			return null;
		}
		RDFResource subjResource = (RDFResource) cls;

		Instance indexInst = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, indexEntity);
		if (indexInst == null) {
			return null;
		}

		String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
		String operationDescription = (isInclusionFlag
				? "Made base index term '" + indexInst.getBrowserText() + "' also a base inclusion term."
				: "Removed base index term '" + indexInst.getBrowserText()
						+ "' from the list of base inclusion terms.");

		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {

				kb.beginTransaction(operationDescription, subject);

				RDFSNamedClass termBaseIndexClass = cm.getTermBaseInclusionClass();
				if (termBaseIndexClass != null) {
					if (isInclusionFlag && (!indexInst.hasDirectType(termBaseIndexClass))) {
						indexInst.addDirectType(termBaseIndexClass);
					} else {
						indexInst.removeDirectType(termBaseIndexClass);
					}
				}

				RDFProperty indexBaseInclusionProperty = cm.getIndexBaseInclusionProperty();
				if (indexBaseInclusionProperty != null) {
					if (isInclusionFlag && (!subjResource.hasPropertyValue(indexBaseInclusionProperty, indexInst))) {
						subjResource.addPropertyValue(indexBaseInclusionProperty, indexInst);
					} else {
						subjResource.removePropertyValue(indexBaseInclusionProperty, indexInst);
					}
				}

				kb.commitTransaction();

			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at changing inclusion flag for: " + subject + " and index entity: " + indexEntity, e);
				kb.rollbackTransaction();
				throw new RuntimeException("Error at changing inclusion flag for: " + subject + " and index entity: "
						+ indexEntity + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
			}
		}

		EntityPropertyValues res = new EntityPropertyValues(new EntityData(subject));
		for (String reifiedPropName : reifiedProps) {
			Slot reifiedSlot = edu.stanford.bmir.whofic.KBUtil.getSlot(kb, reifiedPropName);
			if (reifiedSlot != null) {
				res.addPropertyValues(new PropertyEntityData(reifiedSlot.getName()),
						createEntityList(indexInst.getOwnSlotValues(reifiedSlot)));
			}
		}
		return res;
	}

	public void removeBaseIndexTerm(String projectName, String entityName, String value, String user,
			String operationDescription) {
		Project project = getProject(projectName);
		if (project == null) {
			return;
		}
		KnowledgeBase kb = project.getKnowledgeBase();
		Instance subject = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, entityName);
		if (subject == null) {
			return;
		}

		Instance valueInst = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, value);
		if (valueInst == null) {
			throw new RuntimeException("Cannot find index term to remove: " + value + " for entity: " + entityName);
		}

		WHOFICContentModel cm = getContentModel((OWLModel) kb);
		RDFProperty synonymProperty = cm.getSynonymProperty();
		RDFProperty narrowerProperty = cm.getNarrowerProperty();
		RDFProperty baseIndexProperty = cm.getBaseIndexProperty();

		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			boolean runsInTransaction = WebProtegeKBUtil.shouldRunInTransaction(operationDescription);
			try {
				if (runsInTransaction) {
					kb.beginTransaction(operationDescription);
				}

				subject.removeOwnSlotValue(synonymProperty, valueInst);
				subject.removeOwnSlotValue(narrowerProperty, valueInst);
				subject.removeOwnSlotValue(baseIndexProperty, valueInst);

				if (runsInTransaction) {
					kb.commitTransaction();
				}
			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at removing base index term subj: " + subject + " value: " + value, e);
				if (runsInTransaction) {
					kb.rollbackTransaction();
				}
				throw new RuntimeException(e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
			}
		}
	}

	public List<String> getListOfSelectedPostCoordinationAxes(String projectName, String entity,
			List<String> reifiedProps) {
		List<String> selectedPCAxisProperties = new ArrayList<String>();

		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel((OWLModel) kb);

		Slot allowedPcAxesProperty = cm.getAllowedPostcoordinationAxesProperty();
		Slot allowedPcAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
		Slot requiredPcAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

		// need to create a copy of the constant array list in order to be able to
		// modify if (e.g. by calling .retainAll())
		List<String> pcAxisProperties = new ArrayList<String>(cm.getPostcoordinationAxesPropertyList());
		if (reifiedProps != null) {
			pcAxisProperties.retainAll(reifiedProps);
		}

		Instance subjInst = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, entity);
		Collection<?> pcSpecs = subjInst.getOwnSlotValues(allowedPcAxesProperty);
		for (Object pcSpec : pcSpecs) {
			Instance pcSpecInst = (Instance) pcSpec;
			if (pcSpecInst != null) {
				Collection<?> allowedPcAxisPropertyValues = pcSpecInst.getOwnSlotValues(allowedPcAxisPropertyProperty);
				Collection<?> requiredPcAxisPropertyValues = pcSpecInst
						.getOwnSlotValues(requiredPcAxisPropertyProperty);
				for (String pcAxisPropName : pcAxisProperties) {
					Slot pcAxisProperty = edu.stanford.bmir.whofic.KBUtil.getSlot(kb, pcAxisPropName);
					if (allowedPcAxisPropertyValues.contains(pcAxisProperty)
							|| requiredPcAxisPropertyValues.contains(pcAxisProperty)) {
						selectedPCAxisProperties.add(pcAxisPropName);
					}
				}
			}
		}

		return selectedPCAxisProperties;
	}

	public List<EntityPropertyValues> getEntityPropertyValuesForPostCoordinationAxes(String projectName,
			List<String> entities, List<String> properties, List<String> reifiedProps) {
		WHOFICContentModel cm = null;
		if (projectName != null) {
			Project project = getProject(projectName);
			if (project != null) {
				cm = getContentModel((OWLModel) project.getKnowledgeBase());
			}
		}

		List<String> regularReifiedProperties = new ArrayList<String>(reifiedProps);
		List<String> specialReifiedProperties = new ArrayList<String>();
		List<String> pcAxesPropertyList = (cm == null ? new ArrayList<String>()
				: cm.getPostcoordinationAxesPropertyList());
		for (String reifiedProp : reifiedProps) {
			if (pcAxesPropertyList.contains(reifiedProp)) {
				regularReifiedProperties.remove(reifiedProp);
				specialReifiedProperties.add(reifiedProp);
			}
		}

		return getEntityPropertyValuesForPostCoordinationAxes(projectName, entities, properties,
				regularReifiedProperties, (cm == null ? null : cm.getAllowedPostcoordinationAxisPropertyProperty()),
				(cm == null ? null : cm.getRequiredPostcoordinationAxisPropertyProperty()), specialReifiedProperties);
	}

	public List<EntityPropertyValues> getEntityPropertyValuesForPostCoordinationAxes(String projectName,
			List<String> entities, List<String> properties, List<String> reifiedProps,
			RDFProperty allowedPcAxisProperty, RDFProperty requiredPcAxisProperty, List<String> pcAxisProperties) {

		List<EntityPropertyValues> entityPropValues = getEntityPropertyValues(projectName, entities, properties,
				reifiedProps);

		Project project = getProject(projectName);
		if (project == null) {
			return entityPropValues;
		}

		KnowledgeBase kb = project.getKnowledgeBase();

		if (entityPropValues == null) {
			entityPropValues = new ArrayList<EntityPropertyValues>();
		}
		if (allowedPcAxisProperty != null && pcAxisProperties != null && pcAxisProperties.size() > 0) {
			for (EntityPropertyValues epv : entityPropValues) {
				String instanceName = epv.getSubject().getName();
				Instance valueInst = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, instanceName);
				if (valueInst != null) {
					Collection<?> allowedPcAxisPropertyValues = valueInst.getOwnSlotValues(allowedPcAxisProperty);
					Collection<?> requiredPcAxisPropertyValues = valueInst.getOwnSlotValues(requiredPcAxisProperty);
					for (String pcAxisPropName : pcAxisProperties) {
						Slot pcAxisProperty = edu.stanford.bmir.whofic.KBUtil.getSlot(kb, pcAxisPropName);
						int value = 0;
						if (allowedPcAxisPropertyValues.contains(pcAxisProperty)) {
							value |= 1;
						}
						if (requiredPcAxisPropertyValues.contains(pcAxisProperty)) {
							value |= 2;
						}
						if (pcAxisProperty != null) {
							epv.addPropertyValue(new PropertyEntityData(pcAxisProperty.getName()),
									createEntityData(value));
						}
					}
					// entityPropValues.add(epv);
				} else {
					assert false : "Unable to get instance " + instanceName
							+ " which was created in getEntityPropertyValues(String, List, List, List) method";
				}
			}
		}

		return prepareLinearizationEntityPropertyValues(projectName, entities, entityPropValues, false);
	}

	/**
	 * @return true if this is the first linearization where we have set this
	 *         post-coordination axis as allowed. False otherwise.
	 */
	public boolean addAllowedPostCoordinationAxis(String projectName, String subject, String postcoordinationEntity,
			String postcoordinationProperty, boolean isRequiredFlag) {
		Project project = getProject(projectName);
		if (project == null) {
			return false;
		}
		KnowledgeBase kb = project.getKnowledgeBase();
		if (!(kb instanceof OWLModel)) {
			assert false : "The methods of ICDServiceImpl, including addAllowedPostCoordinationAxis, "
					+ "are suppose to work with OWL ontologies that conform to the ICD content model";
			return false;
		}
		OWLModel owlModel = (OWLModel) kb;
		WHOFICContentModel cm = getContentModel(owlModel);

		RDFResource subjResource = edu.stanford.bmir.whofic.KBUtil.getOWLNamedClass(owlModel, subject);
		if (subjResource == null) {
			return false;
		}

		RDFResource postCoordInd = edu.stanford.bmir.whofic.KBUtil.getOWLIndividual(owlModel, postcoordinationEntity);
		if (postCoordInd == null) {
			return false;
		}

		RDFProperty postCoordProperty = edu.stanford.bmir.whofic.KBUtil.getOWLProperty(owlModel, postcoordinationProperty);
		if (postCoordProperty == null) {
			return false;
		}

		RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
		RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

		if (allowedPostCoordinationAxisPropertyProperty == null
				|| requiredPostCoordinationAxisPropertyProperty == null) {
			throw new RuntimeException("Invalid content model! The following properties could not be retrieved:"
					+ (allowedPostCoordinationAxisPropertyProperty == null
							? " " + WHOFICContentModelConstants.ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP
							: "")
					+ (requiredPostCoordinationAxisPropertyProperty == null
							? " " + WHOFICContentModelConstants.REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP
							: ""));
		}

		RDFProperty linViewProp = cm.getLinearizationViewProperty();
		RDFResource linView = (RDFResource) postCoordInd.getPropertyValue(linViewProp);

		boolean axisAlreadyUsed = isAxisPartOfAnyLinearization(cm, subjResource, postCoordProperty);

		String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
		String linViewName = (linView == null ? "" : linView.getBrowserText());
		String operationDescription = "Added '" + postCoordProperty.getBrowserText() + "' as "
				+ (isRequiredFlag ? "a required" : "an allowed") + " post-coordination axis " + "in the " + linViewName
				+ " linearization of " + subjResource.getBrowserText() + ".";

		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {

				kb.beginTransaction(operationDescription, subject);

				Collection<?> allowedPcAxes = postCoordInd
						.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
				Collection<?> requiredPcAxes = postCoordInd
						.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

				if (requiredPcAxes.contains(postCoordProperty)) {
					postCoordInd.removePropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
				}
				if (allowedPcAxes.contains(postCoordProperty)) {
					postCoordInd.removePropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
				}

				if (isRequiredFlag) {
					postCoordInd.addPropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
				} else {
					postCoordInd.addPropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
				}

				kb.commitTransaction();

			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at adding postcoordination property " + postcoordinationProperty + " for: " + subject
								+ " and post-coordination specification: " + postcoordinationEntity + " (" + linViewName
								+ ")",
						e);
				kb.rollbackTransaction();
				throw new RuntimeException("Error at adding postcoordination property " + postcoordinationProperty
						+ " for: " + subject + " and post-coordination specification: " + postcoordinationEntity + " ("
						+ linViewName + ")" + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
			}
		}

		return (!axisAlreadyUsed);

	}

	/**
	 * @return true if after we removed this post-coordination axis, there are no
	 *         other linearizations where this property is set as a possible
	 *         post-coordination axis. False otherwise.
	 */
	public boolean removeAllowedPostCoordinationAxis(String projectName, String subject, String postcoordinationEntity,
			String postcoordinationProperty) {
		Project project = getProject(projectName);
		if (project == null) {
			return false;
		}
		KnowledgeBase kb = project.getKnowledgeBase();
		if (!(kb instanceof OWLModel)) {
			assert false : "The methods of ICDServiceImpl, including addAllowedPostCoordinationAxis, "
					+ "are suppose to work with OWL ontologies that conform to the ICD content model";
			return false;
		}
		OWLModel owlModel = (OWLModel) kb;
		WHOFICContentModel cm = getContentModel(owlModel);

		RDFResource subjResource = edu.stanford.bmir.whofic.KBUtil.getOWLNamedClass(owlModel, subject);
		if (subjResource == null) {
			return false;
		}

		RDFResource postCoordInd = edu.stanford.bmir.whofic.KBUtil.getOWLIndividual(owlModel, postcoordinationEntity);
		if (postCoordInd == null) {
			return false;
		}

		RDFProperty postCoordProperty = edu.stanford.bmir.whofic.KBUtil.getOWLProperty(owlModel, postcoordinationProperty);
		if (postCoordProperty == null) {
			return false;
		}

		RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
		RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

		if (allowedPostCoordinationAxisPropertyProperty == null
				|| requiredPostCoordinationAxisPropertyProperty == null) {
			throw new RuntimeException("Invalid content model! The following properties could not be retrieved:"
					+ (allowedPostCoordinationAxisPropertyProperty == null
							? " " + WHOFICContentModelConstants.ALLOWED_POSTCOORDINATION_AXIS_PROPERTY_PROP
							: "")
					+ (requiredPostCoordinationAxisPropertyProperty == null
							? " " + WHOFICContentModelConstants.REQUIRED_POSTCOORDINATION_AXIS_PROPERTY_PROP
							: ""));
		}

		RDFProperty linViewProp = cm.getLinearizationViewProperty();
		RDFResource linView = (RDFResource) postCoordInd.getPropertyValue(linViewProp);

		String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
		String linViewName = (linView == null ? "" : linView.getBrowserText());
		String operationDescription = "Removed '" + postCoordProperty.getBrowserText() + "' as "
				+ "a possible post-coordination axis " + "in the " + linViewName + " linearization of "
				+ subjResource.getBrowserText() + ".";

		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {

				kb.beginTransaction(operationDescription, subject);

				Collection<?> allowedPcAxes = postCoordInd
						.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
				Collection<?> requiredPcAxes = postCoordInd
						.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

				if (requiredPcAxes.contains(postCoordProperty)) {
					postCoordInd.removePropertyValue(requiredPostCoordinationAxisPropertyProperty, postCoordProperty);
				}
				if (allowedPcAxes.contains(postCoordProperty)) {
					postCoordInd.removePropertyValue(allowedPostCoordinationAxisPropertyProperty, postCoordProperty);
				}

				kb.commitTransaction();

			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at removing postcoordination property " + postcoordinationProperty + " for: " + subject
								+ " and post-coordination specification: " + postcoordinationEntity + " (" + linViewName
								+ ")",
						e);
				kb.rollbackTransaction();
				throw new RuntimeException("Error at removing postcoordination property " + postcoordinationProperty
						+ " for: " + subject + " and post-coordination specification: " + postcoordinationEntity + " ("
						+ linViewName + ")" + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
			}
		}

		boolean axisUsed = isAxisPartOfAnyLinearization(cm, subjResource, postCoordProperty);
		return (!axisUsed);

	}

	private boolean isAxisPartOfAnyLinearization(WHOFICContentModel cm, RDFResource subjResource,
			RDFProperty postCoordProperty) {
		RDFProperty allowedPostCoordinationAxesProperty = cm.getAllowedPostcoordinationAxesProperty();
		RDFProperty allowedPostCoordinationAxisPropertyProperty = cm.getAllowedPostcoordinationAxisPropertyProperty();
		RDFProperty requiredPostCoordinationAxisPropertyProperty = cm.getRequiredPostcoordinationAxisPropertyProperty();

		Collection<?> postCoordSpecifications = subjResource.getPropertyValues(allowedPostCoordinationAxesProperty);
		for (Object postCoordSpec : postCoordSpecifications) {
			RDFIndividual postCoordSpecInd = (RDFIndividual) postCoordSpec;
			Collection<?> allowedPcAxes = postCoordSpecInd
					.getPropertyValues(allowedPostCoordinationAxisPropertyProperty);
			Collection<?> requiredPcAxes = postCoordSpecInd
					.getPropertyValues(requiredPostCoordinationAxisPropertyProperty);

			if (allowedPcAxes.contains(postCoordProperty) || requiredPcAxes.contains(postCoordProperty)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public List<ScaleInfoData> getPostCoordinationAxesScales(String projectName, List<String> properties) {

		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel(owlModel);

		RDFProperty referencedValueProperty = cm.getReferencedValueProperty();

		List<ScaleInfoData> res = new ArrayList<ScaleInfoData>();
		for (String property : properties) {
			RDFProperty prop = edu.stanford.bmir.whofic.KBUtil.getRDFProperty(owlModel, property);
			if (prop == null) {
				Log.getLogger()
						.warning("No property found with name: " + property
								+ " in method ICDSerivceImpl.getPostCoordinationAxesScales. "
								+ "Please check your configurations.");
			} else {
				// TODO deal with (or at least check for) multiple ranges (compare with rev.
				// 27220)
				RDFResource range = prop.getRange();
				if (range == null) {
					Log.getLogger().warning("It is not possible to retrieve the (fixed) scale values for property "
							+ property + " because it's range is not set");
				} else {
					OWLClass rangeCls = (OWLClass) range;
					Collection<?> allValidValues = rangeCls.getInstances(false);
					List<EntityData> propertyValues = new ArrayList<EntityData>();
					List<String> propertyValueDefinitions = new ArrayList<String>();
					for (Object rangeValue : allValidValues) {
						// TODO get the referencedValue and get the title and definition from it - DONE
						// check below
						OWLIndividual ind = (OWLIndividual) rangeValue;
						Instance refInst = (Instance) ind.getPropertyValue(referencedValueProperty);
						propertyValues.add(new EntityData(ind.getName(), ind.getBrowserText()));
						String definition = getDefinition(cm, refInst);
						propertyValueDefinitions.add(definition);
					}
					// build property info:
					ScaleInfoData propertyInfo = new ScaleInfoData(propertyValues);
					propertyInfo.setProperty(new PropertyEntityData(property));
					propertyInfo.setDefinitions(propertyValueDefinitions);
					res.add(propertyInfo);
				}
			}
		}
		return res;
	}

	private String getDefinition(WHOFICContentModel cm, Instance inst) {
		String definition = null;
		if (inst instanceof RDFSNamedClass) {
			RDFResource defTerm = cm.getTerm(((RDFSNamedClass) inst), cm.getDefinitionProperty());
			if (defTerm != null) {
				definition = (String) defTerm.getPropertyValue(cm.getLabelProperty());
			}
		}
		return definition;
	}

	/**
	 * This method returns all superclasses or superproperties of a given class or
	 * property ordered from most specific to the most general.
	 */
	public List<EntityData> getAllSuperEntities(String projectName, EntityData entity) {
		List<EntityData> superEntities = new ArrayList<EntityData>();

		if (entity == null || entity.getName() == null) {
			return superEntities;
		}

		Project project = getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();

		Class<? extends RDFResource> type = null;
		RDFResource res = null;
		RDFResource top = null;
		if (entity instanceof PropertyEntityData) {
			type = RDFProperty.class;
			res = edu.stanford.bmir.whofic.KBUtil.getRDFProperty(owlModel, entity.getName());
		} else {
			type = RDFSClass.class;
			res = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, entity.getName());
			top = getTopCategoryClass(owlModel);
		}

		if (type != null && res != null) {
			List<RDFResource> superResources = getAllSuperEntities(owlModel, res, type, top);
			for (RDFResource superRes : superResources) {
				if (RDFProperty.class.equals(type)) {
					superEntities.add(new PropertyEntityData(superRes.getName(), superRes.getBrowserText(), null));
				} else {
					superEntities.add(new EntityData(superRes.getName(), superRes.getBrowserText()));
				}
			}
		}

		return superEntities;
	}

	private List<RDFResource> getAllSuperEntities(OWLModel owlModel, RDFResource res, Class<? extends RDFResource> type,
			RDFResource top) {
		List<RDFResource> result = new ArrayList<RDFResource>();

		collectAllSuperEntities(owlModel, res, type, top, result);

		return result;
	}

	private void collectAllSuperEntities(OWLModel owlModel, RDFResource res, Class<? extends RDFResource> type,
			RDFResource top, List<RDFResource> result) {
		if (res.equals(top)) {
			return;
		}

		Collection<RDFResource> superEntities = getSuperEntities(res, type);
		List<RDFResource> toBeVisited = new ArrayList<RDFResource>();
		for (RDFResource parent : superEntities) {
			if (parent != null && /* (!parent.equals(top)) && */ (!result.contains(parent))) {
				result.add(parent);
				toBeVisited.add(parent);
			}
		}

		for (RDFResource parent : toBeVisited) {
			collectAllSuperEntities(owlModel, parent, type, top, result);
		}
	}

	@SuppressWarnings("unchecked")
	private Collection<RDFResource> getSuperEntities(RDFResource res, Class<? extends RDFResource> type) {
		if (RDFSClass.class.equals(type)) {
			return filterOutAnonymousClasses(((RDFSClass) res).getSuperclasses(false));
		} else if (RDFProperty.class.equals(type)) {
			return ((RDFProperty) res).getSuperproperties(false);
		} else {
			return Collections.emptyList();
		}
	}

	private Collection<RDFResource> filterOutAnonymousClasses(Collection<RDFResource> superclasses) {
		Collection<RDFResource> res = new ArrayList<RDFResource>();
		for (RDFResource cls : superclasses) {
			if (cls instanceof RDFSNamedClass) {
				res.add((RDFSNamedClass) cls);
			}
		}
		return res;
	}

	@Override
	public List<PrecoordinationClassExpressionData> getPreCoordinationClassExpressions(String projectName,
			String entity, List<String> properties) {
		Project project = getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel(owlModel);

		RDFSNamedClass cls = cm.getICDCategory(entity);
		Collection<PrecoordinationDefinitionComponent> propertyValues = cm.getPrecoordinationPropertyValues(cls,
				properties);
		List<PrecoordinationClassExpressionData> res = new ArrayList<PrecoordinationClassExpressionData>();
		for (Iterator<PrecoordinationDefinitionComponent> it = propertyValues.iterator(); it.hasNext();) {
			PrecoordinationDefinitionComponent defComp = (PrecoordinationDefinitionComponent) it.next();

			PrecoordinationClassExpressionData resData = new PrecoordinationClassExpressionData(defComp.getProperty(),
					defComp.isDefinitional());

			String value = defComp.getValue();
			ValueType valueType = defComp.getValueType();
			RDFResource valueRes = null;
			if (valueType == ValueType.INSTANCE) {
				valueRes = edu.stanford.bmir.whofic.KBUtil.getRDFIndividual(owlModel, value);
			} else if (valueType == ValueType.CLS) {
				valueRes = edu.stanford.bmir.whofic.KBUtil.getOWLNamedClass(owlModel, value);
			}

			if (valueRes != null) {
				resData.setValue(valueRes.getName(), valueRes.getBrowserText(),
						(valueType == ValueType.INSTANCE
								? edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Instance
								: edu.stanford.bmir.protege.web.client.rpc.data.ValueType.Class));
				res.add(resData);
			} else {
				if (value != null) {
					Log.getLogger().warning("Problem at creating PrecoordinationClassExpressionData for"
							+ "PrecoordinationDefinitionComponent: " + defComp + " (value could not be converted)");
				}
			}
		}
		return res;
	}

	@Override
	public List<AllowedPostcoordinationValuesData> getAllowedPostCoordinationValues(String projectName, String entity,
			List<String> customScaleProperties, List<String> treeValueProperties, List<String> fixedScaleProperties) {
		Project project = getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel(owlModel);

		RDFSNamedClass cls = cm.getICDCategory(entity);
		RDFSNamedClass precoordSuperclass = cm.getPrecoordinationSuperclass(cls);

		List<AllowedPostcoordinationValuesData> res = new ArrayList<AllowedPostcoordinationValuesData>();
		if (precoordSuperclass == null) {
			return res;
		}

		List<String> allProperties = new ArrayList<String>(treeValueProperties);

		List<String> relevantProperties = (List<String>) getListOfSelectedPostCoordinationAxes(projectName,
				precoordSuperclass.getName(), allProperties);
		
		for (String propName : relevantProperties) {
			AllowedPostcoordinationValuesData allowedPostcoordinationValuesData = new AllowedPostcoordinationValuesData(propName);
			List<EntityData> propertyValues = null;
			
			if (treeValueProperties.contains(propName)) {
				propertyValues = getAllowedTreeNodeValues(cm, owlModel, precoordSuperclass, propName);
			}
			
			if (propertyValues == null) {
				allowedPostcoordinationValuesData.setValues(null);
			} else {
				for (EntityData propValue : propertyValues) {
					allowedPostcoordinationValuesData.addValue(propValue);
				}
			}
			res.add(allowedPostcoordinationValuesData);
		}

		return res;
	}

	private List<EntityData> getAllowedCustomScaleValues(WHOFICContentModel cm, OWLModel owlModel,
			RDFSNamedClass icdClass, String pcPropName) {
		String scalePropName = ICDContentModelConstants.PC_AXIS_PROP_TO_VALUE_SET_PROP.get(pcPropName);
		Collection<?> propertyValues = icdClass.getPropertyValues(edu.stanford.bmir.whofic.KBUtil.getRDFProperty(owlModel, scalePropName));
		if (propertyValues == null || propertyValues.isEmpty()) {
			return null;
		}
		RDFSNamedClass pcScaleTermClass = cm.getPostcoordinationScaleTermClass();
		RDFProperty hasScaleValueProperty = cm.getHasScaleValueProperty();

		List<EntityData> res = new ArrayList<EntityData>();
		for (Object propValue : propertyValues) {
			if (propValue instanceof OWLIndividual) {
				// get scale values
				OWLIndividual scaleValueTerm = (OWLIndividual) propValue;
				if (scaleValueTerm.hasRDFType(pcScaleTermClass, true)) {
					Collection<?> scaleValues = scaleValueTerm.getPropertyValues(hasScaleValueProperty);
					for (Object scValue : scaleValues) {
						if (scValue instanceof OWLIndividual) {
//							//get referenced value
//							Instance refValue = getReferencedValue((OWLIndividual) scValue, 
//									pcValueRefClass, referencedValueProperty);
//							if (refValue != null) {
//								res.add(new EntityData(refValue.getName(), refValue.getBrowserText())); //set types as well, if relevant
//							}
							Instance scValInst = (OWLIndividual) scValue;
							res.add(new EntityData(scValInst.getName(), scValInst.getBrowserText())); // set types as
																										// well, if
																										// relevant
						} else {
							Log.getLogger().warning("Invalid scale value for PostcoordinationScaleTerm: " + scValue);
						}
					}
				} else {
					Log.getLogger().warning("Scale value term " + scaleValueTerm + " does not have the right type");
				}
			}
		}
		return res;
	}

	protected Instance getReferencedValue(OWLIndividual scaleValueInd, RDFSNamedClass pcValueRefClass,
			RDFProperty referencedValueProperty) {
		if (scaleValueInd.hasRDFType(pcValueRefClass, true)) {
			Object refValue = scaleValueInd.getPropertyValue(referencedValueProperty);
			if (refValue instanceof Instance) {
				return (Instance) refValue;
			} else {
				Log.getLogger()
						.warning("Invalid referenced value for PostcoordinationValueReference: " + scaleValueInd);
			}
		} else {
			Log.getLogger().warning("Scale value " + scaleValueInd + " does not have the right type");
		}
		return null;
	}

	private List<EntityData> getAllowedTreeNodeValues(WHOFICContentModel cm, OWLModel owlModel, RDFSNamedClass icdClass,
			String pcPropName) {

		RDFSNamedClass pcValueRefClass = cm.getPostcoordinationValueReferenceClass();
		RDFProperty referencedValueProperty = cm.getReferencedValueProperty();

		Collection<?> propertyValues = icdClass.getPropertyValues(edu.stanford.bmir.whofic.KBUtil.getRDFProperty(owlModel, pcPropName));
		if (propertyValues == null || propertyValues.isEmpty()) {
			return null;
		}

		List<EntityData> res = new ArrayList<EntityData>();
		for (Object scValue : propertyValues) {
			if (scValue instanceof OWLIndividual) {
				// get referenced value
				Instance refValue = getReferencedValue((OWLIndividual) scValue, pcValueRefClass,
						referencedValueProperty);
				if (refValue != null) {
					res.add(new EntityData(refValue.getName(), refValue.getBrowserText())); // set types as well, if
																							// relevant
				}
			} else {
				Log.getLogger().warning("Invalid scale value for PostcoordinationScaleTerm: " + scValue);
			}
		}

		return res;
	}

	private Map<String, List<EntityData>> getAllowedFixedScaleValues(String projectName, OWLModel owlModel,
			RDFSNamedClass icdClass, List<String> relevantFixedScaleProperties) {
		Map<String, List<EntityData>> res = new HashMap<String, List<EntityData>>();

		List<ScaleInfoData> postCoordinationAxesScales = getPostCoordinationAxesScales(projectName,
				relevantFixedScaleProperties);
		for (ScaleInfoData scInfoData : postCoordinationAxesScales) {
			List<EntityData> values = new ArrayList<EntityData>();
			for (int i = 0; i < scInfoData.getValueCount(); i++) {
				values.add(scInfoData.getScaleValue(i));
			}
			res.put(scInfoData.getProperty().getName(), values);
		}

		return res;
	}

	@Override
	public boolean setPrecoordinationPropertyValue(String projectName, String entity, String property,
			EntityData oldValue, EntityData newValue, String user, String operationDescription) {

		Project project = getProject(projectName);
		OWLModel kb = (OWLModel) project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel(kb);

		RDFSNamedClass cls = cm.getICDCategory(entity);

		boolean returnValue = false;

		boolean eventsEnabled = WebProtegeKBUtil.setEventGenerationForRemotePrj(kb, false);
		
		boolean runsInTransaction = WebProtegeKBUtil.shouldRunInTransaction(operationDescription);
		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {
				if (runsInTransaction) {
					kb.beginTransaction(operationDescription);
				}

				returnValue = cm.setPrecoordinationDefinitionPropertyValue(cls, property,
						(oldValue == null ? null : oldValue.getName()), (newValue == null ? null : newValue.getName()));

				if (runsInTransaction) {
					kb.commitTransaction();
				}
			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at editing the logical definition in " + projectName + " class: " + cls, e);
				if (runsInTransaction) {
					kb.rollbackTransaction();
				}
				throw new RuntimeException("Error at editing the logical definition in " + projectName + " class: "
						+ cls + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
				WebProtegeKBUtil.setEventGenerationForRemotePrj(kb, eventsEnabled);
			}
		}

		return returnValue;
	}

	@Override
	public boolean changeIsDefinitionalFlag(String projectName, String entity, String property,
			boolean isDefinitionalFlag) {

		Project project = getProject(projectName);
		OWLModel kb = (OWLModel) project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel(kb);

		RDFSNamedClass cls = cm.getICDCategory(entity);
		RDFProperty prop = kb.getRDFProperty(property);
		
		String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
		String operationDescription = "Changed the logical definition to " +
				(isDefinitionalFlag == true ? "defining" : "non-defining") +
				" on property " + prop.getBrowserText() +
				" for class " + cls.getBrowserText() + 
				" -- Apply to: " + cls.getName();
		
		boolean result = false;
		
		boolean eventsEnabled = WebProtegeKBUtil.setEventGenerationForRemotePrj(kb, false);
		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);
			try {
				kb.beginTransaction(operationDescription);
		
				//FIXME: this method should rather throw exceptions than return false.. this way we can
				//record false changes
				result = cm.changeIsDefinitionalFlag(cls, property, isDefinitionalFlag);
		
				kb.commitTransaction();
			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Error at editing the logical definition in " + projectName + " class: " + cls, e);
				kb.rollbackTransaction();
				throw new RuntimeException("Error at editing the logical definition in " + projectName + " class: "
						+ cls + ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
				WebProtegeKBUtil.setEventGenerationForRemotePrj(kb, eventsEnabled);
			}
		}
		return result;
	}

	@Override
	public boolean reorderSiblings(String projectName, String movedClass, String targetClass, boolean isBelow,
			String parent) {

		String user = WebProtegeKBUtil.getUserInSession(getThreadLocalRequest());
		if (user == null) {
			return false;
		}

		Project project = getProject(projectName);
		OWLModel owlModel = (OWLModel) project.getKnowledgeBase();
		WHOFICContentModel cm = getContentModel(owlModel);

		RDFSNamedClass movedCls = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, movedClass);
		RDFSNamedClass targetCls = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, targetClass);
		RDFSNamedClass parentCls = edu.stanford.bmir.whofic.KBUtil.getRDFSNamedClass(owlModel, parent);

		if (movedCls == null || targetClass == null || parentCls == null) {
			return false;
		}

		String opDescription = "Changed order of  " + movedCls.getBrowserText() + " in parent: "
				+ parentCls.getBrowserText() + ". Moved " + (isBelow ? "below " : "above") + " "
				+ targetCls.getBrowserText();

		boolean success = false;

		boolean eventsEnabled = WebProtegeKBUtil.setEventGenerationForRemotePrj(owlModel, false);
		synchronized (owlModel) {
			WebProtegeKBUtil.morphUser(owlModel, user);
			try {
				owlModel.beginTransaction(opDescription, movedClass);

				success = cm.reorderSibling(movedCls, targetCls, isBelow, parentCls, user);

				owlModel.commitTransaction();
			} catch (Exception e) {
				Log.getLogger().log(Level.WARNING, "Error on operation: " + opDescription, e);
				owlModel.rollbackTransaction();
				throw new RuntimeException("Error on operation: " + opDescription, e);
			} finally {
				WebProtegeKBUtil.restoreUser(owlModel);
				WebProtegeKBUtil.setEventGenerationForRemotePrj(owlModel, eventsEnabled);
			}

			return success;
		}
	}

	protected WHOFICContentModel getContentModel(OWLModel owlModel) {
		// return new ICIContentModel(owlModel);
		return new ICDContentModel(owlModel);
	}

	protected RDFResource getTopCategoryClass(OWLModel owlModel) {
		// return new ICIContentModel(owlModel).getICICategoryClass();
		return new ICDContentModel(owlModel).getICDCategoryClass();
	}

	/**** Internal reference ***/

	public EntityData createInternalReference(String projectName, EntityData entity, String referenceClassName,
			String referencePropertyName, String referencedValuePropertyName, EntityData referencedEntity, String user,
			String operationDescription) {

		Project project = getProject(projectName);
		KnowledgeBase kb = project.getKnowledgeBase();

		Instance instance = kb instanceof OWLModel ? edu.stanford.bmir.whofic.KBUtil.getRDFResource(((OWLModel) kb), entity.getName())
				: edu.stanford.bmir.whofic.KBUtil.getInstance(kb, entity.getName());

		if (instance == null) {
			throw new RuntimeException("Failed to import reference. Entity does not exist: " + entity.getName());
		}

		Slot referenceSlot = kb instanceof OWLModel ? edu.stanford.bmir.whofic.KBUtil.getRDFProperty(((OWLModel) kb), referencePropertyName)
				: edu.stanford.bmir.whofic.KBUtil.getSlot(kb, referencePropertyName);

		if (referenceSlot == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the reference property is not part of the ontology. Property name: "
					+ referencePropertyName);
		}

		Slot referencedValueSlot = kb instanceof OWLModel ? edu.stanford.bmir.whofic.KBUtil.getRDFProperty(((OWLModel) kb), referencedValuePropertyName)
				: edu.stanford.bmir.whofic.KBUtil.getSlot(kb, referencedValuePropertyName);

		if (referencedValueSlot == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the referenced value property is not part of the ontology. Property name: "
					+ referencedValuePropertyName);
		}

		Cls refClass = edu.stanford.bmir.whofic.KBUtil.getCls(kb, referenceClassName);

		if (refClass == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the reference class is not part of the ontology. Class name: " + referenceClassName);
		}

		Instance refEntity = edu.stanford.bmir.whofic.KBUtil.getInstance(kb, referencedEntity.getName());

		if (refEntity == null) {
			throw new RuntimeException("Could not create reference for " + entity.getName()
					+ " because the referenced entity not part of the ontology. Entity name: "
					+ referencedEntity.getName());
		}

		Instance refInstance = null;
		synchronized (kb) {
			WebProtegeKBUtil.morphUser(kb, user);

			boolean runsInTransaction = WebProtegeKBUtil.shouldRunInTransaction(operationDescription);
			try {
				if (runsInTransaction) {
					kb.beginTransaction(operationDescription);
				}

				refInstance = refClass.createDirectInstance(IcdIdGenerator.getNextUniqueId(kb));

				refInstance.setOwnSlotValue(referencedValueSlot, refEntity);

				instance.addOwnSlotValue(referenceSlot, refInstance);

				if (runsInTransaction) {
					kb.commitTransaction();
				}
			} catch (Exception e) {
				Log.getLogger().log(Level.SEVERE,
						"Could not create internal reference in " + projectName + " for entity " + entity.getName(), e);
				if (runsInTransaction) {
					kb.rollbackTransaction();
				}
				throw new RuntimeException("Could not create internal reference for entity " + entity.getName()
						+ ". Message: " + e.getMessage(), e);
			} finally {
				WebProtegeKBUtil.restoreUser(kb);
			}
		}

		return OntologyServiceImpl.createEntityData(refInstance);
	}

}
