/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport.report;

import java.util.Date;
import java.util.HashMap;

/**
 * The singleton ImportReportManager
 */
public class ImportReportManager {

  private static ImportReport importReport = null;
  private static HashMap<String, ComponentReport> componentReportMap = null;

  /**
   * Creates an import report manager for a given import. At construction of this manager instance,
   * the date at which the import starts is set automatically at now.
   */
  public ImportReportManager() {
    importReport = new ImportReport();
    componentReportMap = new HashMap<>();
    importReport.setStartDate(new Date());
  }

  public ComponentReport getComponentReport(String componentId) {
    ComponentReport componentReport = componentReportMap.get(componentId);
    if (componentReport == null) {
      componentReport = new ComponentReport(componentId);
      componentReportMap.put(componentId, componentReport);
      importReport.addComponentReport(componentReport);
    }

    return componentReport;
  }

  public void setComponentName(String componentId, String componentName) {
    getComponentReport(componentId).setComponentName(componentName);
  }

  public void addMassiveReport(MassiveReport massiveReport,
      String componentId) {
    getComponentReport(componentId).addMassiveReport(massiveReport);
  }

  public void addUnitReport(UnitReport unitReport, String componentId) {
    getComponentReport(componentId).addUnitReport(unitReport);
  }

  public void addImportedFileSize(long size, String componentId) {
    getComponentReport(componentId).addImportedFileSize(size);
  }

  public ImportReport getImportReport() {
    return importReport;
  }

  /**
   * Indicates the end of the import. The end date of the import is then reported.
   */
  public void reportImportEnd() {
    importReport.setEndDate(new Date());
  }

  public void addNumberOfFilesProcessed(int n) {
    importReport.addNumberOfFilesProcessed(n);
  }

  public void addNumberOfFilesNotImported(int n) {
    importReport.addNbFilesNotImported(n);
  }
}
