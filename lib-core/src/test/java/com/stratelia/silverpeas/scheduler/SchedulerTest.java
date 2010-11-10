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

import java.util.Date;
import java.util.Calendar;
import com.stratelia.silverpeas.scheduler.trigger.JobTrigger;
import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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
public class SchedulerTest {

  public static final String CURRENT_SCHEDULING_SYSTEM = SimpleScheduler.class.getName();
  private MySchedulingEventHandler eventHandler = new MySchedulingEventHandler();
  private boolean isJobExecuted = false;

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
  }

  @After
  public void tearDown() {
  }

  /**
   * An empty test just to check the setting up of the fixture is ok.
   */
  @Test
  public void emptyTest() {
    assertTrue(true);
  }

  //@Test
  public void theDefaultImplementationShouldBeSimpleScheduler() {
//    Scheduler currentScheduler = SchedulerFactory.getScheduler();
//    assertEquals(CURRENT_SCHEDULING_SYSTEM, currentScheduler);
  }

  //@Test
  public void schedulingEveryMinuteAJobExecutionShouldSendAnExecutionEventAtExpectedTime()
      throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.MINUTE);
    SchedulerJob job = SimpleScheduler.scheduleJob("test", trigger, eventHandler);
    assertNotNull(job);
    assertEquals("test", job.getJobName());
    assertEquals(eventHandler, job.getOwner());
    await().atMost(1, MINUTES).until(jobIsFired());
  }

  //@Test
  public void schedulingEveryMinutesAJobShouldRunThatJobAtTheExpectedTime() throws Exception {
    JobTrigger trigger = JobTrigger.triggerEvery(1, TimeUnit.MINUTE);
    SchedulerJob job = SimpleScheduler.scheduleJob(new Job("test")    {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        isJobExecuted = true;
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals("test", job.getJobName());
    assertEquals(eventHandler, job.getOwner());
    await().atMost(65, SECONDS).until(jobIsExecuted());
  }

  //@Test
  public void schedulingAJobExecutionWithACronExpressionShouldRunThatJobAtTheExpectedTime() throws
      Exception {
    Calendar calendar = Calendar.getInstance();
    int minute = calendar.get(Calendar.MINUTE);
    String cron = (minute + 1) + " * * * * ";
    JobTrigger trigger = JobTrigger.triggerAt(cron);
    SchedulerJob job = SimpleScheduler.scheduleJob(new Job("test")    {

      @Override
      public void execute(JobExecutionContext context) throws Exception {
        isJobExecuted = true;
      }
    }, trigger, eventHandler);
    assertNotNull(job);
    assertEquals("test", job.getJobName());
    assertEquals(eventHandler, job.getOwner());
    await().atMost(1, MINUTES).until(jobIsExecuted());
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
