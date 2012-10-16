/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.importExportPeas.control;

import com.silverpeas.importExport.report.ExportReport;

public class ExportThread extends Thread {
  protected ImportExportSessionController m_toAwake = null;
  protected boolean m_isEncours = false;
  protected Exception m_ErrorOccured = null;
  protected ExportReport m_ExportReport = null;

  public ExportThread(ImportExportSessionController toAwake) {
    m_toAwake = toAwake;
  }

  public boolean isEnCours() {
    return m_isEncours;
  }

  public Exception getErrorOccured() {
    return m_ErrorOccured;
  }

  public ExportReport getReport() {
    return m_ExportReport;
  }

  public void startTheThread() {
    m_isEncours = true;
    m_ErrorOccured = null;
    m_ExportReport = null;
    start();
  }
}
