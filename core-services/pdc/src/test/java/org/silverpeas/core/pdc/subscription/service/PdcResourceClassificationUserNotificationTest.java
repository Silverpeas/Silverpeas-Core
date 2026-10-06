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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.io.TempDir;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.contribution.contentcontainer.content.ManagedContribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.Thumbnail;
import org.silverpeas.core.notification.user.NullUserNotification;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationSendingHandler;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.pdc.pdc.model.AxisValueCriterion;
import org.silverpeas.core.pdc.subscription.model.PdcSubscriptionPositionCriteria;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LocalizationBundleStub;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the notification sent to the subscribers to position criteria on the PdC when a
 * contribution is classified, out of any event on the contribution itself, on a position that
 * satisfies these criteria. It is a notification to subscribers as any other one: its message is
 * rendered from a template and its sending can be delayed.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class PdcResourceClassificationUserNotificationTest {

  private static final String FR = LocalizationBundleStub.LANGUAGE_FR;
  private static final String EN = LocalizationBundleStub.LANGUAGE_EN;
  private static final String DE = LocalizationBundleStub.LANGUAGE_DE;
  private static final String COMPONENT_NAME = "kmelia";
  private static final String COMPONENT_ID = "kmelia42";
  private static final String CONTRIBUTION_ID = "23";
  private static final String AUTHOR = "1";
  private static final String A_SUBSCRIBER = "3";
  private static final String ANOTHER_SUBSCRIBER = "5";
  private static final byte[] AN_IMAGE = {(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10};

  @RegisterExtension
  static LocalizationBundleStub pdcSubscriptionBundle = new LocalizationBundleStub(
      "org.silverpeas.pdcSubscription.multilang.pdcsubscription",
      LocalizationBundleStub.LANGUAGE_ALL);

  @TestManagedMock
  ComponentAccessControl accessControl;
  @TestManagedMock
  Administration administration;
  @TestManagedBean
  UserSubscriptionNotificationSendingHandler sendingHandler;

  private final PdcSubscriptionPositionCriteria criteria =
      new PdcSubscriptionPositionCriteria("1", "Legal & <tax> watch",
          List.of(new AxisValueCriterion(3, "/12/")));

  @BeforeEach
  void setUpBundle() {
    pdcSubscriptionBundle.put(FR, "standartMessage", "Classement_FR");
    pdcSubscriptionBundle.put(EN, "standartMessage", "Classification_EN");
    pdcSubscriptionBundle.put(DE, "standartMessage", "Klassifizierung_DE");
  }

  @BeforeEach
  void setUpMocks(@TestManagedMock OrganizationController organizationController,
      @TestManagedMock SilverpeasComponentInstanceProvider componentInstanceProvider,
      @TestManagedMock UserProvider userProvider) {
    final ComponentInstLight componentInstance = new ComponentInstLight();
    componentInstance.setLocalId(42);
    componentInstance.setName(COMPONENT_NAME);
    final Optional<SilverpeasComponentInstance> instance = Optional.of(componentInstance);
    when(organizationController.getComponentInstLight(COMPONENT_ID)).thenReturn(componentInstance);
    when(organizationController.getComponentInstance(COMPONENT_ID)).thenReturn(instance);
    when(componentInstanceProvider.getById(COMPONENT_ID)).thenReturn(instance);

    when(userProvider.getUser(anyString())).thenAnswer(invocation -> aUser(invocation.getArgument(0)));

    when(accessControl.isUserAuthorized(anyString(), eq(COMPONENT_ID))).thenReturn(true);
  }

  @Test
  void theSubscribersAreNotifiedAboutTheClassificationOfTheContribution() {
    final UserNotification notification = aNotificationTo(A_SUBSCRIBER, ANOTHER_SUBSCRIBER).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getAction(), is(NotifAction.CLASSIFIED));
    assertThat(metaData.getComponentId(), is(COMPONENT_ID));
    assertThat(usersIn(metaData.getUserRecipients()),
        containsInAnyOrder(A_SUBSCRIBER, ANOTHER_SUBSCRIBER));
  }

  @Test
  void theNotificationCanBeDelayedAsAnyOtherNotificationToSubscribers() {
    final UserNotification notification = aNotificationTo(A_SUBSCRIBER).build();

    assertThat(notification.getNotificationMetaData().isSendImmediately(), is(false));
  }

  /**
   * Such data are required for the notification to be reported in the synthesis of the delayed
   * notifications.
   */
  @Test
  void theNotificationCarriesTheDataAboutTheClassifiedContribution() {
    final UserNotification notification = aNotificationTo(A_SUBSCRIBER).build();

    for (final String language : List.of(FR, EN, DE)) {
      final NotificationResourceData data =
          notification.getNotificationMetaData().getNotificationResourceData(language);
      data.setCurrentLanguage(language);
      assertThat(data.getResourceId(), is(CONTRIBUTION_ID));
      assertThat(data.getResourceType(), is("Publication"));
      assertThat(data.getComponentInstanceId(), is(COMPONENT_ID));
      assertThat(data.getResourceName(), is("A contribution in " + language));
      assertThat(data.getResourceDescription(), is("Its description in " + language));
      assertThat(data.getResourceUrl(), containsString("Id=" + CONTRIBUTION_ID));
    }
  }

  @Test
  void theAuthorOfTheClassificationIsTheSenderAndIsExcludedFromTheRecipients() {
    final UserNotification notification = aNotificationTo(AUTHOR, A_SUBSCRIBER).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getSender(), is(AUTHOR));
    assertThat(usersIn(metaData.getUserRecipientsToExclude()), contains(AUTHOR));
  }

  /**
   * It is the case of a classification done by a batch process, like an import of contributions.
   */
  @Test
  void aClassificationWithoutAuthorIsNotifiedOnBehalfOfNobody() {
    final UserNotification notification =
        new PdcResourceClassificationUserNotification(criteria, List.of(A_SUBSCRIBER),
            aContribution(), null).build();

    final NotificationMetaData metaData = notification.getNotificationMetaData();
    assertThat(metaData.getSender(), is(""));
    assertThat(usersIn(metaData.getUserRecipients()), contains(A_SUBSCRIBER));
  }

  @Test
  void aSubscriberWithoutAccessToTheApplicationIsNotNotified() {
    when(accessControl.isUserAuthorized(ANOTHER_SUBSCRIBER, COMPONENT_ID)).thenReturn(false);

    final UserNotification notification = aNotificationTo(A_SUBSCRIBER, ANOTHER_SUBSCRIBER).build();

    assertThat(usersIn(notification.getNotificationMetaData().getUserRecipients()),
        contains(A_SUBSCRIBER));
  }

  @Test
  void nothingIsNotifiedWithoutAnySubscriber() {
    final UserNotification notification = aNotificationTo().build();

    assertThat(notification, instanceOf(NullUserNotification.class));
  }

  /**
   * Only the given subscribers are notified: the other subscribers on the PdC concerned by the
   * contribution are notified by the notification about the position criteria they subscribed to.
   */
  @Test
  void theRecipientsAreStrictlyTheGivenSubscribers() {
    assertThat(aNotificationTo(A_SUBSCRIBER).getSubscribedContribution().isPresent(), is(false));
  }

  @Test
  void theTitleOfTheNotificationIsLocalized() {
    final NotificationMetaData metaData =
        aNotificationTo(A_SUBSCRIBER).build().getNotificationMetaData();

    assertThat(metaData.getTitle(FR), is("Classement_FR"));
    assertThat(metaData.getTitle(EN), is("Classification_EN"));
    assertThat(metaData.getTitle(DE), is("Klassifizierung_DE"));
  }

  @Test
  void theMessageNamesBothTheClassifiedContributionAndTheCenterOfInterestOfTheSubscriber() {
    final NotificationMetaData metaData =
        aNotificationTo(A_SUBSCRIBER).build().getNotificationMetaData();

    for (final String language : List.of(FR, EN, DE)) {
      final String message = metaData.getContent(language);
      assertThat(message, containsString("<b>A contribution in " + language + "</b>"));
      assertThat(message, containsString("<b>Legal &amp; &lt;tax&gt; watch</b>"));
    }
    assertThat(metaData.getContent(FR), not(is(metaData.getContent(EN))));
    assertThat(metaData.getContent(EN), not(is(metaData.getContent(DE))));
  }

  /**
   * As for the other notifications to subscribers, the thumbnail of the contribution is inlined
   * into the message and carried for the synthesis of the delayed notifications.
   */
  @Test
  void theThumbnailOfTheClassifiedContributionIsCarriedByTheNotification(@TempDir Path directory)
      throws IOException {
    final ManagedContribution contribution = aContribution();
    final Thumbnail thumbnail = aThumbnailIn(directory);
    when(contribution.getThumbnail()).thenReturn(thumbnail);

    final NotificationMetaData metaData =
        new PdcResourceClassificationUserNotification(criteria, List.of(A_SUBSCRIBER),
            contribution, aUser(AUTHOR)).build().getNotificationMetaData();

    final String image = "data:image/png;base64," + Base64.getEncoder().encodeToString(AN_IMAGE);
    for (final String language : List.of(FR, EN, DE)) {
      assertThat(metaData.getContent(language), containsString("<img src=\"" + image + "\""));
      assertThat(metaData.getNotificationResourceData(language).getResourceThumbnail(),
          is(image));
    }
  }

  @Test
  void theMessageAboutAContributionWithoutThumbnailHasNoImage() {
    final NotificationMetaData metaData =
        aNotificationTo(A_SUBSCRIBER).build().getNotificationMetaData();

    for (final String language : List.of(FR, EN, DE)) {
      assertThat(metaData.getContent(language), not(containsString("<img")));
      assertThat(metaData.getNotificationResourceData(language).getResourceThumbnail(),
          is(nullValue()));
    }
  }

  private static Thumbnail aThumbnailIn(final Path directory) throws IOException {
    final Path image = Files.write(directory.resolve("thumbnail.png"), AN_IMAGE);
    final Thumbnail thumbnail = mock(Thumbnail.class);
    when(thumbnail.getMimeType()).thenReturn("image/png");
    when(thumbnail.getPath()).thenReturn(Optional.of(image));
    return thumbnail;
  }

  private PdcResourceClassificationUserNotification aNotificationTo(final String... userIds) {
    return new PdcResourceClassificationUserNotification(criteria, List.of(userIds),
        aContribution(), aUser(AUTHOR));
  }

  private static ManagedContribution aContribution() {
    final ManagedContribution contribution = mock(ManagedContribution.class);
    when(contribution.getId()).thenReturn(CONTRIBUTION_ID);
    when(contribution.getContributionType()).thenReturn("Publication");
    when(contribution.getComponentInstanceId()).thenReturn(COMPONENT_ID);
    when(contribution.getIdentifier()).thenReturn(
        ContributionIdentifier.from(COMPONENT_ID, CONTRIBUTION_ID, "Publication"));
    when(contribution.getName(anyString())).thenAnswer(
        invocation -> "A contribution in " + invocation.getArgument(0));
    when(contribution.getDescription(anyString())).thenAnswer(
        invocation -> "Its description in " + invocation.getArgument(0));
    return contribution;
  }

  private static User aUser(final String userId) {
    final UserDetail user = mock(UserDetail.class);
    when(user.getId()).thenReturn(userId);
    when(user.isActivatedState()).thenReturn(true);
    when(user.getDisplayedName()).thenReturn("User " + userId);
    return user;
  }

  private static List<String> usersIn(final Collection<UserRecipient> recipients) {
    return recipients.stream().map(UserRecipient::getUserId).toList();
  }
}
