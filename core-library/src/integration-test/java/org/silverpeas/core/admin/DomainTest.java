/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of
 * the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with this
 * program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.domain.model.Domain;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class DomainTest {

  @Inject
  private AdminController adminController;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("create_space_components_database.sql")
          .loadInitialDataSetFrom("test-spaces_and_components-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DomainTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .build();
  }

  @Before
  public void reloadCache() {
    adminController.reloadAdminCache();
  }

  @Test
  public void testGetDomain() {
    String domainId = "0";
    Domain domain = new Domain();
    domain.setName("Silverpeas");
    domain.setPropFileName("org.silverpeas.domains.domainSP");
    domain.setDriverClassName("org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver");
    domain.setAuthenticationServer("autDomainSP");
    domain.setId(domainId);
    Domain savedDomain = adminController.getDomain(domainId);
    assertThat(savedDomain, is(domain));
  }

  @Test
  public void testAddDomain() {
    Domain domain = new Domain();
    domain.setName("Test new");
    domain.setDriverClassName("org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver");
    domain.setPropFileName("org.silverpeas.domains.domainSQL");
    domain.setAuthenticationServer("autDomainSQL");
    domain.setSilverpeasServerURL("http://localhost:8000");
    String domainId = adminController.addDomain(domain);
    assertThat(domainId, is(notNullValue()));
    assertThat(domainId, is("1"));
    domain.setId(domainId);
    Domain savedDomain = adminController.getDomain(domainId);
    assertThat(savedDomain, is(domain));
  }
}