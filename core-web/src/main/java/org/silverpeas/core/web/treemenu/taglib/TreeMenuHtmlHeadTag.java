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

package org.silverpeas.core.web.treemenu.taglib;

import org.silverpeas.core.admin.component.model.WAComponent;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Collection;

import static org.silverpeas.core.web.treemenu.model.MenuConstants.ICON_STYLE_PREFIX;

/**
 * @author David Derigent
 */
public class TreeMenuHtmlHeadTag extends TagSupport {

  /**
   * stores the style to display the icons components
   */
  private static String iconStyle = null;
  /**
   * Indicates if the css File must be display
   */
  private boolean displayCssFile = false;
  /**
   * Indicates if the javascript File must be display
   */
  private boolean displayJavascriptFile = false;
  /**
   * Indicates if the icons components must be display
   */
  private boolean displayIconsStyles = false;
  /**
   * context name application
   */
  private String contextName = null;
  /**
   *
   */
  private static final long serialVersionUID = 2678199573554487425L;

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
   */
  @Override
  public int doEndTag() throws JspException {
    try {
      StringBuilder builder = new StringBuilder();

      // displays the css file used by menu tree
      if (displayCssFile) {
        // stylesheets for YUI treeView menu

        builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(
            contextName).append(
            "/util/styleSheets/treeMenu/fonts-min.css\">");
        builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(
            contextName).append(
            "/util/styleSheets/treeMenu/treeview.css\">");
        builder.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"").append(
            contextName).append(
            "/util/styleSheets/treeMenu/tree.css\">");

      }
      // // displays the Javascript file used by menu tree
      if (displayJavascriptFile) {
        builder.append("<script type=\"text/javascript\" src=\"").append(
            contextName).append(
            "/util/yui/yahoo-dom-event/yahoo-dom-event.js\"></script>");

        builder.append("<script type=\"text/javascript\" src=\"").append(
            contextName).append(
            "/util/yui/json/json-min.js\"></script>");

        builder.append("<script type=\"text/javascript\" src=\"").append(
            contextName).append(
            "/util/yui/connection/connection-min.js\"></script>");

        builder.append("<script type=\"text/javascript\" src=\"").append(
            contextName).append(
            "/util/yui/treeview/treeview-min.js\"></script>");

        builder.append("<script type=\"text/javascript\" src=\"").append(
            contextName).append(
            "/util/javaScript/treeMenu/menu.js\"></script>");

        builder.append("<script type=\"text/javascript\" src=\"").append(
            contextName).append(
            "/util/javaScript/jquery/jquery-1.5.min.js\"></script>");
      }
      // displays the styles used to represent the icon component
      if (displayIconsStyles) {
        if (iconStyle == null) {
          StringBuilder iconStyleBuilder = new StringBuilder();
          OrganizationController controller =
              OrganizationControllerProvider.getOrganisationController();
          Collection<WAComponent> components = WAComponent.getAll();
          if (!components.isEmpty()) {
            iconStyleBuilder.append("<style type=\"text/css\">");
          }
          for (WAComponent component : components) {
            String name = component.getName();
            iconStyleBuilder.append(".").append(ICON_STYLE_PREFIX).append(name).append(
                "{ display:block; height: 15px; padding-left: 20px; background: transparent url(").
                append(contextName).append(
                "/util/icons/component/").append(name).append("Small.gif) 0 0px no-repeat; } ");
          }
          if (iconStyleBuilder.length() > 1) {
            iconStyleBuilder.append("</style>");
            iconStyle = iconStyleBuilder.toString();
          }
        }

        builder.append(iconStyle);
      }

      pageContext.getOut().println(builder.toString());
    } catch (IOException e) {
      throw new JspException("TreeMenuHtmlHeadTag Tag", e);
    }
    return EVAL_PAGE;
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    return EVAL_BODY_INCLUDE;
  }

  /**
   * @return the displayCssFile
   */
  public boolean isDisplayCssFile() {
    return displayCssFile;
  }

  /**
   * @param displayCssFile the displayCssFile to set
   */
  public void setDisplayCssFile(boolean displayCssFile) {
    this.displayCssFile = displayCssFile;
  }

  /**
   * @return the displayJavascriptFile
   */
  public boolean isDisplayJavascriptFile() {
    return displayJavascriptFile;
  }

  /**
   * @param displayJavascriptFile the displayJavascriptFile to set
   */
  public void setDisplayJavascriptFile(boolean displayJavascriptFile) {
    this.displayJavascriptFile = displayJavascriptFile;
  }

  /**
   * @return the displayIconsStyles
   */
  public boolean isDisplayIconsStyles() {
    return displayIconsStyles;
  }

  /**
   * @param displayIconStyle the displayIconsStyles to set
   */
  public void setDisplayIconsStyles(boolean displayIconStyle) {
    this.displayIconsStyles = displayIconStyle;
  }

  /**
   * @return the contextName
   */
  public String getContextName() {
    return contextName;
  }

  /**
   * @param contextName the contextName to set
   */
  public void setContextName(String contextName) {
    this.contextName = contextName;
  }
}
