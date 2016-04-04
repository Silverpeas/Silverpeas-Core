/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.subscription.web;

import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.SubscriptionServiceProvider;
import org.silverpeas.core.subscription.service.ComponentSubscription;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.NodeSubscription;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import com.silverpeas.web.RESTWebServiceTest;
import com.silverpeas.web.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.node.model.NodePK;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import static org.silverpeas.core.subscription.web.SubscriptionTestResources.COMPONENT_ID;
import static org.silverpeas.core.subscription.web.SubscriptionTestResources.UNSUBSCRIBE_RESOURCE_PATH;
import static com.silverpeas.web.UserPrivilegeValidation.HTTP_SESSIONKEY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class UnsubscribePostTest extends RESTWebServiceTest<SubscriptionTestResources> {

  public UnsubscribePostTest() {
    super("org.silverpeas.core.subscription.web", "spring-subscription-webservice.xml");
  }

  @Override
  public String[] getExistingComponentInstances() {
    return new String[] {COMPONENT_ID};
  }

  @Before
  public void setup() {
    assertThat(getTestResources().getMockableSubscriptionService(),
            is(SubscriptionServiceProvider.getSubscribeService()));
  }

  @Test
  public void subscribeForAnNotAuthenticatedUser() {
    WebResource resource = resource();
    try {
      String result = resource.path(UNSUBSCRIBE_RESOURCE_PATH).accept(MediaType.APPLICATION_JSON).post(
              String.class);
      fail("A non authenticated user shouldn't access the comment");
    } catch (UniformInterfaceException ex) {
      int recievedStatus = ex.getResponse().getStatus();
      int unauthorized = Status.UNAUTHORIZED.getStatusCode();
      assertThat(recievedStatus, is(unauthorized));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unsubscribeFromComponentByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    ComponentSubscription subscription = new ComponentSubscription(user.getId(), COMPONENT_ID);
    getTestResources().getMockableSubscriptionService().setImplementation(mockedSubscriptionService);
    String[] result = resource.path(UNSUBSCRIBE_RESOURCE_PATH).header(HTTP_SESSIONKEY, sessionKey)
        .accept(MediaType.APPLICATION_JSON).post(String[].class);
    assertThat(result[0], is("OK"));
    verify(mockedSubscriptionService, only()).unsubscribe(subscription);
    verify(mockedSubscriptionService, times(1)).unsubscribe(subscription);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unsubscribeOtherUserFromComponentByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    ComponentSubscription subscription =
        new ComponentSubscription(UserSubscriptionSubscriber.from("6"), COMPONENT_ID, "2");
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    String[] result =
        resource.path(UNSUBSCRIBE_RESOURCE_PATH + "/user/6").header(HTTP_SESSIONKEY, sessionKey)
            .accept(MediaType.APPLICATION_JSON).post(String[].class);
    assertThat(result[0], is("OK"));
    verify(mockedSubscriptionService, only()).unsubscribe(subscription);
    verify(mockedSubscriptionService, times(1)).unsubscribe(subscription);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unsubscribeGroupFromComponentByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    ComponentSubscription subscription =
        new ComponentSubscription(GroupSubscriptionSubscriber.from("4"), COMPONENT_ID, "2");
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    String[] result =
        resource.path(UNSUBSCRIBE_RESOURCE_PATH + "/group/4").header(HTTP_SESSIONKEY, sessionKey)
            .accept(MediaType.APPLICATION_JSON).post(String[].class);
    assertThat(result[0], is("OK"));
    verify(mockedSubscriptionService, only()).unsubscribe(subscription);
    verify(mockedSubscriptionService, times(1)).unsubscribe(subscription);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unsubscribeFromTopicByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    NodeSubscription subscription = new NodeSubscription(user.getId(), new NodePK("205", COMPONENT_ID));
    getTestResources().getMockableSubscriptionService().setImplementation(mockedSubscriptionService);


    String[] result =
        resource.path(UNSUBSCRIBE_RESOURCE_PATH + "/topic/205").header(HTTP_SESSIONKEY, sessionKey)
            .accept(MediaType.APPLICATION_JSON).post(String[].class);
    assertThat(result[0], is("OK"));
    verify(mockedSubscriptionService, only()).unsubscribe(subscription);
    verify(mockedSubscriptionService, times(1)).unsubscribe(subscription);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unsubscribeOtherUserFromTopicByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    NodeSubscription subscription = new NodeSubscription(UserSubscriptionSubscriber.from("26"),
        new NodePK("205", COMPONENT_ID), "2");
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    String[] result = resource.path(UNSUBSCRIBE_RESOURCE_PATH + "/topic/205/user/26")
        .header(HTTP_SESSIONKEY, sessionKey).accept(MediaType.APPLICATION_JSON)
        .post(String[].class);
    assertThat(result[0], is("OK"));
    verify(mockedSubscriptionService, only()).unsubscribe(subscription);
    verify(mockedSubscriptionService, times(1)).unsubscribe(subscription);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void unsubscribeGroupFromTopicByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = getTestResources().aUserNamed("Bart", "Simpson");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    NodeSubscription subscription = new NodeSubscription(GroupSubscriptionSubscriber.from("14"),
        new NodePK("205", COMPONENT_ID));
    getTestResources().getMockableSubscriptionService()
        .setImplementation(mockedSubscriptionService);
    String[] result = resource.path(UNSUBSCRIBE_RESOURCE_PATH + "/topic/205/group/14")
        .header(HTTP_SESSIONKEY, sessionKey).accept(MediaType.APPLICATION_JSON)
        .post(String[].class);
    assertThat(result[0], is("OK"));
    verify(mockedSubscriptionService, only()).unsubscribe(subscription);
    verify(mockedSubscriptionService, times(1)).unsubscribe(subscription);
  }
}
