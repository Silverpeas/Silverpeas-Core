package com.stratelia.webactiv.util.viewGenerator.html.icons;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane;
import com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPaneTag;

public class IconTag extends TagSupport {

  static final String TABBEDPANE_PAGE_ATT = "pageContextTabbedPane";

  private String iconName = null;
  private String altText = null;
  private String action = null;
  private String imagePath = null;

  public int doEndTag() throws JspException {

    Tag parent = findAncestorWithClass(this, IconPaneTag.class);
    if (parent != null) {
      IconPane iconPane = (IconPane) pageContext
          .getAttribute(IconPaneTag.ICONPANE_PAGE_ATT);
      Icon icon = iconPane.addIcon();
      icon.setProperties(iconName, altText, action);
    }
    return EVAL_PAGE;
  }

  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  /**
   * @return the iconName
   */
  public String getIconName() {
    return iconName;
  }

  /**
   * @param iconName
   *          the iconName to set
   */
  public void setIconName(String iconName) {
    this.iconName = iconName;
  }

  /**
   * @return the altText
   */
  public String getAltText() {
    return altText;
  }

  /**
   * @param altText
   *          the altText to set
   */
  public void setAltText(String altText) {
    this.altText = altText;
  }

  /**
   * @return the action
   */
  public String getAction() {
    return action;
  }

  /**
   * @param action
   *          the action to set
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * @return the imagePath
   */
  public String getImagePath() {
    return imagePath;
  }

  /**
   * @param imagePath
   *          the imagePath to set
   */
  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

}
