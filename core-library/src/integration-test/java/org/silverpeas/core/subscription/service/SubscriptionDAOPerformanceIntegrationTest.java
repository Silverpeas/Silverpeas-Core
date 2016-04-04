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
package org.silverpeas.core.subscription.service;

import org.silverpeas.core.subscription.AbstractCommonSubscriptionIntegrationTest;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.node.model.NodePK;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;

import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.test.rule.DbSetupRule.getSafeConnection;

/**
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class SubscriptionDAOPerformanceIntegrationTest
    extends AbstractCommonSubscriptionIntegrationTest {

  private static SubscriptionDao subscriptionDao = new SubscriptionDao();
  private static final List<NodeSubscriptionResource> nodePks = Arrays.asList(
      NodeSubscriptionResource.from(new NodePK("0", "100", "kmelia60")),
      NodeSubscriptionResource.from(new NodePK("1", "100", "kmelia60")),
      NodeSubscriptionResource.from(new NodePK("10", "100", "kmelia60")),
      NodeSubscriptionResource.from(new NodePK("20", "100", "kmelia60")),
      NodeSubscriptionResource.from(new NodePK("30", "100", "kmelia60")),
      NodeSubscriptionResource.from(new NodePK("40", "100", "kmelia60")));

  private static final String TABLE_CREATION_SCRIPT = "/node-create-database.sql";
  private static final String DATASET_XML_SCRIPT = "node-actors-performance-test-dataset.xml";

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Test
  public void testGetActorPKsByNodePKsInLoop() throws Exception {
    try (Connection connection = getSafeConnection()) {
      long startTime = System.currentTimeMillis();
      for (int i = 0; i < 10000; i++) {
        Collection<SubscriptionSubscriber> result = subscriptionDao.getSubscribers(connection,
            nodePks, null);
        assertThat(result, hasSize(15));
        assertThat(result, hasItem("1"));
        assertThat(result, hasItem("2"));
        assertThat(result, hasItem("3"));
        assertThat(result, hasItem("4"));
        assertThat(result, hasItem("5"));
        assertThat(result, hasItem("11"));
        assertThat(result, hasItem("12"));
        assertThat(result, hasItem("13"));
        assertThat(result, hasItem("14"));
        assertThat(result, hasItem("15"));
        assertThat(result, hasItem("21"));
        assertThat(result, hasItem("22"));
        assertThat(result, hasItem("23"));
        assertThat(result, hasItem("24"));
        assertThat(result, hasItem("25"));
      }
      long duration = System.currentTimeMillis() - startTime;
      System.out.println("GetActorPKsByNodePKsInLoop " + duration);
    }
  }

  /**
   * Centralization.
   *
   * @param userId
   * @return
   */
  private Matcher<Iterable<? super UserSubscriptionSubscriber>> hasItem(String userId) {
    return Matchers.hasItem(UserSubscriptionSubscriber.from(userId));
  }
}
