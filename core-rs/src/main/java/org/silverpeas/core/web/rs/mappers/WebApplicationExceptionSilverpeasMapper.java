/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.web.rs.mappers;

import edu.psu.swe.commons.jaxrs.exceptions.mappers.WebApplicationExceptionMapper;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Priority;
import javax.interceptor.Interceptor;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Customizes JAX-RS behaviour so that a {@link WebApplicationException} thrown with a cause and a
 * response with no content is mapped to a response containing the exception message. A priority is
 * given to the mapper in order to take precedence over the same type of mappers provided by any
 * external libraries (id est commons-jaxrs). The priority isn't too high in order to give priority
 * to any Silverpeas mapper of a WebApplicationException if any.
 */
@Provider
@Priority(Interceptor.Priority.APPLICATION + 1000)
public class WebApplicationExceptionSilverpeasMapper extends WebApplicationExceptionMapper
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
      return super.toResponse(exception);
    }
  }
}
