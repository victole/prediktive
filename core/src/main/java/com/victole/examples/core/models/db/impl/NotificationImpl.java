package com.victole.examples.core.models.db.impl;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.victole.examples.core.models.db.Notification;

import java.util.Objects;

@DynamoDBTable(tableName="ProjectNotification")
public class NotificationImpl implements Notification {

    private String userId;
    private String timestamp;
    private String body;
    private String sender;
    private String status;
    private String subject;
    private String notificationProjectId;


    public NotificationImpl() {
        super();
    }

    public NotificationImpl(String body, String sender, String subject, String notificationProjectId) {
        this.body = body;
        this.sender = sender;
        this.subject = subject;
        this.notificationProjectId = notificationProjectId;
    }

    @Override
    @DynamoDBHashKey(attributeName= Notification.PK_KEY)
    public String getUserId() {
        return this.userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    @DynamoDBRangeKey(attributeName="Timestamp")
    public String getTimestamp() {
        return this.timestamp;
    }

    @Override
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    @DynamoDBAttribute(attributeName="Body")
    public String getBody() {
        return this.body;
    }

    @Override
    public void setBody(String body) {
        this.body = body;
    }

    @Override
    @DynamoDBAttribute(attributeName="Sender")
    public String getSender() {
        return this.sender;
    }

    @Override
    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    @DynamoDBAttribute(attributeName=Notification.STATUS_KEY)
    public String getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    @DynamoDBAttribute(attributeName="Subject")
    public String getSubject() {
        return this.subject;
    }

    @Override
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return String.format("Notification: { userId= %s, timestamp=%s} ", this.userId, this.timestamp);
    }

    @Override
    @DynamoDBAttribute(attributeName="NotificationProjectId")
    public String getNotificationProjectId() {
        return notificationProjectId;
    }

    public void setNotificationProjectId(String notificationProjectId) {
        this.notificationProjectId = notificationProjectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass())  {
            return false;
        }
        NotificationImpl that = (NotificationImpl) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, timestamp);
    }
}
