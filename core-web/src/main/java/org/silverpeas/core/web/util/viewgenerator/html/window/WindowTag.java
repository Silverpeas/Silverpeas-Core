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

package org.silverpeas.core.web.util.viewgenerator.html.window;

import org.silverpeas.core.web.util.viewgenerator.html.NeedWindowTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

public class WindowTag extends NeedWindowTag {

  private static final long serialVersionUID = -9075732954978662710L;

  private boolean browseBarVisible = true;
  private boolean popup = false;

  public boolean isBrowseBarVisible() {
    return browseBarVisible;
  }

  public void setBrowseBarVisible(boolean browseBarVisible) {
    this.browseBarVisible = browseBarVisible;
  }

  public void setPopup(boolean popup) {
    this.popup = popup;
  }

  public boolean isPopup() {
    return popup;
  }

  @Override
  public int doEndTag() throws JspException {
    Window window = (Window) pageContext.getAttribute(WINDOW_PAGE_ATT, PageContext.REQUEST_SCOPE);
    window.setPopup(isPopup());
    try {
      pageContext.getOut().println(window.printAfter());
    } catch (IOException e) {
      throw new JspException("Window Tag", e);
    }
    return EVAL_PAGE;
  }

  @Override
  public int doStartTag() throws JspException {
    Window window = getWindow();
    window.setBrowseBarVisibility(isBrowseBarVisible());
    window.setPopup(isPopup());
    try {
      pageContext.getOut().println(window.printBefore());
    } catch (IOException e) {
      throw new JspException("Window Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }
}
