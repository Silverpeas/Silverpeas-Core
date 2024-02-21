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
package org.silverpeas.core.admin.component.exception;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.admin.component.model.ComponentFileFilterParameter;

import static org.silverpeas.kernel.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class ComponentFileFilterException extends RuntimeException {

  private final ComponentFileFilterParameter fromComponentFileFilter;
  private final String forbiddenFileName;
  private String language;

  /**
   * Default constructor
   * @param fromComponentFileFilter
   * @param forbiddenFileName
   */
  public ComponentFileFilterException(final ComponentFileFilterParameter fromComponentFileFilter,
      final String forbiddenFileName) {
    this.fromComponentFileFilter = fromComponentFileFilter;
    this.forbiddenFileName = forbiddenFileName;
  }

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * @param language the language to set
   */
  public void setLanguage(final String language) {
    this.language = language;
  }

  /**
   * @return the fromComponent
   */
  public ComponentFileFilterParameter getComponentFileFilterParameter() {
    return fromComponentFileFilter;
  }

  /**
   * @return thr forbidden file name
   */
  public String getForbiddenFileName() {
    return forbiddenFileName;
  }

  /**
   * @return the fromComponentURL
   */
  @SuppressWarnings("UnusedDeclaration")
  public String getFromComponentUrl() {
    return (isDefined(getComponentFileFilterParameter().getComponent().getId())) ?
        URLUtil.getApplicationURL() +
            URLUtil.getURL(getComponentFileFilterParameter().getComponent().getName(), null,
                getComponentFileFilterParameter().getComponent().getId()) + "Main" : "";
  }
}
