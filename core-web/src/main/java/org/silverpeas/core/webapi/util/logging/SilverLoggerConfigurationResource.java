/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.util.logging;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.webapi.base.annotation.Authenticated;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A Web resource representing the configuration of a given logger. It is a REST-based Web service.
 * @author mmoquillon
 */
@WebService
@Path(SilverLoggerConfigurationResource.LOGGING_PATH + "/{logger}/configuration")
@Authenticated
public class SilverLoggerConfigurationResource extends AbstractLoggingResource {

  static final String LOGGING_PATH = "logging";

  @PathParam("logger")
  private String namespace;

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public LoggerConfigurationEntity changeLoggerConfiguration(LoggerConfigurationEntity config) {
    LoggerConfigurationManager configManager = LoggerConfigurationManager.get();
    if (namespace.equals(config.getLogger())) {
      LoggerConfiguration loggerConfig = config.toLoggerConfiguration();
      /* save the logger configuration */
      configManager.saveLoggerConfiguration(loggerConfig);
      /* and now apply the change to the existing logger */
      SilverLogger.getLogger(namespace).setLevel(loggerConfig.getLevel());
    } else {
      throw new WebApplicationException(
          "The module name of the configuration doesn't match with the URI",
          Response.Status.BAD_REQUEST);
    }
    return LoggerConfigurationEntity.toWebEntity(
        configManager.getLoggerConfiguration(namespace))
        .withAsURi(getUri().getRequestUri());
  }

  @Override
  protected String getResourceBasePath() {
    return LOGGING_PATH;
  }

}
