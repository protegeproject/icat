package edu.stanford.bmir.protege.web.client.rpc.data;

public class ConditionItem extends EntityData {

    public final static String NS_SEP =  "NECESSARY & SUFFICIENT ";
    public final static String N_SEP = "NECESSARY ";
    public final static String INH_SEP = "INHERITED ";

    private String inheritedFromName;
    private String inheritedFromBrowserText;

    //the index in the ConditionsTableModel
    private int index;

    public ConditionItem() { }

    public ConditionItem(String name) {
        super(name);
    }

    public String getInheritedFromName() {
        return inheritedFromName;
    }

    public void setInheritedFromName(String inheritedFromName) {
        this.inheritedFromName = inheritedFromName;
    }

    public String getInheritedFromBrowserText() {
        return inheritedFromBrowserText;
    }

    public void setInheritedFromBrowserText(String inheritedFromBrowserText) {
        this.inheritedFromBrowserText = inheritedFromBrowserText;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSeparator() {
        String name = getName();
        if (name == null) {
            return false;
        }
        return name.equals(NS_SEP) || name.equals(N_SEP) || name.equals(INH_SEP);
    }

    public boolean isNSSeparator() {
        String name = getName();
        if (name == null) {
            return false;
        }
        return name.equals(NS_SEP);
    }

    public boolean isNSeparator() {
        String name = getName();
        if (name == null) {
            return false;
        }
        return name.equals(N_SEP);
    }

    public boolean isINHSeparator() {
        String name = getName();
        if (name == null) {
            return false;
        }
        return name.equals(INH_SEP);
    }

}
