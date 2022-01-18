package com.victole.examples.core.utils;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import java.util.HashMap;
import java.util.Map;

public final class ResourceResolverProvider {

    private ResourceResolverProvider() {
    }

    /**
     * Returns the resource resolver using the System User passed by parameter.
     *
     * @throws LoginException
     *             if the resource resolver can't be resolved
     */
    public static ResourceResolver getServiceResourceResolver(ResourceResolverFactory resourceResolverFactory, String serviceUsername) throws LoginException {
        Map<String, Object> param = new HashMap<>();
        param.put(ResourceResolverFactory.SUBSERVICE, serviceUsername);
        return resourceResolverFactory.getServiceResourceResolver(param);
    }

}
