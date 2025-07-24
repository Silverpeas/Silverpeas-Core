/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
package org.silverpeas.core.admin;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;

import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Arquillian.class)
public class DomainIT {

  @Inject
  private Administration admin;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("create_space_components_database.sql")
          .loadInitialDataSetFrom("test-spaces_and_components-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DomainIT.class)
        .enableAdministrationFeatures()
        .enablePublicationTemplateFeatures()
        .build();
  }

  @Before
  public void reloadCache() {
    admin.reloadCache();
  }

  @Test
  public void testGetDomain() throws AdminException {
    String domainId = "0";
    Domain domain = new Domain();
    domain.setName("Silverpeas");
    domain.setPropFileName("org.silverpeas.domains.domainSP");
    domain.setDriverClassName("org.silverpeas.core.admin.domain.driver.SilverpeasDomainDriver");
    domain.setAuthenticationServer("autDomainSP");
    domain.setId(domainId);
    Domain savedDomain = admin.getDomain(domainId);
    assertThat(savedDomain, is(domain));
  }

  @Test
  public void testAddDomain() throws AdminException {
    Domain domain = new Domain();
    domain.setName("Test new");
    domain.setDriverClassName("org.silverpeas.core.admin.domain.driver.sqldriver.SQLDriver");
    domain.setPropFileName("org.silverpeas.domains.domainSQL");
    domain.setAuthenticationServer("autDomainSQL");
    domain.setSilverpeasServerURL("http://localhost:8000");
    String domainId = admin.addDomain(domain);
    assertThat(domainId, is(notNullValue()));
    assertThat(domainId, is("1"));
    domain.setId(domainId);
    Domain savedDomain = admin.getDomain(domainId);
    assertThat(savedDomain, is(domain));
  }
}