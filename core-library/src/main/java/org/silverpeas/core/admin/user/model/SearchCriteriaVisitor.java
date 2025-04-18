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

package org.silverpeas.core.admin.user.model;

import org.silverpeas.kernel.SilverpeasRuntimeException;

/**
 * A visitor of {@link SearchCriteria} objects to perform some peculiar treatments on them or from
 * them. Usually a visitor refines some criteria according to some of others ones or it can validate
 * some of them.
 *
 * @author mmoquillon
 */
public interface SearchCriteriaVisitor {

  /**
   * Visits the specified criteria to perform additional treatments in order to search users
   * satisfying those criteria. The visitor can clear the specified criteria if the conditions on
   * which the behaviour of his visitor is expecting aren't satisfied. Such conditions can be, for
   * example, some of the criteria have to satisfy well defined validation rules, or some given
   * criteria have to be refined from some others criteria and according to well defined business
   * rules.
   *
   * @param criteria the criteria to visit
   * @throws SilverpeasRuntimeException if the additional treatments fail unexpectedly.
   */
  void visit(UserDetailsSearchCriteria criteria) throws SilverpeasRuntimeException;

  /**
   * Visits the specified criteria to perform additional treatments in order to search user groups
   * satisfying those criteria. The visitor can clear the specified criteria if the conditions on
   * which the behaviour of his visitor is expecting aren't satisfied. Such conditions can be, for
   * example, some of the criteria have to satisfy well defined validation rules, or some given
   * criteria have to be refined from some others criteria and according to well defined business
   * rules.
   *
   * @param criteria the criteria to visit
   * @throws SilverpeasRuntimeException if the additional treatments fail unexpectedly.
   */
  void visit(GroupsSearchCriteria criteria) throws SilverpeasRuntimeException;
}
  