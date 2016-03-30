/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.webapi.notification;

import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.notification.message.MessageManager;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

/**
 * A REST Web resource giving gallery data.
 * @author Yohann Chastagnier
 */
@Service
@RequestScoped
@Path(MessageResourceURIs.MESSAGE_BASE_URI)
@Authenticated
public class MessageResource extends AbstractMessageResource {

  /**
   * User authentication is not necessary for this WEB Service. The authentication processing is
   * used here to identify the user behind the call if possible.
   * @param validation the validation instance to use.
   * @throws javax.ws.rs.WebApplicationException
   */
  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation)
      throws WebApplicationException {
    try {
      super.validateUserAuthentication(
          validation.skipLastUserAccessTimeRegistering(getHttpServletRequest()));
    } catch (WebApplicationException wae) {
      if (Status.UNAUTHORIZED.getStatusCode() != wae.getResponse().getStatus()) {
        throw wae;
      }
    }
  }

  /**
   * Gets the JSON representation of message container.
   * If it doesn't exist, a 404 HTTP code is returned.
   * If a problem occurs when processing the request, a 503 HTTP code is returned.
   * @return the response to the HTTP GET request with the JSON representation of the asked
   *         photo.
   */
  @GET
  @Path("{registredKey}")
  @Produces(MediaType.APPLICATION_JSON)
  public MessageContainerEntity getMessageContainer(
      @PathParam("registredKey") String registredKey) {
    try {
      return asWebEntity(MessageManager.getMessageContainer(registredKey));
    } catch (final WebApplicationException ex) {
      throw ex;
    } catch (final Exception ex) {
      throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
    } finally {
      // The consumer has to clear messages performed.
      MessageManager.clear(registredKey);
    }
  }
}
