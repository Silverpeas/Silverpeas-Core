/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.attachment;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.repository.HistoryDocumentSorter;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class VersioningRequestRouter extends ComponentRequestRouter<VersioningSessionController> {

  private static final long serialVersionUID = 4808952397898736028L;
  private static final String ALIAS_ATTR = "Alias";
  private static final String PUBLIC_VERSIONS_ATTR = "PublicVersions";
  private static final String DOCUMENT_ATTR = "Document";
  private static final String DOC_ID_ATTR = "DocId";
  private static final String VERSIONS_ATTR = "Versions";
  private static final String LANGUAGE_ATTR = "Language";

  @Override
  public VersioningSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new VersioningSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getSessionControlBeanName() {
    return "versioningPeas";
  }

  @Override
  public String getDestination(String function, VersioningSessionController versioningSC,
      HttpRequest request) {
    String destination;

    String rootDestination = "/versioningPeas/jsp/";
    try {
      String flag = versioningSC.getProfile();

      // Handle the content language.
      String contentLanguage = request.getParameter(LANGUAGE_ATTR);
      if (!StringUtil.isDefined(contentLanguage)) {
        contentLanguage = null;
      }
      request.setAttribute("ContentLanguage", contentLanguage);
      versioningSC.setContentLanguage(contentLanguage);

      request.setAttribute("Profile", flag);
      if ("ListPublicVersionsOfDocument".equals(function)) {
        String documentId = request.getParameter(DOC_ID_ATTR);
        String isAlias = request.getParameter(ALIAS_ATTR);
        SimpleDocumentPK documentPK =
            new SimpleDocumentPK(documentId, versioningSC.getComponentId());
        SimpleDocument document = versioningSC.getDocument(documentPK);
        List<SimpleDocument> publicVersions = versioningSC.getPublicDocumentVersions(documentPK);
        request.setAttribute(DOCUMENT_ATTR, document);
        request.setAttribute(PUBLIC_VERSIONS_ATTR, publicVersions);
        request.setAttribute(ALIAS_ATTR, isAlias);
        destination = "/versioningPeas/jsp/publicVersions.jsp";
      } else if ("ViewAllVersions".equals(function)) {
        return viewVersions(request, versioningSC);
      } else {
        destination = rootDestination + function;
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String viewVersions(HttpServletRequest request,
      VersioningSessionController versioningSC) {
    String documentId = request.getParameter(DOC_ID_ATTR);
    String isAlias = request.getParameter(ALIAS_ATTR);
    SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId, versioningSC.getComponentId());

    SimpleDocument document = versioningSC.getDocument(documentPK);

    List<SimpleDocument> versions;
    if (!versioningSC.isWriter(document, versioningSC.getUserId())) {
      versions = versioningSC.getPublicDocumentVersions(documentPK);
      if (document.isPublic() && !versions.contains(document)) {
        versions.add(document);
      }
    } else {
      versions = versioningSC.getDocumentVersions(documentPK);
      versions.add(document);
    }
    HistoryDocumentSorter.sortHistory(versions);
    request.setAttribute(DOCUMENT_ATTR, document);
    request.setAttribute(VERSIONS_ATTR, versions);
    request.setAttribute(ALIAS_ATTR, isAlias);
    return "/attachment/jsp/publicVersions.jsp";
  }
}
