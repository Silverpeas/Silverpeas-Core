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
package org.silverpeas.core.importexport.control;

import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.ImportReportManager;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.pdc.pdc.importexport.PdcImportExport;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.file.FileFolderManager;

import javax.inject.Inject;

public class MassiveDocumentImport {

  @Inject
  private PdcImportExport pdcImportExport;
  @Inject
  private RepositoriesTypeManager repositoriesTypeManager;

  public static MassiveDocumentImport get() {
    return ServiceProvider.getService(MassiveDocumentImport.class);
  }

  protected MassiveDocumentImport() {
  }

  /**
   * @param importSettings
   * @param massiveReport
   * @return a report of the import
   * @throws ImportExportException
   */
  public ImportReport importDocuments(ImportSettings importSettings,
      MassiveReport massiveReport) throws ImportExportException {
    ImportReportManager reportManager = new ImportReportManager();
    try {
      massiveReport.setRepositoryPath(importSettings.getPathToImport());
      reportManager.addMassiveReport(massiveReport, importSettings.getComponentId());
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(importSettings.getUser(),
          importSettings.getComponentId());
      importSettings.setVersioningUsed(ImportExportHelper.isVersioningUsed(importSettings
          .getComponentId()));
      repositoriesTypeManager.processImportRecursiveReplicate(reportManager, massiveReport, gedIE,
          pdcImportExport, importSettings);
      reportManager.reportImportEnd();
    } finally {
      FileFolderManager.deleteFolder(importSettings.getPathToImport());
    }
    return reportManager.getImportReport();
  }
}
