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

package com.silverpeas.importExport.report;

import java.util.Date;
import java.util.HashMap;

public final class ImportReportManager {
  private static ImportReport importReport = null;
  private static HashMap<String, ComponentReport> componentReportMap = null;

  private ImportReportManager() {
  }

  public static void init() {
    importReport = new ImportReport();
    componentReportMap = new HashMap<String, ComponentReport>();
    importReport.setStartDate(new Date());
  }

  public static ComponentReport getComponentReport(String componentId) {
    ComponentReport componentReport = componentReportMap.get(componentId);
    if (componentReport == null) {
      componentReport = new ComponentReport(componentId);
      componentReportMap.put(componentId, componentReport);
      importReport.addComponentReport(componentReport);
    }

    return componentReport;
  }

  public static void setComponentName(String componentId, String componentName) {
    getComponentReport(componentId).setComponentName(componentName);
  }

  public static void addMassiveReport(MassiveReport massiveReport,
      String componentId) {
    getComponentReport(componentId).addMassiveReport(massiveReport);
  }

  public static void addUnitReport(UnitReport unitReport, String componentId) {
    getComponentReport(componentId).addUnitReport(unitReport);
  }

  public static void addImportedFileSize(long size, String componentId) {
    getComponentReport(componentId).addImportedFileSize(size);
  }

  public static ImportReport getImportReport() {
    return importReport;
  }

  public static void setEndDate(Date date) {
    importReport.setEndDate(date);
  }

  public static void addNumberOfFilesProcessed(int n) {
    importReport.addNumberOfFilesProcessed(n);
  }

  public static void addNumberOfFilesNotImported(int n) {
    importReport.addNbFilesNotImported(n);
  }
}