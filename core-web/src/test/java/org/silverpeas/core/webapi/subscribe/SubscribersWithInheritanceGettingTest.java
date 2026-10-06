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
package org.silverpeas.core.webapi.subscribe;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.subscription.ContributionSubscribersProvider;
import org.silverpeas.core.subscription.ResourceSubscriptionService;
import org.silverpeas.core.subscription.SubscriberDirective;
import org.silverpeas.core.subscription.SubscriptionFactory;
import org.silverpeas.core.subscription.SubscriptionSubscriber;
import org.silverpeas.core.subscription.service.GroupSubscriptionSubscriber;
import org.silverpeas.core.subscription.service.ResourceSubscriptionProvider;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.COMPONENT;
import static org.silverpeas.core.subscription.constant.CommonSubscriptionResourceConstants.NODE;

/**
 * Unit tests on the getting of the subscribers, with inheritance, of a resource. It is used to
 * ask to a user, before he validates the modification of a contribution, whether the subscribers
 * have to be notified about it. So, when the modified contribution is specified, the subscribers
 * concerned by that contribution for other reasons than a subscription to the resource (its
 * classification on the PdC for example) have to be also taken into account. Whatever the reason
 * for which they are concerned, only the subscribers able to access the application count: the
 * others won't be notified.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SubscribersWithInheritanceGettingTest {

  private static final String COMPONENT_NAME = "kmelia";
  private static final String COMPONENT_ID = "kmelia42";
  private static final String FOLDER_ID = "12";
  private static final String CONTRIBUTION_ID = "23";
  private static final String CONTRIBUTION_TYPE = "Publication";
  private static final ContributionIdentifier CONTRIBUTION =
      ContributionIdentifier.from(COMPONENT_ID, CONTRIBUTION_ID, CONTRIBUTION_TYPE);
  private static final SubscriptionSubscriber A_FOLDER_SUBSCRIBER =
      UserSubscriptionSubscriber.from("3");
  private static final SubscriptionSubscriber A_PDC_SUBSCRIBER =
      UserSubscriptionSubscriber.from("5");
  private static final SubscriptionSubscriber A_SUBSCRIBED_GROUP_ON_PDC =
      GroupSubscriptionSubscriber.from("8");
  private static final String A_USER_OF_THE_SUBSCRIBED_GROUP = "9";
  private static final String NO_LOCATION = null;
  private static final boolean EXISTENCE_ONLY = true;
  private static final boolean ALL_SUBSCRIBERS = false;

  @TestManagedBean
  SubscriptionFactory subscriptionFactory;
  @TestManagedMock
  ResourceSubscriptionService subscriptionService;
  @TestManagedMock
  ContributionSubscribersProvider contributionSubscribersProvider;
  @TestManagedMock
  OrganizationController organizationController;
  @TestManagedMock
  ComponentAccessControl componentAccessControl;
  @TestedBean
  SubscriptionResource resource;

  @BeforeEach
  void setUpTheSubscriptionsOfTheApplication() throws Exception {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(42);
    componentInstance.setName(COMPONENT_NAME);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(
        Optional.of(componentInstance));
    subscriptionServices().put(COMPONENT_NAME, subscriptionService);
    FieldUtils.writeField(resource, "componentId", COMPONENT_ID, true);

    theSubscribersOfTheFolderAre();
    theSubscribersConcernedByTheContributionAre();
  }

  @BeforeEach
  void anyUserCanAccessTheApplication() {
    when(componentAccessControl.isUserAuthorized(anyString(), eq(COMPONENT_ID))).thenReturn(true);
    final User aUserOfTheGroup = mock(User.class);
    when(aUserOfTheGroup.getId()).thenReturn(A_USER_OF_THE_SUBSCRIBED_GROUP);
    doReturn(new User[]{aUserOfTheGroup}).when(organizationController)
        .getAllUsersOfGroup(A_SUBSCRIBED_GROUP_ON_PDC.getId());
  }

  @AfterEach
  void clearTheSubscriptionsOfTheApplication() throws Exception {
    subscriptionServices().clear();
  }

  @Test
  void nobodyIsSubscribed() {
    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE), is(false));
  }

  @Test
  void theSubscribersOfTheResourceExistWithoutAnySpecifiedContribution() {
    theSubscribersOfTheFolderAre(A_FOLDER_SUBSCRIBER);

    assertThat(existSubscribersOfTheFolder(null, null), is(true));
    verify(contributionSubscribersProvider, never()).getSubscribersOf(any());
  }

  @Test
  void theSubscribersConcernedByTheContributionAreIgnoredWhenItIsNotSpecified() {
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER);

    assertThat(existSubscribersOfTheFolder(null, null), is(false));
    verify(contributionSubscribersProvider, never()).getSubscribersOf(any());
  }

  @Test
  void theSubscribersConcernedByTheSpecifiedContributionExist() {
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER);

    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE), is(true));
  }

  @Test
  void theSubscribersConcernedByTheContributionOfAnApplicationExist() {
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER);

    final Response response =
        resource.getComponentSubscribersWithInheritance(COMPONENT.getName(), CONTRIBUTION_ID,
            CONTRIBUTION_TYPE, EXISTENCE_ONLY);

    assertThat(response.getEntity(), is("true"));
  }

  @Test
  void aPartiallyIdentifiedContributionIsIgnored() {
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER);

    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, null), is(false));
    assertThat(existSubscribersOfTheFolder(null, CONTRIBUTION_TYPE), is(false));
    verify(contributionSubscribersProvider, never()).getSubscribersOf(any());
  }

  @Test
  void theSubscribersOfTheResourceAndTheOnesConcernedByTheContributionAreAllGot() {
    theSubscribersOfTheFolderAre(A_FOLDER_SUBSCRIBER);
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER, A_SUBSCRIBED_GROUP_ON_PDC);

    assertThat(subscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE),
        containsInAnyOrder("3", "5", "8"));
  }

  @Test
  void aSubscriberOfBothTheResourceAndThePdcIsGotOnce() {
    theSubscribersOfTheFolderAre(A_FOLDER_SUBSCRIBER, A_PDC_SUBSCRIBER);
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER);

    assertThat(subscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE),
        containsInAnyOrder("3", "5"));
  }

  @Test
  void aSubscriberOfTheResourceWithoutAccessToTheApplicationDoesNotCount() {
    theSubscribersOfTheFolderAre(A_FOLDER_SUBSCRIBER);
    cannotAccessTheApplication(A_FOLDER_SUBSCRIBER.getId());

    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE), is(false));
  }

  @Test
  void aSubscriberConcernedByTheContributionWithoutAccessToTheApplicationDoesNotCount() {
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER);
    cannotAccessTheApplication(A_PDC_SUBSCRIBER.getId());

    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE), is(false));
  }

  @Test
  void aSubscribedGroupWithAUserAccessingTheApplicationCounts() {
    theSubscribersConcernedByTheContributionAre(A_SUBSCRIBED_GROUP_ON_PDC);

    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE), is(true));
  }

  @Test
  void aSubscribedGroupWithoutAnyUserAccessingTheApplicationDoesNotCount() {
    theSubscribersConcernedByTheContributionAre(A_SUBSCRIBED_GROUP_ON_PDC);
    cannotAccessTheApplication(A_USER_OF_THE_SUBSCRIBED_GROUP);

    assertThat(existSubscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE), is(false));
  }

  @Test
  void onlyTheSubscribersAccessingTheApplicationAreGot() {
    theSubscribersOfTheFolderAre(A_FOLDER_SUBSCRIBER);
    theSubscribersConcernedByTheContributionAre(A_PDC_SUBSCRIBER, A_SUBSCRIBED_GROUP_ON_PDC);
    cannotAccessTheApplication(A_PDC_SUBSCRIBER.getId());
    cannotAccessTheApplication(A_USER_OF_THE_SUBSCRIBED_GROUP);

    assertThat(subscribersOfTheFolder(CONTRIBUTION_ID, CONTRIBUTION_TYPE),
        containsInAnyOrder(A_FOLDER_SUBSCRIBER.getId()));
  }

  @Test
  void anUnknownTypeOfSubscriptionIsNotFound() {
    final WebApplicationException error = assertThrows(WebApplicationException.class,
        () -> resource.getSubscribersWithInheritance("foo", FOLDER_ID, NO_LOCATION,
            CONTRIBUTION_ID, CONTRIBUTION_TYPE, EXISTENCE_ONLY));

    assertThat(error.getResponse().getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
  }

  private boolean existSubscribersOfTheFolder(final String contributionId,
      final String contributionType) {
    final Response response =
        resource.getSubscribersWithInheritance(NODE.getName(), FOLDER_ID, NO_LOCATION,
            contributionId, contributionType, EXISTENCE_ONLY);
    return Boolean.parseBoolean((String) response.getEntity());
  }

  @SuppressWarnings("unchecked")
  private List<String> subscribersOfTheFolder(final String contributionId,
      final String contributionType) {
    final Response response =
        resource.getSubscribersWithInheritance(NODE.getName(), FOLDER_ID, NO_LOCATION,
            contributionId, contributionType, ALL_SUBSCRIBERS);
    return ((Collection<SubscriberEntity>) response.getEntity()).stream()
        .map(SubscriberEntity::getId)
        .toList();
  }

  private void cannotAccessTheApplication(final String userId) {
    when(componentAccessControl.isUserAuthorized(userId, COMPONENT_ID)).thenReturn(false);
  }

  private void theSubscribersOfTheFolderAre(final SubscriptionSubscriber... subscribers) {
    when(subscriptionService.getSubscribersOfComponentAndTypedResource(eq(COMPONENT_ID), any(),
        any(), any(SubscriberDirective[].class))).thenAnswer(
        invocation -> new SubscriptionSubscriberList(List.of(subscribers)));
  }

  private void theSubscribersConcernedByTheContributionAre(
      final SubscriptionSubscriber... subscribers) {
    when(contributionSubscribersProvider.getSubscribersOf(CONTRIBUTION)).thenAnswer(
        invocation -> new SubscriptionSubscriberList(List.of(subscribers)));
  }

  @SuppressWarnings("unchecked")
  private static Map<String, ResourceSubscriptionService> subscriptionServices()
      throws IllegalAccessException {
    return (Map<String, ResourceSubscriptionService>) FieldUtils.readDeclaredStaticField(
        ResourceSubscriptionProvider.class, "componentImplementations", true);
  }
}
