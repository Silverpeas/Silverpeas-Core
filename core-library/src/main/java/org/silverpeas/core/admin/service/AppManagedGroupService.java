/*
 * Copyright (C) 2000 - 2025 Silverpeas
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

package org.silverpeas.core.admin.service;

import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.util.ServiceProvider;

import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * A service to handle application managed groups. This service aims to ease the management of user
 * groups for the Silverpeas applications without having to use explicitly the
 * {@link org.silverpeas.core.admin.service.Administration} service and in order to avoid errors
 * while handling such groups for the account of applications in regards to the constrains on such
 * group types.
 *
 * @author mmoquillon
 */
@Service
@Singleton
@Transactional(Transactional.TxType.MANDATORY)
public class AppManagedGroupService {

  @Inject
  private Administration admin;

  /**
   * Gets an instance of a {@link AppManagedGroupService}.
   *
   * @return an {@link AppManagedGroupService} instance.
   */
  public static AppManagedGroupService get() {
    return ServiceProvider.getService(AppManagedGroupService.class);
  }

  /**
   * Gets the user group identified by the specified identifier. If no such group exists, null is
   * returned. If the group isn't managed by an application, an {@link IllegalArgumentException} is
   * thrown.
   *
   * @param groupId the unique identifier of a user group
   * @return an application managed user group.
   * @throws AdminException if an error occurs while fetching the asked user group
   * @throws IllegalArgumentException if the asked group isn't managed by a Silverpeas application.
   */
  public AppManagedGroupDetail getGroup(String groupId) throws AdminException {
    return asAppManagedGroupDetail(admin.getGroup(groupId));
  }

  /**
   * Saves any changes in the specified user group in the datasource.
   *
   * @param group the user group.
   * @throws AdminException if the update fails.
   */
  public void updateGroup(AppManagedGroupDetail group) throws AdminException {
    admin.updateGroup(group, true);
  }

  /**
   * Deletes the specified user group in the datasource.
   *
   * @param group the user group to delete.
   * @throws AdminException if the deletion fails.
   */
  public void deleteGroup(AppManagedGroupDetail group) throws AdminException {
    admin.deleteGroupById(group.getId(), true);
  }

  /**
   * Creates the specified user group for the Silverpeas application related by the group. The
   * Silverpeas application is identified with the {@link AppManagedGroupDetail#getInstanceId()}
   * property.
   *
   * @param group the user group to create in Silverpeas for the underlying component instance.
   * @throws AdminException if the creation fails.
   */
  public void createGroup(AppManagedGroupDetail group) throws AdminException {
    admin.addGroup(group, true);
  }

  private AppManagedGroupDetail asAppManagedGroupDetail(GroupDetail groupDetail) {
    return groupDetail == null ? null : new AppManagedGroupDetail(groupDetail);
  }

}
  