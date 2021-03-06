package com.sap.cloud.lm.sl.cf.core.model;

import static java.text.MessageFormat.format;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;
import com.sap.cloud.lm.sl.cf.core.dao.filters.ConfigurationFilter;
import com.sap.cloud.lm.sl.mta.message.Messages;
import com.sap.cloud.lm.sl.mta.model.v1_0.Module;
import com.sap.cloud.lm.sl.mta.model.v1_0.Resource;
import com.sap.cloud.lm.sl.mta.model.v2_0.ProvidedDependency;
import com.sap.cloud.lm.sl.mta.model.v2_0.RequiredDependency;

public class ConfigurationSubscription {

    private long id;

    @Expose
    private String mtaId;
    @Expose
    private ConfigurationFilter filter;
    @Expose
    private String spaceId;
    @Expose
    private String appName;
    @Expose
    private ModuleDto moduleDto;
    @Expose
    private ResourceDto resourceDto;

    public ConfigurationSubscription(long id, String mtaId, String spaceId, String appName, ConfigurationFilter filter, ModuleDto moduleDto,
        ResourceDto resourceDto) {
        this.filter = filter;
        this.spaceId = spaceId;
        this.appName = appName;
        this.id = id;
        this.moduleDto = moduleDto;
        this.mtaId = mtaId;
        this.resourceDto = resourceDto;
    }

    public long getId() {
        return id;
    }

    public ConfigurationFilter getFilter() {
        return filter;
    }

    public String getMtaId() {
        return mtaId;
    }

    public String getAppName() {
        return appName;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public ModuleDto getModuleDto() {
        return moduleDto;
    }

    public ResourceDto getResourceDto() {
        return resourceDto;
    }

    public static ConfigurationSubscription from(String mtaId, String spaceId, String appName, ConfigurationFilter filter, Module module,
        Resource resource, int majorSchemaVersion) {
        switch (majorSchemaVersion) {
            case 2:
                ResourceDto resourceDto = ResourceDto.from2((com.sap.cloud.lm.sl.mta.model.v2_0.Resource) resource);
                ModuleDto moduleDto = ModuleDto.from2((com.sap.cloud.lm.sl.mta.model.v2_0.Module) module);
                return new ConfigurationSubscription(00, mtaId, spaceId, appName, filter, moduleDto, resourceDto);
            default:
                throw new UnsupportedOperationException(format(Messages.UNSUPPORTED_VERSION, majorSchemaVersion));
        }
    }

    public static class ModuleDto {

        @Expose
        private String name;
        @Expose
        private Map<String, Object> properties;
        @Expose
        private List<ProvidedDependencyDto> providedDependencies;
        @Expose
        private List<RequiredDependencyDto> requiredDependencies;

        public ModuleDto(String name, Map<String, Object> properties, List<ProvidedDependencyDto> providedDependencies,
            List<RequiredDependencyDto> requiredDependencies) {
            this.name = name;
            this.properties = properties;
            this.providedDependencies = providedDependencies;
            this.requiredDependencies = requiredDependencies;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public List<ProvidedDependencyDto> getProvidedDependencies() {
            return providedDependencies;
        }

        public List<RequiredDependencyDto> getRequiredDependencies() {
            return requiredDependencies;
        }

        public static ModuleDto from2(com.sap.cloud.lm.sl.mta.model.v2_0.Module module) {
            return new ModuleDto(module.getName(), module.getProperties(), fromProvidedDependencies2(module.getProvidedDependencies2_0()),
                fromRequiredDependencies2(module.getRequiredDependencies2_0()));
        }

        private static List<ProvidedDependencyDto> fromProvidedDependencies2(List<ProvidedDependency> providedDependencies) {
            return providedDependencies.stream().map((dependency) -> ProvidedDependencyDto.from2(dependency)).collect(Collectors.toList());
        }

        private static List<RequiredDependencyDto> fromRequiredDependencies2(List<RequiredDependency> requiredDependencies) {
            return requiredDependencies.stream().map((dependency) -> RequiredDependencyDto.from2(dependency)).collect(Collectors.toList());
        }

    }

    public static class ResourceDto {

        @Expose
        private String name;
        @Expose
        private Map<String, Object> properties;

        public ResourceDto(String name, Map<String, Object> properties) {
            this.name = name;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public static ResourceDto from2(com.sap.cloud.lm.sl.mta.model.v2_0.Resource resource) {
            return new ResourceDto(resource.getName(), resource.getProperties());
        }

    }

    public static class RequiredDependencyDto {

        @Expose
        private String name;
        @Expose
        private String list;
        @Expose
        private Map<String, Object> properties;

        public RequiredDependencyDto(String name, String list, Map<String, Object> properties) {
            this.name = name;
            this.list = list;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public String getList() {
            return list;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public static RequiredDependencyDto from2(com.sap.cloud.lm.sl.mta.model.v2_0.RequiredDependency requiredDependency) {
            return new RequiredDependencyDto(requiredDependency.getName(), requiredDependency.getList(),
                requiredDependency.getProperties());
        }

    }

    public static class ProvidedDependencyDto {

        @Expose
        private String name;
        @Expose
        private Map<String, Object> properties;

        public ProvidedDependencyDto(String name, Map<String, Object> properties) {
            this.name = name;
            this.properties = properties;
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public static ProvidedDependencyDto from2(com.sap.cloud.lm.sl.mta.model.v2_0.ProvidedDependency providedDependency) {
            return new ProvidedDependencyDto(providedDependency.getName(), providedDependency.getProperties());
        }

    }

    public boolean matches(List<ConfigurationEntry> entries) {
        return entries.stream().anyMatch((entry) -> filter.matches(entry));
    }

    public boolean matches(ConfigurationEntry entry) {
        return filter.matches(entry);
    }

}
