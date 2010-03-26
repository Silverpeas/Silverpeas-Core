/**
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

import java.util.ArrayList;
import java.util.List;

import com.silverpeas.importExport.control.ImportExport;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ExportPDFReport;
import com.silverpeas.importExport.report.ExportReport;
import com.silverpeas.importExport.report.ImportReport;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.util.WAAttributeValuePair;

/**
 * @author neysseri
 */
public class ImportExportSessionController extends AbstractComponentSessionController {

  ExportThread m_theThread = null;
  Exception m_ErrorOccured = null;
  ExportReport m_ExportReport = null;

  public ImportExportSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle, String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
  }

  public ImportReport processImport(String xmlFileName,
      ResourcesWrapper resource) throws ImportExportException {
    ImportReport importReport = null;
    ImportExport importExport = new ImportExport();

    importReport = importExport.processImport(getUserDetail(), xmlFileName);
    importExport.writeImportToLog(importReport, resource);

    return importReport;
  }

  /**
   * @param itemsToExport a List of WAAttributeValuePair contains ids of elements to export
   * (objectId and instanceId)
   * @throws ImportExportException
   */
  public void processExport(String language, List<WAAttributeValuePair> itemsToExport, String rootId)
      throws ImportExportException {
    SilverTrace.info("importExportPeas", "ImportExportSessionController.processExport()",
        "root.MSG_GEN_ENTER_METHOD");
    if (m_theThread == null) {
      m_theThread = new ExportXMLThread(this, itemsToExport, language, rootId);
      m_ErrorOccured = null;
      m_ExportReport = null;
      m_theThread.startTheThread();
      SilverTrace.info("importExportPeas", "ImportExportSessionController.processExport()",
          "root.MSG_GEN_PARAM_VALUE", "------------THREAD EXPORT LANCE-----------");
    } else {
      SilverTrace.info("importExportPeas", "ImportExportSessionController.processExport()",
          "root.MSG_GEN_PARAM_VALUE", "------------!!!! EXPORT : DEUXIEME APPEL !!!!!-----------");
    }
  }

  public boolean isExportInProgress() {
    if (m_theThread == null) {
      return false;
    } else {
      return m_theThread.isEnCours();
    }
  }

  public Exception getErrorOccured() {
    return m_ErrorOccured;
  }

  public ExportReport getExportReport() {
    if (m_ErrorOccured != null) {
      return new ExportReport();
    } else {
      return m_ExportReport;
    }
  }

  public void threadFinished() {
    m_ErrorOccured = m_theThread.getErrorOccured();
    m_ExportReport = m_theThread.getReport();
    m_theThread = null;
  }

  /**
   * Export Pdf attachements of selected publications to a unique PDF. Useful for a single print or
   * download.
   * @param language : language
   * @param itemsToExport : List<WAAttributeValuePair> contains ids of elements to export (objectId
   * and instanceId)
   * @param rootId :
   * @return
   * @throws ImportExportException
   */
  public ExportPDFReport processExportPDF(String language,
      List<WAAttributeValuePair> itemsToExport,
      String rootId) throws ImportExportException {
    ImportExport importExport = new ImportExport();

    ExportPDFReport report = importExport.processExportPDF(getUserDetail(),
        language, itemsToExport, rootId);

    return report;
  }

  /**
   * @param itemsToExport a List of WAAttributeValuePair contains ids of elements to export
   * (objectId and instanceId)
   * @param Arraylist of combination (/0/1/1 ... /0/2/7)
   * @param timeCriteria
   * @throws ImportExportException
   */
  public ExportReport processExportKmax(String language, List<WAAttributeValuePair> itemsToExport,
      ArrayList combination, String timeCriteria) throws ImportExportException {
    ImportExport importExport = new ImportExport();
    ExportReport report = importExport.processExportKmax(getUserDetail(),
        language, itemsToExport, combination, timeCriteria);
    return report;
  }

}