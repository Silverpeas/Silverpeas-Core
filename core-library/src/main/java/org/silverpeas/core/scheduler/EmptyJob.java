/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler;

/**
 * Empty job. It does nothing and it is just a convienence way to schedule a job whereas the
 * true job execution is performed in fact by a {@link SchedulerEventListener} object.
 * @author mmoquillon
 */
public class EmptyJob extends Job {

  /**
   * Creates a new job with the specified name.
   * @param name the name under which the job has to be registered in the scheduler.
   */
  public EmptyJob(final String name) {
    super(name);
  }

  @Override
  public void execute(final JobExecutionContext context) {
    // does nothing.
  }
}
  