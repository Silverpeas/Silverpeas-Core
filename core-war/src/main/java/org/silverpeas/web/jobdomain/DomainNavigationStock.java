/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
/*
 * DomainNavigationStock.java
 */

package org.silverpeas.web.jobdomain;

import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.StringUtil;

import java.util.List;

/**
 * This class manage the informations needed for domains navigation and browse PRE-REQUIRED : the
 * Domain passed in the constructor MUST BE A VALID DOMAIN (with Id, etc...)
 * @t.leroi
 */
public class DomainNavigationStock extends NavigationStock {
  Domain m_NavDomain = null;
  String m_DomainId = null;
  List<String> manageableGroupIds = null;

  public DomainNavigationStock(String navDomain, AdminController adc,
      List<String> manageableGroupIds) {
    super(adc);
    m_DomainId = navDomain;
    this.manageableGroupIds = manageableGroupIds;
    refresh();
  }

  public void refresh() {
    m_NavDomain = m_adc.getDomain(m_DomainId);
    m_SubUsers = m_adc.getUsersOfDomain(m_NavDomain.getId());
    if (m_SubUsers == null) {
      m_SubUsers = new UserDetail[0];
    }
    JobDomainSettings.sortUsers(m_SubUsers);
    m_SubGroups = m_adc.getRootGroupsOfDomain(m_NavDomain.getId());
    if (m_SubGroups == null) {
      m_SubGroups = new Group[0];
    }

    if (manageableGroupIds != null)
      m_SubGroups = filterGroupsToGroupManager(manageableGroupIds, m_SubGroups);

    JobDomainSettings.sortGroups(m_SubGroups);
    verifIndexes();
  }

  public Domain getThisDomain() {
    return m_NavDomain;
  }

  static public boolean isDomainValid(Domain dom) {
    return dom != null && StringUtil.isDefined(dom.getId());
  }
}