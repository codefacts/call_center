package com.imslbd.call_center.template.page;

import com.imslbd.call_center.util.MyUtil;
import io.crm.util.Util;
import io.crm.web.util.Script;
import org.watertemplate.Template;
import org.watertemplate.TemplateMap;

import java.util.Collection;
import java.util.List;

import static io.crm.web.template.TemplateUtil.EMPTY_TEMPLATE;

/**
 * Created by someone on 29/10/2015.
 */
final public class PageTmpt extends Template {
    private final String page_title;
    private final Template body;

    PageTmpt(final String page_title, final Template body, final Collection<Script> scripts, final Collection<String> styles, final List<String> hiddens) {
        this.page_title = page_title;
        this.body = body;
        add("page_title", page_title);
        addCollection("scripts", scripts, ((script, arguments) -> {
            arguments.add("src", script.src);
            arguments.add("type", script.type.value);
        }));
        addCollection("styles", styles);
        addCollection("hiddens", hiddens);
    }

    @Override
    protected void addSubTemplates(TemplateMap.SubTemplates subTemplates) {
        subTemplates.add("body", Util.or(body, EMPTY_TEMPLATE));
    }

    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/page.html");
    }
}
