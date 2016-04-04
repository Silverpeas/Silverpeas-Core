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

package org.silverpeas.core.scheduler;

import org.silverpeas.core.scheduler.trigger.CronJobTrigger;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;

import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.Callable;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;

/**
 * The scheduling system is backed by an interface, Scheduler, and a factory on implementation of
 * this interface, SchedulerFactory.
 * The factory hides the concrete underlying scheduling system in use by Silverpeas and provides a
 * a single access to it through the Scheduler interface.
 * Silverpeas scheduling API provides its own interface to manage scheduled jobs that are eaten by
 * a scheduler and keeps actually a backward compability with its old previous API.
 * This test checks the current concrete scheduler is ok.
 */
@RunWith(Arquillian.class)
public class SchedulerTest {

  private static final String JOB_NAME = "test";
  private MySchedulingEventListener eventHandler;
  private boolean isJobExecuted;
  private Scheduler scheduler = null;

  public SchedulerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }


  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SchedulerTest.class)
        .addCommonBasicUtilities()
        .addSchedulerFeatures()
        .addMavenDependencies("com.jayway.awaitility:awaitility", "org.antlr:stringtemplate")
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.initialization");
        }).build();
  }

  @Before
  public void setUp() {
    scheduler = SchedulerProvider.getScheduler();
    assertThat(scheduler, notNullValue());
    eventHandler = new MySchedulingEventListener();
    isJobExecuted = false;
  }

  @After
  public void tearDown() throws Exception {
    scheduler.unscheduleJob(JOB_NAME);
  }

  /**
   * An empty test just to check the setting up of the fixture is ok.
   */
  @Test
  @Ignore
  public void emptyTest() {

  }

  @Test
  public void schedulingAJobThatIsPerformedByAnEventListener()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertThat(job, notNullValue());
    assertThat(JOB_NAME, is(job.getName()));
    assertThat(eventHandler, is(job.getSchedulerEventListener()));
    assertThat(trigger, is(job.getTrigger()));
  }

  @Test
  public void schedulingAJobWithoutEventListener()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {
      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger);
    assertThat(job, notNullValue());
    assertThat(JOB_NAME, is(job.getName()));
    assertThat(job.getSchedulerEventListener(), nullValue());
    assertThat(trigger, is(job.getTrigger()));
  }

  @Test
  public void schedulingAJobWithAnEventListener()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {
      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger, eventHandler);
    assertThat(job, notNullValue());
    assertThat(JOB_NAME, is(job.getName()));
    assertThat(eventHandler, is(job.getSchedulerEventListener()));
    assertThat(trigger, is(job.getTrigger()));
  }

  @Test(expected = SchedulerException.class)
  public void schedulingAnAlreadyScheduledJobShouldThrowASchedulerException() throws Exception {
    scheduleAJob(JOB_NAME);
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
  }

  @Test(expected = SchedulerException.class)
  public void schedulingAnAlreadyScheduledJobExecutionShouldThrowASchedulerException()
      throws Exception {
    scheduleAJob(JOB_NAME);
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(new Job(JOB_NAME) {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger, eventHandler);
  }

  @Test
  public void schedulingEveryTimeAJobExecutionShouldSendAnExecutionEventAtTheExpectedTime()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    await().atMost(2, SECONDS).until(eventHandlingCompleted());
    assertThat(eventHandler.isJobFired(), is(true));
    assertThat(eventHandler.isJobSucceeded(), is(true));
  }

  @Test
  public void aFailureJobExecutionShouldFireACorrespondingSchedulerEvent() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler.mustFail());
    await().atMost(2, SECONDS).until(eventHandlingCompleted());
    assertThat(eventHandler.isJobFired(), is(true));
    assertThat(eventHandler.isJobSucceeded(), is(false));
  }

  @Test
  public void schedulingAJobWithoutEventListenerShouldRunThatJobAtTheExpectedTime()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(new Job(JOB_NAME) {
      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger);
    await().atMost(2, SECONDS).until(jobIsExecuted());
    assertThat(isJobExecuted(), is(true));
  }

  @Test
  public void aFailureJobShouldFireACorrespondingSchedulerEvent() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(new Job(JOB_NAME) {
      @Override
      public void execute(JobExecutionContext context) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    }, trigger, eventHandler);
    await().atMost(2, SECONDS).until(eventHandlingCompleted());
    assertThat(eventHandler.isJobSucceeded(), is(false));
  }

  @Test
  public void schedulingEveryTimeAJobShouldRunThatJobAtTheExpectedTime() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(new Job(JOB_NAME) {
      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger, eventHandler);
    await().atMost(3, SECONDS).until(jobIsExecuted());
    assertThat(eventHandler.isJobSucceeded(), is(true));
  }

  @Test
  public void schedulingWithDayOfMonthAndDayOfWeekBothSetShouldThrowAnException() throws Exception {
    try {
      String cron = "* * 24 * 3";
      JobTrigger.triggerAt(cron);
      fail("A SchedulerException should be thrown!");
    } catch (ParseException ex) {
      assertThat(
          "Support for specifying both a day-of-week AND a day-of-month parameter is not " +
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
  }


  /**
   * Is a job was fired at a given time?
   * @return true if a job was fired.
   */
  private Callable<Boolean> jobIsFired() {
    return () -> eventHandler.isJobFired();
  }

  /**
   * Is a job was executed at a given time?
   * @return true if a job was executed.
   */
  private Callable<Boolean> jobIsExecuted() {
    return () -> isJobExecuted();
  }

  /**
   * Is the event handler completed its treatment?
   * @return true if the event handler completes its treatment on the event fired by the scheduler.
   */
  private Callable<Boolean> eventHandlingCompleted() {
    return () -> eventHandler.isCompleted();
  }

  /**
   * Sets the job as executed.
   */
  protected void jobExecuted() {
    isJobExecuted = true;
  }

  /**
   * Is the job executed?
   * @return
   */
  protected boolean isJobExecuted() {
    return isJobExecuted;
  }

  /**
   * Schedules a job under the specified name.
   * This method is dedicated for fixture preparations. If the job scheduling throw an exception,
   * then the fixture will fail.
   * @param jobName the name of the job to schedule.
   */
  protected void scheduleAJob(final String jobName) {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    try {
      ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
      assertNotNull(job);
    } catch (SchedulerException ex) {
      fail(ex.getMessage());
    }
  }
}
