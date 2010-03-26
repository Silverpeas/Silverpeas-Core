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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/
package com.silverpeas.importExportPeas.servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.importExport.control.GEDImportExport;
import com.silverpeas.importExport.control.ImportExportFactory;
import com.silverpeas.importExport.control.ImportExportHelper;
import com.silverpeas.importExport.control.RepositoriesTypeManager;
import com.silverpeas.importExport.report.ImportReportManager;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.pdc.importExport.PdcImportExport;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.silverpeas.versioning.importExport.VersioningImportExport;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

/**
 * Class declaration
 * @author
 */
public class ImportDragAndDrop extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("importExportPeas", "ImportDragAndDrop.init",
          "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("importExportPeas", "ImportDragAndDrop.doPost",
        "root.MSG_GEN_ENTER_METHOD");
    boolean runOnUnix = !FileUtil.isWindows();
    SilverTrace.info("importExportPeas", "Drop", "root.MSG_GEN_PARAM_VALUE",
        "runOnUnix = " + runOnUnix);

    try {
      String componentId = request.getParameter("ComponentId");
      String topicId = request.getParameter("TopicId");
      if (!StringUtil.isDefined(topicId)) {
        String sessionId = request.getParameter("SessionId");
        HttpSession session =
            SessionManager.getInstance().getUserDataSession(sessionId).getHttpSession();
        topicId = (String) session.getAttribute("Silverpeas_DragAndDrop_TopicId");
      }
      String userId = request.getParameter("UserId");
      String ignoreFolders = request.getParameter("IgnoreFolders");
      String draftMode = request.getParameter("Draft");

      SilverTrace.info("importExportPeas", "Drop", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId + " topicId = " + topicId
          + " userId = " + userId + " ignoreFolders = " + ignoreFolders
          + ", draftMode = " + draftMode);

      String savePath = FileRepositoryManager.getTemporaryPath() + "tmpupload"
          + File.separator + topicId
          + new Long(new Date().getTime()).toString() + File.separator;

      List<FileItem> items = FileUploadUtil.parseRequest(request);

      String parentPath = FileUploadUtil.getParameter(items, "userfile_parent");
      SilverTrace.info("importExportPeas", "Drop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "parentPath = " + parentPath);

      String fullFileName = null;
      SilverTrace.info("importExportPeas", "Drop.doPost",
          "root.MSG_GEN_PARAM_VALUE", "debut de la boucle");
      for (int i = 0; i < items.size(); i++) {
        FileItem item = items.get(i);
        fullFileName = item.getName();
        SilverTrace.info("importExportPeas", "Drop.doPost",
            "root.MSG_GEN_PARAM_VALUE", "item #" + i + " = "
            + item.getFieldName() + " - " + fullFileName);

        String fileName = null;
        if (fullFileName != null && parentPath != null
            && !parentPath.equals("")) {
          if (parentPath.endsWith(":\\")) // special case for file on root of
          // disk
          {
            parentPath = parentPath.substring(0, parentPath.indexOf(":") + 1);
          }

          fileName = fullFileName.substring(fullFileName.indexOf(parentPath)
              + 1 + parentPath.length());
          SilverTrace.info("importExportPeas", "Drop.doPost",
              "root.MSG_GEN_PARAM_VALUE", "fileName = " + fileName);
          if (fileName != null && runOnUnix) {
            fileName = fileName.replace('\\', File.separatorChar);
            SilverTrace.info("importExportPeas", "Drop.doPost",
                "root.MSG_GEN_PARAM_VALUE", "fileName on Unix = " + fileName);
          }

          if (fileName.indexOf(File.separator) != -1 && ignoreFolders != null
              && "1".equals(ignoreFolders)) {
            continue;
          }

          if (!savePath.equals("")) {
            File f = new File(savePath + fileName);
            File parent = f.getParentFile();
            if (!parent.exists()) {
              parent.mkdirs();
            }
            item.write(f);
          }
        }
      }
      AttachmentImportExport attachmentIE = new AttachmentImportExport();
      VersioningImportExport versioningIE = new VersioningImportExport();
      PdcImportExport pdcIE = new PdcImportExport();

      OrganizationController controller = new OrganizationController();
      UserDetail userDetail = controller.getUserDetail(userId);
      ComponentInst componentInst = controller.getComponentInst(componentId);

      // Import Report creation
      ImportReportManager.init();
      MassiveReport massiveReport = new MassiveReport();
      massiveReport.setRepositoryPath(savePath);
      ImportReportManager.addMassiveReport(massiveReport, componentId);
      GEDImportExport gedIE = ImportExportFactory.createGEDImportExport(
          userDetail, componentId);
      RepositoriesTypeManager rtm = new RepositoriesTypeManager();

      boolean isVersioningUsed = ImportExportHelper
          .isVersioningUsed(componentInst);

      boolean isDraftUsed = "1".equals(draftMode);

      boolean isPOIUsed = true;
      try {
        // Traitement recursif specifique
        rtm.processImportRecursiveReplicate(massiveReport, userDetail,
            new File(savePath), gedIE, attachmentIE, versioningIE, pdcIE,
            componentId, Integer.parseInt(topicId), isPOIUsed,
            isVersioningUsed, isDraftUsed);
        ImportReportManager.setEndDate(new Date());
      } catch (Exception ex) {
        massiveReport
            .setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
      }
      // Delete import Folder
      FileFolderManager.deleteFolder(savePath);
    } catch (Exception e) {
      SilverTrace.debug("importExportPeas", "FileUploader.doPost",
          "root.MSG_GEN_PARAM_VALUE", e);
    }
  }

}