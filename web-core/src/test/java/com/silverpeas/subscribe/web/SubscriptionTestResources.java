/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.subscribe.web;

import com.silverpeas.web.TestResources;
import com.silverpeas.subscribe.MockableSubscriptionService;
import javax.inject.Inject;
import javax.inject.Named;
import static org.junit.Assert.*;

/**
 * Wrapper of resources required by the unit tests on the subscription web service.
 */
@Named(TestResources.TEST_RESOURCES_NAME)
public class SubscriptionTestResources extends TestResources {
  
  public static final String COMPONENT_ID = "questionReply12";
  public static final String KMELIA_ID = "kmelia12";
  public static final String SUBSCRIBE_RESOURCE_PATH = "subscribe/" + COMPONENT_ID;
  public static final String KMELIA_SUBSCRIBE_RESOURCE_PATH = "subscribe/" + COMPONENT_ID;
  public  static final String SUBSCRIPTION_RESOURCE_PATH = "subscriptions/" + COMPONENT_ID;
  public static final String KMELIA_SUBSCRIPTION_RESOURCE_PATH = "subscriptions/" + COMPONENT_ID;
  public static final String UNSUBSCRIBE_RESOURCE_PATH = "unsubscribe/" + COMPONENT_ID;
  public static final String KMELIA_UNSUBSCRIBE_RESOURCE_PATH = "unsubscribe/" + COMPONENT_ID;
  
  @Inject
  private MockableSubscriptionService subscriptionService;
  
  public MockableSubscriptionService getMockableSubscriptionService() {
    assertNotNull(subscriptionService);
    return subscriptionService;
  }
}
