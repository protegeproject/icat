package edu.stanford.bmir.protege.web.client.ui.icd.pc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Grid;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.Panel;

import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.AbstractAsyncHandler;
import edu.stanford.bmir.protege.web.client.rpc.ICDServiceManager;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.icd.ScaleInfoData;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

@Deprecated
public class FixedScaleValuePresenter extends AbstractPropertyWidget {

	private Panel wrappingPanel;
	private Grid table;
	private Map<String, Integer> prop2RowMap = new HashMap<String, Integer>();

	protected  List<String> properties = new ArrayList<String>(); //stores allowed properties
	
	public FixedScaleValuePresenter(Project project) {
		super(project);
	}
    
    @Override
    public void setValues(Collection<EntityData> values) {
    	// Do nothing.
    	// The content of this widget changes using different mechanism 
    	// than setting values with setValues()    	
    }

	@Override
	public Component getComponent() {
		if (wrappingPanel == null) {
			createComponent();
		}
		return wrappingPanel;
	}

	@Override
	public Component createComponent() {
		wrappingPanel = new Panel();
		table = new Grid(1,2);
		fillTableContent();
		
		wrappingPanel.add(table);
		return wrappingPanel;
	}

	private void fillTableContent() {
		Map<String, String> allowedProps = getAllowedProperties();
		properties = new ArrayList<String>(allowedProps.keySet());
		ICDServiceManager.getInstance().getPostCoordinationAxesScales(
				getProject().getProjectName(), properties, 
				new GetPostCoordinationAxesScalesHandler(allowedProps));
	}

    public Map<String, String> getAllowedProperties() {
    	Map<String, String> allowedValues = UIUtil.getAllowedValuesConfigurationProperty(getWidgetConfiguration());
    	Map<String, String> valueToDisplayTextMap = new LinkedHashMap<String, String>();
        if (allowedValues != null) {
            for (String key : allowedValues.keySet()) {
                valueToDisplayTextMap.put(allowedValues.get(key), key);
            }
        }

        return valueToDisplayTextMap;
    }

    
	private boolean setVisible(String property, boolean flag) {
		Integer rowIndex = prop2RowMap.get(property);
		if (rowIndex != null) {
			int row = rowIndex.intValue();
			if (flag) {
				//System.out.println("Show it! (" + property + ")");
				table.getRowFormatter().setStyleName(row, "table-row-visible");
			}
			else {
				//System.out.println("Hide it! (" + property + ")");
				table.getRowFormatter().setStyleName(row, "table-row-hidden");
			}
			return !flag;
		}
		return false;
	}

	protected class GetPostCoordinationAxesScalesHandler extends AbstractAsyncHandler<List<ScaleInfoData>> {
		
		private Map<String, String> properties;
		
	    public GetPostCoordinationAxesScalesHandler(Map<String, String> properties) {
			this.properties = properties;
		}

		@Override
		public void handleFailure(Throwable caught) {
	        GWT.log("Error at getting scale information for post-cooordination axes properties " + properties, caught);
	    	for (String propertyName : properties.keySet()) {
	    		setVisible(propertyName, true);
	    	}
		}

		@Override
	    public void handleSuccess(List<ScaleInfoData> validPropertiesScaleInfo) {
			
        	for (ScaleInfoData scaleInfo : validPropertiesScaleInfo) {
        		String propertyName = scaleInfo.getProperty().getName();
        		
        		int row = table.getRowCount();
        		table.insertRow(row);
        		table.getCellFormatter().setStyleName(row, 0, "form_label");
        		table.setText(row, 0, properties.get(scaleInfo.getProperty().getName()));
        		table.setHTML(row, 1, createHTMLForScaleInfoData(scaleInfo));
    			setVisible(propertyName, true);
    			prop2RowMap.put(propertyName, row);
        	}

	    }
		
		private String createHTMLForScaleInfoData(ScaleInfoData scaleInfo) {
			Grid g = new Grid(1,2);
			g.setText(0, 0, "Value");
			g.setText(0, 1, "Definition");
			g.getRowFormatter().setStyleName(0, "fixedscalevalues-header");
			
    		for (int i = 0; i < scaleInfo.getValueCount(); i++) {
    			int row = i + 1;
				g.insertRow(row);
				EntityData scaleValue = scaleInfo.getScaleValue(i);
				if (scaleValue != null) {
					g.setText(row, 0, scaleValue.getBrowserText());
					String scaleValueDef = scaleInfo.getDefinition(i);
					g.setText(row, 1, scaleValueDef == null ? "" : scaleValueDef);
				}
			}
			
			return "<table class=\"fixedscalevalues-table\"> " + g.getElement().getInnerHTML() + " </table>";
		}
	}
	
	public void show(String propertyName) {
		setVisible(propertyName, true);
	}

	public void hide(String propertyName) {
		setVisible(propertyName, false);
	}

}
