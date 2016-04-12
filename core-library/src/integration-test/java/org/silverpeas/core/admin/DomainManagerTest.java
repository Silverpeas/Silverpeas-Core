/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.constant.UserAccessLevel;

import javax.inject.Inject;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class DomainManagerTest {

  @Inject
  private AdminController adminController;

  public DomainManagerTest() {
  }

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/admin/domain/driver/create_table.sql")
          .loadInitialDataSetFrom("test-domainmanager-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DomainManagerTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addAsResource("org/silverpeas/core/admin/domain/driver")
        .build();
  }

  @Before
  public void reloadCache() {
    adminController.reloadAdminCache();
  }

  @Test
  public void testGetDomainManagerUser() throws Exception {
    String sUserId = "5";
    String expectedDomainId = "0";
    UserDetail userDetail = adminController.getUserDetail(sUserId);
    assertThat(userDetail.getDomainId(), is(expectedDomainId));
    assertThat(userDetail.getAccessLevel(), is(UserAccessLevel.DOMAIN_ADMINISTRATOR));
    assertThat(adminController.isDomainManagerUser(sUserId, expectedDomainId), is(true));
    assertThat(adminController.isDomainManagerUser("4", expectedDomainId), is(false));
    assertThat(adminController.isDomainManagerUser("0", "0"), is(false));
  }

}