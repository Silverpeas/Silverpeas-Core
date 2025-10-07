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

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.kernel.util.StringUtil;

/**
 * A group of users managed directly by an application instead of by a user domain or at
 * organization level. Such group is dedicated to gather users with which the application
 * or the users playing a role in that application can interact. The life-cycle of these
 * groups are then taken in charge by the application and not by the Silverpeas administrative
 * service and they are dedicated to be used by the application. Such groups belongs to the mixed
 * user domain and they cannot be synchronized nor be deleted or updated; only the application
 * managing group can modify and delete it.
 *
 * @author mmoquillon
 */
public class AppManagedGroupDetail extends GroupDetail {

  /**
   * Constructs a new application managed group with the given name and for the specified Silverpeas
   * application.
   *
   * @param name the name of the user group.
   * @param instanceId the unique identifier of the Silverpeas instance for which this group is
   * created.
   */
  public AppManagedGroupDetail(String name, String instanceId) {
    super();
    setName(name);
    setRule(null);
    setDomainId(Domain.MIXED_DOMAIN_ID);
    if (StringUtil.isNotDefined(instanceId)) {
      throw new IllegalArgumentException(
          "The identifier of the application instance managing the group should be set");
    }
    setInstanceId(instanceId);
  }

  public AppManagedGroupDetail(GroupDetail groupDetail) {
    super(groupDetail);
    if (!groupDetail.isApplicationManaged()) {
      throw new IllegalArgumentException(
          "The specified group isn't a group managed by an application");
    }
    setTotalNbUsers(groupDetail.getTotalUsersCount());
    setRule(null);
    setDomainId(Domain.MIXED_DOMAIN_ID);
  }

  @Override
  public boolean isApplicationManaged() {
    return true;
  }

  @Override
  public boolean isSynchronized() {
    return false;
  }

  @Override
  public void setDomainId(String newDomainId) {
    if (!newDomainId.equals(Domain.MIXED_DOMAIN_ID)) {
      throw new IllegalArgumentException("An application managed group can belong only to the " +
          "mixed user domain");
    }
    super.setDomainId(newDomainId);
  }

  /**
   * The synchronization isn't supported by the application managed groups. So, this method does
   * nothing.
   *
   * @param rule the synchronization rule to set.
   */
  @Override
  public void setRule(String rule) {
    // not supported by this Group implementation
  }



}
  