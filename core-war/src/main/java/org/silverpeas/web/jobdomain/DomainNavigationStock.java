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
/*
 * DomainNavigationStock.java
 */

package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;

import java.util.List;

/**
 * This class manage the information needed for domains navigation and browsing.
 * REQUIREMENT: the Domain passed in the constructor MUST BE A VALID DOMAIN (with Id, etc...)
 * @author t.leroi
 */
public class DomainNavigationStock extends NavigationStock {
  private Domain domain = null;
  private final String domainId;
  private final List<String> manageableGroupIds;

  public DomainNavigationStock(String navDomain, AdminController adc,
      List<String> manageableGroupIds) {
    super(adc);
    domainId = navDomain;
    this.manageableGroupIds = manageableGroupIds;
    refresh();
  }

  public void refresh() {
    userStateFilter = null;
    domain = adminController.getDomain(domainId);
    subUsers = adminController.getUsersOfDomain(domain.getId());
    if (subUsers == null) {
      subUsers = new UserDetail[0];
    }
    JobDomainSettings.sortUsers(subUsers);
    subGroups = adminController.getRootGroupsOfDomain(domain.getId());
    if (subGroups == null) {
      subGroups = new Group[0];
    }

    if (manageableGroupIds != null)
      subGroups = filterGroupsToGroupManager(manageableGroupIds, subGroups);

    JobDomainSettings.sortGroups(subGroups);
  }

  public Domain getThisDomain() {
    return domain;
  }

}