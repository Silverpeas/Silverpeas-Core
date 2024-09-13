/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.persistence.datasource.repository.basicjpa.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.PersonBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

import javax.inject.Singleton;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Repository
@Singleton
public class PersonBasicEntityRepository
    extends BasicJpaEntityRepository<PersonBasicEntity> {

  public PersonBasicEntity getByFirstName(String firstName) {
    String jpqlQuery = "select p from PersonBasicEntity p where p.firstName = :firstName";
    return getFromJpqlString(jpqlQuery, newNamedParameters().add("firstName", firstName));
  }

  public List<PersonBasicEntity> getByLastName(String lastName) {
    return listFromNamedQuery("getPersonsByNameCustom", newNamedParameters().add("name", lastName));
  }

  public List<AnimalBasicEntity> getAnimalByLastNameOfPerson(String lastName) {
    String jpqlQuery = "select a from AnimalBasicEntity a where a.person.lastName like :lastName";
    return listFromJpqlString(jpqlQuery, newNamedParameters().add("lastName", lastName),
        AnimalBasicEntity.class);
  }

  public long deletePersonFirstNamesHavingAtLeastOneAnimal() {
    String jpqlQuery = "delete PersonBasicEntity p where p.animals is not empty";
    return deleteFromJpqlQuery(jpqlQuery, noParameter());
  }
}