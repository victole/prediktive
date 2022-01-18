package com.victole.examples.core.models.impl;

import com.day.cq.tagging.Tag;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.victole.examples.core.enums.ContentType;
import com.victole.examples.core.enums.UserType;
import com.victole.examples.core.models.ArticlePageModel;
import com.victole.examples.core.utils.LinkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.json.JSONException;

import javax.annotation.PostConstruct;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Model(adaptables = Resource.class, adapters = ArticlePageModel.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL, resourceType = ArticlePageModelImpl.RESOURCE_TYPE)
public class ArticlePageModelImpl extends BasePageModelImpl implements ArticlePageModel {

    @ValueMapValue(name = "jcr:content/notifyEmployees")
    @Default(values = "false")
    private Boolean notifyEmployees;

    @ValueMapValue(name = "jcr:content/notifyExternalUsers")
    @Default(values = "false")
    private Boolean notifyExternalUsers;

    @ValueMapValue(name = "jcr:content/squareThumbnailImage")
    private String smallThumbnailImagePath;

    @ValueMapValue(name = "jcr:content/wideThumbnailImage")
    private String mediumThumbnailImagePath;

    public static final String RESOURCE_TYPE = "project/components/page/articlepage";

    private static final Gson gson = new Gson();

    @Override
    @PostConstruct
    public void init() {
        super.init();

        //removed custom business logic
    }

    @Override
    public Boolean isVideo() {
        return getContentTypes().contains(ContentType.VIDEOARTICLE);
    }

    @Override
    public Boolean isPublication() {
        return getContentTypes().contains(ContentType.PUBLICATIONARTICLE);
    }

    @Override
    public List<ContentType> getContentTypes() {
        List<Tag> contentTypeTags = getContentTypeTags(getPage());
        return contentTypeTags.stream().map(ContentType::valueFromTag).collect(Collectors.toList());
    }

    @Override
    public JsonObject toJSON(Locale locale) throws JSONException {
        JsonObject item = new JsonObject();
        item.addProperty("link", LinkUtils.sanitizeLink(this.getPath()));

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String date = formatter.format(this.getLastPublished());
        item.addProperty("cardDate", date);

        item.addProperty("title", this.getTitle());
        if (StringUtils.isNotEmpty(this.getDescription())) {
            item.addProperty("description", this.getDescription());
        }
        item.addProperty("isVideo", this.isVideo());
        item.addProperty("isPublication", this.isPublication());
        item.addProperty("categoryName", this.getCategory());
        item.addProperty("path", this.getPath());
        return item;
    }

    @Override
    public UserType getUserTypeToNotify() {
        if(notifyEmployees && notifyExternalUsers) {
            return UserType.ALL;
        } else if (notifyEmployees) {
            return UserType.EMPLOYEES;
        } else if (notifyExternalUsers){
            return UserType.EXTERNAL;
        } else {
            return UserType.NONE;
        }
    }
}
