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
package com.stratelia.webactiv.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.AccessForbiddenException;
import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class GoToDocument extends GoTo {

  private static final long serialVersionUID = 1L;

  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    // Check first if document exists
    VersioningUtil versioningUtil = new VersioningUtil();
    DocumentVersion version =
        versioningUtil.getLastPublicVersion(new DocumentPK(Integer.parseInt(objectId)));
    if (version == null)
      return null;

    return redirectToFile(version, req, res);
  }

  public String redirectToFile(DocumentVersion version, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    boolean isLoggedIn = isUserLogin(req);

    String componentId = version.getInstanceId();
    VersioningUtil versioningUtil = new VersioningUtil();
    Document document = versioningUtil.getDocument(version.getDocumentPK());
    String foreignId = document.getForeignKey().getId();

    if (isLoggedIn) {
      boolean isAccessAuthorized = false;
      // L'utilisateur est déjà loggué
      if (isUserAllowed(req, componentId)) {
        // L'utilisateur a-t-il le droit de consulter le fichier/la publication
        isAccessAuthorized = true;
        if (componentId.startsWith("kmelia")) {
          try {
            ComponentSecurity security =
                (ComponentSecurity) Class.forName("com.stratelia.webactiv.kmelia.KmeliaSecurity")
                .newInstance();
            isAccessAuthorized =
                security.isAccessAuthorized(componentId, getUserId(req), foreignId);
          } catch (Exception e) {
            SilverTrace.error("peasUtil", "GoToDocument.doPost", "root.EX_CLASS_NOT_INITIALIZED",
                "com.stratelia.webactiv.kmelia.KmeliaSecurity", e);
            return null;
          }
        }

        if (isAccessAuthorized) {
          res.sendRedirect(new VersioningUtil().getDocumentVersionURL(componentId, version
              .getLogicalName(), version.getDocumentPK().getId(), version.getPk().getId()));
        }
      }

      if (!isAccessAuthorized)
        throw new AccessForbiddenException("GoToFile.getDestination", SilverpeasException.WARNING,
            null);

      return "useless";
    }

    return "ComponentId=" + componentId + "&AttachmentId=" + version.getPk().getId() +
        "&Mapping=Version&ForeignId=" + foreignId;
  }
}