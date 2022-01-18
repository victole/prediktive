package com.victole.examples.core.services.impl;

import com.adobe.cq.dam.cfm.ContentFragment;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.victole.examples.core.dto.UserListingResults;
import com.victole.examples.core.enums.UserType;
import com.victole.examples.core.exceptions.NotificationException;
import com.victole.examples.core.models.ArticlePageModel;
import com.victole.examples.core.models.config.SiteConfigurationModel;
import com.victole.examples.core.models.db.Notification;
import com.victole.examples.core.models.db.impl.NotificationImpl;
import com.victole.examples.core.services.CAConfigurationService;
import com.victole.examples.core.services.CustomSlingMappingService;
import com.victole.examples.core.services.NotificationService;
import com.victole.examples.core.services.NotificationStorageService;
import com.victole.examples.core.utils.Constants;
import com.victole.examples.core.utils.LinkUtils;
import com.victole.examples.core.utils.ResourceResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.caconfig.resource.ConfigurationResourceResolver;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;

@Component(service = NotificationService.class, immediate = true)
@Designate(ocd = NotificationServiceImpl.Config.class)
public class NotificationServiceImpl implements NotificationService {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationServiceImpl.class);
    public static final String EXCEPTION_LOADING_FRAGMENT = "Error on loading notification fragment: can't load resolve fragment resource.";
    private static final String PAGE_TITLE_AND_LINK_PLACEHOLDER = "${PageTitleAndLink}";
    private static final String PAGE_TITLE_PLACEHOLDER = "${PageTitle}";

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private NotificationStorageService notificationStorageService;

    @Reference
    private ConfigurationResourceResolver configurationResourceResolver;

    @Reference
    private CAConfigurationService configurationService;

    @Reference
    private CustomSlingMappingService customSlingMappingService;

    private HttpRequestFactory requestFactory;

    private Config configuration;

    public NotificationServiceImpl() {
        CloseableHttpClient client = HttpClients.custom().build();
        this.requestFactory = new ApacheHttpTransport(client)
                .createRequestFactory();
    }

    public NotificationServiceImpl(HttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
    }

    @Activate
    private void activate(Config configuration) {
        this.configuration = configuration;
    }

    @ObjectClassDefinition(name = "Notification Service configuration",
            description = "Notification Service configuration")
    protected @interface Config {

        @AttributeDefinition(name = "User Listing Endpoint Path",
                description = "Endpoint path for User Listing",
                type = AttributeType.STRING)
        String serverUrl() default StringUtils.EMPTY;

        @AttributeDefinition(name = "User",
                description = "User for Basic Auth",
                type = AttributeType.STRING)
        String user() default StringUtils.EMPTY;

        @AttributeDefinition(name = "User Password",
                description = "User Password for Basic Auth",
                type = AttributeType.STRING)
        String password() default StringUtils.EMPTY;

    }


    @Override
    public void notifyUsers(UserType userTypeToNotify, Resource pageResource) throws NotificationException {
        try (ResourceResolver resourceResolver = ResourceResolverProvider.getServiceResourceResolver(resourceResolverFactory, Constants.PROJECT_SUBSERVICE_ID)) {
            Resource contentResource = pageResource.getChild(JCR_CONTENT);
            ArticlePageModel articlePageModel = modelFactory.createModel(pageResource, ArticlePageModel.class);
            List<String> users = this.loadUsersToNotify(userTypeToNotify);
            if (!users.isEmpty()) {
                SiteConfigurationModel siteConfiguration = configurationService.getSiteConfiguration(contentResource);
                String notificationPath = siteConfiguration.articleNotificationContentFragment();
                if (!StringUtils.isEmpty(siteConfiguration.notificationProjectId())) {
                    Notification notification = createNotificationFromFragment(notificationPath, resourceResolver, articlePageModel, siteConfiguration.notificationProjectId());
                    LOG.debug("Connecting to notification storage to generate notifications for users: {}", users);
                    notificationStorageService.createNotification(users, notification);
                    LOG.debug("Notifications created...");
                } else {
                    LOG.error("No Notification Project ID configured for notifications. Skipping.");
                    throw new NotificationException("No Notification Project ID configured for notifications.");
                }
            }
            else {
                LOG.warn("No users found for creating notifications. Skipping.");
            }

        } catch (LoginException e) {
            LOG.error("Error on retrieving resolver for notification service.", e);
            throw new NotificationException("Error on retrieving resolver for notification service.");
        }
    }

    private List<String> loadUsersToNotify(UserType userType) throws NotificationException {
        GenericUrl endpoint = new GenericUrl(configuration.serverUrl());
        endpoint.put("userType", userType.getType());
        try {
            HttpRequest request = requestFactory.buildGetRequest(endpoint);
            BasicAuthentication basicAuthentication = new BasicAuthentication(configuration.user(), configuration.password());
            request.setInterceptor(basicAuthentication);
            HttpResponse response = request.execute();
            ObjectMapper mapper = new ObjectMapper();
            UserListingResults userListingResults = mapper.readValue(response.getContent(), UserListingResults.class);
            return userListingResults.getUsers();
        } catch (IOException e) {
            LOG.error("Error on reaching user listing servlet on publish: {}", e.getMessage(), e);
            throw new NotificationException("Error on reaching user listing servlet on publish.");
        }
    }

    private Notification createNotificationFromFragment(String notificationPath, ResourceResolver resourceResolver, ArticlePageModel articlePageModel, String notificationProjectId) throws NotificationException {
        Resource notificationFragmentResource = resourceResolver.getResource(notificationPath);
        if (notificationFragmentResource != null) {
            ContentFragment notificationFragment = notificationFragmentResource.adaptTo(ContentFragment.class);
            if (notificationFragment != null) {
                String body = notificationFragment.getElement("body").getContent();
                body = replaceVariables(body, articlePageModel);
                String sender = notificationFragment.getElement("sender").getContent();
                String subject = notificationFragment.getElement("subject").getContent();
                subject = replaceVariables(subject, articlePageModel);
                Notification notification = new NotificationImpl(body, sender, subject, notificationProjectId);
                return notification;
            } else {
                LOG.error("Error on loading notification fragment: {}", notificationPath);
                throw new NotificationException(EXCEPTION_LOADING_FRAGMENT);
            }
        } else {
            LOG.error(EXCEPTION_LOADING_FRAGMENT);
            throw new NotificationException(EXCEPTION_LOADING_FRAGMENT);
        }
    }

    private String replaceVariables(String rawContent, ArticlePageModel articlePageModel) {
        String finalContent = rawContent;
        if (rawContent.contains(PAGE_TITLE_AND_LINK_PLACEHOLDER)) {
            String publishUrl = customSlingMappingService.map(LinkUtils.sanitizeLink(articlePageModel.getPath()));
            finalContent = rawContent.replace(PAGE_TITLE_AND_LINK_PLACEHOLDER, String.format("<a href=\"%s\">%s</a>", publishUrl, articlePageModel.getTitle()));
        }
        if (rawContent.contains(PAGE_TITLE_PLACEHOLDER)) {
            finalContent = rawContent.replace(PAGE_TITLE_PLACEHOLDER, articlePageModel.getTitle());
        }
        return finalContent;
    }

    public void setConfiguration(Config configuration) {
        this.configuration = configuration;
    }
}
