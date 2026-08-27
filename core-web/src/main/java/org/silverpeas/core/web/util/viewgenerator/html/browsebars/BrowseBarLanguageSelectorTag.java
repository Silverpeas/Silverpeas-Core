package org.silverpeas.core.web.util.viewgenerator.html.browsebars;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.tagext.TagSupport;

/**
 * Selector of a display language within a browse bar.
 *
 * @author mmoquillon
 */
public class BrowseBarLanguageSelectorTag extends TagSupport {


  private String language;
  private String link;

  /**
   * Sets the actual display language of the web page.
   * @param language the ISO 639-1 code of a language.
   */
  public void setLang(String language) {
    this.language = language;
  }

  /**
   * Sets the link of the selector function.
   * @param link the link to a selector function. Can be an URL.
   */
  public void setLink(String link) {
    this.link = link;
  }

  @Override
  public int doEndTag() throws JspException {
    BrowseBarTag browseBar = (BrowseBarTag) findAncestorWithClass(this, BrowseBarTag.class);
    browseBar.setI18N(link, language);
    return EVAL_PAGE;
  }
}
  