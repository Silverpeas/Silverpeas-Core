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
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.admin.domain;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaService;
import org.silverpeas.core.admin.quota.service.QuotaService;
import org.silverpeas.core.admin.quota.service.TestDummyQuotaServiceWithAdditionalTools;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Named;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(Arquillian.class)
public class DomainServiceProviderTest {

  @Inject
  @Named("externalDomainService")
  private DomainService externalDomainService;

  @Inject
  @Named("sqlDomainService")
  private DomainService sqlDomainService;

  @Inject
  private QuotaService<UserDomainQuotaKey> userDomainQuotaService;

  @Inject
  private TestDummyQuotaServiceWithAdditionalTools dummyQuotaServiceWithAdditionalTools;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DomainServiceProviderTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.admin.domain");
        }).build();
  }

  @Test
  public void getDomainService() {
    // It exists two implementations today
    assertThat(ServiceProvider.getAllServices(DomainService.class),
        hasSize(DomainType.values().length));
    // Verifying the type for EXTERNAL
    DomainService testExternalDomainService =
        DomainServiceProvider.getDomainService(DomainType.EXTERNAL);
    assertThat(testExternalDomainService, sameInstance(externalDomainService));
    assertThat(testExternalDomainService, instanceOf(ExternalDomainService.class));
    // Verifying the type for SQL (internal)
    DomainService testSqlDomainService = DomainServiceProvider.getDomainService(DomainType.SQL);
    assertThat(testSqlDomainService, sameInstance(sqlDomainService));
    assertThat(testSqlDomainService, instanceOf(SQLDomainService.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void getUserDomainQuotaService() {
    // Verifying that it exists several implementation of QuotaService interface for the test in
    // order to check that among all this implementation, the right aimed is injected.
    assertThat(dummyQuotaServiceWithAdditionalTools, notNullValue());
    // Verifying the injection
    QuotaService<UserDomainQuotaKey> testExternalDomainService =
        DomainServiceProvider.getUserDomainQuotaService();
    assertThat(testExternalDomainService, sameInstance(userDomainQuotaService));
    assertThat(testExternalDomainService, instanceOf(UserDomainQuotaService.class));
  }
}