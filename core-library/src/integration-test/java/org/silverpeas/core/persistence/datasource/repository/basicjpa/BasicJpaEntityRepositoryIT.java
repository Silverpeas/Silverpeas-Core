/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.persistence.datasource.repository.basicjpa;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.hamcrest.Matchers;
import org.hibernate.LazyInitializationException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.AnimalTypeBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.EquipmentBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.basicjpa.model.PersonBasicEntity;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityServiceTest;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class BasicJpaEntityRepositoryIT {

  private BasicJpaEntityServiceTest basicJpaEntityServiceTest;

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
    basicJpaEntityServiceTest = ServiceProvider.getService(BasicJpaEntityServiceTest.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(BasicJpaEntityRepositoryIT.class)
        .addDatabaseToolFeatures()
        .addJpaPersistenceFeatures().testFocusedOn((warBuilder) -> {
          warBuilder.addClasses(BasicJpaEntityServiceTest.class);
          warBuilder.addPackages(true, "org.silverpeas.core.persistence.datasource.repository");
          warBuilder.addAsResource(
              "org/silverpeas/core/persistence/datasource/basicjpa/create_table.sql");
        }).build();
  }

  @Test
  public void getAll() {
    List<PersonBasicEntity> personCustomEntities = basicJpaEntityServiceTest.getAllPersons();
    assertThat(personCustomEntities, hasSize(5));
    List<AnimalBasicEntity> animalCustomEntities = basicJpaEntityServiceTest.getAllAnimals();
    assertThat(animalCustomEntities, hasSize(5));
    List<EquipmentBasicEntity> equipmentCustomEntities =
        basicJpaEntityServiceTest.getAllEquiments();
    assertThat(equipmentCustomEntities, hasSize(1));
  }

  @Test
  public void getPerson() {
    PersonBasicEntity personBasicEntity = basicJpaEntityServiceTest.getPersonById("person_1");
    assertThat(personBasicEntity,
        not(sameInstance(basicJpaEntityServiceTest.getPersonById("person_1"))));
    assertThat(personBasicEntity, notNullValue());
    assertThat(personBasicEntity.getFirstName(), is("Yohann"));
    assertThat(personBasicEntity.getLastName(), is("Chastagnier"));
    assertThat(personBasicEntity.getAnimals(), hasSize(1));
    AnimalBasicEntity personAnimalBasicEntity = personBasicEntity.getAnimals().get(0);
    assertThat(personAnimalBasicEntity.getId(), is("1"));
    assertThat(personAnimalBasicEntity.getType(), Matchers.is(AnimalTypeBasicEntity.cat));
    assertThat(personAnimalBasicEntity.getName(), is("Blacky"));

    personBasicEntity = basicJpaEntityServiceTest.getPersonById("person_2");
    assertThat(personBasicEntity, notNullValue());
    assertThat(personBasicEntity.getFirstName(), is("Nicolas"));
    assertThat(personBasicEntity.getLastName(), is("Eysseric"));
    assertThat(personBasicEntity.getAnimals(), hasSize(2));

    personBasicEntity = basicJpaEntityServiceTest.getPersonById("person_3");
    assertThat(personBasicEntity, notNullValue());
    assertThat(personBasicEntity.getFirstName(), is("Miguel"));
    assertThat(personBasicEntity.getLastName(), is("Moquillon"));
    assertThat(personBasicEntity.getAnimals(), empty());

    assertThat(basicJpaEntityServiceTest.getPersonById("person_that_doesnt_exist"), nullValue());
  }

  @Test
  public void getPersons() {
    List<PersonBasicEntity> personCustomEntities = basicJpaEntityServiceTest
        .getPersonById("person_1", "person_11", "person_3", "person_12", "person_2", "person_13");
    assertThat(personCustomEntities, hasSize(3));
  }

  @Test
  public void getAnimal() {
    AnimalBasicEntity animalBasicEntity = basicJpaEntityServiceTest.getAnimalById("1");
    assertThat(animalBasicEntity,
        not(sameInstance(basicJpaEntityServiceTest.getAnimalById("1"))));
    assertThat(animalBasicEntity, notNullValue());
    assertThat(animalBasicEntity.getType(), is(AnimalTypeBasicEntity.cat));
    assertThat(animalBasicEntity.getName(), is("Blacky"));
    assertThat(animalBasicEntity.getPerson(), notNullValue());
    assertThat(animalBasicEntity.getPerson().getId(), is("person_1"));
    assertThat(animalBasicEntity.getPerson().getFirstName(), is("Yohann"));
    assertThat(animalBasicEntity.getPerson().getLastName(), is("Chastagnier"));
    assertThat(animalBasicEntity.getPerson().getAnimals(), hasSize(1));

    animalBasicEntity = basicJpaEntityServiceTest.getAnimalById("2");
    assertThat(animalBasicEntity, notNullValue());
    assertThat(animalBasicEntity.getType(), is(AnimalTypeBasicEntity.dog));
    assertThat(animalBasicEntity.getName(), is("Bagels"));
    assertThat(animalBasicEntity.getPerson(), notNullValue());
    assertThat(animalBasicEntity.getPerson().getId(), is("person_2"));
    assertThat(animalBasicEntity.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animalBasicEntity.getPerson().getLastName(), is("Eysseric"));
    assertThat(animalBasicEntity.getPerson().getAnimals(), hasSize(2));

    animalBasicEntity = basicJpaEntityServiceTest.getAnimalById("3");
    assertThat(animalBasicEntity, notNullValue());
    assertThat(animalBasicEntity.getType(), is(AnimalTypeBasicEntity.bird));
    assertThat(animalBasicEntity.getName(), is("Titi"));
    assertThat(animalBasicEntity.getPerson(), notNullValue());
    assertThat(animalBasicEntity.getPerson().getId(), is("person_2"));
    assertThat(animalBasicEntity.getPerson().getFirstName(), is("Nicolas"));
    assertThat(animalBasicEntity.getPerson().getLastName(), is("Eysseric"));
    assertThat(animalBasicEntity.getPerson().getAnimals(), hasSize(2));

    // Animal doesn't exist
    assertThat(basicJpaEntityServiceTest.getAnimalById("25"), nullValue());
  }

  @Test
  public void getAnimals() {
    List<AnimalBasicEntity> animalCustomEntities =
        basicJpaEntityServiceTest.getAnimalById("1", "11", "3", "12", "2", "13");
    assertThat(animalCustomEntities, hasSize(3));
  }

  /**
   * Created by information is missing on insert.
   */
  @Test(expected = Exception.class)
  public void savePersonBadlyInsert() {
    PersonBasicEntity newPersonBasicEntity =
        new PersonBasicEntity().setFirstName("Aurore").setLastName("Allibe");
    basicJpaEntityServiceTest.save(null, newPersonBasicEntity);
  }

  @Test(expected = LazyInitializationException.class)
  public void entityCloneFailureWithLazyFetching() {
    AnimalBasicEntity animal = basicJpaEntityServiceTest.getAnimalsByName("Titi");
    animal.copy();
  }

  @Test
  public void entityGetUpdateCloneBehaviour() {
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(5));

    // Get
    PersonBasicEntity personBasicEntity = basicJpaEntityServiceTest.getPersonById("person_1");
    assertThat(personBasicEntity.getId(), notNullValue());

    // Update
    PersonBasicEntity personBasicEntityUpdated = Transaction.performInOne(() -> {
      PersonBasicEntity entity = basicJpaEntityServiceTest.save(personBasicEntity);
      // we force the loading of animals here (must be in a persistence session)
      assertThat(entity.getAnimals().stream().mapToLong(a -> a.getEquipments().size()).sum() >= 0,
          is(true));
      return entity;
    });
    assertThat(personBasicEntityUpdated, not(sameInstance(personBasicEntity)));
    assertThat(personBasicEntityUpdated.getId(), notNullValue());
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(5));


    // Clone
    PersonBasicEntity personBasicEntityCloned = personBasicEntityUpdated.copy();
    assertThat(personBasicEntityCloned.getId(), nullValue());
    assertThat(personBasicEntityCloned.getFirstName(),
        is(personBasicEntityUpdated.getFirstName()));
    assertThat(personBasicEntityCloned.getLastName(), is(personBasicEntityUpdated.getLastName()));
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(5));

    // Save clone
    PersonBasicEntity personBasicEntitySaveResult =
        basicJpaEntityServiceTest.save(personBasicEntityCloned);
    assertThat(personBasicEntitySaveResult, sameInstance(personBasicEntityCloned));
    PersonBasicEntity personBasicEntityClonedReloaded =
        basicJpaEntityServiceTest.getPersonById(personBasicEntityCloned.getId());
    assertThat(personBasicEntityClonedReloaded, not(sameInstance(personBasicEntityCloned)));
    assertThat(personBasicEntityClonedReloaded.getId(), notNullValue());
    assertThat(personBasicEntityClonedReloaded.getFirstName(),
        is(personBasicEntityCloned.getFirstName()));
    assertThat(personBasicEntityClonedReloaded.getLastName(),
        is(personBasicEntityCloned.getLastName()));
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(6));
  }

  @Test
  public void savePerson() {
    PersonBasicEntity newPersonBasicEntity =
        new PersonBasicEntity().setFirstName("Aurore").setLastName("Allibe");
    assertThat(newPersonBasicEntity.getId(), nullValue());
    PersonBasicEntity personBasicEntitySaveResult =
        basicJpaEntityServiceTest.save(newPersonBasicEntity);
    assertThat(personBasicEntitySaveResult, sameInstance(newPersonBasicEntity));
    assertThat(newPersonBasicEntity.getId(), notNullValue());
    PersonBasicEntity personBasicEntityCreated =
        basicJpaEntityServiceTest.getPersonById(newPersonBasicEntity.getId());
    assertThat(personBasicEntityCreated, notNullValue());
    assertThat(personBasicEntityCreated, not(sameInstance(newPersonBasicEntity)));
    assertThat(personBasicEntityCreated, is(personBasicEntityCreated));
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      basicJpaEntityServiceTest.save(
          new PersonBasicEntity().setFirstName("FirstName_" + i).setLastName("LastName_" + i),
          new PersonBasicEntity().setFirstName("FirstName#" + i).setLastName("LastName#" + i));
    }
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void savePersonWithIdSetManually() {
    PersonBasicEntity newPersonBasicEntity =
        new PersonBasicEntity().setId("id_that_will_be_changed").setFirstName("Aurore")
            .setLastName("Allibe");
    assertThat(newPersonBasicEntity.getId(), is("id_that_will_be_changed"));
    PersonBasicEntity personBasicEntitySaveResult =
        basicJpaEntityServiceTest.save(newPersonBasicEntity);
    assertThat(personBasicEntitySaveResult, sameInstance(newPersonBasicEntity));
    assertThat(newPersonBasicEntity.getId(), notNullValue());
    assertThat(newPersonBasicEntity.getId(), not(is("id_that_will_be_changed")));
    PersonBasicEntity personBasicEntityCreated =
        basicJpaEntityServiceTest.getPersonById(personBasicEntitySaveResult.getId());
    assertThat(personBasicEntityCreated, notNullValue());
    assertThat(personBasicEntityCreated, not(sameInstance(newPersonBasicEntity)));
    assertThat(personBasicEntityCreated, is(personBasicEntityCreated));
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(6));

    for (int i = 0; i < 50; i++) {
      basicJpaEntityServiceTest.save(
          new PersonBasicEntity().setFirstName("FirstName_" + i).setLastName("LastName_" + i),
          new PersonBasicEntity().setFirstName("FirstName#" + i).setLastName("LastName#" + i));
    }
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(106));
  }

  @Test
  public void saveAnimal() {
    PersonBasicEntity personBasicEntity = basicJpaEntityServiceTest.getPersonById("person_3");

    AnimalBasicEntity newAnimalBasicEntity =
        new AnimalBasicEntity().setType(AnimalTypeBasicEntity.cat).setName("Pilou")
            .setPerson(personBasicEntity);
    assertThat(newAnimalBasicEntity.getId(), nullValue());
    AnimalBasicEntity animalBasicEntitySaveResult =
        basicJpaEntityServiceTest.save(newAnimalBasicEntity);
    assertThat(animalBasicEntitySaveResult, sameInstance(newAnimalBasicEntity));
    assertThat(newAnimalBasicEntity.getId(), notNullValue());
    AnimalBasicEntity animalBasicEntityCreated =
        basicJpaEntityServiceTest.getAnimalById(newAnimalBasicEntity.getId());
    assertThat(animalBasicEntityCreated, notNullValue());
    assertThat(animalBasicEntityCreated, not(sameInstance(newAnimalBasicEntity)));
    assertThat(animalBasicEntityCreated, is(animalBasicEntityCreated));
    assertThat(animalBasicEntityCreated.getId(), is("10"));
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(6));

    for (long i = 0; i < 50; i++) {
      AnimalBasicEntity animalBasicEntity1 =
          new AnimalBasicEntity().setType(AnimalTypeBasicEntity.bird).setName("Name_" + i)
              .setPerson(personBasicEntity);
      AnimalBasicEntity animalBasicEntity2 =
          new AnimalBasicEntity().setType(AnimalTypeBasicEntity.cat).setName("Name#" + i)
              .setPerson(personBasicEntity);
      AnimalBasicEntity animalBasicEntity3 =
          new AnimalBasicEntity().setType(AnimalTypeBasicEntity.dog).setName("Name-" + i)
              .setPerson(personBasicEntity);
      basicJpaEntityServiceTest
          .save(animalBasicEntity1, animalBasicEntity2, animalBasicEntity3);
      assertThat(animalBasicEntity1.getId(), is(String.valueOf(11 + (i * 3))));
      assertThat(animalBasicEntity2.getId(), is(String.valueOf(12 + (i * 3))));
      assertThat(animalBasicEntity3.getId(), is(String.valueOf(13 + (i * 3))));
    }
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(156));
  }

  @Test
  public void deleteEntity() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    assertThat(basicJpaEntityServiceTest.getAllEquiments(), hasSize(1));
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(5));
    basicJpaEntityServiceTest.delete(basicJpaEntityServiceTest.getAnimalById("1"));
    assertThat(basicJpaEntityServiceTest.getAnimalById("1"), nullValue());
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(4));
    basicJpaEntityServiceTest.delete(basicJpaEntityServiceTest.getAnimalById("3"),
        basicJpaEntityServiceTest.getAnimalById("2"), new AnimalBasicEntity());
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(2));
    // Verifying here that cascade process has been performed ...
    assertThat(basicJpaEntityServiceTest.getAllEquiments(), hasSize(0));

    // Persons
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(5));
    basicJpaEntityServiceTest.delete(basicJpaEntityServiceTest.getPersonById("person_1"));
    assertThat(basicJpaEntityServiceTest.getPersonById("person_1"), nullValue());
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(4));
    basicJpaEntityServiceTest.delete(basicJpaEntityServiceTest.getPersonById("person_3"),
        basicJpaEntityServiceTest.getPersonById("person_2"), new PersonBasicEntity());
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(2));
  }

  @Test
  public void deleteEntityById() {

    // Animals (animals are dependents to persons, so they have to be deleted before)
    assertThat(basicJpaEntityServiceTest.getAllEquiments(), hasSize(1));
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(5));
    long nbDeleted = basicJpaEntityServiceTest.deleteAnimalById("1");
    assertThat(basicJpaEntityServiceTest.getAnimalById("1"), nullValue());
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(4));
    assertThat(nbDeleted, is(1L));
    nbDeleted =
        basicJpaEntityServiceTest.deleteAnimalById("38", "26", "3", "27", "38", "2", "36", "22");
    assertThat(basicJpaEntityServiceTest.getAllAnimals(), hasSize(2));
    assertThat(nbDeleted, is(2L));
    // Verifying here that cascade process is not performed in this deletion way ...
    assertThat(basicJpaEntityServiceTest.getAllEquiments(), hasSize(1));

    // Persons
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(5));
    nbDeleted = basicJpaEntityServiceTest.deletePersonById("person_1");
    assertThat(basicJpaEntityServiceTest.getPersonById("person_1"), nullValue());
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(4));
    assertThat(nbDeleted, is(1L));
    nbDeleted = basicJpaEntityServiceTest
        .deletePersonById("person_38", "person_26", "person_3", "person_27", "person_38",
            "person_2", "person_36", "person_22");
    assertThat(basicJpaEntityServiceTest.getAllPersons(), hasSize(2));
    assertThat(nbDeleted, is(2L));
  }

  @Test
  public void getPersonsByLastName() {
    assertThat(basicJpaEntityServiceTest.getPersonsByLastName("Eysseric"), hasSize(1));
    assertThat(basicJpaEntityServiceTest.getPersonsByLastName("Nicolas"), hasSize(0));
  }

  @Test
  public void getPersonsByFirstName() {
    assertThat(basicJpaEntityServiceTest.getPersonsByFirstName("Nicolas"), notNullValue());
    assertThat(basicJpaEntityServiceTest.getPersonsByFirstName("Eysseric"), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getPersonsByFirstNameNotUnique() {
    basicJpaEntityServiceTest.getPersonsByFirstName("firstName");
  }

  @Test
  public void getAnimalsOfPersonByLastName() {
    assertThat(basicJpaEntityServiceTest.getAnimalsByLastNameOfPerson("Eysseric"), hasSize(2));
    assertThat(basicJpaEntityServiceTest.getAnimalsByLastNameOfPerson("eysseric"), hasSize(0));
    assertThat(basicJpaEntityServiceTest.getPersonsByLastName("Moquillon"), hasSize(1));
    assertThat(basicJpaEntityServiceTest.getAnimalsByLastNameOfPerson("Moquillon"), hasSize(0));
  }

  @Test
  public void getAnimalsByName() {
    assertThat(basicJpaEntityServiceTest.getAnimalsByName("Titi"), notNullValue());
    assertThat(basicJpaEntityServiceTest.getAnimalsByName("titi"), nullValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void getAnimalsByNameNotUnique() {
    basicJpaEntityServiceTest.getAnimalsByName("name");
  }

  @Test
  public void getAnimalsByType() {
    assertThat(basicJpaEntityServiceTest.getAnimalsByType(AnimalTypeBasicEntity.bird),
        hasSize(1));
    assertThat(basicJpaEntityServiceTest.getAnimalsByType(AnimalTypeBasicEntity.frog),
        hasSize(0));
  }

  @Test
  public void getPersonsHaveTypeOfAnimal() {
    assertThat(basicJpaEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalTypeBasicEntity.bird),
        hasSize(1));
    assertThat(basicJpaEntityServiceTest.getPersonsHaveTypeOfAnimal(AnimalTypeBasicEntity.frog),
        hasSize(0));
  }

  @Test
  public void deleteAnimalsByType() {
    AnimalBasicEntity animalBasicEntity = basicJpaEntityServiceTest.getAnimalById("1");
    assertThat(animalBasicEntity, notNullValue());
    assertThat(basicJpaEntityServiceTest.deleteAnimalsByType(animalBasicEntity.getType()),
        is(1L));
    assertThat(basicJpaEntityServiceTest.deleteAnimalsByType(animalBasicEntity.getType()),
        is(0L));
    assertThat(basicJpaEntityServiceTest.deleteAnimalsByType(AnimalTypeBasicEntity.frog), is(0L));
    AnimalBasicEntity animalBasicEntityReloaded =
        basicJpaEntityServiceTest.getAnimalById(animalBasicEntity.getId());
    assertThat(animalBasicEntityReloaded, nullValue());
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