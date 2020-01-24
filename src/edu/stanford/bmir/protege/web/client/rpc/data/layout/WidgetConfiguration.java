package edu.stanford.bmir.protege.web.client.rpc.data.layout;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.ui.portlet.AbstractPropertyWidget;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;
import edu.stanford.bmir.protege.web.client.ui.util.UIUtil;

public class WidgetConfiguration extends GenericConfiguration {

	private static final long serialVersionUID = -7404647187863928324L;

	public WidgetConfiguration(Map<String, Object> configMap) {
		setProperties(configMap);
	}
	

	/**	 * 
	 * @return a list of values to be shown to the user 
	 * based on his/her membership in groups whose name are listed 
	 * under the "group_specific_allowed_values" property, 
	 * or <code>null</code> if the user is not member of any of the goups
	 * with restricted values
	 * @see AbstractPropertyWidget.userPartOfWriteAccessGroup
	 */
    public Set<String> getUserSpecificAllowedValues() {
        if (!GlobalSettings.getGlobalSettings().isLoggedIn()) {
            return null;
        }

        Map<String, List<String>> groupSpecificAllowedValues = UIUtil.getMapConfigurationProperty(
        		getProperties(), FormConstants.GROUP_SPECIFIC_ALLOWED_VALUES);
        if (groupSpecificAllowedValues == null) {
            return null;
        }
        Collection<String> userGroups = GlobalSettings.getGlobalSettings().getUser().getGroups();
        if (userGroups == null) {
            return null;
        }
        Set<String> res = null;
        for (String group : groupSpecificAllowedValues.keySet()) {
            if (userGroups.contains(group)) {
            	List<String> allowedValuesForGroup = groupSpecificAllowedValues.get(group);
            	if (res == null) {
            		res = new HashSet<String>();
            	}
            	res.addAll(allowedValuesForGroup);
            }
        }

        return res;
    }

}
