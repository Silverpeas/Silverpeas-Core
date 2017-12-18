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
package org.silverpeas.core.reminder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cache.model.SimpleCache;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.contribution.ComponentInstanceContributionManager;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.scheduler.SchedulerInitializer;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Integration tests on the reminders
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class ReminderIT {

  private static final ContributionIdentifier CONTRIBUTION =
      ContributionIdentifier.from("myApp42", "42", "CustomContrib");

  private static final String USER_ID = "2";

  private static final String CACHE_KEY =
      ComponentInstanceContributionManager.class.getName() + "###myApp42";

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
        .testFocusedOn((warBuilder) -> {
          warBuilder.addAsResource("org/silverpeas/core/scheduler/create_quartz_tables.sql")
              .addAsResource("org/silverpeas/core/admin/create_space_components_database.sql")
              .addAsResource("org/silverpeas/core/reminder/create_table.sql")
              .addAsResource("org/silverpeas/core/reminder/reminder-dataset.sql")
              .addPackages(true, "org.silverpeas.core.initialization")
              .addPackages(false, "org.silverpeas.core.contribution");
        })
        .build();
  }

  @Before
  public void initSchedulers() {
    SchedulerInitializer.get().init();
    SimpleCache cache = CacheServiceProvider.getRequestCacheService().getCache();
    cache.put(CACHE_KEY, (ComponentInstanceContributionManager) contributionId -> Optional.of(
        new CustomContrib(contributionId).authoredBy(User.getById(USER_ID))));
  }

  @Test
  public void emptyTest() {
    // empty test to check the testing environment is working
  }

  @Test
  public void scheduleAReminderInDateTimeWillPersistIt() {
    final String reminderText = "Remind me!";
    final OffsetDateTime triggerDate = OffsetDateTime.now().plusSeconds(30);
    Reminder expectedReminder =
        new DateTimeReminder(CONTRIBUTION, User.getById(USER_ID)).withText(reminderText)
            .triggerAt(triggerDate)
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder actualReminder = ReminderRepository.get().getById(expectedReminder.getId());
    assertThat(actualReminder, notNullValue());
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION));
    assertThat(actualReminder.getUserId(), is(USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getTriggeringDate(),
        is(triggerDate.withOffsetSameInstant(ZoneOffset.UTC)));
  }

  @Test
  public void scheduleAReminderAtADurationBeforeAGivenDateTimeWillPersistIt() {
    final String reminderText = "Remind me!";
    Reminder expectedReminder =
        new DurationReminder(CONTRIBUTION, User.getById(USER_ID)).withText(reminderText)
            .triggerBefore(30, TimeUnit.SECOND, "startDate")
            .schedule();

    assertThat(expectedReminder.isPersisted(), is(true));
    assertThat(expectedReminder.isScheduled(), is(true));

    Reminder reminder = ReminderRepository.get().getById(expectedReminder.getId());
    assertThat(reminder, notNullValue());
    assertThat(reminder, instanceOf(DurationReminder.class));

    DurationReminder actualReminder = (DurationReminder) reminder;
    assertThat(actualReminder.getContributionId(), is(CONTRIBUTION));
    assertThat(actualReminder.getUserId(), is(USER_ID));
    assertThat(actualReminder.getText(), is(reminderText));
    assertThat(actualReminder.getDuration(), is(30));
    assertThat(actualReminder.getTimeUnit(), is(TimeUnit.SECOND));
    assertThat(actualReminder.getContributionProperty(), is("startDate"));
  }

  @Test
  public void basicReminderTriggeringShouldFireItOneShot() {
    final String reminderText = "Remind me!";
    Reminder reminder =
        new DurationReminder(CONTRIBUTION, User.getById(USER_ID)).withText(reminderText)
            .triggerBefore(30, TimeUnit.SECOND, "startDate")
            .schedule();
    assertThat(reminder.isTriggered(), is(false));

    await().atMost(31, SECONDS).until(isTriggered(reminder));
  }

  private Callable<Boolean> isTriggered(final Reminder reminder) {
    return () -> Reminder.getById(reminder.getId()).isTriggered();
  }
}
  