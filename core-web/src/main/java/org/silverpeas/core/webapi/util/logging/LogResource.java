/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.util.logging;

import org.silverpeas.core.webapi.base.annotation.Authorized;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.util.logging.LogsAccessor;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * A Web resource representing a given log used by Silverpeas. It is a REST-based Web service.
 * @author mmoquillon
 */
@Service
@RequestScoped
@Authorized
@Path("logging/logs/{logName}")
public class LogResource extends RESTWebService {

  @Inject
  private LogsAccessor logsAccessor;

  @PathParam("logName")
  private String logName;

  @GET
  @Consumes(MediaType.APPLICATION_JSON)
  public String[] getLastLogRecords(@QueryParam("count")int count) {
    try {
      return logsAccessor.getLastLogRecords(logName, count);
    } catch (FileNotFoundException ex) {
      SilverLogger.getLogger("core").error(ex.getMessage(), ex);
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    } catch (IOException ex) {
      SilverLogger.getLogger("core").error(ex.getMessage(), ex);
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation) throws
      WebApplicationException {
    if (!getUserDetail().isAccessAdmin()) {
      throw new WebApplicationException("Only administrators can play with logger configurations!",
          Response.Status.FORBIDDEN);
    }
  }

  /**
   * Gets the identifier of the component instance to which the requested resource belongs to.
   * @return the identifier of the Silverpeas component instance.
   */
  @Override
  public String getComponentId() {
    return null;
  }
}
