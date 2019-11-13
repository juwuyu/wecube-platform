package com.webank.wecube.platform.core.service.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.webank.wecube.platform.core.commons.WecubeCoreException;
import com.webank.wecube.platform.core.domain.SystemVariable;
import com.webank.wecube.platform.core.domain.plugin.PluginConfig;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterface;
import com.webank.wecube.platform.core.domain.plugin.PluginConfigInterfaceParameter;
import com.webank.wecube.platform.core.domain.plugin.PluginInstance;
import com.webank.wecube.platform.core.entity.workflow.ProcExecBindingEntity;
import com.webank.wecube.platform.core.entity.workflow.ProcInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeDefInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecParamEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeExecRequestEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeInstInfoEntity;
import com.webank.wecube.platform.core.entity.workflow.TaskNodeParamEntity;
import com.webank.wecube.platform.core.jpa.workflow.ProcExecBindingRepository;
import com.webank.wecube.platform.core.jpa.workflow.ProcInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeDefInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecParamRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeExecRequestRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeInstInfoRepository;
import com.webank.wecube.platform.core.jpa.workflow.TaskNodeParamRepository;
import com.webank.wecube.platform.core.model.workflow.InputParamAttr;
import com.webank.wecube.platform.core.model.workflow.InputParamObject;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationCommand;
import com.webank.wecube.platform.core.model.workflow.PluginInvocationResult;
import com.webank.wecube.platform.core.service.PluginInstanceService;
import com.webank.wecube.platform.core.service.SystemVariableService;
import com.webank.wecube.platform.core.service.plugin.PluginConfigService;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationContext;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInterfaceInvocationResult;
import com.webank.wecube.platform.core.service.workflow.PluginInvocationProcessor.PluginInvocationOperation;
import com.webank.wecube.platform.core.support.plugin.PluginServiceStub;

@Service
public class PluginInvocationService {
    private static final Logger log = LoggerFactory.getLogger(PluginInvocationService.class);

    private static final String MAPPING_TYPE_CONTEXT = "context";
    private static final String MAPPING_TYPE_ENTITY = "entity";
    private static final String MAPPING_TYPE_SYSTEM_VARIABLE = "system_variable";

    private static final int RESULT_CODE_OK = 0;
    private static final int RESULT_CODE_ERR = 1;

    @Autowired
    private PluginInvocationResultService pluginInvocationResultService;

    @Autowired
    private PluginServiceStub pluginServiceStub;

    @Autowired
    private PluginInvocationProcessor pluginInvocationProcessor;

    @Autowired
    private ProcInstInfoRepository procInstInfoRepository;

    @Autowired
    private TaskNodeInstInfoRepository taskNodeInstInfoRepository;

    @Autowired
    private TaskNodeDefInfoRepository taskNodeDefInfoRepository;

    @Autowired
    private PluginConfigService pluginConfigService;

    @Autowired
    private ProcExecBindingRepository procExecBindingRepository;

    @Autowired
    private TaskNodeExecParamRepository taskNodeExecParamRepository;

    @Autowired
    private SystemVariableService systemVariableService;

    @Autowired
    private PluginInstanceService pluginInstanceService;

    @Autowired
    private TaskNodeParamRepository taskNodeParamRepository;

    @Autowired
    private TaskNodeExecRequestRepository taskNodeExecRequestRepository;

    /**
     * 
     * @param cmd
     */
    public void invokePluginInterface(PluginInvocationCommand cmd) {
        if (log.isInfoEnabled()) {
            log.info("invoke plugin interface with:{}", cmd);
        }

        ProcInstInfoEntity procInstEntity = retrieveProcInstInfoEntity(cmd);
        TaskNodeInstInfoEntity taskNodeInstEntity = retrieveTaskNodeInstInfoEntity(procInstEntity.getId(),
                cmd.getNodeId());
        TaskNodeDefInfoEntity taskNodeDefEntity = retrieveTaskNodeDefInfoEntity(procInstEntity.getProcDefId(),
                cmd.getNodeId());
        List<ProcExecBindingEntity> nodeObjectBindings = retrieveProcExecBindingEntities(taskNodeInstEntity);
        PluginConfigInterface pluginConfigInterface = retrievePluginConfigInterface(taskNodeDefEntity, cmd.getNodeId());

        List<InputParamObject> inputParamObjs = calculateInputParamObjects(procInstEntity, taskNodeInstEntity,
                taskNodeDefEntity, nodeObjectBindings, pluginConfigInterface);

        PluginInterfaceInvocationContext ctx = new PluginInterfaceInvocationContext() //
                .withNodeObjectBindings(nodeObjectBindings) //
                .withPluginConfigInterface(pluginConfigInterface) //
                .withProcInstEntity(procInstEntity) //
                .withTaskNodeInstEntity(taskNodeInstEntity)//
                .withTaskNodeDefEntity(taskNodeDefEntity)//
                .withPluginInvocationCommand(cmd);

        parsePluginInstance(ctx);

        buildTaskNodeExecRequestEntity(ctx);
        List<Map<String, Object>> pluginParameters = calculateInputParameters(inputParamObjs, ctx.getRequestId());

        PluginInvocationOperation operation = new PluginInvocationOperation() //
                .withCallback(this::handlePluginInterfaceInvocationResult) //
                .withPluginServiceStub(this.pluginServiceStub) //
                .withPluginParameters(pluginParameters) //
                .withInstanceHost(ctx.getInstanceHost()) //
                .withInterfacePath(ctx.getInterfacePath()) //
                .withPluginInterfaceInvocationContext(ctx) //
                .withRequestId(ctx.getRequestId());

        pluginInvocationProcessor.process(operation);
    }

    private void buildTaskNodeExecRequestEntity(PluginInterfaceInvocationContext ctx) {

        TaskNodeExecRequestEntity formerRequestEntity = taskNodeExecRequestRepository
                .findCurrentEntityByNodeInstId(ctx.getTaskNodeInstEntity().getId());

        if (formerRequestEntity != null) {
            formerRequestEntity.setCurrent(false);
            formerRequestEntity.setUpdatedTime(new Date());
            taskNodeExecRequestRepository.save(formerRequestEntity);
        }

        String requestId = UUID.randomUUID().toString();

        TaskNodeInstInfoEntity taskNodeInstEntity = ctx.getTaskNodeInstEntity();

        TaskNodeExecRequestEntity requestEntity = new TaskNodeExecRequestEntity();
        requestEntity.setNodeInstId(taskNodeInstEntity.getId());
        requestEntity.setRequestId(requestId);
        requestEntity.setRequestUrl(ctx.getInstanceHost() + ctx.getInterfacePath());

        taskNodeExecRequestRepository.save(requestEntity);

        ctx.withTaskNodeExecRequestEntity(requestEntity);
        ctx.setRequestId(requestId);

    }

    private void parsePluginInstance(PluginInterfaceInvocationContext ctx) {
        PluginConfigInterface pluginConfigInterface = ctx.getPluginConfigInterface();
        PluginInstance pluginInstance = retrieveAvailablePluginInstance(pluginConfigInterface);
        String interfacePath = pluginConfigInterface.getPath();
        String instanceHost = String.format("%s://%s:%s", pluginConfigInterface.getHttpMethod(),
                pluginInstance.getHost(), pluginInstance.getPort());

        ctx.setInstanceHost(instanceHost);
        ctx.setInterfacePath(interfacePath);
    }

    private List<InputParamObject> calculateInputParamObjects(ProcInstInfoEntity procInstEntity,
            TaskNodeInstInfoEntity taskNodeInstEntity, TaskNodeDefInfoEntity taskNodeDefEntity,
            List<ProcExecBindingEntity> nodeObjectBindings, PluginConfigInterface pluginConfigInterface) {

        List<InputParamObject> inputParamObjs = new ArrayList<InputParamObject>();

        Set<PluginConfigInterfaceParameter> configInterfaceInputParams = pluginConfigInterface.getInputParameters();
        for (ProcExecBindingEntity nodeObjectBinding : nodeObjectBindings) {
            // TODO
            String entityId = nodeObjectBinding.getEntityId();

            InputParamObject inputObj = new InputParamObject();
            inputObj.setEntityId(entityId);

            // TODO to call data route service to get entity
            for (PluginConfigInterfaceParameter param : configInterfaceInputParams) {
                // TODO get from data route
                String paramName = param.getName();
                String paramType = param.getDataType();

                inputObj.addAttrNames(paramName);

                InputParamAttr inputAttr = new InputParamAttr();
                inputAttr.setName(paramName);
                inputAttr.setType(paramType);

                List<Object> objectVals = new ArrayList<Object>();
                //
                String mappingType = param.getMappingType();
                inputAttr.setMapType(mappingType);
                // TODO get value from entity expression
                if (MAPPING_TYPE_ENTITY.equals(mappingType)) {
                    String mappingEntityExpression = param.getMappingEntityExpression();
                    // TODO
                    List<Object> attrValsPerExpr = new ArrayList<Object>();

                    // TODO
                    objectVals.addAll(attrValsPerExpr);

                }

                // TODO get value from execution context
                if (MAPPING_TYPE_CONTEXT.equals(mappingType)) {
                    String curTaskNodeDefId = taskNodeDefEntity.getId();
                    TaskNodeParamEntity nodeParamEntity = taskNodeParamRepository
                            .findOneByTaskNodeDefIdAndParamName(curTaskNodeDefId, paramName);

                    if (nodeParamEntity == null) {
                        throw new WecubeCoreException("");
                    }

                    String bindNodeId = nodeParamEntity.getBindNodeId();
                    String bindParamType = nodeParamEntity.getBindParamType();
                    String bindParamName = nodeParamEntity.getBindParamName();

                    // get by procInstId and nodeId
                    TaskNodeInstInfoEntity bindNodeInstEntity = taskNodeInstInfoRepository
                            .findOneByProcInstIdAndNodeId(procInstEntity.getId(), bindNodeId);

                    if (bindNodeInstEntity == null) {
                        throw new WecubeCoreException("");
                    }

                    TaskNodeExecRequestEntity requestEntity = taskNodeExecRequestRepository
                            .findCurrentEntityByNodeInstId(bindNodeInstEntity.getId());

                    List<TaskNodeExecParamEntity> execParamEntities = taskNodeExecParamRepository
                            .findAllByRequestIdAndParamNameAndParamType(requestEntity.getRequestId(), bindParamName,
                                    bindParamType);

                    if (execParamEntities == null || execParamEntities.isEmpty()) {
                        if ("Y".equals(param.getRequired())) {
                            throw new WecubeCoreException("");
                        }
                    }

                    // TODO
                    // FIXME
                    String paramVal = execParamEntities.get(0).getParamDataValue();
                    // TODO
                    Object finalInputParam = paramVal;

                    objectVals.add(finalInputParam);
                }

                // TODO get value from system variable
                if (MAPPING_TYPE_SYSTEM_VARIABLE.equals(mappingType)) {
                    Integer svId = param.getMappingSystemVariableId();
                    SystemVariable sVariable = systemVariableService.getSystemVariableById(svId);

                    if (sVariable == null && "Y".equals(param.getRequired())) {
                        log.error("variable is null but is mandatory for {}", paramName);
                        throw new WecubeCoreException("Variable is null but mandatory.");
                    }

                    String sVal = sVariable.getValue();
                    if (StringUtils.isBlank(sVal)) {
                        sVal = sVariable.getDefaultValue();
                    }

                    if (StringUtils.isBlank(sVal) && "Y".equals(param.getRequired())) {
                        log.error("variable is blank but is mandatory for {}", paramName);
                        throw new WecubeCoreException("Variable is blank but mandatory.");
                    }

                    Object finalInputParam = null;
                    if ("int".equals(paramType)) {
                        finalInputParam = Integer.parseInt(sVal);
                    } else {
                        finalInputParam = sVal;
                    }

                    objectVals.add(finalInputParam);
                }

                inputAttr.addValues(objectVals);

                inputObj.addAttrs(inputAttr);
            }

            inputParamObjs.add(inputObj);

        }

        return inputParamObjs;
    }

    private PluginConfigInterface retrievePluginConfigInterface(TaskNodeDefInfoEntity taskNodeDefEntity,
            String nodeId) {
        String serviceId = retrieveServiceId(taskNodeDefEntity, nodeId);
        PluginConfigInterface pluginConfigInterface = pluginConfigService
                .getPluginConfigInterfaceByServiceName(serviceId);

        if (pluginConfigInterface == null) {
            log.error("Plugin config interface does not exist for {} {} {}", taskNodeDefEntity.getId(), nodeId,
                    serviceId);
            throw new WecubeCoreException("Plugin config interface does not exist.");
        }

        return pluginConfigInterface;
    }

    private List<ProcExecBindingEntity> retrieveProcExecBindingEntities(TaskNodeInstInfoEntity taskNodeInstEntity) {
        List<ProcExecBindingEntity> nodeObjectBindings = procExecBindingRepository
                .findAllTaskNodeBindings(taskNodeInstEntity.getProcInstId(), taskNodeInstEntity.getId());

        if (nodeObjectBindings == null) {
            log.warn("node object bindings is empty for {} {}", taskNodeInstEntity.getProcInstId(),
                    taskNodeInstEntity.getId());
            nodeObjectBindings = new ArrayList<>();
        }

        return nodeObjectBindings;
    }

    private String retrieveServiceId(TaskNodeDefInfoEntity taskNodeDefEntity, String nodeId) {
        String serviceId = taskNodeDefEntity.getServiceId();
        if (StringUtils.isBlank(serviceId)) {
            log.error("service ID is invalid for {} {}", taskNodeDefEntity.getProcDefId(), nodeId);
            throw new WecubeCoreException("Service ID is invalid.");
        }

        if (log.isDebugEnabled()) {
            log.info("retrieved service id {} for {},{}", serviceId, taskNodeDefEntity.getProcDefId(), nodeId);
        }
        return serviceId;
    }

    private TaskNodeInstInfoEntity retrieveTaskNodeInstInfoEntity(Integer procInstId, String nodeId) {
        TaskNodeInstInfoEntity taskNodeInstEntity = taskNodeInstInfoRepository.findOneByProcInstIdAndNodeId(procInstId,
                nodeId);
        if (taskNodeInstEntity == null) {
            log.error("Task node instance does not exist for {} {}", procInstId, nodeId);
            throw new WecubeCoreException("Task node instance does not exist.");
        }

        if (!TaskNodeInstInfoEntity.IN_PROGRESS_STATUS.equals(taskNodeInstEntity.getStatus())) {
            String originalStatus = taskNodeInstEntity.getStatus();
            taskNodeInstEntity.setStatus(TaskNodeInstInfoEntity.IN_PROGRESS_STATUS);
            taskNodeInstEntity.setUpdatedTime(new Date());

            if (log.isDebugEnabled()) {
                log.debug("task node instance {} update status from {} to {}", taskNodeInstEntity.getId(),
                        originalStatus, taskNodeInstEntity.getStatus());
            }

            taskNodeInstInfoRepository.save(taskNodeInstEntity);
        }

        return taskNodeInstEntity;
    }

    private TaskNodeDefInfoEntity retrieveTaskNodeDefInfoEntity(String procDefId, String nodeId) {
        TaskNodeDefInfoEntity taskNodeDefEntity = taskNodeDefInfoRepository
                .findOneWithProcessIdAndNodeIdAndStatus(procDefId, nodeId, TaskNodeDefInfoEntity.DEPLOYED_STATUS);

        if (taskNodeDefEntity == null) {
            log.error("Task node definition does not exist for {} {} {}", procDefId, nodeId,
                    TaskNodeDefInfoEntity.DEPLOYED_STATUS);
            throw new WecubeCoreException("Task node definition does not exist.");
        }

        return taskNodeDefEntity;
    }

    private ProcInstInfoEntity retrieveProcInstInfoEntity(PluginInvocationCommand cmd) {
        String procInstKernelId = cmd.getProcInstId();
        ProcInstInfoEntity procInstEntity = procInstInfoRepository.findOneByProcInstKernelId(procInstKernelId);

        if (procInstEntity == null) {
            log.error("Process instance info does not exist for id:{}", procInstKernelId);
            throw new WecubeCoreException("Process instance information does not exist.");
        }

        if (!ProcInstInfoEntity.IN_PROGRESS_STATUS.equals(procInstEntity.getStatus())) {

            String orignalStatus = procInstEntity.getStatus();
            procInstEntity.setUpdatedTime(new Date());
            procInstEntity.setStatus(ProcInstInfoEntity.IN_PROGRESS_STATUS);

            if (log.isDebugEnabled()) {
                log.debug("process instance {} update status from {} to {}", procInstEntity.getId(), orignalStatus,
                        ProcInstInfoEntity.IN_PROGRESS_STATUS);
            }

            procInstInfoRepository.save(procInstEntity);
        }

        return procInstEntity;
    }

    private List<Map<String, Object>> calculateInputParameters(List<InputParamObject> inputParamObjs,
            String requestId) {
        List<Map<String, Object>> pluginParameters = new ArrayList<Map<String, Object>>();

        int objectId = 0;

        for (InputParamObject ipo : inputParamObjs) {
            if (log.isDebugEnabled()) {
                log.debug("process input parameters for entity:{}", ipo.getEntityId());
            }

            String sObjectId = String.valueOf(objectId);
            String entityId = ipo.getEntityId();

            Map<String, Object> inputMap = new HashMap<String, Object>();

            for (InputParamAttr attr : ipo.getAttrs()) {
                TaskNodeExecParamEntity e = new TaskNodeExecParamEntity();
                e.setRequestId(requestId);
                e.setParamName(attr.getName());
                e.setParamDataType(attr.getType());
                e.setObjectId(sObjectId);
                e.setParamDataValue(attr.getValuesAsString());
                e.setNodeRootEntityId(entityId);

                taskNodeExecParamRepository.save(e);

                inputMap.put(attr.getName(), attr.getExpectedValue());
            }

            pluginParameters.add(inputMap);

            objectId++;
        }

        return pluginParameters;
    }

    private PluginInstance retrieveAvailablePluginInstance(PluginConfigInterface itf) {
        PluginConfig config = itf.getPluginConfig();
        String pluginName = config.getName();

        List<PluginInstance> instances = pluginInstanceService.getRunningPluginInstances(pluginName);

        return instances.get(0);

    }

    protected String asString(Object val, String sType) {
        if (val == null) {
            return null;
        }
        if (val instanceof String && "str".equals(sType)) {
            return (String) val;
        }

        if (val instanceof Integer && "int".equals(sType)) {
            return String.valueOf(val);
        }

        // TODO
        return val.toString();

    }

    protected Object fromString(String val, String sType) {
        if ("str".equals(sType)) {
            return val;
        }

        if (StringUtils.isBlank(val)) {
            return null;
        }

        if ("int".equals(sType)) {
            return Integer.parseInt(val);
        }

        // TODO
        return val;
    }

    public void handlePluginInterfaceInvocationResult(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("handle plugin interface invocation result");
        }

        PluginInvocationResult result = new PluginInvocationResult()
                .parsePluginInvocationCommand(ctx.getPluginInvocationCommand());

        if (!pluginInvocationResult.isSuccess() || pluginInvocationResult.hasErrors()) {
            log.error("system errors:{}", pluginInvocationResult.getErrMsg());
            result.setResultCode(RESULT_CODE_ERR);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "400", "system errors");

            return;
        }

        List<Object> resultData = pluginInvocationResult.getResultData();
        PluginConfigInterface pluginConfigInterface = ctx.getPluginConfigInterface();
        Set<PluginConfigInterfaceParameter> outputParameters = pluginConfigInterface.getOutputParameters();

        if (resultData == null) {
            if (outputParameters == null || outputParameters.isEmpty()) {
                log.debug("output parameter is NOT configured for interface {}",
                        pluginConfigInterface.getServiceName());
                result.setResultCode(RESULT_CODE_OK);
                pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);
                return;
            }

            if (outputParameters != null && !outputParameters.isEmpty()) {
                if (ctx.getPluginParameters() == null || ctx.getPluginParameters().isEmpty()) {
                    log.debug("output parameter is configured but INPUT is empty for interface {}",
                            pluginConfigInterface.getServiceName());
                    result.setResultCode(RESULT_CODE_OK);
                    pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                    handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);
                    return;
                } else {
                    log.error("output parameter is configured but result is empty for interface {}",
                            pluginConfigInterface.getServiceName());
                    result.setResultCode(RESULT_CODE_ERR);
                    pluginInvocationResultService.responsePluginInterfaceInvocation(result);
                    handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "100", "output is null");
                    return;
                }
            }
        }

        try {
            handleResultData(pluginInvocationResult, ctx, resultData);
            result.setResultCode(RESULT_CODE_OK);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationSuccess(pluginInvocationResult, ctx);
        } catch (Exception e) {
            log.error("result data handling failed", e);
            result.setResultCode(RESULT_CODE_ERR);
            pluginInvocationResultService.responsePluginInterfaceInvocation(result);
            handlePluginInterfaceInvocationFailure(pluginInvocationResult, ctx, "101",
                    "result data handling failed:" + e.getMessage());
        }

        return;
    }

    private void handleResultData(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, List<Object> resultData) {

        for (Object obj : resultData) {
            if (obj == null) {
                continue;
            }

            if (!(obj instanceof Map)) {
                log.error("unexpected data type:returned object is not a instance of map, obj={}", obj);
                throw new WecubeCoreException("Unexpected data type");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> retRecord = (Map<String, Object>) obj;
        }
        // TODO
        // Scenario 4: if output not needed and no need to write back to
        // entities
        // scenario 5: write back to entities if output configured

    }

    private void handlePluginInterfaceInvocationSuccess(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx) {
        //TODO
    }

    private void handlePluginInterfaceInvocationFailure(PluginInterfaceInvocationResult pluginInvocationResult,
            PluginInterfaceInvocationContext ctx, String errorCode, String errorMsg) {
        //TODO
    }

}
