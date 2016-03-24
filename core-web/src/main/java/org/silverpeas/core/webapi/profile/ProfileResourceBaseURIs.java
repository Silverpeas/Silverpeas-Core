/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
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
package org.silverpeas.core.webapi.profile;

import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base URIs from which the REST-based ressources representing users and groups are defined.
 */
public final class ProfileResourceBaseURIs {

  public static final String USERS_BASE_URI = "profile/users";

  public static final String GROUPS_BASE_URI = "profile/groups";

  public static URI uriOfUser(final String userId) {
    try {
      return new URI(USERS_BASE_URI + "/" + userId);
    } catch (URISyntaxException ex) {
      Logger.getLogger(ProfileResourceBaseURIs.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public static URI uriOfUser(final UserDetail user, String atUsersUri) {
    try {
      return new URI(atUsersUri + "/" + user.getId());
    } catch (URISyntaxException ex) {
      Logger.getLogger(ProfileResourceBaseURIs.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public static URI computeParentUriOfGroupByUri(final URI groupUri) {
    String uri = groupUri.toString();
    String parentUri = uri.replaceAll("/groups/[0-9]+$", "");
    if (parentUri.endsWith("profile")) {
      return null;
    } else {
      try {
        return new URI(parentUri);
      } catch (URISyntaxException ex) {
        Logger.getLogger(ProfileResourceBaseURIs.class.getName()).log(Level.SEVERE, null, ex);
        throw new RuntimeException(ex.getMessage(), ex);
      }
    }
  }

  public static URI uriOfGroup(final Group group, String atGroupsUri) {
    try {
      return new URI(atGroupsUri + "/" + group.getId());
    } catch (URISyntaxException ex) {
      Logger.getLogger(ProfileResourceBaseURIs.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public static URI computeChildrenUriOfGroupByUri(final URI groupUri) {
    try {
      return new URI(groupUri.toString() + "/groups");
    } catch (URISyntaxException ex) {
      Logger.getLogger(ProfileResourceBaseURIs.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  public static URI computeUsersUriOfGroupById(final URI groupUri, String groupId) {
    try {
      return new URI(getUsersBaseUriFromGroupUri(groupUri) + "?group=" + groupId);
    } catch (URISyntaxException ex) {
      Logger.getLogger(ProfileResourceBaseURIs.class.getName()).log(Level.SEVERE, null, ex);
      throw new RuntimeException(ex.getMessage(), ex);
    }
  }

  private static String getUsersBaseUriFromGroupUri(URI groupUri) {
    String groupUriAsString = groupUri.toString();
    return groupUriAsString.substring(0, groupUriAsString.indexOf(GROUPS_BASE_URI)) +
        USERS_BASE_URI;
  }
}
