/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.webapi.look;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.UriInfo;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * Base URIs from which the REST-based ressources representing look entities are defined.
 * @author Yohann Chastagnier
 */
public final class LookResourceURIs {

  public static final String DISPLAY_BASE_URI = "display";
  public static final String DISPLAY_USER_CONTEXT_URI_PART = "userContext";

  private static final char separator = '/';

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
      SilverLogger.getLogger(LookResourceURIs.class).error(ex.getMessage(), ex);
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
