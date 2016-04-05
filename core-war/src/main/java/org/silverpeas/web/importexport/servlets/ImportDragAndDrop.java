/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.web.importexport.servlets;

import org.silverpeas.core.importexport.control.ImportSettings;
import org.silverpeas.core.importexport.control.MassiveDocumentImport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ComponentReport;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.io.upload.UploadSession;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;

/**
 * Class declaration
 * @author
 */
public class ImportDragAndDrop extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 1L;

  @Inject
  private PdcClassificationService pdcClassificationService;

  @Override
  public void init(ServletConfig config) {
    try {
      super.init(config);
    } catch (ServletException se) {
      SilverTrace
          .fatal("importExportPeas", "ImportDragAndDrop.init", "peasUtil.CANNOT_ACCESS_SUPERCLASS");
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    doPost(req, res);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {

    HttpRequest request = HttpRequest.decorate(req);
    request.setCharacterEncoding("UTF-8");

    UserDetail currentUser = UserDetail.getCurrentRequester();
    String userLanguage = currentUser.getUserPreferences().getLanguage();
    UploadSession uploadSession = UploadSession.from(request);
    StringBuilder result = new StringBuilder();
    try {
      String componentId = request.getParameter("ComponentId");

      if (!uploadSession.isUserAuthorized(componentId)) {
        throwHttpForbiddenError();
      }

      String topicId = request.getParameter("TopicId");
      if (!StringUtil.isDefined(topicId)) {
        SessionInfo session = getSessionInfo(req);
        topicId = session.getAttribute("Silverpeas_DragAndDrop_TopicId");
      }
      boolean ignoreFolders = request.getParameterAsBoolean("IgnoreFolders");
      String contentLanguage = request.getParameter("ContentLanguage");
      boolean draftUsed = request.getParameterAsBoolean("Draft");
      String publicationName = request.getParameter("PublicationName");
      String publicationDescription =
          isDescriptionHandled(uploadSession) ? request.getParameter("PublicationDescription") : "";
      String publicationKeywords =
          areKeywordsHandled(uploadSession) ? request.getParameter("PublicationKeywords") : "";
      boolean onePublicationForAll = StringUtil.isDefined(publicationName);
      String versionType = request.getParameter("VersionType");

      if (isDescriptionMandatory(uploadSession) &&
          StringUtil.isNotDefined(publicationDescription)) {
        throwHttpPreconditionFailedError();
      }

      File rootUploadFolder = uploadSession.getRootFolder();
      if (!ignoreFolders && !onePublicationForAll) {
        File[] foldersAtRoot =
            rootUploadFolder.listFiles((FileFilter) FileFilterUtils.directoryFileFilter());
        if (foldersAtRoot != null && foldersAtRoot.length > 0) {
          result.append("newFolder=true&");
        }
      } else {
        FileUtil.moveAllFilesAtRootFolder(rootUploadFolder);
      }

      MassiveReport massiveReport = new MassiveReport();

      try {
        ImportSettings settings =
            new ImportSettings(rootUploadFolder.getPath(), currentUser, componentId, topicId,
                draftUsed, true, ImportSettings.FROM_DRAGNDROP);
        settings.setVersioningUsed(
            StringUtil.isDefined(versionType) && StringUtil.isInteger(versionType));
        if (settings.isVersioningUsed()) {
          settings.setVersionType(Integer.valueOf(versionType));
        }
        settings.setContentLanguage(contentLanguage);
        if (onePublicationForAll) {
          settings.getPublicationForAllFiles().setName(publicationName);
          settings.getPublicationForAllFiles().setDescription(publicationDescription);
          settings.getPublicationForAllFiles().setKeywords(publicationKeywords);
        }

        ImportReport importReport =
            MassiveDocumentImport.get().importDocuments(settings, massiveReport);

        if (isDefaultClassificationModifiable(topicId, componentId)) {
          ComponentReport componentReport = importReport.getListComponentReport().get(0);
          List<MassiveReport> listMassiveReport = componentReport.getListMassiveReports();
          for (MassiveReport theMassiveReport : listMassiveReport) {
            List<UnitReport> listMassiveUnitReport = theMassiveReport.getListUnitReports();
            for (UnitReport unitReport : listMassiveUnitReport) {
              if (unitReport.getStatus() == UnitReport.STATUS_PUBLICATION_CREATED) {
                result.append("pubid=").append(unitReport.getLabel()).append("&");
              }
            }
          }
        }
      } catch (ImportExportException ex) {
        massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
        SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(ex, userLanguage);
        throw new ServletException(ex);
      }
    } finally {
      uploadSession.clear();
    }

    if (result.length() > 0) {
      res.getOutputStream().println(result.substring(0, result.length() - 1));
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
    PdcClassification defaultClassification =
        pdcClassificationService.findAPreDefinedClassification(topicId, componentId);
    return defaultClassification != NONE_CLASSIFICATION && defaultClassification.isModifiable();
  }

  /**
   * Indicates if the description data of a publication is handled for the component instance
   * represented by the given identifier.
   * @param uploadSession the upload session that stores the component instance identifier.
   * @return true if the description data is handled, false otherwise.
   */
  private boolean isDescriptionHandled(UploadSession uploadSession) {
    String paramValue = uploadSession.getComponentInstanceParameterValue("useDescription");
    return "1".equalsIgnoreCase(paramValue) || "2".equalsIgnoreCase(paramValue) ||
        "".equals(paramValue);
  }

  /**
   * Indicates if the description data of a publication is mandatory for the component instance
   * represented by the given identifier.
   * @param uploadSession the upload session that stores component instance identifier.
   * @return true if the description data is mandatory, false otherwise.
   */
  private boolean isDescriptionMandatory(UploadSession uploadSession) {
    return "2".equalsIgnoreCase(uploadSession.getComponentInstanceParameterValue("useDescription"));
  }

  /**
   * Indicates if the keyword data of a publication is handled for the component instance
   * represented by the given identifier.
   * @param uploadSession the upload session that stores component instance identifier.
   * @return true if the keyword data is handled, false otherwise.
   */
  private boolean areKeywordsHandled(UploadSession uploadSession) {
    String paramValue = uploadSession.getComponentInstanceParameterValue("useKeywords");
    return StringUtil.getBooleanValue(paramValue) || "".equals(paramValue);
  }
}
