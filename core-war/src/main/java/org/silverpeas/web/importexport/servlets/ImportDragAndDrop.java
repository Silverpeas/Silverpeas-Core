/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.importexport.servlets;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.documenttemplate.DocumentTemplate;
import org.silverpeas.core.importexport.control.ImportSettings;
import org.silverpeas.core.importexport.control.MassiveDocumentImport;
import org.silverpeas.core.importexport.model.ImportExportException;
import org.silverpeas.core.importexport.report.ComponentReport;
import org.silverpeas.core.importexport.report.ImportReport;
import org.silverpeas.core.importexport.report.MassiveReport;
import org.silverpeas.core.importexport.report.UnitReport;
import org.silverpeas.core.io.upload.UploadSession;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.error.SilverpeasTransverseErrorUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.webapi.documenttemplate.DocumentTemplateWebManager;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;

/**
 * Class declaration
 * @author silveryocha
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
      SilverLogger.getLogger(this).error(se);
    }
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse res) {
    doPost(req, res);
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse res) {
    final HttpRequest request = HttpRequest.decorate(req);
    final UploadSession uploadSession = UploadSession.from(request);
    final StringBuilder result = new StringBuilder();
    try {
      request.setCharacterEncoding("UTF-8");
      String componentId = request.getParameter("ComponentId");
      final boolean fromDocumentTemplate = adjustUploadSessionWithDocumentTemplateIfAny(
          uploadSession, request, componentId);
      checkUserAuthorizations(uploadSession, componentId);
      checkUploadSession(uploadSession, request);
      final String topicId = getTopicId(request);
      final boolean ignoreFolders = request.getParameterAsBoolean("IgnoreFolders");
      final boolean draftUsed = request.getParameterAsBoolean("Draft");
      final String publicationName = request.getParameter("PublicationName");
      final String publicationDescription = getPublicationDescription(uploadSession, request);
      final boolean onePublicationForAll = StringUtil.isDefined(publicationName);
      // root folder initialization
      final File rootUploadFolder = getRootUploadFileFolder(uploadSession, result, ignoreFolders,
          onePublicationForAll);
      // import setting initialization
      final ImportSettings settings = new ImportSettings(rootUploadFolder.getPath(),
          UserDetail.getCurrentRequester(), componentId, topicId, draftUsed, true,
          ImportSettings.FROM_DRAGNDROP);
      final String contentLanguage = request.getParameter("ContentLanguage");
      final String publicationKeywords = getPublicationKeywords(uploadSession, request);
      final String versionType = request.getParameter("VersionType");
      final String validatorIds = request.getParameter("ValidatorIds");
      settings.setVersioningUsed(
          StringUtil.isDefined(versionType) && StringUtil.isInteger(versionType));
      if (settings.isVersioningUsed()) {
        final String versionComment = request.getParameter("commentMessage");
        settings.setVersionAndComment(Integer.parseInt(versionType), versionComment);
      }
      settings.setSingleFileTitle(request.getParameter("fileTitle"));
      settings.setSingleFileDescription(request.getParameter("fileDescription"));
      settings.setTargetValidatorIds(validatorIds);
      settings.setContentLanguage(contentLanguage);
      if (onePublicationForAll) {
        settings.getPublicationForAllFiles().setName(publicationName);
        settings.getPublicationForAllFiles().setDescription(publicationDescription);
        settings.getPublicationForAllFiles().setKeywords(publicationKeywords);
      }
      if (fromDocumentTemplate) {
        settings.setUseFileMetadata(false);
      }
      // import
      result.append(performImport(settings));
    } catch (ImportExportException | IOException e) {
      SilverLogger.getLogger(this).error(e);
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    } finally {
      uploadSession.clear();
    }
    if (res.getStatus() != HttpServletResponse.SC_INTERNAL_SERVER_ERROR && result.length() > 0) {
      try {
        res.getOutputStream().println(result.substring(0, result.length() - 1));
      } catch (IOException e) {
        SilverLogger.getLogger(this).error(e);
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
    }
  }

  private void handleClassificationIfAny(final String componentId, final StringBuilder result, final String topicId,
      final ImportReport importReport) {
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
  }

  private String performImport(final ImportSettings settings) throws ImportExportException {
    final MassiveReport massiveReport = new MassiveReport();
    final StringBuilder result = new StringBuilder();
    try {
      final ImportReport importReport = MassiveDocumentImport.get()
          .importDocuments(settings, massiveReport);
      handleClassificationIfAny(settings.getComponentId(), result, settings.getFolderId(),
          importReport);
    } catch (ImportExportException ex) {
      massiveReport.setError(UnitReport.ERROR_NOT_EXISTS_OR_INACCESSIBLE_DIRECTORY);
      SilverpeasTransverseErrorUtil.throwTransverseErrorIfAny(ex,
          User.getCurrentRequester().getUserPreferences().getLanguage());
      throw ex;
    }
    return result.toString();
  }

  private String getPublicationKeywords(final UploadSession uploadSession,
      final HttpRequest request) {
    return areKeywordsHandled(uploadSession) ? request.getParameter("PublicationKeywords") : "";
  }

  private String getPublicationDescription(final UploadSession uploadSession,
      final HttpRequest request) {
    return isDescriptionHandled(uploadSession) ?
        request.getParameter("PublicationDescription") :
        "";
  }

  private void checkUploadSession(final UploadSession uploadSession, final HttpRequest request) {
    if (isDescriptionMandatory(uploadSession) &&
        StringUtil.isNotDefined(getPublicationDescription(uploadSession, request))) {
      throwHttpPreconditionFailedError();
    }
  }

  private static File getRootUploadFileFolder(final UploadSession uploadSession,
      final StringBuilder result, final boolean ignoreFolders, final boolean onePublicationForAll)
      throws IOException {
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
    return rootUploadFolder;
  }

  private String getTopicId(final HttpRequest request) {
    String topicId = request.getParameter("TopicId");
    if (!StringUtil.isDefined(topicId)) {
      SessionInfo session = getSessionInfo(request);
      topicId = session.getAttribute("Silverpeas_DragAndDrop_TopicId");
    }
    return topicId;
  }

  private void checkUserAuthorizations(final UploadSession uploadSession, final String componentId) {
    if (!uploadSession.isUserAuthorized(componentId)) {
      throwHttpForbiddenError();
    }
  }

  private boolean adjustUploadSessionWithDocumentTemplateIfAny(final UploadSession uploadSession,
      final HttpRequest request, final String componentId) {
    var documentTemplate = ofNullable(request.getParameter("documentTemplateId"))
        .filter(StringUtil::isDefined)
        .map(DocumentTemplateWebManager.get()::getDocumentTemplate)
        .map(t -> Pair.of(t, request.getParameter("fileName")))
        .filter(p ->StringUtil.isDefined(p.getSecond()));
    if (documentTemplate.isPresent()) {
      uploadSession.forComponentInstanceId(componentId);
      final DocumentTemplate docTpl = documentTemplate.get().getFirst();
      final String fileName = documentTemplate.get().getSecond() + "." + docTpl.getExtension();
      try (var i = docTpl.openInputStream()) {
        uploadSession.getUploadSessionFile(fileName).write(i);
      } catch (IOException e) {
        throwHttpForbiddenError();
      }
    }
    return documentTemplate.isPresent();
  }

  /**
   * Is the default classification on the PdC used to classify the publications published in the
   * specified topic of the specified component instance can be modified during the
   * multi-publications import process? If no default classification is defined for the specified
   * topic (and for any of its parent topics), then false is returned.
   * @param topicId the unique identifier of the topic.
   * @param componentId the unique identifier of the component instance.
   * @return true if the default classification can be modified during the automatic
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
