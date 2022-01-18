package com.victole.examples.core.models;


import java.util.Date;

import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.Page;

/**
 * Interface for Sling Model for retrieving current page basic information. Extended by {@link ArticlePageModel}.
 */
public interface BasePageModel {

    /**
     * Return page as {@link Page} type
     * @return page as {@link Page} type
     */
    Page getPage();

    /**
     * Return page path
     * @return page path
     */
    String getPath();

    /*
     * Returns page description
     * @return page description
     */
    String getDescription();

    /*
     * Returns page resource type
     * @return page resource type
     */
    String getResourceType();

    /*
     * Returns page title
     * @return page title
     */
    String getTitle();

    /*
     * Returns page subtitle
     * @return page subtitle
     */
    String getSubtitle();

    /*
     * Returns name of page creator, from AEM user
     * @return name of page creator
     */
    String getCreatedBy();

    /*
     * Returns page author name, from content fragment
     * @return page author name
     */
    String getAuthorName();

    /*
     * Returns last modification date
     * @return last modification date
     */
    Date getLastModified();

    /**
     * last publication date, retrieved from jcr:created date on publish.
     * @return last publication date
     */
    Date getLastPublished();

    /**
     * Returns last replication action: publish, unpublish
     * @return last replication action
     */
    String getLastReplicationAction();

    /**
     * Returns date page was created
     * @return page creation date
     */
    Date getCreatedAt();

    /**
     * Returns array of {@link Tag} applied to page
     * @return array of {@link Tag}
     */
    Tag[] getTags();

    /**
     * Returns page's public URL for publish environments. Uses Resource Resolver for mapping.
     * @return page's public URL
     */
    String getPublicUrl();

    /**
     * Returns pages navigation title
     * @return pages navigation title
     */
    String getNavigationTitle();

    /**
     * Returns Article Category tag set in the page
     * @return String with category information
     */
    String getCategory();

    /**
     * Returns path of content fragment selected as for author information.
     * @return path of author content fragment
     */
    String getAuthorFragmentPath();
}
