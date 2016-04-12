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

import java.util.Calendar;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class SilverStatisticsPeasDAOConnexionEmptyTest {

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("test-stats-connections-empty-dataset.sql");

  @Before
  public void generalSetUp() throws Exception {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WarCore.onWarForTestClass(SilverStatisticsPeasDAOConnexionEmptyTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.web.silverstatistics");
        }).build();
  }

  public SilverStatisticsPeasDAOConnexionEmptyTest() {
  }

  /**
   * Test of getYears method, of class SilverStatisticsPeasDAOConnexion.
   */
  @Test
  public void testGetYears() throws Exception {
    Collection<String> result = SilverStatisticsPeasDAOConnexion.getYears();
    assertThat(result, is(notNullValue()));
    assertThat(result.size(), is(1));
    assertThat(result, hasItem(Integer.toString(Calendar.getInstance().get(Calendar.YEAR))));
  }

}
