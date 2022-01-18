package com.victole.examples.core.models.impl;

import com.adobe.acs.commons.models.injectors.annotation.AemObject;
import com.adobe.acs.commons.wcm.PageRootProvider;
import com.day.cq.dam.api.s7dam.utils.PublishUtils;
import com.day.cq.tagging.Tag;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.victole.examples.core.models.BasePageModel;
import com.victole.examples.core.utils.Constants;
import com.victole.examples.core.utils.LinkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.apache.sling.models.factory.ModelFactory;
import org.apache.sling.settings.SlingSettingsService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Model(
        adaptables = Resource.class,
        adapters = BasePageModel.class,
        defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class BasePageModelImpl implements BasePageModel {

    public static final String ARTICLES_FOLDER_PATH = "/articles/";

    @ValueMapValue(name = "jcr:content/subtitle")
    private String subtitle;

    @ValueMapValue(name = "jcr:content/author")
    private String author;

    @ValueMapValue(name = "jcr:content/authorFragmentPath")
    private String authorFragmentPath;

    @ValueMapValue(name = NameConstants.PN_CREATED_BY)
    private String createdBy;

    @ValueMapValue(name = NameConstants.PN_CREATED)
    private Calendar createdAt;

    @ValueMapValue(name = "jcr:content/" + NameConstants.PN_CREATED)
    private Calendar lastPublished;

    @ValueMapValue(name = "jcr:content/" + NameConstants.PN_PAGE_LAST_REPLICATION_ACTION)
    private String lastReplicationAction;

    @AemObject
    protected Resource resource;

    @AemObject
    private ResourceResolver resourceResolver;

    @AemObject
    private Page page;

    @OSGiService
    private ModelFactory modelFactory;

    @OSGiService
    private PageRootProvider pageRootProvider;

    @OSGiService
    SlingSettingsService slingSettingsService;

    @OSGiService
    PublishUtils publishUtils;
    @PostConstruct
    public void init() {
        if (resource == null) {
            return;
        }

        page = resource.adaptTo(Page.class);

        String categoryRootPath = getCategoryRootPath();

        //business logic implementation

    }

    @Override public Page getPage() {
        return page;
    }

    @Override public String getPath() {
        return page.getPath();
    }

    @Override public String getDescription() {
        return page.getDescription();
    }

    @Override public String getResourceType() {
        return page.getContentResource().getResourceType();
    }

    @Override public String getTitle() {
        return page.getTitle();
    }

    @Override
    public String getSubtitle() {
        return subtitle;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }

    @Override
    public String getAuthorName() {
        return author;
    }

    @Override public Date getLastModified() {
        if (page.getLastModified() == null) {
            return null;
        }

        return page.getLastModified().getTime();
    }

    @Override public Date getLastPublished() {
        if (lastPublished == null) {
            return null;
        }
        return lastPublished.getTime();
    }

    @Override
    public String getLastReplicationAction() {
        return lastReplicationAction;
    }

    @Override
    public Date getCreatedAt() {
        if (createdAt == null) {
            return null;
        }
        return createdAt.getTime();
    }

    @Override public Tag[] getTags() {
        return page.getTags();
    }

    @Override
    public String getPublicUrl() {
        return resourceResolver.map(LinkUtils.sanitizeLink(page.getPath()));
    }

    @Override public String getNavigationTitle() {
        return page.getNavigationTitle();
    }

    @Override public String getCategory() {
        return "";
    }

    public static List<Tag> getContentTypeTags(Page page) {
        List<Tag> tags =  new ArrayList<>();
        for (Tag tag: page.getTags()) {
            if (tag.getTagID().startsWith(Constants.CONTENT_TYPES_TAG_ID)) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private String getCategoryRootPath() {
        String categoryRootPath = StringUtils.EMPTY;
        String homepagePath = pageRootProvider.getRootPagePath(getPath());
        if (StringUtils.isEmpty(homepagePath)) {
            return categoryRootPath;
        }
        String relativePath;
        if (getPath().contains(ARTICLES_FOLDER_PATH)) {
            relativePath = getPath().replace(homepagePath + ARTICLES_FOLDER_PATH, "");
        } else {
            relativePath = getPath().replace(homepagePath + "/", "");
        }
        if (StringUtils.isNotEmpty(relativePath)) {
            int index = relativePath.indexOf("/");
            if (index > 0) {
                if (getPath().contains(ARTICLES_FOLDER_PATH)) {
                    categoryRootPath = getPath().substring(0, homepagePath.length() + ARTICLES_FOLDER_PATH.length() + index + 1);
                } else {
                    categoryRootPath = getPath().substring(0, homepagePath.length() + index + 1);
                }
            } else if (index == -1) {
                categoryRootPath = homepagePath.concat("/").concat(relativePath);
            }
        }
        return resourceResolver.map(LinkUtils.sanitizeLink(categoryRootPath));
    }

    @Override
    public String getAuthorFragmentPath() {
        return authorFragmentPath;
    }

}
