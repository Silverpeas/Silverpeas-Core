/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.icons;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPaneTag;

public class IconTag extends TagSupport {

  private static final long serialVersionUID = -3286118333215989238L;

  static final String TABBEDPANE_PAGE_ATT = "pageContextTabbedPane";

  private String iconName = null;
  private String altText = null;
  private String action = null;
  private String imagePath = null;

  @Override
  public int doEndTag() throws JspException {

    Tag parent = findAncestorWithClass(this, IconPaneTag.class);
    if (parent != null) {
      IconPane iconPane = (IconPane) pageContext.getAttribute(IconPaneTag.ICONPANE_PAGE_ATT);
      Icon icon = iconPane.addIcon();
      icon.setProperties(iconName, altText, action);
    } else {
      try {
        Icon icon = new IconWA();
        icon.setProperties(iconName, altText, action);
        pageContext.getOut().println(icon.print());
      } catch (IOException ex) {
         throw new JspException("Icon Tag", ex);
      }
    }
    return EVAL_PAGE;
  }

  @Override
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
   * @param iconName the iconName to set
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
   * @param altText the altText to set
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
   * @param action the action to set
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
   * @param imagePath the imagePath to set
   */
  public void setImagePath(String imagePath) {
    this.imagePath = imagePath;
  }

}
