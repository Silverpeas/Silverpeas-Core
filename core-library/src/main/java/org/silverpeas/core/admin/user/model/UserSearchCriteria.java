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
package org.silverpeas.core.admin.user.model;

/**
 * Criteria in searching of user details.
 */
public interface UserSearchCriteria {

  /**
   * The whatever value to be used as criterion value if you don't care of a given criterion.
   */
  static final String[] ANY = null;

  /**
   * Appends a criteria conjonction.
   * @return the criteria enriched with a conjonction. The conjonction will be applied with the last
   * added criterion and the next one.
   */
  UserSearchCriteria and();

  /**
   * The users must be part of the specified domain or must have the administration priviledges
   * (the administrators are visible by anyone in the platform in order to be contacted).
   * @param domainId the unique identifier of the domain.
   * @return the criteria enriched with a criterion on the domain.
   */
  UserSearchCriteria onDomainId(String domainId);

  /**
   * The users must be part of the specified user groups.
   * @param groupIds the unique identifier of the groups.
   * @return the criteria enriched with a criterion on the user groups.
   */
  UserSearchCriteria onGroupIds(String... groupIds);

  /**
   * The users must have their firstname or their lastname matching the specified pattern on the
   * name.
   * @param name a pattern on user name.
   * @return the criteria enriched with a criterion on the user name.
   */
  UserSearchCriteria onName(String name);

  /**
   * The user identifiers must match the specified ones.
   * @param userIds the user identifiers.
   * @return the criteria enriched with a criterion on the user identifiers.
   */
  UserSearchCriteria onUserIds(String... userIds);

  /**
   * Appends a criteria disjonction.
   * @return the criteria enriched with a disjonction. The disjonction will be applied with the last
   * added criterion and the next one.
   */
  UserSearchCriteria or();

  /**
   * Is this criteria empty?
   * @return true if this criteria has no criterion, false otherwise.
   */
  boolean isEmpty();
}
