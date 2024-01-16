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

package org.silverpeas.core.webapi.wopi;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.wopi.WopiFile;
import org.silverpeas.core.wopi.WopiFileEditionManager;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.function.Supplier;

import static java.text.MessageFormat.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.silverpeas.core.wopi.WopiLogger.logger;

/**
 * @author silveryocha
 */
public abstract class AbstractWopiFileResource implements WopiProtectedWebResource {

  @PathParam("fileId")
  private String fileId;

  private WopiFileEditionContext editionContext;

  @Inject
  private WopiRequestContext requestContext;

  @Inject
  private WopiFileEditionManager editionManager;

  @Context
  private HttpServletRequest httpRequest;

  @Context
  private HttpServletResponse httpResponse;

  @PostConstruct
  protected void initContext() {
    requestContext.init(httpRequest, httpResponse);
  }

  @Override
  public WopiRequestContext getSilverpeasContext() {
    return requestContext;
  }

  /**
   * Processes the given supplier after authorization checking
   * @param supplier the supplier of response.
   * @return the response given by supplier.
   */
  protected Response process(Supplier<Response> supplier) {
    try {
      final String accessToken = requestContext.getAccessToken();
      editionContext = getEditionManager().getEditionContextFrom(fileId, accessToken,
          (u, f) -> new WopiFileEditionContext(u.orElse(null), f.orElse(null)));
      final Optional<Response> errorResponse = checkEditionContext();
      return errorResponse.orElseGet(supplier);
    } catch (WopiResponseError e) {
      return e.getResponse();
    } catch (Exception e) {
      logger().error(e);
      return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  private Optional<Response> checkEditionContext() {
    if (editionContext.getUser() == null) {
      return of(Response.status(Response.Status.UNAUTHORIZED).build());
    }
    final User user = editionContext.getUser().asSilverpeas();
    final WopiFile wopiFile = editionContext.getFile();
    if (wopiFile == null) {
      return of(Response.status(Response.Status.NOT_FOUND).build());
    }
    if (HttpMethod.GET.equals(httpRequest.getMethod()) && !wopiFile.canBeAccessedBy(user)) {
      final String error = format("User {0} can not access the file {1}", user.getId(), wopiFile);
      logger().error(error);
      return of(Response.status(Response.Status.UNAUTHORIZED).build());
    } else if (!HttpMethod.GET.equals(httpRequest.getMethod()) && !wopiFile.canBeModifiedBy(user)){
      final String error = format("User {0} can not modify the file {1}", user.getId(), wopiFile);
      logger().error(error);
      return of(Response.status(Response.Status.UNAUTHORIZED).build());
    }
    return empty();
  }

  protected WopiFileEditionContext getEditionContext() {
    return editionContext;
  }

  protected WopiFileEditionManager getEditionManager() {
    return editionManager;
  }
}
