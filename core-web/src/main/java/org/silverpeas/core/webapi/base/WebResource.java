/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.base;

import javax.ws.rs.WebApplicationException;

/**
 * A Web resource is a resource representing an entity in Silverpeas that is accessible from the
 * Web. This entity can a be contribution, a contribution's content, or a any business entities
 * (like the user profiles for example).
 * <p>
 * Named as <code>Target</code> in the JAX-RS jargon, as being the endpoint of an URI-based HTTP
 * communication, the Web resource acts in fact, for the Web clients, as a proxy of the entities it
 * is intended to represent and, as such it is uniquely identified by a base URI at which the
 * entities are meant be exposed on the Web. As a proxy, it plays the role of a translator, as it
 * translates the expected HTTP requests in business methods implying the entity(ies) targeted by
 * the requested URI, and then it translates the answer of those methods in an HTTP response that
 * is sent back to the requester; usually, the response carries a representation of the entity(ies)
 * implying in the treatment according to the negotiated format (specified in MIME).
 * </p>
 * <p>
 *   So, an entity that is accessible on the Web is then structured in the following way:
 * </p>
 * <ul>
 *   <li>The entity itself defining the business operations through which the applications in
 *   Silverpeas manage it. This entity has no knowledge of the Web and of how to be interfaced
 *   with.</li>
 *   <li>The Web resource, proxying the entity for the Web and translating the HTTP-verbs in
 *   business operations implying the entity(ies) targeted by the exact requested URI.
 *   </li>
 *   <li>The entity Web state (aka entity state representation for the Web), ready to be encoded
 *   into the negotiated representation format (usually in JSON) and that represents the state of
 *   the entity at the time the HTTP request is answered.</li>
 * </ul>
 * @author mmoquillon
 */
public interface WebResource {

  /**
   * Validates the authentication of the user requesting this web service. If no session was opened
   * for the user, then open a new one. The validation is actually delegated to the validation
   * service by passing it the required information.
   *
   * This method should be invoked for web service requiring an authenticated user. Otherwise, the
   * annotation Authenticated can be also used instead at class level.
   *
   * @see UserPrivilegeValidator
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the authentication isn't valid (no authentication and
   * authentication failure).
   */
  void validateUserAuthentication(final UserPrivilegeValidation validation) throws
      WebApplicationException;

  /**
   * Validates the authorization of the user to request this web service. For doing, the user must
   * have the rights to access the component instance that manages this web resource. The validation
   * is actually delegated to the validation service by passing it the required information.
   *
   * This method should be invoked for web service requiring an authorized access. For doing, the
   * authentication of the user must be first valdiated. Otherwise, the annotation Authorized can be
   * also used instead at class level for both authentication and authorization.
   *
   * @see UserPrivilegeValidator
   * @param validation the validation instance to use.
   * @throws WebApplicationException if the rights of the user are not enough to access this web
   * resource.
   */
  void validateUserAuthorization(final UserPrivilegeValidation validation) throws
      WebApplicationException;

  /**
   * Gets the identifier of the component instance to which the requested resource belongs to.
   * @return the identifier of the Silverpeas component instance.
   */
  String getComponentId();
}
