package edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.gwtext.client.data.FieldDef;
import com.gwtext.client.data.MemoryProxy;
import com.gwtext.client.data.Record;
import com.gwtext.client.data.RecordDef;
import com.gwtext.client.data.Store;
import com.gwtext.client.widgets.grid.ColumnConfig;
import com.gwtext.client.widgets.grid.ColumnModel;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.OntologyServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValues;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityPropertyValuesList;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;


/**
 * This widget should provide similar functionality to its superclass,
 * {@link InstanceGridWidget}, but would allow multiple level of property value redirection. 
 * So, if an {@code InstanceGridWidget} contains in each row information about a 
 * given individual, which is a property value of the selected "subject", 
 * by displaying in the columns different properties of that individual, 
 * a {@code MultilevelInstanceGridWidget} will allow the columns to be property values 
 * not only of the "main" individual represented by the row, 
 * but also property values of any other column value 
 * in case the other column value represents an individual.
 * 
 * @author csnyulas
 *
 */
public class MultilevelInstanceGridWidget extends InstanceGridWidget {

	protected int[] subjectEntityColumns;
	
	
	public MultilevelInstanceGridWidget(Project project) {
		super(project);
		
		//since the properties presented in the columns are not necessarily unique
		//as in the case of InstanceGridWidget
		// (e.g. we could have two columns displaying rdfs:label properties, 
		// one on the individual represented by the row, 
		// and one on the value of another column), 
		//the simple cross reference maps declared in InstanceGridWidget 
		//will not provide adequate information 
		//to navigate from properties to columns and vice-versa.
		prop2Index = null;
	}

	
    protected void createColumns() {
        Map<String, Object> widgetConfig = getWidgetConfiguration();
        if (widgetConfig == null) {
            return;
        }

        int colCount = 0;

        for (String key : widgetConfig.keySet()) {
            if (key.startsWith(FormConstants.COLUMN_PREFIX)) {
                colCount++;
            }
        }

        FieldDef[] fieldDef = new FieldDef[colCount + getExtraColumnCount()];
        ColumnConfig[] columns = new ColumnConfig[colCount + getExtraColumnCount()];
        String[] props = new String[colCount];
        subjectEntityColumns = new int[colCount];
        
        for (String key : widgetConfig.keySet()) {
            if (key.startsWith(FormConstants.COLUMN_PREFIX)) {
                Map<String, Object> columnConfig = (Map<String, Object>) widgetConfig.get(key);
                
                String property = getPropertyNameFromConfig(columnConfig);
                int index = getColumnIndexFromConfig(columnConfig);
                int indexOfSubjCol = getColumnIndexOfSubjectColumnFromConfig(columnConfig);
                props[index] = property;
                ////prop2Index.put(property, index);////
                subjectEntityColumns[index] = indexOfSubjCol;
                
                String cloneOf = isCloneColumn(columnConfig);
                if (cloneOf != null) {
                	Map<String, Object> origColumnConfig = getOriginalOfClone(widgetConfig, columnConfig);
                	createCloneColumn(columnConfig, origColumnConfig, cloneOf, fieldDef, columns, property, index);
                }
                else {
                	createColumn(columnConfig, fieldDef, columns, property, index);
                }
            }
        }

        properties = Arrays.asList(props);
//        for (int i = 0; i < props.length; i++) {
//            prop2Index.put(props[i], i);
//        }

        createInstanceColumn(fieldDef, columns, colCount);
        createActionColumns(fieldDef, columns, colCount);

        recordDef = new RecordDef(fieldDef);

        ColumnModel columnModel = new ColumnModel(columns);
        getGridPanel().setColumnModel(columnModel);
    }

    /**
     * Returns the column index of the subject entity to which a property column applies, 
     * or -1 in case the property applies to the row entity.
     * 
     * @param columnConfig a column configuration in the form of a <String, Object> map
     * @return a column index between 0 and n-1, where n is the total number of columns, or -1
     */
	protected final int getColumnIndexOfSubjectColumnFromConfig(Map<String, Object> columnConfig) {
		String indexStr = (String) columnConfig.get(FormConstants.INDEX_OF_SUBJECT_COLUMN);
		int index = -1;	//default value, for the case the property refers to the row entity
		if (indexStr != null) {
			index = Integer.parseInt(indexStr) - 1; //better be valid
		}
		return index;
	}

    protected final int getIndexOfProperty(String prop) {
        String errMsg = "The 'getIndexOfProperty' method should not be called on the 'MultilevelInstanceGridWidget' class";
		assert false : errMsg;
        System.out.println("WARNING! " + errMsg);
        return -1;
    }

    @Override
    protected void fillValues(List<String> subjects, List<String> props) {
        getStore().removeAll();
        //FIXME shadowStore.removeAll();
        OntologyServiceManager.getInstance().getMultilevelEntityPropertyValues(
        		getProject().getProjectName(), subjects, UIUtil.getFirstItem(props), properties, 
                subjectEntityColumns, new GetTriplesHandler(getSubject()));
    }


    protected class GetTriplesHandler extends AbstractAsyncHandler<List<EntityPropertyValuesList>> {

        private EntityData mySubject = null;

        public GetTriplesHandler(EntityData subject) {
            mySubject = subject;
        }

        @Override
        public void handleFailure(Throwable caught) {
            GWT.log("Instance Grid Widget: Error at getting triples for " + getSubject(), caught);
            updateActionLinks(isReplace());
        }

        @Override
        public void handleSuccess(List<EntityPropertyValuesList> entityPropertyValues) {
            if (!UIUtil.equals(mySubject, getSubject())) {  return; }
            Store store = getStore();
            Store shadowStore = getShadowStore();
            
            store.removeAll();
            shadowStore.removeAll();

            if (entityPropertyValues != null) {
                Object[][] data = createDataArrayFromValueList(entityPropertyValues);
                store.setDataProxy(new MemoryProxy(data));
                store.load();

                //load also the shadow store
                Object[][] shadowData = createDataArrayFromValueList(entityPropertyValues, true);
                shadowStore.setDataProxy(new MemoryProxy(shadowData));
                shadowStore.load();
                
//TODO see if we really want to support this here
//                if (fieldNameSorted != null) {
//                    store.sort(fieldNameSorted, SortDir.ASC);   //WARNING! This seems to be very slow
//                }
            }

            setOldDisplayedSubject(getSubject());

            updateActionLinks(isReplace());
            setLoadingStatus(false);

        }
    }

    @Override
    protected Object[][] createDataArray(List<EntityPropertyValues> entityPropertyValues, boolean asEntityData) {
    	System.out.println("Make sure that this method will not be called");
        int i = 0;
        Object[][] data = new Object[entityPropertyValues.size()][properties.size() + getExtraColumnCount()];
        for (EntityPropertyValues epv : entityPropertyValues) {
        	int j = 0;
            for (PropertyEntityData ped : epv.getProperties()) {
                if (asEntityData == true) {
                    List<EntityData> values = epv.getPropertyValues(ped);
                    //FIXME: just take the first
                    EntityData value = UIUtil.getFirstItem(values);
                    data[i][j] = value;
                } else {
                    data[i][j] = getCellText(epv, ped);
                }
                j++;
            }

            if (!asEntityData) {
                setExtraColumnValues(data[i], epv);
            }
            i++;
        }
        return data;
    }

    protected Object[][] createDataArrayFromValueList(List<EntityPropertyValuesList> entityPropertyValues) {
        return createDataArrayFromValueList(entityPropertyValues, false);
    }

    protected Object[][] createDataArrayFromValueList(List<EntityPropertyValuesList> entityPropertyValues, boolean asEntityData) {
        int i = 0;
        Object[][] data = new Object[entityPropertyValues.size()][properties.size() + getExtraColumnCount()];
        for (EntityPropertyValuesList epv : entityPropertyValues) {
            for (int j = 0; j < properties.size(); j++) {
                if (asEntityData == true) {
                    List<EntityData> values = epv.getPropertyValues(j);
                    //FIXME: just take the first
                    EntityData value = UIUtil.getFirstItem(values);
                    data[i][j] = value;
                } else {
                    data[i][j] = UIUtil.prettyPrintList(epv.getPropertyValues(j));
                }
            }

            if (!asEntityData) {
                setExtraColumnValues(data[i], epv);
            }
            i++;
        }
        return data;
    }

    /**
     * Fills the extra column values for a data row, based on an EntityPropertyValuesList.
     * <B>Important note:</B> Please make sure that both this method and all the
     * methods that override this method will correctly handle the situation when
     * the property-value map in <code>epv</code> is empty, in case when a new instance
     * is created in the grid.
     *
     * @param datarow a grid data row
     * @param epv an EntityPropertyValuesList instance.
     */
    protected void setExtraColumnValues(Object[] datarow, EntityPropertyValuesList epv) {
        setExtraColumnValues(datarow, epv.getSubject());
    }

    @Override
    protected String getSubjectOfPropertyValue(Record record, int rowIndex, int colIndex) {
    	int subjColIndex = subjectEntityColumns[colIndex];
		if (subjColIndex == -1) {
    		//return super.getSubjectOfPropertyValue(record, column);
    		return record.getAsString(INSTANCE_FIELD_NAME);
    	}
    	else {
    		EntityData subjEntityData = (EntityData) getShadowStore().getAt(rowIndex).getAsObject(getPropertyFieldName(subjColIndex));
    		return subjEntityData.getName();
    	}
    }
}
