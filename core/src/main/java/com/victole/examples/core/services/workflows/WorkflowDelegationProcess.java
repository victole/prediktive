package com.victole.examples.core.services.workflows;

import com.adobe.acs.commons.util.WorkflowHelper;
import com.adobe.granite.workflow.PayloadMap;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;
import com.victole.examples.core.utils.WorkflowUtils;
import org.apache.commons.lang.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This workflow step invokes another workflow/s on the current workflow's payload.
 * <p>
 * The delegate workflow is determined by looking up Workflow Id on workflow param map
 * Arguments should be provided as this:
 * <p>
 *     workflowModelIds=model1, model2, ...
 *     terminateWorkflowOnDelegation=true|false
 * <p>
 */

@Component(
        service= WorkflowProcess.class,
        property = {"process.label=Workflow Delegation"}
)
public class WorkflowDelegationProcess implements WorkflowProcess {

    private static final Logger log = LoggerFactory.getLogger(WorkflowDelegationProcess.class);

    // comma separated string with the workflow model to delegate
    public static final String WORKFLOW_MODEL_ID_PROPERTY_NAME = "workflowModelIds";
    // When set to true, this Workflow will terminate after successful delegation. This is useful if there is a use-case when this step has WF steps behind it.
    private static final String TERMINATE_ON_DELEGATION = "terminateWorkflowOnDelegation";

    @Reference
    private WorkflowHelper workflowHelper;

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap metaDataMap) throws WorkflowException {
        //key and multiple values map
        Map<String, String[]> args = WorkflowUtils.getProcessArgsMap(metaDataMap);

        String[] workflowModelsIds = args.get(WORKFLOW_MODEL_ID_PROPERTY_NAME);
        //in case the models are passed by workflow data in the steps flow
        if (null == workflowModelsIds) {
            String workflowModels = workItem.getWorkflowData().getMetaDataMap().get(WORKFLOW_MODEL_ID_PROPERTY_NAME, String.class);
            workflowModelsIds = StringUtils.isNotBlank(workflowModels) ? workflowModels.split(",") : null;
        }

        if (workflowModelsIds == null || workflowModelsIds.length == 0) {
            throw new WorkflowException("PROCESS_ARG [ " + WORKFLOW_MODEL_ID_PROPERTY_NAME + " ] not defined");
        }

        WorkflowData wfData = workItem.getWorkflowData();

        if (!workflowHelper.isPathTypedPayload(wfData)) {
            log.warn("Could not locate a JCR_PATH payload for this workflow. Skipping delegation.");
            return;
        }

        boolean terminateOnDelegation = args.containsKey(TERMINATE_ON_DELEGATION) && Boolean.parseBoolean(StringUtils.lowerCase(args.get(TERMINATE_ON_DELEGATION)[0]));
        final List<WorkflowModel> delegateWorkflowModels = getDelegateWorkflowModels(workflowSession, workflowModelsIds);


        WorkflowData newProcessWfData = workflowSession.newWorkflowData(PayloadMap.TYPE_JCR_PATH, wfData.getPayload());

        if (!delegateWorkflowModels.isEmpty()) {
            for (WorkflowModel delegateWfModel : delegateWorkflowModels) {
                workflowSession.startWorkflow(delegateWfModel, newProcessWfData);
                log.info("Delegating payload [ {} ] to Workflow Model [ {} ]", wfData.getPayload(), delegateWfModel.getId());
                log.info("Terminating current workflow due to PROCESS_ARGS[ {} ] = [ {} ]", TERMINATE_ON_DELEGATION, terminateOnDelegation);
                if (terminateOnDelegation) {
                    workflowSession.terminateWorkflow(workItem.getWorkflow());
                }
            }
        } else {
            log.warn("No valid delegate Workflow Model could be located. Skipping workflow delegation.");
        }
    }

    /**
     * Creates the WorkflowModel list from the IDs array
     * @param workflowSession workflow session
     * @param workflowModelIds list of workflows path from /var
     * @return Workflow models id list
     * @throws WorkflowException in case invalid workflow path
     */
    private List<WorkflowModel> getDelegateWorkflowModels(WorkflowSession workflowSession,
                                                          String[] workflowModelIds) throws WorkflowException {
        List<WorkflowModel> workflowModels = new ArrayList<>();
        for (String workflowModelId : workflowModelIds) {
            if (StringUtils.isBlank(workflowModelId)) {
                throw new WorkflowException("PROCESS_ARG [ " + WORKFLOW_MODEL_ID_PROPERTY_NAME + " ] not defined");
            }

            WorkflowModel workflowModel = WorkflowUtils.getWorkflowModel(workflowSession, workflowModelId);

            if (workflowModel != null) {
                log.debug("Using configured delegate Workflow Model [ {} ]", workflowModel.getId());
            } else {
                throw new WorkflowException(String.format("Could not find configured Workflow Model at [ %s ]", workflowModelId));
            }

            workflowModels.add(workflowModel);
        }

        return workflowModels;
    }


}
