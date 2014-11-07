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

package com.silverpeas.web;

import javax.ws.rs.WebApplicationException;

/**
 * A Web resource is a resource representing an entity in Silverpeas that is accessible from the
 * Web. This entity can a be contribution, a contribution's content, or a any business entities
 * (like the user profiles for example).
 * <p>
 * In fact, the Web resource acts as a proxy to one entity or to all the entities of the same type.
 * It listens for incoming HTTP requests and answers them by working on the entity they represent
 * in the Web side; for example it can sent back the entity under the negotiated representation form
 * (defined as a MIME type) or it can update it or it can delete it.
 * </p>
 * <p>
 *   An entity that is accessible to the Web is then structured in the following way:
 * </p>
 * <ul>
 *   <li>The entity itself defining the business operations through which the applications in
 *   Silverpeas manage it. This entity has no knowledge of the Web and of how to be interfaced
 *   with.</li>
 *   <li>The Web resource, proxying the entity for the Web and answering the expected HTTP requests.
 *   It decorates the entity with the mechanism to interact with the Web.
 *   </li>
 *   <li>the entity Web state (aka representation of it for the Web), ready to be encoded into
 *   the negotiated representation format (usually in JSON) and that represents the state of the
 *   entity at the time the HTTP request is answered.</li>
 * </ul>
 * @author mmoquillon
 */
public interface WebResource {

  void validateUserAuthentication(final UserPrivilegeValidation validation) throws
      WebApplicationException;

  void validateUserAuthorization(final UserPrivilegeValidation validation) throws
      WebApplicationException;
}
