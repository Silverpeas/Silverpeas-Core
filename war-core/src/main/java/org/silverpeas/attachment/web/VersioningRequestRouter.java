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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.web;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.http.HttpServletRequest;
import java.rmi.RemoteException;
import java.util.List;

public class VersioningRequestRouter extends ComponentRequestRouter<VersioningSessionController> {

  private static final long serialVersionUID = 4808952397898736028L;

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
      HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("versioningPeas", "VersioningRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "User=" + versioningSC.getUserId() + " Function=" + function);
    String rootDestination = "/versioningPeas/jsp/";
    try {
      String flag = versioningSC.getProfile();

      request.setAttribute("Profile", flag);
      if ("ListPublicVersionsOfDocument".equals(function)) {
        String documentId = request.getParameter("DocId");
        String isAlias = request.getParameter("Alias");
        SimpleDocumentPK documentPK =
            new SimpleDocumentPK(documentId, versioningSC.getComponentId());
        SimpleDocument document = versioningSC.getDocument(documentPK);
        List<SimpleDocument> publicVersions = versioningSC.getPublicDocumentVersions(documentPK);

        request.setAttribute("Document", document);
        request.setAttribute("PublicVersions", publicVersions);
        request.setAttribute("Alias", isAlias);
        destination = "/versioningPeas/jsp/publicVersions.jsp";
      } else if ("ViewAllVersions".equals(function)) {
        return viewVersions(request, versioningSC);
      } else {
        destination = rootDestination + function;
      }
    } catch (Exception e) {
      SilverTrace.error("versioning", "VersioningRequestRouter.getDestination",
          "root.EX_CANT_GET_REQUEST_DESTINATION", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    SilverTrace.info("versioningPeas", "VersioningRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
    return destination;
  }

  private String viewVersions(HttpServletRequest request, VersioningSessionController versioningSC)
      throws RemoteException {
    String documentId = request.getParameter("DocId");
    String isAlias = request.getParameter("Alias");
    SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId, versioningSC.getComponentId());

    SimpleDocument document = versioningSC.getDocument(documentPK);

    List<SimpleDocument> versions;
    if (!versioningSC.isWriter(document, versioningSC.getUserId())) {
      versions = versioningSC.getPublicDocumentVersions(documentPK);
      if (document.isPublic()) {
        versions.add(document);
      }
    } else {
      versions = versioningSC.getDocumentVersions(documentPK);
      versions.add(document);
    }
    request.setAttribute("Document", document);
    request.setAttribute("Versions", versions);
    request.setAttribute("Alias", isAlias);
    return "/attachment/jsp/publicVersions.jsp";
  }
}
