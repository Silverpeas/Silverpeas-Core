/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

import com.silverpeas.accesscontrol.AccessControlContext;
import com.silverpeas.accesscontrol.AccessControlOperation;
import com.silverpeas.accesscontrol.AccessController;
import com.silverpeas.accesscontrol.AccessControllerProvider;
import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.permalinks.PermalinkServiceFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GoToDocument extends GoTo {

  private static final long serialVersionUID = 1L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    SimpleDocument document;
    if (StringUtil.isInteger(objectId)) {
      document = PermalinkServiceFactory.getPermalinkCompatibilityService().
          findVersionnedDocumentByOldId(Integer.parseInt(objectId));
    } else {
      document = AttachmentServiceFactory.getAttachmentService().searchDocumentById(
          new SimpleDocumentPK(objectId), getContentLanguage(req));
    }
    if (document != null) {
      return redirectToFile(document, req, res);
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
      AccessController<SimpleDocument> accessController =
          AccessControllerProvider.getAccessController("simpleDocumentAccessController");
      boolean isAccessAuthorized = accessController.isUserAuthorized(getUserId(req), version,
          AccessControlContext.init().onOperationsOf(AccessControlOperation.download));
      if (!isAccessAuthorized) {
        SimpleDocument lastPublicVersion = version.getLastPublicVersion();
        if (lastPublicVersion != null) {
          isAccessAuthorized = accessController.isUserAuthorized(getUserId(req), lastPublicVersion,
              AccessControlContext.init().onOperationsOf(AccessControlOperation.download));
          if (isAccessAuthorized) {
            return URLManager.getServerURL(req) + lastPublicVersion.getUniversalURL();
          }
        }
      } else {
        return URLManager.getServerURL(req) + version.getUniversalURL();
      }
    }
    return null;
  }
}
