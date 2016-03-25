package com.imslbd.call_center.template.page;

import com.imslbd.call_center.util.MyUtil;
import org.watertemplate.Template;

/**
 * Created by shahadat on 3/6/16.
 */
public class SiteUmTmpt extends Template {
    @Override
    protected String getFilePath() {
        return MyUtil.templatePath("/pages/site-um.html");
    }
}
