/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.webapi.look;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.silverpeas.core.webapi.look.delegate.LookWebDelegate;

import org.silverpeas.core.webapi.base.RESTWebService;

/**
 * Centralizations of look resource processings
 * @author Yohann Chastagnier
 */
public abstract class AbstractLookResource extends RESTWebService {

  private LookWebDelegate lookDelegate;

  /**
   * Not recognized object : error.
   * @param object
   * @return the corresponding component entity.
   */
  protected AbstractLookEntity<?> asWebEntity(final Object object) {
    throw new WebApplicationException(Status.NOT_FOUND);
  }

  /**
   * Gets the common look services for Web Services
   * @return
   */
  protected LookWebDelegate getLookDelegate() {
    verifyUserAuthorizedToAccessLookContext();
    return lookDelegate;
  }

  /**
   * Verifies the requester user is authorized to access the look context
   */
  protected void verifyUserAuthorizedToAccessLookContext() {
    // If the look helper is not accessible, then the user is not authorized
    if (!isUserAuthorizedToAccessLookContext()) {
      throw new WebApplicationException(Response.Status.FORBIDDEN);
    }
  }

  /**
   * Indicates if the requester user is authorized to access the look context
   */
  protected boolean isUserAuthorizedToAccessLookContext() {
    // If the look helper is not accessible, then the user is not authorized
    if (lookDelegate == null) {
      lookDelegate =
          LookWebDelegate.getInstance(getUserDetail(), getUserPreferences(),
              getHttpServletRequest());
    }
    return (lookDelegate != null && lookDelegate.getHelper() != null);
  }
}
