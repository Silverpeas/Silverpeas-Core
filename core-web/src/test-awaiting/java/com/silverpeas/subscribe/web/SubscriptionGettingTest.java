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

package org.silverpeas.core.subscription.web;

import org.silverpeas.core.subscription.Subscription;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.constant.SubscriberType;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.ComponentSubscriptionResource;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.NodeSubscriptionResource;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberMapBySubscriberType;
import com.silverpeas.util.CollectionUtil;
import com.silverpeas.web.RESTWebServiceTest;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.user.constant.UserState;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collection;
import java.util.Iterator;

import org.silverpeas.util.CollectionUtil;
import static org.silverpeas.core.subscription.web.SubscriptionTestResources.COMPONENT_ID;
import static org.silverpeas.core.subscription.web.SubscriptionTestResources.SUBSCRIPTION_RESOURCE_PATH;
import static com.silverpeas.web.UserPrivilegeValidation.HTTP_SESSIONKEY;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SubscriptionGettingTest extends RESTWebServiceTest<SubscriptionTestResources> {

  public SubscriptionGettingTest() {
    super("org.silverpeas.core.subscription.web", "spring-subscription-webservice.xml");
  }

  @Before
  public void specificSetup() {
    when(getOrganizationControllerMock().getAllUsersOfGroup(anyString()))
        .thenReturn(new UserDetail[0]);
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[]{COMPONENT_ID};
  }

  @Before
  public void setup() {
    assertThat(getTestResources().getMockableSubscriptionService(),
        is(SubscriptionServiceProvider.getFactory().getSubscribeService()));
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
    Collection<Subscription> subscriptions = CollectionUtil.asList((Subscription) subscription);
    when(mockedSubscriptionService.
        getByResource(ComponentSubscriptionResource.from(COMPONENT_ID))).thenReturn(subscriptions);
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    SubscriptionEntity[] entities = resource.path(SUBSCRIPTION_RESOURCE_PATH)
        .header(HTTP_SESSIONKEY, sessionKey).
        accept(MediaType.APPLICATION_JSON).get(SubscriptionEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(1));
    assertThat(entities[0], SubscriptionEntityMatcher.matches(subscription));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getNodeSubscribersByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();
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

  @Test
  @SuppressWarnings("unchecked")
  public void getComponentSubscribersWithInheritanceByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();
    subscribers.add(UserSubscriptionSubscriber.from("5"));
    subscribers.add(UserSubscriptionSubscriber.from("6"));
    subscribers.add(GroupSubscriptionSubscriber.from("7"));
    subscribers.add(UserSubscriptionSubscriber.from("20"));
    when(mockedSubscriptionService.getSubscribers(ComponentSubscriptionResource.from(COMPONENT_ID)))
        .thenReturn(subscribers);
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);

    // Detailed result
    SubscriberEntity[] entities =
        resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/component/inheritance")
            .queryParam("existenceIndicatorOnly", "false").header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(4));
    SubscriptionSubscriberMapBySubscriberType indexedExpected = subscribers.indexBySubscriberType();
    for (SubscriberEntity entity : entities) {
      if (entity.isGroup()) {
        assertThat(indexedExpected.get(SubscriberType.GROUP).getAllIds(), hasItem(entity.getId()));
      } else {
        assertThat(indexedExpected.get(SubscriberType.USER).getAllIds(), hasItem(entity.getId()));
      }
    }

    // Existence result
    boolean existenceResult =
        resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/component/inheritance")
            .queryParam("existenceIndicatorOnly", "true").header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(Boolean.class);
    assertThat(existenceResult, is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void
  getComponentSubscribersWithInheritanceAndUninterpretedResourceIdByAnAuthenticatedUser()
      throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();
    subscribers.add(UserSubscriptionSubscriber.from("5"));
    subscribers.add(UserSubscriptionSubscriber.from("6"));
    subscribers.add(GroupSubscriptionSubscriber.from("7"));
    subscribers.add(UserSubscriptionSubscriber.from("20"));
    when(mockedSubscriptionService.getSubscribers(ComponentSubscriptionResource.from(COMPONENT_ID)))
        .thenReturn(subscribers);
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);

    // Detailed result
    SubscriberEntity[] entities = resource.path(
        SUBSCRIPTION_RESOURCE_PATH + "/subscribers/component/inheritance/uninterpretedResourceId")
        .header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(4));
    SubscriptionSubscriberMapBySubscriberType indexedExpected = subscribers.indexBySubscriberType();
    for (SubscriberEntity entity : entities) {
      if (entity.isGroup()) {
        assertThat(indexedExpected.get(SubscriberType.GROUP).getAllIds(), hasItem(entity.getId()));
      } else {
        assertThat(indexedExpected.get(SubscriberType.USER).getAllIds(), hasItem(entity.getId()));
      }
    }

    // Existence result
    boolean existenceResult = resource.path(
        SUBSCRIPTION_RESOURCE_PATH + "/subscribers/component/inheritance/uninterpretedResourceId")
        .queryParam("existenceIndicatorOnly", "true").header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(Boolean.class);
    assertThat(existenceResult, is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getNodeSubscribersWithInheritanceAndWrongNodeIdByAnAuthenticatedUser()
      throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    final SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();
    when(mockedSubscriptionService.getSubscribers(Matchers.any(NodeSubscriptionResource.class)))
        .thenAnswer(new Answer<SubscriptionSubscriberList>() {
          @Override
          public SubscriptionSubscriberList answer(final InvocationOnMock invocation)
              throws Throwable {
            if (invocation.getArguments()[0] instanceof NodeSubscriptionResource) {
              NodeSubscriptionResource nodeSubscriptionResource =
                  (NodeSubscriptionResource) invocation.getArguments()[0];
              if (nodeSubscriptionResource.getId().equals("26") &&
                  nodeSubscriptionResource.getInstanceId().equals(COMPONENT_ID)) {
                subscribers.add(UserSubscriptionSubscriber.from("5"));
                subscribers.add(UserSubscriptionSubscriber.from("6"));
                subscribers.add(GroupSubscriptionSubscriber.from("7"));
                subscribers.add(UserSubscriptionSubscriber.from("20"));
              }
            }
            return subscribers;
          }
        });
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);

    // Detailed result
    SubscriberEntity[] entities =
        resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/NODE/inheritance/wrongId")
            .header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(0));

    // Existence result
    boolean existenceResult =
        resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/NODE/inheritance/wrongId")
            .queryParam("existenceIndicatorOnly", "true").header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(Boolean.class);
    assertThat(existenceResult, is(false));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getNodeSubscribersWithInheritanceByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    final SubscriptionSubscriberList subscribers = new SubscriptionSubscriberList();
    when(mockedSubscriptionService.getSubscribers(Matchers.any(NodeSubscriptionResource.class)))
        .thenAnswer(new Answer<SubscriptionSubscriberList>() {
          @Override
          public SubscriptionSubscriberList answer(final InvocationOnMock invocation)
              throws Throwable {
            if (invocation.getArguments()[0] instanceof NodeSubscriptionResource) {
              NodeSubscriptionResource nodeSubscriptionResource =
                  (NodeSubscriptionResource) invocation.getArguments()[0];
              if (nodeSubscriptionResource.getId().equals("26") &&
                  nodeSubscriptionResource.getInstanceId().equals(COMPONENT_ID)) {
                subscribers.add(UserSubscriptionSubscriber.from("5"));
                subscribers.add(UserSubscriptionSubscriber.from("6"));
                subscribers.add(GroupSubscriptionSubscriber.from("7"));
                subscribers.add(UserSubscriptionSubscriber.from("20"));
              }
            }
            return subscribers;
          }
        });
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);

    // Detailed result
    SubscriberEntity[] entities =
        resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/NODE/inheritance/26")
            .queryParam("existenceIndicatorOnly", "toto").header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
    assertNotNull(entities);
    assertThat(entities.length, is(4));
    SubscriptionSubscriberMapBySubscriberType indexedExpected = subscribers.indexBySubscriberType();
    for (SubscriberEntity entity : entities) {
      if (entity.isGroup()) {
        assertThat(indexedExpected.get(SubscriberType.GROUP).getAllIds(), hasItem(entity.getId()));
      } else {
        assertThat(indexedExpected.get(SubscriberType.USER).getAllIds(), hasItem(entity.getId()));
      }
    }

    // Existence result
    boolean existenceResult =
        resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/NODE/inheritance/26")
            .queryParam("existenceIndicatorOnly", "true").header(HTTP_SESSIONKEY, sessionKey).
            accept(MediaType.APPLICATION_JSON).get(Boolean.class);
    assertThat(existenceResult, is(true));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getUnknownResourceSubscribersWithInheritanceByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);

    // Detailed result
    try {
      resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/wrong_type/inheritance/26")
          .queryParam("existenceIndicatorOnly", "toto").header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
      fail("a NOT FOUND web error should be returned");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getNodeSubscribersWithInheritanceByAnAuthenticatedUserButMissingNodeId() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    user.setState(UserState.VALID);
    String sessionKey = authenticate(user);

    // Detailed result
    try {
      resource.path(SUBSCRIPTION_RESOURCE_PATH + "/subscribers/node/inheritance")
          .queryParam("existenceIndicatorOnly", "toto").header(HTTP_SESSIONKEY, sessionKey).
          accept(MediaType.APPLICATION_JSON).get(SubscriberEntity[].class);
      fail("a NOT FOUND web error should be returned");
    } catch (UniformInterfaceException ex) {
      int receivedStatus = ex.getResponse().getStatus();
      int notFound = Status.NOT_FOUND.getStatusCode();
      assertThat(receivedStatus, is(notFound));
    }
  }

}
