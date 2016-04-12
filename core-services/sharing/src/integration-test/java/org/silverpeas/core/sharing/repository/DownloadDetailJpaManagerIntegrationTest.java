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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

package org.silverpeas.core.sharing.repository;

import org.silverpeas.core.sharing.model.DownloadDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.sharing.test.WarBuilder4Sharing;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class DownloadDetailJpaManagerIntegrationTest {

  private DownloadDetailRepository service;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "sharing_dataset.xml");

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(DownloadDetailRepository.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Sharing.onWarForTestClass(DownloadDetailJpaManagerIntegrationTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.sharing");
        }).build();
  }

  @Test
  public void testGetAll() throws Exception {
    List<DownloadDetail> downloads = service.getAll();
    assertThat(2, is(downloads.size()));
  }

  @Test
  public void testGetById() throws Exception {
    DownloadDetail download = service.getById("1");
    assertThat(download, is(notNullValue()));
    assertThat(download.getKeyFile(), is("965e985d-c711-47b3-a467-62779505965e"));
  }
}