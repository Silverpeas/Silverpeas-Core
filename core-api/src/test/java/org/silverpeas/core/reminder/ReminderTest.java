/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.model.NoSuchPropertyException;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.test.rule.CommonAPIRule;

import java.time.OffsetDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.silverpeas.core.reminder.ReminderTestContext.PROCESS_NAME;

/**
 * Unit tests on the reminder engine. Its goal is to help to design it.
 * @author mmoquillon
 */
public class ReminderTest {

  @Rule
  public CommonAPIRule commonAPIRule = new CommonAPIRule();
  private ReminderTestContext context = new ReminderTestContext(commonAPIRule);

  @Before
  public void prepareInjection() {
    context.setUp();
  }

  @Test
  public void createNewReminderToTriggerBeforeTheStartDateOfAPlannableContribution() {
    ContributionIdentifier contribution = context.getPlannableContribution();
    User user = context.getUser();
    Reminder reminder = Reminder.make(contribution, user)
        .withText("Don't forget the meeting in two days!")
        .triggerBefore(2, TimeUnit.DAY, "startDate", PROCESS_NAME)
        .schedule();
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test
  public void useTriggerBeforeWithANonPlannable() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DurationReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerBefore(2, TimeUnit.DAY, "publicationDate")
        .schedule();
    assertThat(reminder.isSystemUser(), is(false));
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test
  public void useSystemTriggerBeforeWithANonPlannable() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    Reminder reminder = new DurationReminder(contribution, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerBefore(2, TimeUnit.DAY, "publicationDate")
        .schedule();
    assertThat(reminder.getUserId(), is("-1"));
    assertThat(reminder.isSystemUser(), is(true));
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test(expected = NoSuchPropertyException.class)
  public void useTriggerFromANonValidProperty() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DateTimeReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerFrom("startDate")
        .schedule();
  }

  @Test(expected = IllegalArgumentException.class)
  public void useTriggerFromANonValidPropertyType() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DateTimeReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerFrom("title")
        .schedule();
  }

  @Test(expected = NoSuchPropertyException.class)
  public void useTriggerBeforeANonValidProperty() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DurationReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerBefore(2, TimeUnit.DAY, "startDate")
        .schedule();
  }

  @Test(expected = IllegalArgumentException.class)
  public void useTriggerBeforeANonValidPropertyType() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DurationReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerBefore(2, TimeUnit.DAY, "title")
        .schedule();
  }

  @Test
  public void createNewReminderToTriggerAtDateTime() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = Reminder.make(contribution, user)
        .withText("Don't forget the meeting in two days!")
        .triggerAt(OffsetDateTime.now().plusMonths(1), PROCESS_NAME)
        .schedule();
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test
  public void createNewReminderToTriggerFromContributionModelProperty() {
    ContributionIdentifier contribution = context.getNonPlannableContribution();
    User user = context.getUser();
    Reminder reminder = Reminder.make(contribution, user)
        .withText("Don't forget the meeting in two days!")
        .triggerFrom("publicationDate", PROCESS_NAME)
        .schedule();
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test
  public void createNewReminderAboutPlannableToTriggerAtDateTime() {
    ContributionIdentifier contribution = context.getPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DateTimeReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerAt(OffsetDateTime.now().plusMonths(1))
        .schedule();
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test
  public void createNewSystemReminderAboutPlannableToTriggerAtDateTime() {
    ContributionIdentifier contribution = context.getPlannableContribution();
    Reminder reminder = new DateTimeReminder(contribution, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerAt(OffsetDateTime.now().plusMonths(1))
        .schedule();
    assertThat(reminder.getUserId(), is("-1"));
    assertThat(reminder.isSystemUser(), is(true));
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }

  @Test
  public void createNewReminderAboutPlannableToTriggerFromContributionModelProperty() {
    ContributionIdentifier contribution = context.getPlannableContribution();
    User user = context.getUser();
    Reminder reminder = new DateTimeReminder(contribution, user, PROCESS_NAME)
        .withText("Don't forget the meeting in two days!")
        .triggerFrom("startDate")
        .schedule();
    assertThat(reminder.isSystemUser(), is(false));
    assertThat(reminder.isPersisted(), is(true));
    assertThat(reminder.isScheduled(), is(true));
  }
}
  