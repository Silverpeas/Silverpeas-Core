/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.reminder;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.component.service.SilverpeasComponentInstanceProvider;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.notification.CalendarEventUserNotificationReminder;
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.user.UserNotification;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.security.authorization.ComponentAccessControl;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.test.extention.TestManagedMocks;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMap;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProvider;
import org.silverpeas.core.web.mvc.route.ComponentInstanceRoutingMapProviderByInstance;

import javax.ws.rs.core.UriBuilder;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
@ExtendWith(MockitoExtension.class)
@TestManagedMocks({DefaultContributionReminderUserNotification.class,
    CalendarEventUserNotificationReminder.class})
class DefaultContributionReminderUserNotificationTest {
  private static final ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

  private static final ReminderProcessName PROCESS_NAME = () -> "TestReminderProcess";
  private static final String INSTANCE_ID = "componentNameTest26";
  private static final String LOCAL_ID = "localId";
  private static final String CONTRIBUTION_TYPE = "contributionType";
  private static final ContributionIdentifier CONTRIBUTION_IDENTIFIER = ContributionIdentifier
      .from(INSTANCE_ID, LOCAL_ID, CONTRIBUTION_TYPE);
  private static final String FR = "fr";
  private static final String EN = "en";
  private static final String DE = "de";
  private static final String COMPONENT_NAME = "componentNameTest";

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  @TestManagedMock
  private PublicationService publicationService;
  @TestManagedMock
  private ComponentAccessControl componentAccessControl;
  @TestManagedMock
  private Administration administration;

  @Mock
  private User receiver;

  @BeforeEach
  void setup(@TestManagedMock ContributionManager contributionManager,
      @TestManagedMock ComponentInstanceRoutingMapProviderByInstance routingMap,
      @TestManagedMock ComponentInstanceRoutingMapProvider routingMapProvider,
      @TestManagedMock ComponentInstanceRoutingMap instanceRoutingMap,
      @TestManagedMock SilverpeasComponentInstanceProvider instanceProvider,
      @Mock SilverpeasComponentInstance componentInstance, @Mock Contribution contribution) {

    when(componentInstance.getName()).thenReturn(COMPONENT_NAME);
    when(instanceProvider.getById(INSTANCE_ID)).thenReturn(Optional.of(componentInstance));
    when(instanceProvider.getComponentName(INSTANCE_ID)).thenReturn(COMPONENT_NAME);

    when(routingMap.getByInstanceId(INSTANCE_ID))
        .thenReturn(routingMapProvider);
    when(routingMapProvider.absolute()).thenReturn(instanceRoutingMap);
    when(instanceRoutingMap.getPermalink(CONTRIBUTION_IDENTIFIER))
        .thenReturn(UriBuilder.fromPath("").build());

    UserPreferences userPreferences = new UserPreferences("26", "fr", ZoneId.systemDefault(), null,
        null, false, false, false, UserMenuDisplay.DEFAULT);
    when(receiver.getId()).thenReturn("26");
    when(receiver.getUserPreferences()).thenReturn(userPreferences);

    when(contribution.getContributionId()).thenReturn(CONTRIBUTION_IDENTIFIER);
    when(contribution.getTitle()).thenReturn("super test");
    when(contributionManager.getById(CONTRIBUTION_IDENTIFIER))
        .thenReturn(Optional.of(contribution));

    when(UserProvider.get().getUser(receiver.getId())).thenReturn(receiver);
    mocker.setField(DisplayI18NHelper.class, Locale.getDefault().getLanguage(), "defaultLanguage");
  }

  @Test
  void durationReminderOf0MinuteOnGenericContributionShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder()
        .triggerBefore(0, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder, OffsetDateTime.parse("2018-02-21T00:00:00Z"));
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the contribution super test - 21.02.2018 00:00"));
    assertThat(titles.get(EN), is("Reminder about the contribution super test - 02/21/2018 00:00"));
    assertThat(titles.get(FR), is("Rappel sur la contribution super test - 21/02/2018 00:00"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("You set a reminder <b>just</b> before the contribution <b>super test</b> (21.02.2018 00:00)."));
    assertThat(contents.get(EN), is("You set a reminder <b>just</b> before the contribution <b>super test</b> (02/21/2018 00:00)."));
    assertThat(contents.get(FR), is("Vous avez demandé un rappel <b>juste</b> avant la contribution <b>super test</b> (21/02/2018 00:00)."));
  }

  @Test
  void durationReminderOf1MinuteOnGenericContributionShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder()
        .triggerBefore(1, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder, OffsetDateTime.parse("2018-02-20T23:59:00Z"));
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the contribution super test - 21.02.2018 00:00"));
    assertThat(titles.get(EN), is("Reminder about the contribution super test - 02/21/2018 00:00"));
    assertThat(titles.get(FR), is("Rappel sur la contribution super test - 21/02/2018 00:00"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("You set a reminder <b>1 minute</b> before the contribution <b>super test</b> (21.02.2018 00:00)."));
    assertThat(contents.get(EN), is("You set a reminder <b>1 minute</b> before the contribution <b>super test</b> (02/21/2018 00:00)."));
    assertThat(contents.get(FR), is("Vous avez demandé un rappel <b>1 minute</b> avant la contribution <b>super test</b> (21/02/2018 00:00)."));
  }

  @Test
  void durationReminderOf5MinutesOnGenericContributionShouldWork() throws Exception {
    final DurationReminder durationReminder = initReminderBuilder()
        .triggerBefore(5, TimeUnit.MINUTE, "", PROCESS_NAME);
    triggerDateTime(durationReminder, OffsetDateTime.parse("2018-02-20T23:55:00Z"));
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the contribution super test - 21.02.2018 00:00"));
    assertThat(titles.get(EN), is("Reminder about the contribution super test - 02/21/2018 00:00"));
    assertThat(titles.get(FR), is("Rappel sur la contribution super test - 21/02/2018 00:00"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("You set a reminder <b>5 minutes</b> before the contribution <b>super test</b> (21.02.2018 00:00)."));
    assertThat(contents.get(EN), is("You set a reminder <b>5 minutes</b> before the contribution <b>super test</b> (02/21/2018 00:00)."));
    assertThat(contents.get(FR), is("Vous avez demandé un rappel <b>5 minutes</b> avant la contribution <b>super test</b> (21/02/2018 00:00)."));
  }

  @Test
  void durationReminderOf0HourAndWithAnotherUserZoneIdOnGenericContributionShouldWork()
      throws Exception {
    receiver.getUserPreferences().setZoneId(ZoneId.of("Asia/Muscat"));
    final DurationReminder durationReminder = initReminderBuilder()
        .triggerBefore(0, TimeUnit.HOUR, "", PROCESS_NAME);
    triggerDateTime(durationReminder, OffsetDateTime.parse("2018-02-21T00:00:00Z"));
    final Map<String, String> titles = computeNotificationTitles(durationReminder);
    assertThat(titles.get(DE), is("Reminder about the contribution super test - 21.02.2018 00:00 (UTC)"));
    assertThat(titles.get(EN), is("Reminder about the contribution super test - 02/21/2018 00:00 (UTC)"));
    assertThat(titles.get(FR), is("Rappel sur la contribution super test - 21/02/2018 00:00 (UTC)"));
    final Map<String, String> contents = computeNotificationContents(durationReminder);
    assertThat(contents.get(DE), is("You set a reminder <b>just</b> before the contribution <b>super test</b> (21.02.2018 00:00 (UTC))."));
    assertThat(contents.get(EN), is("You set a reminder <b>just</b> before the contribution <b>super test</b> (02/21/2018 00:00 (UTC))."));
    assertThat(contents.get(FR), is("Vous avez demandé un rappel <b>juste</b> avant la contribution <b>super test</b> (21/02/2018 00:00 (UTC))."));
  }

  private Reminder.ReminderBuilder initReminderBuilder() {
    return new Reminder.ReminderBuilder().about(CONTRIBUTION_IDENTIFIER).withText("Dummy test")
        .forUser(receiver);
  }

  private void triggerDateTime(Reminder reminder, OffsetDateTime dateTime)
      throws IllegalAccessException {
    FieldUtils.writeField(reminder, "triggerDateTime", dateTime, true);
  }

  private Map<String, String> computeNotificationContents(Reminder reminder) {
    final UserNotification userNotification = new DefaultContributionReminderUserNotification(reminder).build();
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getContent(userNotification, FR));
    result.put(EN, getContent(userNotification, EN));
    result.put(DE, getContent(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private Map<String, String> computeNotificationTitles(Reminder reminder) {
    final UserNotification userNotification = new DefaultContributionReminderUserNotification(reminder).build();
    final Map<String, String> result = new HashMap<>();
    result.put(FR, getTitle(userNotification, FR));
    result.put(EN, getTitle(userNotification, EN));
    result.put(DE, getTitle(userNotification, DE));
    assertThat(result.get(FR), not(is(result.get(EN))));
    assertThat(result.get(EN), not(is(result.get(DE))));
    return result;
  }

  private String getContent(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getContent(language)
        .replaceAll("<!--BEFORE_MESSAGE_FOOTER--><!--AFTER_MESSAGE_FOOTER-->", "");
  }

  private String getTitle(final UserNotification userNotification, final String language) {
    return userNotification.getNotificationMetaData().getTitle(language);
  }

  static {
    // This static block permits to ensure that the UNIT TEST is entirely executed into UTC
    // TimeZone.
    TimeZone.setDefault(TimeZone.getTimeZone(UTC_ZONE_ID));
  }
}