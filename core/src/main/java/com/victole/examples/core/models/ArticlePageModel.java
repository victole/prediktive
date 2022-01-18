package com.victole.examples.core.models;

import com.google.gson.JsonObject;
import com.victole.examples.core.enums.ContentType;
import com.victole.examples.core.enums.UserType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

/**
 * Interface for Sling Model for retrieving additional page information specific
 * for Article Pages. Extends {@link BasePageModel}.
 */
public interface ArticlePageModel extends BasePageModel {


    /**
     * Returns if article is video article
     *
     * @return if article is video article
     */
    Boolean isVideo();

    /**
     * Returns if article is publication article
     *
     * @return if article is publication article
     */
    Boolean isPublication();

    /**
     * Returns JSON from article
     *
     * @return article {@link JSONObject}
     */
    JsonObject toJSON(Locale locale) throws JSONException;

    /**
     * Returns configured UserType to notify
     *
     * @return user type {@link UserType}
     */
    UserType getUserTypeToNotify();

    /**
     * Returns list of content types
     *
     * @return list of content types
     */
    List<ContentType> getContentTypes();
}
