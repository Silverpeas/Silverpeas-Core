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
package org.silverpeas.core.subscription;

import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.DefaultResourceSubscriptionService;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
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
      .toArray(new SubscriberType[SubscriberType.getValidValues().size()]);

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
        .addAsResource(DATASET_XML_SCRIPT.substring(1))
        .testFocusedOn(war -> war
            .addPackages(true, "org.silverpeas.core.node", "org.silverpeas.core.subscription")
            .addAsResource("node-create-database.sql"))
        .build();
  }

  @Before
  public void setup() throws Exception {
    ServiceProvider.getService(DefaultResourceSubscriptionService.class).init();
  }

  @After
  public void clear() throws Exception {
    Map map = (Map) FieldUtils
        .readDeclaredStaticField(ResourceSubscriptionProvider.class, "componentImplementations",
            true);
    SilverLogger.getLogger(this).info(
        "Clearing ResourceSubscriptionProvider.componentImplementations which contains {0} " +
            "implementation {0,choice, 1#instance| 1<instances}", map.size());
    map.clear();
  }
}