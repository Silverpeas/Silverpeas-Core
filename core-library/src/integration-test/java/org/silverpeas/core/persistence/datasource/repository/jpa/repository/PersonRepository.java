/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.core.persistence.datasource.repository.jpa.repository;

import org.silverpeas.core.persistence.datasource.repository.jpa.model.Person;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.repository.jpa.SilverpeasJpaEntityManager;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Animal;

import javax.inject.Singleton;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Singleton
public class PersonRepository extends SilverpeasJpaEntityManager<Person, UuidIdentifier> {

  public Person getByFirstName(String firstName) {
    String jpqlQuery = "from Person p where p.firstName = :firstName";
    return getFromJpqlString(jpqlQuery, newNamedParameters().add("firstName", firstName));
  }

  public List<Person> getByLastName(String lastName) {
    return listFromNamedQuery("getPersonsByName",
        newNamedParameters().add("name", lastName));
  }

  public List<Animal> getAnimalByLastNameOfPerson(String lastName) {
    String jpqlQuery = "select a from Animal a where a.person.lastName like :lastName";
    return listFromJpqlString(jpqlQuery, newNamedParameters().add("lastName", lastName),
        Animal.class);
  }

  public long updatePersonFirstNameHavingAtLeastOneAnimal(Person person) {
    String jpqlQuery = "update Person p set p.firstName = :name, " +
        "p.lastUpdatedBy = :lastUpdatedBy, p.lastUpdateDate = :lastUpdateDate, " +
        "p.version = :version where p.id = :id and p.animals is not empty";
    return updateFromJpqlQuery(jpqlQuery, newNamedParameters().add("lastUpdatedBy", "dummy")
            .add("name", person.getFirstName())
            .add("id", UuidIdentifier.from(person.getId()))
            .add("version", person.getVersion() + 1));
  }

  /**
   * Missing technical data
   * @return
   */
  public long badUpdateMissingLastUpdatedBy() {
    String jpqlQuery = "update Person p set p.firstName = concat(p.firstName, '_updated'), " +
        "p.lastUpdateDate = :lastUpdateDate, " +
        "p.version = (p.version + 1) where p.animals is not empty";
    return updateFromJpqlQuery(jpqlQuery,
        newNamedParameters().add("lastUpdatedBy", "dummy"));
  }

  /**
   * Missing technical data
   * @return
   */
  public long badUpdateMissingLastUpdateDate() {
    String jpqlQuery = "update Person p set p.firstName = concat(p.firstName, '_updated'), " +
        "p.lastUpdatedBy = :lastUpdatedBy, " +
        "p.version = (p.version + 1) where p.animals is not empty";
    return updateFromJpqlQuery(jpqlQuery,
        newNamedParameters().add("lastUpdatedBy", "dummy"));
  }

  /**
   * Missing technical data
   * @return
   */
  public long badUpdateMissingVersionManagement() {
    String jpqlQuery = "update Person p set p.firstName = concat(p.firstName, '_updated'), " +
        "p.lastUpdatedBy = :lastUpdatedBy, p.lastUpdateDate = :lastUpdateDate, " +
        "p.version = (p.version + 2) where p.animals is not empty";
    return updateFromJpqlQuery(jpqlQuery,
        newNamedParameters().add("lastUpdatedBy", "dummy"));
  }

  public long deletePersonFirstNamesHavingAtLeastOneAnimal() {
    String jpqlQuery = "delete Person p where p.animals is not empty";
    return deleteFromJpqlQuery(jpqlQuery, newNamedParameters());
  }
}