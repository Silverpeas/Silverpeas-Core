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
package org.silverpeas.core.persistence.datasource.repository.basicjpa;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.hamcrest.Matchers;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalTypeCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.PersonCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.EquipmentCustomEntity;
import org.silverpeas.core.persistence.datasource.repository.jpa.JpaCustomEntityServiceTest;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class SilverpeasJpaCustomEntityManagerTest {

  private JpaCustomEntityServiceTest jpaCustomEntityServiceTest;

  public static final String TABLES_CREATION =
      "/org/silverpeas/core/persistence/datasource/basicjpa/create_table.sql";
  public static final Operation PERSON_SET_UP = Operations.insertInto("test_persons")
      .columns("id", "firstName", "lastName")
      .values("person_1", "Yohann", "Chastagnier")
      .values("person_2", "Nicolas", "Eysseric")
      .values("person_3", "Miguel", "Moquillon")
      .values("person_1000", "firstName", "lastName")
      .values("person_1001", "firstName", "lastName")
      .build();
  public static final Operation ANIMAL_SET_UP = Operations.insertInto("test_animals")
      .columns("id", "type", "name", "personId")
      .values(1L, "cat", "Blacky", "person_1")
      .values(2L, "dog", "Bagels", "person_2")
      .values(3L, "bird", "Titi", "person_2")
      .values(1000L, "type", "name", "person_1000")
      .values(1001L, "type", "name", "person_1001")
      .build();
  public static final Operation EQUIPEMENT_SET_UP = Operations.insertInto("test_equipments")
      .columns("id", "name", "animalId")
      .values("equipment_1", "necklace", 2L)
      .build();
  public static final Operation UNIQUE_ID_SET_UP =
      Operations.insertInto("UniqueId").columns("maxId", "tableName").values(9, "test_animals")
          .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLES_CREATION)
      .loadInitialDataSetFrom(PERSON_SET_UP, ANIMAL_SET_UP, EQUIPEMENT_SET_UP, UNIQUE_ID_SET_UP);

  @Before
  public void setup() {
    jpaCustomEntityServiceTest = ServiceProvider.getService(JpaCustomEntityServiceTest.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SilverpeasJpaCustomEntityManagerTest.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures().testFocusedOn((warBuilder) -> {
          warBuilder.addClasses(JpaCustomEntityServiceTest.class);
          warBuilder.addPackages(true, "org.silverpeas.core.persistence.datasource.repository");
          warBuilder.addAsResource(
              "org/silverpeas/core/persistence/datasource/basicjpa/create_table.sql");
        }).build();
  }

  @Test
  public void getAll() {
    List<PersonCustomEntity> personCustomEntities = jpaCustomEntityServiceTest.getAllPersons();
    assertThat(personCustomEntities, hasSize(5));
    List<AnimalCustomEntity> animalCustomEntities = jpaCustomEntityServiceTest.getAllAnimals();
    assertThat(animalCustomEntities, hasSize(5));
    List<EquipmentCustomEntity> equipmentCustomEntities =
        jpaCustomEntityServiceTest.getAllEquiments();
    assertThat(equipmentCustomEntities, hasSize(1));
  }

  @Test
  public void getPerson() {
    PersonCustomEntity personCustomEntity = jpaCustomEntityServiceTest.getPersonById("person_1");
    assertThat(personCustomEntity,
        not(sameInstance(jpaCustomEntityServiceTest.getPersonById("person_1"))));
    assertThat(personCustomEntity, notNullValue());
    assertThat(personCustomEntity.getFirstName(), is("Yohann"));
    assertThat(personCustomEntity.getLastName(), is("Chastagnier"));
    assertThat(personCustomEntity.getAnimals(), hasSize(1));
    AnimalCustomEntity personAnimalCustomEntity = personCustomEntity.getAnimals().get(0);
    assertThat(personAnimalCustomEntity.getId(), is("1"));
    assertThat(personAnimalCustomEntity.getType(), Matchers.is(AnimalTypeCustomEntity.cat));
    assertThat(personAnimalCustomEntity.getName(), is("Blacky"));

    personCustomEntity = jpaCustomEntityServiceTest.getPersonById("person_2");
    assertThat(personCustomEntity, notNullValue());
    assertThat(personCustomEntity.getFirstName(), is("Nicolas"));
    assertThat(personCustomEntity.getLastName(), is("Eysseric"));
    assertThat(personCustomEntity.getAnimals(), hasSize(2));

    personCustomEntity = jpaCustomEntityServiceTest.getPersonById("person_3");
    assertThat(personCustomEntity, notNullValue());
    assertThat(personCustomEntity.getFirstName(), is("Miguel"));
    assertThat(personCustomEntity.getLastName(), is("Moquillon"));
    assertThat(personCustomEntity.getAnimals(), empty());

    assertThat(jpaCustomEntityServiceTest.getPersonById("person_that_doesnt_exist"), nullValue());
  }

  @Test
  public void getPersons() {
    List<PersonCustomEntity> personCustomEntities = jpaCustomEntityServiceTest
        .getPersonById("person_1", "person_11", "person_3", "person_12", "person_2", "person_13");
    assertThat(personCustomEntities, hasSize(3));
  }

  @Test
  public void getAnimal() {
    AnimalCustomEntity animalCustomEntity = jpaCustomEntityServiceTest.getAnimalById("1");
    assertThat(animalCustomEntity,
        not(sameInstance(jpaCustomEntityServiceTest.getAnimalById("1"))));
    assertThat(animalCustomEntity, notNullValue());
    assertThat(animalCustomEntity.getType(), is(AnimalTypeCustomEntity.cat));
    assertThat(animalCustomEntity.getName(), is("Blacky"));
    assertThat(animalCustomEntity.getPerson(), notNullValue());
    assertThat(animalCustomEntity.getPerson().getId(), is("person_1"));
    assertThat(animalCustomEntity.getPerson().getFirstName(), is("Yohann"));
    assertThat(animalCustomEntity.getPerson().getLastName(), is("Chastagnier"));
    assertThat(animalCustomEntity.getPerson().getAnimals(), hasSize(1));

    animalCustomEntity = jpaCustomEntityServiceTest.getAnimalById("2");
    assertThat(animalCustomEntity, notNullValue());
    assertThat(animalCustomEntity.getType(), is(AnimalTypeCustomEntity.dog));
    assertThat(animalCustomEntity.getName(), is("Bagels"));
    assertThat(animalCustomEntity.getPerson(), notNullValue());
    assertThat(animalCustomEntity.getPerson().getId(), is("person_2"));
    assertThat(animalCustomEntity.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animalCustomEntity.getPerson().getLastName(), is("Eysseric"));
    assertThat(animalCustomEntity.getPerson().getAnimals(), hasSize(2));

    animalCustomEntity = jpaCustomEntityServiceTest.getAnimalById("3");
    assertThat(animalCustomEntity, notNullValue());
    assertThat(animalCustomEntity.getType(), is(AnimalTypeCustomEntity.bird));
    assertThat(animalCustomEntity.getName(), is("Titi"));
    assertThat(animalCustomEntity.getPerson(), notNullValue());
    assertThat(animalCustomEntity.getPerson().getId(), is("person_2"));
    assertThat(animalCustomEntity.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animalCustomEntity.getPerson().getLastName(), is("Eysseric"));
    assertThat(animalCustomEntity.getPerson().getAnimals(), hasSize(2));

    // Animal doesn't exist
    assertThat(jpaCustomEntityServiceTest.getAnimalById("25"), nullValue());
  }

  @Test
  public void getAnimals() {
    List<AnimalCustomEntity> animalCustomEntities =
        jpaCustomEntityServiceTest.getAnimalById("1", "11", "3", "12", "2", "13");
    assertThat(animalCustomEntities, hasSize(3));
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert() {
    PersonCustomEntity newPersonCustomEntity =
        new PersonCustomEntity().setFirstName("Aurore").setLastName("Allibe");
    jpaCustomEntityServiceTest.save(null, newPersonCustomEntity);
  }

  @Test
  public void entityGetUpdateCloneBehaviour() {
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(5));

    // Get
    PersonCustomEntity personCustomEntity = jpaCustomEntityServiceTest.getPersonById("person_1");
    assertThat(personCustomEntity.getId(), notNullValue());

    // Update
    PersonCustomEntity personCustomEntityUpdated =
        jpaCustomEntityServiceTest.save(personCustomEntity);
    assertThat(personCustomEntityUpdated, not(sameInstance(personCustomEntity)));
    assertThat(personCustomEntityUpdated.getId(), notNullValue());
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(5));

    // Clone
    PersonCustomEntity personCustomEntityCloned = personCustomEntityUpdated.clone();
    assertThat(personCustomEntityCloned.getId(), nullValue());
    assertThat(personCustomEntityCloned.getFirstName(),
        is(personCustomEntityUpdated.getFirstName()));
    assertThat(personCustomEntityCloned.getLastName(), is(personCustomEntityUpdated.getLastName()));
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(5));

    // Save clone
    PersonCustomEntity personCustomEntitySaveResult =
        jpaCustomEntityServiceTest.save(personCustomEntityCloned);
    assertThat(personCustomEntitySaveResult, sameInstance(personCustomEntityCloned));
    PersonCustomEntity personCustomEntityClonedReloaded =
        jpaCustomEntityServiceTest.getPersonById(personCustomEntityCloned.getId());
    assertThat(personCustomEntityClonedReloaded, not(sameInstance(personCustomEntityCloned)));
    assertThat(personCustomEntityClonedReloaded.getId(), notNullValue());
    assertThat(personCustomEntityClonedReloaded.getFirstName(),
        is(personCustomEntityCloned.getFirstName()));
    assertThat(personCustomEntityClonedReloaded.getLastName(),
        is(personCustomEntityCloned.getLastName()));
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(6));
  }

  @Test
  public void savePerson() {
    PersonCustomEntity newPersonCustomEntity =
        new PersonCustomEntity().setFirstName("Aurore").setLastName("Allibe");
    assertThat(newPersonCustomEntity.getId(), nullValue());
    PersonCustomEntity personCustomEntitySaveResult =
        jpaCustomEntityServiceTest.save(newPersonCustomEntity);
    assertThat(personCustomEntitySaveResult, sameInstance(newPersonCustomEntity));
    assertThat(newPersonCustomEntity.getId(), notNullValue());
    PersonCustomEntity personCustomEntityCreated =
        jpaCustomEntityServiceTest.getPersonById(newPersonCustomEntity.getId());
    assertThat(personCustomEntityCreated, notNullValue());
    assertThat(personCustomEntityCreated, not(sameInstance(newPersonCustomEntity)));
    assertThat(personCustomEntityCreated, is(personCustomEntityCreated));
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      jpaCustomEntityServiceTest.save(
          new PersonCustomEntity().setFirstName("FirstName_" + i).setLastName("LastName_" + i),
          new PersonCustomEntity().setFirstName("FirstName#" + i).setLastName("LastName#" + i));
    }
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void savePersonWithIdSetManually() {
    PersonCustomEntity newPersonCustomEntity =
        new PersonCustomEntity().setId("id_that_will_be_changed").setFirstName("Aurore")
            .setLastName("Allibe");
    assertThat(newPersonCustomEntity.getId(), is("id_that_will_be_changed"));
    PersonCustomEntity personCustomEntitySaveResult =
        jpaCustomEntityServiceTest.save(newPersonCustomEntity);
    assertThat(personCustomEntitySaveResult, not(sameInstance(newPersonCustomEntity)));
    assertThat(newPersonCustomEntity.getId(), notNullValue());
    PersonCustomEntity personCustomEntityCreated =
        jpaCustomEntityServiceTest.getPersonById(newPersonCustomEntity.getId());
    assertThat(personCustomEntityCreated, nullValue());
    personCustomEntityCreated =
        jpaCustomEntityServiceTest.getPersonById(personCustomEntitySaveResult.getId());
    assertThat(personCustomEntityCreated, notNullValue());
    assertThat(personCustomEntityCreated, not(sameInstance(newPersonCustomEntity)));
    assertThat(personCustomEntityCreated, is(personCustomEntityCreated));
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      jpaCustomEntityServiceTest.save(
          new PersonCustomEntity().setFirstName("FirstName_" + i).setLastName("LastName_" + i),
          new PersonCustomEntity().setFirstName("FirstName#" + i).setLastName("LastName#" + i));
    }
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void saveAnimal() {
    PersonCustomEntity personCustomEntity = jpaCustomEntityServiceTest.getPersonById("person_3");

    AnimalCustomEntity newAnimalCustomEntity =
        new AnimalCustomEntity().setType(AnimalTypeCustomEntity.cat).setName("Pilou")
            .setPerson(personCustomEntity);
    assertThat(newAnimalCustomEntity.getId(), nullValue());
    AnimalCustomEntity animalCustomEntitySaveResult =
        jpaCustomEntityServiceTest.save(newAnimalCustomEntity);
    assertThat(animalCustomEntitySaveResult, sameInstance(newAnimalCustomEntity));
    assertThat(newAnimalCustomEntity.getId(), notNullValue());
    AnimalCustomEntity animalCustomEntityCreated =
        jpaCustomEntityServiceTest.getAnimalById(newAnimalCustomEntity.getId());
    assertThat(animalCustomEntityCreated, notNullValue());
    assertThat(animalCustomEntityCreated, not(sameInstance(newAnimalCustomEntity)));
    assertThat(animalCustomEntityCreated, is(animalCustomEntityCreated));
    assertThat(animalCustomEntityCreated.getId(), is("10"));
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(6));

    for (long i = 0; i < 50; i++) {
      AnimalCustomEntity animalCustomEntity1 =
          new AnimalCustomEntity().setType(AnimalTypeCustomEntity.bird).setName("Name_" + i)
              .setPerson(personCustomEntity);
      AnimalCustomEntity animalCustomEntity2 =
          new AnimalCustomEntity().setType(AnimalTypeCustomEntity.cat).setName("Name#" + i)
              .setPerson(personCustomEntity);
      AnimalCustomEntity animalCustomEntity3 =
          new AnimalCustomEntity().setType(AnimalTypeCustomEntity.dog).setName("Name-" + i)
              .setPerson(personCustomEntity);
      jpaCustomEntityServiceTest
          .save(animalCustomEntity1, animalCustomEntity2, animalCustomEntity3);
      assertThat(animalCustomEntity1.getId(), is(String.valueOf(11 + (i * 3))));
      assertThat(animalCustomEntity2.getId(), is(String.valueOf(12 + (i * 3))));
      assertThat(animalCustomEntity3.getId(), is(String.valueOf(13 + (i * 3))));
    }
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(156));
  }

  @Test
  public void deleteEntity() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    assertThat(jpaCustomEntityServiceTest.getAllEquiments(), hasSize(1));
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(5));
    jpaCustomEntityServiceTest.delete(jpaCustomEntityServiceTest.getAnimalById("1"));
    assertThat(jpaCustomEntityServiceTest.getAnimalById("1"), nullValue());
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(4));
    jpaCustomEntityServiceTest.delete(jpaCustomEntityServiceTest.getAnimalById("3"),
        jpaCustomEntityServiceTest.getAnimalById("2"), new AnimalCustomEntity());
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(2));
    // Verifying here that cascade process has been performed ...
    assertThat(jpaCustomEntityServiceTest.getAllEquiments(), hasSize(0));

    // Persons
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(5));
    jpaCustomEntityServiceTest.delete(jpaCustomEntityServiceTest.getPersonById("person_1"));
    assertThat(jpaCustomEntityServiceTest.getPersonById("person_1"), nullValue());
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(4));
    jpaCustomEntityServiceTest.delete(jpaCustomEntityServiceTest.getPersonById("person_3"),
        jpaCustomEntityServiceTest.getPersonById("person_2"), new PersonCustomEntity());
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(2));
  }

  @Test
  public void deleteEntityById() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    assertThat(jpaCustomEntityServiceTest.getAllEquiments(), hasSize(1));
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(5));
    long nbDeleted = jpaCustomEntityServiceTest.deleteAnimalById("1");
    assertThat(jpaCustomEntityServiceTest.getAnimalById("1"), nullValue());
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(4));
    assertThat(nbDeleted, is(1L));
    nbDeleted =
        jpaCustomEntityServiceTest.deleteAnimalById("38", "26", "3", "27", "38", "2", "36", "22");
    assertThat(jpaCustomEntityServiceTest.getAllAnimals(), hasSize(2));
    assertThat(nbDeleted, is(2L));
    // Verifying here that cascade process is not performed in this deletion way ...
    assertThat(jpaCustomEntityServiceTest.getAllEquiments(), hasSize(1));

    // Persons
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(5));
    nbDeleted = jpaCustomEntityServiceTest.deletePersonById("person_1");
    assertThat(jpaCustomEntityServiceTest.getPersonById("person_1"), nullValue());
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(4));
    assertThat(nbDeleted, is(1L));
    nbDeleted = jpaCustomEntityServiceTest
        .deletePersonById("person_38", "person_26", "person_3", "person_27", "person_38",
            "person_2", "person_36", "person_22");
    assertThat(jpaCustomEntityServiceTest.getAllPersons(), hasSize(2));
    assertThat(nbDeleted, is(2L));
  }

  @Test
  public void getPersonsByLastName() {
    assertThat(jpaCustomEntityServiceTest.getPersonsByLastName("Eysseric"), hasSize(1));
    assertThat(jpaCustomEntityServiceTest.getPersonsByLastName("Nicolas"), hasSize(0));
  }

  @Test
  public void getPersonsByFirstName() {
    assertThat(jpaCustomEntityServiceTest.getPersonsByFirstName("Nicolas"), notNullValue());
    assertThat(jpaCustomEntityServiceTest.getPersonsByFirstName("Eysseric"), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getPersonsByFirstNameNotUnique() {
    jpaCustomEntityServiceTest.getPersonsByFirstName("firstName");
  }

  @Test
  public void getAnimalsOfPersonByLastName() {
    assertThat(jpaCustomEntityServiceTest.getAnimalsByLastNameOfPerson("Eysseric"), hasSize(2));
    assertThat(jpaCustomEntityServiceTest.getAnimalsByLastNameOfPerson("eysseric"), hasSize(0));
    assertThat(jpaCustomEntityServiceTest.getPersonsByLastName("Moquillon"), hasSize(1));
    assertThat(jpaCustomEntityServiceTest.getAnimalsByLastNameOfPerson("Moquillon"), hasSize(0));
  }

  @Test
  public void getAnimalsByName() {
    assertThat(jpaCustomEntityServiceTest.getAnimalsByName("Titi"), notNullValue());
    assertThat(jpaCustomEntityServiceTest.getAnimalsByName("titi"), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getAnimalsByNameNotUnique() {
    jpaCustomEntityServiceTest.getAnimalsByName("name");
  }

  @Test
  public void getAnimalsByType() {
    assertThat(jpaCustomEntityServiceTest.getAnimalsByType(AnimalTypeCustomEntity.bird),
        hasSize(1));
    assertThat(jpaCustomEntityServiceTest.getAnimalsByType(AnimalTypeCustomEntity.frog),
        hasSize(0));
  }

  @Test
  public void getPersonsHaveTypeOfAnimal() {
    assertThat(jpaCustomEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalTypeCustomEntity.bird),
        hasSize(1));
    assertThat(jpaCustomEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalTypeCustomEntity.frog),
        hasSize(0));
  }

  @Test
  public void updateAnimalName() {
    AnimalCustomEntity animalCustomEntity = jpaCustomEntityServiceTest.getAnimalById("1");
    String previousName = animalCustomEntity.getName();
    assertThat(animalCustomEntity, notNullValue());
    animalCustomEntity.setName(previousName + "_toto");
    assertThat(jpaCustomEntityServiceTest.updateAnimalName(animalCustomEntity), is(1L));
    AnimalCustomEntity animalCustomEntityReloaded =
        jpaCustomEntityServiceTest.getAnimalById(animalCustomEntity.getId());
    assertThat(animalCustomEntityReloaded, notNullValue());
    assertThat(animalCustomEntityReloaded.getName(), is(previousName + "_toto"));
  }

  @Test
  public void deleteAnimalsByType() {
    AnimalCustomEntity animalCustomEntity = jpaCustomEntityServiceTest.getAnimalById("1");
    assertThat(animalCustomEntity, notNullValue());
    assertThat(jpaCustomEntityServiceTest.deleteAnimalsByType(animalCustomEntity.getType()),
        is(1L));
    assertThat(jpaCustomEntityServiceTest.deleteAnimalsByType(animalCustomEntity.getType()),
        is(0L));
    assertThat(jpaCustomEntityServiceTest.deleteAnimalsByType(AnimalTypeCustomEntity.frog), is(0L));
    AnimalCustomEntity animalCustomEntityReloaded =
        jpaCustomEntityServiceTest.getAnimalById(animalCustomEntity.getId());
    assertThat(animalCustomEntityReloaded, nullValue());
  }

  @Test
  public void updatePersonFirstNamesHavingAtLeastOneAnimal() {
    PersonCustomEntity personCustomEntity1 = jpaCustomEntityServiceTest.getPersonById("person_1");
    PersonCustomEntity personCustomEntity2 = jpaCustomEntityServiceTest.getPersonById("person_2");
    PersonCustomEntity personCustomEntity3 = jpaCustomEntityServiceTest.getPersonById("person_3");
    String previousPerson1Name = personCustomEntity1.getFirstName();
    String previousPerson2Name = personCustomEntity2.getFirstName();
    String previousPerson3Name = personCustomEntity3.getFirstName();
    assertThat(personCustomEntity1, notNullValue());
    assertThat(personCustomEntity2, notNullValue());
    assertThat(personCustomEntity3, notNullValue());

    personCustomEntity1.setFirstName(previousPerson1Name + "_updated");
    personCustomEntity2.setFirstName(previousPerson2Name + "_updated");
    personCustomEntity3.setFirstName(previousPerson3Name + "_updated");
    assertThat(
        jpaCustomEntityServiceTest.updatePersonFirstNameHavingAtLeastOneAnimal(personCustomEntity1),
        is(1L));
    assertThat(
        jpaCustomEntityServiceTest.updatePersonFirstNameHavingAtLeastOneAnimal(personCustomEntity2),
        is(1L));
    assertThat(
        jpaCustomEntityServiceTest.updatePersonFirstNameHavingAtLeastOneAnimal(personCustomEntity3),
        is(0L));
    PersonCustomEntity personCustomEntity1Reloaded =
        jpaCustomEntityServiceTest.getPersonById("person_1");
    PersonCustomEntity personCustomEntity2Reloaded =
        jpaCustomEntityServiceTest.getPersonById("person_2");
    PersonCustomEntity personCustomEntity3Reloaded =
        jpaCustomEntityServiceTest.getPersonById("person_3");
    assertThat(personCustomEntity1Reloaded, notNullValue());
    assertThat(personCustomEntity1Reloaded.getFirstName(), is(previousPerson1Name + "_updated"));
    assertThat(personCustomEntity2Reloaded, notNullValue());
    assertThat(personCustomEntity2Reloaded.getFirstName(), is(previousPerson2Name + "_updated"));
    assertThat(personCustomEntity3Reloaded, notNullValue());
    assertThat(personCustomEntity3Reloaded.getFirstName(), is(previousPerson3Name));
  }

  @Test
  public void deletePersonFirstNamesHavingAtLeastOneAnimal() {
    PersonCustomEntity personCustomEntity1 = jpaCustomEntityServiceTest.getPersonById("person_1");
    PersonCustomEntity personCustomEntity2 = jpaCustomEntityServiceTest.getPersonById("person_2");
    PersonCustomEntity personCustomEntity3 = jpaCustomEntityServiceTest.getPersonById("person_3");
    assertThat(personCustomEntity1, notNullValue());
    assertThat(personCustomEntity2, notNullValue());
    assertThat(personCustomEntity3, notNullValue());
    assertThat(jpaCustomEntityServiceTest.deletePersonFirstNamesHavingAtLeastOneAnimal(), is(4L));
    assertThat(jpaCustomEntityServiceTest.deletePersonFirstNamesHavingAtLeastOneAnimal(), is(0L));
    personCustomEntity1 = jpaCustomEntityServiceTest.getPersonById("person_1");
    personCustomEntity2 = jpaCustomEntityServiceTest.getPersonById("person_2");
    personCustomEntity3 = jpaCustomEntityServiceTest.getPersonById("person_3");
    assertThat(personCustomEntity1, nullValue());
    assertThat(personCustomEntity2, nullValue());
    assertThat(personCustomEntity3, notNullValue());
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
}