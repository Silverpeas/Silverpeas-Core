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

package org.silverpeas.core.contribution.rating.model;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author mmoquillon
 */
@Singleton
public class RatingFinder {

  @PersistenceContext
  private EntityManager entityManager;

  private static RatingFinder getInstance() {
    return ServiceProvider.getService(RatingFinder.class);
  }

  public static List<Rating> getSomeByQuery(String query) {
    return getInstance().entityManager.createQuery(query, Rating.class).getResultList();
  }

  public static Rating getById(int id) {
    return getInstance().entityManager.find(Rating.class, UniqueIntegerIdentifier.from(id));
  }

  public static long count() {
    return getInstance().entityManager.createQuery("select count(r) from Rating r",
        Long.class).getSingleResult();
  }
}
