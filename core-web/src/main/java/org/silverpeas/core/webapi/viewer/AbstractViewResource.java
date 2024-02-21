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
package org.silverpeas.core.webapi.viewer;

import org.silverpeas.core.web.rs.RESTWebService;

import static org.silverpeas.kernel.util.StringUtil.definedString;

/**
 * This abstraction centralizes the commons stuffs between the WEB services of view and preview
 * of documents.
 * <p>
 * In particular, it provides a method for checking user authorizations to access a target resource.
 * </p>
 * @author silveryocha
 */
public abstract class AbstractViewResource extends RESTWebService {

  /**
   * @see ResourceViewProvider#getAuthorizedResourceView(String, String, String)
   */
  protected ResourceView getAuthorizedResourceView(final String id, final String type, final String language) {
    // Content Language
    final String contentLanguage = definedString(language).orElseGet(() -> getUserPreferences().getLanguage());
    // Retrieve resource view data
    return ResourceViewProvider.getAuthorizedResourceView(id, type, contentLanguage);
  }

  @Override
  public String getComponentId() {
    return null;
  }
}
