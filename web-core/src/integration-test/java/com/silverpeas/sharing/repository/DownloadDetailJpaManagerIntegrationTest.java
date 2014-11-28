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

package com.silverpeas.sharing.repository;

import com.ninja_squad.dbsetup.operation.Operation;
import com.silverpeas.sharing.model.DownloadDetail;
import com.silverpeas.sharing.services.JpaSharingTicketService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.DataSetTest;
import org.silverpeas.test.WarBuilder4WebCore;
import org.silverpeas.test.rule.DbUnitLoadingRule;
import org.silverpeas.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class DownloadDetailJpaManagerIntegrationTest extends DataSetTest {

  public DownloadDetailJpaManagerIntegrationTest() {
  }

  private DownloadDetailRepository service;

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(this, "create-database.sql", "sharing_dataset.xml");

  @Override
  protected Operation getDbSetupOperations() {
    return null;
  }

  @Before
  public void generalSetUp() throws Exception {
    service = ServiceProvider.getService(DownloadDetailRepository.class);
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4WebCore.onWarFor(JpaSharingTicketService.class).testFocusedOn(warBuilder -> {
      warBuilder.addPackages(true, "com.silverpeas.sharing");
      warBuilder.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }).build();
  }

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

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