/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.profile.service;

import com.stratelia.webactiv.beans.admin.UserDetail;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;

/**
 * This service provides the different business operations to manage the users in the Silverpeas
 * portal.
 * 
 * As currently all theses operations are provided by several classes (OrganizationController,
 * AdminController and Admin) at different abstraction layer, the UserProfileService instance wraps
 * all of theses in order to publish a single point to access the operations about the user profiles.
 */
@Named
public class UserProfileService {
  
  /**
   * Gets all the users in the Silverpeas portal that are accessible to the user identified by the
   * specified unique identifier.
   * 
   * The accessible computation is performed from both the profile of the user and the current domain
   * isolation policy.
   * @param userId the unique identifier of the user for which all the users it can access have to
   * be get.
   * @return a list of all the users in Silverpeas that are accessible by the specified user. 
   */
  public List<UserDetail> getAllUsersAccessibleTo(String userId) {
    List<UserDetail> accessibleUsers;
    UserDetail forUser = UserDetail.getById(userId);
    if (forUser.isDomainRestricted()) {
      accessibleUsers = UserDetail.getAllInDomain(forUser.getDomainId());
    } else {
      accessibleUsers = UserDetail.getAll();
    }
    return accessibleUsers;
  }
}
