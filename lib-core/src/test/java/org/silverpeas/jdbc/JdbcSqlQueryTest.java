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

package org.silverpeas.jdbc;

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.util.DBUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.jdbc.JdbcSqlQuery.*;

public class JdbcSqlQueryTest extends AbstractTestDao {

  private final static long NB_ROW_AT_BEGINNING = 100L;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  protected String getDatasetFileName() {
    return "test-jdbc-query-dataset.xml";
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-jdbc-query-database.sql";
  }

  @Test
  public void testSelectCountVerifications() throws SQLException {
    assertThat(createCountFor("a_table").execute(), is(NB_ROW_AT_BEGINNING));
    assertThat(createCountFor("a_table").where("id != ?", 8).execute(),
        is(NB_ROW_AT_BEGINNING - 1));
    assertThat(createCountFor("a_table").where("id != ?", 8).and("LENGTH(value) <= ?", 7).execute(),
        is(9l));
  }

  @Test
  public void testSelectAll() throws SQLException {
    List<Pair<Long, String>> rows =
        createSelect("* from a_table").execute(new TableResultProcess());
    long l = 0;
    assertThat(rows, hasSize((int) NB_ROW_AT_BEGINNING));
    for (Pair<Long, String> row : rows) {
      assertThat(row.getLeft(), is(l));
      l++;
    }
  }

  @Test
  public void testSelectAllButUsingUnique() throws SQLException {
    try {
      createSelect("* from a_table").executeUnique(new TableResultProcess());
    } catch (IllegalArgumentException e) {
      return;
    }
    fail("IllegalArgumentException should be thrown...");
  }

  @Test
  public void testSelectOneParameter() throws SQLException {
    final String sqlQuery = "* from a_table where id = ?";
    List<Pair<Long, String>> rows = createSelect(sqlQuery, 30).execute(new TableResultProcess());
    assertThat(rows, hasSize(1));
    assertThat(unique(rows).getLeft(), is(30L));
    rows = createSelect(sqlQuery, 200).execute(new TableResultProcess());
    assertThat(rows, hasSize(0));
    assertThat(unique(rows), nullValue());
  }

  @Test
  public void testSelectUsingOneAppendParameter() throws SQLException {
    List<Pair<Long, String>> rows =
        createSelect("* from a_table where id = ?", 26).execute(new TableResultProcess());
    assertThat(rows, hasSize(1));
    assertThat(unique(rows).getLeft(), is(26L));
  }

  @Test
  public void testSelectUsingTwoAppendParametersAndAppendListOfParameters() throws SQLException {
    JdbcSqlQuery sqlQuery = createSelect("* from a_table where (id = ?", 26);
    sqlQuery.or("LENGTH(value) <= ?)", 7);
    sqlQuery.or("id").in(38, 39, 40);
    List<Pair<Long, String>> rows = sqlQuery.execute(new TableResultProcess());
    assertThat(rows, hasSize(14));
  }

  @Test
  public void testSelectUsingAppendListOfParameters() throws SQLException {
    List<Pair<Long, String>> rows =
        createSelect("* from a_table where id").in(38, 39, 40).execute(new TableResultProcess());
    assertThat(rows, hasSize(3));
  }

  private static class TableResultProcess implements SelectResultRowProcess<Pair<Long, String>> {
    @Override
    public Pair<Long, String> currentRow(final ResultSetWrapper row) throws SQLException {
      return Pair.of(row.getLongObject(1), row.getString(2));
    }
  }

  @Test
  public void testCreateRowUsingAppendSaveParameter() throws SQLException {
    assertThat(getTableLines(), hasSize(100));
    JdbcSqlQuery insertSqlQuery = JdbcSqlQuery.createInsertFor("a_table");
    insertSqlQuery.addInsertParam("id", 200);
    insertSqlQuery.addInsertParam("value", "value_200_inserted");
    long insertCount = insertSqlQuery.execute();
    assertThat(insertCount, is(1L));
    assertThat(getTableLines(), hasSize(101));
    assertThat(getTableLines().get(100), is("200@value_200_inserted"));
  }

  @Test
  public void testUpdateTwoRowsFromThreeUpdatesUsingAppendSaveParameter() throws SQLException {
    assertThat(getTableLines().get(0), is("0@value_0"));
    assertThat(getTableLines().get(26), is("26@value_26"));
    assertThat(getTableLines().get(38), is("38@value_38"));

    JdbcSqlQuery firstInsertSqlQuery = createUpdateFor("a_table");
    firstInsertSqlQuery.addUpdateParam("value", "value_26_updated");
    firstInsertSqlQuery.where("id = ?", 26);

    JdbcSqlQuery secondInsertSqlQuery = createUpdateFor("a_table");
    secondInsertSqlQuery.addUpdateParam("value", "value_38_updated");
    secondInsertSqlQuery.where("id = ?", 38);

    JdbcSqlQuery thirdInsertSqlQuery = createUpdateFor("a_table");
    thirdInsertSqlQuery.addUpdateParam("value", "value_200_updated");
    thirdInsertSqlQuery.where("id = ?", 200);

    long updateCount = JdbcSqlExecutorProvider.getJdbcSqlExecutor()
        .executeModify(firstInsertSqlQuery, secondInsertSqlQuery, thirdInsertSqlQuery);
    assertThat(updateCount, is(2L));

    assertThat(getTableLines().get(0), is("0@value_0"));
    assertThat(getTableLines().get(26), is("26@value_26_updated"));
    assertThat(getTableLines().get(38), is("38@value_38_updated"));
  }

  @Test
  public void testDeleteRows() throws SQLException {
    assertThat(getTableLines(), hasSize(100));
    long deleteCount = createDeleteFor("a_table").where("LENGTH(value) <= ?", 7).execute();
    assertThat(deleteCount, is(10L));
    assertThat(getTableLines(), hasSize(90));
  }

  @Test
  public void testDropTableA() throws SQLException {
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "a_table")
        .execute(), is(1L));
    createDropFor("a_table").execute();
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "a_table")
        .execute(), is(0L));
    createTable("a_table").addField("id", "integer primary key").execute();
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "a_table")
        .execute(), is(1L));
  }

  @Test
  public void testCreateTableB() throws SQLException {
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "b_table")
        .execute(), is(0L));
    createTable("b_table").addField("identifier", "integer primary key")
        .addField("description", "varchar(50) NOT NULL").execute();
    createInsertFor("b_table").addInsertParam("identifier", 26)
        .addInsertParam("description", "Drôme").execute();
    assertThat(createCountFor("INFORMATION_SCHEMA.TABLES").where("lower(TABLE_NAME) = ?", "b_table")
        .execute(), is(1L));
    assertThat(createCountFor("b_table").execute(), is(1L));
  }

  /**
   * Gets the content of a_table.
   * @return the content of a_table.
   */
  private List<String> getTableLines() {
    Connection connection = null;
    PreparedStatement statement = null;
    ResultSet resultSet = null;
    try {
      connection = getConnection().getConnection();
      statement = connection.prepareStatement("SELECT * FROM a_table ORDER BY id");
      resultSet = statement.executeQuery();
      List<String> result = new ArrayList<String>();
      while (resultSet.next()) {
        result.add(resultSet.getLong(1) + "@" + resultSet.getString(2));
      }
      return result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      DBUtil.close(resultSet, statement);
    }
  }
}