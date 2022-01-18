package com.victole.examples.core.utils;

import com.adobe.acs.commons.util.ParameterUtil;
import com.adobe.acs.commons.util.WorkflowHelper;
import com.adobe.granite.workflow.WorkflowException;
import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.adobe.granite.workflow.model.WorkflowModel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public final class WorkflowUtils {
    private static final Logger log = LoggerFactory.getLogger(WorkflowUtils.class);

    private WorkflowUtils() {
    }

    /**
     * Returns the PROCESS_ARGS value from the workflow data
     * @param metaDataMap
     * @return map with the values
     */
    public static Map<String, String[]> getProcessArgsMap(MetaDataMap metaDataMap) {
        final String processArgs = metaDataMap.get(WorkflowHelper.PROCESS_ARGS, "");
        return ParameterUtil.toMap(StringUtils.split(processArgs, System.getProperty("line.separator")), "=",",");
    }

    /**
     * Returns the WorkflowModel object based on the /var workflow model path
     * @param workflowSession
     * @param workflowModelId
     * @return workflow model
     */
    public static WorkflowModel getWorkflowModel(WorkflowSession workflowSession, String workflowModelId) {
        workflowModelId = StringUtils.stripToEmpty(workflowModelId);
        WorkflowModel workflowModel = null;

        if (StringUtils.isNotBlank(workflowModelId)) {
            try {
                workflowModel = workflowSession.getModel(workflowModelId);
            } catch (WorkflowException e) {
                log.warn("Could not find Workflow Model for [ {} ]", workflowModelId, e);
            }
        }

        return workflowModel;
    }
}
