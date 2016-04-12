/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.importexport.control;

import org.silverpeas.core.importexport.control.ImportExport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ExportPDFReport;
import org.silverpeas.core.importexport.report.ExportReport;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.core.node.model.NodePK;

import java.util.List;

/**
 * @author neysseri
 */
public class ImportExportSessionController extends AbstractComponentSessionController {

  ExportThread exportThread = null;
  Exception errorOccured = null;
  ExportReport exportReport = null;
  ImportExport importExport = ServiceProvider.getService(ImportExport.class);

  private List<WAAttributeValuePair> items = null;
  private NodePK rootPK = null;

  public ImportExportSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle, String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
  }

  public ImportReport processImport(String xmlFileName,
      MultiSilverpeasBundle resource) throws ImportExportException {
    ImportReport importReport = importExport.processImport(getUserDetail(), xmlFileName);
    importExport.writeImportToLog(importReport, resource);
    return importReport;
  }

  public void processExport(List<WAAttributeValuePair> itemsToExport, NodePK rootPK)
      throws ImportExportException {
    processExport(itemsToExport, rootPK, ImportExport.EXPORT_FULL);
  }

  public void processExport(List<WAAttributeValuePair> itemsToExport, NodePK rootPK, int mode)
      throws ImportExportException {

    if (exportThread == null) {
      exportThread = new ExportXMLThread(this, itemsToExport, getLanguage(), rootPK, mode);
      errorOccured = null;
      exportReport = null;
      exportThread.startTheThread();

    } else {

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
    return importExport.processExportPDF(getUserDetail(), itemsToExport, rootPK);
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
    ExportReport report = importExport.processExportKmax(getUserDetail(),
        language, itemsToExport, combination, timeCriteria);
    return report;
  }

  public void processExportOfSavedItems(String mode) throws ImportExportException {
    processExport(this.items, this.rootPK, Integer.parseInt(mode));
  }

  public void saveItems(List<WAAttributeValuePair> items, NodePK rootPK) {
    this.items = items;
    this.rootPK = rootPK;
  }

  public void clearItems() {
    this.items = null;
    this.rootPK = null;
  }

}