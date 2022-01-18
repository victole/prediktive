package com.victole.examples.core.models.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

@Configuration(label = "Site Configuration Model")
public @interface SiteConfigurationModel {

    @Property(label = "New Article Notification Content Fragment", property = {
            "widgetType=pathbrowser",
            "pathbrowserRootPath=/content/dam"
    }, order = 3)
    String articleNotificationContentFragment() default "";

    @Property(label = "Notification Project ID", order = 5)
    String notificationProjectId() default StringUtils.EMPTY;

}
