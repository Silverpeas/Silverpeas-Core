/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.Optional;
import java.util.ResourceBundle;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.util.ResourceLocator.getGeneralLocalizationBundle;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.web.mvc.controller.MainSessionController.MAIN_SESSION_CONTROLLER_ATT;

/**
 * Centralizing common data providing.
 * @author silveryocha
 */
abstract class SilverpeasLayout extends BodyTagSupport {
  private static final long serialVersionUID = -8485442477706985045L;

  public SilverpeasLayout() {
    this.init();
  }

  private Optional<MainSessionController> getMainSessionController() {
    final HttpSession session = ((HttpServletRequest) pageContext.getRequest()).getSession(false);
    return session != null
        ? ofNullable((MainSessionController) session.getAttribute(MAIN_SESSION_CONTROLLER_ATT))
        : empty();
  }

  String getUserLanguage() {
    return getMainSessionController()
        .map(MainSessionController::getFavoriteLanguage)
        .orElseGet(() -> {
          String userLanguage = (String) pageContext.getRequest().getAttribute("language");
          if (isNotDefined(userLanguage)) {
            userLanguage = (String) pageContext.getRequest().getAttribute("userLanguage");
          }
          if (isNotDefined(userLanguage) && pageContext.getRequest().getLocale() != null) {
            userLanguage = pageContext.getRequest().getLocale().getLanguage();
          }
          return DisplayI18NHelper.verifyLanguage(userLanguage);
        });
  }

  String getComponentId() {
    final String[] context = (String[]) pageContext.getRequest().getAttribute("browseContext");
    return context != null ? context[3] : null;
  }

  ResourceBundle getBundle() {
    final MultiSilverpeasBundle resources = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute("resources");
    return resources != null
        ? resources.getMultilangBundle()
        : getGeneralLocalizationBundle(getUserLanguage());
  }

  @Override
  public void release() {
    super.release();
    this.init();
  }

  abstract void init();
}
