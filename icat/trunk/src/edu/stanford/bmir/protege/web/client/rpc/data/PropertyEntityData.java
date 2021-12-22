package edu.stanford.bmir.protege.web.client.rpc.data;

import java.util.Collection;
import java.util.List;
import com.google.gwt.regexp.shared.RegExp;

public class PropertyEntityData extends EntityData {

	private static final long serialVersionUID = 3996847017712492831L;

    private List<EntityData> allowedValues;
    private int minCardinality = 0;
    private int maxCardinality = -1;
    private PropertyType propertyType; //TODO - temporary solution, will be removed

    static RegExp INDEX_REGEX_PATTERN = RegExp.compile("(.*)\\[\\d+\\]$");

    public PropertyEntityData() {
        this(null, null);
    }

    public PropertyEntityData(String name) {
        this(name, name);
    }
    
    public PropertyEntityData(String name, String browserText) {
    	this(name, browserText, null);
    }

    public PropertyEntityData(String name, String browserText, Collection<EntityData> types) {
        super(name, browserText, types);

        //TODO: See if we can remove this from here, e.g. by creating a specialized subclass 
        //		and use that instead of this everywhere(!!!) where we deal with properties coming 
        //		from the configuration.
        
    	//remove index of form [1], [2], etc. from the end of the name (using tests that are optimized for performance)
    	if (name != null && name.endsWith("]") && INDEX_REGEX_PATTERN.test(name)) {
    		name = name.substring(0, name.lastIndexOf("[") );
        	setName(name);
    	}
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public void setAllowedValues(List<EntityData> allowedValues) {
        this.allowedValues = allowedValues;
    }

    public List<EntityData> getAllowedValues() {
        return allowedValues;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

}
