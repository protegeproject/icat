package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.Collection;

public class EntityData implements Serializable {

    private static final long serialVersionUID = 8012327979110652428L;
    private String name;
    private String browserText;
    private int localAnnotationsCount;
    private int childrenAnnotationsCount;
    private Collection<EntityData> types;
    private ValueType valueType;
    private String language = "";

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
        this.name = name;
        this.browserText = browserText;
        this.types = types;
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
    
    public void setLanguage(String lang) {
        this.language = lang;
    }

    public String getLanguage(){
        return language;
    }
}
