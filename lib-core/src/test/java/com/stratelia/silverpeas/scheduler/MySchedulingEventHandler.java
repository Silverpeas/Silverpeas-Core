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

/**
 * Handler of scheduling events for testing purpose.
 * It is a stub dedicated to tests.
 */
public class MySchedulingEventHandler implements SchedulerEventHandler {
  
  private boolean executed = false;
  private boolean succeeded = false;

  @Override
  public void handleSchedulerEvent(SchedulerEvent aEvent) {
    switch(aEvent.getType()) {
      case SchedulerEvent.EXECUTION:
        executed = true;
        break;
      case SchedulerEvent.EXECUTION_SUCCESSFULL:
        succeeded = true;
        break;
      case SchedulerEvent.EXECUTION_NOT_SUCCESSFULL:
        succeeded = false;
        break;
    }
  }
  
  /**
   * Resets the counters.
   */
  public void reset() {
    executed = false;
    succeeded = false;
  }
  
  /**
   * Is a job fired?
   * @return true if an event about a job firing was recieved, false otherwise.
   */
  public boolean jobFired() {
    return executed;
  }
  
  /**
   * Is a job execution succeeded?
   * @return true if the execution of a job was done correctly, false otherwise. If no job was
   * fired, returns by default false.
   */
  public boolean jobSucceeded() {
    return succeeded;
  }
}
