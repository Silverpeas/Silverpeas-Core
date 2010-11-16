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

/**
 * Defines a scheduling system with which Silverpeas components can schedule jobs at given moments
 * in time and in a periodic way. The scheduling system delegates the actual scheduling tasks to
 * an underlying scheduling backend; it encapsulates the implementation used for scheduling jobs and
 * the way they are scheduled.
 * 
 * Provides a <code>Scheduler</code> interface with which components schedules their jobs in time.
 * The <code>Scheduler</code> represents a single entry point to the scheduling system and,
 * whatever the <code>Scheduler</code> object is used, a job registered with one scheduler can be
 * accessed by another one. The only way to get a scheduler is to use a <code>SchedulerFactory</code>
 * instance that is built upon the actual scheduling backend.
 */
package com.stratelia.silverpeas.scheduler;
