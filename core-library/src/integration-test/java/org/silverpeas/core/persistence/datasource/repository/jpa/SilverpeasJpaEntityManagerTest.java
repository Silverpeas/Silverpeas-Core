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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.hamcrest.MatcherAssert;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.datasource.repository.OperationContext;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Animal;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.AnimalType;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Equipment;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Person;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

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
@RunWith(Arquillian.class)
public class SilverpeasJpaEntityManagerTest {

  private JpaEntityServiceTest jpaEntityServiceTest;

  public static final String TABLES_CREATION =
      "/org/silverpeas/core/persistence/datasource/create_table.sql";
  public static final Operation PERSON_SET_UP = Operations.insertInto("test_persons")
      .columns("id", "firstName", "lastName", "createDate", "createdBy", "lastUpdateDate",
          "lastUpdatedBy", "version")
      .values("person_1", "Yohann", "Chastagnier", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_2", "Nicolas", "Eysseric", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_3", "Miguel", "Moquillon", "2013-11-21 09:57:30.003", "2",
          "2013-11-22 22:00:50.006", "10", 3L)
      .values("person_1000", "firstName", "lastName", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_1001", "firstName", "lastName", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L).build();
  public static final Operation ANIMAL_SET_UP = Operations.insertInto("test_animals")
      .columns("id", "type", "name", "personId", "createDate", "createdBy", "lastUpdateDate",
          "lastUpdatedBy", "version")
      .values(1L, "cat", "Blacky", "person_1", "2013-11-21 09:57:30.003", "1",
          "2013-11-22 22:00:50.006", "2", 2L)
      .values(2L, "dog", "Bagels", "person_2", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .values(3L, "bird", "Titi", "person_2", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .values(1000L, "type", "name", "person_1000", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L)
      .values(1001L, "type", "name", "person_1001", "2013-11-21 09:57:30.003", "10",
          "2013-11-21 09:57:30.003", "10", 0L).build();
  public static final Operation EQUIPEMENT_SET_UP = Operations.insertInto("test_equipments")
      .columns("id", "name", "animalId", "createDate", "createdBy", "lastUpdateDate",
          "lastUpdatedBy", "version")
      .values("equipment_1", "necklace", 2L, "2013-11-21 09:57:30.003", "1",
          "2013-11-22 22:00:50.006", "2", 10L).build();
  public static final Operation UNIQUE_ID_SET_UP =
      Operations.insertInto("UniqueId").columns("maxId", "tableName").values(9, "test_animals")
          .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLES_CREATION)
      .loadInitialDataSetFrom(PERSON_SET_UP, ANIMAL_SET_UP, EQUIPEMENT_SET_UP, UNIQUE_ID_SET_UP);

  @Before
  public void setup() {
    jpaEntityServiceTest = ServiceProvider.getService(JpaEntityServiceTest.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SilverpeasJpaEntityManagerTest.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .addAsResource("org/silverpeas/core/persistence/datasource/create_table.sql")
        .testFocusedOn((warBuilder) -> warBuilder.addPackages(true,
            "org.silverpeas.core.persistence.datasource.repository.jpa"))
        .build();
  }

  @Test
  public void getAll() {
    List<Person> persons = jpaEntityServiceTest.getAllPersons();
    assertThat(persons, hasSize(5));
    List<Animal> animals = jpaEntityServiceTest.getAllAnimals();
    assertThat(animals, hasSize(5));
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
    assertThat(person.getLastUpdatedBy(), is(person.getCreatedBy()));
    assertThat(person.getLastUpdateDate(), is(person.getCreateDate()));
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
    assertThat(person.getLastUpdatedBy(), is(person.getCreatedBy()));
    assertThat(person.getLastUpdateDate(), is(person.getCreateDate()));
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
    assertThat(animal.getPerson().getLastUpdatedBy(), is("1"));
    assertThat(animal.getPerson().getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(1));

    animal = jpaEntityServiceTest.getAnimalById("2");
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.dog));
    assertThat(animal.getName(), is("Bagels"));
    assertThat(animal.getCreatedBy(), is("10"));
    assertThat(animal.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdatedBy(), is("10"));
    assertThat(animal.getLastUpdateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getVersion(), is(0L));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getPerson().getId(), is("person_2"));
    assertThat(animal.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animal.getPerson().getLastName(), is("Eysseric"));
    assertThat(animal.getPerson().getCreatedBy(), is("1"));
    assertThat(animal.getPerson().getCreateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdatedBy(), is("1"));
    assertThat(animal.getPerson().getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(2));

    animal = jpaEntityServiceTest.getAnimalById("3");
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.bird));
    assertThat(animal.getName(), is("Titi"));
    assertThat(animal.getCreatedBy(), is("10"));
    assertThat(animal.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdatedBy(), is("10"));
    assertThat(animal.getLastUpdateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getVersion(), is(0L));
    assertThat(animal.getPerson().getId(), is("person_2"));
    assertThat(animal.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animal.getPerson().getLastName(), is("Eysseric"));
    assertThat(animal.getPerson().getCreatedBy(), is("1"));
    assertThat(animal.getPerson().getCreateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdatedBy(), is("1"));
    assertThat(animal.getPerson().getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
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
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));

    // Get
    Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person.getId(), notNullValue());
    assertThat(person.getCreatedBy(), is("1"));
    assertThat(person.getCreateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdatedBy(), is("1"));
    assertThat(person.getLastUpdateDate(), is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
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
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));

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
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));

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
    assertThat(personClonedReloaded.getLastUpdatedBy(), is(personCloned.getLastUpdatedBy()));
    assertThat(personClonedReloaded.getLastUpdateDate(), is(personCloned.getLastUpdateDate()));
    assertThat(personClonedReloaded.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(6));
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
    assertThat(personCreated.getLastUpdatedBy(), is(personCreated.getCreatedBy()));
    assertThat(personCreated.getLastUpdateDate(), is(personCreated.getCreateDate()));
    assertThat(personCreated.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      jpaEntityServiceTest.save(createOperationContext("26"),
          new Person().setFirstName("FirstName_" + i).setLastName("LastName_" + i)
              .setCreatedBy("38").setLastUpdatedBy("not_registred_I_hope"),
          new Person().setFirstName("FirstName#" + i).setLastName("LastName#" + i)
              .setCreatedBy("69").setLastUpdatedBy("not_registred_I_hope"));
    }
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void savePersonWithIdSetManually() {
    Person newPerson =
        new Person().setId("id_that_will_be_changed").setFirstName("Aurore").setLastName("Allibe")
            .setCreatedBy("200").setLastUpdatedBy("not_registred_I_hope");
    assertThat(newPerson.getVersion(), is(0L));
    assertThat(newPerson.getId(), is("id_that_will_be_changed"));
    Person personSaveResult = jpaEntityServiceTest.save(createOperationContext("400"), newPerson);
    assertThat(personSaveResult, not(sameInstance(newPerson)));
    assertThat(newPerson.getId(), notNullValue());
    Person personCreated = jpaEntityServiceTest.getPersonById(newPerson.getId());
    assertThat(personCreated, nullValue());
    personCreated = jpaEntityServiceTest.getPersonById(personSaveResult.getId());
    assertThat(personCreated, notNullValue());
    assertThat(personCreated, not(sameInstance(newPerson)));
    assertThat(personCreated, is(personCreated));
    assertThat(personCreated.getCreatedBy(), is("400"));
    assertThat(newPerson.getCreateDate(), nullValue());
    assertThat(personCreated.getCreateDate(), is(personSaveResult.getCreateDate()));
    assertThat(personCreated.getLastUpdatedBy(), is(personCreated.getCreatedBy()));
    assertThat(personCreated.getLastUpdateDate(), is(personCreated.getCreateDate()));
    assertThat(personCreated.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      jpaEntityServiceTest.save(createOperationContext("26"),
          new Person().setFirstName("FirstName_" + i).setLastName("LastName_" + i)
              .setCreatedBy("38").setLastUpdatedBy("not_registred_I_hope"),
          new Person().setFirstName("FirstName#" + i).setLastName("LastName#" + i)
              .setCreatedBy("69").setLastUpdatedBy("not_registred_I_hope")
      );
    }
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void verifyLastUpdateDateWhenSavingPerson() {
    Person person1 = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person1, notNullValue());
    assertThat(person1.getLastUpdatedBy(), is("1"));
    assertThat(person1.getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person1.getVersion(), is(0L));
    assertThat(person1.hasBeenModified(), is(false));

    // No changes
    Person personSaveResult = jpaEntityServiceTest.save(createOperationContext("400"), person1);
    assertThat(personSaveResult.getLastUpdatedBy(), is("1"));
    assertThat(personSaveResult.getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(personSaveResult.getVersion(), is(0L));
    assertThat(personSaveResult.hasBeenModified(), is(false));

    // Change specifically the last update date
    person1 = jpaEntityServiceTest.getPersonById("person_1");
    person1.markAsModified();
    assertThat(person1.getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.004")));
    personSaveResult = jpaEntityServiceTest.save(createOperationContext("400"), person1);
    assertThat(personSaveResult.getLastUpdatedBy(), is("400"));
    assertThat(personSaveResult.getLastUpdateDate(),
        greaterThan(Timestamp.valueOf("2013-11-21 09:57:30.004")));
    assertThat(personSaveResult.getVersion(), is(1L));
    assertThat(personSaveResult.hasBeenModified(), is(true));
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
    assertThat(animalCreated.getLastUpdatedBy(), is(animalCreated.getCreatedBy()));
    assertThat(animalCreated.getLastUpdateDate(), is(animalCreated.getCreateDate()));
    assertThat(animalCreated.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(6));

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
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(156));
  }

  @Test
  public void deleteEntity() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(1));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(5));
    jpaEntityServiceTest.delete(jpaEntityServiceTest.getAnimalById("1"));
    assertThat(jpaEntityServiceTest.getAnimalById("1"), nullValue());
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(4));
    jpaEntityServiceTest
        .delete(jpaEntityServiceTest.getAnimalById("3"), jpaEntityServiceTest.getAnimalById("2"),
            new Animal());
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(2));
    // Verifying here that cascade process has been performed ...
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(0));

    // Persons
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));
    jpaEntityServiceTest.delete(jpaEntityServiceTest.getPersonById("person_1"));
    assertThat(jpaEntityServiceTest.getPersonById("person_1"), nullValue());
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(4));
    jpaEntityServiceTest.delete(jpaEntityServiceTest.getPersonById("person_3"),
        jpaEntityServiceTest.getPersonById("person_2"), new Person());
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(2));
  }

  @Test
  public void deleteEntityById() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(1));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(5));
    long nbDeleted = jpaEntityServiceTest.deleteAnimalById("1");
    assertThat(jpaEntityServiceTest.getAnimalById("1"), nullValue());
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(4));
    assertThat(nbDeleted, is(1L));
    nbDeleted = jpaEntityServiceTest.deleteAnimalById("38", "26", "3", "27", "38", "2", "36", "22");
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(2));
    assertThat(nbDeleted, is(2L));
    // Verifying here that cascade process is not performed in this deletion way ...
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllEquiments(), hasSize(1));

    // Persons
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));
    nbDeleted = jpaEntityServiceTest.deletePersonById("person_1");
    assertThat(jpaEntityServiceTest.getPersonById("person_1"), nullValue());
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(4));
    assertThat(nbDeleted, is(1L));
    nbDeleted = jpaEntityServiceTest
        .deletePersonById("person_38", "person_26", "person_3", "person_27", "person_38",
            "person_2", "person_36", "person_22");
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(2));
    assertThat(nbDeleted, is(2L));
  }

  @Test
  public void getPersonsByLastName() {
    MatcherAssert.assertThat(jpaEntityServiceTest.getPersonsByLastName("Eysseric"), hasSize(1));
    MatcherAssert.assertThat(jpaEntityServiceTest.getPersonsByLastName("Nicolas"), hasSize(0));
  }

  @Test
  public void getPersonsByFirstName() {
    assertThat(jpaEntityServiceTest.getPersonsByFirstName("Nicolas"), notNullValue());
    assertThat(jpaEntityServiceTest.getPersonsByFirstName("Eysseric"), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getPersonsByFirstNameNotUnique() {
    jpaEntityServiceTest.getPersonsByFirstName("firstName");
  }

  @Test
  public void getAnimalsOfPersonByLastName() {
    MatcherAssert.assertThat(jpaEntityServiceTest.getAnimalsByLastNameOfPerson("Eysseric"), hasSize(2));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAnimalsByLastNameOfPerson("eysseric"), hasSize(0));
    MatcherAssert.assertThat(jpaEntityServiceTest.getPersonsByLastName("Moquillon"), hasSize(1));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAnimalsByLastNameOfPerson("Moquillon"), hasSize(0));
  }

  @Test
  public void getAnimalsByName() {
    assertThat(jpaEntityServiceTest.getAnimalsByName("Titi"), notNullValue());
    assertThat(jpaEntityServiceTest.getAnimalsByName("titi"), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getAnimalsByNameNotUnique() {
    jpaEntityServiceTest.getAnimalsByName("name");
  }

  @Test
  public void getAnimalsByType() {
    MatcherAssert.assertThat(jpaEntityServiceTest.getAnimalsByType(AnimalType.bird), hasSize(1));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAnimalsByType(AnimalType.frog), hasSize(0));
  }

  @Test
  public void getPersonsHaveTypeOfAnimal() {
    MatcherAssert.assertThat(jpaEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalType.bird), hasSize(1));
    MatcherAssert.assertThat(jpaEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalType.frog), hasSize(0));
  }

  @Test
  public void updateAnimalName() {
    Animal animal = jpaEntityServiceTest.getAnimalById("1");
    String previousName = animal.getName();
    assertThat(animal, notNullValue());
    assertThat(animal.getLastUpdatedBy(), is("2"));
    assertThat(animal.getLastUpdateDate(), notNullValue());
    assertThat(animal.getVersion(), is(2L));
    animal.setName(previousName + "_toto");
    assertThat(jpaEntityServiceTest.updateAnimalName(animal), is(1L));
    Animal animalReloaded = jpaEntityServiceTest.getAnimalById(animal.getId());
    assertThat(animalReloaded, notNullValue());
    assertThat(animalReloaded.getName(), is(previousName + "_toto"));
    // In that case of update, last update by, last update date and version must also be handled
    assertThat(animalReloaded.getLastUpdatedBy(), is("dummy"));
    Date lastUpdateDate = animalReloaded.getLastUpdateDate();
    assertThat(lastUpdateDate, greaterThan(animal.getLastUpdateDate()));
    assertThat(animalReloaded.getVersion(), is(3L));
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
    String previousPerson1Name = person1.getFirstName();
    String previousPerson2Name = person2.getFirstName();
    String previousPerson3Name = person3.getFirstName();
    assertThat(person1, notNullValue());
    assertThat(person1.getLastUpdatedBy(), is("1"));
    assertThat(person1.getLastUpdateDate(),
        is((Date) Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person1.getVersion(), is(0L));
    assertThat(person2, notNullValue());
    assertThat(person3, notNullValue());

    person1.setFirstName(previousPerson1Name + "_updated");
    person2.setFirstName(previousPerson2Name + "_updated");
    person3.setFirstName(previousPerson3Name + "_updated");
    assertThat(jpaEntityServiceTest.updatePersonFirstNameHavingAtLeastOneAnimal(person1), is(1L));
    assertThat(jpaEntityServiceTest.updatePersonFirstNameHavingAtLeastOneAnimal(person2), is(1L));
    assertThat(jpaEntityServiceTest.updatePersonFirstNameHavingAtLeastOneAnimal(person3), is(0L));
    Person person1Reloaded = jpaEntityServiceTest.getPersonById("person_1");
    Person person2Reloaded = jpaEntityServiceTest.getPersonById("person_2");
    Person person3Reloaded = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person1Reloaded, notNullValue());
    assertThat(person1Reloaded.getFirstName(), is(previousPerson1Name + "_updated"));
    // In that case of update, last update by, last update date and version must also be handled
    assertThat(person1Reloaded.getLastUpdatedBy(), is("dummy"));
    assertThat(person1Reloaded.getLastUpdateDate(), greaterThan(person1.getLastUpdateDate()));
    assertThat(person1Reloaded.getVersion(), is(1L));
    assertThat(person2Reloaded, notNullValue());
    assertThat(person2Reloaded.getFirstName(), is(previousPerson2Name + "_updated"));
    assertThat(person3Reloaded, notNullValue());
    assertThat(person3Reloaded.getFirstName(), is(previousPerson3Name));
  }

  @Test
  public void deletePersonFirstNamesHavingAtLeastOneAnimal() {
    Person person1 = jpaEntityServiceTest.getPersonById("person_1");
    Person person2 = jpaEntityServiceTest.getPersonById("person_2");
    Person person3 = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person1, notNullValue());
    assertThat(person2, notNullValue());
    assertThat(person3, notNullValue());
    assertThat(jpaEntityServiceTest.deletePersonFirstNamesHavingAtLeastOneAnimal(), is(4L));
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
   * @param userId the identifier of the user to create.
   * @return the created user.
   */
  private static UserDetail createUser(String userId) {
    UserDetail user = new UserDetail();
    user.setId(userId);
    return user;
  }

  /**
   * Create an operation context.
   * @param userId the identifier of the user behind the operation/
   * @return the context of the operation.
   */
  private static OperationContext createOperationContext(String userId) {
    return OperationContext.fromUser(createUser(userId));
  }
}