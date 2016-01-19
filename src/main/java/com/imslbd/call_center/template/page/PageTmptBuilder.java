package com.imslbd.call_center.template.page;

import io.crm.web.util.Script;
import org.watertemplate.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageTmptBuilder {
    private String page_title = "";
    private Template body;
    private final Map<String, Script> scripts = new HashMap<>();

    private final Map<String, String> styles = new HashMap<>();

    private final List<String> hiddens = new ArrayList<>();

    public PageTmptBuilder(String page_title) {
        this.page_title = page_title;
    }

    public static PageTmptBuilder create(String page_title) {
        final PageTmptBuilder pageBuilder = new PageTmptBuilder(page_title);
        return pageBuilder;
    }

    public PageTmptBuilder body(Template body) {
        this.body = body;
        return this;
    }

    public PageTmptBuilder addStyle(final String name, final String href) {
        styles.put(name, href);
        return this;
    }

    public PageTmptBuilder addBabelScript(final String name, final String src) {
        scripts.put(name, new Script(name, src, Script.Type.Babel));
        return this;
    }

    public PageTmptBuilder addScript(final String name, final String src) {
        scripts.put(name, new Script(name, src, Script.Type.JavaScript));
        return this;
    }

    public PageTmptBuilder addHidden(String hidden) {
        hiddens.add(hidden);
        return this;
    }

    public PageTmpt build() {
        return new PageTmpt(page_title, body, scripts.values(), styles.values(), hiddens);
    }
}