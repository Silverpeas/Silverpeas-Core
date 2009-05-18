package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

import javax.servlet.jsp.JspException;

import com.stratelia.webactiv.util.viewGenerator.html.NeedWindowTag;
import com.stratelia.webactiv.util.viewGenerator.html.window.Window;

public class BrowseBarTag extends NeedWindowTag {
  private String link;

  private String path;

  private String extraInformations;

  public void setLink(String link) {
    this.link = link;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setExtraInformations(String extraInformations) {
    this.extraInformations = extraInformations;
  }

  public int doEndTag() throws JspException {
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    Window window = getWindow();
    BrowseBar browseBar = window.getBrowseBar();
    String[] browseContext = (String[]) pageContext.getRequest().getAttribute(
        "browseContext");
    String spaceLabel = browseContext[0];
    String componentLabel = browseContext[1];
    browseBar.setDomainName(spaceLabel);
    if (path != null) {
      browseBar.setPath(path);
    }
    if (extraInformations != null) {
      browseBar.setExtraInformation(extraInformations);
    }

    browseBar.setComponentName(componentLabel, link);
    return EVAL_BODY_INCLUDE;
  }
}
