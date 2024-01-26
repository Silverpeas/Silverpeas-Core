/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.web.util.viewgenerator.html.layout;

import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.mvc.controller.SilverpeasWebUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.util.ResourceBundle;

import static org.silverpeas.kernel.bundle.ResourceLocator.getGeneralLocalizationBundle;

/**
 * Centralizing common data providing.
 * @author silveryocha
 */
abstract class SilverpeasLayout extends BodyTagSupport {
  private static final long serialVersionUID = -8485442477706985045L;

  public SilverpeasLayout() {
    this.init();
  }

  String getUserLanguage() {
    return SilverpeasWebUtil.get().getUserLanguage((HttpServletRequest) pageContext.getRequest());
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
