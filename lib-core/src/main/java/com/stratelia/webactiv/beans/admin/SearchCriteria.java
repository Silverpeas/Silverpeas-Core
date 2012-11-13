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
package com.stratelia.webactiv.beans.admin;

/**
 * Criteria to use in a search of resources managed and exposed in Silverpeas (like user profiles or
 * user groups).
 */
public interface SearchCriteria {

  /**
   * The whatever value to be used as criterion value if you don't care of a given criterion.
   */
  static final String[] ANY = null;

  /**
   * Appends a criteria conjonction.
   *
   * @return the criteria enriched with a conjonction. The conjonction will be applied with the last
   * added criterion and the next one.
   */
  SearchCriteria and();

  /**
   * Appends a criterion on the component instance for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   *
   * @param instanceId the unique identifier of the component instance.
   * @return the criteria enriched with a criterion on the component instance.
   */
  SearchCriteria onComponentInstanceId(String instanceId);

  /**
   * Appends a criterion on a given component instance's resource for which the search must be
   * constrained to. This criterion has a meaning only when coupled with the criterion on the
   * component instance. The properties of the resources to fetch have to satisfy both the criterion
   * on the component instance and this one.
   *
   * @param resourceId the unique identifier of the resource managed in the component instance. As
   * each resource is particular to a given Silverpeas component, the unique identifier is made up
   * of the resource type and of the resource identifier.
   * @return the criteria enriched with a criterion on the resource in the component instance.
   */
  SearchCriteria onResourceId(String resourceId);

  /**
   * Appends a criterion on the user domain for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   *
   * @param domainId the unique identifier of the user domain.
   * @return the criteria enriched with a criterion on the user domain.
   */
  SearchCriteria onDomainId(String domainId);

  /**
   * Appends a criterion on the user groups for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   *
   * @param groupIds the unique identifiers of the groups.
   * @return the criteria enriched with a criterion on the user groups.
   */
  SearchCriteria onGroupIds(String... groupIds);

  /**
   * Appends a criterion on the resources name for which the search must be constrained to. The name
   * of the resources to fetch have to satisfy this criterion.
   *
   * @param name a pattern on the name the resources to fetch must have.
   * @return the criteria enriched with a criterion on the user name.
   */
  SearchCriteria onName(String name);

  /**
   * Appends a criterion on the user roles for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   *
   * @param roleNames the name of the user roles on which the criterion has to be built.
   * @return the criteria enriched with a criterion on the role names.
   */
  SearchCriteria onRoleNames(String... roleNames);

  /**
   * Appends a criteria on the user profiles for which the search must be constrained to. The
   * properties of the resources to fetch have to satisfy this criterion.
   *
   * @param userIds the user identifiers.
   * @return the criteria enriched with a criterion on the user identifiers.
   */
  SearchCriteria onUserIds(String... userIds);

  /**
   * Appends a criteria disjonction.
   *
   * @return the criteria enriched with a disjonction. The disjonction will be applied with the last
   * added criterion and the next one.
   */
  SearchCriteria or();

  /**
   * Is this criteria empty?
   *
   * @return true if this criteria has no criterion, false otherwise.
   */
  boolean isEmpty();
}
