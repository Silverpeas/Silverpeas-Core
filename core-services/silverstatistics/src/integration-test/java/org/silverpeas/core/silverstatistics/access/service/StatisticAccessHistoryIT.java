/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.silverstatistics.access.service;

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.silverstatistics.access.model.HistoryByUser;
import org.silverpeas.core.silverstatistics.test.WarBuilder4Statistics;
import org.silverpeas.core.test.integration.DataSetTest;
import org.silverpeas.core.util.SilverpeasList;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests on the access history of a resource, the readers being the ones having
 * accessed it.
 */
@RunWith(Arquillian.class)
public class StatisticAccessHistoryIT extends DataSetTest {

  private static final String PUBLICATION = "Publication";
  private static final int ACCESS = 1;
  private static final ResourceReference THE_PUBLICATION =
      new ResourceReference("42", "kmelia1");

  private static final Operation DROP_ALL =
      Operations.sql("DROP TABLE IF EXISTS SB_Statistic_History");

  private static final Operation TABLE_CREATION = Operations.sql(
      "CREATE TABLE IF NOT EXISTS SB_Statistic_History (" +
          "dateStat varchar(10) NOT NULL," +
          "heureStat varchar(10) NOT NULL," +
          "userId varchar(100) NOT NULL," +
          "resourceId varchar(50) NOT NULL," +
          "componentId varchar(50) NOT NULL," +
          "actionType int NOT NULL," +
          "resourceType varchar(50) NOT NULL)");

  /**
   * The publication has been read twice by the user 1, once by the user 2 and twice by the user 3.
   * The three last accesses are then the one of the user 2, followed by the one of the user 1 and
   * at last by the one of the user 3. The three last rows are noise: they are about another
   * resource, another component instance and another action.
   */
  private static final Operation DATA_SET_UP = Operations.insertInto("SB_Statistic_History")
      .columns("dateStat", "heureStat", "userId", "resourceId", "componentId", "actionType",
          "resourceType")
      .values("2026/09/19", "07:00", "3", "42", "kmelia1", ACCESS, PUBLICATION)
      .values("2026/09/19", "08:00", "3", "42", "kmelia1", ACCESS, PUBLICATION)
      .values("2026/09/19", "09:00", "1", "42", "kmelia1", ACCESS, PUBLICATION)
      .values("2026/09/20", "10:00", "1", "42", "kmelia1", ACCESS, PUBLICATION)
      .values("2026/09/21", "09:00", "2", "42", "kmelia1", ACCESS, PUBLICATION)
      .values("2026/09/21", "23:00", "4", "43", "kmelia1", ACCESS, PUBLICATION)
      .values("2026/09/21", "23:00", "5", "42", "kmelia2", ACCESS, PUBLICATION)
      .values("2026/09/21", "23:00", "6", "42", "kmelia1", 2, PUBLICATION)
      .build();

  @Inject
  private StatisticService statisticService;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Statistics.onWarForTestClass(StatisticAccessHistoryIT.class)
        .testFocusedOn(war -> war.addPackages(true, "org.silverpeas.core.silverstatistics"))
        .build();
  }

  @Override
  protected Operation getDbSetupInitializations() {
    return Operations.sequenceOf(DROP_ALL, TABLE_CREATION, DATA_SET_UP);
  }

  @Test
  public void eachReaderIsGotOnceFromTheMostRecentAccessToTheOldestOne() {
    SilverpeasList<HistoryByUser> readers =
        statisticService.getHistoryByUser(THE_PUBLICATION, ACCESS, PUBLICATION, null, null);

    assertThat(readerIds(readers), contains("2", "1", "3"));
    assertThat(readers.get(0).getNbAccess(), is(1));
    assertThat(readers.get(1).getNbAccess(), is(2));
  }

  @Test
  public void theExcludedReadersAreNotGot() {
    SilverpeasList<HistoryByUser> readers = statisticService.getHistoryByUser(THE_PUBLICATION,
        ACCESS, PUBLICATION, singleton("2"), null);

    assertThat(readerIds(readers), contains("1", "3"));
  }

  @Test
  public void theReadersCanBeGotPageByPage() {
    SilverpeasList<HistoryByUser> readers = statisticService.getHistoryByUser(THE_PUBLICATION,
        ACCESS, PUBLICATION, null, new PaginationPage(1, 2));

    assertThat(readerIds(readers), contains("2", "1"));

    readers = statisticService.getHistoryByUser(THE_PUBLICATION, ACCESS, PUBLICATION, null,
        new PaginationPage(2, 2));

    assertThat(readerIds(readers), contains("3"));

    // asking for a page beyond the last reader must just return no reader at all
    readers = statisticService.getHistoryByUser(THE_PUBLICATION, ACCESS, PUBLICATION, null,
        new PaginationPage(3, 2));

    assertThat(readerIds(readers), is(empty()));
  }

  @Test
  public void noReaderIsGotWhenAllOfThemAreExcluded() {
    SilverpeasList<HistoryByUser> readers = statisticService.getHistoryByUser(THE_PUBLICATION,
        ACCESS, PUBLICATION, List.of("1", "2", "3"), new PaginationPage(2, 2));

    assertThat(readerIds(readers), is(empty()));
  }

  private List<String> readerIds(final List<HistoryByUser> readers) {
    return readers.stream().map(HistoryByUser::getUserId).collect(Collectors.toList());
  }
}
