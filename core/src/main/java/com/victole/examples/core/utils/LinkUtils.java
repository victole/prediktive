package com.victole.examples.core.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LinkUtils {

    private LinkUtils() {}

    private static final Logger LOG = LoggerFactory.getLogger(LinkUtils.class);

    /**
     * Externalize page path
     *
     * @param path page path
     */
    public static String externalizeLink(String path, ResourceResolver resourceResolver) {
        return resourceResolver.map(sanitizeLink(path));
    }

    /**
     * Add .html to link if is internal
     *
     * @param link
     */
    public static String sanitizeLink(String link) {
        if (StringUtils.isBlank(link)) {
            return "";
        }
        else if (link.startsWith("/content/")) {
            return link + ".html";
        }
        return link;
    }

    /**
     * Return if path is internal AEM path
     * @param path path to check
     * @return true if internal, false is other
     */
    public static boolean isInternalPagePath(String path) {
        return StringUtils.startsWith(path,"/content");
    }

    /**
     * Return mapped URL if internal and un-mapped if external.
     * @param url url to map or not
     * @param resourceResolver resolver with mapping configurations
     * @return mapped url
     */
    public static String getMappedUrl(String url, ResourceResolver resourceResolver) {
        if (!StringUtils.isEmpty(url)) {
            if (url.startsWith("/content/")) {
                return resourceResolver.map(sanitizeLink(url));
            }
            return url;
        }
        return StringUtils.EMPTY;
    }
}
