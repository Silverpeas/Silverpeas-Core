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
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

import javax.inject.Singleton;
import java.util.List;

/**
 * Default implementation of the {@link DelegationRepository} interface built upon JPA. All the
 * common JPA functionalities are provided by the {@link SilverpeasJpaEntityRepository} class.
 * @author mmoquillon
 */
@Singleton
public class DefaultDelegationRepository extends SilverpeasJpaEntityRepository<Delegation>
    implements DelegationRepository {

  @Override
  public List<Delegation> getByDelegator(final User user) {
    NamedParameters parameters = newNamedParameters().add("delegator", user.getId());
    return findByNamedQuery("Delegation.findByDelegator", parameters);
  }

  @Override
  public List<Delegation> getByDelegate(final User user) {
    NamedParameters parameters = newNamedParameters().add("delegate", user.getId());
    return findByNamedQuery("Delegation.findByDelegate", parameters);
  }

  @Override
  public List<Delegation> getByDelegatorAndComponentId(final User user, final String componentId) {
    NamedParameters parameters =
        newNamedParameters().add("delegator", user.getId()).add("componentId", componentId);
    return findByNamedQuery("Delegation.findByDelegatorByComponentId", parameters);
  }

  @Override
  public List<Delegation> getByDelegateAndComponentId(final User user, final String componentId) {
    NamedParameters parameters =
        newNamedParameters().add("delegate", user.getId()).add("componentId", componentId);
    return findByNamedQuery("Delegation.findByDelegateByComponentId", parameters);
  }
}
  