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

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.persistence.repository.jpa.model.Animal;
import org.silverpeas.persistence.repository.jpa.model.AnimalType;
import org.silverpeas.persistence.repository.jpa.model.Equipment;
import org.silverpeas.persistence.repository.jpa.model.Person;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This class of tests are verified :
 * - Entity
 * - EntityRepository
 * - Service and transactions
 * <p/>
 * Entities :
 * - Person (Uuid identifier, has a bag of animal without cascade behaviour)
 * - Animal (Unique Id identifier, attached to a person and has a bag of equipment with {@link
 * javax.persistence.CascadeType#ALL} behaviour)
 * - Equipment (Uuid identifier, attached to an animal)
 * <p/>
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {"/spring-persistence.xml", "/spring-persistence-embedded-datasource.xml"})
public class AbstractJpaEntityRepositoryTest {

  private static ReplacementDataSet dataSet;

  @Inject
  private JpaEntityServiceTest jpaEntityServiceTest;

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    final FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(
        AbstractJpaEntityRepositoryTest.class.getClassLoader()
            .getResourceAsStream("org/silverpeas/persistence/persistence-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @Before
  public void generalSetUp() throws Exception {
    final IDatabaseConnection myConnection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(myConnection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @Test
  public void getAll() {
    List<Person> persons = jpaEntityServiceTest.getAllPersons();
    assertThat(persons, hasSize(3));
    List<Animal> animals = jpaEntityServiceTest.getAllAnimals();
    assertThat(animals, hasSize(3));
    List<Equipment> equipments = jpaEntityServiceTest.getAllEquiments();
    assertThat(equipments, hasSize(1));
  }

  @Test
  public void getPerson() {
    Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person, not(sameInstance(jpaEntityServiceTest.getPersonById("person_1"))));
    assertThat(person, notNullValue());
    assertThat(person.getFirstName(), is("Yohann"));
    assertThat(person.getLastName(), is("Chastagnier"));
    assertThat(person.getCreatedBy(), is("1"));
    assertThat(person.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdatedBy(), nullValue());
    assertThat(person.getLastUpdateDate(), nullValue());
    assertThat(person.getVersion(), is(0L));
    assertThat(person.getAnimals(), hasSize(1));
    Animal personAnimal = person.getAnimals().get(0);
    assertThat(personAnimal.getId(), is("1"));
    assertThat(personAnimal.getType(), is(AnimalType.cat));
    assertThat(personAnimal.getName(), is("Blacky"));
    assertThat(personAnimal.getCreatedBy(), is("1"));
    assertThat(personAnimal.getCreateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(personAnimal.getLastUpdatedBy(), is("2"));
    assertThat(personAnimal.getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-22 22:00:50.006")));
    assertThat(personAnimal.getVersion(), is(2L));

    person = jpaEntityServiceTest.getPersonById("person_2");
    assertThat(person, notNullValue());
    assertThat(person.getFirstName(), is("Nicolas"));
    assertThat(person.getLastName(), is("Eysseric"));
    assertThat(person.getCreatedBy(), is("1"));
    assertThat(person.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdatedBy(), nullValue());
    assertThat(person.getLastUpdateDate(), nullValue());
    assertThat(person.getAnimals(), hasSize(2));
    assertThat(person.getVersion(), is(0L));

    person = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person, notNullValue());
    assertThat(person.getFirstName(), is("Miguel"));
    assertThat(person.getLastName(), is("Moquillon"));
    assertThat(person.getCreatedBy(), is("2"));
    assertThat(person.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdatedBy(), is("10"));
    assertThat(person.getLastUpdateDate(), is((Date) Timestamp.valueOf("2013-11-22 22:00:50.006")));
    assertThat(person.getAnimals(), empty());
    assertThat(person.getVersion(), is(3L));

    assertThat(jpaEntityServiceTest.getPersonById("person_that_doesnt_exist"), nullValue());
  }

  @Test
  public void getPersons() {
    List<Person> persons = jpaEntityServiceTest
        .getPersonById("person_1", "person_11", "person_3", "person_12", "person_2", "person_13");
    assertThat(persons, hasSize(3));
  }

  @Test
  public void getAnimal() {
    Animal animal = jpaEntityServiceTest.getAnimalById("1");
    assertThat(animal, not(sameInstance(jpaEntityServiceTest.getAnimalById("1"))));
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.cat));
    assertThat(animal.getName(), is("Blacky"));
    assertThat(animal.getCreatedBy(), is("1"));
    assertThat(animal.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdatedBy(), is("2"));
    assertThat(animal.getLastUpdateDate(), is((Date) Timestamp.valueOf("2013-11-22 22:00:50.006")));
    assertThat(animal.getVersion(), is(2L));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getPerson().getId(), is("person_1"));
    assertThat(animal.getPerson().getFirstName(), is("Yohann"));
    assertThat(animal.getPerson().getLastName(), is("Chastagnier"));
    assertThat(animal.getPerson().getCreatedBy(), is("1"));
    assertThat(animal.getPerson().getCreateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdatedBy(), nullValue());
    assertThat(animal.getPerson().getLastUpdateDate(), nullValue());
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(1));

    animal = jpaEntityServiceTest.getAnimalById("2");
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.dog));
    assertThat(animal.getName(), is("Bagels"));
    assertThat(animal.getCreatedBy(), is("10"));
    assertThat(animal.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdatedBy(), nullValue());
    assertThat(animal.getLastUpdateDate(), nullValue());
    assertThat(animal.getVersion(), is(0L));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getPerson().getId(), is("person_2"));
    assertThat(animal.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animal.getPerson().getLastName(), is("Eysseric"));
    assertThat(animal.getPerson().getCreatedBy(), is("1"));
    assertThat(animal.getPerson().getCreateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdatedBy(), nullValue());
    assertThat(animal.getPerson().getLastUpdateDate(), nullValue());
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(2));

    animal = jpaEntityServiceTest.getAnimalById("3");
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.bird));
    assertThat(animal.getName(), is("Titi"));
    assertThat(animal.getCreatedBy(), is("10"));
    assertThat(animal.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdatedBy(), nullValue());
    assertThat(animal.getLastUpdateDate(), nullValue());
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getVersion(), is(0L));
    assertThat(animal.getPerson().getId(), is("person_2"));
    assertThat(animal.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animal.getPerson().getLastName(), is("Eysseric"));
    assertThat(animal.getPerson().getCreatedBy(), is("1"));
    assertThat(animal.getPerson().getCreateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdatedBy(), nullValue());
    assertThat(animal.getPerson().getLastUpdateDate(), nullValue());
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(2));

    // Animal doesn't exist
    assertThat(jpaEntityServiceTest.getAnimalById("25"), nullValue());
  }

  @Test
  public void getAnimals() {
    List<Animal> animals = jpaEntityServiceTest.getAnimalById("1", "11", "3", "12", "2", "13");
    assertThat(animals, hasSize(3));
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert() {
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe");
    jpaEntityServiceTest.save(null, newPerson);
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert2() {
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe");
    jpaEntityServiceTest.save(OperationContext.createInstance(), newPerson);
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert3() {
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe");
    jpaEntityServiceTest.save(createOperationContext(null), newPerson);
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert4() {
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe");
    jpaEntityServiceTest.save(createOperationContext("   "), newPerson);
  }

  /**
   * Last updated by information is missing on update.
   */
  @Test(expected = Exception.class)
  public void savePersonBadUpdate() {
    Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person, notNullValue());
    person.setFirstName("FirstName");
    person.setLastName("LastName");
    jpaEntityServiceTest.save(createOperationContext(""), person);
  }

  @Test
  public void entityGetUpdateCloneBehaviour() {
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(3));

    // Get
    Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person.getId(), notNullValue());
    assertThat(person.getCreatedBy(), is("1"));
    assertThat(person.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdatedBy(), nullValue());
    assertThat(person.getLastUpdateDate(), nullValue());
    assertThat(person.getVersion(), is(0L));

    // Update
    person.setLastUpdatedBy("26");
    Person personUpdated = jpaEntityServiceTest.save(createOperationContext("26"), person);
    assertThat(personUpdated, not(sameInstance(person)));
    assertThat(personUpdated.getId(), notNullValue());
    assertThat(person.getCreatedBy(), is("1"));
    assertThat(person.getCreateDate(), is(person.getCreateDate()));
    assertThat(personUpdated.getLastUpdatedBy(), is("26"));
    assertThat(personUpdated.getLastUpdateDate(), notNullValue());
    assertThat(personUpdated.getVersion(), is(1L));
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(3));

    // Clone
    Person personCloned = personUpdated.clone();
    assertThat(personCloned.getId(), nullValue());
    assertThat(personCloned.getFirstName(), is(personUpdated.getFirstName()));
    assertThat(personCloned.getLastName(), is(personUpdated.getLastName()));
    assertThat(personCloned.getCreatedBy(), nullValue());
    assertThat(personCloned.getCreateDate(), nullValue());
    assertThat(personCloned.getLastUpdatedBy(), nullValue());
    assertThat(personCloned.getLastUpdateDate(), nullValue());
    assertThat(personCloned.getVersion(), is(0L));
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(3));

    // Save clone
    personCloned.setCreatedBy("26");
    Person personSaveResult = jpaEntityServiceTest.save(createOperationContext("26"), personCloned);
    assertThat(personSaveResult, sameInstance(personCloned));
    Person personClonedReloaded = jpaEntityServiceTest.getPersonById(personCloned.getId());
    assertThat(personClonedReloaded, not(sameInstance(personCloned)));
    assertThat(personClonedReloaded.getId(), notNullValue());
    assertThat(personClonedReloaded.getFirstName(), is(personCloned.getFirstName()));
    assertThat(personClonedReloaded.getLastName(), is(personCloned.getLastName()));
    assertThat(personClonedReloaded.getCreatedBy(), is(personCloned.getCreatedBy()));
    assertThat(personClonedReloaded.getCreateDate(), is(personCloned.getCreateDate()));
    assertThat(personClonedReloaded.getLastUpdatedBy(), nullValue());
    assertThat(personClonedReloaded.getLastUpdateDate(), nullValue());
    assertThat(personClonedReloaded.getVersion(), is(0L));
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(4));
  }

  @Test
  public void savePerson() {
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe").setCreatedBy("200")
        .setLastUpdatedBy("not_registred_I_hope");
    assertThat(newPerson.getVersion(), is(0L));
    assertThat(newPerson.getId(), nullValue());
    Person personSaveResult = jpaEntityServiceTest.save(createOperationContext("400"), newPerson);
    assertThat(personSaveResult, sameInstance(newPerson));
    assertThat(newPerson.getId(), notNullValue());
    Person personCreated = jpaEntityServiceTest.getPersonById(newPerson.getId());
    assertThat(personCreated, notNullValue());
    assertThat(personCreated, not(sameInstance(newPerson)));
    assertThat(personCreated, is(personCreated));
    assertThat(personCreated.getCreatedBy(), is("400"));
    assertThat(personCreated.getCreateDate(), is(newPerson.getCreateDate()));
    assertThat(personCreated.getLastUpdatedBy(), nullValue());
    assertThat(personCreated.getLastUpdateDate(), nullValue());
    assertThat(personCreated.getVersion(), is(0L));
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(4));

    for (int i = 0; i < 50; i++) {
      jpaEntityServiceTest.save(createOperationContext("26"),
          new Person().setFirstName("FirstName_" + i).setLastName("LastName_" + i)
              .setCreatedBy("38").setLastUpdatedBy("not_registred_I_hope"),
          new Person().setFirstName("FirstName#" + i).setLastName("LastName#" + i)
              .setCreatedBy("69").setLastUpdatedBy("not_registred_I_hope"));
    }
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(104));
  }

  @Test
  public void saveAnimal() {
    Person person = jpaEntityServiceTest.getPersonById("person_3");

    Animal newAnimal =
        new Animal().setType(AnimalType.cat).setName("Pilou").setCreatedBy("26").setPerson(person)
            .setLastUpdatedBy("not_registred_I_hope");
    assertThat(newAnimal.getId(), nullValue());
    Animal animalSaveResult = jpaEntityServiceTest.save(createOperationContext("400"), newAnimal);
    assertThat(animalSaveResult, sameInstance(newAnimal));
    assertThat(newAnimal.getId(), notNullValue());
    assertThat(newAnimal.getVersion(), is(0L));
    Animal animalCreated = jpaEntityServiceTest.getAnimalById(newAnimal.getId());
    assertThat(animalCreated, notNullValue());
    assertThat(animalCreated, not(sameInstance(newAnimal)));
    assertThat(animalCreated, is(animalCreated));
    assertThat(animalCreated.getId(), is("10"));
    assertThat(animalCreated.getCreatedBy(), is("400"));
    assertThat(animalCreated.getCreateDate(), is(newAnimal.getCreateDate()));
    assertThat(animalCreated.getLastUpdatedBy(), nullValue());
    assertThat(animalCreated.getLastUpdateDate(), nullValue());
    assertThat(animalCreated.getVersion(), is(0L));
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(4));

    for (long i = 0; i < 50; i++) {
      Animal animal1 = new Animal().setType(AnimalType.bird).setName("Name_" + i).setCreatedBy("38")
          .setPerson(person).setLastUpdatedBy("not_registred_I_hope");
      Animal animal2 = new Animal().setType(AnimalType.cat).setName("Name#" + i).setCreatedBy("381")
          .setPerson(person).setLastUpdatedBy("not_registred_I_hope");
      Animal animal3 =
          new Animal().setType(AnimalType.dog).setName("Name-" + i).setCreatedBy("3811")
              .setPerson(person).setLastUpdatedBy("not_registred_I_hope");
      jpaEntityServiceTest.save(createOperationContext("26"), animal1, animal2, animal3);
      assertThat(animal1.getId(), is(String.valueOf(11 + (i * 3))));
      assertThat(animal2.getId(), is(String.valueOf(12 + (i * 3))));
      assertThat(animal3.getId(), is(String.valueOf(13 + (i * 3))));
    }
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(154));
  }

  @Test
  public void deleteEntity() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(1));
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(3));
    jpaEntityServiceTest.delete(jpaEntityServiceTest.getAnimalById("1"));
    assertThat(jpaEntityServiceTest.getAnimalById("1"), nullValue());
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(2));
    jpaEntityServiceTest
        .delete(jpaEntityServiceTest.getAnimalById("3"), jpaEntityServiceTest.getAnimalById("2"),
            new Animal());
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(0));
    // Verifying here that cascade process has been performed ...
    assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(0));

    // Persons
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(3));
    jpaEntityServiceTest.delete(jpaEntityServiceTest.getPersonById("person_1"));
    assertThat(jpaEntityServiceTest.getPersonById("person_1"), nullValue());
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(2));
    jpaEntityServiceTest.delete(jpaEntityServiceTest.getPersonById("person_3"),
        jpaEntityServiceTest.getPersonById("person_2"), new Person());
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(0));
  }

  @Test
  public void deleteEntityById() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(1));
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(3));
    long nbDeleted = jpaEntityServiceTest.deleteAnimalById("1");
    assertThat(jpaEntityServiceTest.getAnimalById("1"), nullValue());
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(2));
    assertThat(nbDeleted, is(1L));
    nbDeleted = jpaEntityServiceTest.deleteAnimalById("38", "26", "3", "27", "38", "2", "36", "22");
    assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(0));
    assertThat(nbDeleted, is(2L));
    // Verifying here that cascade process is not performed in this deletion way ...
    assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(1));

    // Persons
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(3));
    nbDeleted = jpaEntityServiceTest.deletePersonById("person_1");
    assertThat(jpaEntityServiceTest.getPersonById("person_1"), nullValue());
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(2));
    assertThat(nbDeleted, is(1L));
    nbDeleted = jpaEntityServiceTest
        .deletePersonById("person_38", "person_26", "person_3", "person_27", "person_38",
            "person_2", "person_36", "person_22");
    assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(0));
    assertThat(nbDeleted, is(2L));
  }

  @Test
  public void getPersonsByLastName() {
    assertThat(jpaEntityServiceTest.getPersonsByLastName("Eysseric"), hasSize(1));
    assertThat(jpaEntityServiceTest.getPersonsByLastName("Nicolas"), hasSize(0));
    assertThat(jpaEntityServiceTest.getPersonsByFirstName("Nicolas"), hasSize(1));
    assertThat(jpaEntityServiceTest.getPersonsByFirstName("Eysseric"), hasSize(0));
  }

  @Test
  public void getAnimalsOfPersonByLastName() {
    assertThat(jpaEntityServiceTest.getAnimalsByLastNameOfPerson("Eysseric"), hasSize(2));
    assertThat(jpaEntityServiceTest.getAnimalsByLastNameOfPerson("eysseric"), hasSize(0));
    assertThat(jpaEntityServiceTest.getPersonsByLastName("Moquillon"), hasSize(1));
    assertThat(jpaEntityServiceTest.getAnimalsByLastNameOfPerson("Moquillon"), hasSize(0));
  }

  @Test
  public void getAnimalsByName() {
    assertThat(jpaEntityServiceTest.getAnimalsByName("Titi"), hasSize(1));
    assertThat(jpaEntityServiceTest.getAnimalsByName("titi"), hasSize(0));
  }

  @Test
  public void getAnimalsByType() {
    assertThat(jpaEntityServiceTest.getAnimalsByType(AnimalType.bird), hasSize(1));
    assertThat(jpaEntityServiceTest.getAnimalsByType(AnimalType.frog), hasSize(0));
  }

  @Test
  public void getPersonsHaveTypeOfAnimal() {
    assertThat(jpaEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalType.bird), hasSize(1));
    assertThat(jpaEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalType.frog), hasSize(0));
  }

  @Test
  public void updateAnimalNamesByType() {
    Animal animal = jpaEntityServiceTest.getAnimalById("1");
    assertThat(animal, notNullValue());
    assertThat(animal.getLastUpdatedBy(), is("2"));
    assertThat(animal.getLastUpdateDate(), notNullValue());
    assertThat(animal.getVersion(), is(2L));
    assertThat(jpaEntityServiceTest.updateAnimalNamesByType(animal.getType()), is(1L));
    assertThat(jpaEntityServiceTest.updateAnimalNamesByType(AnimalType.frog), is(0L));
    Animal animalReloaded = jpaEntityServiceTest.getAnimalById(animal.getId());
    assertThat(animalReloaded, notNullValue());
    assertThat(animalReloaded.getName(), is(animal.getName() + "_toto"));
    // In that case of update, last update by, last update date and version must also be handled
    assertThat(animalReloaded.getLastUpdatedBy(), is("dummy"));
    Date lastUpdateDate = animalReloaded.getLastUpdateDate();
    assertThat(lastUpdateDate, greaterThan(animal.getLastUpdateDate()));
    assertThat(animalReloaded.getVersion(), is(3L));
    assertThat(jpaEntityServiceTest.updateAnimalNamesByType(animal.getType()), is(1L));
    animalReloaded = jpaEntityServiceTest.getAnimalById(animal.getId());
    assertThat(animalReloaded, notNullValue());
    assertThat(animalReloaded.getName(), is(animal.getName() + "_toto_toto"));
    // Technical data
    assertThat(animalReloaded.getLastUpdatedBy(), is("dummy"));
    assertThat(animalReloaded.getLastUpdateDate(), greaterThanOrEqualTo(lastUpdateDate));
    assertThat(animalReloaded.getVersion(), is(4L));
  }

  @Test
  public void deleteAnimalsByType() {
    Animal animal = jpaEntityServiceTest.getAnimalById("1");
    assertThat(animal, notNullValue());
    assertThat(jpaEntityServiceTest.deleteAnimalsByType(animal.getType()), is(1L));
    assertThat(jpaEntityServiceTest.deleteAnimalsByType(animal.getType()), is(0L));
    assertThat(jpaEntityServiceTest.deleteAnimalsByType(AnimalType.frog), is(0L));
    Animal animalReloaded = jpaEntityServiceTest.getAnimalById(animal.getId());
    assertThat(animalReloaded, nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void badUpdateMissingLastUpdatedBy() {
    jpaEntityServiceTest.badUpdateMissingLastUpdatedBy();
  }

  @Test(expected = IllegalArgumentException.class)
  public void badUpdateMissingLastUpdateDate() {
    jpaEntityServiceTest.badUpdateMissingLastUpdateDate();
  }

  @Test(expected = IllegalArgumentException.class)
  public void badUpdateMissingVersionManagement() {
    jpaEntityServiceTest.badUpdateMissingVersionManagement();
  }

  @Test
  public void updatePersonFirstNamesHavingAtLeastOneAnimal() {
    Person person1 = jpaEntityServiceTest.getPersonById("person_1");
    Person person2 = jpaEntityServiceTest.getPersonById("person_2");
    Person person3 = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person1, notNullValue());
    assertThat(person1.getLastUpdatedBy(), nullValue());
    assertThat(person1.getLastUpdateDate(), nullValue());
    assertThat(person1.getVersion(), is(0L));
    assertThat(person2, notNullValue());
    assertThat(person3, notNullValue());
    assertThat(jpaEntityServiceTest.updatePersonFirstNamesHavingAtLeastOneAnimal(), is(2L));
    Person person1Reloaded = jpaEntityServiceTest.getPersonById("person_1");
    Person person2Reloaded = jpaEntityServiceTest.getPersonById("person_2");
    Person person3Reloaded = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person1Reloaded, notNullValue());
    assertThat(person1Reloaded.getFirstName(), is(person1.getFirstName() + "_updated"));
    // In that case of update, last update by, last update date and version must also be handled
    assertThat(person1Reloaded.getLastUpdatedBy(), is("dummy"));
    assertThat(person1Reloaded.getLastUpdateDate(), notNullValue());
    assertThat(person1Reloaded.getVersion(), is(1L));
    assertThat(person2Reloaded, notNullValue());
    assertThat(person2Reloaded.getFirstName(), is(person2.getFirstName() + "_updated"));
    assertThat(person3Reloaded, notNullValue());
    assertThat(person3Reloaded.getFirstName(), is(person3.getFirstName()));
  }

  @Test
  public void deletePersonFirstNamesHavingAtLeastOneAnimal() {
    Person person1 = jpaEntityServiceTest.getPersonById("person_1");
    Person person2 = jpaEntityServiceTest.getPersonById("person_2");
    Person person3 = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person1, notNullValue());
    assertThat(person2, notNullValue());
    assertThat(person3, notNullValue());
    assertThat(jpaEntityServiceTest.deletePersonFirstNamesHavingAtLeastOneAnimal(), is(2L));
    assertThat(jpaEntityServiceTest.deletePersonFirstNamesHavingAtLeastOneAnimal(), is(0L));
    person1 = jpaEntityServiceTest.getPersonById("person_1");
    person2 = jpaEntityServiceTest.getPersonById("person_2");
    person3 = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person1, nullValue());
    assertThat(person2, nullValue());
    assertThat(person3, notNullValue());
  }

  /**
   * Create a user.
   * @param userId
   * @return
   */
  private static UserDetail createUser(String userId) {
    UserDetail user = new UserDetail();
    user.setId(userId);
    return user;
  }

  /**
   * Create a user.
   * @param userId
   * @return
   */
  private static OperationContext createOperationContext(String userId) {
    return OperationContext.fromUser(createUser(userId));
  }
}