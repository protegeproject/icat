package edu.stanford.bmir.protege.web.client.ui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.gwtext.client.core.ExtElement;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.MessageBoxConfig;
import com.gwtext.client.widgets.WaitConfig;

import edu.stanford.bmir.protege.web.client.model.GlobalSettings;
import edu.stanford.bmir.protege.web.client.model.PermissionConstants;
import edu.stanford.bmir.protege.web.client.model.Project;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.PropertyEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.SubclassEntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.GenericConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.PortletConfiguration;
import edu.stanford.bmir.protege.web.client.rpc.data.layout.ProjectConfiguration;
import edu.stanford.bmir.protege.web.client.ui.portlet.propertyForm.FormConstants;

public class UIUtil {

    protected static final String HELP_ICON_STYLE_STRING = "style=\"position:relative; top:2px;\"";

    public static final String TRANSACTION_APPLY_TO_TRAILER_STRING = " -- Apply to: ";
    public static final String LOCAL = "#local_";
    public static final String ADD_PREFIX = "add_parent";
    public static final String REMOVE_PREFIX = "remove_parent_";
    public static final String GOTO_PREFIX = "goto_parent_";
    private static String[] noteTypes = new String[] {"Comment"};   //default value

    public static void showLoadProgessBar(final String message, final String barMessage) {
        MessageBox.show(new MessageBoxConfig() {
            {
                setMsg(message);
                setProgressText(barMessage);
                setDefaultTextHeight(30);
                setWidth(300);
                setWait(true);
                setClosable(true);
                setTitle("Dialog");
                setWaitConfig(new WaitConfig() {
                    {
                        setInterval(200);
                    }
                });
                //setAnimEl(button.getId());
            }
        });
    }

    public static void hideLoadProgessBar() {
        MessageBox.hide();
    }

    public static void mask(final ExtElement el, final String message, final boolean animate, int delayInMilliSeconds) {
        Timer timer = new Timer() {
            @Override
            public void run() {
                el.mask(message, animate);
            }
        };
        if (delayInMilliSeconds > 0) {
            timer.schedule(delayInMilliSeconds);
        }
    }

    public static void unmask(ExtElement el) {
        el.unmask();
    }

    public static String getDisplayText(Object object) {
        if (object == null) {
            return "";
        }
        if (object instanceof EntityData) {
            String browserText = ((EntityData) object).getBrowserText();
            if (browserText == null) {
                browserText = ((EntityData) object).getName();
                
                if (browserText == null) {
                	return "";
                }
            }
            return removeSingleQuotes(browserText);
        } else {
            return object.toString();
        }
    }
    
    public static String removeSingleQuotes(String str) {
    	if (str == null) {
    		return null;
    	}
    	
    	if (str.startsWith("'") && str.endsWith("'")) {
    		return str.substring(1, str.length() - 1);
    	}
    	
    	return str;
    }

    public static String getNiceNoteCountText(final int noteCount) {
    	return noteCount == 1 ? "There is 1 note" : "There are " + noteCount + " notes";
    }

    public static String getHelpImageHtml(String helpURL, String hoverText) {
        return  "&nbsp;<a href=\"" + helpURL + "\" target=\"_blank\" " + HELP_ICON_STYLE_STRING + ">" +
        "<img src=\"images/help.gif\" width=\"14\" title=\"" + hoverText + "\" /></a> ";
    }

    /**
     * This method returns the short name of an entity in the ontology. It only
     * returns the string after the "#" char.
     *
     * @param fullUri
     *            - the fully qualified name of the entity
     * @return - the short name (string after #)
     */
    public static String getShortName(String fullUri) {
        int index = fullUri.lastIndexOf("#");
        return fullUri.substring(index + 1);
    }

    public static String getStringConfigurationProperty(GenericConfiguration config, String prop, String defaultValue) {
        return (config == null ? defaultValue : config.getStringProperty(prop, defaultValue));
    }

    public static int getIntegerConfigurationProperty(GenericConfiguration config, String prop, int defaultValue) {
    	return (config == null ? defaultValue : config.getIntegerProperty(prop, defaultValue));
    }

    public static boolean getBooleanConfigurationProperty(GenericConfiguration config, String prop, boolean defaultValue) {
    	return (config == null ? defaultValue : config.getBooleanProperty(prop, defaultValue));
    }

    public static <T> List<T> getListConfigurationProperty(GenericConfiguration config, String prop, List<T> defaultValue) {
    	return (config == null ? defaultValue : config.getListProperty(prop, defaultValue));
    }

    public static String getStringConfigurationProperty(Map<String, Object> config, String prop, String defaultValue) {
    	if (config == null) {
    		return defaultValue;
    	}

		try {
    		String strValue = (String) config.get(prop);
    		return (strValue == null ? defaultValue : strValue);
		}
		catch (Exception e) {
			return defaultValue;
		}
    }

    public static int getIntegerConfigurationProperty(Map<String, Object> config, String prop, int defaultValue) {
    	if (config == null) {
    		return defaultValue;
    	}

    	try {
    		Integer intValue = (Integer) config.get(prop);
    		return (intValue == null ? defaultValue : intValue.intValue());
    	}
    	catch (Exception e) {
    		try {
        		String strValue = (String) config.get(prop);
        		return (strValue == null ? defaultValue : Integer.parseInt(strValue));
    		}
    		catch (Exception se) {
    			return defaultValue;
    		}
    	}
    }

    public static boolean getBooleanConfigurationProperty(Map<String, Object> config, String prop, boolean defaultValue) {
    	if (config == null) {
    		return defaultValue;
    	}

    	try {
    		Boolean boolValue = (Boolean) config.get(prop);
    		return (boolValue == null ? defaultValue : boolValue.booleanValue());
    	}
    	catch (Exception e) {
    		try {
        		String strValue = (String) config.get(prop);
        		return (strValue == null ? defaultValue : Boolean.parseBoolean(strValue));
    		}
    		catch (Exception se) {
    			return defaultValue;
    		}
    	}
    }

    public static <T> List<T> getListConfigurationProperty(Map<String, Object> config, String prop) {
    	return getListConfigurationProperty(config, prop, null);
    }

    public static <T> List<T> getListConfigurationProperty(Map<String, Object> config, String prop, List <T> defaultValue) {
        if (config == null) {
            return defaultValue;
        }
        try {
        	@SuppressWarnings("unchecked")
			List<T> listValue = (List<T>) config.get(prop);
        	if (listValue == null) {
        		return defaultValue;
        	}
            return listValue;
        }
        catch (Exception e) {
            return defaultValue;
        }
    }

    public static <K, V> Map<K, V> getMapConfigurationProperty(Map<String, Object> config, String prop) {
        if (config == null) {
            return null;
        }
        try {
        	@SuppressWarnings("unchecked")
			Map<K, V> mapValue = (Map<K, V>) config.get(prop);
            return mapValue;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * Returns a {@link Map} of allowed values from a widget configuration,
     * where the keys in the map represent the different possible options (labels) displayed on the screen,
     * and for each key the associated values in the map are the underlying value in the ontology
     * corresponding for that label.
     * <p>
     * If the allowed values in the configuration file is represented by a
     * simple list of String values, this method will build a map from it,
     * where to each key the exact same value will correspond.
     *
     * @param config a widget configuration represented by a String-to-Object map
     * @return the "allowed values" map
     */
    @SuppressWarnings("unchecked")
	public static Map<String, String> getAllowedValuesConfigurationProperty(Map<String, Object> config) {
        if (config == null) {
            return null;
        }
        try {
            Map<String, String> allowedValues = null;
            Object allowedValuesObject = config.get(FormConstants.ALLOWED_VALUES);
            if (allowedValuesObject instanceof Map) {
                allowedValues = (Map<String, String>)allowedValuesObject;
            }
            else if(allowedValuesObject instanceof List) {
                List<String> allowedValuesList = (List<String>) allowedValuesObject;
                allowedValues = new LinkedHashMap<String, String>();
                for (String allowedValue : allowedValuesList) {
                    allowedValues.put(allowedValue, allowedValue);
                }
            }
            return allowedValues;
        }
        catch (Exception e) {
            return null;
        }
    }

    public static String getStringConfigurationProperty(
            Map<String, Object> config, ProjectConfiguration projectConfiguration, String prop, String defaultValue) {
        String projectDefaultValue = getStringConfigurationProperty(projectConfiguration, prop, defaultValue);
        String localValue = getStringConfigurationProperty(config, prop, projectDefaultValue);
        
        if (localValue == null) {
        	return defaultValue;
        }
        
        if (localValue.startsWith("@") && localValue.endsWith("@")) {
        	String projectProperty = localValue.substring(1, localValue.length() - 1);
        	return getStringConfigurationProperty(projectConfiguration, projectProperty, localValue);
        }
        else {
        	return localValue;
        }
    }

    public static int getIntegerConfigurationProperty(
            Map<String, Object> config, ProjectConfiguration projectConfiguration, String prop, int defaultValue) {
        int projectDefaultValue = getIntegerConfigurationProperty(projectConfiguration, prop, defaultValue);
        return getIntegerConfigurationProperty(config, prop, projectDefaultValue);
    }

    public static boolean getBooleanConfigurationProperty(
            Map<String, Object> config, ProjectConfiguration projectConfiguration, String prop, boolean defaultValue) {
        boolean projectDefaultValue = getBooleanConfigurationProperty(projectConfiguration, prop, defaultValue);
        return UIUtil.getBooleanConfigurationProperty(config, prop, projectDefaultValue);
    }

    public static <T> List<T> getListConfigurationProperty(
    		Map<String, Object> config, ProjectConfiguration projectConfiguration, String prop, List<T> defaultValue) {
    	List<T> projectDefaultValue = getListConfigurationProperty(projectConfiguration, prop, defaultValue);
    	return UIUtil.getListConfigurationProperty(config, prop, projectDefaultValue);
    }

    public static void setConfigurationValue(Map<String, Object> config, String prop, Object value) {
        if (config == null) {  return;  }
        config.put(prop, value);
    }

    public static void setConfigurationPropertyValue(PortletConfiguration config, String prop, Object value) {
        if (config == null) {  return;  }
        Map<String, Object> props = config.getProperties();
        if (props == null) {
            props = new HashMap<String, Object>();
            config.setProperties(props);
        }
        props.put(prop, value);
    }


    public static String getAppliedToTransactionString(String operationDescription, String applyTo) {
        return operationDescription + TRANSACTION_APPLY_TO_TRAILER_STRING + applyTo;
    }

    public static void setNoteTypes(String[] noteTypes) {
        UIUtil.noteTypes = noteTypes;
    }

    public static String[] getNoteTypes() {
        return UIUtil.noteTypes;
    }

    /**
     * Returns the first item in the collection or null if the collection is empty
     */
    public static <X> X getFirstItem(Collection<X> c) {
        X o;
        if (c == null || c.isEmpty()) {
            o = null;
        } else if (c instanceof List) {
            o = ((List<X>) c).get(0);
        } else {
            o = c.iterator().next();
        }
        return o;
    }

    public static <X> Collection<X> createCollection(X o) {
        return createList(o);
    }

    public static <X> List<X> createList(X o) {
        List<X> c;
        if (o == null) {
            c = Collections.emptyList();
        } else {
            c = new ArrayList<X>(1);
            c.add(o);
        }
        return c;
    }

    public static <X> String prettyPrintList(Collection<X> c) {
        return prettyPrintList(c, ", ");
    }

    public static <X> String prettyPrintList(Collection<X> c, String separator) {
        StringBuffer text = new StringBuffer();
        if (c == null) { return "(empty)"; }
        Iterator<X> i = c.iterator();
        while (i.hasNext()) {
            Object o = i.next();
            if (text.length() > 0) {
                text.append(separator);
            }
            text.append(getDisplayText(o));
        }
        return text.toString();
    }

    public static boolean confirmIsLoggedIn() {
        if (GlobalSettings.getGlobalSettings().isLoggedIn()) {
            return true;
        } else {
            MessageBox.alert("Sign in", "Please sign in first.");
            return false;
        }
    }

    public static boolean checkWriteOperationAllowed(Project project, boolean showUserAlerts) {
        return checkOperationAllowed(project, PermissionConstants.WRITE, "", "", true, showUserAlerts);
    }

    public static boolean checkOperationAllowed(Project project, String operation, String title, String text, boolean checkWritePermission, boolean showUserAlerts) {
        if (GlobalSettings.getGlobalSettings().isLoggedIn() == false) {
            if (showUserAlerts) {
                MessageBox.alert("Sign in", "Please sign in first.");
            }
            return false;
        }

        if (checkWritePermission && project.hasPermission(PermissionConstants.WRITE) == false) {
            if (showUserAlerts) {
                MessageBox.alert("Warning", "Your user account does not have write permission.");
            }
            return false;
        }

         if (project.hasPermission(operation) == false) {
                if (showUserAlerts) {
                    MessageBox.alert(title, text);
                }
                return false;
         }
         return true;
    }


    public static Collection<String> getStringCollection(Collection<EntityData> entities) {
        Collection<String> coll = new HashSet<String>();
        if (entities == null) {
            return coll;
        }
        for (EntityData entity : entities) {
            coll.add(entity.getName());
        }
        return coll;
    }

    public static String replaceEOLWithBR(String str) {
        return str.replaceAll("\n", "<br />");
    }

    public static boolean equals(EntityData e1, EntityData e2) { //probably not the best class for it..
        if (e1 == null & e2 == null) { return true; }
        if (e1 != null && e2 != null) { return e1.equals(e2); }
        return false;
    }

    public static String removeHTMLTags(String html) { //to be tested in all browsers
        HTML htmlW = new HTML(html);
        return htmlW.getElement().getInnerText();
    }

    public static int getIdentifierStart(String text, int currPos) {
        if (text == null) {
            return -1;
        }
        if (currPos > text.length()) {
            currPos = text.length();
        }

        int res = currPos;
        while (res > 0 && isIdentifierPart(text.charAt(res-1))) {
            res--;
        }
        return res;
    }

    public static int getIdentifierEnd(String text, int currPos) {
        if (text == null) {
            return -1;
        }
        if (currPos < 0) {
            currPos = 0;
        }

        int res = currPos;
        while (res < text.length() && isIdentifierPart(text.charAt(res))) {
            res++;
        }
        return res;
    }
    private static boolean isIdentifierPart(char ch) {
        boolean res = false;
        res = res || Character.isLetter(ch);
        res = res || Character.isDigit(ch);
        res = res || ch == '_';
        res = res || ch == '-';
        res = res || ch == '.';
        res = res || ch == ':';
        return res;
    }

    static final String OWL_CLASS_ID = "http://www.w3.org/2002/07/owl#Class";
    static final String RDFS_CLASS_ID = "http://www.w3.org/2000/01/rdf-schema#Class";
    static final String RDF_PROPERTY_ID = "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property";
    static final String OWL_DATA_PROPERTY_ID = "http://www.w3.org/2002/07/owl#DataProperty";
    static final String OWL_OBJ_PROPERTY_ID = "http://www.w3.org/2002/07/owl#ObjectProperty";

    public static ValueType guessValueType(EntityData entityData) {
        ValueType valueType = entityData.getValueType();
        if (valueType != null) {
            return valueType;
        }

        if (entityData instanceof SubclassEntityData) {
            return ValueType.Cls;
        }

        if(entityData instanceof PropertyEntityData) {
            return ValueType.Property;
        }

        Collection<EntityData> types = entityData.getTypes();
        if (types == null) {
            return null;
        }

        for (EntityData type : types) {
            String typeName = type.getName();
            if (typeName.equals(OWL_CLASS_ID) || typeName.equals(RDFS_CLASS_ID)) {
                return ValueType.Cls;
            } else if (typeName.equals(RDF_PROPERTY_ID) || typeName.equals(OWL_DATA_PROPERTY_ID) || typeName.equals(OWL_OBJ_PROPERTY_ID)) {
                return ValueType.Property;
            }
        }

        return null;
    }
    
    public static boolean calculateVisibilityBasedOnSubjectType( Collection<EntityData> types,
    		List<String> showOnlyForTypesList, List<String> doNotShowForTypesList) {
    	
    	//GWT.log("calculateVisibilityBasedOnSubjectType: types: " + types + " showOnlyFor: " + showOnlyForTypesList + " doNotShowFor: " + doNotShowForTypesList);

    	if (types == null || types.isEmpty()) {
    		if ( showOnlyForTypesList != null && showOnlyForTypesList.size() > 0 ) {
    			//TODO hide this (if not already hidden)
    			return false;
    		}
    		return true;
    	}
    	else {	// we have type information
    		boolean foundANecessaryType = (showOnlyForTypesList == null || showOnlyForTypesList.isEmpty() ? true : false);
    		
    		if ( foundANecessaryType && (doNotShowForTypesList == null || doNotShowForTypesList.isEmpty()) ) {
    			return true;
    		}
    		
    		for (EntityData type : types) {
    			String typeName = type.getName();
    			if (doNotShowForTypesList != null && doNotShowForTypesList.contains(typeName)) {
        			//TODO hide this (if not already hidden)
    				return false;
    				//break;
    			}
    			if (showOnlyForTypesList != null && showOnlyForTypesList.contains(typeName)) {
    				foundANecessaryType = true;
    				if (doNotShowForTypesList == null) {
    					break;
    				}
    			}
    		}
    		
    		return foundANecessaryType;
    	}
    }

	
//	public static <T> boolean differentCollections(Collection<T> oldValues, Collection<T> newValues) {
//		if (oldValues == null) {
//			if (newValues == null) {
//				//they are the same (both are null)
//				return false;
//			}
//			else {
//				//they are different (one is null the other is not)
//				return true;
//			}
//		}
//		else {
//			if (newValues == null) {
//				//they are different (one is null the other is not)
//				return true;
//			}
//			else {
//				if (oldValues.size() != newValues.size()) {
//					//they are different (they have different size)
//					return true;
//				}
//				else {
//					//if retaining all values in newValues in oldValues would modify oldValues
//					//(i.e. the intersection of the two collection is different from oldValues)
//					//then return true, otherwise they must be equal, so return false.
//					return oldValues.retainAll(newValues);
//				}
//			}
//		}
//	}

	
//	private boolean listsHaveDifferentElements(List<String> first, List<String> second) {
	public static <T> boolean differentCollections(Collection<T> first, Collection<T> second) {
		//check whether both lists are null
		if (first == null && second == null) {
			//they are the same (both are null)
			return false;
		}
		
		//if we are here, then at least one of the lists is not null
		if (first == null || second == null) {
			//they are different (one is null, the other is not)
			return true;
		}
		
		//if we are here, then both lists are non-null
		if ( first.size() != second.size() ) {
			//they are different (they have different size)
			return true;
		}
		
		//if we are here, both lists are of the same size
		//return true iff one of the collection does NOT contain all the elements of the other
		return ! first.containsAll( second );
	}

}



