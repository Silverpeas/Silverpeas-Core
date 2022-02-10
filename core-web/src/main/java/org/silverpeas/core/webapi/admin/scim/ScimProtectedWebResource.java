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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.webapi.admin.scim;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.WebResourceUri;
import org.silverpeas.core.web.rs.ProtectedWebResource;
import org.silverpeas.core.web.rs.UserPrivilegeValidation;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;

import static java.text.MessageFormat.format;
import static java.util.Arrays.stream;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.silverpeas.core.util.StringUtil.isNotDefined;
import static org.silverpeas.core.webapi.admin.scim.ScimLogger.logger;
import static org.silverpeas.core.webapi.admin.scim.ScimServerFilter
    .PUSH_SILVERPEAS_AUTHORIZED_ADMIN_IDS_PROP_KEY;

/**
 * <p>
 * All WEB services handling the SCIM client requests in front must implement this interface.
 * </p>
 * <p>
 * The authorization mechanism is not the same as the one implemented into
 * {@link ProtectedWebResource}.
 * </p>
 * @author silveryocha
 */
public interface ScimProtectedWebResource extends ProtectedWebResource {

  @Override
  ScimRequestContext getSilverpeasContext();

  @Override
  default void validateUserAuthorization(final UserPrivilegeValidation validation) {
    Domain domain;
    try {
      domain = Administration.get().getDomain(getSilverpeasContext().getDomainId());
    } catch (AdminException e) {
      throw new WebApplicationException(e, NOT_FOUND);
    }
    final String authorizedUserIds = domain.getSettings()
        .getString(PUSH_SILVERPEAS_AUTHORIZED_ADMIN_IDS_PROP_KEY);
    if (isNotDefined(authorizedUserIds)) {
      final String error = format("Please verify the variable {0} into SCIM domain property file",
          PUSH_SILVERPEAS_AUTHORIZED_ADMIN_IDS_PROP_KEY);
      logger().error(error);
      throw new WebApplicationException(error, FORBIDDEN);
    }
    final User currentUser = getSilverpeasContext().getUser();
    final String currentUserId = currentUser.getId();
    if (stream(authorizedUserIds.split("[ ,;]")).noneMatch(i -> i.trim().equals(currentUserId))) {
      final String error = format(
          "user with id {0} is not authorized to accept push from SCIM client {1}", currentUserId,
          getSilverpeasContext().getRequest().getRemoteHost());
      logger().error(error);
      throw new WebApplicationException(error, FORBIDDEN);
    }

    if (!currentUser.isAccessAdmin()) {
      final String error = format("user with id {0} must have admin access level", currentUserId);
      logger().error(error);
      throw new WebApplicationException(error, FORBIDDEN);
    }
  }

  default AdminController getAdminController() {
    return ServiceProvider.getService(AdminController.class);
  }

  @Override
  default HttpServletRequest getHttpRequest() {
    return getSilverpeasContext().getRequest();
  }

  @Override
  default WebResourceUri getUri() {
    return null;
  }

  @Override
  default String getComponentId() {
    return null;
  }
}
