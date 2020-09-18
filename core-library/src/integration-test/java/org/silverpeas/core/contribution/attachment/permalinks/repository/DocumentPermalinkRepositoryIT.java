/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.contribution.attachment.permalinks.repository;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.DbSetupTracker;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.permalinks.model.DocumentPermalink;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Arquillian.class)
public class DocumentPermalinkRepositoryIT {

  private DocumentPermalinkRepository manager;

  public DocumentPermalinkRepositoryIT() {
  }

  @Resource(lookup = "java:/datasources/silverpeas")
  private DataSource dataSource;
  private DbSetupTracker dbSetupTracker = new DbSetupTracker();

  public static final Operation TABLES_CREATION = Operations.sql(
      "CREATE TABLE IF NOT EXISTS permalinks_document (" +
          "documentId INT PRIMARY KEY NOT NULL, documentUuid VARCHAR (50) NOT NULL)");
  public static final Operation CLEAN_UP = Operations.deleteAllFrom("permalinks_document");
  public static final Operation INSERT_DATA =
      Operations.insertInto("permalinks_document").columns("documentId", "documentUuid")
          .values(1, "ilovesilverpeas").build();

  @Before
  public void prepareDataSource() {
    Operation preparation = Operations.sequenceOf(TABLES_CREATION, CLEAN_UP, INSERT_DATA);
    DbSetup dbSetup = new DbSetup(new DataSourceDestination(dataSource), preparation);
    dbSetupTracker.launchIfNecessary(dbSetup);
    manager = ServiceProvider.getService(DocumentPermalinkRepository.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DocumentPermalinkRepositoryIT.class)
        .addJpaPersistenceFeatures()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.contribution.attachment.permalinks");
    }).build();
  }


  @Test
  public void testFindById() {
    DocumentPermalink permalink = manager.getById("1");
    assertThat(permalink, is(notNullValue()));
    assertThat(permalink.getId(), is("1"));
    assertThat(permalink.getUuid(), is("ilovesilverpeas"));
  }

}
