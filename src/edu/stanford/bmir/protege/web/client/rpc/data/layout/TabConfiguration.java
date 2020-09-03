package edu.stanford.bmir.protege.web.client.rpc.data.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.GWT;

import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;

public class TabConfiguration extends GenericConfiguration implements Serializable{
	private static final long serialVersionUID = 9187571983105881720L;

	private String name;
	private String label;
	private String headerCssClass = null;
	private List<TabColumnConfiguration> columns = new ArrayList<TabColumnConfiguration>();
	private PortletConfiguration controllingPortlet;	

	public TabConfiguration() {		
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
    public String getHeaderCssClass() {
    	return getStringProperty(FormConstants.HEADER_CSS_CLASS, headerCssClass);
    }
    public void setHeaderCssClass(String className) {
        this.headerCssClass = className;
    }
	public List<TabColumnConfiguration> getColumns() {
		return columns;
	}
	public void setColumns(List<TabColumnConfiguration> columns) {
		this.columns = columns;
	}
	public PortletConfiguration getControllingPortlet() {
		return controllingPortlet;
	}
	public void setControllingPortlet(PortletConfiguration controllingPortlet) {
		this.controllingPortlet = controllingPortlet;
	}

	//convenience accessor methods to read values of commonly expected properties
	public boolean getClosable() {
		return getBooleanProperty(FormConstants.CLOSABLE, true);
	}
	
	public void removeAllPortlet(String javaClassName) {
		for (TabColumnConfiguration column : columns) {
			List<PortletConfiguration> portlets = column.getPortlets();
			for (Iterator<PortletConfiguration> iterator = portlets.iterator(); iterator.hasNext();) {
				PortletConfiguration portlet = (PortletConfiguration) iterator.next();
				if (portlet.getName().equals(javaClassName)) {
					iterator.remove();					
				}
			}
		}
	}
	
	public void removeColumn(int colIndex) {
		if (colIndex >= columns.size()) {
			GWT.log("Cannot remove column " + colIndex + " from tab configuration " + getName());
			return;
		}
		
		columns.remove(colIndex);
	}
	
	public void keepOnlyColumn(int colIndex) {
		if (colIndex >= columns.size()) {
			GWT.log("Cannot keep only column " + colIndex + " from tab configuration " + getName());
			return;
		}
		
		List<TabColumnConfiguration> newColumns = new ArrayList<TabColumnConfiguration>();
		newColumns.add(columns.get(colIndex));
		
		columns = newColumns;
	}
	
	public void setColumnWidth(int colIndex, float width) {
		TabColumnConfiguration tabColConfig = columns.get(colIndex);
		
		if (tabColConfig == null) {
			return;
		}
		
		tabColConfig.setWidth(width);
	}
}
