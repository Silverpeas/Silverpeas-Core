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
package org.silverpeas.web.servlets;

import org.apache.commons.codec.CharEncoding;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.viewer.model.DocumentView;
import org.silverpeas.core.viewer.service.ViewService;
import org.silverpeas.core.viewer.service.ViewerContext;
import org.silverpeas.core.web.util.ClientBrowserUtil;
import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.core.webapi.viewer.DocumentViewEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.text.MessageFormat.format;
import static org.silverpeas.core.util.JSONCodec.encodeObject;
import static org.silverpeas.core.util.URLUtil.getFullApplicationURL;

public class GoToFile extends GoTo {

  private static final long serialVersionUID = 1L;

  @Override
  public String getDestination(String objectId, Context context) {
    final HttpServletRequest req = context.getRequest();
    final HttpServletResponse res = context.getResponse();
    SimpleDocument attachment = AttachmentServiceProvider.getAttachmentService().
        searchDocumentById(new SimpleDocumentPK(objectId), getContentLanguage(req));
    if (attachment == null) {
      return null;
    }
    String componentId = attachment.getInstanceId();
    String foreignId = attachment.getForeignId();

    if (isUserLogin(req) && attachment.canBeAccessedBy(UserDetail.getCurrentRequester())) {
      if (context.isFromResponsiveWindow()) {
        return sendJson(encodeObject(o -> o.put("downloadUrl", req.getRequestURI())));
      } else if (attachment.isContentPdf()) {
        final DocumentView view = ViewService.get().getDocumentView(ViewerContext.from(attachment));
        return URLUtil.getServerURL(req) + DocumentViewEntity.createFrom(view).getViewerUri();
      } else {
        res.setCharacterEncoding(CharEncoding.UTF_8);
        res.setContentType(format("{0}; charset=utf-8", attachment.getContentType()));
        String fileName = ClientBrowserUtil.rfc2047EncodeFilename(req, attachment.getFilename());
        res.setHeader("Content-Disposition", format("inline; filename=\"{0}\"", fileName));
        return getFullApplicationURL(req) + encodeFilename(attachment.getAttachmentURL());
      }
    }

    if (StringUtil.isDefined(req.getParameter("ComponentId"))) {
      componentId = req.getParameter("ComponentId");
    }

    return "ComponentId=" + componentId + "&AttachmentId=" + objectId + "&Mapping=File&ForeignId="
        + foreignId;
  }

  @Override
  public String getDestination(final String objectId, final HttpServletRequest req,
      final HttpServletResponse res) {
    // Must not be called
    return null;
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