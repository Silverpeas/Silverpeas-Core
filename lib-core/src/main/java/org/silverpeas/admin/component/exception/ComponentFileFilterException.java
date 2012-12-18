/*
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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.admin.component.exception;

import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.admin.component.parameter.ComponentFileFilterParameter;

import java.io.File;

import static com.silverpeas.util.StringUtil.isDefined;

/**
 * @author Yohann Chastagnier
 */
public class ComponentFileFilterException extends RuntimeException {

  private final ComponentFileFilterParameter fromComponentFileFilter;
  private final File forbiddenFile;
  private String language;

  /**
   * Default constructor
   * @param fromComponentFileFilter
   * @param forbiddenFile
   */
  public ComponentFileFilterException(final ComponentFileFilterParameter fromComponentFileFilter,
      final File forbiddenFile) {
    this.fromComponentFileFilter = fromComponentFileFilter;
    this.forbiddenFile = forbiddenFile;
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
    return forbiddenFile != null ? forbiddenFile.getName() : "";
  }

  /**
   * @return the fromComponentURL
   */
  @SuppressWarnings("UnusedDeclaration")
  public String getFromComponentUrl() {
    return (isDefined(getComponentFileFilterParameter().getComponent().getId())) ?
        URLManager.getApplicationURL() +
            URLManager.getURL(getComponentFileFilterParameter().getComponent().getName(), null,
                getComponentFileFilterParameter().getComponent().getId()) + "Main" : "";
  }
}
