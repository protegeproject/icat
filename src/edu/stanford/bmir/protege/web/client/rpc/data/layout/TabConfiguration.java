package edu.stanford.bmir.protege.web.client.rpc.data.layout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

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

	/**	 * 
	 * @return true if the user is member of a group whose name is listed 
	 *               under the "showOnlyForGroups" property, or the property is not defined
	 * @see AbstractPropertyWidget.userPartOfWriteAccessGroup
	 */
    public boolean userPartOfShowGroup() {
        if (!GlobalSettings.getGlobalSettings().isLoggedIn()) {
            return false;
        }

        List<String> showGroups = UIUtil.getListConfigurationProperty(getProperties(), FormConstants.SHOW_ONLY_FOR_GROUPS);
        if (showGroups == null) {
            return true;
        }
        Collection<String> userGroups = GlobalSettings.getGlobalSettings().getUser().getGroups();
        if (userGroups == null) {
            return false;
        }
        for (String showGroup : showGroups) {
            if (userGroups.contains(showGroup)) {
                return true;
            }
        }

        return false;
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
}
