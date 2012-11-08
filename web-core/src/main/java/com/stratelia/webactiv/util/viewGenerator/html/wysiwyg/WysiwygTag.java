package com.stratelia.webactiv.util.viewGenerator.html.wysiwyg;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.silverpeas.peasCore.URLManager;

public class WysiwygTag extends TagSupport {

  private static final long serialVersionUID = -7606894410387540973L;
  
  private String replace;
  private String height = "500";
  private String width = "100%";
  private String language = "en";
  private String toolbar = "Default";
  
  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getToolbar() {
    return toolbar;
  }

  public void setToolbar(String toolbar) {
    this.toolbar = toolbar;
  }
  
  public String getReplace() {
    return replace;
  }

  public void setReplace(String replace) {
    this.replace = replace;
  }

  @Override
  public int doEndTag() throws JspException {
    Wysiwyg wysiwyg = new Wysiwyg();
    wysiwyg.setReplace(getReplace());
    wysiwyg.setWidth(getWidth());
    wysiwyg.setHeight(getHeight());
    wysiwyg.setLanguage(getLanguage());
    wysiwyg.setToolbar(getToolbar());
    wysiwyg.setServerURL(URLManager.getServerURL((HttpServletRequest) pageContext.getRequest()));
    
    try {     
      pageContext.getOut().println(wysiwyg.print());
    } catch (IOException e) {
      throw new JspException("WysiwygTag tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  
}