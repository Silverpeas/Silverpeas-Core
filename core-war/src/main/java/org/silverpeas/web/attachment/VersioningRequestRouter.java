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
package org.silverpeas.web.attachment;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

import java.util.List;

public class VersioningRequestRouter extends ComponentRequestRouter<VersioningSessionController> {

  private static final long serialVersionUID = 4808952397898736028L;
  private static final String FROM_ALIAS_ATTR = "fromAlias";
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
      // Handle alias
      final boolean fromAlias = request.getParameterAsBoolean(FROM_ALIAS_ATTR);
      request.setAttribute(FROM_ALIAS_ATTR, fromAlias);
      // Handle the content language.
      String contentLanguage = request.getParameter(LANGUAGE_ATTR);
      if (!StringUtil.isDefined(contentLanguage)) {
        contentLanguage = null;
      }
      request.setAttribute("ContentLanguage", contentLanguage);
      versioningSC.setContentLanguage(contentLanguage);
      // Handle profile
      String flag = versioningSC.getProfile();
      request.setAttribute("Profile", flag);
      if ("ViewAllVersions".equals(function)) {
        destination = viewVersions(request, versioningSC, fromAlias);
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

  private String viewVersions(HttpRequest request, VersioningSessionController versioningSC,
      final boolean fromAlias) {
    final String documentId = request.getParameter(DOC_ID_ATTR);
    final SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId, versioningSC.getComponentId());
    final SimpleDocument document = versioningSC.getDocument(documentPK);
    final List<SimpleDocument> versions = versioningSC.getAccessibleDocumentVersions(document, fromAlias);
    request.setAttribute(DOCUMENT_ATTR, document);
    request.setAttribute(VERSIONS_ATTR, versions);
    return "/attachment/jsp/publicVersions.jsp";
  }
}
