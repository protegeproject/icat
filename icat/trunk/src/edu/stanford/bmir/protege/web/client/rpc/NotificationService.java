package edu.stanford.bmir.protege.web.client.rpc;

import java.util.Map;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *
 */
@RemoteServiceRelativePath("notification")
public interface NotificationService extends RemoteService {
    void setNotificationDelay(String user, NotificationType notificationType, NotificationInterval notificationInterval);


    Map<NotificationType, NotificationInterval> getNotificationDelays(String user);
}
