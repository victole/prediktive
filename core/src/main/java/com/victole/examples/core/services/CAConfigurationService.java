package com.victole.examples.core.services;

import com.victole.examples.core.models.config.SiteConfigurationModel;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;

/**
 * Sling CA Configuration Service
 */
public interface CAConfigurationService {

    SiteConfigurationModel getSiteConfiguration(Resource resource);
}
