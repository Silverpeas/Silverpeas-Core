/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.webapi.session;

import org.silverpeas.core.annotation.WebService;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.web.rs.RESTWebService;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;
import org.silverpeas.core.web.rs.annotation.Authenticated;
import org.silverpeas.core.web.token.SilverpeasWebTokenService;
import org.silverpeas.core.web.token.SilverpeasWebTokenService.WebToken;

import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.security.session.SessionManagementProvider.getSessionManagement;

/**
 * This REST Web Service permits to get a {@link WebToken} value linked to the current User Session.
 * @author silveryocha
 */
@WebService
@Path(SilverpeasUserSessionTokenResource.PATH)
@Authenticated
public class SilverpeasUserSessionTokenResource extends RESTWebService {

  protected static final String PATH = "session/token";

  @Override
  public void validateUserAuthentication(final UserPrivilegeValidation validation) {
    super.validateUserAuthentication(
        validation.skipLastUserAccessTimeRegistering(getHttpServletRequest()));
  }

  /**
   * Gets a {@link WebToken} value linked to the current user session.
   * @return a JSON object containing the token into 'token' attribute.
   */
  @GET
  public Response getToken() {
    final String sessionId = ofNullable(getHttpServletRequest().getSession(false))
        .map(HttpSession::getId)
        .map(getSessionManagement()::getSessionInfo)
        .filter(not(SessionInfo::isAnonymous))
        .map(SessionInfo::getId)
        .orElseThrow(() -> new WebApplicationException("User Session does not exist", NOT_FOUND));
    final WebToken token = SilverpeasWebTokenService.get().generateFor(sessionId);
    return Response.ok(JSONCodec.encodeObject(o -> o.put("token", token.getValue()))).build();
  }

  @Override
  public String getComponentId() {
    return null;
  }

  @Override
  protected String getResourceBasePath() {
    return PATH;
  }
}
