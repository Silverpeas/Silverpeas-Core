package org.silverpeas.core.persistence.jdbc.sql;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.SQLDateTimeConstants;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests on the use of the {@link JdbcSqlQuery} class in the persistence of business
 * objects.
 */
@RunWith(Arquillian.class)
public class JdbcSqlQueryUseIT {

  private static final String TABLES_CREATION =
      "/org/silverpeas/core/persistence/datasource/create_table.sql";

  private static final Operation PERSON_SET_UP = Operations.insertInto("test_persons")
      .columns("id", "firstName", "lastName", "birthday", "createDate", "createdBy",
          "lastUpdateDate", "lastUpdatedBy", "version")
      .values("person_1", "Yohann", "Chastagnier", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_2", "Nicolas", "Eysseric", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_3", "Miguel", "Moquillon", "1971-11-30", "2013-11-21 09:57:30.003", "2",
          "2013-11-22 22:00:50.006", "10", 3L)
      .values("person_1000", "firstName", "lastName", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .values("person_1001", "firstName", "lastName", "1980-01-01", "2013-11-21 09:57:30.003", "1",
          "2013-11-21 09:57:30.003", "1", 0L)
      .build();
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
          "2013-11-21 09:57:30.003", "10", 0L)
      .build();
  private static final Operation EQUIPEMENT_SET_UP = Operations.insertInto("test_equipments")
      .columns("id", "name", "animalId", "startDate", "endDate", "inDays", "createDate",
          "createdBy", "lastUpdateDate", "lastUpdatedBy", "version")
      .values("equipment_1", "necklace", 2L, "0001-01-01 00:00:00.000", "9999-12-31 00:00:00.000",
          1, "2013-11-21 09:57:30.003", "1", "2013-11-22 22:00:50.006", "2", 10L)
      .build();
  private static final Operation UNIQUE_ID_SET_UP = Operations.insertInto("UniqueId")
      .columns("maxId", "tableName")
      .values(9, "test_animals")
      .build();

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLES_CREATION)
      .loadInitialDataSetFrom(PERSON_SET_UP, ANIMAL_SET_UP, EQUIPEMENT_SET_UP, UNIQUE_ID_SET_UP);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(JdbcSqlQueryIT.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addAsResource("org/silverpeas/core/persistence/datasource/create_table.sql")
        .build();
  }

  @Test
  public void getYohannChastagnier() throws SQLException {
    final String id = "person_1";
    assertPersistedPerson(id, p -> {
      assertThat(p.get("id"), is(id));
      assertThat(p.get("firstName"), is("Yohann"));
      assertThat(p.get("lastName"), is("Chastagnier"));
      assertThat(p.get("birthday"), is(java.sql.Date.valueOf(LocalDate.parse("1980-01-01"))));
      assertThat(p.get("createDate"), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
      assertThat(p.get("lastUpdateDate"), is(Timestamp.valueOf("2013-11-21 09:57:30.003")));
      assertThat(p.get("createdBy"), is("1"));
      assertThat(p.get("lastUpdatedBy"), is("1"));
      assertThat(p.get("version"), is(0L));
    });
  }

  @Test
  public void saveNewPerson() throws SQLException {
    final String id = "person_666";
    final Date now = new Date();

    // save new person
    Transaction.performInOne(() -> {
      long insertCount = JdbcSqlQuery.insertInto("test_persons")
          .withInsertParam("id", id)
          .withInsertParam("firstName", "Lucifer")
          .withInsertParam("lastName", "Satan")
          .withInsertParam("birthday", SQLDateTimeConstants.MIN_DATE)
          .withInsertParam("createDate", new Timestamp(now.getTime()))
          .withInsertParam("lastUpdateDate", new Timestamp(now.getTime()))
          .withInsertParam("createdBy", "666")
          .withInsertParam("lastUpdatedBy", "666")
          .withInsertParam("version", 0L)
          .execute();
      assertThat(insertCount, is(1L));
      return null;
    });

    // check the new person was correctly saved
    assertPersistedPerson(id, p -> {
      assertThat(p.get("id"), is(id));
      assertThat(p.get("firstName"), is("Lucifer"));
      assertThat(p.get("lastName"), is("Satan"));
      assertThat(p.get("birthday"), is(SQLDateTimeConstants.MIN_DATE));
      assertThat(p.get("createDate"), is(new Timestamp(now.getTime())));
      assertThat(p.get("lastUpdateDate"), is(new Timestamp(now.getTime())));
      assertThat(p.get("createdBy"), is("666"));
      assertThat(p.get("lastUpdatedBy"), is("666"));
      assertThat(p.get("version"), is(0L));
    });
  }

  private void assertPersistedPerson(String id, Consumer<Map<String, Object>> assertion)
      throws SQLException {
    JdbcSqlQuery.select(
        "id, firstName, lastName, birthday, createDate, createdBy, lastUpdateDate, lastUpdatedBy," +
            " version").from("test_persons").where("id = ?", id).executeUnique(rs -> {
      Map<String, Object> personData = new HashMap<>();
      personData.put("id", rs.getString("id"));
      personData.put("firstName", rs.getString("firstName"));
      personData.put("lastName", rs.getString("lastName"));
      personData.put("birthday", rs.getDate("birthday"));
      personData.put("createDate", rs.getTimestamp("createDate"));
      personData.put("lastUpdateDate", rs.getTimestamp("lastUpdateDate"));
      personData.put("createdBy", rs.getString("createdBy"));
      personData.put("lastUpdatedBy", rs.getString("lastUpdatedBy"));
      personData.put("version", rs.getLong("version"));
      assertion.accept(personData);
      return null;
    });
  }
}
