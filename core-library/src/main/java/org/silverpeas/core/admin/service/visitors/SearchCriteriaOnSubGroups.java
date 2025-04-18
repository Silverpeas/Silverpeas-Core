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

package org.silverpeas.core.admin.service.visitors;

import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.GroupManager;
import org.silverpeas.core.admin.user.model.GroupsSearchCriteria;
import org.silverpeas.core.admin.user.model.SearchCriteria;
import org.silverpeas.core.admin.user.model.SearchCriteriaVisitor;
import org.silverpeas.core.admin.user.model.UserDetailsSearchCriteria;
import org.silverpeas.kernel.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Visitor of {@link SearchCriteria} objects to complete them with all the subgroups of each group
 * to search. This visitor does nothing if the search isn't about groups.
 *
 * @author mmoquillon
 */
public class SearchCriteriaOnSubGroups implements SearchCriteriaVisitor {

  private final GroupManager groupManager;

  @Inject
  public SearchCriteriaOnSubGroups(GroupManager groupManager) {
    this.groupManager = groupManager;
  }

  @Override
  public void visit(UserDetailsSearchCriteria searchCriteria) throws SilverpeasRuntimeException {
    try {
      if (searchCriteria.isCriterionOnGroupIdsSet()) {
        final Set<String> allGroupsId = new HashSet<>();
        for (String aGroupId : searchCriteria.getCriterionOnGroupIds()) {
          allGroupsId.addAll(groupManager.getAllSubGroupIdsRecursively(aGroupId));
          allGroupsId.add(aGroupId);
        }
        searchCriteria.onGroupIds(allGroupsId.toArray(new String[0]));
      }
    } catch (AdminException e) {
      throw new SilverpeasRuntimeException(e);
    }
  }

  @Override
  public void visit(GroupsSearchCriteria criteria) throws SilverpeasRuntimeException {
    // nothing to do with such criteria
  }
}
  