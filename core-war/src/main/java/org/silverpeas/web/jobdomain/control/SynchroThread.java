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

package org.silverpeas.web.jobdomain.control;

import org.silverpeas.core.thread.ManagedThreadPool;

public abstract class SynchroThread implements Runnable {
  protected final JobDomainPeasSessionController toAwake;
  protected boolean isRunning = false;
  protected Exception errorOccurred = null;
  protected String synchroReport = "";

  public SynchroThread(JobDomainPeasSessionController toAwake) {
    this.toAwake = toAwake;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public Exception getErrorOccurred() {
    return errorOccurred;
  }

  public String getSynchroReport() {
    return synchroReport;
  }

  public void startTheThread() {
    isRunning = true;
    errorOccurred = null;
    synchroReport = "";
    ManagedThreadPool.getPool().invoke(this);
  }
}
