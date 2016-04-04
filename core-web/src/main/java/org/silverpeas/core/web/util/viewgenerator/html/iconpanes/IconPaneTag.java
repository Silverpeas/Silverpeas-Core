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

package org.silverpeas.core.web.util.viewgenerator.html.iconpanes;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

public class IconPaneTag extends TagSupport {

  static public final String ICONPANE_PAGE_ATT = "pageContextIconPane";
  private static final long serialVersionUID = -942309015545358248L;

  private String orientation = "horizontal";
  private String spacing = null;

  /**
   * @return the orientation
   */
  public String getOrientation() {
    return orientation;
  }

  /**
   * @param orientation the orientation to set
   */
  public void setOrientation(String orientation) {
    this.orientation = orientation;
  }

  /**
   * @return the spacing
   */
  public String getSpacing() {
    return spacing;
  }

  /**
   * @param spacing the spacing to set
   */
  public void setSpacing(String spacing) {
    this.spacing = spacing;
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      IconPane icons = (IconPane) pageContext.getAttribute(ICONPANE_PAGE_ATT);
      pageContext.getOut().println(icons.print());
      pageContext.removeAttribute(ICONPANE_PAGE_ATT);
    } catch (IOException e) {
      throw new JspException("TabbedPane Tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    IconPane icons = gef.getIconPane();
    pageContext.setAttribute(ICONPANE_PAGE_ATT, icons);
    return EVAL_BODY_INCLUDE;
  }

}
