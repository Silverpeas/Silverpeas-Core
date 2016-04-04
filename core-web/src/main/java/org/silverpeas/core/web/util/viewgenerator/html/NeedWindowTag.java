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

package org.silverpeas.core.web.util.viewgenerator.html;

import org.silverpeas.core.web.util.viewgenerator.html.window.Window;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

public class NeedWindowTag extends TagSupport {

  private static final long serialVersionUID = 1610658738987330527L;
  public static final String WINDOW_PAGE_ATT = "pageContextWindow";

  protected Window getWindow() {
    Window window = (Window) pageContext
        .getAttribute(WINDOW_PAGE_ATT, PageContext.REQUEST_SCOPE);
    if (window == null) {
      GraphicElementFactory gef = (GraphicElementFactory) pageContext
          .getSession().getAttribute(
          GraphicElementFactory.GE_FACTORY_SESSION_ATT);
      window = gef.getWindow();
      pageContext.setAttribute(WINDOW_PAGE_ATT, window, PageContext.REQUEST_SCOPE);
    }
    return window;
  }
}
