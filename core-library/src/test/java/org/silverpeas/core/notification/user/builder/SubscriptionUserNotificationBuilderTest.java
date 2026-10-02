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
package org.silverpeas.core.notification.user.builder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.notification.user.DefaultUserNotification;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the rules any builder of notifications to subscribers has to satisfy whatever the
 * way the subscribers are found. These rules are carried by the
 * {@link AbstractUserNotificationBuilder} for the builders qualified by the
 * {@link UserSubscriptionNotificationBehavior} interface.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SubscriptionUserNotificationBuilderTest {

  private static final String SENDER = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final String A_DEACTIVATED_SUBSCRIBER = "7";
  private static final String A_SUBSCRIBED_GROUP = "12";
  private static final String ANOTHER_SUBSCRIBED_GROUP = "14";

  @TestManagedBean
  UserSubscriptionNotificationSendingHandler sendingHandler;

  @BeforeEach
  void setUpUsers(@TestManagedMock UserProvider userProvider) {
    when(userProvider.getUser(anyString())).thenAnswer(invocation -> {
      final String userId = invocation.getArgument(0);
      final User user = mock(User.class);
      when(user.getId()).thenReturn(userId);
      when(user.isActivatedState()).thenReturn(!A_DEACTIVATED_SUBSCRIBER.equals(userId));
      return user;
    });
  }

  @BeforeEach
  @AfterEach
  void clearRequestContext() {
    CacheAccessorProvider.getThreadCacheAccessor().getCache().clear();
  }

  @Test
  void theSubscribersAreTheRecipientsOfTheNotification() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(A_SUBSCRIBER, ANOTHER_SUBSCRIBER)
        .toGroups(A_SUBSCRIBED_GROUP)
        .build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.CREATE));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
    assertThat(groupsIn(metaData.getGroupRecipients()), contains(A_SUBSCRIBED_GROUP));
  }

  @Test
  void theNotificationIsNotSentImmediatelySoThatItCanBeDelayed() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(A_SUBSCRIBER)
        .build();

    assertThat(notification.getNotificationMetaData().isSendImmediately(), is(false));
  }

  @Test
  void theSubscribersWithoutAccessRightAreNotNotified() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(A_SUBSCRIBER, ANOTHER_SUBSCRIBER)
        .toGroups(A_SUBSCRIBED_GROUP, ANOTHER_SUBSCRIBED_GROUP)
        .withoutAccessRightFor(ANOTHER_SUBSCRIBER, ANOTHER_SUBSCRIBED_GROUP)
        .build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(usersIn(metaData.getUserRecipients()), contains(A_SUBSCRIBER));
    assertThat(groupsIn(metaData.getGroupRecipients()), contains(A_SUBSCRIBED_GROUP));
  }

  @Test
  void theDeactivatedSubscribersAreNotNotified() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(A_SUBSCRIBER, A_DEACTIVATED_SUBSCRIBER)
        .build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER));
  }

  @Test
  void theSenderIsExcludedFromTheRecipients() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(SENDER, A_SUBSCRIBER)
        .build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(usersIn(metaData.getUserRecipientsToExclude()), contains(SENDER));
  }

  @Test
  void nothingIsNotifiedWhenThereIsNoSubscriber() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE).build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  @Test
  void nothingIsNotifiedWhenNoneOfTheSubscribersHasAccessRight() {
    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(A_SUBSCRIBER)
        .toGroups(A_SUBSCRIBED_GROUP)
        .withoutAccessRightFor(A_SUBSCRIBER, A_SUBSCRIBED_GROUP)
        .build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  @Test
  void anUpdateIsNotNotifiedWhenItsAuthorAskedToSkipTheNotification() {
    sendingHandler.skipNotificationSend();

    final UserNotification notification = aNotificationAbout(NotifAction.UPDATE)
        .toUsers(A_SUBSCRIBER)
        .build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  @Test
  void aClassificationIsNotNotifiedWhenItsAuthorAskedToSkipTheNotification() {
    sendingHandler.skipNotificationSend();

    final UserNotification notification = aNotificationAbout(NotifAction.CLASSIFIED)
        .toUsers(A_SUBSCRIBER)
        .build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  @Test
  void aCreationIsAlwaysNotifiedEvenIfTheNotificationWasAskedToBeSkipped() {
    sendingHandler.skipNotificationSend();

    final UserNotification notification = aNotificationAbout(NotifAction.CREATE)
        .toUsers(A_SUBSCRIBER)
        .build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER));
  }

  @Test
  void anUpdateIsNotifiedByDefault() {
    final UserNotification notification = aNotificationAbout(NotifAction.UPDATE)
        .toUsers(A_SUBSCRIBER)
        .build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(usersIn(metaData.getUserRecipients()), contains(A_SUBSCRIBER));
    assertThat(groupsIn(metaData.getGroupRecipients()), is(empty()));
  }

  private static SubscriptionNotificationBuilder aNotificationAbout(final NotifAction action) {
    return new SubscriptionNotificationBuilder(action);
  }

  private static List<String> usersIn(final Collection<UserRecipient> recipients) {
    return recipients.stream().map(UserRecipient::getUserId).toList();
  }

  private static List<String> groupsIn(final Collection<GroupRecipient> recipients) {
    return recipients.stream().map(GroupRecipient::getGroupId).toList();
  }

  /**
   * A builder of notifications to subscribers as any Silverpeas application could define it: it
   * only says who are the subscribers and who among them can access the resource.
   */
  private static class SubscriptionNotificationBuilder extends AbstractUserNotificationBuilder
      implements UserSubscriptionNotificationBehavior {

    private final NotifAction action;
    private Collection<String> userIds = List.of();
    private Collection<String> groupIds = List.of();
    private Set<String> unauthorized = Set.of();

    SubscriptionNotificationBuilder(final NotifAction action) {
      super("A title", "A content");
      this.action = action;
    }

    SubscriptionNotificationBuilder toUsers(final String... userIds) {
      this.userIds = List.of(userIds);
      return this;
    }

    SubscriptionNotificationBuilder toGroups(final String... groupIds) {
      this.groupIds = List.of(groupIds);
      return this;
    }

    SubscriptionNotificationBuilder withoutAccessRightFor(final String... userOrGroupIds) {
      this.unauthorized = Set.of(userOrGroupIds);
      return this;
    }

    @Override
    protected UserNotification createNotification() {
      return new DefaultUserNotification(getTitle(), getContent());
    }

    @Override
    protected NotifAction getAction() {
      return action;
    }

    @Override
    protected String getComponentInstanceId() {
      return "kmelia42";
    }

    @Override
    protected String getSender() {
      return SENDER;
    }

    @Override
    protected boolean isUserCanBeNotified(final String userId) {
      return !unauthorized.contains(userId);
    }

    @Override
    protected boolean isGroupCanBeNotified(final String groupId) {
      return !unauthorized.contains(groupId);
    }

    @Override
    protected Collection<String> getUserIdsToNotify() {
      return userIds;
    }

    @Override
    protected Collection<String> getGroupIdsToNotify() {
      return groupIds;
    }

    @Override
    protected void performBuild() {
      // nothing more to build: the tests are about the recipients
    }
  }
}
