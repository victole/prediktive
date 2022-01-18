package com.victole.examples.core.models.db;

public interface Notification {

    static final String PK_KEY = "UserId";

    static final String STATUS_KEY = "Status";

    static final String NOTIFICATION_PROJECT_ID = "NotificationProjectId";

    String getUserId();

    void setUserId(String userId);

    String getTimestamp();

    void setTimestamp(String timestamp);

    String getBody();

    void setBody(String body);

    String getSender();

    void setSender(String sender);

    String getStatus();

    void setStatus(String status);

    String getSubject();

    void setSubject(String subject);

    String getNotificationProjectId();

    void setNotificationProjectId(String subject);
}
