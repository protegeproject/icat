package edu.stanford.bmir.protege.web.client.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Handles the permissions of a project. It basically keeps a map of the users
 * and their permission on this project.
 *
 * @author Tania Tudorache <tudorache@stanford.edu>
 *
 */
public class ProjectPermissionManager {

    private Project project;
    //list of all operations defined in the metaproject
    private Collection<String> allDefinedOperations = new ArrayList<String>();
    private Map<String, Collection<String>> user2permissionMap = new HashMap<String, Collection<String>>();

    public ProjectPermissionManager(Project project) {
        this.project = project;
    }

    /**
     * An operation is permitted, if the user is part of a group who has that operation permitted as part of a policy,
     * or if the operation is not defined in the metaproject. (Same policy as for the Protege desktop client)
     * @param user
     * @param operation
     * @return
     */
    public boolean hasPermission(String user, String operation) {
        if (user == null) {
            return false; // TODO: might be a too restrictive
        }
        if (allDefinedOperations.contains(operation) == false) { //allow, if not defined in metaproject
            return true;
        }
        Collection<String> permissions = user2permissionMap.get(user);
        if (permissions == null) {
            return false;
        }
        return permissions.contains(operation);
    }

    void setAllDefinedOperations(Collection<String> ops) {
        allDefinedOperations.clear();
        allDefinedOperations.addAll(ops);
    }

    void setUserPermissions(String user, Collection<String> allowedOps) {
        Collection<String> permissions = user2permissionMap.get(user);
        if (permissions == null) {
            permissions = new HashSet<String>();
        }
        permissions.addAll(allowedOps);
        user2permissionMap.put(user, permissions);
    }

    public Project getProject() {
        return project;
    }

    public void dispose() {
        user2permissionMap.clear();
    }

}
