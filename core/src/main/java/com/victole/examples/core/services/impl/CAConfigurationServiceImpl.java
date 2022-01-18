package com.victole.examples.core.services.impl;

import com.victole.examples.core.models.config.SiteConfigurationModel;
import com.victole.examples.core.services.CAConfigurationService;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.ConfigurationBuilder;
import org.osgi.service.component.annotations.Component;

@Component(service = CAConfigurationService.class, immediate = true)
public class CAConfigurationServiceImpl implements CAConfigurationService {

    @Override
    public SiteConfigurationModel getSiteConfiguration(Resource resource) {
        return getSiteConfigurationModel(resource);
    }

    private SiteConfigurationModel getSiteConfigurationModel(Resource resource) {
        ConfigurationBuilder configBuilder = resource.adaptTo(ConfigurationBuilder.class);
        if (configBuilder != null) {
            return configBuilder.as(SiteConfigurationModel.class);
        }
        return null;
    }
}

