/**
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
 * "http://repository.silverpeas.com/legal/licensing"
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
 * The class 'SchedulerEvent' holds the event information, if there is a event fired by the
 * scheduler. Only events of the type 'EXECUTION.
 */
public class SchedulerEvent {
  /**
   * Event Type
   */
  public static final int EXECUTION_SUCCESSFULL = 1;

  /**
   * Event Type
   */
  public static final int EXECUTION_NOT_SUCCESSFULL = 2;

  /**
   * Event Type
   */
  public static final int EXECUTION = 3;

  private SchedulerJob theJob;
  private int theType;

  /**
   * This method returns the job
   * @return The job, which is the source for the event
   */
  public SchedulerJob getJob() {
    return theJob;
  }

  /**
   * This method returns the event type
   * @return The type of the event
   */
  public int getType() {
    return theType;
  }

  /**
   * The constructor has proteceted access, because the generation of events should be done
   * internally
   * @param aType The type of the event
   * @param aJob The job, which is the source for the event
   */
  protected SchedulerEvent(int aType, SchedulerJob aJob) {
    theType = aType;
    theJob = aJob;
  }

}
