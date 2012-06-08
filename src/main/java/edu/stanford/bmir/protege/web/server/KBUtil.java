package edu.stanford.bmir.protege.web.server;

import java.util.Collection;
import java.util.HashSet;

import edu.stanford.smi.protege.model.DefaultKnowledgeBase;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.server.Session;
import edu.stanford.smi.protege.server.framestore.RemoteClientFrameStore;

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

}
