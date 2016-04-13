package com.imslbd.um.template;

import com.imslbd.call_center.MyApp;
import io.crm.util.SimpleCounter;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.watertemplate.Template;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by shahadat on 4/13/16.
 */
public class SalesReportTemplate extends Template {

    public SalesReportTemplate(List<JsonArray> list) {
        final SimpleCounter counter = new SimpleCounter(1);

        addCollection("sales", list, (jsonArray, arguments) -> {

            arguments.add("no", counter.counter++ + "");

            int ix = 0;
            arguments.add("Product", jsonArray.getString(ix++));
            arguments.add("Quantity", jsonArray.getInteger(ix++) + "");
            arguments.add("Unit", jsonArray.getString(ix++));
            arguments.add("UnitPrice", jsonArray.getInteger(ix++) + "");
            arguments.add("Total", jsonArray.getInteger(ix++) + "");
        });
    }

    @Override
    protected String getFilePath() {
        return "file:" + Paths.get(MyApp.loadConfig().getString("myTemplateDir"), "/pages/sales-report.html");
    }
}
