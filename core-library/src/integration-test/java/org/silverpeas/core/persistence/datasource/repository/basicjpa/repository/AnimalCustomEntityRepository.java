/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.persistence.datasource.repository.basicjpa.repository;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueLongIdentifier;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalTypeCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.PersonCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaBasicEntityManager;

import javax.inject.Singleton;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Singleton
public class AnimalCustomEntityRepository
    extends JpaBasicEntityManager<AnimalCustomEntity, UniqueLongIdentifier> {

  public List<AnimalCustomEntity> getByType(AnimalTypeCustomEntity type) {
    return listFromNamedQuery("getAnimalsByTypeCustom", newNamedParameters().add("type", type));
  }

  public AnimalCustomEntity getByName(String name) {
    return getFromNamedQuery("getAnimalsByNameCustom", newNamedParameters().add("name", name));
  }

  public List<PersonCustomEntity> getPersonsHaveTypeOfAnimal(AnimalTypeCustomEntity type) {
    String jpqlQuery = "select a.person from AnimalCustomEntity a where a.type like :type";
    return listFromJpqlString(jpqlQuery, newNamedParameters().add("type", type),
        PersonCustomEntity.class);
  }

  public long updateAnimalName(AnimalCustomEntity animalCustomEntity) {
    return updateFromNamedQuery("updateAnimalNameCustom",
        newNamedParameters().add("id", UniqueLongIdentifier.from(animalCustomEntity.getId()))
            .add("name", animalCustomEntity.getName()));
  }

  public long deleteAnimalsByType(AnimalTypeCustomEntity type) {
    return deleteFromNamedQuery("deleteAnimalsByTypeCustom", newNamedParameters().add("type", type));
  }
}