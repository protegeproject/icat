package edu.stanford.bmir.protege.web.server;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import edu.stanford.bmir.protege.web.client.rpc.NotificationInterval;
import edu.stanford.bmir.protege.web.client.rpc.NotificationService;
import edu.stanford.bmir.protege.web.client.rpc.NotificationTimestamp;
import edu.stanford.bmir.protege.web.client.rpc.NotificationType;
import edu.stanford.bmir.protege.web.client.rpc.data.ChangeData;
import edu.stanford.bmir.protege.web.client.rpc.data.EntityData;
import edu.stanford.bmir.protege.web.client.rpc.data.ValueType;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.server.metaproject.PropertyValue;
import edu.stanford.smi.protege.server.metaproject.User;
import edu.stanford.smi.protege.util.Log;

/**
 * Server-side services for notification management.
 */
public class NotificationServiceImpl extends RemoteServiceServlet
        implements NotificationService {

    public void setNotificationDelay(final String user, final NotificationType notificationType, final NotificationInterval notificationInterval) {
        final User userProperties = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(user);
        String currentUserPropertyValue = userProperties.getPropertyValue(notificationType.getValue());
        while (currentUserPropertyValue != null) {
            userProperties.removePropertyValue(notificationType.getValue(), currentUserPropertyValue);
            currentUserPropertyValue = userProperties.getPropertyValue(notificationType.getValue());
        }
        final NotificationTimestamp notificationTimestamp = NotificationTimestamp.fromType(notificationType);
        String currentUserTimestamp = userProperties.getPropertyValue(notificationTimestamp.getValue());
        if (currentUserTimestamp == null) {
            userProperties.addPropertyValue(notificationTimestamp.getValue(), new Long(new Date().getTime()).toString());
        }
        userProperties.addPropertyValue(notificationType.getValue(), notificationInterval.getValue());
    }

    public Map<NotificationType, NotificationInterval> getNotificationDelays(final String user) {
        final Collection<PropertyValue> valueCollection = Protege3ProjectManager.getProjectManager().getMetaProjectManager().getMetaProject().getUser(user).getPropertyValues();
        final Map<NotificationType, NotificationInterval> returnValue = new HashMap<NotificationType, NotificationInterval>();
        for (PropertyValue propertyValue : valueCollection) {
            final NotificationType type = NotificationType.fromString(propertyValue.getPropertyName());
            if (type != null) {
                returnValue.put(type, NotificationInterval.fromString(propertyValue.getPropertyValue()));
            }
        }
        return returnValue;
    }

    private List<ChangeDataWithProject> schedule(final User user, final Date now, final NotificationType notificationType) {
        final Collection<ServerProject<Project>> serverProjects = Protege3ProjectManager.getProjectManager().getOpenServerProjects();
        final NotificationInterval interval = NotificationInterval.fromString(user.getPropertyValue(notificationType.getValue()));
        if (interval == null) {
            return new ArrayList<ChangeDataWithProject>();
        }
        final String notificationTimestampPropertyName = NotificationTimestamp.fromType(notificationType).getValue();
        if (user.getPropertyValue(notificationTimestampPropertyName) == null) {
            return new ArrayList<ChangeDataWithProject>();
        }
        final Date lastRunDate = new Date(new Long((user.getPropertyValue(notificationTimestampPropertyName))));
        final long intervalSinceLastRun = now.getTime() - lastRunDate.getTime();
        final long intervalInMilliseconds = interval.getIntervalInMilliseconds();
        final List<ChangeDataWithProject> changes = new ArrayList<ChangeDataWithProject>();
        if (intervalSinceLastRun > intervalInMilliseconds) {
            ChAOServiceImpl impl = new ChAOServiceImpl();
            for (ServerProject serverProject : serverProjects) {
                final Collection<ChangeData> allChanges = impl.getWatchedEntities(serverProject.getProjectName(), user.getName());
                for (ChangeData change : allChanges) {
                    if (change.getTimestamp().getTime() > lastRunDate.getTime()) {
                        changes.add(new ChangeDataWithProject(change, serverProject));
                    }
                }
            }
        }
        user.removePropertyValue(notificationTimestampPropertyName, user.getPropertyValue(notificationTimestampPropertyName));
        user.addPropertyValue(notificationTimestampPropertyName, new Long(now.getTime()).toString());
        return changes;
    }

    private void sendNotification(final User user, final Collection<ChangeDataWithProject> changes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (ChangeDataWithProject change : changes) {

            MessageFormat format = new MessageFormat("{2} changed {0} at {1,time} on {1,date} hosturl");
            MessageFormat linkFormat = new MessageFormat("link is http://{0}&ontology={1}&tab={2}&id={3}");
            stringBuffer.append(format.format(change.getEntityData().getBrowserText(), change.getTimestamp(), change.getAuthor()));

            try {
                HttpServletRequest request = this.getThreadLocalRequest();
                stringBuffer.append(linkFormat.format(
                        request.getRemoteHost(),
                        URLEncoder.encode(change.getProject().getProjectName(), "UTF-8"),
                        change.getEntityData().getValueType() == ValueType.Instance ? URLEncoder.encode("IndividualsTab", "UTF-8") : URLEncoder.encode("ClassesTab", "UTF-8"),
                        URLEncoder.encode(change.getEntityData().getBrowserText(), "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                Log.getLogger().log(Level.SEVERE, "Error formatting to URLEncoding projectName = " + change.getProject().getProject() + ", tab = " + change.getEntityData().getValueType() + ", id = " + change.getEntityData().getBrowserText(), e);
            }
        }
        EmailUtil.sendEmail(user.getEmail(), "changes", stringBuffer.toString(), ApplicationProperties.getEmailAccount());
    }

    class ChangeDataWithProject extends ChangeData {
        private final ServerProject project;

        public ChangeDataWithProject(EntityData entityData, String author, String description, Date timestamp, ServerProject project) {
            super(entityData, author, description, timestamp);
            this.project = project;
        }

        public ChangeDataWithProject(ChangeData changeData, ServerProject project) {
            this(changeData.getEntityData(), changeData.getDescription(), changeData.getDescription(), changeData.getTimestamp(), project);
        }

        public ServerProject getProject() {
            return project;
        }
    }

}