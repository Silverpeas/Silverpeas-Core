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
package org.silverpeas.persistence.repository.jpa;

import com.silverpeas.annotation.Service;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.persistence.repository.jpa.model.Animal;
import org.silverpeas.persistence.repository.jpa.model.AnimalType;
import org.silverpeas.persistence.repository.jpa.model.Equipment;
import org.silverpeas.persistence.repository.jpa.model.Person;
import org.silverpeas.persistence.repository.jpa.repository.AnimalRepository;
import org.silverpeas.persistence.repository.jpa.repository.EquipmentRepository;
import org.silverpeas.persistence.repository.jpa.repository.PersonRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@Service
@Transactional(propagation = Propagation.SUPPORTS)
public class JpaEntityServiceTest {

  @Inject
  private PersonRepository personRepository;

  @Inject
  private AnimalRepository animalRepository;

  @Inject
  private EquipmentRepository equipmentRepository;

  @PostConstruct
  private void initialize() {
    personRepository.setMaximumItemsInClause(2);
    animalRepository.setMaximumItemsInClause(2);
  }

  public void flush() {
    personRepository.flush();
  }

  public List<Equipment> getAllEquiments() {
    return equipmentRepository.getAll();
  }

  public Equipment getEquipmentById(String id) {
    return equipmentRepository.getById(id);
  }

  public List<Person> getAllPersons() {
    return personRepository.getAll();
  }

  public Person getPersonById(String id) {
    return personRepository.getById(id);
  }

  public List<Person> getPersonById(String... id) {
    return personRepository.getById(id);
  }

  public List<Animal> getAllAnimals() {
    return animalRepository.getAll();
  }

  public Animal getAnimalById(String id) {
    return animalRepository.getById(id);
  }

  public List<Animal> getAnimalById(String... id) {
    return animalRepository.getById(id);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public List<Person> save(final OperationContext context, final Person... person) {
    return personRepository.save(context, person);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public List<Animal> save(final OperationContext context, final Animal... animal) {
    return animalRepository.save(context, animal);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Person save(final OperationContext context, final Person person) {
    return personRepository.save(context, person);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Animal save(final OperationContext context, final Animal animal) {
    return animalRepository.save(context, animal);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(final Person... person) {
    personRepository.delete(person);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void delete(final Animal... animal) {
    animalRepository.delete(animal);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long deletePersonById(final String... personIds) {
    return personRepository.deleteById(personIds);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long deleteAnimalById(final String... animalIds) {
    return animalRepository.deleteById(animalIds);
  }

  public Person getPersonsByFirstName(String name) {
    return personRepository.getByFirstName(name);
  }

  public List<Person> getPersonsByLastName(String name) {
    return personRepository.getByLastName(name);
  }

  public List<Animal> getAnimalsByLastNameOfPerson(String lastName) {
    return personRepository.getAnimalByLastNameOfPerson(lastName);
  }

  public List<Animal> getAnimalsByType(AnimalType type) {
    return animalRepository.getByType(type);
  }

  public Animal getAnimalsByName(String name) {
    return animalRepository.getByName(name);
  }

  public List<Person> getPersonsHaveTypeOfAnimal(AnimalType type) {
    return animalRepository.getPersonsHaveTypeOfAnimal(type);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long updateAnimalNamesByType(AnimalType type) {
    return animalRepository.updateAnimalNamesByType(type);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long deleteAnimalsByType(AnimalType type) {
    return animalRepository.deleteAnimalsByType(type);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long updatePersonFirstNamesHavingAtLeastOneAnimal() {
    return personRepository.updatePersonFirstNamesHavingAtLeastOneAnimal();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long deletePersonFirstNamesHavingAtLeastOneAnimal() {
    return personRepository.deletePersonFirstNamesHavingAtLeastOneAnimal();
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public long badUpdateMissingLastUpdatedBy() {
    return personRepository.badUpdateMissingLastUpdatedBy();
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public long badUpdateMissingLastUpdateDate() {
    return personRepository.badUpdateMissingLastUpdateDate();
  }


  @Transactional(propagation = Propagation.REQUIRED)
  public long badUpdateMissingVersionManagement() {
    return personRepository.badUpdateMissingVersionManagement();
  }
}