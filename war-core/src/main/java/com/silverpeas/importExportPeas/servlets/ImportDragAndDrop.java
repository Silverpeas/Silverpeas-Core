/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.importExportPeas.servlets;

import com.silverpeas.importExport.control.ImportSettings;
import com.silverpeas.importExport.control.MassiveDocumentImport;
import com.silverpeas.importExport.report.ComponentReport;
import com.silverpeas.importExport.report.ImportReport;
import com.silverpeas.importExport.report.MassiveReport;
import com.silverpeas.importExport.report.UnitReport;
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.session.SessionManagement;
import com.silverpeas.session.SessionManagementFactory;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.HTTPSessionInfo;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.admin.OrganisationControllerFactory;
import org.silverpeas.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.web.util.SilverpeasTransverseWebErrorUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.silverpeas.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static com.silverpeas.util.StringUtil.isDefined;

/**
 * Class declaration
 * @author
 */
public class ImportDragAndDrop extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace.fatal("importExportPeas", "ImportDragAndDrop.init",
          "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse res)
      throws ServletException, IOException {
    SilverTrace.info("importExportPeas", "ImportDragAndDrop.doPost", "root.MSG_GEN_ENTER_METHOD");
    request.setCharacterEncoding("UTF-8");
    if (!FileUploadUtil.isRequestMultipart(request)) {
      res.getOutputStream().println("SUCCESS");
      return;
    }

    String userId = null;
    String userLanguage = null;
    StringBuilder result = new StringBuilder();
    try {
      String componentId = request.getParameter("ComponentId");
      String topicId = request.getParameter("TopicId");
      if (!StringUtil.isDefined(topicId)) {
        String sessionId = request.getParameter("SessionId");
        SessionManagementFactory factory = SessionManagementFactory.getFactory();
        SessionManagement sessionManagement = factory.getSessionManagement();
        HttpSession session =
            ((HTTPSessionInfo)sessionManagement.getSessionInfo(sessionId)).getHttpSession();
        topicId = (String) session.getAttribute("Silverpeas_DragAndDrop_TopicId");
      }
      userId = request.getParameter("UserId");
      userLanguage = StringUtil.isNotDefined(userId) ? I18NHelper.defaultLanguage :
          UserDetail.getById(userId).getUserPreferences().getLanguage();
      boolean ignoreFolders = StringUtil.getBooleanValue(request.getParameter("IgnoreFolders"));
      boolean draftUsed = StringUtil.getBooleanValue(request.getParameter("Draft"));

      SilverTrace.info("importExportPeas", "Drop", "root.MSG_GEN_PARAM_VALUE",
          "componentId = " + componentId + " topicId = " + topicId
          + " userId = " + userId + " ignoreFolders = " + ignoreFolders
          + ", draftUsed = " + draftUsed);

      String savePath = FileRepositoryManager.getTemporaryPath() + "tmpupload"
          + File.separator + topicId + System.currentTimeMillis() + File.separator;

      List<FileItem> items = FileUploadUtil.parseRequest(request);
      for (FileItem item : items) {
        if (!item.isFormField()) {
          String fileUploadId = item.getFieldName().substring(4);
          String parentPath = FileUploadUtil.getParameter(items, "relpathinfo" + fileUploadId, null);
          String fileName = FileUploadUtil.getFileName(item);
          if (StringUtil.isDefined(parentPath)) {
            if (parentPath.endsWith(":\\")) { // special case for file on root of disk
              parentPath = parentPath.substring(0, parentPath.indexOf(':') + 1);
            }
          }
          parentPath = FileUtil.convertPathToServerOS(parentPath);
          SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE",
              "fileName = " + fileName);
          if (fileName != null) {
            if (fileName.contains(File.separator)) {
              fileName = fileName.substring(fileName.lastIndexOf(File.separatorChar));
              parentPath = parentPath + File.separatorChar + fileName.substring(0, fileName.
                  lastIndexOf(File.separatorChar));
            }
            SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE",
                "fileName on Unix = " + fileName);
          }
          if (!ignoreFolders) {
            if (parentPath != null && parentPath.length() > 0) {
              result.append("newFolder=true&");
              fileName = File.separatorChar + parentPath + File.separatorChar + fileName;
            }
          }
          if (!"".equals(savePath)) {
            File f = new File(savePath + fileName);
            File parent = f.getParentFile();
            if (!parent.exists()) {
              parent.mkdirs();
            }
            item.write(f);
          }
        } else {
          SilverTrace.info("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE", "item = "
              + item.getFieldName() + " - " + item.getString());
        }
      }
      MassiveReport massiveReport = new MassiveReport();
      UserDetail userDetail = OrganisationControllerFactory
          .getOrganisationController().getUserDetail(userId);

      try {
        MassiveDocumentImport massiveImporter = new MassiveDocumentImport();
        ImportSettings settings =
            new ImportSettings(savePath, userDetail, componentId, topicId, draftUsed, true, ImportSettings.FROM_DRAGNDROP);
        ImportReport importReport =
            massiveImporter.importDocuments(settings, massiveReport);

        if (isDefaultClassificationModifiable(topicId, componentId)) {
          ComponentReport componentReport = importReport.getListComponentReport().get(0);
          List<MassiveReport> listMassiveReport = componentReport.getListMassiveReports();
          for (MassiveReport theMassiveReport : listMassiveReport) {
            List<UnitReport> listMassiveUnitReport = theMassiveReport.getListUnitReports();
            for (UnitReport unitReport : listMassiveUnitReport) {
              if(unitReport.getStatus() == UnitReport.STATUS_PUBLICATION_CREATED) {
                result.append("pubid=").append(unitReport.getLabel()).append("&");
              }
            }
          }
        }
      } catch (Exception ex) {
        massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
        SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(ex, userLanguage);
        res.getOutputStream().println("ERROR");
        return;
      }
    } catch (Exception e) {
      SilverTrace.debug("importExportPeas", "Drop.doPost", "root.MSG_GEN_PARAM_VALUE", e);
      final StringBuilder sb = new StringBuilder("ERROR: ");
      String transverseMessage =
          SilverpeasTransverseWebErrorUtil.performAppletAlertExceptionMessage(e, userLanguage);
      if (isDefined(transverseMessage)) {
        sb.append(transverseMessage);
      } else {
        sb.append(e.getMessage());
      }
      res.getOutputStream().println(sb.toString());
      return;
    }

    if (result.length() > 0) {
      res.getOutputStream().println(result.substring(0, result.length() - 1));
    } else {
      res.getOutputStream().println("SUCCESS");
    }
  }

  /**
   * Is the default classification on the PdC used to classify the publications published in the
   * specified topic of the specified component instance can be modified during the
   * multi-publications import process? If no default classification is defined for the specified
   * topic (and for any of its parent topics), then false is returned.
   * @param topicId the unique identifier of the topic.
   * @param componentId the unique identifier of the component instance.
   * @return true if the default classification can be modified during the automatical
   * classification of the imported publications. False otherwise.
   */
  protected boolean isDefaultClassificationModifiable(String topicId, String componentId) {
    PdcClassificationService classificationService = PdcServiceFactory.getFactory().
        getPdcClassificationService();
    PdcClassification defaultClassification = classificationService.findAPreDefinedClassification(
        topicId, componentId);
    return defaultClassification != NONE_CLASSIFICATION && defaultClassification.isModifiable();
  }
}
