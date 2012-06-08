package edu.stanford.bmir.protege.web.client.rpc.data;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author Jennifer Vendetti <vendetti@stanford.edu>
 */

public class UserData implements Serializable, Comparator<UserData> {
    private String name;
    private String password;

    public UserData() {
    }

    public UserData(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int compare(UserData o1, UserData o2) {
        try {
            if (o1.getName() == null || o2.getName() == null) {
                return 0;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public boolean equals(Object obj) {
        UserData userDataToCompare = (UserData) obj;
        if (this.getName().equals(userDataToCompare.getName())) {
            return true;
        }
        return false;
    }
}
