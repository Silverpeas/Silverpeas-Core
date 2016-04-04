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

package org.silverpeas.core.web.util.viewgenerator.html.security;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.ResourceLocator;

import javax.servlet.ServletException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 *Tag that checks the validity of the current session and forwards to the error page.
 */
public class SessionTimeoutTag extends TagSupport {
  private static final long serialVersionUID = -8792580298207815280L;
  @Override
  public int doEndTag() throws JspException {
    MainSessionController mainSessionController = (MainSessionController) pageContext.getSession()
        .getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);

    if (mainSessionController == null) {
      // No session controller in the request -> security exception
      String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString("sessionTimeout");
      try {
        pageContext.forward(sessionTimeout);
      } catch (IOException ioex) {
        throw new JspException(ioex);
      } catch (ServletException sex) {
        throw new JspException(sex);
      }
      return SKIP_PAGE;
    }
    return EVAL_PAGE;
  }

}
