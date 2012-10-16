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

package com.silverpeas.importExport.control;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.importExport.model.ImportExportException;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MassiveDocumentImport {
  private OrganizationController controller = new OrganizationController();

  public List<PublicationDetail> importDocuments(ComponentSessionController sessionController,
      String directory, int topicId, boolean draftMode, boolean isPOIUsed)
      throws ImportExportException {
    return importDocuments(sessionController.getUserDetail(), sessionController.getComponentId(),
        directory, topicId, draftMode, isPOIUsed, new MassiveReport());
  }

  public List<PublicationDetail> importDocuments(UserDetail userDetail, String componentId,
      String directory, int topicId, boolean draftMode, boolean isPOIUsed,
      MassiveReport massiveReport)
      throws ImportExportException {
    List<PublicationDetail> publicationDetails = new ArrayList<PublicationDetail>();
    try {
      AttachmentImportExport attachmentIE = new AttachmentImportExport();
      VersioningImportExport versioningIE = new VersioningImportExport();
      PdcImportExport pdcIE = new PdcImportExport();
      ImportReportManager.init();

      massiveReport.setRepositoryPath(directory);
      ImportReportManager.addMassiveReport(massiveReport, componentId);
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(userDetail, componentId);
      RepositoriesTypeManager rtm = new RepositoriesTypeManager();
      publicationDetails = rtm.processImportRecursiveReplicate(massiveReport, userDetail, new File(
          directory), gedIE, attachmentIE, versioningIE, pdcIE, componentId, topicId,
          isPOIUsed, isVersioningUsed(componentId), draftMode);
      ImportReportManager.setEndDate(new Date());

    } finally {
      FileFolderManager.deleteFolder(directory);
    }
    return publicationDetails;
  }

  private boolean isVersioningUsed(String componentId) {

    ComponentInst componentInst = controller.getComponentInst(componentId);
    return ImportExportHelper.isVersioningUsed(componentInst);

  }
}
