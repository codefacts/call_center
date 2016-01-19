package com.imslbd.call_center.template.page;

import com.imslbd.call_center.util.MyUtil;
import io.crm.QC;
import io.vertx.core.json.JsonObject;
import org.watertemplate.Template;
import org.watertemplate.TemplateMap;

/**
 * Created by someone on 29/10/2015.
 */
final public class DashboardTmpt extends Template {
    private final JsonObject user;
    private final Template sidebarTemplate;
    private final Template contentTemplate;

    DashboardTmpt(JsonObject user, Template sidebarTemplate, Template contentTemplate) {
        this.user = user;
        this.sidebarTemplate = sidebarTemplate;
        this.contentTemplate = contentTemplate;
        add(QC.username, user.getString(QC.username));
    }

    @Override
    protected void addSubTemplates(TemplateMap.SubTemplates subTemplates) {
        subTemplates.add("sidebar", sidebarTemplate);
        subTemplates.add("main_content", contentTemplate);
    }

    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/dashboard.html");
    }
}
