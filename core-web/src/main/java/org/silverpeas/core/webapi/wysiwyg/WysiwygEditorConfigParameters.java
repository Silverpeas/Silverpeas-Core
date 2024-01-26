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

package org.silverpeas.core.webapi.wysiwyg;

import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.http.RequestParameterDecoder;
import org.silverpeas.core.web.util.WysiwygEditorConfig;

import javax.xml.bind.annotation.XmlElement;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * Representation of different parameters with which the WYSIWYG editor configuration could be
 * overridden.<br>
 * To get a loaded container, use {@link RequestParameterDecoder#decode(HttpRequest, Class)}.
 * @author Yohann Chastagnier
 */
class WysiwygEditorConfigParameters {

  @XmlElement
  private String height;

  @XmlElement
  private String width;

  @XmlElement
  private String language;

  @XmlElement
  private String toolbar;

  @XmlElement
  private Boolean toolbarStartExpanded;

  @XmlElement
  private Boolean fileBrowserDisplayed;

  @XmlElement
  private String stylesheet;

  /**
   * Applies the parameters on a configuration.
   * @param config a wysiwyg editor configuration.
   * @return the given configuration.
   */
  WysiwygEditorConfig applyOn(final WysiwygEditorConfig config) {
    if (isDefined(height)) {
      config.setHeight(height);
    }
    if (isDefined(width)) {
      config.setWidth(width);
    }
    if (isDefined(language)) {
      config.setLanguage(language);
    }
    if (isDefined(toolbar)) {
      config.setToolbar(toolbar);
    }
    if (toolbarStartExpanded != null) {
      config.setToolbarStartExpanded(toolbarStartExpanded);
    }
    if (fileBrowserDisplayed != null) {
      config.setFileBrowserDisplayed(fileBrowserDisplayed);
    }
    if (isDefined(stylesheet)) {
      config.setStylesheet(stylesheet);
    }
    return config;
  }
}
