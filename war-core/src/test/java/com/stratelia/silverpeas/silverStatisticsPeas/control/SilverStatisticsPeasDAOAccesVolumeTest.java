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
import java.util.Hashtable;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-jdbc-datasource.xml"})
public class SilverStatisticsPeasDAOAccesVolumeTest extends AbstractTestDao {
  
  private static final String dateBegin = "2010-12-01";
  private static final String dateEnd = "2011-07-01";

  @Override
  protected String getDatasetFileName() {
    return "test-stats-volume-dataset.xml";
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetYears() throws Exception {
    Collection<String> result = SilverStatisticsPeasDAOAccesVolume.getYears(
        SilverStatisticsPeasDAOAccesVolume.TYPE_VOLUME);
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasItem("2011"));
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetVolumeYears() throws Exception {
    Collection<String> result = SilverStatisticsPeasDAOAccesVolume.getVolumeYears();
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasItem("2011"));
  }


  /**
   * Test of getStatsPublicationsVentil method, of class SilverStatisticsPeasDAOAccesVolume.
   */
  @Test
  public void testGetStatsPublicationsVentil() throws Exception {
    System.out.println("getStatsPublicationsVentil");
    String dateStat = "";
    String currentUserId = "";
    String filterIdGroup = "";
    String filterIdUser = "";
    Hashtable expResult = null;
    Hashtable result = SilverStatisticsPeasDAOAccesVolume.getStatsPublicationsVentil(dateStat,
            currentUserId, filterIdGroup, filterIdUser);
    assertEquals(expResult, result);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }
}
