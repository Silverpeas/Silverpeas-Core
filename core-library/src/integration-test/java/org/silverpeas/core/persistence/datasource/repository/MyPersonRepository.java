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
 * "http://www.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.persistence.datasource.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.silverpeas.core.annotation.Repository;

import java.util.Optional;

@Repository
public class MyPersonRepository {

  @PersistenceContext
  private EntityManager entityManager;

  @Transactional
  public MyPerson save(final MyPerson person) {
    if (person.isPersisted()) {
      return entityManager.merge(person);
    } else {
      entityManager.persist(person);
      return person;
    }
  }

  public Optional<MyPerson> findById(final Long id) {
    return Optional.ofNullable(entityManager.find(MyPerson.class, new MyPersonId().setId(id)));
  }
}
  