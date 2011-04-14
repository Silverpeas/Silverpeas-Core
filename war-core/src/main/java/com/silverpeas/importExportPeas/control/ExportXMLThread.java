/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.importExportPeas.control;

import java.util.List;

import com.silverpeas.importExport.control.ImportExport;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.WAAttributeValuePair;

public class ExportXMLThread extends ExportThread {

  private final List<WAAttributeValuePair> pksToExport;
  private final String language;
  private final String rootId;

  public ExportXMLThread(ImportExportSessionController toAwake, List<WAAttributeValuePair> pks,
      String language, String rootId) {
    super(toAwake);
    pksToExport = pks;
    this.language = language;
    this.rootId = rootId;
  }

  @Override
  public void run() {
    SilverTrace.info("importExportPeas", "ExportXMLThread.run", "root.MSG_GEN_PARAM_VALUE",
        "------------DEBUT DU THREAD D'EXPORT-----------");
    try {
      ImportExport importExport = new ImportExport();
      m_ExportReport = importExport.processExport(super.m_toAwake.getUserDetail(), language, 
          pksToExport, rootId);
      SilverTrace.info("importExportPeas", "ExportXMLThread.run", "root.MSG_GEN_PARAM_VALUE",
          "------------TOUT EST OK-----------");
      m_isEncours = false;
      m_toAwake.threadFinished();
      SilverTrace.info("importExportPeas", "ExportXMLThread.run", "root.MSG_GEN_PARAM_VALUE",
          "------------AFTER NOTIFY-----------");
    } catch (Exception e) {
      m_ErrorOccured = e;
      m_isEncours = false;
      m_toAwake.threadFinished();
      SilverTrace.info("importExportPeas", "ExportXMLThread.run", "root.MSG_GEN_PARAM_VALUE",
          "------------ERREUR-----------", e);
    }
  }
}
