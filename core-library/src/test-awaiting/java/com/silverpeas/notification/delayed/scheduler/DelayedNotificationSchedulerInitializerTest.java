package com.silverpeas.usernotification.delayed.scheduler;

import org.silverpeas.core.scheduler.Scheduler;
import javax.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
