/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.subscription;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.DefaultResourceSubscriptionService;
import org.silverpeas.core.subscription.service.PKSubscription;
import org.silverpeas.core.subscription.service.PKSubscriptionResource;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Map;

public abstract class AbstractCommonSubscriptionIntegrationTest {

  private static final String TABLE_CREATION_SCRIPT = "/node-create-database.sql";
  private static final String DATASET_XML_SCRIPT =
      "/org/silverpeas/core/subscription/service/node-actors-test-dataset.xml";

  public static final String GROUPID_WITH_ONE_USER = "55";
  public static final String USERID_OF_GROUP_WITH_ONE_USER = "userFromGroupOnly_GroupId_55";
  protected static final String INSTANCE_ID = "kmelia60";

  protected final static SubscriberType[] validSubscriberTypes = SubscriberType.getValidValues()
      .toArray(new SubscriberType[0]);

  /**
   * The resource is a forum. Used by component instances handling forums.
   */
  protected static final SubscriptionContributionType FORUM = new SubscriptionContributionType() {
    private static final long serialVersionUID = -1130015664194572265L;

    @Override
    public int priority() {
      return 100;
    }

    @Override
    public String getName() {
      return "FORUM";
    }
  };

  /**
   * The resource is a message in a given forum. Used by component instances handling forums.
   */
  protected static final SubscriptionContributionType FORUM_MESSAGE = new SubscriptionContributionType() {
    private static final long serialVersionUID = 6385822460694305970L;

    @Override
    public int priority() {
      return 101;
    }

    @Override
    public String getName() {
      return "FORUM_MESSAGE";
    }
  };

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule(TABLE_CREATION_SCRIPT, DATASET_XML_SCRIPT);

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(AbstractCommonSubscriptionIntegrationTest.class)
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addIndexEngineFeatures()
        .addWysiwygFeatures()
        .addPublicationTemplateFeatures()
        .addAsResource(DATASET_XML_SCRIPT.substring(1))
        .testFocusedOn(war -> war
            .addPackages(true, "org.silverpeas.core.node", "org.silverpeas.core.subscription")
            .addAsResource("node-create-database.sql"))
        .build();
  }

  @Before
  public void setup() throws Exception {
    ServiceProvider.getService(DefaultResourceSubscriptionService.class).init();
    SubscriptionFactory.get().register(FORUM,
        (r, s, i) -> new TestForumSubscriptionResource(new ResourceReference(r, i)),
        (s, r, c) -> new TestForumSubscription(s, (TestForumSubscriptionResource) r, c));
    SubscriptionFactory.get().register(FORUM_MESSAGE,
        (r, s, i) -> new TestForumMessageSubscriptionResource(new ResourceReference(r, i)),
        (s, r, c) -> new TestForumMessageSubscription(s, (TestForumMessageSubscriptionResource) r, c));
  }

  @SuppressWarnings("unchecked")
  @After
  public void clear() throws Exception {
    Map<String, ResourceSubscriptionService> map = (Map<String, ResourceSubscriptionService>) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    SilverLogger.getLogger(this).info(
        "Clearing ResourceSubscriptionProvider.componentImplementations which contains {0} " +
            "implementation {0,choice, 1#instance| 1<instances}", map.size());
    map.clear();
  }

  public static class TestForumSubscriptionResource extends PKSubscriptionResource {
    public TestForumSubscriptionResource(final ResourceReference pk) {
      super(pk, FORUM);
    }
  }
  public static class TestForumSubscription extends PKSubscription<TestForumSubscriptionResource> {
    public TestForumSubscription(final String subscriberId,
        final TestForumSubscriptionResource resource) {
      super(subscriberId, resource);
    }
    public TestForumSubscription(final SubscriptionSubscriber subscriber,
        final TestForumSubscriptionResource resource, final String creatorId) {
      super(subscriber, resource, creatorId);
    }
  }

  public static class TestForumMessageSubscriptionResource extends PKSubscriptionResource {
    public TestForumMessageSubscriptionResource(final ResourceReference pk) {
      super(pk, FORUM_MESSAGE);
    }
  }
  public static class TestForumMessageSubscription extends PKSubscription<TestForumMessageSubscriptionResource> {
    public TestForumMessageSubscription(final SubscriptionSubscriber subscriber,
        final TestForumMessageSubscriptionResource resource, final String creatorId) {
      super(subscriber, resource, creatorId);
    }
  }
}
