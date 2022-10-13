/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler;

import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;

/**
 * Execution context of a job. Because only the class name of the job is persisted with the
 * scheduler, it cannot be possible to have a stateful job. So we delegate this state into
 * another class that is managed by IoC.
 * @author mmoquillon
 */
@Singleton
public class TestJobExecutionContext {

  private boolean jobExecuted;

  public static final TestJobExecutionContext get() {
    return ServiceProvider.getService(TestJobExecutionContext.class);
  }

  public void done() {
    this.jobExecuted = true;
  }

  public boolean isJobExecuted() {
    return jobExecuted;
  }
}
  