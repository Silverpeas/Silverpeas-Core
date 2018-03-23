/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core.delegation;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.persistence.datasource.repository.EntityRepository;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

/**
 * The repository into which are persisted the {@link Delegation} instances.
 * @author mmoquillon
 */
public interface DelegationRepository extends EntityRepository<Delegation> {

  /**
   * Gets an instance of such a entity repository.
   * @return a {@link DelegationRepository} object.
   */
  static DelegationRepository get() {
    return ServiceProvider.getService(DelegationRepository.class);
  }

  /**
   * Gets all the delegations coming from the specified user.
   * @param user a user in Silverpeas.
   * @return a list of {@link Delegation} instances, each of them being a delegation of a given
   * responsibility to another user in Silverpeas. If the user hasn't emitted any delegations then
   * an empty list is returned.
   */
  List<Delegation> getByDelegator(final User user);

  /**
   * Gets all the delegations attributed to the specified user.
   * @param user a user in Silverpeas.
   * @return a list of {@link Delegation} instances, each of them being a delegation of a given
   * responsibility from another user in Silverpeas. If the specified user isn't a delegate of any
   * responsibility, then an empty list is returned.
   */
  List<Delegation> getByDelegate(User user);

  /**
   * Gets all the delegations coming from the specified user and set for the specified component
   * instance.
   * @param user a user in Silverpeas.
   * @param componentId the unique identifier of a component instance.
   * @return a list of {@link Delegation} instances, each of them being a delegation of a given
   * responsibility in the the given component instance and that was attributed by the specified
   * user. If the user hasn't emitted any delegations for the component instance then an empty list
   * is returned.
   */
  List<Delegation> getByDelegatorAndComponentId(User user, String componentId);

  /**
   * Gets all the delegations attributed to the specified user and for the specified component
   * instance.
   * @param user a user in Silverpeas.
   * @param componentId the unique identifier of a component instance.
   * @return a list of {@link Delegation} instances, each of them being a delegation of a given
   * responsibility in the the given component instance and that was attributed to the specified
   * user. If the user hasn't received any delegations for the component instance then an empty
   * list is returned.
   */
  List<Delegation> getByDelegateAndComponentId(User user, String componentId);
}
