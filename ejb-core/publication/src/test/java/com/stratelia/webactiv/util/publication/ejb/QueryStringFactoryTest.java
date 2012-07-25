/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.publication.ejb;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.util.DBUtil;

/**
 * @author Yohann Chastagnier
 */
public class QueryStringFactoryTest extends AbstractTestDao {

  private final String TABLE_NAME = "@#@TABLE_NAME@#@";
  private final int NB_EXECUTIONS = 3;

  public QueryStringFactoryTest() {
  }

  @Override
  protected String getDatasetFileName() {
    return "test-empty-dataset.xml";
  }

  @Test
  public void testGetSelectByBeginDateDescAndStatusAndNotLinkedToFatherId() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      final String query =
          QueryStringFactory.getSelectByBeginDateDescAndStatusAndNotLinkedToFatherId(TABLE_NAME);

      assertQueryStructure(query, "testGetSelectByBeginDateDescAndStatusAndNotLinkedToFatherId");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetSelectByFatherPK_1() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      String query = QueryStringFactory.getSelectByFatherPK(TABLE_NAME, false, null);

      assertQueryStructure(query, "testGetSelectByFatherPK_false");
      assertQueryExecution(query);

      query = QueryStringFactory.getSelectByFatherPK(TABLE_NAME, true, null);

      assertQueryStructure(query, "testGetSelectByFatherPK_true");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetSelectByFatherPK_2() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      String query = QueryStringFactory.getSelectByFatherPK(TABLE_NAME, true, null);

      assertQueryStructure(query, "testGetSelectByFatherPK_true");
      assertQueryExecution(query);

      query = QueryStringFactory.getSelectByFatherPK(TABLE_NAME, false, null);

      assertQueryStructure(query, "testGetSelectByFatherPK_false");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetSelectByFatherPK_3() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      final String query = QueryStringFactory.getSelectByFatherPK(TABLE_NAME);

      assertThat(query, is(QueryStringFactory.getSelectByFatherPK(TABLE_NAME, true, null)));
      assertQueryStructure(query, "testGetSelectByFatherPK_true");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetSelectNotInFatherPK() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      final String query = QueryStringFactory.getSelectNotInFatherPK(TABLE_NAME);

      assertQueryStructure(query, "testGetSelectNotInFatherPK");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetLoadRow() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      final String query = QueryStringFactory.getLoadRow(TABLE_NAME);

      assertQueryStructure(query, "testGetLoadRow");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetSelectByName() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      final String query = QueryStringFactory.getSelectByName();

      assertQueryStructure(query, "testGetSelectByName");
      assertQueryExecution(query);
    }
  }

  @Test
  public void testGetSelectByNameAndNodeId() throws Exception {
    for (int i = 0; i < NB_EXECUTIONS; i++) {

      final String query = QueryStringFactory.getSelectByNameAndNodeId();

      assertQueryStructure(query, "testGetSelectByNameAndNodeId");
      assertQueryExecution(query);
    }
  }

  /**
   * Checking the string query
   * @param query
   * @param fileNameContainingExpectedQueryResult
   * @throws Exception
   */
  private void assertQueryStructure(final String query,
      final String fileNameContainingExpectedQueryResult) throws Exception {
    assertThat(
        query.trim().replaceAll("[ ]{2,}", " "),
        is(IOUtils
            .toString(
                this.getClass()
                    .getClassLoader()
                    .getResourceAsStream(
                        "com/stratelia/webactiv/util/publication/ejb/" +
                            fileNameContainingExpectedQueryResult + ".txt"), "UTF-8").trim()
            .replaceAll("[\r\n]", "")));
  }

  /**
   * Trying to execute the query
   * @param query
   * @throws Exception
   */
  private void assertQueryExecution(final String query) throws Exception {
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt =
          getConnection().getConnection().prepareStatement(
              query.replaceAll(TABLE_NAME, "SB_Publication_Publi"));
      String lastQueryPart = "";
      int paramCount = 1;
      final String dummyString = "dummyString";
      for (final String queryPart : query.split(" ")) {
        if (queryPart.replaceAll("[\\.a-zA-Z]", "").length() == 0) {
          lastQueryPart = queryPart;
        } else if (queryPart.equals("?")) {
          if (lastQueryPart.toLowerCase().endsWith("nodeid") ||
              lastQueryPart.toLowerCase().endsWith("pubid")) {
            prepStmt.setInt(paramCount++, -1);
          } else {
            prepStmt.setString(paramCount++, dummyString + paramCount);
          }
        }
      }

      rs = prepStmt.executeQuery();

      assertThat(rs, Matchers.notNullValue());
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}
