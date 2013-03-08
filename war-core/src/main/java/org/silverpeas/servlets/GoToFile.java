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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.CharEncoding;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.peasUtil.GoTo;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.security.ComponentSecurity;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ClientBrowserUtil;

public class GoToFile extends GoTo {

  private static final long serialVersionUID = 1L;
  public static final String KMELIA_SECURITY_CLASS = "com.stratelia.webactiv.kmelia.KmeliaSecurity";

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    MainSessionController mainSessionCtrl = util.getMainSessionController(req);
    String language = I18NHelper.defaultLanguage;
    if (mainSessionCtrl != null) {
      language = mainSessionCtrl.getFavoriteLanguage();
    }
    if(StringUtil.isLong(objectId)) {
      
    }
    SimpleDocument attachment = AttachmentServiceFactory.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(objectId), language);
    if (attachment == null) {
      return null;
    }
    String componentId = attachment.getInstanceId();
    String foreignId = attachment.getForeignId();

    if (isUserLogin(req) && isUserAllowed(req, componentId)) {
      // L'utilisateur a-t-il le droit de consulter le fichier/la publication
      boolean isAccessAuthorized = true;
      if (componentId.startsWith("kmelia")) {
        try {
          ComponentSecurity security = (ComponentSecurity) Class.forName(KMELIA_SECURITY_CLASS).newInstance();
          isAccessAuthorized = security.isAccessAuthorized(componentId, getUserId(req), foreignId);
        } catch (Exception e) {
          SilverTrace.error("peasUtil", "GoToFile.doPost", "root.EX_CLASS_NOT_INITIALIZED",
              "com.stratelia.webactiv.kmelia.KmeliaSecurity", e);
          return null;
        }
      }
      if (isAccessAuthorized) {
        res.setCharacterEncoding(CharEncoding.UTF_8);
        res.setContentType("text/html; charset=utf-8");
        String fileName = ClientBrowserUtil.rfc2047EncodeFilename(req, attachment.getFilename());
        res.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        return URLManager.getFullApplicationURL(req) + encodeFilename(attachment.getAttachmentURL());
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
          fileName = URLEncoder.encode(url.substring(end + 1), CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
          fileName = url.substring(end + 1);
        }
        return subUrl + fileName;
      }

    }
    return url;
  }
}
