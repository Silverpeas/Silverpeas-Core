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
package org.silverpeas.web.importexport.control;

import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.cache.service.SessionCacheService;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.thread.ManagedThreadPool;

public abstract class ExportTask implements Runnable {
  final ImportExportSessionController toAwake;
  private final Object mutex = new Object();
  Exception errorOccurred = null;
  ExportReport exportReport = null;
  private boolean isRunning = false;

  ExportTask(ImportExportSessionController toAwake) {
    super();
    this.toAwake = toAwake;
  }

  public boolean isRunning() {
    synchronized (mutex) {
      return isRunning;
    }
  }

  void markAsEnded() {
    synchronized (mutex) {
      isRunning = false;
      toAwake.threadFinished();
    }
  }

  public Exception getErrorOccurred() {
    return errorOccurred;
  }

  public ExportReport getReport() {
    return exportReport;
  }

  public void startTheExport() {
    isRunning = true;
    errorOccurred = null;
    exportReport = null;
    ManagedThreadPool.getPool().invoke(this);
  }

  @Override
  public final void run() {
    CacheServiceProvider.clearAllThreadCaches();
    ((SessionCacheService) CacheServiceProvider.getSessionCacheService())
        .newSessionCache(this.toAwake.getUserDetail());
    doExport();
  }

  protected abstract void doExport();
}
