package com.webank.wecube.platform.core.dto;

import com.webank.wecube.platform.core.domain.plugin.PluginPackageAttribute;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageDataModel;
import com.webank.wecube.platform.core.domain.plugin.PluginPackageEntity;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TrimmedPluginPackageEntityDto {
    private Integer id;
    private String packageName;
    private Integer dataModelVersion;
    private String name;
    private String displayName;


    public TrimmedPluginPackageEntityDto(Integer entityId, String packageName, Integer dataModelVersion, String name, String displayName) {
        this.id = entityId;
        this.packageName = packageName;
        this.name = name;
        this.displayName = displayName;
        this.dataModelVersion = dataModelVersion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getDataModelVersion() {
        return dataModelVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TrimmedPluginPackageEntityDto that = (TrimmedPluginPackageEntityDto) o;

        return new EqualsBuilder()
                .append(getPackageName(), that.getPackageName())
                .append(getName(), that.getName())
                .append(getDisplayName(), that.getDisplayName())
                .append(getDataModelVersion(), that.getDataModelVersion())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getPackageName())
                .append(getName())
                .append(getDisplayName())
                .append(getDataModelVersion())
                .toHashCode();
    }
}

public class PluginPackageEntityDto {
    private Integer id;
    private String packageName;
    private String name;
    private String displayName;
    private String description;

    private Integer dataModelVersion;
    private Set<TrimmedPluginPackageEntityDto> referenceToEntityList = new HashSet<>();
    private Set<TrimmedPluginPackageEntityDto> referenceByEntityList = new HashSet<>();
    private List<PluginPackageAttributeDto> attributes = new ArrayList<>();


    public PluginPackageEntityDto(Integer id, String name, String displayName, String description, List<PluginPackageAttributeDto> attributes) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.attributes = attributes;
    }

    public PluginPackageEntityDto() {
    }

    /**
     * @param pluginPackageEntity input entity domain object
     * @return entity dto exposed to the server
     */
    public static PluginPackageEntityDto fromDomain(PluginPackageEntity pluginPackageEntity) {
        PluginPackageEntityDto pluginPackageEntityDto = new PluginPackageEntityDto();
        pluginPackageEntityDto.setId(pluginPackageEntity.getId());
        pluginPackageEntityDto.setPackageName(pluginPackageEntity.getPluginPackageDataModel().getPackageName());
        pluginPackageEntityDto.setName(pluginPackageEntity.getName());
        pluginPackageEntityDto.setDisplayName(pluginPackageEntity.getDisplayName());
        pluginPackageEntityDto.setDescription(pluginPackageEntity.getDescription());
        pluginPackageEntityDto.setDataModelVersion(pluginPackageEntity.getPluginPackageDataModel().getVersion());
        if (pluginPackageEntity.getPluginPackageAttributeList() != null) {
            pluginPackageEntity.getPluginPackageAttributeList()
                    .forEach(pluginPackageAttribute -> pluginPackageEntityDto.attributes
                            .add(PluginPackageAttributeDto.fromDomain(pluginPackageAttribute)));
        }
        return pluginPackageEntityDto;
    }

    /**
     * @param pluginPackageEntityDto input entity dto
     * @param dataModel
     * @return transformed entity domain object
     */
    public static PluginPackageEntity toDomain(PluginPackageEntityDto pluginPackageEntityDto, PluginPackageDataModel dataModel) {

        PluginPackageEntity pluginPackageEntity = new PluginPackageEntity(dataModel, pluginPackageEntityDto.getName(), pluginPackageEntityDto. getDisplayName(), pluginPackageEntityDto.getDescription());

        if (pluginPackageEntityDto.getAttributes() != null) {
            List<PluginPackageAttribute> pluginPackageAttributeList = new ArrayList<>();
            for (PluginPackageAttributeDto pluginPackageAttributeDto : pluginPackageEntityDto.getAttributes()) {
                pluginPackageAttributeList.add(PluginPackageAttributeDto.toDomain(pluginPackageAttributeDto, null, pluginPackageEntity));
            }
            pluginPackageEntity.setPluginPackageAttributeList(pluginPackageAttributeList);
        }

        return pluginPackageEntity;
    }

    public void updateReferenceBy(Integer entityId, String packageName, Integer dataModelVersion, String name, String displayName) {
        TrimmedPluginPackageEntityDto trimmedPluginPackageEntityDto = new TrimmedPluginPackageEntityDto(entityId, packageName, dataModelVersion, name, displayName);
        this.referenceByEntityList.add(trimmedPluginPackageEntityDto);
    }

    public void updateReferenceTo(Integer entityId, String packageName, Integer dataModelVersion, String name, String displayName) {
        TrimmedPluginPackageEntityDto trimmedPluginPackageEntityDto = new TrimmedPluginPackageEntityDto(entityId, packageName, dataModelVersion, name, displayName);
        this.referenceToEntityList.add(trimmedPluginPackageEntityDto);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDataModelVersion() {
        return dataModelVersion;
    }

    public void setDataModelVersion(Integer dataModelVersion) {
        this.dataModelVersion = dataModelVersion;
    }

    public List<PluginPackageAttributeDto> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PluginPackageAttributeDto> attributes) {
        this.attributes = attributes;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Set<TrimmedPluginPackageEntityDto> getReferenceToEntityList() {
        return referenceToEntityList;
    }

    public void setReferenceToEntityList(Set<TrimmedPluginPackageEntityDto> referenceToEntityList) {
        this.referenceToEntityList = referenceToEntityList;
    }

    public Set<TrimmedPluginPackageEntityDto> getReferenceByEntityList() {
        return referenceByEntityList;
    }

    public void setReferenceByEntityList(Set<TrimmedPluginPackageEntityDto> referenceByEntityList) {
        this.referenceByEntityList = referenceByEntityList;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
