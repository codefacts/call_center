package com.imslbd.call_center.template.page;

import com.imslbd.call_center.util.MyUtil;
import org.watertemplate.Template;

/**
 * Created by someone on 25/11/2015.
 */
public class SiteTmpt extends Template {

    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/site.html");
    }
}
