package com.victole.examples.core.services.workflows;


import com.adobe.acs.commons.util.WorkflowHelper;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.victole.examples.core.enums.UserType;
import com.victole.examples.core.models.ArticlePageModel;
import com.victole.examples.core.utils.Constants;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.victole.examples.core.utils.ResourceResolverProvider.getServiceResourceResolver;

/**
 * This workflow process step determines which users group type should be notified.
 * It is defined in the Article properties.
 */

@Component(
        service= WorkflowProcess.class,
        property = {"process.label=Determine Notified User Type Process"}
)
public class DetermineNotifiedUserTypeWorkflowProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(DetermineNotifiedUserTypeWorkflowProcess.class);
    public static final String WF_MODEL_NOTIFY_INTERNAL_USERS_OF_NEW_ARTICLE = "/var/workflow/models/notify-internal-users-of-new-article";
    public static final String WF_MODEL_NOTIFY_EXTERNAL_USERS_OF_NEW_ARTICLE = "/var/workflow/models/notify-external-users-of-new-article";

    @Reference
    private WorkflowHelper workflowHelper;

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    @Reference
    private ModelFactory modelFactory;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        WorkflowData wfData = workItem.getWorkflowData();

        if (!workflowHelper.isPathTypedPayload(wfData)) {
            log.warn("Could not locate a JCR_PATH payload for this workflow. Skipping delegation.");
            return;
        }

        String path = wfData.getPayload().toString();
        try {
            ResourceResolver resourceResolver = getServiceResourceResolver(resourceResolverFactory, Constants.PROJECT_SUBSERVICE_ID);

            Resource pageResource = resourceResolver.resolve(path);
            ArticlePageModel articlePageModel = modelFactory.createModel(pageResource, ArticlePageModel.class);
            UserType userTypeToNotify = articlePageModel.getUserTypeToNotify();
            //if the author does not select a user group the workflow should terminate
            if (UserType.NONE.equals(userTypeToNotify)) {
                workflowSession.terminateWorkflow(workItem.getWorkflow());
                return;
            }
            String userTypeAsWfData = userTypeToWfModel(userTypeToNotify);
            saveApprovalGroupOnWorkflowData(workItem, userTypeAsWfData, resourceResolver);
        } catch (LoginException | PersistenceException e) {
            throw new WorkflowException(e.getMessage(), e);
        }

    }

    private String userTypeToWfModel(UserType userTypeToNotify) {
        switch(userTypeToNotify) {
            case EMPLOYEES:
                return WF_MODEL_NOTIFY_INTERNAL_USERS_OF_NEW_ARTICLE;
            case EXTERNAL:
                return WF_MODEL_NOTIFY_EXTERNAL_USERS_OF_NEW_ARTICLE;
            case ALL:
                return WF_MODEL_NOTIFY_INTERNAL_USERS_OF_NEW_ARTICLE + ","+ WF_MODEL_NOTIFY_EXTERNAL_USERS_OF_NEW_ARTICLE;
            default:
                return "";
        }
    }


    private void saveApprovalGroupOnWorkflowData(WorkItem workItem, String wfModels, ResourceResolver resourceResolver) throws PersistenceException {
        workItem.getWorkflowData().getMetaDataMap().put(WorkflowDelegationProcess.WORKFLOW_MODEL_ID_PROPERTY_NAME, wfModels);
        resourceResolver.commit();
    }
}
