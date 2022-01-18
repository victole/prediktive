package com.victole.examples.core.services;

import com.adobe.granite.workflow.PayloadMap;
import com.day.cq.replication.ReplicationAction;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.workflow.WorkflowException;
import com.day.cq.workflow.WorkflowService;
import com.day.cq.workflow.WorkflowSession;
import com.day.cq.workflow.exec.WorkflowData;
import com.day.cq.workflow.model.WorkflowModel;
import com.victole.examples.core.utils.Constants;
import com.victole.examples.core.utils.ResourceResolverProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.settings.SlingSettingsService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import java.util.Arrays;

import static com.day.cq.commons.Externalizer.AUTHOR;
import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.victole.examples.core.models.impl.ArticlePageModelImpl.RESOURCE_TYPE;

@Component(service = EventHandler.class, immediate = true,
        property = { EventConstants.EVENT_TOPIC + "=" + ReplicationAction.EVENT_TOPIC })
@Designate(ocd = ActivationEventHandler.Config.class)
public class ActivationEventHandler implements EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ActivationEventHandler.class);
    private static final String DEFAULT_NOTIFICATION_WORKFLOW_PATH = "/var/workflow/models/new-article";

    @Reference
    private SlingSettingsService slingSettingsService;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private WorkflowService workflowService;

    @Activate
    private Config configuration;

    @ObjectClassDefinition(name = "Activation Event Handler Configuration", description = "Activation Event Handler Configuration")
    protected @interface Config {
        @AttributeDefinition(name = "List of Resource Type To Monitor", description = "List of Resource Types To Monitor")
        String[] resourceTypes() default { RESOURCE_TYPE };

        @AttributeDefinition(name = "New Article Notification Workflow Path", description = "New Article Notification Workflow Path")
        String workflowPath() default DEFAULT_NOTIFICATION_WORKFLOW_PATH;

        @AttributeDefinition(name = "Paths to monitor for events", description = "Events will be captured just for these configured paths.")
        String[] monitoringPaths() default {};
    }

    @Override
    public void handleEvent(Event event) {
        LOG.debug("Activation Event handler called");
        if (slingSettingsService.getRunModes().contains(AUTHOR)) {
            final ReplicationAction action = ReplicationAction.fromEvent(event);
            if (action != null && ReplicationActionType.ACTIVATE == action.getType() && isMonitoredPath(action.getPath())) {
                try (ResourceResolver serviceResourceResolver = ResourceResolverProvider.getServiceResourceResolver(resourceResolverFactory,
                        Constants.PROJECT_SUBSERVICE_ID)) {
                    final Resource resource = serviceResourceResolver.resolve(action.getPath() + "/" +JCR_CONTENT);
                    boolean isCurrentResourceTypeMonitored = Arrays.asList(configuration.resourceTypes()).contains(resource.getResourceType());
                    ValueMap valueMap = resource.getValueMap();
                    boolean notificationsEnabled = valueMap.get("notifications", StringUtils.EMPTY).equalsIgnoreCase("enabled");
                    boolean notified = valueMap.get("notified", Boolean.FALSE);
                    if (isCurrentResourceTypeMonitored && notificationsEnabled && !notified) {
                        performWorkflow(configuration.workflowPath(), action.getPath(), serviceResourceResolver);
                    }
                } catch (LoginException e) {
                    LOG.info("Errors while handling activation event: {}", e.getMessage(), e);
                }
            }
        }
    }


    private void performWorkflow(final String model, final String path, ResourceResolver resourceResolver) {
        try {
            Session session = resourceResolver.adaptTo(Session.class);
            WorkflowSession wfSession = workflowService.getWorkflowSession(session);

            WorkflowModel wfModel = wfSession.getModel(model);
            if (wfModel != null) {
                WorkflowData wfData = wfSession.newWorkflowData(PayloadMap.TYPE_JCR_PATH, path);
                wfSession.startWorkflow(wfModel, wfData);
            } else {
                LOG.warn("New Article Notification Workflow model found. Not starting workflow.");
            }
        } catch (WorkflowException e) {
            LOG.info("Errors while starting workflow: {}", e.getMessage(), e);
        }
    }

    private boolean isMonitoredPath(String path) {
        return Arrays.stream(configuration.monitoringPaths()).anyMatch(path::startsWith);
    }
}
