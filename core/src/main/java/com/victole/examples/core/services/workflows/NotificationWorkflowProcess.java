package com.victole.examples.core.services.workflows;

import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.victole.examples.core.enums.UserType;
import com.victole.examples.core.exceptions.NotificationException;
import com.victole.examples.core.services.NotificationService;
import com.victole.examples.core.utils.Constants;
import com.victole.examples.core.utils.WorkflowUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.day.cq.commons.jcr.JcrConstants.JCR_CONTENT;
import static com.victole.examples.core.utils.ResourceResolverProvider.getServiceResourceResolver;

/**
 * This workflow step send the notification when a new article is activated.
 * <p>
 * The user type to be notified is determined by looking up UserType on workflow param map
 * Arguments should be provided as this:
 * <p>
 *     userTypeToBeNotified=external|employees|all
 * <p>
 */
@Component(
        service= WorkflowProcess.class,
        property = {"process.label=New Article Notification Workflow Process Step"}
)
public class NotificationWorkflowProcess implements WorkflowProcess {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationWorkflowProcess.class);

    private static final String PN_NOTIFIED = "notified";
    private static final String USER_TYPE_TO_BE_NOTIFIED = "userTypeToBeNotified";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private NotificationService notificationService;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        //key and multiple values map
        Map<String, String[]> args = WorkflowUtils.getProcessArgsMap(metaDataMap);

        String[] userTypeTobeNotified = args.get(USER_TYPE_TO_BE_NOTIFIED);

        if (userTypeTobeNotified == null || userTypeTobeNotified.length == 0 || StringUtils.isBlank(userTypeTobeNotified[0])) {
            throw new WorkflowException("PROCESS_ARG [ " + USER_TYPE_TO_BE_NOTIFIED + " ] not defined");
        }

        final WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();
        if (!StringUtils.equals(type, "JCR_PATH")) {
            LOG.warn("Could not locate a JCR_PATH payload for this workflow.");
            return;
        }
        String path = workflowData.getPayload().toString();

        try {
            ResourceResolver resourceResolver = getServiceResourceResolver(resourceResolverFactory, Constants.PROJECT_SUBSERVICE_ID);
            Resource pageResource = resourceResolver.getResource(path);
            if (pageResource == null) {
                LOG.warn("Could not locate a resource {}", path);
                return;
            }
            UserType userType = UserType.valueFromUserType(userTypeTobeNotified[0]);
            if (!UserType.NONE.equals(userType)) {
                notificationService.notifyUsers(userType, pageResource);
                ModifiableValueMap map = resourceResolver.resolve(path + "/" + JCR_CONTENT).adaptTo(ModifiableValueMap.class);
                if (map != null) {
                    map.put(PN_NOTIFIED, Boolean.TRUE);
                }
                if (resourceResolver.hasChanges()) {
                    resourceResolver.commit();
                }
            }
        } catch (LoginException | PersistenceException e) {
            LOG.error("Error while executing notification workflow: {}", e.getMessage(), e);
            throw new WorkflowException(e.getMessage(), e);
        } catch (NotificationException e) {
            LOG.error("Error while notifying users for path {}", path, e);
            throw new WorkflowException(e.getMessage(), e);
        }
    }
}
