package com.stratelia.webactiv.util.viewGenerator.html;

import java.util.ResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.taglibs.standard.tag.common.core.Util;

public class SetBundleTag extends TagSupport {
  private int scope = PageContext.PAGE_SCOPE;
  private String var;

  private ResourceBundle bundle;

  public void setVar(String var) {
    this.var = var;
  }

  public void setScope(String scope) {
    this.scope = Util.getScope(scope);
  }

  public void setBundle(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  public int doEndTag() throws JspException {
    LocalizationContext locCtxt = new LocalizationContext(bundle);
    if (var != null) {
      pageContext.setAttribute(var, locCtxt, scope);
    } else {
      Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
    }
    return EVAL_PAGE;
  }
}
