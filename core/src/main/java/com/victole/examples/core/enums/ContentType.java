package com.victole.examples.core.enums;

import com.day.cq.tagging.Tag;

import static com.victole.examples.core.utils.Constants.PROJECT_TAG_ID;

public enum ContentType {

    VIDEOARTICLE(PROJECT_TAG_ID +"content-types/video", "videoarticle"),
    TEXTARTICLE(PROJECT_TAG_ID + "content-types/text", "textarticle"),
    PUBLICATIONARTICLE(PROJECT_TAG_ID + "content-types/publications", "publications");


    private String tagNamespace;

    private String name;

    ContentType(String tagNamespace, String name) {
        this.tagNamespace = tagNamespace;
        this.name = name;
    }

    public String getTagNamespace() {
        return tagNamespace;
    }

    public String getName() {
        return name;
    }

    public static ContentType valueFromTag(Tag tag) {
        if (tag != null) {
            for (ContentType contentType : ContentType.values()) {
                if (tag.getTagID().equals(contentType.getTagNamespace())) {
                    return contentType;
                }
            }
        }
        return TEXTARTICLE;
    }
}
