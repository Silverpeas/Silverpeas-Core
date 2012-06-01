package com.silverpeas.notification.delayed.scheduler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.silverpeas.scheduler.Scheduler;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring-delayed-notification-scheduler.xml")
public class DelayedNotificationSchedulerInitializerTest {

  @Inject
  private Scheduler scheduler;

  @Test
  public void test() {
    assertThat(scheduler.isJobScheduled(DelayedNotificationSchedulerInitializer.JOB_NAME), is(true));
  }
}
