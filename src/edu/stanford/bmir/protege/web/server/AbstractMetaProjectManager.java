package edu.stanford.bmir.protege.web.server;

import edu.stanford.smi.protege.server.metaproject.*;

import java.util.ArrayList;
import java.util.Collection;


public abstract class AbstractMetaProjectManager implements MetaProjectManager  {

    public boolean hasValidCredentials(String userName, String password) {
        if (getMetaProject() == null){
            return false;
        }
        User user = getMetaProject().getUser(userName);
        if (user == null) {
            return false;
        }
        return user.verifyPassword(password);
    }

    public void changePassword(String userName, String password) {
        final MetaProject metaProject = getMetaProject();
        if (metaProject == null){
            throw new IllegalStateException("Metaproject is set to null");
        }
        User user = metaProject.getUser(userName);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user name: " + userName);
        }
        user.setPassword(password);
    }

    public String getUserEmail(String userName) {
        final MetaProject metaProject = getMetaProject();
        if (metaProject == null){
           throw new IllegalStateException("Metaproject is set to null");
        }
        User user = metaProject.getUser(userName);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user name: " + userName);
        }
        return user.getEmail();
    }

    public void setUserEmail(String userName, String email) {
        final MetaProject metaProject = getMetaProject();
        if (metaProject == null){
            throw new IllegalStateException("Metaproject is set to null");
        }
        User user = metaProject.getUser(userName);
        if (user == null) {
            throw new IllegalArgumentException("Invalid user name: " + userName);
        }
        user.setEmail(email);
    }

    public Collection<Operation> getAllowedOperations(String projectName, String userName) {
        Collection<Operation> allowedOps = new ArrayList<Operation>();
        final MetaProject metaProject = getMetaProject();
        if (metaProject == null){
            throw new IllegalStateException("Metaproject is set to null");
        }
        Policy policy = metaProject.getPolicy();
        User user = policy.getUserByName(userName);
        ProjectInstance project = getMetaProject().getPolicy().getProjectInstanceByName(projectName);
        if (user == null || project == null) {  return allowedOps;  }
        for (Operation op : policy.getKnownOperations()) {
            if (policy.isOperationAuthorized(user, op, project)) {
                allowedOps.add(op);
            }
        }
        return allowedOps;
    }

    public Collection<Operation> getAllowedServerOperations(String userName) {
        Collection<Operation> allowedOps = new ArrayList<Operation>();
        if (userName == null) {  return allowedOps;  }
        final MetaProject metaProject = getMetaProject();
        if (metaProject == null){
            throw new IllegalStateException("Metaproject is set to null");
        }
        Policy policy = metaProject.getPolicy();
        User user = policy.getUserByName(userName);
        ServerInstance firstServerInstance = metaProject.getPolicy().getFirstServerInstance();
        if (user == null || firstServerInstance == null) {  return allowedOps;  }
        for (Operation op : policy.getKnownOperations()) {
            if (policy.isOperationAuthorized(user, op, firstServerInstance)) {
                allowedOps.add(op);
            }
        }
        return allowedOps;
    }
}
