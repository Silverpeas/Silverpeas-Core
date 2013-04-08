/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.admin.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriInfo;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;

/**
 * Base URIs from which the REST-based ressources representing admin entities are defined.
 * @author Yohann Chastagnier
 */
public final class AdminResourceURIs {

  public static final String SPACES_BASE_URI = "spaces";
  public static final String SPACES_SPACES_URI_PART = "spaces";
  public static final String SPACES_COMPONENTS_URI_PART = "components";
  public static final String SPACES_CONTENT_URI_PART = "content";
  public static final String SPACES_APPEARANCE_URI_PART = "appearance";
  public static final String SPACES_PERSONAL_URI_PART = "personal";

  public static final String FORCE_GETTING_FAVORITE_PARAM = "forceGettingFavorite";
  public static final String GET_NOT_USED_COMPONENTS_PARAM = "getNotUsedComponents";
  public static final String GET_USED_COMPONENTS_PARAM = "getUsedComponents";
  public static final String GET_USED_TOOLS_PARAM = "getUsedTools";

  public static final String COMPONENTS_BASE_URI = "components";

  private static final char separator = '/';

  /**
   * Builds a space URI
   * @param space
   * @param uriInfo
   * @return
   */
  public static URI buildURIOfSpace(final SpaceInstLight space, final UriInfo uriInfo) {
    return buildURIOfSpace(space.getShortId(), uriInfo);
  }

  /**
   * Builds a space URI
   * @param spaceId
   * @param uriInfo
   * @return
   */
  public static URI buildURIOfSpace(final String spaceId, final UriInfo uriInfo) {
    return buildURI(uriInfo, SPACES_BASE_URI, spaceId);
  }

  /**
   * Builds a space appearance URI
   * @param space
   * @param uriInfo
   * @return
   */
  public static URI buildURIOfSpaceAppearance(final SpaceInstLight space, final UriInfo uriInfo) {
    return buildURIOfSpaceAppearance(space.getShortId(), uriInfo);
  }

  /**
   * Builds a space appearance URI
   * @param spaceId
   * @param uriInfo
   * @return
   */
  public static URI buildURIOfSpaceAppearance(final String spaceId, final UriInfo uriInfo) {
    return buildURI(uriInfo, SPACES_BASE_URI, spaceId, SPACES_APPEARANCE_URI_PART);
  }

  /**
   * Builds a component URI
   * @param component
   * @param uriInfo
   * @return
   */
  public static URI buildURIOfComponent(final ComponentInstLight component, final UriInfo uriInfo) {
    return buildURIOfComponent(
        (component.getId() == null ? "" : component.getId().replaceFirst(component.getName(), "")),
        uriInfo);
  }

  /**
   * Builds a component URI
   * @param componentId
   * @param uriInfo
   * @return
   */
  public static URI buildURIOfComponent(final String componentId, final UriInfo uriInfo) {
    return buildURI(uriInfo, COMPONENTS_BASE_URI, componentId);
  }

  /**
   * Gets the URI from a given UriInfo and URI path parts
   * @param uriInfo
   * @param uriPathParts
   * @return
   */
  protected static URI buildURI(final UriInfo uriInfo, final String... uriPathParts) {
    return buildURI(uriInfo.getBaseUri().toString(), uriPathParts);
  }

  /**
   * Gets the URI from a given URI base and URI path parts
   * @param uriBase
   * @param uriPathParts
   * @return
   */
  protected static URI buildURI(final String uriBase, final String... uriPathParts) {
    try {
      return new URI(buildStringURI(uriBase, uriPathParts));
    } catch (final URISyntaxException ex) {
      Logger.getLogger(AdminResourceURIs.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  /**
   * Gets the URI from a given URI base and URI path parts
   * @param uriBase
   * @param uriPathParts
   * @return
   */
  private static String buildStringURI(final String uriBase, final String... uriPathParts) {

    if (!StringUtil.isDefined(uriBase)) {
      return "";
    }

    final StringBuilder stringURI = new StringBuilder(uriBase);
    if (uriPathParts != null) {
      for (final String pathPart : uriPathParts) {
        if (stringURI.charAt(stringURI.length() - 1) != separator) {
          stringURI.append(separator);
        }
        stringURI.append(pathPart);
      }
    }
    return stringURI.toString();
  }
}
