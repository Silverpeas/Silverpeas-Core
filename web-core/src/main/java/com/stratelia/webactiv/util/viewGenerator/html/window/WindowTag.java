/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.viewGenerator.html.window;

import com.stratelia.webactiv.util.viewGenerator.html.NeedWindowTag;

import javax.servlet.jsp.JspException;
import java.io.IOException;

public class WindowTag extends NeedWindowTag {

  private static final long serialVersionUID = -9075732954978662710L;

  private boolean browseBarVisible = true;

  public boolean isBrowseBarVisible() {
    return browseBarVisible;
  }

  public void setBrowseBarVisible(boolean browseBarVisible) {
    this.browseBarVisible = browseBarVisible;
  }

  @Override
  public int doEndTag() throws JspException {
    Window window = (Window) pageContext.getAttribute(WINDOW_PAGE_ATT);
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
    try {
      pageContext.getOut().println(window.printBefore());
    } catch (IOException e) {
      throw new JspException("Window Tag", e);
    }
    return EVAL_BODY_INCLUDE;
  }
}
