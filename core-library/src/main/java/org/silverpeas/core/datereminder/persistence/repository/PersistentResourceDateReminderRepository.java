/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.datereminder.persistence.repository;

import org.silverpeas.core.datereminder.persistence.PersistentResourceDateReminder;
import org.silverpeas.core.persistence.datasource.repository.jpa.NamedParameters;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityRepository;

import javax.inject.Singleton;
import java.util.Collection;
import java.util.Date;

/**
 * @author Cécile Bonin
 */
@Singleton
public class PersistentResourceDateReminderRepository extends
    SilverpeasJpaEntityRepository<PersistentResourceDateReminder> {

  public PersistentResourceDateReminder getByTypeAndResourceId(String type,
      String resourceId) {
    NamedParameters namedParameters = newNamedParameters().add("type", type).add("resourceId", resourceId);
    return getFromNamedQuery("getResource", namedParameters);
  }

  public Collection<PersistentResourceDateReminder> getByDeadLine(Date deadLine) {
    NamedParameters namedParameters = newNamedParameters().add("dateReminder", deadLine);
    return listFromNamedQuery("getListResourceByDeadLine", namedParameters);
  }
}