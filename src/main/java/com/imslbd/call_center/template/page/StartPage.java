package com.imslbd.call_center.template.page;

import com.imslbd.call_center.util.MyUtil;
import io.vertx.core.json.JsonObject;
import org.watertemplate.Template;

import java.util.List;

/**
 * Created by shahadat on 1/17/16.
 */
public class StartPage extends Template {
    public StartPage(List<JsonObject> agents) {
        addCollection("agents", agents, (entries, arguments) -> {
            arguments.add("id", entries.getLong("id").toString());
            arguments.add("name", entries.getString("name"));
        });
    }

    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/start-page.html");
    }
}
