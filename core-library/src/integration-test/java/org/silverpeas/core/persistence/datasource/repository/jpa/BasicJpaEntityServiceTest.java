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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalTypeBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.PersonBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository.AnimalBasicEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository.EquipmentBasicEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.EquipmentBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository.PersonBasicEntityRepository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class BasicJpaEntityServiceTest {

  @Inject
  private PersonBasicEntityRepository personBasicEntityRepository;

  @Inject
  private AnimalBasicEntityRepository animalBasicEntityRepository;

  @Inject
  private EquipmentBasicEntityRepository equipmentBasicEntityRepository;

  @PostConstruct
  private void initialize() {
    personBasicEntityRepository.setMaximumItemsInClause(2);
    animalBasicEntityRepository.setMaximumItemsInClause(2);
  }

  public void flush() {
    personBasicEntityRepository.flush();
  }

  public List<EquipmentBasicEntity> getAllEquiments() {
    return equipmentBasicEntityRepository.getAll();
  }

  public EquipmentBasicEntity getEquipmentById(String id) {
    return equipmentBasicEntityRepository.getById(id);
  }

  public List<PersonBasicEntity> getAllPersons() {
    return personBasicEntityRepository.getAll();
  }

  public PersonBasicEntity getPersonById(String id) {
    return personBasicEntityRepository.getById(id);
  }

  public List<PersonBasicEntity> getPersonById(String... id) {
    return personBasicEntityRepository.getById(id);
  }

  public List<AnimalBasicEntity> getAllAnimals() {
    return animalBasicEntityRepository.getAll();
  }

  public AnimalBasicEntity getAnimalById(String id) {
    return animalBasicEntityRepository.getById(id);
  }

  public List<AnimalBasicEntity> getAnimalById(String... id) {
    return animalBasicEntityRepository.getById(id);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public List<PersonBasicEntity> save(final PersonBasicEntity... personBasicEntity) {
    return personBasicEntityRepository.save(personBasicEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public List<AnimalBasicEntity> save(final AnimalBasicEntity... animalBasicEntity) {
    return animalBasicEntityRepository.save(animalBasicEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public PersonBasicEntity save(final PersonBasicEntity personBasicEntity) {
    return personBasicEntityRepository.save(personBasicEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public AnimalBasicEntity save(final AnimalBasicEntity animalBasicEntity) {
    return animalBasicEntityRepository.save(animalBasicEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void delete(final PersonBasicEntity... personBasicEntity) {
    personBasicEntityRepository.delete(personBasicEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void delete(final AnimalBasicEntity... animalBasicEntity) {
    animalBasicEntityRepository.delete(animalBasicEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deletePersonById(final String... personIds) {
    return personBasicEntityRepository.deleteById(personIds);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deleteAnimalById(final String... animalIds) {
    return animalBasicEntityRepository.deleteById(animalIds);
  }

  public PersonBasicEntity getPersonsByFirstName(String name) {
    return personBasicEntityRepository.getByFirstName(name);
  }

  public List<PersonBasicEntity> getPersonsByLastName(String name) {
    return personBasicEntityRepository.getByLastName(name);
  }

  public List<AnimalBasicEntity> getAnimalsByLastNameOfPerson(String lastName) {
    return personBasicEntityRepository.getAnimalByLastNameOfPerson(lastName);
  }

  public List<AnimalBasicEntity> getAnimalsByType(AnimalTypeBasicEntity type) {
    return animalBasicEntityRepository.getByType(type);
  }

  public AnimalBasicEntity getAnimalsByName(String name) {
    return animalBasicEntityRepository.getByName(name);
  }

  public List<PersonBasicEntity> getPersonsHaveTypeOfAnimal(AnimalTypeBasicEntity type) {
    return animalBasicEntityRepository.getPersonsHaveTypeOfAnimal(type);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deleteAnimalsByType(AnimalTypeBasicEntity type) {
    return animalBasicEntityRepository.deleteAnimalsByType(type);
  }
}