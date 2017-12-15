/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler;

import org.silverpeas.core.SilverpeasException;

import java.io.Serializable;

/**
 * A job to schedule at a given moments in time. A job is identified in the scheduler by a name that
 * must be unique.
 * <p>
 * The job is serializable to be able to be stored into a persistence context. Only
 * stateless and non-anonymous jobs can be serialized correctly as only its class name is
 * serialized. Indeed, the job is then constructed each time it is being executed so that any
 * change in its execution logic will be taken into account. A persistent job can be managed by
 * an underlying IoC container. When fetching from the persistence context, if
 * such a job is managed by the {@link org.silverpeas.core.util.ServiceProvider}, then this
 * managed job will be used; otherwise it is constructed.
 * </p>
 * <p>
 * Any jobs scheduled by a volatile scheduler aren't constrains by the same limitations that a job
 * scheduled by a persistent scheduler: it can be a stateful or an anonymous job.
 * </p>
 */
public abstract class Job implements Serializable {

  private String name;

  /**
   * Creates a new job with the specified name.
   * @param name the name under which the job has to be registered in the scheduler.
   */
  public Job(final String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("The job name is required!");
    }
    this.name = name.trim();
  }

  /**
   * Gets the name under which this job should be scheduled.
   * @return the job name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Executes the job with the specified execution context. The context carries the information that
   * can be required by the job to fulfill its execution, like the job parameters.
   * @param context the context under which this job is executed.
   * @throws SilverpeasException if an error occurs during the job execution.
   */
  public abstract void execute(final JobExecutionContext context) throws SilverpeasException;

}
