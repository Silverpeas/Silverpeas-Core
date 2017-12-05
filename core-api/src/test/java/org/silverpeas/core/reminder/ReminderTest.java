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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.contribution.ContributionManager;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.model.identifier.UuidIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.persistence.datasource.model.jpa.PersistenceIdentifierSetter;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.ScheduledJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.test.rule.CommonAPI4Test;

import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the reminder engine. Its goal is to help to design it.
 * @author mmoquillon
 */
public class ReminderTest {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private final ContributionIdentifier contribution =
      ContributionIdentifier.from("calendar23", "event12", "event");

  @Before
  public void prepareInjection() {
    setUpContributions();
    setUpReminderRepository();
    setUpEntityManager();
    setUpScheduler();
  }

  @Test
  public void createNewReminder() {
    Reminder reminder = new Reminder(contribution).withText("Don't forget the meeting in two days!")
        .triggerBefore(2, TimeUnit.DAY);
    assertThat(reminder.isPersisted(), is(true));
    //assertThat(reminder.isScheduled(), is(true));
  }

  /**
   * We prepare a set of contributions ready to be get by the reminder to perform its business
   * operations.
   */
  private void setUpContributions() {
    ContributionManager contributionManager = mock(ContributionManager.class);
    when(contributionManager.getById(any(ContributionIdentifier.class))).thenAnswer(
        invocation -> Optional.of(new MyPlannableContribution(invocation.getArgument(0)).startingAt(
            OffsetDateTime.now())));
    commonAPI4Test.injectIntoMockedBeanContainer(contributionManager);
  }

  /**
   * We mock the JPA persistence layer as used in this test.
   */
  private void setUpEntityManager() {
    EntityManager entityManager = mock(EntityManager.class);
    EntityManagerProvider entityManagerProvider = mock(EntityManagerProvider.class);
    when(entityManagerProvider.getEntityManager()).thenReturn(entityManager);
    when(entityManager.find(eq(Reminder.class), any(UuidIdentifier.class))).thenReturn(
        new Reminder(contribution));
    commonAPI4Test.injectIntoMockedBeanContainer(entityManagerProvider);
    // inject a Transaction object, dependency of Reminder
    commonAPI4Test.injectIntoMockedBeanContainer(new Transaction());
  }

  /**
   * We mock the ReminderRepository.
   */
  private void setUpReminderRepository() {
    ReminderRepository repository = mock(ReminderRepository.class);
    when(repository.save(any(Reminder.class))).thenAnswer(invocation -> {
      Reminder reminder = invocation.getArgument(0);
      PersistenceIdentifierSetter.setIdTo(reminder);
      return reminder;
    });
    // inject a mock of the ReminderRepository
    commonAPI4Test.injectIntoMockedBeanContainer(repository);
  }

  /**
   * We mock the Silverpeas scheduler engine.
   */
  private void setUpScheduler() {
    Scheduler scheduler = mock(Scheduler.class);
    try {
      when(scheduler.scheduleJob(any(Job.class), any(JobTrigger.class))).thenAnswer(invocation -> {
        Job job = invocation.getArgument(0);
        when(scheduler.isJobScheduled(job.getName())).thenReturn(true);
        return mock(ScheduledJob.class);
      });
    } catch (SchedulerException e) {

    }
    commonAPI4Test.injectIntoMockedBeanContainer(scheduler);
  }
}
  