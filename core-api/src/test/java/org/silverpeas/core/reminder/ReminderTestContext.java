/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.reminder;

import org.silverpeas.core.ApplicationService;
import org.silverpeas.core.ApplicationServiceProvider;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.calendar.PlannableOnCalendar;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.EntityManagerProvider;
import org.silverpeas.core.persistence.datasource.model.jpa.PersistenceIdentifierSetter;
import org.silverpeas.core.personalization.UserMenuDisplay;
import org.silverpeas.core.personalization.UserPreferences;
import org.silverpeas.core.scheduler.PersistentScheduling;
import org.silverpeas.core.scheduler.ScheduledJob;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.test.TestBeanContainer;
import org.silverpeas.core.util.BeanContainer;
import org.silverpeas.core.util.ServiceProvider;

import javax.enterprise.util.AnnotationLiteral;
import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test context for the unit tests on the reminder.
 * @author mmoquillon
 */
public class ReminderTestContext {

  static final ReminderProcessName PROCESS_NAME = () -> "TestReminderProcess";

  private final ContributionIdentifier plannable =
      ContributionIdentifier.from("calendar23", "event12", PlannableOnCalendar.class.getSimpleName());
  private final ContributionIdentifier contribution =
      ContributionIdentifier.from("kmelia12", "12", Contribution.class.getSimpleName());
  private final User user = mock(User.class);

  public void setUp() {
    setUpUsers();
    setUpContributions();
    setUpEntityManager();
    setUpReminderRepository();
    setUpScheduler();
  }

  private void setUpUsers() {
    UserProvider userProvider = ServiceProvider.getService(UserProvider.class);
    when(user.getId()).thenReturn("2");
    when(user.getUserPreferences()).thenReturn(
        new UserPreferences("2", "fr", ZoneId.of("UTC"), "", "", false, false, false,
            UserMenuDisplay.DEFAULT));
    when(userProvider.getUser("2")).thenAnswer(a -> user);
  }

  public ContributionIdentifier getPlannableContribution() {
    return plannable;
  }

  public ContributionIdentifier getNonPlannableContribution() {
    return contribution;
  }

  public User getUser() {
    return user;
  }

  /**
   * We prepare a set of contributions ready to be get by the reminder to perform its business
   * operations.
   */
  private void setUpContributions() {
    ApplicationServiceProvider applicationServiceProvider =
        ServiceProvider.getService(ApplicationServiceProvider.class);
    ApplicationService applicationService = ServiceProvider.getService(ApplicationService.class);
    when(applicationServiceProvider.getApplicationServiceById(anyString())).thenReturn(
        Optional.of(applicationService));
    when(applicationService.getContributionById(any(ContributionIdentifier.class))).thenAnswer(
        invocation -> {
          ContributionIdentifier id = invocation.getArgument(0);
          Contribution contribution;
          if (PlannableOnCalendar.class.getSimpleName()
              .equals(id.getType())) {
            contribution = new MyPlannableContribution(id).startingAt(OffsetDateTime.now()
                .plusMonths(1));
          } else {
            Date publicationDate = Date.from(OffsetDateTime.now()
                .plusWeeks(1)
                .toInstant());
            contribution = new MyContribution(id).publishedAt(publicationDate);
          }
          return Optional.of(contribution);
        });
  }

  /**
   * We mock the JPA persistence layer as used in this test.
   */
  private void setUpEntityManager() {
    EntityManager entityManager = mock(EntityManager.class);
    EntityManagerProvider entityManagerProvider =
        ServiceProvider.getService(EntityManagerProvider.class);
    when(entityManagerProvider.getEntityManager()).thenReturn(entityManager);

    DurationReminder durationReminder = new DurationReminder(plannable, user, PROCESS_NAME);
    when(entityManager.find(eq(DurationReminder.class), any(ReminderIdentifier.class))).thenReturn(
        durationReminder);
    DateTimeReminder dateTimeReminder = new DateTimeReminder(contribution, user, PROCESS_NAME);
    when(entityManager.find(eq(DateTimeReminder.class), any(ReminderIdentifier.class))).thenReturn(
        dateTimeReminder);
  }

  /**
   * We mock the ReminderRepository.
   */
  private void setUpReminderRepository() {
    ReminderRepository repository = ServiceProvider.getService(ReminderRepository.class);
    when(repository.save(any(Reminder.class))).thenAnswer(invocation -> {
      Reminder reminder = invocation.getArgument(0);
      PersistenceIdentifierSetter.setIdTo(reminder, ReminderIdentifier.class);
      return reminder;
    });
  }

  /**
   * We mock the Silverpeas scheduler engine.
   */
  private void setUpScheduler() {
    Scheduler scheduler = mock(Scheduler.class);
    BeanContainer beanContainer = TestBeanContainer.getMockedBeanContainer();
    when(beanContainer.getBeanByType(Scheduler.class,
        new AnnotationLiteral<PersistentScheduling>() {})).thenReturn(scheduler);
    try {
      when(scheduler.scheduleJob(anyString(), any(JobTrigger.class),
          any(ReminderProcess.class))).thenAnswer(invocation -> {
        String jobName = invocation.getArgument(0);
        ScheduledJob job = mock(ScheduledJob.class);
        when(job.getName()).thenReturn(jobName);
        when(scheduler.isJobScheduled(jobName)).thenReturn(true);
        return job;
      });
    } catch (SchedulerException e) {
      // nothing to do
    }
  }
}
  