/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.web;

import com.silverpeas.accesscontrol.AccessController;
import com.stratelia.silverpeas.peasCore.SessionInfo;
import com.stratelia.silverpeas.peasCore.SessionManagement;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.webactiv.beans.admin.UserDetail;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

/**
 * The class of the Silverpeas REST web services.
 * It provides all of the common features required by the web services in Silverpeas like the
 * user priviledge checking.
 */
public abstract class RESTWebService {

  @Inject
  @Named("sessionManager")
  private SessionManagement sessionManager;

  @Inject
  @Named("componentAccessController")
  private AccessController<String> accessController;

  @DefaultValue("") @HeaderParam("X-Silverpeas-SessionKey")
  private String sessionKey;

  @Context
  private UriInfo uriInfo;

  /**
   * Gets the controller of user access on the silverpeas resources.
   * @return the user access controller.
   */
  private AccessController<String> getAccessController() {
    return accessController;
  }

  /**
   * Sets a specific access controller other than the default one in Silverpeas.
   * At Silverpeas bootstrapping, an instance of access controller is created and injected as
   * dependency in each new REST web service instance (for doing, the web service must be managed
   * by an IoC container).
   * This method is mainly for testing purpose.
   * @param accessController the access controller to set.
   */
  protected void setAccessController(AccessController<String> accessController) {
    this.accessController = accessController;
  }

  /**
   * Gets the session manager to use to control the user authentication.
   * @return the user session manager.
   */
  private SessionManagement getSessionManager() {
    return sessionManager;
  }

  /**
   * Sets a specific session manager other than the default one in Silverpeas.
   * At Silverpeas bootstrapping, an instance of a session manager is created and injected as
   * dependency in each new REST web service instance (for doing, the web service must be managed
   * by an IoC container).
   * This method is mainly for testing purpose.
   * @param sessionManager the session manager to set.
   */
  protected void setSessionManager(SessionManager sessionManager) {
    this.sessionManager = sessionManager;
  }

  /**
   * Gets the key of the session of the user calling this web service.
   * If no session key is set in the request, then an empty string is returned.
   * @return the user session key.
   */
  protected String getUserSessionKey() {
    return sessionKey;
  }

  /**
   * Gets information about the URI with which this web service was invoked.
   * @return an UriInfo instance.
   */
  protected UriInfo getUriInfo() {
    return uriInfo;
  }

  /**
   * Gets the detail about the user that has called this web service.
   * If no user can retrieve from the request, id est if no user session is set, then null is
   * returned.
   * @return the detail about the user or null if no session key is set in the request.
   */
  protected UserDetail getUserDetail() {
    SessionInfo sessionInfo = getSessionManager().getSessionInfo(sessionKey);
    if (sessionInfo == null) {
      return null;
    }
    return sessionInfo.getUserDetail();
  }

  /**
   * Checks the user has the correct priviledges to access the underlying referenced resource that
   * belongs to the specified componentId.
   * It the check fail, a WebException is thrown with the HTTP status code set according to the
   * failure.
   * @param componentId the identifier of the component to which the resource belongs.
   */
  protected void checkUserPriviledges(String componentId) {
    UserDetail user = getUserDetail();
    if (user == null) {
      throw new WebApplicationException(Status.UNAUTHORIZED);
    }

    if (!getAccessController().isUserAuthorized(user.getId(), componentId)) {
      throw new WebApplicationException(Status.FORBIDDEN);
    }
  }
}
