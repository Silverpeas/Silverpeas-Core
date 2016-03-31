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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalTypeCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.PersonCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository
    .AnimalCustomEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository
    .EquipmentCustomEntityRepository;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.EquipmentCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.repository.PersonCustomEntityRepository;

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
public class JpaCustomEntityServiceTest {

  @Inject
  private PersonCustomEntityRepository personCustomEntityRepository;

  @Inject
  private AnimalCustomEntityRepository animalCustomEntityRepository;

  @Inject
  private EquipmentCustomEntityRepository equipmentCustomEntityRepository;

  @PostConstruct
  private void initialize() {
    personCustomEntityRepository.setMaximumItemsInClause(2);
    animalCustomEntityRepository.setMaximumItemsInClause(2);
  }

  public void flush() {
    personCustomEntityRepository.flush();
  }

  public List<EquipmentCustomEntity> getAllEquiments() {
    return equipmentCustomEntityRepository.getAll();
  }

  public EquipmentCustomEntity getEquipmentById(String id) {
    return equipmentCustomEntityRepository.getById(id);
  }

  public List<PersonCustomEntity> getAllPersons() {
    return personCustomEntityRepository.getAll();
  }

  public PersonCustomEntity getPersonById(String id) {
    return personCustomEntityRepository.getById(id);
  }

  public List<PersonCustomEntity> getPersonById(String... id) {
    return personCustomEntityRepository.getById(id);
  }

  public List<AnimalCustomEntity> getAllAnimals() {
    return animalCustomEntityRepository.getAll();
  }

  public AnimalCustomEntity getAnimalById(String id) {
    return animalCustomEntityRepository.getById(id);
  }

  public List<AnimalCustomEntity> getAnimalById(String... id) {
    return animalCustomEntityRepository.getById(id);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public List<PersonCustomEntity> save(final PersonCustomEntity... personCustomEntity) {
    return personCustomEntityRepository.save(personCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public List<AnimalCustomEntity> save(final AnimalCustomEntity... animalCustomEntity) {
    return animalCustomEntityRepository.save(animalCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public PersonCustomEntity save(final PersonCustomEntity personCustomEntity) {
    return personCustomEntityRepository.save(personCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public AnimalCustomEntity save(final AnimalCustomEntity animalCustomEntity) {
    return animalCustomEntityRepository.save(animalCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void delete(final PersonCustomEntity... personCustomEntity) {
    personCustomEntityRepository.delete(personCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public void delete(final AnimalCustomEntity... animalCustomEntity) {
    animalCustomEntityRepository.delete(animalCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deletePersonById(final String... personIds) {
    return personCustomEntityRepository.deleteById(personIds);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deleteAnimalById(final String... animalIds) {
    return animalCustomEntityRepository.deleteById(animalIds);
  }

  public PersonCustomEntity getPersonsByFirstName(String name) {
    return personCustomEntityRepository.getByFirstName(name);
  }

  public List<PersonCustomEntity> getPersonsByLastName(String name) {
    return personCustomEntityRepository.getByLastName(name);
  }

  public List<AnimalCustomEntity> getAnimalsByLastNameOfPerson(String lastName) {
    return personCustomEntityRepository.getAnimalByLastNameOfPerson(lastName);
  }

  public List<AnimalCustomEntity> getAnimalsByType(AnimalTypeCustomEntity type) {
    return animalCustomEntityRepository.getByType(type);
  }

  public AnimalCustomEntity getAnimalsByName(String name) {
    return animalCustomEntityRepository.getByName(name);
  }

  public List<PersonCustomEntity> getPersonsHaveTypeOfAnimal(AnimalTypeCustomEntity type) {
    return animalCustomEntityRepository.getPersonsHaveTypeOfAnimal(type);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long updateAnimalName(AnimalCustomEntity animalCustomEntity) {
    return animalCustomEntityRepository.updateAnimalName(animalCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deleteAnimalsByType(AnimalTypeCustomEntity type) {
    return animalCustomEntityRepository.deleteAnimalsByType(type);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long updatePersonFirstNameHavingAtLeastOneAnimal(PersonCustomEntity personCustomEntity) {
    return personCustomEntityRepository
        .updatePersonFirstNameHavingAtLeastOneAnimal(personCustomEntity);
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long deletePersonFirstNamesHavingAtLeastOneAnimal() {
    return personCustomEntityRepository.deletePersonFirstNamesHavingAtLeastOneAnimal();
  }

  @Transactional(Transactional.TxType.REQUIRED)
  public long badUpdateMissingLastUpdatedBy() {
    return personCustomEntityRepository.badUpdateMissingLastUpdatedBy();
  }


  @Transactional(Transactional.TxType.REQUIRED)
  public long badUpdateMissingLastUpdateDate() {
    return personCustomEntityRepository.badUpdateMissingLastUpdateDate();
  }


  @Transactional(Transactional.TxType.REQUIRED)
  public long badUpdateMissingVersionManagement() {
    return personCustomEntityRepository.badUpdateMissingVersionManagement();
  }
}