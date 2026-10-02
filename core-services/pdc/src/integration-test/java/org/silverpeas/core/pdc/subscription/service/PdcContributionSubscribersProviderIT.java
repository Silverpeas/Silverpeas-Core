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
package org.silverpeas.core.pdc.subscription.service;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.pdc.subscription.test.WarBuilder4Pdc;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Integration tests on the plugging of the PdC into the subscription API of Silverpeas as a
 * provider of the subscribers concerned by a contribution.
 * <p>
 * The classification of a contribution is under the responsibility of the PdC engine, which isn't
 * deployed here: these tests are only about what is done without it. The rules about the
 * subscribers of a classified contribution are covered by the unit tests.
 * </p>
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class PdcContributionSubscribersProviderIT {

  private static final String TABLE_CREATION_SCRIPT = "/create-database.sql";
  private static final String COMPONENT_ID = "kmelia42";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom(TABLE_CREATION_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4Pdc.onWarForTestClass(PdcContributionSubscribersProviderIT.class).build();
  }

  @Test
  public void thePdcIsAProviderOfSubscribersKnownByTheSubscriptionApi() {
    final Set<ContributionSubscribersProvider> providers =
        ServiceProvider.getAllServices(ContributionSubscribersProvider.class);

    assertThat(providers, hasItem(instanceOf(PdcContributionSubscribersProvider.class)));
  }

  @Test
  public void nobodyIsConcernedByAContributionThatIsNotClassified() {
    final ContributionIdentifier contribution =
        ContributionIdentifier.from(COMPONENT_ID, "23", "Publication");

    assertThat(ResourceSubscriptionProvider.getSubscribersConcernedBy(contribution), is(empty()));
  }

  /**
   * Looking for the subscribers concerned by a contribution mustn't register its application as a
   * provider of contents classified on the PdC.
   */
  @Test
  public void lookingForTheSubscribersDoesntRegisterTheApplicationInTheContentManager()
      throws Exception {
    final ContributionIdentifier contribution =
        ContributionIdentifier.from(COMPONENT_ID, "23", "Publication");

    ResourceSubscriptionProvider.getSubscribersConcernedBy(contribution);

    final long registered = JdbcSqlQuery.countAll()
        .from("SB_ContentManager_Instance")
        .where("componentId = ?", COMPONENT_ID)
        .execute();
    assertThat(registered, is(0L));
  }
}
