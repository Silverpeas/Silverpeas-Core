/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.web.util.viewgenerator.html.navigationlist;

import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

public class NavigationListTag extends TagSupport {

  private static final long serialVersionUID = 643450785387779507L;

  private String title;
  private Integer nbCol;

  private NavigationList navigationList;

  public void setTitle(final String title) {
    this.title = title;
  }

  public void setNbCol(final Integer nbCol) {
    this.nbCol = nbCol;
  }

  public void addItem(Item item) {
    navigationList.addItem(item.getLabel(), item.getURL(), item.getNbelem(), item.getInfo(),
        item.getUniversalLink());
  }

  @Override
  public int doStartTag() throws JspException {
    GraphicElementFactory gef = (GraphicElementFactory) pageContext.getSession()
        .getAttribute(GraphicElementFactory.GE_FACTORY_SESSION_ATT);
    navigationList = gef.getNavigationList();
    navigationList.setTitle(title);
    if (nbCol != null) {
      navigationList.setNbcol(nbCol);
    }
    return EVAL_BODY_INCLUDE;
  }

  @Override
  public int doEndTag() throws JspException {
    try {
      pageContext.getOut().println(navigationList.print());
    } catch (IOException e) {
      throw new JspException("NavigationListTag Tag", e);
    }
    return EVAL_PAGE;
  }

}
