package com.victole.examples.core.services;


import com.victole.examples.core.enums.UserType;
import com.victole.examples.core.exceptions.NotificationException;
import org.apache.sling.api.resource.Resource;

/**
 * Notification Service
 */
public interface NotificationService {

    /**
     * Notify users from an update in the pageResource and with the specified user type
     * @param userTypeToNotify page to be notified about
     * @param pageResource page to be notified about
     * @throws NotificationException
     */
    void notifyUsers(UserType userTypeToNotify, Resource pageResource) throws NotificationException;
}
