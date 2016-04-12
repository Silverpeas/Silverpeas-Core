/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.silverstatistics.control;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.web.test.WarBuilder4WarCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class SilverStatisticsPeasDAOConnexionTest {

  private static final String dateBegin = "2010-12-01";
  private static final String dateEnd = "2011-07-01";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("test-stats-connections-dataset.sql");

  @Before
  public void generalSetUp() throws Exception {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WarCore.onWarForTestClass(SilverStatisticsPeasDAOConnexionTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.web.silverstatistics");
        }).build();
  }

  public SilverStatisticsPeasDAOConnexionTest() {
  }

  /**
   * Test of getStatsConnexionAllAll method, of class SilverStatisticsPeasDAOConnexion. Aggregate
   * the Number of connections and the total durations for a period.
   */
  @Test
  public void testGetStatsConnexionAllAll() throws Exception {
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionAllAll(dateBegin, dateEnd);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    String[] aggregate = result.iterator().next();
    assertThat(aggregate, is(notNullValue()));
    assertThat(aggregate.length, is(4));
    assertThat(aggregate, arrayContaining("*", "1265", "6397404", ""));
  }

  /**
   * Test of getStatsUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testGetStatsUser() throws Exception {
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsUser(dateBegin, dateEnd);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    Collection<String> dates = (Collection<String>) result[0];
    assertThat(dates, is(notNullValue()));
    assertThat(dates, hasSize(8));
    assertThat(dates, contains("2010-12-01", "2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
        "2011-05-01", "2011-06-01", "2011-07-01"));
    Collection<String> counts = (Collection<String>) result[1];
    assertThat(counts, is(notNullValue()));
    assertThat(counts, hasSize(8));
    assertThat(counts, contains("0", "5", "5", "5", "5", "5", "5", "0"));
  }

  /**
   * Test of getStatsConnexion method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexion() throws Exception {
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsConnexion(dateBegin, dateEnd);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    @SuppressWarnings("unchecked") Collection<String> dates = (Collection<String>) result[0];
    assertThat(dates, is(notNullValue()));
    assertThat(dates, hasSize(8));
    assertThat(dates, contains("2010-12-01", "2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
        "2011-05-01", "2011-06-01", "2011-07-01"));
    Collection<String> counts = (Collection<String>) result[1];
    assertThat(counts, is(notNullValue()));
    assertThat(counts, hasSize(8));
    assertThat(counts, contains("0", "223", "129", "289", "394", "115", "115", "0"));
  }

  /**
   * Test of getStatsConnexionAllUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexionAllUser() throws Exception {
    int idUser = 2;
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser(dateBegin, dateEnd, idUser);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    String[] aggregate = result.iterator().next();
    assertThat(aggregate, is(notNullValue()));
    assertThat(aggregate.length, is(4));
    assertThat(aggregate, arrayContaining("Simpson", "147", "7008283", String.valueOf(idUser)));
  }

  /**
   * Test of getStatsUserConnexion method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsUserConnexion() throws Exception {
    String idUser = "2";
    Collection[] result =
        SilverStatisticsPeasDAOConnexion.getStatsUserConnexion(dateBegin, dateEnd, idUser);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    Collection<String> dates = (Collection<String>) result[0];
    assertThat(dates, is(notNullValue()));
    assertThat(dates, hasSize(8));
    assertThat(dates, contains("2010-12-01", "2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
        "2011-05-01", "2011-06-01", "2011-07-01"));
    Collection<String> counts = (Collection<String>) result[1];
    assertThat(counts, is(notNullValue()));
    assertThat(counts, hasSize(8));
    assertThat(counts, contains("0", "5", "8", "64", "2", "51", "17", "0"));
  }

  /**
   * Test of getStatsConnexionAllGroup method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexionAllGroup() throws Exception {
    int idGroup = 2;
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup(dateBegin, dateEnd, idGroup);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    String[] aggregate = result.iterator().next();
    assertThat(aggregate, is(notNullValue()));
    assertThat(aggregate.length, is(4));
    assertThat(aggregate, arrayContaining("Children", "545", "8236353", String.valueOf(idGroup)));
  }

  /**
   * Test of getStatsGroupConnexion method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsGroupConnexion() throws Exception {
    String idGroup = "2";
    Collection[] result =
        SilverStatisticsPeasDAOConnexion.getStatsGroupConnexion(dateBegin, dateEnd, idGroup);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    Collection<String> dates = (Collection<String>) result[0];
    assertThat(dates, is(notNullValue()));
    assertThat(dates, hasSize(8));
    assertThat(dates, contains("2010-12-01", "2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
        "2011-05-01", "2011-06-01", "2011-07-01"));
    Collection<String> counts = (Collection<String>) result[1];
    assertThat(counts, is(notNullValue()));
    assertThat(counts, hasSize(8));
    assertThat(counts, contains("0", "110", "67", "149", "58", "64", "97", "0"));

    idGroup = "1";
    result = SilverStatisticsPeasDAOConnexion.getStatsGroupConnexion(dateBegin, dateEnd, idGroup);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    dates = (Collection<String>) result[0];
    assertThat(dates, is(notNullValue()));
    assertThat(dates, hasSize(8));
    assertThat(dates, contains("2010-12-01", "2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
        "2011-05-01", "2011-06-01", "2011-07-01"));
    counts = (Collection<String>) result[1];
    assertThat(counts, is(notNullValue()));
    assertThat(counts, hasSize(8));
    assertThat(counts, contains("0", "223", "129", "289", "394", "115", "115", "0"));
  }

  /**
   * Test of getStatsConnexionGroupAll method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexionGroupAll() throws Exception {
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll(dateBegin, dateEnd);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(3));
    for (String[] aggregate : result) {
      assertThat(aggregate, is(notNullValue()));
      assertThat(aggregate.length, is(4));
      int id = Integer.parseInt(aggregate[3]);
      switch (id) {
        case 1:
          assertThat(aggregate, arrayContaining("Simpsons", "1265", "6397404", "1"));
          break;
        case 2:
          assertThat(aggregate, arrayContaining("Children", "545", "8236353", "2"));
          break;
        case 3:
          assertThat(aggregate, arrayContaining("Parents", "720", "5005423", "3"));
          break;
      }
    }
  }

  /**
   * Test of getStatsConnexionGroupUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexionGroupUser() throws Exception {
    int groupId = 3;
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup(dateBegin, dateEnd, groupId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    Iterator<String[]> iter = result.iterator();
    String[] aggregate = iter.next();
    assertThat(aggregate, is(notNullValue()));
    assertThat(aggregate.length, is(4));
    assertThat(aggregate, arrayContaining("Parents", "720", "5005423", String.valueOf(groupId)));

    groupId = 1;
    result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup(dateBegin, dateEnd, groupId);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    aggregate = result.iterator().next();
    assertThat(aggregate, is(notNullValue()));
    assertThat(aggregate.length, is(4));
    assertThat(aggregate, arrayContaining("Simpsons", "1265", "6397404", String.valueOf(groupId)));
  }

  /**
   * Test of getStatsConnexionUserAll method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexionUserAll() throws Exception {
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll(dateBegin, dateEnd);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(5));
    for (String[] aggregate : result) {
      assertThat(aggregate, is(notNullValue()));
      assertThat(aggregate.length, is(4));
      int id = Integer.parseInt(aggregate[3]);
      switch (id) {
        case 1:
          assertThat(aggregate, arrayContaining("Simpson", "105", "8517612", "1"));
          break;
        case 2:
          assertThat(aggregate, arrayContaining("Simpson", "147", "7008283", "2"));
          break;
        case 3:
          assertThat(aggregate, arrayContaining("Simpson", "293", "8751691", "3"));
          break;
        case 4:
          assertThat(aggregate, arrayContaining("Simpson", "371", "4888906", "4"));
          break;
        case 5:
          assertThat(aggregate, arrayContaining("Simpson", "349", "5129284", "5"));
          break;
      }
    }
  }

  /**
   * Test of getStatsConnexionUserUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsConnexionUserUser() throws Exception {
    int idUser = 2;
    Collection<String[]> result =
        SilverStatisticsPeasDAOConnexion.getStatsConnexionUserUser(dateBegin, dateEnd, idUser);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    for (String[] aggregate : result) {
      assertThat(aggregate, is(notNullValue()));
      assertThat(aggregate.length, is(4));
      assertThat(aggregate, arrayContaining("Simpson", "147", "7008283", "2"));
    }
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetYears() throws Exception {
    Collection<String> result = SilverStatisticsPeasDAOConnexion.getYears();
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasItem("2011"));
  }

  /**
   * Test of getStatsUserFq method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetStatsUserFq() throws Exception {
    int min = 50;
    int max = 100;
    Collection[] result =
        SilverStatisticsPeasDAOConnexion.getStatsUserFq(dateBegin, dateEnd, min, max);
    assertThat(result, is(notNullValue()));
    assertThat(result.length, is(2));
    Collection<String> dates = (Collection<String>) result[0];
    assertThat(dates, is(notNullValue()));
    assertThat(dates, hasSize(8));
    assertThat(dates, contains("2010-12-01", "2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
        "2011-05-01", "2011-06-01", "2011-07-01"));
    Collection<String> counts = (Collection<String>) result[1];
    assertThat(counts, is(notNullValue()));
    assertThat(counts, hasSize(8));
    assertThat(counts, contains("0", "2", "1", "1", "1", "1", "1", "0"));
  }

}
