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
package org.silverpeas.servlets;

import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GoToDocument extends GoTo {

  private static final long serialVersionUID = 1L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
        new SimpleDocumentPK(objectId), null);
    if (document != null) {
      SimpleDocument version = document.getLastPublicVersion();
      if (version != null) {
        return redirectToFile(version, req, res);
      }
    }
    return null;
  }

  public String redirectToFile(SimpleDocument version, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    boolean isLoggedIn = isUserLogin(req);
    String componentId = version.getInstanceId();
    String foreignId = version.getForeignId();
    if (StringUtil.isDefined(componentId)) {
      setGefSpaceId(req, componentId);
    }
    if (isLoggedIn) {
      // L'utilisateur est déjà loggué
      if (isUserAllowed(req, componentId)) {
        // L'utilisateur a-t-il le droit de consulter le fichier/la publication
        boolean isAccessAuthorized = true;
        if (componentId.startsWith("kmelia")) {
          try {
            ComponentSecurity security = (ComponentSecurity) Class.forName(
                "com.stratelia.webactiv.kmelia.KmeliaSecurity").newInstance();
            isAccessAuthorized = security.isAccessAuthorized(componentId, getUserId(req), foreignId);
          } catch (ClassNotFoundException e) {
            SilverTrace.error("peasUtil", "GoToDocument.doPost", "root.EX_CLASS_NOT_INITIALIZED",
                "com.stratelia.webactiv.kmelia.KmeliaSecurity", e);
            return null;
          }
        }

        if (isAccessAuthorized) {
          return URLManager.getFullApplicationURL(req) + version.getUniversalURL();
        }
      }
    }
    return "ComponentId=" + componentId + "&AttachmentId=" + version.getId()
        + "&Mapping=Version&ForeignId=" + foreignId;
  }
}
