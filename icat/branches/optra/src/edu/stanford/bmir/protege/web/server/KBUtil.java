package edu.stanford.bmir.protege.web.server;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.stanford.bmir.protege.web.client.rpc.data.UserData;
import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;
import edu.stanford.smi.protege.util.Log;

public class KBUtil {

    public static void morphUser(KnowledgeBase kb, String user) {
        if (kb.getProject().isMultiUserClient()) {
            Session s = (Session) RemoteClientFrameStore.getCurrentSession(kb);
            s.setDelegate(user);
        } else {
            ((DefaultKnowledgeBase) kb).setUserName(user);
        }
    }

    public static void restoreUser(KnowledgeBase kb) {
        String defaultUser = ApplicationProperties.getProtegeServerUser();
        if (kb.getProject().isMultiUserClient()) {
            Session s = (Session) RemoteClientFrameStore.getCurrentSession(kb);
            s.setDelegate(null);
        } else {
            ((DefaultKnowledgeBase) kb).setUserName(defaultUser);
        }
    }

    public static boolean shouldRunInTransaction(String operationDescription) {
        return operationDescription != null && operationDescription.length() > 0;
    }

    @SuppressWarnings("unchecked")
    public static <X> Collection<X> getCollection(KnowledgeBase kb, Collection<String> names, Class<? extends X> javaInterface) {
        Collection<X> entities = new HashSet<X>();
        if (names == null) {
            return entities;
        }
        for (String name : names) {
            Frame frame = kb.getFrame(name);
            if (frame != null && javaInterface.isAssignableFrom(frame.getClass())) {
                entities.add((X)frame);
            }
        }
        return entities;
    }

    //TODO: not the best util class for this method.. find a better one
    public static String getUserInSession(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        final UserData userData = (UserData) session.getAttribute(SessionConstants.USER_DATA_PARAMETER);
        if (userData == null) {
            return null;
        }
        return userData.getName();
    }

    public static String getRemoteProjectName(Project prj) {
        URI uri = prj.getProjectURI();
        if (uri == null) { return null; }
        try {
            String path = uri.getPath();
            int index = path.lastIndexOf("/");
            if (index > -1) {
                return path.substring(index + 1);
            }
        } catch (Exception e) {
            Log.emptyCatchBlock(e);
            //do nothing
        }
        return null;
    }

}
