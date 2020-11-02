package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntityData implements Serializable {

    private static final long serialVersionUID = 8012327979110652428L;
    private String name;
    private String browserText;
    
    private int localAnnotationsCount;
    private int childrenAnnotationsCount;
    
    private Collection<EntityData> types;
    private ValueType valueType;
    
    private Watch watch;
    
    private Map<String, String> properties;
    
    private boolean isSystem = false;
    private boolean isReleased = false;

    public EntityData() {
        this(null, null);
    }

    public EntityData(String name) {
        this(name, name);
    }

    public EntityData(String name, String browserText) {
        this(name, browserText, null);
    }

    public EntityData(String name, String browserText, Collection<EntityData> types) {
    	//TODO: this needs to go!!
    	
    	//remove index of form [1], [2], etc. from the end of the name,.
    	if (name != null && name.matches("(.*)\\[\\d+\\]$")) {
    		name = name.substring(0, name.lastIndexOf("[") );
    		
    	}

        this.name = name;
        this.browserText = browserText;
        this.types = types;
    }

    public void copyValuesFrom(EntityData sourceEntityData) {
        setName(sourceEntityData.getName());
        setBrowserText(sourceEntityData.getBrowserText());
        setLocalAnnotationsCount(sourceEntityData.getLocalAnnotationsCount());
        setChildrenAnnotationsCount(sourceEntityData.getChildrenAnnotationsCount());
        setValueType(sourceEntityData.getValueType());
        setWatch(sourceEntityData.getWatch());
        Collection<EntityData> sourceTypes = sourceEntityData.getTypes();
        if (sourceTypes != null) {
            setTypes(new ArrayList<EntityData>(sourceTypes));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrowserText() {
        return browserText;
    }

    public void setBrowserText(String browserText) {
        this.browserText = browserText;
    }

    public Collection<EntityData> getTypes() {
        return types;
    }

    public void setTypes(Collection<EntityData> types) {
        this.types = types;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EntityData)) {
            return false;
        }
        return ((EntityData) obj).getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        if (name != null) {
            return name.length() * 11 + 42 + name.hashCode();
        }
        return 42;
    }

    @Override
    public String toString() {
        /*
        StringBuffer buffer = new StringBuffer();
        buffer.append(name);
        buffer.append(", browser text: ");
        buffer.append(browserText);
        return buffer.toString();
        */
        return browserText;
    }

    public int getLocalAnnotationsCount() {
        return localAnnotationsCount;
    }

    public void setLocalAnnotationsCount(int localAnnotationsCount) {
        this.localAnnotationsCount = localAnnotationsCount;
    }

    public int getChildrenAnnotationsCount() {
        return childrenAnnotationsCount;
    }

    public void setChildrenAnnotationsCount(int childrenAnnotationsCount) {
        this.childrenAnnotationsCount = childrenAnnotationsCount;
    }

    public Watch getWatch() {
        return watch;
    }

    public void setWatch(Watch watch) {
        this.watch = watch;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setIsSystem(boolean isSystem) {
        this.isSystem = isSystem;
    }
    
    public boolean isReleased() {
    	return isReleased;
    }
    
    public void setReleased(boolean released) {
    	this.isReleased = released;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getProperty(String prop) {
        if (properties == null) {
            return null;
        }
        return properties.get(prop);
    }

    public void setProperty(String prop, String value) {
        if (properties == null) {
            properties = new LinkedHashMap<String, String>();
        }
        properties.put(prop, value);
    }
}
