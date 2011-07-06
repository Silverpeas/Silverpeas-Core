/*
 *  Copyright (C) 2000 - 2011 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.com/legal/licensing"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

import com.silverpeas.components.model.AbstractTestDao;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.Collection;
import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-jdbc-datasource.xml"})
public class SilverStatisticsPeasDAOConnexionTest extends AbstractTestDao {

  @Override
  protected String getDatasetFileName() {
    return "test-stats-connections-dataset.xml";
  }

  public SilverStatisticsPeasDAOConnexionTest() {
  }

  /**
   * Test of getStatsConnexionAllAll method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsConnexionAllAll() throws Exception {
    String dateBegin = "2011-01-01";
    String dateEnd = "2011-07-31";
    Collection<String[]> result = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllAll(dateBegin, dateEnd);
    assertThat(result, is (notNullValue()));
    assertEquals(result.size(), is(35));
    String[] firstInfo = result.iterator().next();
    assertNotNull(firstInfo);
    assertThat(firstInfo.length, is(4));
    assertThat(firstInfo, arrayContaining("2011-01-01","1" ,"34","353282954"));
  }

  /**
   * Test of getStatsUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  @SuppressWarnings("unchecked")
  public void testGetStatsUser() throws Exception {
    String dateBegin = "2011-01-01";
    String dateEnd = "2011-07-31";
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsUser(dateBegin, dateEnd);
    assertThat(result, is (notNullValue()));
    assertThat(result.length, is(2));
    Collection<String> dates = (Collection<String>)result[0];
    assertNotNull(dates);
    assertThat(dates, hasSize(7));
    assertThat(dates, hasItems(new String[]{"2011-01-01", "2011-02-01", "2011-03-01", "2011-04-01",
              "2011-05-01", "2011-06-01"}));
    Collection<String> counts = (Collection<String>)result[1];
    assertNotNull(counts);
    assertThat(counts, hasSize(7));
   // assertThat(counts, hasItems(new String[]{"223", "129", "289", "394", "115", "115"}));
  }

  /**
   * Test of getStatsConnexion method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsConnexion() throws Exception {
    System.out.println("getStatsConnexion");
    String dateBegin = "2011-01-01";
    String dateEnd = "2011-07-31";
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsConnexion(dateBegin, dateEnd);
    assertNotNull(result);
  }

  /**
   * Test of getStatsConnexionAllUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsConnexionAllUser() throws Exception {
    System.out.println("getStatsConnexionAllUser");
    String dateBegin = "";
    String dateEnd = "";
    int idUser = 0;
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllUser(dateBegin, dateEnd,
            idUser);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsUserConnexion method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsUserConnexion() throws Exception {
    System.out.println("getStatsUserConnexion");
    String dateBegin = "";
    String dateEnd = "";
    String idUser = "";
    Collection[] expResult = null;
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsUserConnexion(dateBegin, dateEnd,
            idUser);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsConnexionAllGroup method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsConnexionAllGroup() throws Exception {
    System.out.println("getStatsConnexionAllGroup");
    String dateBegin = "";
    String dateEnd = "";
    int idGroup = 0;
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getStatsConnexionAllGroup(dateBegin,
            dateEnd, idGroup);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsGroupConnexion method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsGroupConnexion() throws Exception {
    System.out.println("getStatsGroupConnexion");
    String dateBegin = "";
    String dateEnd = "";
    String idGroup = "";
    Collection[] expResult = null;
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsGroupConnexion(dateBegin, dateEnd,
            idGroup);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsConnexionGroupAll method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsConnexionGroupAll() throws Exception {
    System.out.println("getStatsConnexionGroupAll");
    String dateBegin = "";
    String dateEnd = "";
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupAll(dateBegin,
            dateEnd);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsConnexionGroupUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  // @Test
  public void testGetStatsConnexionGroupUser() throws Exception {
    System.out.println("getStatsConnexionGroupUser");
    String dateBegin = "";
    String dateEnd = "";
    int idUser = 0;
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getStatsConnexionGroupUser(dateBegin,
            dateEnd, idUser);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsConnexionUserAll method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsConnexionUserAll() throws Exception {
    System.out.println("getStatsConnexionUserAll");
    String dateBegin = "";
    String dateEnd = "";
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getStatsConnexionUserAll(dateBegin, dateEnd);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsConnexionUserUser method, of class SilverStatisticsPeasDAOConnexion.
   */
  // @Test
  public void testGetStatsConnexionUserUser() throws Exception {
    System.out.println("getStatsConnexionUserUser");
    String dateBegin = "";
    String dateEnd = "";
    int idUser = 0;
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getStatsConnexionUserUser(dateBegin,
            dateEnd, idUser);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOConnexion.
   */
  // @Test
  public void testGetYears() throws Exception {
    System.out.println("getYears");
    Collection expResult = null;
    Collection result = SilverStatisticsPeasDAOConnexion.getYears();
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of getStatsUserFq method, of class SilverStatisticsPeasDAOConnexion.
   */
  //@Test
  public void testGetStatsUserFq() throws Exception {
    System.out.println("getStatsUserFq");
    String dateBegin = "";
    String dateEnd = "";
    int min = 0;
    int max = 0;
    Collection[] expResult = null;
    Collection[] result = SilverStatisticsPeasDAOConnexion.getStatsUserFq(dateBegin, dateEnd, min,
            max);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
