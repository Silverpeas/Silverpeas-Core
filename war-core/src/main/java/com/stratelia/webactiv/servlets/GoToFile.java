/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ClientBrowserUtil;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;

public class GoToFile extends GoTo {

  private static final long serialVersionUID = 1L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    // Check first if attachment exists
    AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(new AttachmentPK(
        objectId));
    if (attachment == null) {
      return null;
    }
    String componentId = attachment.getInstanceId();
    String foreignId = attachment.getForeignKey().getId();

    if (isUserLogin(req) && isUserAllowed(req, componentId)) {
      // L'utilisateur a-t-il le droit de consulter le fichier/la publication
      boolean isAccessAuthorized = true;
      if (componentId.startsWith("kmelia")) {
        try {
          ComponentSecurity security = (ComponentSecurity) Class.forName(
              "com.stratelia.webactiv.kmelia.KmeliaSecurity").newInstance();
          isAccessAuthorized = security.isAccessAuthorized(componentId,
              getUserId(req), foreignId);
        } catch (Exception e) {
          SilverTrace.error("peasUtil", "GoToFile.doPost",
              "root.EX_CLASS_NOT_INITIALIZED",
              "com.stratelia.webactiv.kmelia.KmeliaSecurity", e);
          return null;
        }
      }
      if (isAccessAuthorized) {
        res.setCharacterEncoding("UTF-8");
        res.setContentType("text/html; charset=utf-8");
        String fileName = ClientBrowserUtil.rfc2047EncodeFilename(req, attachment.getLogicalName());
        res.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        return URLManager.getFullApplicationURL(req) +
            encodeFilename(attachment.getAttachmentURL());
      }
    }
    return "ComponentId=" + componentId + "&AttachmentId=" + objectId + "&Mapping=File&ForeignId="
        + foreignId;
  }

  private String encodeFilename(String url) {
    if (url.indexOf('/') >= 0) {
      int end = url.lastIndexOf('/');
      if (end < url.length()) {
        String subUrl = url.substring(0, end + 1);
        String fileName;
        try {
          fileName = URLEncoder.encode(url.substring(end + 1), "UTF-8");
        } catch (UnsupportedEncodingException e) {
          fileName = url.substring(end + 1);
        }
        return subUrl + fileName;
      }

    }
    return url;
  }
}
