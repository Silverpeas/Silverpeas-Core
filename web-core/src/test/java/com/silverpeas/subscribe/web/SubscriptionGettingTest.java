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

import com.google.common.collect.Lists;
import com.silverpeas.subscribe.Subscription;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.SubscriptionSubscriber;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.ComponentSubscriptionResource;
import com.silverpeas.subscribe.service.GroupSubscriptionSubscriber;
import com.silverpeas.subscribe.service.NodeSubscriptionResource;
import com.silverpeas.subscribe.service.UserSubscriptionSubscriber;
import com.silverpeas.web.RESTWebServiceTest;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;
import org.silverpeas.admin.user.constant.UserState;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.silverpeas.subscribe.web.SubscriptionTestResources.COMPONENT_ID;
import static com.silverpeas.subscribe.web.SubscriptionTestResources.SUBSCRIPTION_RESOURCE_PATH;
import static com.silverpeas.web.UserPriviledgeValidation.HTTP_SESSIONKEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionGettingTest extends RESTWebServiceTest<SubscriptionTestResources> {

  public SubscriptionGettingTest() {
    super("com.silverpeas.subscribe.web", "spring-subscription-webservice.xml");
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_ID};
  }

  @Before
  public void setup() {
    assertThat(getTestResources().getMockableSubscriptionService(),
        is(SubscriptionServiceFactory.getFactory().getSubscribeService()));
  }

  @Test
  public void getSubscriptionsByANonAuthenticatedUser() {
    WebResource resource = resource();
    try {
      resource.path(SUBSCRIPTION_RESOURCE_PATH).accept(MediaType.APPLICATION_JSON)
          .get(String.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getComponentSubscriptionByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    ComponentSubscription subscription = new ComponentSubscription(user.getId(), COMPONENT_ID);
    Collection<Subscription> subscriptions = Lists.newArrayList((Subscription) subscription);
    when(mockedSubscriptionService.
        getByResource(ComponentSubscriptionResource.from(COMPONENT_ID))).thenReturn(subscriptions);
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    SubscriptionEntity[] entities =
        resource.path(SUBSCRIPTION_RESOURCE_PATH).header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(SubscriptionEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(1));
    assertThat(entities[0], SubscriptionEntityMatcher.matches(subscription));
  }


  @Test
  @SuppressWarnings("unchecked")
  public void getComponentSubscribersByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    List<SubscriptionSubscriber> subscribers = new ArrayList<SubscriptionSubscriber>();
    subscribers.add(UserSubscriptionSubscriber.from("5"));
    subscribers.add(UserSubscriptionSubscriber.from("6"));
    subscribers.add(GroupSubscriptionSubscriber.from("7"));
    subscribers.add(UserSubscriptionSubscriber.from("20"));
    when(mockedSubscriptionService
        .getSubscribers(NodeSubscriptionResource.from(new NodePK("0", COMPONENT_ID))))
        .thenReturn(subscribers);
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    SubscriberEntity[] entities = resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/0")
        .header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(4));
    Iterator<SubscriptionSubscriber> it = subscribers.iterator();
    for (SubscriberEntity entity : entities) {
      assertThat(entity, SubscriberEntityMatcher.matches(it.next()));
    }
  }
}
