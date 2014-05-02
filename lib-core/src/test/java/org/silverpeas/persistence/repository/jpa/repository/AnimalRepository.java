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
package org.silverpeas.persistence.repository.jpa.repository;

import org.silverpeas.persistence.model.identifier.UniqueLongIdentifier;
import org.silverpeas.persistence.repository.jpa.SilverpeasJpaEntityManager;
import org.silverpeas.persistence.repository.jpa.model.Animal;
import org.silverpeas.persistence.repository.jpa.model.AnimalType;
import org.silverpeas.persistence.repository.jpa.model.Person;

import javax.inject.Named;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Named
public class AnimalRepository extends SilverpeasJpaEntityManager<Animal, UniqueLongIdentifier> {

  public List<Animal> getByType(AnimalType type) {
    return listFromNamedQuery("getAnimalsByType", newNamedParameters().add("type", type));
  }

  public Animal getByName(String name) {
    return getFromNamedQuery("getAnimalsByName", newNamedParameters().add("name", name));
  }

  public List<Person> getPersonsHaveTypeOfAnimal(AnimalType type) {
    String jpqlQuery = "select a.person from Animal a where a.type like :type";
    return listFromJpqlString(jpqlQuery, newNamedParameters().add("type", type),
        Person.class);
  }

  public long updateAnimalNamesByType(AnimalType type) {
    return updateFromNamedQuery("updateAnimalNamesByType",
        newNamedParameters().add("type", type).add("lastUpdatedBy", "dummy"));
  }

  public long deleteAnimalsByType(AnimalType type) {
    return deleteFromNamedQuery("deleteAnimalsByType",
        newNamedParameters().add("type", type));
  }
}