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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagementEngine;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.ManagedContribution;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.contribution.contentcontainer.content.SilverpeasContentManager;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.pdc.classification.Value;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.subscription.SubscriptionService;
import org.silverpeas.core.subscription.service.UserSubscriptionSubscriber;
import org.silverpeas.core.subscription.util.SubscriptionSubscriberList;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the notification of the subscribers to positions on the PdC when a contribution is
 * classified on the PdC out of any event on the contribution itself: who are notified and with
 * which notification.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcSubscribersNotificationTest {

  private static final String COMPONENT_ID = "kmelia42";
  private static final int SILVER_CONTENT_ID = 1024;
  private static final int AN_AXIS = 3;
  private static final int ANOTHER_AXIS = 8;
  private static final String AUTHOR_OF_THE_CLASSIFICATION = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";

  private final List<PdcSubscriptionPositionCriteria> allCriteria = new ArrayList<>();
  private final List<Value> classification =
      List.of(new Value(AN_AXIS, "/12/45/"), new Value(ANOTHER_AXIS, "/33/"));

  private DefaultPdcSubscriptionService service;
  private ContentManagementEngine contentManagementEngine;
  private OrganizationController organizationController;
  private SubscriptionService subscriptionService;
  private SilverpeasContentManager contentManager;
  private UserProvider userProvider;

  @BeforeEach
  void setUp() throws Exception {
    this.contentManagementEngine = mock(ContentManagementEngine.class);
    this.subscriptionService = mock(SubscriptionService.class);
    this.organizationController = mock(OrganizationController.class);
    this.contentManager = mock(SilverpeasContentManager.class);
    this.userProvider = mock(UserProvider.class);

    // the position criteria are got directly from the data source: the service is spied to
    // provide those of the tests
    service = spy(new DefaultPdcSubscriptionService());
    FieldUtils.writeField(service, "organizationController", organizationController, true);
    FieldUtils.writeField(service, "contentManagementEngine", contentManagementEngine, true);
    FieldUtils.writeField(service, "subscriptionService", subscriptionService, true);
    FieldUtils.writeField(service, "userProvider", userProvider, true);
    doReturn(allCriteria).when(service).getAllPositionCriteria();

    // by default the contribution is classified by a user
    final User author = mock(User.class);
    when(author.getId()).thenReturn(AUTHOR_OF_THE_CLASSIFICATION);
    when(userProvider.getCurrentRequester()).thenReturn(author);

    // by default the classified contribution is visible and accessible to anyone
    theContributionIsVisible(true);
    final ContentPeas contentPeas = mock(ContentPeas.class);
    when(contentPeas.getContentManager()).thenReturn(contentManager);
    when(contentManagementEngine.getContentPeas(COMPONENT_ID)).thenReturn(contentPeas);
    when(contentManagementEngine.getResourceReferencesByContentIds(anyList())).thenReturn(
        List.of(new ResourceReference("12", COMPONENT_ID)));
    final ManagedContribution contribution = aContribution();
    when(contentManager.getSilverContentByReference(anyList(), anyString())).thenReturn(
        List.of(contribution));
    when(organizationController.isComponentAvailableToUser(anyString(), anyString())).thenReturn(
        true);
  }

  @Test
  void theSubscribersOfAMatchingPositionAreNotifiedAboutTheClassification() {
    final PdcSubscriptionPositionCriteria criteria = aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/"));
    subscribe(criteria, A_SUBSCRIBER, ANOTHER_SUBSCRIBER);

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications, hasSize(1));
    final PdcResourceClassificationUserNotification notification = notifications.get(0);
    assertThat(notification.getPdcSubscriptionPositionCriteria(), is(criteria));
    assertThat(notification.getAction(), is(NotifAction.CLASSIFIED));
    assertThat(notification.getComponentInstanceId(), is(COMPONENT_ID));
    assertThat(notification.getUserIdsToNotify(),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
  }

  @Test
  void nobodyIsNotifiedWhenNoPositionMatchesTheClassification() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/99/")), A_SUBSCRIBER);

    assertThat(classify(), is(empty()));
  }

  @Test
  void nobodyIsNotifiedWhenTheMatchingPositionHasNoSubscriber() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")));

    assertThat(classify(), is(empty()));
  }

  @Test
  void nobodyIsNotifiedWhenTheContributionIsNotVisible() throws Exception {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")), A_SUBSCRIBER);
    theContributionIsVisible(false);

    assertThat(classify(), is(empty()));
  }

  @Test
  void aSubscriberWithoutAccessToTheApplicationIsNotNotified() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")), A_SUBSCRIBER,
        ANOTHER_SUBSCRIBER);
    when(organizationController.isComponentAvailableToUser(COMPONENT_ID, ANOTHER_SUBSCRIBER))
        .thenReturn(false);

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications, hasSize(1));
    assertThat(notifications.get(0).getUserIdsToNotify(), contains(A_SUBSCRIBER));
  }

  @Test
  void aSubscriberWithoutAccessToTheContributionIsNotNotified() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")), A_SUBSCRIBER,
        ANOTHER_SUBSCRIBER);
    when(contentManager.getSilverContentByReference(anyList(), org.mockito.ArgumentMatchers.eq(
        ANOTHER_SUBSCRIBER))).thenReturn(List.of());

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications, hasSize(1));
    assertThat(notifications.get(0).getUserIdsToNotify(), contains(A_SUBSCRIBER));
  }

  @Test
  void theSenderIsTheAuthorOfTheClassification() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")), A_SUBSCRIBER);

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications.get(0).getSender(), is(AUTHOR_OF_THE_CLASSIFICATION));
  }

  /**
   * It is the case of a classification done by a batch process, like an import of contributions.
   */
  @Test
  void theSubscribersAreNotifiedOnBehalfOfNobodyWhenTheClassificationIsNotDoneByAUser() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")), A_SUBSCRIBER);
    when(userProvider.getCurrentRequester()).thenReturn(null);

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications, hasSize(1));
    assertThat(notifications.get(0).getSender(), is(""));
    assertThat(notifications.get(0).getUserIdsToNotify(), contains(A_SUBSCRIBER));
  }

  /**
   * A notification is sent for each matching position, so that it tells the subscriber which of
   * his centers of interest is concerned. But a subscriber is notified only once about a
   * classification, whatever the number of his subscriptions matching it.
   */
  @Test
  void aSubscriberOfSeveralMatchingPositionsIsNotifiedOnlyOnce() {
    final PdcSubscriptionPositionCriteria aCriteria =
        aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/"));
    final PdcSubscriptionPositionCriteria anotherCriteria =
        aPosition("2", new AxisValueCriterion(ANOTHER_AXIS, "/33/"));
    subscribe(aCriteria, A_SUBSCRIBER);
    subscribe(anotherCriteria, A_SUBSCRIBER, ANOTHER_SUBSCRIBER);

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications, hasSize(2));
    assertThat(notifications.get(0).getPdcSubscriptionPositionCriteria(), is(aCriteria));
    assertThat(notifications.get(0).getUserIdsToNotify(), contains(A_SUBSCRIBER));
    assertThat(notifications.get(1).getPdcSubscriptionPositionCriteria(), is(anotherCriteria));
    assertThat(notifications.get(1).getUserIdsToNotify(), contains(ANOTHER_SUBSCRIBER));
  }

  @Test
  void noNotificationIsSentAboutAPositionWhoseSubscribersAreAllAlreadyNotified() {
    subscribe(aPosition("1", new AxisValueCriterion(AN_AXIS, "/12/")), A_SUBSCRIBER,
        ANOTHER_SUBSCRIBER);
    subscribe(aPosition("2", new AxisValueCriterion(ANOTHER_AXIS, "/33/")), A_SUBSCRIBER);

    final List<PdcResourceClassificationUserNotification> notifications = classify();

    assertThat(notifications, hasSize(1));
    assertThat(notifications.get(0).getUserIdsToNotify(),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
  }

  /**
   * Classifies the contribution on the PdC with alerting of the subscribers.
   * @return the notifications that have been asked to be sent to the subscribers.
   */
  private List<PdcResourceClassificationUserNotification> classify() {
    final ArgumentCaptor<UserNotificationBuilder> builders =
        ArgumentCaptor.forClass(UserNotificationBuilder.class);
    try (MockedStatic<UserNotificationHelper> helper = mockStatic(UserNotificationHelper.class)) {
      service.notifyClassification(classification, COMPONENT_ID, SILVER_CONTENT_ID);
      helper.verify(() -> UserNotificationHelper.buildAndSend(builders.capture()),
          org.mockito.Mockito.atLeast(0));
    }
    return builders.getAllValues()
        .stream()
        .map(PdcResourceClassificationUserNotification.class::cast)
        .toList();
  }

  private PdcSubscriptionPositionCriteria aPosition(final String id,
      final AxisValueCriterion... criteria) {
    final PdcSubscriptionPositionCriteria position =
        new PdcSubscriptionPositionCriteria(id, "position " + id, List.of(criteria));
    allCriteria.add(position);
    return position;
  }

  private void subscribe(final PdcSubscriptionPositionCriteria criteria, final String... userIds) {
    when(subscriptionService.getSubscribers(criteria)).thenReturn(new SubscriptionSubscriberList(
        Stream.of(userIds).map(UserSubscriptionSubscriber::from).toList()));
  }

  private void theContributionIsVisible(final boolean visible) throws Exception {
    when(contentManagementEngine.getSilverContentVisibility(SILVER_CONTENT_ID)).thenReturn(
        new SilverContentVisibility(new Date(), new Date(), visible));
  }

  private static ManagedContribution aContribution() {
    final ManagedContribution contribution = mock(ManagedContribution.class);
    when(contribution.getComponentInstanceId()).thenReturn(COMPONENT_ID);
    return contribution;
  }
}
