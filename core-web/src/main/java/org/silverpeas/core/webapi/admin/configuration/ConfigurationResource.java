/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.webapi.admin.configuration;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.silverpeas.core.admin.configuration.ConfigurationService;
import org.silverpeas.core.admin.configuration.CustomSettingsTarget;
import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authorized;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Set;

/** General-administrator access to installed configuration files and their documentation. */
@WebService
@Authorized
@Path("admin/configuration")
public class ConfigurationResource extends RESTWebService {

  @Inject
  private ConfigurationService service;

  @GET
  @Path("files")
  @jakarta.ws.rs.Produces(MediaType.APPLICATION_JSON)
  public Response list() {
    return respond(service::list);
  }

  @GET
  @Path("persistence")
  @jakarta.ws.rs.Produces(MediaType.APPLICATION_JSON)
  public Response persistenceTargets() {
    return respond(service::getPersistenceTargets);
  }

  @POST
  @Path("persistence")
  @Consumes(MediaType.APPLICATION_JSON)
  @jakarta.ws.rs.Produces(MediaType.APPLICATION_JSON)
  public Response persist(PersistenceRequest request) {
    return respond(() -> {
      if (request == null) {
        throw new IllegalArgumentException("A persistence selection is required");
      }
      return service.persist(request.path(), request.revision(), request.keys(), request.target());
    });
  }

  @GET
  @Path("file")
  @jakarta.ws.rs.Produces(MediaType.APPLICATION_JSON)
  public Response read(@QueryParam("path") String path) {
    return respond(() -> service.read(path));
  }

  @PUT
  @Path("file")
  @Consumes(MediaType.APPLICATION_JSON)
  @jakarta.ws.rs.Produces(MediaType.APPLICATION_JSON)
  public Response update(@QueryParam("path") String path, Update update) {
    return respond(() -> {
      if (update == null || update.revision() == null || update.revision().isBlank() ||
          update.properties() == null) {
        throw new IllegalArgumentException("A revision and property changes are required");
      }
      var filePath = path != null ? path : update.path();
      if (filePath == null || filePath.isBlank() ||
          (path != null && update.path() != null && !path.equals(update.path()))) {
        throw new IllegalArgumentException("A consistent configuration file path is required");
      }
      return service.update(filePath, update.revision(), update.properties());
    });
  }

  @Override
  public void validateUserAuthorization(UserPrivilegeValidation validation) {
    requireAdministrator();
  }

  @Override
  protected String getResourceBasePath() {
    return "admin/configuration";
  }

  @Override
  public String getComponentId() {
    return null;
  }

  private Response respond(ConfigurationOperation operation) {
    requireAdministrator();
    try {
      return response(Response.Status.OK, operation.execute());
    } catch (NoSuchFileException e) {
      throw failure(Response.Status.NOT_FOUND, "configuration.notFound");
    } catch (ConcurrentModificationException e) {
      throw failure(Response.Status.CONFLICT, "configuration.conflict");
    } catch (IllegalArgumentException e) {
      throw failure(Response.Status.BAD_REQUEST, "configuration.invalid");
    } catch (SecurityException e) {
      throw failure(Response.Status.FORBIDDEN, "configuration.forbidden");
    } catch (IOException e) {
      throw failure(Response.Status.INTERNAL_SERVER_ERROR, "configuration.ioError");
    }
  }

  private void requireAdministrator() {
    if (getUser() == null || !getUser().isAccessAdmin()) {
      throw failure(Response.Status.FORBIDDEN, "configuration.forbidden");
    }
  }

  private static WebApplicationException failure(Response.Status status, String error) {
    return new WebApplicationException(response(status, Map.of("error", error)));
  }

  private static Response response(Response.Status status, Object entity) {
    return Response.status(status).type(MediaType.APPLICATION_JSON_TYPE).entity(entity)
        .header("Cache-Control", "no-store")
        .header("Pragma", "no-cache")
        .header("X-Content-Type-Options", "nosniff").build();
  }

  /**
   * A patch of raw values and its relative file path. Documentation cannot be changed.
   * @param path relative configuration file path; query paths remain supported for API clients
   * @param revision revision of the file opened by the administrator
   * @param properties only the existing keys to modify
   */
  public record Update(String path, String revision, Map<String, String> properties) {
    public Update(String revision, Map<String, String> properties) {
      this(null, revision, properties);
    }

    @Override
    public String toString() {
      return "Configuration update";
    }
  }

  /**
   * A selection of saved values to merge into an installation script, without client-supplied values.
   * @param path relative properties path
   * @param revision expected runtime file revision
   * @param keys selected existing property keys
   * @param target selected XML file and expected revision
   */
  public record PersistenceRequest(String path, String revision, Set<String> keys,
                                   CustomSettingsTarget target) {
  }

  @FunctionalInterface
  private interface ConfigurationOperation {
    Object execute() throws IOException;
  }
}
