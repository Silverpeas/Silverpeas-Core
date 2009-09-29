/* SimpleScheduler (a small library for scheduling jobs)
   Copyright (C) 2001  Thomas Breitkreuz

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

	Thomas Breitkreuz (tb@cdoc.de)
 */

package com.stratelia.silverpeas.scheduler;

/**
 * The class 'SchedulerEvent' holds the event information, if there is a event
 * fired by the scheduler. Only events of the type 'EXECUTION.
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
   * 
   * @return The job, which is the source for the event
   */
  public SchedulerJob getJob() {
    return theJob;
  }

  /**
   * This method returns the event type
   * 
   * @return The type of the event
   */
  public int getType() {
    return theType;
  }

  /**
   * The constructor has proteceted access, because the generation of events
   * should be done internally
   * 
   * @param aType
   *          The type of the event
   * @param aJob
   *          The job, which is the source for the event
   */
  protected SchedulerEvent(int aType, SchedulerJob aJob) {
    theType = aType;
    theJob = aJob;
  }

}
