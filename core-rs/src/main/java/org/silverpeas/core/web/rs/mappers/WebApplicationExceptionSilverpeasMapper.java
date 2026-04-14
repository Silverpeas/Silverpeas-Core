/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.web.rs.mappers;

import jakarta.xml.bind.annotation.*;
import org.apache.commons.lang3.StringUtils;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * Customizes JAX-RS behavior so that a {@link WebApplicationException} thrown with a cause and a
 * response with no content is mapped to a response containing the exception message. A priority is
 * given to the mapper in order to take precedence over the same type of mappers provided by any
 * external libraries. The priority isn't too high in order to give priority
 * to any Silverpeas mapper of a WebApplicationException if any.
 * @author mmoquillon
 */
@Provider
@Priority(Interceptor.Priority.APPLICATION + 1000)
public class WebApplicationExceptionSilverpeasMapper
    implements ExceptionMapper<WebApplicationException> {

  @Override
  public Response toResponse(final WebApplicationException exception) {
    if (exception.getClass().equals(WebApplicationException.class) &&
        StringUtils.isNotBlank(exception.getMessage()) &&
        exception.getResponse()
            .getEntity() == null) {
      return Response.fromResponse(exception.getResponse())
          .entity(exception.getMessage())
          .type("text/plain")
          .build();
    } else {
      Response response = exception.getResponse();
      Response.Status status = Response.Status.fromStatusCode(response.getStatus());
      ErrorMessage msg = new ErrorMessage();
      msg.setStatus(status.getReasonPhrase());
      if (status.getFamily().equals(Response.Status.Family.CLIENT_ERROR)) {
        msg.setErrorMessage(exception.getMessage());
        SilverLogger.getLogger(this).info(exception.getMessage());
      } else if (status.getFamily().equals(Response.Status.Family.SERVER_ERROR)) {
        SilverLogger.getLogger(this).error(exception.getMessage(), exception);
      }
      return responseBuilder(status, msg).build();
    }
  }

  private Response.ResponseBuilder responseBuilder(Response.Status status, ErrorMessage message) {
    Response.ResponseBuilder builder = Response.status(status);
    builder.entity(message);
    return builder;
  }

  @XmlRootElement
  @XmlAccessorType(XmlAccessType.PROPERTY)
  private static class ErrorMessage {
    private String status;
    private String msg;

    @XmlElement
    public String getStatus() {
      return status;
    }

    public void setStatus(String status) {
      this.status = status;
    }

    @XmlElement
    public String getErrorMessage() {
      return msg;
    }

    public void setErrorMessage(String errorMessage) {
      this.msg = errorMessage;
    }
  }
}
