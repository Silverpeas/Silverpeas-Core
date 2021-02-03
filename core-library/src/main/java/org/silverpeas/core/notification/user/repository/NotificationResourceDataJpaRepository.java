/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.notification.user.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;

import java.util.List;

@Repository
public class NotificationResourceDataJpaRepository
    extends BasicJpaEntityRepository<NotificationResourceData>
    implements NotificationResourceDataRepository {

  @Override
  public long deleteResources() {
    return deleteFromNamedQuery("NotificationResourceData.deleteResources", noParameter());
  }

  @Override
  public NotificationResourceData getExistingResource(final String resourceId,
      final String resourceType, final String componentInstanceId) {

    // Parameters
    NamedParameters parameters = newNamedParameters();

    // Query
    final StringBuilder query = new StringBuilder("from NotificationResourceData where");
    query.append(" resourceId = :");
    query.append(parameters.add("resourceId", resourceId).getLastParameterName());
    query.append(" and resourceType = :");
    query.append(parameters.add("resourceType", resourceType).getLastParameterName());
    query.append(" and componentInstanceId = :");
    query.append(parameters.add("componentInstanceId", componentInstanceId).getLastParameterName());

    // Result
    final List<NotificationResourceData> resources =
        listFromJpqlString(query.toString(), parameters, NotificationResourceData.class);
    if (resources.size() == 1) {
      return resources.get(0);
    }
    return null;
  }
}
