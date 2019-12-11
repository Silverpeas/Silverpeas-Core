/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.layout;

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.ResourceBundle;

/**
 * Centralizing common data providing.
 * @author silveryocha
 */
class SilverpeasLayout extends BodyTagSupport {
  private static final long serialVersionUID = -8485442477706985045L;

  MainSessionController getMainSessionController() {
    return (MainSessionController) ((HttpServletRequest) pageContext.getRequest())
        .getSession(false).getAttribute(MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  }

  String getComponentId() {
    return ((String[]) pageContext.getRequest().getAttribute("browseContext"))[3];
  }

  ResourceBundle getBundle() {
    return ((MultiSilverpeasBundle) pageContext.getRequest().getAttribute("resources")).getMultilangBundle();
  }
}
