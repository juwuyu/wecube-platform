package com.webank.wecube.platform.core.dto.workflow;

import java.util.List;

import com.webank.wecube.platform.core.dto.plugin.PageableDto;
import com.webank.wecube.platform.core.dto.plugin.SortingDto;

public class WorkflowExecutionReportQueryDto {
    private String startDate;
    private String endDate;
    private List<String> procDefIds;
    private List<String> taskNodeIds;

    private List<String> serviceIds;

    private List<String> entityDataIds;
    
    protected PageableDto pageable = new PageableDto();
    
    protected SortingDto sorting;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<String> getProcDefIds() {
        return procDefIds;
    }

    public void setProcDefIds(List<String> procDefIds) {
        this.procDefIds = procDefIds;
    }

    public List<String> getTaskNodeIds() {
        return taskNodeIds;
    }

    public void setTaskNodeIds(List<String> taskNodeIds) {
        this.taskNodeIds = taskNodeIds;
    }

    public List<String> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds;
    }

    public List<String> getEntityDataIds() {
        return entityDataIds;
    }

    public void setEntityDataIds(List<String> entityDataIds) {
        this.entityDataIds = entityDataIds;
    }

    public PageableDto getPageable() {
        return pageable;
    }

    public void setPageable(PageableDto pageable) {
        this.pageable = pageable;
    }

    public SortingDto getSorting() {
        return sorting;
    }

    public void setSorting(SortingDto sorting) {
        this.sorting = sorting;
    }

    
}
