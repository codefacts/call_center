package com.imslbd.call_center.template.js;

import io.crm.web.template.Page;
import org.watertemplate.Template;

/**
 * Created by someone on 26/10/2015.
 */
public class CallDetailsSummaryViewJS extends Template {
    @Override
    protected String getFilePath() {
        return Page.templatePath("/pages/call-details-summary-view.js");
    }
}
