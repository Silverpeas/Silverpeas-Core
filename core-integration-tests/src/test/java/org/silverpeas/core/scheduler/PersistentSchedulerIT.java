/*
 * Copyright (C) 2000 - 2025 Silverpeas
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
package org.silverpeas.core.scheduler;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.scheduler.SchedulerInitializer.SchedulerType;
import org.silverpeas.core.scheduler.trigger.CronJobTrigger;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.inject.Inject;
import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.concurrent.Callable;

import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.Assert.fail;

/**
 * The scheduling engine in Silverpeas provides an API to get either a volatile or a persistent
 * scheduler. The first one is for scheduling volatile jobs in the time, jobs that will be
 * discarded at each VM restarting. The last one is for scheduling persistent jobs, that is to say
 * the scheduled jobs are serialized into a persistence context so that they can be restored at
 * each VM restarting. Both are built atop of an existing scheduling system (currently Quartz) and
 * the Scheduling Engine encapsulates it. It keeps a backward compatibility with an old previous
 * API, this is why a there is a great use of the scheduler event listeners in the code to perform
 * the actual jobs (instead of using a {@link Job} itself).
 * <p>
 * This integration test is about the persistent scheduler.
 * </p>
 */
@RunWith(Arquillian.class)
public class PersistentSchedulerIT {

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createDefaultTables();

  private static final String JOB_NAME = "persistent-test";
  @Inject
  private MySchedulingEventListener eventHandler;
  private Scheduler scheduler = null;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(PersistentSchedulerIT.class)
        .enableSchedulingFeatures()
        .addMavenDependencies("org.awaitility:awaitility", "org.antlr:ST4")
        .testFocusedOn((warBuilder) ->
            warBuilder.addPackages(true, "org.silverpeas.core.initialization"))
        .build();
  }

  @Before
  public void setUp() {
    SchedulerInitializer.get().init(SchedulerType.PERSISTENT);
    scheduler = SchedulerProvider.getPersistentScheduler();
    assertThat(scheduler, notNullValue());
    assertThat(eventHandler, notNullValue());
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(false));
    eventHandler.reset();
  }

  @After
  public void tearDown() {
    try {
      if (scheduler.isJobScheduled(JOB_NAME)) {
        scheduler.unscheduleJob(JOB_NAME);
      }
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  /**
   * An empty test just to check the setting up of the fixture is ok.
   */
  @Test
  public void emptyTest() {
    assertThat(true, is(true));
  }

  @Test
  public void schedulingAJobThatIsPerformedByAnEventListener() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusHours(1));
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(job, notNullValue());
    assertThat(JOB_NAME, is(job.getName()));
    assertThat(Instant.now().plus(1, HOURS),
        greaterThanOrEqualTo(job.getNextExecutionTime().toInstant()));
  }

  @Test
  public void schedulingAJobWithoutEventListener() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusHours(1));
    ScheduledJob job = scheduler.scheduleJob(new MyJob(JOB_NAME), trigger);
    assertThat(job, notNullValue());
    assertThat(JOB_NAME, is(job.getName()));
    assertThat(Instant.now().plus(1, HOURS),
        greaterThanOrEqualTo(job.getNextExecutionTime().toInstant()));
  }

  @Test
  public void schedulingAJobWithAnEventListener() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusHours(1));
    ScheduledJob job = scheduler.scheduleJob(new MyJob(JOB_NAME), trigger, eventHandler);
    assertThat(job, notNullValue());
    assertThat(JOB_NAME, is(job.getName()));
    assertThat(Instant.now().plus(1, HOURS),
        greaterThanOrEqualTo(job.getNextExecutionTime().toInstant()));
  }

  @Test(expected = SchedulerException.class)
  public void schedulingAnAlreadyScheduledJobShouldThrowASchedulerException() throws Exception {
    scheduleAJob(JOB_NAME);
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusHours(1));
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
  }

  @Test(expected = SchedulerException.class)
  public void schedulingAnAlreadyScheduledJobExecutionShouldThrowASchedulerException()
      throws Exception {
    scheduleAJob(JOB_NAME);
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusHours(1));
    scheduler.scheduleJob(new MyJob(JOB_NAME), trigger, eventHandler);
  }

  @Test
  public void schedulingAtGivenTimeAJobExecutionShouldSendAnExecutionEventAtTheExpectedTime()
      throws Exception {
    SilverLogger.getLogger(this)
        .info(
            "scheduling at given time a job execution should send an execution event at the " +
                "expected time");
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusSeconds(30));
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    await().pollInterval(5, SECONDS).atMost(44, SECONDS).until(eventHandlingCompleted());
    assertThat(eventHandler.isJobFired(), is(true));
    assertThat(eventHandler.isJobSucceeded(), is(true));
  }

  @Test
  public void schedulingAJobInThePastShouldFireIt() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().minusDays(1));
    scheduler.scheduleJob(new MyJob(JOB_NAME), trigger);
    await().pollInterval(5, SECONDS).atMost(44, SECONDS).until(jobIsExecuted());
    assertThat(isJobExecuted(), is(true));
  }

  @Test
  public void aFailureJobExecutionShouldFireACorrespondingSchedulerEvent() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusSeconds(30));
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler.mustFail());
    await().pollInterval(5, SECONDS).atMost(44, SECONDS).until(eventHandlingCompleted());
    assertThat(eventHandler.isJobFired(), is(true));
    assertThat(eventHandler.isJobSucceeded(), is(false));
  }

  @Test
  public void schedulingAJobWithoutEventListenerShouldRunThatJobAtTheExpectedTime()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusSeconds(30));
    scheduler.scheduleJob(new MyJob(JOB_NAME), trigger);
    await().pollInterval(5, SECONDS).atMost(44, SECONDS).until(jobIsExecuted());
    assertThat(isJobExecuted(), is(true));
  }

  @Test
  public void aFailureJobShouldFireACorrespondingSchedulerEvent() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusSeconds(30));
    scheduler.scheduleJob(new MyFailureJob(JOB_NAME), trigger, eventHandler);
    await().pollInterval(5, SECONDS).atMost(44, SECONDS).until(eventHandlingCompleted());
    assertThat(eventHandler.isJobFired(), is(true));
    assertThat(eventHandler.isJobSucceeded(), is(false));
  }

  @Test
  public void schedulingAtGivenTimeAJobShouldRunThatJobAtTheExpectedTime() throws Exception {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusSeconds(30));
    scheduler.scheduleJob(new MyJob(JOB_NAME), trigger, eventHandler);
    await().pollInterval(5, SECONDS).atMost(44, SECONDS).until(jobIsExecuted());
    assertThat(eventHandler.isJobSucceeded(), is(true));
  }

  @Test
  public void schedulingWithDayOfMonthAndDayOfWeekBothSetShouldThrowAnException() {
    try {
      String cron = "* * 24 * 3";
      JobTrigger.triggerAt(cron);
      fail("A SchedulerException should be thrown!");
    } catch (ParseException ex) {
      assertThat("Support for specifying both a day-of-week AND a day-of-month parameter is not " +
          "implemented.", is(ex.getMessage()));
    }
  }

  @Test
  public void aScheduledJobShouldBeFound() throws Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    int minute = calendar.get(Calendar.MINUTE);
    String cron = minute + " * * * *";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
    assertThat(scheduler.getScheduledJob(JOB_NAME).isPresent(), is(true));
  }

  @Test
  public void aScheduledJobAtAGivenTimeInEveryDayShouldBeFound() throws Exception {
    String cron = "0 5 * * *";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
  }

  @Test
  public void aScheduledJobAtWithATwoDigitsShouldBeFound() throws Exception {
    String cron = "05 13 * * *";
    CronJobTrigger trigger = (CronJobTrigger) JobTrigger.triggerAt(cron);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
  }

  @Test
  public void aScheduledJobAtWhateverTheDayOfWeekShouldBeFound() throws Exception {
    String cron = "0 5 * * ?";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
  }

  @Test
  public void aScheduledJobAtWhateverTheDayOfMonthShouldBeFound() throws Exception {
    String cron = "0 5 ? * *";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
  }

  @Test
  public void aScheduledJobAtASpecificDayOfWeekShouldBeFound() throws Exception {
    String cron = "0 5 * * 6";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(true));
  }

  @Test(expected = ParseException.class)
  public void aScheduledJobAtWhateverTheDayOfWeekAndMonthShouldThrowAnException() throws Exception {
    String cron = "0 5 ? * ?";
    JobTrigger.triggerAt(cron);
  }

  @Test
  public void aNonScheduledJobShouldBeNotFound() throws Exception {
    assertThat(scheduler.isJobScheduled(JOB_NAME), is(false));
  }

  @Test
  public void unscheduleANonScheduledJobShouldDoesNothing() throws Exception {
    scheduler.unscheduleJob(JOB_NAME);
    assertThat(true, is(true));
  }

  /**
   * Is a job was executed at a given time?
   * @return true if a job was executed.
   */
  private Callable<Boolean> jobIsExecuted() {
    return this::isJobExecuted;
  }

  /**
   * Is the event handler completed its treatment?
   * @return true if the event handler completes its treatment on the event fired by the scheduler.
   */
  private Callable<Boolean> eventHandlingCompleted() {
    return () -> eventHandler.isCompleted();
  }

  private boolean isJobExecuted() {
    return TestJobExecutionContext.get().isJobExecuted();
  }

  /**
   * Schedules a job under the specified name.
   * This method is dedicated for fixture preparations. If the job scheduling throw an exception,
   * then the fixture will fail.
   * @param jobName the name of the job to schedule.
   */
  protected void scheduleAJob(final String jobName) {
    JobTrigger trigger = JobTrigger.triggerAt(OffsetDateTime.now().plusSeconds(30));
    try {
      ScheduledJob job = scheduler.scheduleJob(jobName, trigger, eventHandler);
      assertThat(job, notNullValue());
    } catch (SchedulerException ex) {
      fail(ex.getMessage());
    }
  }
}
