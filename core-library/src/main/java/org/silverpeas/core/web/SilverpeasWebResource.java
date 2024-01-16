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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
package org.silverpeas.core.web;

import org.silverpeas.core.util.URLUtil;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.UriBuilder;

/**
 * The representation of a resource managed by Silverpeas in the Web. A resource accessible by the
 * Web provides an access to the instances of the resource and defines a set of operations that
 * be performed on those instances. A resource is always identified by an URI by which it can be
 * accessed in the Web; all instances of such a resource are then identified from this URI that is
 * used as the base one for the resource instances and operations.
 * <p>
 * A Web resource represents an entity in Silverpeas that is accessible through the Web. This
 * entity can a be contribution, a contribution's content, or a any business entities used in
 * Silverpeas (like the user profiles for example).
 * </p>
 * <p>
 * Named as <code>Target</code> in the JAX-RS jargon, as being the endpoint of an URI-based HTTP
 * communication, the Web resource acts in fact, for the Web clients, as a proxy of the entities it
 * is intended to represent and, as such it is uniquely identified by a base URI at which the
 * entities are meant to be exposed on the Web. As a proxy, it plays the role of a translator, as it
 * translates the expected HTTP requests in business methods implying the entity(ies) targeted by
 * the requested URI, and then it translates the answer of those methods in an HTTP response that
 * is sent back to the requester; usually, the response carries a representation of the entity(ies)
 * implying in the treatment according to the negotiated format (specified in MIME).
 * </p>
 * <p>
 * So, an entity that is accessible on the Web is then structured in the following way:
 * </p>
 * <ul>
 * <li>The entity itself defining the business operations through which the applications in
 * Silverpeas manage it. This entity has no knowledge of the Web and of how to be interfaced
 * with.</li>
 * <li>The Web resource, proxying the entity for the Web and translating the HTTP-verbs in
 * business operations implying the entity(ies) targeted by the exact requested URI.
 * </li>
 * <li>The entity Web state (aka entity state representation for the Web), ready to be encoded
 * into the negotiated representation format (usually in JSON) and that represents the state of
 * the entity at the time the HTTP request is answered.</li>
 * </ul>
 * @author Yohann Chastagnier
 * @author Miguel Moquillon
 */
public interface SilverpeasWebResource {

  String BASE_PATH = "/services";

  /**
   * Gets the base path of all the web resources relative to the root path of the Silverpeas URL by
   * taking into account the settings on the application URL. All the web
   * resources are defined from this base URI.
   * @return the base path from which all the web resources are defined as a string.
   */
  static String getBasePath() {
    return getBasePathBuilder().build().toString();
  }

  /**
   * Gets the absolute base path of all the web resources relative to the root path of the
   * Silverpeas URL by taking into account the settings on the application URL. All the web
   * resources are defined from this absolute base URI.
   * @return the absolute base path from which all the web resources are defined as a string.
   */
  static String getAbsoluteBasePath() {
    return UriBuilder.fromUri(URLUtil.getAbsoluteApplicationURL())
        .path(BASE_PATH)
        .build()
        .toString();
  }

  /**
   * Gets the base path of all the web resources relative to the root path of the Silverpeas URL
   * in the form of a {@link UriBuilder} . All the web resources are defined from this base URI.
   * @return the {@link UriBuilder} initialized with the base path from which all the web resources
   * are defined.
   */
  static UriBuilder getBasePathBuilder() {
    return UriBuilder.fromPath(URLUtil.getApplicationURL()).path(BASE_PATH);
  }

  /**
   * Gets the base URI of all the web resources. All the web resources are defined
   * from this base URI. The URI includes everything preceding the path (host, port, etc).
   * The URI in its full form is dedicated to be used to expose the resource out
   * of the Web context of Silverpeas.
   * @param request the HTTP servlet request that is received from the Web client and from which
   * the base URI in its full form will be computed.
   * @return the full base URI as an {@link UriBuilder}.
   */
  static UriBuilder getBaseUriBuilder(HttpServletRequest request) {
    return UriBuilder.fromUri(URLUtil.getFullApplicationURL(request)).path(BASE_PATH);
  }

  /**
   * Gets the current HTTP request received by this Web resource.
   * @return the current HTTP request.
   */
  HttpServletRequest getHttpRequest();

  /**
   * Gets the URI targeted by the current HTTP request in the form of a {@link WebResourceUri}.
   * @return the {@link WebResourceUri} initialized with the URI request.
   */
  WebResourceUri getUri();

  /**
   * Gets the identifier of the component instance to which the requested resource belongs to.
   * @return the identifier of the Silverpeas component instance.
   */
  String getComponentId();
}
