/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
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

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import com.silverpeas.subscribe.service.Subscription;
import com.silverpeas.subscribe.MockableSubscriptionService;
import org.junit.Before;
import java.util.Collection;
import com.google.common.collect.Lists;
import com.silverpeas.subscribe.SubscriptionService;
import com.silverpeas.personalization.service.PersonalizationService;
import com.silverpeas.rest.mock.UserDetailWithProfiles;
import com.stratelia.webactiv.SilverpeasRole;
import com.silverpeas.personalization.service.MockablePersonalizationService;
import javax.inject.Inject;
import com.silverpeas.rest.RESTWebServiceTest;
import com.silverpeas.subscribe.SubscriptionServiceFactory;
import com.silverpeas.subscribe.service.ComponentSubscription;
import com.silverpeas.subscribe.service.NodeSubscription;
import com.silverpeas.util.ForeignPK;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import org.junit.Test;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static com.silverpeas.rest.RESTWebService.*;

public class SubscribePostTest extends RESTWebServiceTest {

  @Inject
  private MockableSubscriptionService subscriptionService;
  @Inject
  private MockablePersonalizationService personalisationService;
  protected static final String COMPONENT_ID = "questionReply12";
  protected static final String KMELIA_ID = "kmelia12";
  protected static final String RESOURCE_PATH = "subscribe/" + COMPONENT_ID;
  protected static final String KMELIA_RESOURCE_PATH = "subscribe/" + COMPONENT_ID;

  public SubscribePostTest() {
    super("com.silverpeas.subscribe.web", "spring-subscription-webservice.xml");
  }

  @Before
  public void setup() {
    assertNotNull(subscriptionService);
    assertThat(subscriptionService,
            is(SubscriptionServiceFactory.getFactory().getSubscribeService()));
  }

  @Test
  public void subscribeForAnNotAuthenticatedUser() {
    WebResource resource = resource();
    try {
      String result = resource.path(RESOURCE_PATH).accept(MediaType.APPLICATION_JSON).post(
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
  public void subscribeToComponentByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.setId("10");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    personalisationService.setPersonalizationService(mock(PersonalizationService.class));
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    ComponentSubscription subscription = new ComponentSubscription("10", COMPONENT_ID);
    subscriptionService.setImplementation(mockedSubscriptionService);
    String result = resource.path(RESOURCE_PATH).header(HTTP_SESSIONKEY, sessionKey).accept(
            MediaType.APPLICATION_JSON).post(String.class);
    assertThat(result, is("OK"));
    verify(mockedSubscriptionService, only()).subscribe(subscription);
    verify(mockedSubscriptionService, times(1)).subscribe(subscription);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void subscribeToTopicByAnAuthenticatedUser() throws Exception {
    WebResource resource = resource();
    UserDetailWithProfiles user = new UserDetailWithProfiles();
    user.setFirstName("Bart");
    user.setLastName("Simpson");
    user.setId("10");
    user.addProfile(COMPONENT_ID, SilverpeasRole.writer);
    user.addProfile(COMPONENT_ID, SilverpeasRole.user);
    String sessionKey = authenticate(user);
    personalisationService.setPersonalizationService(mock(PersonalizationService.class));
    SubscriptionService mockedSubscriptionService = mock(SubscriptionService.class);
    NodeSubscription subscription = new NodeSubscription("10", new NodePK("205", COMPONENT_ID));
    subscriptionService.setImplementation(mockedSubscriptionService);
    String result = resource.path(RESOURCE_PATH + "/topic/205").header(HTTP_SESSIONKEY, sessionKey).accept(
            MediaType.APPLICATION_JSON).post(String.class);
    assertThat(result, is("OK"));
    verify(mockedSubscriptionService, only()).subscribe(subscription);
    verify(mockedSubscriptionService, times(1)).subscribe(subscription);
  }
}
