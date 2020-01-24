package edu.stanford.bmir.protege.web.client.rpc.data.layout;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class GenericConfiguration implements Serializable {
	private static final long serialVersionUID = 7898127979213L;
	
	private Map<String, Object> properties;

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void addPropertyValue(String key, Object value) {
		properties.put(key, value);
	}
	
    public String getStringProperty(String prop, String defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        try {
            String value = (String) properties.get(prop);
            if (value == null) {
                value = defaultValue;
            }
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    public int getIntegerProperty(String prop, int defaultValue) {
    	if (properties == null) {
    		return defaultValue;
    	}
    	try {
    		Integer value = (Integer) properties.get(prop);
    		if (value == null) {
    			value = defaultValue;
    		}
    		return value.intValue();
    	} catch (Exception e) {
        	try {
        		String value = (String) properties.get(prop);
        		return Integer.parseInt(value);
        	} catch (Exception se) {
        		return defaultValue;
        	}
    	}
    }

    public boolean getBooleanProperty(String prop, boolean defaultValue) {
        if (properties == null) {
            return defaultValue;
        }
        try {
        	Boolean value = (Boolean) properties.get(prop);
            if (value == null) {
                value = defaultValue;
            }
            return value.booleanValue();
        } catch (Exception e) {
        	try {
        		String value = (String) properties.get(prop);
        		return Boolean.parseBoolean(value);
        	} catch (Exception se) {
        		return defaultValue;
        	}
        }
    }
	

	/**	 * 
	 * @return <ul>
	 *               <li><code>true</code> if the user is member of a group whose name is listed 
	 *               under the "showOnlyForGroups" property, </li>
	 *               <li><code>false</code> if the "showOnlyForGroups" property is defined but
	 *               the user is not member of a group whose name is listed under that property, </li>
	 *               <li><code>false</code> if the user is member of a group whose name is listed 
	 *               under the "doNotShowForGroups" property, </li>
	 *               <li><code>true</code> if the "doNotShowForGroups" property is defined but
	 *               the user is not member of a group whose name is listed under that property, </li>
	 *               <li><code>true</code> in case neither property is defined.</li>
	 * @see AbstractPropertyWidget.userPartOfWriteAccessGroup
	 */
    public boolean userPartOfShowGroup() {
        if (!GlobalSettings.getGlobalSettings().isLoggedIn()) {
            return false;
        }

        Collection<String> userGroups = GlobalSettings.getGlobalSettings().getUser().getGroups();
        if (userGroups == null) {
            return false;
        }

        List<String> doNotShowGroups = UIUtil.getListConfigurationProperty(getProperties(), FormConstants.DO_NOT_SHOW_FOR_GROUPS);

        if (doNotShowGroups != null) {
	        for (String doNotshowGroup : doNotShowGroups) {
	            if (userGroups.contains(doNotshowGroup)) {
	                return false;
	            }
	        }
        }

        List<String> showGroups = UIUtil.getListConfigurationProperty(getProperties(), FormConstants.SHOW_ONLY_FOR_GROUPS);

        if (showGroups != null) {
	        for (String showGroup : showGroups) {
	            if (userGroups.contains(showGroup)) {
	                return true;
	            }
	        }
	        return false;
        }
        
        return true;
    }
	
}
