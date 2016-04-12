/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.notification.user.server.channel.popup;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author mmoquillon
 */
@Singleton
public class POPUPMessageBeanFinder {

  @PersistenceContext
  private EntityManager entityManager;

  private static POPUPMessageBeanFinder getInstance() {
    return ServiceProvider.getService(POPUPMessageBeanFinder.class);
  }

  public static List<POPUPMessageBean> getSomeByQuery(String query) {
    return getInstance().entityManager.createQuery(query, POPUPMessageBean.class).getResultList();
  }

  public static POPUPMessageBean getById(long id) {
    return getInstance().entityManager.find(POPUPMessageBean.class, UniqueLongIdentifier.from(id));
  }

  public static long count() {
    return getInstance().entityManager.createQuery("select count(m) from POPUPMessageBean m",
        Long.class).getSingleResult();
  }
}
