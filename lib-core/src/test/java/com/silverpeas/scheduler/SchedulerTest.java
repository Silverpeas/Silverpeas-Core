/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.scheduler;

import com.silverpeas.scheduler.trigger.TimeUnit;
import java.util.Calendar;
import com.silverpeas.scheduler.trigger.JobTrigger;
import java.text.ParseException;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import static org.junit.Assert.*;
import static com.jayway.awaitility.Awaitility.*;
import static java.util.concurrent.TimeUnit.*;

/**
 * The scheduling system is backed by an interface, Scheduler, and a factory on implementation of
 * this interface, SchedulerFactory.
 * The factory hides the concrete underlying scheduling system in use by Silverpeas and provides a
 * a single access to it through the Scheduler interface.
 * Silverpeas scheduling API provides its own interface to manage scheduled jobs that are eaten by
 * a scheduler and keeps actually a backward compability with its old previous API.
 * This test checks the current concrete scheduler is ok.
 * @author mmoquillon
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = "/spring-scheduling.xml")
public class SchedulerTest {

  private static final String JOB_NAME = "test";
  private MySchedulingEventListener eventHandler;
  private boolean isJobExecuted;
  private Scheduler scheduler = null;
  private long time;

  public SchedulerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    ApplicationContext context = new ClassPathXmlApplicationContext("/spring-scheduling.xml");
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
    SchedulerFactory schedulerFactory = SchedulerFactory.getFactory();
    assertNotNull(schedulerFactory);
    scheduler = schedulerFactory.getScheduler();
    assertNotNull(scheduler);
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
  public void emptyTest() {
    assertTrue(true);
  }

  @Test(expected=SchedulerException.class)
  public void schedulingAnAlreadyScheduledJobShouldThrowASchedulerException() throws Exception {
    scheduleAJob(JOB_NAME);
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
  }

  @Test(expected=SchedulerException.class)
  public void schedulingAnAlreadyScheduledJobExecutionShouldThrowASchedulerException() throws
      Exception {
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
  public void schedulingEveryMinuteAJobExecutionShouldSendAnExecutionEventAtExpectedTime()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(2, SECONDS).until(jobIsFired());
    assertTrue(eventHandler.isJobSucceeded());
  }

  @Test
  public void aFailureJobExecutionShouldFireACorrespondingSchedulerEvent()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler.mustFail());
    await().atMost(2, SECONDS).until(jobIsFired());
    assertFalse(eventHandler.isJobSucceeded());
  }

  @Test
  public void schedulingAJobWithoutEventListenerShouldRunThatJobAtTheExpectedTime() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertNull(job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(2, SECONDS).until(jobIsExecuted());
  }

  @Test
  public void aFailureJobShouldFireACorrespondingSchedulerEvent()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        throw new Error("Not supported yet.");
      }
    }, trigger, eventHandler);
    await().atMost(2, SECONDS).until(jobIsFired());
    assertFalse(eventHandler.isJobSucceeded());
  }

  @Test
  public void schedulingEveryMinutesAJobShouldRunThatJobAtTheExpectedTime() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.SECOND);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(2, SECONDS).until(jobIsExecuted());
    assertTrue(eventHandler.isJobSucceeded());
  }

  @Test
  public void schedulingAJobWithACronExpressionShouldRunThatJobAtTheExpectedTime() throws
      Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    int minute = calendar.get(Calendar.MINUTE);
    String cron = minute + " * * * * ";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        jobExecuted();
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(10, MINUTES).until(jobIsExecuted());
    assertTrue(eventHandler.isJobSucceeded());
  }

  @Test
  public void schedulingAJobWithACronExpressionShouldRunThatJobOnlyOnceAtTheExpectedTime() throws
      Exception {
    time = System.currentTimeMillis();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    int minute = calendar.get(Calendar.MINUTE);
    String cron = minute + " * * * * ";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME) {

      private int counter = 0;

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        assertEquals(1, ++counter);
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(5, MINUTES).until(timeThresholdIsDone());
    assertTrue(eventHandler.isJobSucceeded());
  }

  @Test
  public void schedulingAJobExecutionWithACronExpressionShouldRunThatJobAtTheExpectedTime() throws
      Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    int minute = calendar.get(Calendar.MINUTE);
    String cron = minute + " * * * *";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(1, MINUTES).until(jobIsFired());
    assertTrue(eventHandler.isJobSucceeded());
  }

  @Test
  public void schedulingWithDayOfMonthAndDayOfWeekBothSetShouldThrowAnException() throws Exception {
    try {
      String cron = "* * 24 * 3";
      JobTrigger trigger = JobTrigger.triggerAt(cron);
      fail("A SchedulerException should be thrown!");
    } catch (ParseException ex) {
      assertEquals("Support for specifying both a day-of-week AND a day-of-month parameter is not "
          + "implemented.", ex.getMessage());
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
    assertTrue(scheduler.isJobScheduled(JOB_NAME));
  }

  @Test
  public void aNonScheduledJobShouldBeNotFound() throws Exception {
    assertFalse(scheduler.isJobScheduled(JOB_NAME));
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
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return eventHandler.isJobFired();
      }
    };
  }

  /**
   * Is a job was executed at a given time?
   * @return true if a job was executed.
   */
  private Callable<Boolean> jobIsExecuted() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return isJobExecuted();
      }
    };
  }

  /**
   * Is a job was executed at a given time?
   * @return true if a job was executed.
   */
  private Callable<Boolean> timeThresholdIsDone() {
    return new Callable<Boolean>() {

      @Override
      public Boolean call() throws Exception {
        return (System.currentTimeMillis() - time) >=  180000;
      }
    };
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
