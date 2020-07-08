/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.reminder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.component.WAComponentRegistry;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.backgroundprocess.BackgroundProcessLogger;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.calendar.notification.CalendarEventUserNotificationReminder;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.notification.user.builder.AbstractContributionTemplateUserNotificationBuilder;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.personalization.service.DefaultPersonalizationService;
import org.silverpeas.core.personalization.service.PersonalizationService;
import org.silverpeas.core.scheduler.SchedulerInitializer;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.rule.MavenTargetDirectoryRule;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.core.util.logging.Level;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration tests on the reminders
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReminderIT {

  private static final ReminderProcessName PROCESS_NAME = () -> "TestReminderProcess";

  private static final ContributionIdentifier CONTRIBUTION_FOR_NOW =
      ContributionIdentifier.from("kmelia42", "42", EventContrib.class.getSimpleName());

  private static final ContributionIdentifier CONTRIBUTION_FOR_LATER =
      ContributionIdentifier.from("kmelia42", "43", EventContrib.class.getSimpleName());

  private static final String SYSTEM_USER_ID = "-1";
  private static final String USER_ID = "2";

  private static final String REMINDER_ID = "Reminder#1ed074deee814b6a8035b9ced02ff56d";

  @Rule
  public MavenTargetDirectoryRule mavenTargetDirectoryRule = new MavenTargetDirectoryRule(this);

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/scheduler/create_quartz_tables.sql",
          "/org/silverpeas/core/admin/create_space_components_database.sql",
          "/org/silverpeas/core/reminder/create_table.sql")
          .loadInitialDataSetFrom("/org/silverpeas/core/reminder/reminder-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(ReminderIT.class)
        .addAdministrationFeatures()
        .addStringTemplateFeatures()
        .addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core-api")
        .addMavenDependencies("org.awaitility:awaitility", "org.antlr:stringtemplate")
        .addPackages(true, AbstractContributionTemplateUserNotificationBuilder.class.getPackage().getName())
        .addClasses(CalendarEventUserNotificationReminder.class,
            DefaultContributionReminderUserNotification.class, PublicationTemplateManager.class)
        .testFocusedOn((warBuilder) ->
          warBuilder.addAsResource("org/silverpeas/core/scheduler/create_quartz_tables.sql")
              .addAsResource("org/silverpeas/core/admin/create_space_components_database.sql")
              .addAsResource("org/silverpeas/core/reminder/create_table.sql")
              .addAsResource("org/silverpeas/core/reminder/reminder-dataset.sql")
              .addPackages(true, "org.silverpeas.core.initialization")
              .addPackages(false, "org.silverpeas.core.contribution")
              .addPackages(true, "org.silverpeas.core.personalization")
        )
        .build();
  }

  @Before
  public void initSchedulers() throws Exception {
    File silverpeasHome = mavenTargetDirectoryRule.getResourceTestDirFile();
    SystemWrapper.get().getenv().put("SILVERPEAS_HOME", silverpeasHome.getPath());
    WAComponentRegistry.get().init();
    CacheServiceProvider.clearAllThreadCaches();

    SchedulerInitializer.get().init();
    when(StubbedPersonalizationService.getMock().getUserSettings(anyString()))
        .then((Answer<UserPreferences>) i -> {
          final String userId = (String) i.getArguments()[0];
          return new UserPreferences(userId, "fr", ZoneId.of("UTC"), "", "", false, false, false,
              UserMenuDisplay.DEFAULT);
        });
    KmeliaInstanceContributionManager manager = KmeliaInstanceContributionManager.get();
    User aUser = User.getById(USER_ID);
    manager.clearAll();
    manager.addContribution(new EventContrib(CONTRIBUTION_FOR_NOW).authoredBy(aUser));
    manager.addContribution(new EventContrib(CONTRIBUTION_FOR_LATER).authoredBy(aUser)
        .publishAt(OffsetDateTime.now().plusSeconds(45)));
    new BackgroundProcessLogger().init();
    BackgroundProcessLogger.get().setLevel(Level.DEBUG);
  }

  @Test
  public void emptyTest() {
    // empty test to check the testing environment is working
  }

  @Test
  public void getAllRemindersAboutAGivenContribution() {
    List<Reminder> reminders = Reminder.getByContribution(
        ContributionIdentifier.from("myApp42", "42", EventContrib.class.getSimpleName()));
    assertThat(reminders.size(), is(2));
    assertThat(reminders.get(0).getId(), is("Reminder#1ed074deee814b6a8035b9ced02ff56d"));
    assertThat(reminders.get(0), instanceOf(DateTimeReminder.class));
    assertThat(reminders.get(0).getUserId(), is("2"));
    assertThat(reminders.get(0).getContributionId().getLocalId(), is("42"));
    assertThat(reminders.get(1).getId(), is("Reminder#1ed074deee814b6a8035b9ced02ff56e"));
    assertThat(reminders.get(1), instanceOf(DurationReminder.class));
    assertThat(reminders.get(1).getUserId(), is("3"));
    assertThat(reminders.get(1).getContributionId().getLocalId(), is("42"));
  }

  @Test
  public void getAllRemindersOfAGivenUser() {
    List<Reminder> reminders = Reminder.getByUser(User.getById("3"));
    assertThat(reminders.size(), is(2));
    assertThat(reminders.get(0).getId(), is("Reminder#1ed074deee814b6a8035b9ced02ff56e"));
    assertThat(reminders.get(0), instanceOf(DurationReminder.class));
    assertThat(reminders.get(0).getUserId(), is("3"));
    assertThat(reminders.get(0).getContributionId().getLocalId(), is("42"));
    assertThat(reminders.get(1).getId(), is("Reminder#1ed074deee814b6a8035b9ced02ff56f"));
    assertThat(reminders.get(1), instanceOf(DateTimeReminder.class));
    assertThat(reminders.get(1).getUserId(), is("3"));
    assertThat(reminders.get(1).getContributionId().getLocalId(), is("12"));
  }

  @Test
  public void getAllRemindersOfAGivenUserAboutAGivenContribution() {
    List<Reminder> reminders = Reminder.getByContributionAndUser(
        ContributionIdentifier.from("myApp42", "42", EventContrib.class.getSimpleName()),
        User.getById("3"));
    assertThat(reminders.size(), is(1));
    assertThat(reminders.get(0).getId(), is("Reminder#1ed074deee814b6a8035b9ced02ff56e"));
    assertThat(reminders.get(0), instanceOf(DurationReminder.class));
    assertThat(reminders.get(0).getUserId(), is("3"));
    assertThat(reminders.get(0).getContributionId().getLocalId(), is("42"));
  }

  @Test
  public void getNoRemindersAboutAContributionWithoutAnyReminders() {
    List<Reminder> reminders = Reminder.getByContribution(
        ContributionIdentifier.from("bidule22", "22", EventContrib.class.getSimpleName()));
    assertThat(reminders.isEmpty(), is(true));
  }

  @Test
  public void getNoRemindersOfAUserHavingSetNoReminders() {
    List<Reminder> reminders = Reminder.getByUser(User.getById("1"));
    assertThat(reminders.isEmpty(), is(true));
  }

  @Test
  public void getNoRemindersOfAUserAboutAContributionWithoutAnyReminders() {
    List<Reminder> reminders = Reminder.getByContributionAndUser(
        ContributionIdentifier.from("bidule22", "22", EventContrib.class.getSimpleName()),
        User.getById("2"));
    assertThat(reminders.isEmpty(), is(true));
  }

  @Test
  public void getNoRemindersOfAUserHavingSetNoRemindersAndAboutAContribution() {
    List<Reminder> reminders = Reminder.getByContributionAndUser(
        ContributionIdentifier.from("myApp42", "42", EventContrib.class.getSimpleName()),
        User.getById("1"));
    assertThat(reminders.isEmpty(), is(true));
  }

  @Test
  public void scheduleAReminderInDateTimeWillPersistIt() {
    final String reminderText = "Remind me!";
    final OffsetDateTime triggerDate = OffsetDateTime.now().plusDays(1);
    Reminder expectedReminder =
        new DateTimeReminder(CONTRIBUTION_FOR_NOW, User.getById(USER_ID), PROCESS_NAME).withText(reminderText)
            .triggerAt(triggerDate)
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder reminder = Reminder.getById(expectedReminder.getId());
    assertThat(reminder, notNullValue());
    assertThat(reminder, instanceOf(DateTimeReminder.class));

    DateTimeReminder actualReminder = (DateTimeReminder) reminder;
    assertThat(actualReminder, notNullValue());
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION_FOR_NOW));
    assertThat(actualReminder.getUserId(), is(USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getDateTime(), is(triggerDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  public void scheduleASystemReminderInDateTimeWillPersistIt() {
    final String reminderText = "Remind me!";
    final OffsetDateTime triggerDate = OffsetDateTime.now().plusDays(1);
    Reminder expectedReminder =
        new DateTimeReminder(CONTRIBUTION_FOR_NOW, PROCESS_NAME).withText(reminderText)
            .triggerAt(triggerDate)
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder reminder = Reminder.getById(expectedReminder.getId());
    assertThat(reminder, notNullValue());
    assertThat(reminder, instanceOf(DateTimeReminder.class));

    DateTimeReminder actualReminder = (DateTimeReminder) reminder;
    assertThat(actualReminder, notNullValue());
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION_FOR_NOW));
    assertThat(actualReminder.getUserId(), is(SYSTEM_USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getDateTime(), is(triggerDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  public void scheduleASystemReminderInDateTimeFromModelPropertyWillPersistIt() {
    final String reminderText = "Remind me!";
    Reminder expectedReminder =
        new DateTimeReminder(CONTRIBUTION_FOR_LATER, PROCESS_NAME).withText(reminderText)
            .triggerFrom("publicationDate")
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder reminder = Reminder.getById(expectedReminder.getId());
    assertThat(reminder, notNullValue());
    assertThat(reminder, instanceOf(DateTimeReminder.class));

    EventContrib forLater = (EventContrib) KmeliaInstanceContributionManager.get()
        .getById(CONTRIBUTION_FOR_LATER).orElseThrow(IllegalArgumentException::new);

    DateTimeReminder actualReminder = (DateTimeReminder) reminder;
    assertThat(actualReminder, notNullValue());
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION_FOR_LATER));
    assertThat(actualReminder.getUserId(), is(SYSTEM_USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getDateTime(), is(forLater.getPublicationDate().withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  public void scheduleAReminderAtADurationBeforeAGivenAttributeWillPersistIt() {
    final String reminderText = "Remind me!";
    Reminder expectedReminder =
        new DurationReminder(CONTRIBUTION_FOR_LATER, User.getById(USER_ID), PROCESS_NAME).withText(reminderText)
            .triggerBefore(30, TimeUnit.SECOND, "publicationDate")
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder reminder = Reminder.getById(expectedReminder.getId());
    assertThat(reminder, notNullValue());
    assertThat(reminder, instanceOf(DurationReminder.class));

    DurationReminder actualReminder = (DurationReminder) reminder;
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION_FOR_LATER));
    assertThat(actualReminder.getUserId(), is(USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getDuration(), is(30));
    assertThat(actualReminder.getTimeUnit(), is(TimeUnit.SECOND));
    assertThat(actualReminder.getContributionProperty(), is("publicationDate"));
  }

  @Test
  public void scheduleAReminderAtADurationBeforeAGivenPropertyWillPersistIt() {
    final String reminderText = "Remind me!";
    Reminder expectedReminder =
        new DurationReminder(CONTRIBUTION_FOR_NOW, User.getById(USER_ID), PROCESS_NAME).withText(reminderText)
            .triggerBefore(30, TimeUnit.SECOND, "nextOccurrenceSince")
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder reminder = Reminder.getById(expectedReminder.getId());
    assertThat(reminder, notNullValue());
    assertThat(reminder, instanceOf(DurationReminder.class));

    DurationReminder actualReminder = (DurationReminder) reminder;
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION_FOR_NOW));
    assertThat(actualReminder.getUserId(), is(USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getDuration(), is(30));
    assertThat(actualReminder.getTimeUnit(), is(TimeUnit.SECOND));
    assertThat(actualReminder.getContributionProperty(), is("nextOccurrenceSince"));
  }

  @Test
  public void rescheduleAScheduledReminderShouldApplyTheChange() {
    final String reminderText = "Remind me!";
    final OffsetDateTime triggerDate = OffsetDateTime.now().plusSeconds(30);
    DateTimeReminder reminder = getAReminderScheduledInOneDay();
    assertThat(reminder.isScheduled(), is(true));
    reminder.withText(reminderText).triggerAt(triggerDate).schedule();

    DateTimeReminder beforeTriggered = (DateTimeReminder) Reminder.getById(reminder.getId());
    assertThat(beforeTriggered, notNullValue());
    assertThat(beforeTriggered.isTriggered(), is(false));
    assertThat(beforeTriggered.getContributionId(), is(reminder.getContributionId()));
    assertThat(beforeTriggered.getUserId(), is(reminder.getUserId()));
    assertThat(beforeTriggered.isSystemUser(), is(false));
    assertThat(beforeTriggered.getText(), is(reminderText));
    assertThat(beforeTriggered.getDateTime(), is(triggerDate.withOffsetSameInstant(ZoneOffset.UTC)));

    await().pollInterval(5, SECONDS).timeout(5, MINUTES).until(isTriggered(reminder));
    assertThat(reminder.isScheduled(), is(false));
    final DateTimeReminder afterTriggered = (DateTimeReminder) Reminder.getById(reminder.getId());
    assertThat(afterTriggered, notNullValue());
    assertThat(afterTriggered.isScheduled(), is(false));
    assertThat(afterTriggered.isTriggered(), is(true));
  }

  @Test
  public void rescheduleAScheduledSystemReminderShouldRemoveItAfterTriggered() {
    final String reminderText = "Remind me!";
    final OffsetDateTime triggerDate = OffsetDateTime.now().plusSeconds(30);
    DateTimeReminder reminder = getASystemReminderScheduledInOneDay();
    assertThat(reminder.isScheduled(), is(true));
    reminder.withText(reminderText).triggerAt(triggerDate).schedule();

    final DateTimeReminder beforeTriggered = (DateTimeReminder) Reminder.getById(reminder.getId());
    assertThat(beforeTriggered, notNullValue());
    assertThat(beforeTriggered.getContributionId(), is(reminder.getContributionId()));
    assertThat(beforeTriggered.getUserId(), is(reminder.getUserId()));
    assertThat(beforeTriggered.isSystemUser(), is(true));
    assertThat(beforeTriggered.getText(), is(reminderText));
    assertThat(beforeTriggered.getDateTime(), is(triggerDate.withOffsetSameInstant(ZoneOffset.UTC)));

    await().pollInterval(5, SECONDS).timeout(5, MINUTES).until(isDeleted(reminder));
    assertThat(reminder.isScheduled(), is(false));
  }

  @Test
  public void basicDateTimeReminderTriggeringShouldFireItOneShot() {
    final String reminderText = "Remind me!";
    Reminder reminder =
        new DateTimeReminder(CONTRIBUTION_FOR_NOW, User.getById(USER_ID), PROCESS_NAME).withText(reminderText)
            .triggerAt(OffsetDateTime.now().plusSeconds(30))
            .schedule();
    assertThat(reminder.isSchedulable(), is(true));
    assertThat(reminder.isTriggered(), is(false));

    await().pollInterval(5, SECONDS).timeout(5, MINUTES).until(isTriggered(reminder));

    reminder = Reminder.getById(reminder.getId());
    await().pollInterval(1, SECONDS).timeout(5, SECONDS).until(isNotScheduled(reminder));
    assertThat(reminder.isScheduled(), is(false));
    assertThat(reminder.isSchedulable(), is(false));
  }

  @Test
  public void basicDurationReminderTriggeringShouldFireItOneShot() {
    final String reminderText = "Remind me!";
    Reminder reminder =
        new DurationReminder(CONTRIBUTION_FOR_LATER, User.getById(USER_ID), PROCESS_NAME).withText(reminderText)
            .triggerBefore(30, TimeUnit.SECOND, "publicationDate")
            .schedule();
    assertThat(reminder.isSchedulable(), is(true));
    assertThat(reminder.isTriggered(), is(false));

    await().pollInterval(5, SECONDS).timeout(5, MINUTES).until(isTriggered(reminder));

    reminder = Reminder.getById(reminder.getId());
    assertThat(reminder.isScheduled(), is(false));
    assertThat(reminder.isSchedulable(), is(false));
  }

  @Test
  public void repeatableDurationReminderTriggeringShouldFireItSeveralTimes() {
    final String reminderText = "Remind me!";
    Reminder reminder =
        new DurationReminder(CONTRIBUTION_FOR_NOW, User.getById(USER_ID), PROCESS_NAME).withText(reminderText)
            .triggerBefore(30, TimeUnit.SECOND, "nextOccurrenceSince")
            .schedule();
    assertThat(reminder.isSchedulable(), is(true));
    assertThat(reminder.isTriggered(), is(false));

    await().pollInterval(5, SECONDS).timeout(5, MINUTES).until(isTriggered(reminder));
    assertThat(reminder.isSchedulable(), is(true));
    assertThat(reminder.isScheduled(), is(true));
    final DurationReminder afterFirstTrigger = (DurationReminder) Reminder.getById(reminder.getId());
    assertThat(afterFirstTrigger, notNullValue());
    assertThat(afterFirstTrigger.isScheduled(), is(true));
    assertThat(afterFirstTrigger.isTriggered(), is(true));

    await().pollInterval(5, SECONDS).timeout(5, MINUTES).until(isTriggered(reminder));
    assertThat(reminder.isSchedulable(), is(true));
    assertThat(reminder.isScheduled(), is(true));

    reminder = Reminder.getById(reminder.getId());
    assertThat(reminder.computeTriggeringDate(), notNullValue());
  }

  @Test
  public void unscheduleANonYetTriggeredReminderShouldRemoveIt() {
    Reminder reminder = getAReminderScheduledInOneDay();
    assertThat(reminder.isTriggered(), is(false));
    assertThat(reminder.isScheduled(), is(true));

    reminder.unschedule();
    assertThat(reminder.isScheduled(), is(false));
    Reminder unscheduledReminder = Reminder.getById(reminder.getId());
    assertThat(unscheduledReminder, nullValue());
  }

  @Test
  public void unscheduleAnAlreadyTriggeredReminderShouldRemoveIt() {
    Reminder reminder = getATriggeredReminder();
    assertThat(reminder.isTriggered(), is(true));
    assertThat(reminder.isScheduled(), is(false));

    reminder.unschedule();
    assertThat(reminder.isScheduled(), is(false));
    Reminder unscheduledReminder = Reminder.getById(reminder.getId());
    assertThat(unscheduledReminder, nullValue());
  }

  private DateTimeReminder getAReminderScheduledInOneDay() {
    return new DateTimeReminder(CONTRIBUTION_FOR_NOW, User.getById(USER_ID), PROCESS_NAME).triggerAt(
        OffsetDateTime.now().plusDays(1)).schedule();
  }

  private DateTimeReminder getASystemReminderScheduledInOneDay() {
    return new DateTimeReminder(CONTRIBUTION_FOR_NOW, PROCESS_NAME).triggerAt(
        OffsetDateTime.now().plusDays(1)).schedule();
  }

  private Reminder getATriggeredReminder() {
    return Reminder.getById(REMINDER_ID);
  }

  private Callable<Boolean> isDeleted(final Reminder reminder) {
    return () -> Reminder.getById(reminder.getId()) == null;
  }

  private Callable<Boolean> isTriggered(final Reminder reminder) {
    return () -> Reminder.getById(reminder.getId()).isTriggered();
  }

  private Callable<Boolean> isNotScheduled(final Reminder reminder) {
    return () -> !Reminder.getById(reminder.getId()).isScheduled();
  }

  /**
   * @author Yohann Chastagnier
   */
  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedPersonalizationService extends DefaultPersonalizationService {

    private PersonalizationService mock = mock(PersonalizationService.class);

    static PersonalizationService getMock() {
      return ((StubbedPersonalizationService) ServiceProvider
          .getService(PersonalizationService.class)).mock;
    }

    @Override
    public UserPreferences getUserSettings(final String userId) {
      return mock.getUserSettings(userId);
    }
  }
}
  