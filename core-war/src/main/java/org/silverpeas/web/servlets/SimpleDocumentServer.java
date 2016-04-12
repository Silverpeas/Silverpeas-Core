/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.servlets;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.CharEncoding;

import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;

import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.security.authorization.ComponentAuthorization;

import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.web.util.ClientBrowserUtil;

/**
 * Servlet to access a simple document directly.
 *
 * @author ehugonnet
 */
public class SimpleDocumentServer extends GoTo {

  private static final long serialVersionUID = 1L;
  public static final String KMELIA_SECURITY_CLASS = "org.silverpeas.components.kmelia.KmeliaAuthorization";

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    MainSessionController mainSessionCtrl = util.getMainSessionController(req);
    String language = I18NHelper.defaultLanguage;
    if (mainSessionCtrl != null) {
      language = mainSessionCtrl.getFavoriteLanguage();
    }
    SimpleDocumentPK pk = new SimpleDocumentPK(objectId);
    if (StringUtil.isLong(objectId)) {
      pk.setOldSilverpeasId(Long.parseLong(objectId));
    }

    SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
        searchDocumentById(pk, language);
    if (attachment == null) {
      return null;
    }
    String componentId = attachment.getInstanceId();
    String foreignId = attachment.getForeignId();

    if (isUserLogin(req) && isUserAllowed(req, componentId)) {
      boolean isAccessAuthorized = true;
      if (componentId.startsWith("kmelia")) {
        try {
          ComponentAuthorization
              security = (ComponentAuthorization) Class.forName(KMELIA_SECURITY_CLASS).newInstance();
          isAccessAuthorized = security.isAccessAuthorized(componentId, getUserId(req), foreignId);
        } catch (Exception e) {
          SilverTrace.error("util", "GoToFile.doPost", "root.EX_CLASS_NOT_INITIALIZED",
              "org.silverpeas.components.kmelia.KmeliaAuthorization", e);
          return null;
        }
      }
      if (isAccessAuthorized) {
        res.setCharacterEncoding(CharEncoding.UTF_8);
        res.setContentType("text/html; charset=utf-8");
        String fileName = ClientBrowserUtil.rfc2047EncodeFilename(req, attachment.getFilename());
        res.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
        return URLUtil.getFullApplicationURL(req) + encodeFilename(attachment.getAttachmentURL());
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
