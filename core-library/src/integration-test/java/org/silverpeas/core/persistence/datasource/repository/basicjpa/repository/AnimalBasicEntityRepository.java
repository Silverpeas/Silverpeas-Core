/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.persistence.datasource.repository.basicjpa.repository;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalTypeBasicEntity;
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
public class AnimalBasicEntityRepository
    extends BasicJpaEntityRepository<AnimalBasicEntity> {

  public List<AnimalBasicEntity> getByType(AnimalTypeBasicEntity type) {
    return listFromNamedQuery("getAnimalsByTypeCustom", newNamedParameters().add("type", type));
  }

  public AnimalBasicEntity getByName(String name) {
    return getFromNamedQuery("getAnimalsByNameCustom", newNamedParameters().add("name", name));
  }

  public List<PersonBasicEntity> getPersonsHaveTypeOfAnimal(AnimalTypeBasicEntity type) {
    String jpqlQuery = "select a.person from AnimalBasicEntity a where a.type like :type";
    return listFromJpqlString(jpqlQuery, newNamedParameters().add("type", type),
        PersonBasicEntity.class);
  }

  public long deleteAnimalsByType(AnimalTypeBasicEntity type) {
    return deleteFromNamedQuery("deleteAnimalsByTypeCustom", newNamedParameters().add("type", type));
  }
}