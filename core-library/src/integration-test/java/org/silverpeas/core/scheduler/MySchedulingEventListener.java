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

import static org.junit.Assert.*;

/**
 * Handler of scheduling events for testing purpose.
 * It is a stub dedicated to tests.
 */
public class MySchedulingEventListener implements SchedulerEventListener {

  private boolean fired = false;
  private boolean succeeded = false;
  private boolean mustFail = false;
  private boolean completed = false;

  /**
   * The processing of a scheduler event about a trigger firing must throw an error.
   * @return the event listener itself.
   */
  public MySchedulingEventListener mustFail() {
    mustFail = true;
    return this;
  }

  /**
   * Resets the counters.
   */
  public void reset() {
    fired = false;
    succeeded = false;
    mustFail = false;
  }

  /**
   * Is a job fired?
   * @return true if an event about a job firing was recieved, false otherwise.
   */
  public boolean isJobFired() {
    return fired;
  }

  /**
   * Is a job execution succeeded?
   * @return true if the execution of a job was done correctly, false otherwise. If no job was
   * fired, returns by default false.
   */
  public boolean isJobSucceeded() {
    return succeeded;
  }

  /**
   * Is the treatment of this listener about the job is completed? The treatment is done once the
   * job performed and the state of its execution treated by this listener.
   * @return true if the listener has finished to treat the different events triggered from the
   * scheduler.
   */
  public boolean isCompleted() {
    return completed;
  }

  @Override
  public void triggerFired(SchedulerEvent anEvent) {
    try {
      assertSchedulerEvent(anEvent);
      if (mustFail) {
        throw new UnsupportedOperationException();
      }
    } finally {
      fired = true;
    }
  }

  @Override
  public void jobSucceeded(SchedulerEvent anEvent) {
    assertSchedulerEvent(anEvent);
    succeeded = true;
    completed = true;
  }

  @Override
  public void jobFailed(SchedulerEvent anEvent) {
    assertSchedulerEvent(anEvent);
    assertNotNull(anEvent.getJobThrowable());
    succeeded = false;
    completed = true;
  }

  /**
   * Asserts the scheduler event is correctly set.
   * @param anEvent the event to check.
   */
  private void assertSchedulerEvent(final SchedulerEvent anEvent) {
    assertNotNull(anEvent);
    assertNotNull(anEvent.getJobExecutionContext());
    assertNotNull(anEvent.getJobExecutionContext().getJobName());
    assertNotNull(anEvent.getJobExecutionContext().getFireTime());
  }
}
