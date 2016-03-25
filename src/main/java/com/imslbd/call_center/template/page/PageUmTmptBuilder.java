package com.imslbd.call_center.template.page;

import io.crm.web.util.Script;
import org.watertemplate.Template;

import java.util.Collection;
import java.util.List;

public class PageUmTmptBuilder {
    private String pageTitle;
    private Template body;
    private Collection<Script> scripts;
    private Collection<String> styles;
    private List<String> hiddens;

    public PageUmTmptBuilder setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
        return this;
    }

    public PageUmTmptBuilder setBody(Template body) {
        this.body = body;
        return this;
    }

    public PageUmTmptBuilder setScripts(Collection<Script> scripts) {
        this.scripts = scripts;
        return this;
    }

    public PageUmTmptBuilder setStyles(Collection<String> styles) {
        this.styles = styles;
        return this;
    }

    public PageUmTmptBuilder setHiddens(List<String> hiddens) {
        this.hiddens = hiddens;
        return this;
    }

    public PageUmTmpt createPageUmTmpt() {
        return new PageUmTmpt(pageTitle, body, scripts, styles, hiddens);
    }
}