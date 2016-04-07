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

import org.silverpeas.core.webapi.base.annotation.Authenticated;
import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.webapi.base.RESTWebService;
import org.silverpeas.core.webapi.base.UserPrivilegeValidation;
import org.silverpeas.core.util.logging.LoggerConfigurationManager;
import org.silverpeas.core.util.logging.LoggerConfigurationManager.LoggerConfiguration;
import org.silverpeas.core.util.logging.SilverLogger;

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
@Service
@RequestScoped
@Path("logging/{logger}/configuration")
@Authenticated
public class SilverLoggerConfigurationResource extends RESTWebService {

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
        .withAsURi(getUriInfo().getRequestUri());
  }

  @Override
  public void validateUserAuthorization(final UserPrivilegeValidation validation)
      throws WebApplicationException {
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
