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
package org.silverpeas.core.persistence.datasource.repository.jpa;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.hamcrest.MatcherAssert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Animal;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.AnimalType;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Equipment;
import org.silverpeas.core.persistence.datasource.repository.jpa.model.Person;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.stub.StubbedOrganizationController;
import org.silverpeas.core.util.ServiceProvider;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This class of tests are verified :
 * - Entity
 * - EntityRepository
 * - Service and transactions
 * <p>
 * Entities :
 * - Person (Uuid identifier, has a bag of animal without cascade behaviour)
 * - Animal (Unique Id identifier, attached to a person and has a bag of equipment with {@link
 * javax.persistence.CascadeType#ALL} behaviour)
 * - Equipment (Uuid identifier, attached to an animal)
 * <p>
 * User: Yohann Chastagnier
 * Date: 20/11/13
 */
@RunWith(Arquillian.class)
public class SilverpeasJpaEntityRepositoryIT {

  private JpaEntityServiceTest jpaEntityServiceTest;

  public static final String TABLES_CREATION =
      "/org/silverpeas/core/persistence/datasource/create_table.sql";
  private static final Operation PERSON_SET_UP = Operations.insertInto("test_persons")
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
  private static final Operation ANIMAL_SET_UP = Operations.insertInto("test_animals")
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
  private static final Operation EQUIPEMENT_SET_UP = Operations.insertInto("test_equipments")
      .columns("id", "name", "animalId", "createDate", "createdBy", "lastUpdateDate",
          "lastUpdatedBy", "version")
      .values("equipment_1", "necklace", 2L, "2013-11-21 09:57:30.003", "1",
          "2013-11-22 22:00:50.006", "2", 10L).build();
  private static final Operation UNIQUE_ID_SET_UP =
      Operations.insertInto("UniqueId").columns("maxId", "tableName").values(9, "test_animals")
          .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLES_CREATION)
      .loadInitialDataSetFrom(PERSON_SET_UP, ANIMAL_SET_UP, EQUIPEMENT_SET_UP, UNIQUE_ID_SET_UP);

  @Before
  public void setup() {
    CacheServiceProvider.clearAllThreadCaches();
    jpaEntityServiceTest = ServiceProvider.getService(JpaEntityServiceTest.class);
    when(StubbedOrganizationController.getMock().getUserDetail(anyString()))
        .thenAnswer(invocation -> {
          UserDetail user = new UserDetail();
          String id = (String) invocation.getArguments()[0];
          try {
            Integer.parseInt(id);
            user.setId(id);
            return user;
          } catch (Exception e) {
            return null;
          }
        });
    OperationContext.fromUser("0");
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SilverpeasJpaEntityRepositoryIT.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures()
        .addStubbedOrganizationController()
        .addPublicationTemplateFeatures()
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
    assertThat(person.getCreatorId(), is("1"));
    assertThat(person.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdaterId(), is(person.getCreatorId()));
    assertThat(person.getLastUpdateDate(), is(person.getCreationDate()));
    assertThat(person.getVersion(), is(0L));
    assertThat(person.getAnimals(), hasSize(1));
    Animal personAnimal = person.getAnimals().get(0);
    assertThat(personAnimal.getId(), is("1"));
    assertThat(personAnimal.getType(), is(AnimalType.cat));
    assertThat(personAnimal.getName(), is("Blacky"));
    assertThat(personAnimal.getCreatorId(), is("1"));
    assertThat(personAnimal.getCreationDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(personAnimal.getLastUpdaterId(), is("2"));
    assertThat(personAnimal.getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-22 22:00:50.006")));
    assertThat(personAnimal.getVersion(), is(2L));

    person = jpaEntityServiceTest.getPersonById("person_2");
    assertThat(person, notNullValue());
    assertThat(person.getFirstName(), is("Nicolas"));
    assertThat(person.getLastName(), is("Eysseric"));
    assertThat(person.getCreatorId(), is("1"));
    assertThat(person.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdaterId(), is(person.getCreatorId()));
    assertThat(person.getLastUpdateDate(), is(person.getCreationDate()));
    assertThat(person.getAnimals(), hasSize(2));
    assertThat(person.getVersion(), is(0L));

    person = jpaEntityServiceTest.getPersonById("person_3");
    assertThat(person, notNullValue());
    assertThat(person.getFirstName(), is("Miguel"));
    assertThat(person.getLastName(), is("Moquillon"));
    assertThat(person.getCreatorId(), is("2"));
    assertThat(person.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdaterId(), is("10"));
    assertThat(person.getLastUpdateDate(), is(Timestamp.valueOf("2013-11-22 22:00:50.006")));
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
    assertThat(animal.getCreatorId(), is("1"));
    assertThat(animal.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdaterId(), is("2"));
    assertThat(animal.getLastUpdateDate(), is(Timestamp.valueOf("2013-11-22 22:00:50.006")));
    assertThat(animal.getVersion(), is(2L));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getPerson().getId(), is("person_1"));
    assertThat(animal.getPerson().getFirstName(), is("Yohann"));
    assertThat(animal.getPerson().getLastName(), is("Chastagnier"));
    assertThat(animal.getPerson().getCreatorId(), is("1"));
    assertThat(animal.getPerson().getCreationDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdaterId(), is("1"));
    assertThat(animal.getPerson().getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(1));

    animal = jpaEntityServiceTest.getAnimalById("2");
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.dog));
    assertThat(animal.getName(), is("Bagels"));
    assertThat(animal.getCreatorId(), is("10"));
    assertThat(animal.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdaterId(), is("10"));
    assertThat(animal.getLastUpdateDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getVersion(), is(0L));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getPerson().getId(), is("person_2"));
    assertThat(animal.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animal.getPerson().getLastName(), is("Eysseric"));
    assertThat(animal.getPerson().getCreatorId(), is("1"));
    assertThat(animal.getPerson().getCreationDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdaterId(), is("1"));
    assertThat(animal.getPerson().getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getVersion(), is(0L));
    assertThat(animal.getPerson().getAnimals(), hasSize(2));

    animal = jpaEntityServiceTest.getAnimalById("3");
    assertThat(animal, notNullValue());
    assertThat(animal.getType(), is(AnimalType.bird));
    assertThat(animal.getName(), is("Titi"));
    assertThat(animal.getCreatorId(), is("10"));
    assertThat(animal.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getLastUpdaterId(), is("10"));
    assertThat(animal.getLastUpdateDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson(), notNullValue());
    assertThat(animal.getVersion(), is(0L));
    assertThat(animal.getPerson().getId(), is("person_2"));
    assertThat(animal.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animal.getPerson().getLastName(), is("Eysseric"));
    assertThat(animal.getPerson().getCreatorId(), is("1"));
    assertThat(animal.getPerson().getCreationDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(animal.getPerson().getLastUpdaterId(), is("1"));
    assertThat(animal.getPerson().getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
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
    OperationContext.fromUser((User)null);
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe");
    jpaEntityServiceTest.save(newPerson);
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert3() {
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe");
    newPerson.createdBy("   ");
    jpaEntityServiceTest.save(newPerson);
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
    person.lastUpdatedBy("");
    jpaEntityServiceTest.save(person);
  }

  @Test
  public void entityGetUpdateCloneBehaviour() {
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));

    // Get
    Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person.getId(), notNullValue());
    assertThat(person.getCreatorId(), is("1"));
    assertThat(person.getCreationDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getLastUpdaterId(), is("1"));
    assertThat(person.getLastUpdateDate(), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person.getVersion(), is(0L));

    // Update
    person.lastUpdatedBy("26");
    Person personUpdated = jpaEntityServiceTest.save(person);
    assertThat(personUpdated, not(sameInstance(person)));
    assertThat(personUpdated.getId(), notNullValue());
    assertThat(person.getCreatorId(), is("1"));
    assertThat(person.getCreationDate(), is(person.getCreationDate()));
    assertThat(personUpdated.getLastUpdaterId(), is("26"));
    assertThat(personUpdated.getLastUpdateDate(), notNullValue());
    assertThat(personUpdated.getVersion(), is(1L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));

    // Clone
    Person personCloned = personUpdated.clone();
    assertThat(personCloned.getId(), nullValue());
    assertThat(personCloned.getFirstName(), is(personUpdated.getFirstName()));
    assertThat(personCloned.getLastName(), is(personUpdated.getLastName()));
    assertThat(personCloned.getCreatorId(), nullValue());
    assertThat(personCloned.getCreationDate(), nullValue());
    assertThat(personCloned.getLastUpdaterId(), nullValue());
    assertThat(personCloned.getLastUpdateDate(), nullValue());
    assertThat(personCloned.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(5));

    // Save clone
    personCloned.createdBy("26");
    Person personSaveResult = jpaEntityServiceTest.save(personCloned);
    assertThat(personSaveResult, sameInstance(personCloned));
    Person personClonedReloaded = jpaEntityServiceTest.getPersonById(personCloned.getId());
    assertThat(personClonedReloaded, not(sameInstance(personCloned)));
    assertThat(personClonedReloaded.getId(), notNullValue());
    assertThat(personClonedReloaded.getFirstName(), is(personCloned.getFirstName()));
    assertThat(personClonedReloaded.getLastName(), is(personCloned.getLastName()));
    assertThat(personClonedReloaded.getCreatorId(), is(personCloned.getCreatorId()));
    assertThat(personClonedReloaded.getCreationDate().getTime(),
        is(personCloned.getCreationDate().getTime()));
    assertThat(personClonedReloaded.getLastUpdaterId(), is(personCloned.getLastUpdaterId()));
    assertThat(personClonedReloaded.getLastUpdateDate().getTime(),
        is(personCloned.getLastUpdateDate().getTime()));
    assertThat(personClonedReloaded.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(6));
  }

  @Test
  public void savePerson() {
    Date createdAndLastUpdateDate = new Date();
    Person newPerson = new Person().setFirstName("Aurore").setLastName("Allibe")
        .createdBy(User.getById("200"), createdAndLastUpdateDate)
        .updatedBy(User.getById("400"), createdAndLastUpdateDate);
    assertThat(newPerson.getVersion(), is(0L));
    assertThat(newPerson.getId(), nullValue());
    Person personSaveResult = jpaEntityServiceTest.save(newPerson);
    assertThat(personSaveResult, sameInstance(newPerson));
    assertThat(newPerson.getId(), notNullValue());
    Person personCreated = jpaEntityServiceTest.getPersonById(newPerson.getId());
    assertThat(personCreated, notNullValue());
    assertThat(personCreated, not(sameInstance(newPerson)));
    assertThat(personCreated, is(personCreated));
    assertThat(personCreated.getCreatorId(), is("200"));
    assertThat(personCreated.getCreationDate().getTime(), is(newPerson.getCreationDate().getTime()));
    assertThat(personCreated.getLastUpdaterId(), is("400"));
    assertThat(personCreated.getLastUpdateDate().getTime(), is(personCreated.getCreationDate().getTime()));
    assertThat(personCreated.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      jpaEntityServiceTest.save(
          new Person().setFirstName("FirstName_" + i).setLastName("LastName_" + i)
              .createdBy("26"),
          new Person().setFirstName("FirstName#" + i).setLastName("LastName#" + i)
              .createdBy("26"));
    }
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void saveAsUsualAPersonWithIdSetManually() {
    final String manualId = "id_that_will_be_changed";
    Person newPerson =
        new Person().setId(manualId).setFirstName("Aurore").setLastName("Allibe")
            .createdBy("400");
    assertThat(newPerson.getVersion(), is(0L));
    assertThat(newPerson.getId(), is(manualId));
    Person personSaveResult = jpaEntityServiceTest.save(newPerson);
    assertThat(personSaveResult, sameInstance(newPerson));
    assertThat(newPerson.getId(), notNullValue());
    assertThat(personSaveResult.getId(), not(is(manualId)));
    Person personCreated = jpaEntityServiceTest.getPersonById(manualId);
    assertThat(personCreated, nullValue());
    personCreated = jpaEntityServiceTest.getPersonById(personSaveResult.getId());
    assertThat(personCreated, notNullValue());
    assertThat(personCreated, not(sameInstance(newPerson)));
    assertThat(personCreated, is(personCreated));
    assertThat(personCreated.getCreatorId(), is("400"));
    assertThat(personCreated.getCreationDate().getTime(),
        is(personSaveResult.getCreationDate().getTime()));
    assertThat(personCreated.getLastUpdaterId(), is(personCreated.getCreatorId()));
    assertThat(personCreated.getLastUpdateDate().getTime(),
        is(personCreated.getCreationDate().getTime()));
    assertThat(personCreated.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      jpaEntityServiceTest.save(
          new Person().setFirstName("FirstName_" + i).setLastName("LastName_" + i)
              .createdBy("26"),
          new Person().setFirstName("FirstName#" + i).setLastName("LastName#" + i)
              .createdBy("26")
      );
    }
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void verifyLastUpdateDateWhenSavingPerson() {
    Person person1 = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person1, notNullValue());
    assertThat(person1.getLastUpdaterId(), is("1"));
    assertThat(person1.getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(person1.getVersion(), is(0L));
    assertThat(person1.hasBeenModified(), is(false));

    // Putting a current requester for the next actions of this test.
    User user = mock(User.class);
    when(user.getId()).thenReturn("400");
    ((SessionCacheService) CacheServiceProvider.getSessionCacheService())
        .newSessionCache(user);
    OperationContext.fromUser(user);

    // No changes
    Person personSaveResult = jpaEntityServiceTest.save(person1);
    assertThat(personSaveResult.getLastUpdaterId(), is("1"));
    assertThat(personSaveResult.getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
    assertThat(personSaveResult.getVersion(), is(0L));
    assertThat(personSaveResult.hasBeenModified(), is(false));

    // Change specifically the last update date
    person1 = jpaEntityServiceTest.getPersonById("person_1");
    person1.markAsModified();
    assertThat(person1.getLastUpdateDate(),
        is(Timestamp.valueOf("2013-11-21 09:57:30.004")));
    personSaveResult = jpaEntityServiceTest.save(person1);
    assertThat(personSaveResult.getLastUpdaterId(), is("400"));
    assertThat(personSaveResult.getLastUpdateDate(),
        greaterThan(Timestamp.valueOf("2013-11-21 09:57:30.004")));
    assertThat(personSaveResult.getVersion(), is(1L));
    assertThat(personSaveResult.hasBeenModified(), is(true));
  }

  @Test
  public void saveAnimal() {
    Person person = jpaEntityServiceTest.getPersonById("person_3");

    Animal newAnimal =
        new Animal().setType(AnimalType.cat).setName("Pilou").createdBy("400").setPerson(person)
            .lastUpdatedBy("500");
    assertThat(newAnimal.getId(), nullValue());
    Animal animalSaveResult = jpaEntityServiceTest.save(newAnimal);
    assertThat(animalSaveResult, sameInstance(newAnimal));
    assertThat(newAnimal.getId(), notNullValue());
    assertThat(newAnimal.getVersion(), is(0L));
    Animal animalCreated = jpaEntityServiceTest.getAnimalById(newAnimal.getId());
    assertThat(animalCreated, notNullValue());
    assertThat(animalCreated, not(sameInstance(newAnimal)));
    assertThat(animalCreated, is(animalCreated));
    assertThat(animalCreated.getId(), is("10"));
    assertThat(animalCreated.getCreatorId(), is("400"));
    assertThat(animalCreated.getCreationDate().getTime(),
        is(newAnimal.getCreationDate().getTime()));
    assertThat(animalCreated.getLastUpdaterId(), is("500"));
    assertThat(animalCreated.getVersion(), is(0L));
    MatcherAssert.assertThat(jpaEntityServiceTest.getAllAnimals(), hasSize(6));

    for (long i = 0; i < 50; i++) {
      Animal animal1 = new Animal().setType(AnimalType.bird).setName("Name_" + i).createdBy("26")
          .setPerson(person).lastUpdatedBy("500");
      Animal animal2 = new Animal().setType(AnimalType.cat).setName("Name#" + i).createdBy("26")
          .setPerson(person).lastUpdatedBy("500");
      Animal animal3 =
          new Animal().setType(AnimalType.dog).setName("Name-" + i).createdBy("26")
              .setPerson(person).lastUpdatedBy("500");
      jpaEntityServiceTest.save(animal1, animal2, animal3);
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
  public void deleteAnimalsByType() {
    Animal animal = jpaEntityServiceTest.getAnimalById("1");
    assertThat(animal, notNullValue());
    assertThat(jpaEntityServiceTest.deleteAnimalsByType(animal.getType()), is(1L));
    assertThat(jpaEntityServiceTest.deleteAnimalsByType(animal.getType()), is(0L));
    assertThat(jpaEntityServiceTest.deleteAnimalsByType(AnimalType.frog), is(0L));
    Animal animalReloaded = jpaEntityServiceTest.getAnimalById(animal.getId());
    assertThat(animalReloaded, nullValue());
  }

}