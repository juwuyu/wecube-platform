package com.webank.wecube.platform.core.controller.workflow;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.webank.wecube.platform.core.dto.plugin.CommonResponseDto;
import com.webank.wecube.platform.core.dto.plugin.QueryResponse;
import com.webank.wecube.platform.core.dto.workflow.ProcInstInfoDto;
import com.webank.wecube.platform.core.dto.workflow.ProceedProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.ProcessDataPreviewDto;
import com.webank.wecube.platform.core.dto.workflow.StartProcInstRequestDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeDefObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeExecContextDto;
import com.webank.wecube.platform.core.dto.workflow.TaskNodeInstObjectBindInfoDto;
import com.webank.wecube.platform.core.dto.workflow.WorkflowInstQueryDto;
import com.webank.wecube.platform.core.service.workflow.WorkflowDataService;
import com.webank.wecube.platform.core.service.workflow.WorkflowProcInstService;

@RestController
@RequestMapping("/v1")
public class WorkflowProcessInstanceController {

    @Autowired
    private WorkflowProcInstService procInstService;

    @Autowired
    private WorkflowDataService workflowDataService;

    /**
     * Start a new process instance.
     * 
     * @param requestDto
     * @return
     */
    @PostMapping("/process/instances")
    public CommonResponseDto createProcessInstance(@RequestBody StartProcInstRequestDto requestDto) {
        ProcInstInfoDto result = procInstService.createProcessInstanceWithPermissionValidation(requestDto);
        return CommonResponseDto.okayWithData(result);
    }

    /**
     * Fetch all process instances.
     * 
     * @return
     */
    @GetMapping("/process/instances")
    public CommonResponseDto getProcessInstances() {
        List<ProcInstInfoDto> result = procInstService.getProcessInstances();
        return CommonResponseDto.okayWithData(result);
    }
    
    /**
     * Fetch page able process instances.
     * 
     * @return
     */
    @PostMapping("/process/instancesWithPaging")
    public CommonResponseDto getPageableProcessInstances(@RequestBody WorkflowInstQueryDto queryDto) {
        
        QueryResponse<ProcInstInfoDto> itemsResponse = procInstService
                .getPageableProcessInstances(queryDto);
        return CommonResponseDto.okayWithData(itemsResponse);
    }

    /**
     * Fetch specific process instance.
     * 
     * @param procInstId
     * @return
     */
    @GetMapping("/process/instances/{id}")
    public CommonResponseDto getProcessInstance(@PathVariable(name = "id") Integer procInstId) {
        ProcInstInfoDto result = procInstService.getProcessInstanceById(procInstId);
        if (result == null) {
            return CommonResponseDto.error(String.format("Process instance [%s] does not exist.", procInstId));
        }

        return CommonResponseDto.okayWithData(result);
    }

    /**
     * Proceed once errors occurred manually.
     * 
     * @param requestDto
     * @return
     */
    @PostMapping("/process/instances/proceed")
    public CommonResponseDto proceedProcessInstance(@RequestBody ProceedProcInstRequestDto requestDto) {
        procInstService.proceedProcessInstance(requestDto);
        return CommonResponseDto.okay();
    }

    /**
     * 
     * @param nodeDefId
     * @param processSessionId
     * @param bindings
     * @return
     */
    @RequestMapping(path = "/process/instances/tasknodes/{node-def-id}/session/{process-session-id}/tasknode-bindings", method = {
            RequestMethod.POST, RequestMethod.PUT }, consumes = {
                    MediaType.APPLICATION_JSON_VALUE }, produces = { MediaType.APPLICATION_JSON_VALUE })
    public CommonResponseDto updateProcessInstanceExecBindingsOfSession(
            @PathVariable(name = "node-def-id") String nodeDefId,
            @PathVariable(name = "process-session-id") String processSessionId,
            @RequestBody List<TaskNodeDefObjectBindInfoDto> bindings) {
        workflowDataService.updateProcessInstanceExecBindingsOfSession(nodeDefId, processSessionId, bindings);
        return CommonResponseDto.okay();
    }

    /**
     * 
     * @param processSessionId
     * @return
     */
    @GetMapping("/process/instances/tasknodes/session/{process-session-id}/tasknode-bindings")
    public CommonResponseDto getProcessInstanceExecBindingsOfSession(
            @PathVariable(name = "process-session-id") String processSessionId) {

        List<TaskNodeDefObjectBindInfoDto> result = workflowDataService
                .getProcessInstanceExecBindingsOfSession(processSessionId);
        return CommonResponseDto.okayWithData(result);

    }

    /**
     * 
     * @param nodeDefId
     * @param processSessionId
     * @return
     */
    @GetMapping("/process/instances/tasknodes/{node-def-id}/session/{process-session-id}/tasknode-bindings")
    public CommonResponseDto getProcessInstanceExecBindingsOfSessionAndNode(
            @PathVariable(name = "node-def-id") String nodeDefId,
            @PathVariable(name = "process-session-id") String processSessionId) {

        List<TaskNodeDefObjectBindInfoDto> result = workflowDataService
                .getProcessInstanceExecBindingsOfSessionAndNode(nodeDefId, processSessionId);

        return CommonResponseDto.okayWithData(result);
    }

    /**
     * 
     * @param procInstId
     * @return
     */
    @GetMapping("/process/instances/{proc-inst-id}/tasknode-bindings")
    public CommonResponseDto getProcessInstanceExecBindings(@PathVariable(name = "proc-inst-id") Integer procInstId) {
        List<TaskNodeDefObjectBindInfoDto> result = procInstService.getProcessInstanceExecBindings(procInstId);
        return CommonResponseDto.okayWithData(result);
    }

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    @GetMapping("/process/instances/{proc-inst-id}/tasknodes/{node-inst-id}/tasknode-bindings")
    public CommonResponseDto getTaskNodeInstanceExecBindings(@PathVariable(name = "proc-inst-id") Integer procInstId,
            @PathVariable(name = "node-inst-id") Integer nodeInstId) {
        List<TaskNodeInstObjectBindInfoDto> result = workflowDataService.getTaskNodeInstanceExecBindings(procInstId,
                nodeInstId);
        return CommonResponseDto.okayWithData(result);
    }

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    @PostMapping("/process/instances/{proc-inst-id}/tasknodes/{node-inst-id}/tasknode-bindings")
    public CommonResponseDto updateTaskNodeInstanceExecBindings(@PathVariable(name = "proc-inst-id") Integer procInstId,
            @PathVariable(name = "node-inst-id") Integer nodeInstId,
            @RequestBody List<TaskNodeInstObjectBindInfoDto> bindings) {
        workflowDataService.updateTaskNodeInstanceExecBindings(procInstId, nodeInstId, bindings);
        return CommonResponseDto.okay();
    }

    /**
     * 
     * @param procInstId
     * @param nodeInstId
     * @return
     */
    @GetMapping("/process/instances/{proc-inst-id}/tasknodes/{node-inst-id}/context")
    public CommonResponseDto getTaskNodeContextInfo(@PathVariable(name = "proc-inst-id") Integer procInstId,
            @PathVariable(name = "node-inst-id") Integer nodeInstId) {
        TaskNodeExecContextDto result = workflowDataService.getTaskNodeContextInfo(procInstId, nodeInstId);
        return CommonResponseDto.okayWithData(result);
    }

    /**
     * 
     * @param procInstId
     * @return
     */
    @GetMapping("/process/instances/{proc-inst-id}/preview/entities")
    public CommonResponseDto getProcessDataPreview(@PathVariable(name = "proc-inst-id") Integer procInstId) {
        ProcessDataPreviewDto result = workflowDataService.generateProcessDataPreviewForProcInstance(procInstId);
        return CommonResponseDto.okayWithData(result);
    }
}
