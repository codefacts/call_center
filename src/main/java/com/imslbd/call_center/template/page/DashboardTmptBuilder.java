package com.imslbd.call_center.template.page;

import io.vertx.core.json.JsonObject;
import org.watertemplate.Template;

import static io.crm.util.Util.getOrDefault;
import static io.crm.web.template.TemplateUtil.EMPTY_TEMPLATE;

public class DashboardTmptBuilder {
    private JsonObject user;
    private Template sidebarTemplate;
    private Template contentTemplate;

    public DashboardTmptBuilder setUser(JsonObject user) {
        this.user = user;
        return this;
    }

    public DashboardTmptBuilder setSidebarTemplate(Template sidebarTemplate) {
        this.sidebarTemplate = sidebarTemplate;
        return this;
    }

    public DashboardTmptBuilder setContentTemplate(Template contentTemplate) {
        this.contentTemplate = contentTemplate;
        return this;
    }

    public DashboardTmpt build() {
        return new DashboardTmpt(user, getOrDefault(sidebarTemplate, EMPTY_TEMPLATE), getOrDefault(contentTemplate, EMPTY_TEMPLATE));
    }
}