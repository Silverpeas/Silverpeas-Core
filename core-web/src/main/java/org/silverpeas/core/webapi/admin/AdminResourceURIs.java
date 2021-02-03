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
package org.silverpeas.core.webapi.admin;

/**
 * Base URIs from which the REST-based resources representing admin entities are defined.
 * @author Yohann Chastagnier
 */
public final class AdminResourceURIs {

  public static final String SPACES_BASE_URI = "spaces";
  public static final String SPACES_SPACES_URI_PART = "spaces";
  public static final String SPACES_COMPONENTS_URI_PART = "components";
  public static final String SPACES_CONTENT_URI_PART = "content";
  public static final String SPACES_APPEARANCE_URI_PART = "appearance";
  public static final String SPACES_PERSONAL_URI_PART = "personal";

  public static final String USERS_AND_GROUPS_ROLES_URI_PART = "usersAndGroupsRoles";

  public static final String ROLES_PARAM = "roles";
  public static final String FORCE_GETTING_FAVORITE_PARAM = "forceGettingFavorite";
  public static final String GET_NOT_USED_COMPONENTS_PARAM = "getNotUsedComponents";
  public static final String GET_USED_COMPONENTS_PARAM = "getUsedComponents";
  public static final String GET_USED_TOOLS_PARAM = "getUsedTools";

  public static final String COMPONENTS_BASE_URI = "components";

}
