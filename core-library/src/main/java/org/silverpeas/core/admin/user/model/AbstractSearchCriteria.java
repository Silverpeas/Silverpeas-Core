/*
 * Copyright (C) 2000 - 2023 Silverpeas
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

package org.silverpeas.core.admin.user.model;

import org.apache.commons.lang3.ArrayUtils;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.silverpeas.core.util.ArrayUtil.isNotEmpty;

/**
 * @author silveryocha
 */
public abstract class AbstractSearchCriteria implements SearchCriteria {

  private static final String USER_STATES_TO_EXCLUDE = "userStatesToExclude";
  private static final String INCLUDE_REMOVED_USERS = "includeRemovedUsers";
  private static final String ROLE_NAMES = "roleIds";
  private static final String MATCHING_ALL_ROLE_NAMES = "matchingAllRoleNames";

  protected Map<String, Object> criteria = new HashMap<>();

  @Override
  public SearchCriteria onUserStatesToExclude(final UserState... userStates) {
    if (isNotEmpty(userStates)) {
      criteria.put(USER_STATES_TO_EXCLUDE, userStates);
    }
    return adjustUserStatesToExclude();
  }

  @Override
  public SearchCriteria includeRemovedUsers() {
    criteria.put(INCLUDE_REMOVED_USERS, true);
    return adjustUserStatesToExclude();
  }

  @Override
  public SearchCriteria onRoleNames(String... roleIds) {
    if (ArrayUtils.isNotEmpty(roleIds)) {
      criteria.put(ROLE_NAMES,
          Arrays.stream(roleIds).filter(StringUtil::isDefined).toArray(String[]::new));
    }
    return this;
  }

  @Override
  public SearchCriteria matchingAllRoleNames() {
    criteria.put(MATCHING_ALL_ROLE_NAMES, true);
    return this;
  }

  @SuppressWarnings("unchecked")
  private <T extends SearchCriteria> T adjustUserStatesToExclude() {
    if (isCriterionOnUserStatesToExcludeSet() && mustIncludeRemovedUsers()) {
      final UserState[] statedToExclude = Stream.of(getCriterionOnUserStatesToExclude())
          .filter(Predicate.not(UserState.REMOVED::equals))
          .toArray(UserState[]::new);
      if (statedToExclude.length > 0) {
        criteria.put(USER_STATES_TO_EXCLUDE, statedToExclude);
      } else {
        criteria.remove(USER_STATES_TO_EXCLUDE);
      }
    }
    return (T) this;
  }

  /**
   * Is the user states to exclude criterion set?
   * @return true if set, false otherwise.
   */
  public boolean isCriterionOnUserStatesToExcludeSet() {
    return criteria.containsKey(USER_STATES_TO_EXCLUDE);
  }

  /**
   * Is the criterion on role names set?
   * @return true if set, false otherwise.
   */
  public boolean isCriterionOnRoleNamesSet() {
    return criteria.containsKey(ROLE_NAMES);
  }

  /**
   * Gets user states to exclude criterion.
   * @return the access level criterion.
   */
  public UserState[] getCriterionOnUserStatesToExclude() {
    return (UserState[]) criteria.get(USER_STATES_TO_EXCLUDE);
  }

  /**
   * Must the removed users be included criterion.
   * @return true if removed users must be taken into account, false otherwise.
   */
  public boolean mustIncludeRemovedUsers() {
    return Boolean.TRUE.equals(criteria.get(INCLUDE_REMOVED_USERS));
  }

  /**
   * Gets the disjunction on the role names.
   * @return an array with each element of the disjunction.
   */
  public String[] getCriterionOnRoleNames() {
    return (String[]) criteria.get(ROLE_NAMES);
  }

  /**
   * Must each result item matches all role names.
   * @return true if it must match all roles, false otherwise.
   */
  public boolean mustMatchAllRoles() {
    return isCriterionOnRoleNamesSet() && getCriterionOnRoleNames().length > 1 &&
        Boolean.TRUE.equals(criteria.get(MATCHING_ALL_ROLE_NAMES));
  }
}
