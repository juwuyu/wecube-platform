package com.webank.wecube.platform.core.entity.plugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class PluginPackageEntities {
    private String id;

    private String dataModelId;

    private Integer dataModelVersion;

    private String packageName;

    private String name;

    private String displayName;

    private String description;

    private transient PluginPackageDataModel pluginPackageDataModel;

    private transient List<PluginPackageAttributes> pluginPackageAttributes = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id == null ? null : id.trim();
    }

    public String getDataModelId() {
        return dataModelId;
    }

    public void setDataModelId(String dataModelId) {
        this.dataModelId = dataModelId == null ? null : dataModelId.trim();
    }

    public Integer getDataModelVersion() {
        return dataModelVersion;
    }

    public void setDataModelVersion(Integer dataModelVersion) {
        this.dataModelVersion = dataModelVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName == null ? null : packageName.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName == null ? null : displayName.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public PluginPackageDataModel getPluginPackageDataModel() {
        return pluginPackageDataModel;
    }

    public void setPluginPackageDataModel(PluginPackageDataModel pluginPackageDataModel) {
        this.pluginPackageDataModel = pluginPackageDataModel;
    }

    public List<PluginPackageAttributes> getPluginPackageAttributes() {
        return pluginPackageAttributes;
    }

    public void setPluginPackageAttributes(List<PluginPackageAttributes> pluginPackageAttributes) {
        this.pluginPackageAttributes = pluginPackageAttributes;
    }
    
    public PluginPackageAttributes getPluginPackageAttributesByAttrName(String attrName) {
        if(StringUtils.isBlank(attrName)) {
            return null;
        }
        
        if(this.pluginPackageAttributes == null || this.pluginPackageAttributes.isEmpty()) {
            return null;
        }
        
        for(PluginPackageAttributes a : this.pluginPackageAttributes) {
            if(attrName.equals(a.getName())) {
                return a;
            }
        }
        
        return null;
    }

}