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
 * This exception is thrown by the scheduler framework. Could be used for
 * exception classification.
 */
public class SchedulerException extends Exception {
  public SchedulerException() {
    super();
  }

  /**
   * @param aMessage
   *          The exception message
   */
  public SchedulerException(String aMessage) {
    super(aMessage);
  }
}
