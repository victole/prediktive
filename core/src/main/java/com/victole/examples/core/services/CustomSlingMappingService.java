package com.victole.examples.core.services;

import javax.servlet.http.HttpServletRequest;

public interface CustomSlingMappingService {

    /**
     * calls mapStudyPage(HttpServletRequest, String) as mapStudyPage(null, resourcePath)
     *
     * @param resourcePath
     * @return
     */
    String map(String resourcePath);


    /**
     * full implementation - apply sling:alias from the resource path - apply
     * /etc/mapStudyPage mappings (inkl. config backwards compat) - return absolute uri
     * if possible
     *
     * @param request
     * @param resourcePath
     * @return
     */
    String map(HttpServletRequest request,
            String resourcePath);
}

