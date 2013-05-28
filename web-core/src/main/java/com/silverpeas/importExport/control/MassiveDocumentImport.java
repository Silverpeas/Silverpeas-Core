/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.importExport.control;

import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import org.silverpeas.importExport.attachment.AttachmentImportExport;
import org.silverpeas.importExport.versioning.VersioningImportExport;
import org.silverpeas.core.admin.OrganisationControllerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MassiveDocumentImport {

  public List<PublicationDetail> importDocuments(ImportSettings importSettings,
      MassiveReport massiveReport) throws ImportExportException {
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    try {
      AttachmentImportExport attachmentIE = new AttachmentImportExport();
      VersioningImportExport versioningIE = new VersioningImportExport(importSettings.getUser());
      PdcImportExport pdcIE = new PdcImportExport();
      ImportReportManager.init();

      massiveReport.setRepositoryPath(importSettings.getPathToImport());
      ImportReportManager.addMassiveReport(massiveReport, importSettings.getComponentId());
      GEDImportExport gedIE =
          ImportExportFactory.createGEDImportExport(importSettings.getUser(),
              importSettings.getComponentId());
      RepositoriesTypeManager rtm = new RepositoriesTypeManager();
      importSettings.setVersioningUsed(isVersioningUsed(importSettings.getComponentId()));
      publicationDetails =
          rtm.processImportRecursiveReplicate(massiveReport, gedIE, attachmentIE, versioningIE,
              pdcIE, importSettings);
      ImportReportManager.setEndDate(new Date());

    } finally {
      FileFolderManager.deleteFolder(importSettings.getPathToImport());
    }
    return publicationDetails;
  }

  private boolean isVersioningUsed(String componentId) {
    ComponentInst componentInst =
        OrganisationControllerFactory.getOrganisationController().getComponentInst(componentId);
    return ImportExportHelper.isVersioningUsed(componentInst);

  }
}
