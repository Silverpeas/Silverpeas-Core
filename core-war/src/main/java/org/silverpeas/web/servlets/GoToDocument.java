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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.AccessControlOperation;
import org.silverpeas.core.security.authorization.AccessController;
import org.silverpeas.core.security.authorization.AccessControllerProvider;
import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.security.authorization.SimpleDocumentAccessControl;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.permalinks.PermalinkServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GoToDocument extends GoTo {

  private static final long serialVersionUID = 1L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    SimpleDocument document;
    if (StringUtil.isInteger(objectId)) {
      document = PermalinkServiceProvider.getPermalinkCompatibilityService().
          findVersionnedDocumentByOldId(Integer.parseInt(objectId));
    } else {
      document = AttachmentServiceProvider.getAttachmentService().searchDocumentById(
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
      AccessController<SimpleDocument> accessController = AccessControllerProvider
          .getAccessController(SimpleDocumentAccessControl.class);
      boolean isAccessAuthorized = accessController.isUserAuthorized(getUserId(req), version,
          AccessControlContext.init().onOperationsOf(AccessControlOperation.download));
      if (!isAccessAuthorized) {
        SimpleDocument lastPublicVersion = version.getLastPublicVersion();
        if (lastPublicVersion != null) {
          isAccessAuthorized = accessController.isUserAuthorized(getUserId(req), lastPublicVersion,
              AccessControlContext.init().onOperationsOf(AccessControlOperation.download));
          if (isAccessAuthorized) {
            return URLUtil.getServerURL(req) + lastPublicVersion.getUniversalURL();
          }
        }
      } else {
        return URLUtil.getServerURL(req) + version.getUniversalURL();
      }
    }
    return null;
  }
}
