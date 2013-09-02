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

import java.util.List;

/**
 * @author neysseri
 */
public class ImportExportSessionController extends AbstractComponentSessionController {

  ExportThread exportThread = null;
  Exception errorOccured = null;
  ExportReport exportReport = null;

  private List<WAAttributeValuePair> items = null;
  private String rootId = null;

  public ImportExportSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle, String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
  }

  public ImportReport processImport(String xmlFileName,
      ResourcesWrapper resource) throws ImportExportException {
    ImportExport importExport = new ImportExport();
    ImportReport importReport = importExport.processImport(getUserDetail(), xmlFileName);
    importExport.writeImportToLog(importReport, resource);
    return importReport;
  }

  /**
   * @param language
   * @param itemsToExport a List of WAAttributeValuePair contains ids of elements to export
   * (objectId and instanceId)
   * @param rootId
   * @throws ImportExportException
   */
  public void processExport(List<WAAttributeValuePair> itemsToExport, String rootId)
      throws ImportExportException {
    processExport(itemsToExport, rootId, ImportExport.EXPORT_FULL);
  }

  public void processExport(List<WAAttributeValuePair> itemsToExport, String rootId, int mode)
      throws ImportExportException {
    SilverTrace.info("importExportPeas", "ImportExportSessionController.processExport()",
        "root.MSG_GEN_ENTER_METHOD");
    if (exportThread == null) {
      exportThread = new ExportXMLThread(this, itemsToExport, getLanguage(), rootId, mode);
      errorOccured = null;
      exportReport = null;
      exportThread.startTheThread();
      SilverTrace.info("importExportPeas", "ImportExportSessionController.processExport()",
          "root.MSG_GEN_PARAM_VALUE", "------------THREAD EXPORT LANCE-----------");
    } else {
      SilverTrace.info("importExportPeas", "ImportExportSessionController.processExport()",
          "root.MSG_GEN_PARAM_VALUE", "------------!!!! EXPORT : DEUXIEME APPEL !!!!!-----------");
    }
  }

  public boolean isExportInProgress() {
    if (exportThread == null) {
      return false;
    }
    return exportThread.isEnCours();
  }

  public Exception getErrorOccured() {
    return errorOccured;
  }

  public ExportReport getExportReport() {
    if (errorOccured != null) {
      return new ExportReport(errorOccured);
    }
    return exportReport;
  }

  public void threadFinished() {
    errorOccured = exportThread.getErrorOccured();
    exportReport = exportThread.getReport();
    exportThread = null;
  }

  /**
   * Export Pdf attachements of selected publications to a unique PDF. Useful for a single print or
   * download.
   * @param itemsToExport : List<WAAttributeValuePair> contains ids of elements to export (objectId
   * and instanceId)
   * @return
   * @throws ImportExportException
   */
  public ExportPDFReport processExportPDF(List<WAAttributeValuePair> itemsToExport)
      throws ImportExportException {
    ImportExport importExport = new ImportExport();

    return importExport.processExportPDF(getUserDetail(), itemsToExport);
  }

  /**
   * @param language
   * @param itemsToExport a List of WAAttributeValuePair contains ids of elements to export
   * (objectId and instanceId)
   * @param combination
   * @param timeCriteria
   * @return
   * @throws ImportExportException
   */
  public ExportReport processExportKmax(String language, List<WAAttributeValuePair> itemsToExport,
      List combination, String timeCriteria) throws ImportExportException {
    ImportExport importExport = new ImportExport();
    ExportReport report = importExport.processExportKmax(getUserDetail(),
        language, itemsToExport, combination, timeCriteria);
    return report;
  }

  public void processExportOfSavedItems(String mode) throws ImportExportException {
    processExport(this.items, this.rootId, Integer.parseInt(mode));
  }

  public void saveItems(List<WAAttributeValuePair> items, String rootId) {
    this.items = items;
    this.rootId = rootId;
  }

  public void clearItems() {
    this.items = null;
    this.rootId = null;
  }

}