package com.imslbd.call_center.template.js;

import io.crm.web.template.Page;
import org.watertemplate.Template;

/**
 * Created by someone on 26/10/2015.
 */
final public class CallDetailsTemplateJS extends Template {
    public CallDetailsTemplateJS(final String callDetailsSummaryView) {
        add("callDetailsSummaryView", callDetailsSummaryView);
    }

    @Override
    protected String getFilePath() {
        return Page.templatePath("/pages/call-details.js");
    }
}
