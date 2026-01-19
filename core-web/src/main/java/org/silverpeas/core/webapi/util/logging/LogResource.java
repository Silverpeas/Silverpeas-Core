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
package org.silverpeas.core.webapi.util.logging;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.ecs.xhtml.span;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.exception.RelativeFileAccessException;
import org.silverpeas.core.util.logging.LogsAccessor;
import org.silverpeas.core.web.rs.annotation.Authorized;
import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.kernel.logging.SilverLogger;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * A Web resource representing a given log used by Silverpeas. It is a REST-based Web service.
 * @author mmoquillon
 */
@WebService
@Authorized
@Path(LogResource.LOGS_PATH + "/{logName}")
public class LogResource extends AbstractLoggingResource {

  static final String LOGS_PATH = SilverLoggerConfigurationResource.LOGGING_PATH + "/logs";

  @Inject
  private LogsAccessor logsAccessor;

  @PathParam("logName")
  private String logName;

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<String> getLastLogRecordsAsJson(@QueryParam("count") int count) {
    return getLastLogRecords(count);
  }

  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getLastLogRecordsAsHtml(@QueryParam("count") int count) {
    return String.join("<br>", getLastLogRecords(count))
        .replace("\t", new span("&#160;&#160;&#160;&#160;").toString());
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String getLastLogRecordsAsText(@QueryParam("count") int count) {
    return String.join("\n", getLastLogRecords(count));
  }

  private List<String> getLastLogRecords(int count) {
    try {
      return logsAccessor.getLastLogRecords(logName, count);
    } catch (SilverpeasException ex) {
      SilverLogger.getLogger(this).error(ex.getMessage(), ex);
      if (ex.getCause() instanceof FileNotFoundException) {
        throw new WebApplicationException(Response.Status.NOT_FOUND);
      } else if (ex.getCause() instanceof RelativeFileAccessException) {
        throw new WebApplicationException(Response.Status.FORBIDDEN);
      }
      throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected String getResourceBasePath() {
    return LOGS_PATH;
  }

}
