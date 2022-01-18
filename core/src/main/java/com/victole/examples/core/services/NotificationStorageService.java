package com.victole.examples.core.services;

import com.victole.examples.core.exceptions.NotificationException;
import com.victole.examples.core.models.db.Notification;

import java.util.List;

/**
 * JSon Web Token service utility.
 */
public interface NotificationStorageService {

    String STATUS_UNREAD = "UNREAD";

    /**
     * Creates notification in batch for a list of users, with selected sender, subject and body
     * @param users list of users to store notification
     * @param notification notification to store {@link Notification}
     */
    void createNotification(List<String> users, Notification notification) throws NotificationException;

    /**
     * Get all notifications by userId
     * @param userId
     * @return list of @{@link Notification}
     */
    List<Notification> getNotificationsByUserId(String userId, String notificationProjectId);

    /**
     * Retrieve count for UNREAD notifications for an specific userId
     * @param userId
     * @param notificationProjectId
     * @return number of UNREAD notifications
     */
    Integer getNotificationsCountByUser(String userId, String notificationProjectId);

    /**
     * Load Notification object from userId and timestamp
     * @param userId
     * @param timestamp
     * @return a Notification
     */
    Notification loadNotification(String userId, String timestamp);

    /**
     * Update list of notifications.
     * @param notifications
     */
    void updateNotifications(List notifications) throws NotificationException;

    /**
     * Delete list of notifications.
     * @param notifications
     */
    void deleteNotifications(List notifications) throws NotificationException;
}
