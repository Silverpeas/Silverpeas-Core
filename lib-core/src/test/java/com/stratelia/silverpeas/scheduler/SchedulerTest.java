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
package com.stratelia.silverpeas.scheduler;

import com.stratelia.silverpeas.scheduler.trigger.TimeUnit;
import java.util.Date;
import java.util.Calendar;
import com.stratelia.silverpeas.scheduler.trigger.JobTrigger;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/spring-scheduling.xml")
public class SchedulerTest {

  private static final String JOB_NAME = "test";
  private MySchedulingEventListener eventHandler = new MySchedulingEventListener();
  private boolean isJobExecuted = false;
  private Scheduler scheduler = null;

  public SchedulerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
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
  }

  @After
  public void tearDown() {
    scheduler.unscheduleJob(JOB_NAME);
  }

  /**
   * An empty test just to check the setting up of the fixture is ok.
   */
  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  @Test
  public void schedulingEveryMinuteAJobExecutionShouldSendAnExecutionEventAtExpectedTime()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEach(1, TimeUnit.MINUTE);
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(65, SECONDS).until(jobIsFired());
  }

  @Test
  public void schedulingEveryMinutesAJobShouldRunThatJobAtTheExpectedTime() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEach(1, TimeUnit.MINUTE);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME)    {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        isJobExecuted = true;
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(65, SECONDS).until(jobIsExecuted());
  }

  @Test
  public void schedulingAJobWithACronExpressionShouldRunThatJobAtTheExpectedTime() throws
      Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    int minute = calendar.get(Calendar.MINUTE);
    String cron = minute + " * * * * ";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    ScheduledJob job = scheduler.scheduleJob(new Job(JOB_NAME)    {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        isJobExecuted = true;
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(65, SECONDS).until(jobIsExecuted());
  }
  
  @Test
  public void schedulingAJobExecutionWithACronExpressionShouldRunThatJobAtTheExpectedTime() throws
      Exception {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 1);
    int minute = calendar.get(Calendar.MINUTE);
    String cron = minute + " * * * * ";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    ScheduledJob job = scheduler.scheduleJob(JOB_NAME, trigger, eventHandler);
    assertNotNull(job);
    assertEquals(JOB_NAME, job.getName());
    assertEquals(eventHandler, job.getSchedulerEventListener());
    assertEquals(trigger, job.getTrigger());
    await().atMost(65, SECONDS).until(jobIsFired());
  }

  /**
   * Is a job was fired at a given time?
   * @return true if a job was fired.
   */
  private Callable<Boolean> jobIsFired() {
    return new Callable<Boolean>()     {

      @Override
      public Boolean call() throws Exception {
        return eventHandler.jobFired();
      }
    };
  }

  /**
   * Is a job was executed at a given time?
   * @return true if a job was executed.
   */
  private Callable<Boolean> jobIsExecuted() {
    return new Callable<Boolean>()     {

      @Override
      public Boolean call() throws Exception {
        return isJobExecuted;
      }
    };
  }

  public void execute(Date date) {
    isJobExecuted = true;
  }
}
