package com.sap.cloud.lm.sl.cf.core.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sap.cloud.lm.sl.cf.core.dto.persistence.ConfigurationSubscriptionDto;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationSubscription;
import com.sap.cloud.lm.sl.common.ConflictException;
import com.sap.cloud.lm.sl.common.NotFoundException;

@Component
public class ConfigurationSubscriptionDao {

    @Autowired
    protected ConfigurationSubscriptionDtoDao dao;

    public List<ConfigurationSubscription> findAll() {
        return toConfigurationSubscriptions(dao.findAll());
    }

    public ConfigurationSubscription update(long id, ConfigurationSubscription subscription) throws ConflictException, NotFoundException {
        return dao.update(id, new ConfigurationSubscriptionDto(subscription)).toConfigurationSubscription();
    }

    public ConfigurationSubscription remove(long id) throws NotFoundException {
        return dao.remove(id).toConfigurationSubscription();
    }

    public ConfigurationSubscription add(ConfigurationSubscription subscriptionn) throws ConflictException {
        return dao.add(new ConfigurationSubscriptionDto(subscriptionn)).toConfigurationSubscription();
    }

    public List<ConfigurationSubscription> findAll(String mtaId, String appName, String spaceId, String resourceName) {
        return toConfigurationSubscriptions(dao.findAll(mtaId, appName, spaceId, resourceName));
    }

    public List<ConfigurationSubscription> findAll(List<ConfigurationEntry> entriess) {
        return findAll().stream().filter((subscriptionn) -> subscriptionn.matches(entriess)).collect(Collectors.toList());
    }

    private static List<ConfigurationSubscription> toConfigurationSubscriptions(List<ConfigurationSubscriptionDto> dtos) {
        return dtos.stream().map((dto) -> dto.toConfigurationSubscription()).collect(Collectors.toList());
    }

    public ConfigurationSubscription find(long id) throws NotFoundException {
        return dao.find(id).toConfigurationSubscription();
    }

}
