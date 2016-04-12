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
package org.silverpeas.web.attachment;

import org.silverpeas.core.web.mvc.webcomponent.SilverpeasAuthenticatedHttpServlet;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.model.UnlockOption;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AjaxServlet extends SilverpeasAuthenticatedHttpServlet {

  private static final long serialVersionUID = 1L;

  private AttachmentService attachmentService = AttachmentService.get();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse resp)
      throws ServletException, IOException {
    HttpRequest req = HttpRequest.decorate(request);

    String action = getAction(req);
    String result = StringUtil.EMPTY;

    if ("Delete".equals(action)) {
      result = deleteAttachment(req);
    } else if ("Checkout".equals(action)) {
      result = checkout(req);
    } else if ("Checkin".equals(action)) {
      result = checkin(req);
    } else if ("Sort".equals(action)) {
      result = sort(req);
    }

    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getAction(HttpRequest req) {
    return req.getParameter("Action");
  }

  private ForeignPK getForeignPK(HttpRequest req) {
    HttpSession session = req.getSession();

    String id = (String) session.getAttribute("Silverpeas_Attachment_ObjectId");
    String componentId = (String) session.getAttribute("Silverpeas_Attachment_ComponentId");

    return new ForeignPK(id, componentId);
  }

  private String getUserId(HttpRequest req) {
    return req.getMainSessionController().getCurrentUserDetail().getId();
  }

  private String checkout(HttpRequest req) {
    String idAttachment = req.getParameter("Id");
    String userId = getUserId(req);
    String fileLanguage = req.getParameter("FileLanguage");
    boolean checkOutOK = attachmentService.lock(idAttachment, userId, fileLanguage);
    if (checkOutOK) {
      SimpleDocumentPK docPk = new SimpleDocumentPK(idAttachment);
      SimpleDocument lockedDocument = attachmentService.searchDocumentById(docPk, fileLanguage);
      return DateUtil.getOutputDateAndHour(lockedDocument.getReservation(), req.getUserLanguage());
    }
    return "nok";
  }

  private String checkin(HttpRequest req) {
    UnlockContext context = new UnlockContext(req.getParameter("Id"), getUserId(req), req.
        getParameter("FileLanguage"));
    if (StringUtil.getBooleanValue(req.getParameter("update_attachment"))) {
      context.addOption(UnlockOption.WEBDAV);
    }
    SimpleDocument doc =
        attachmentService.searchDocumentById(new SimpleDocumentPK(req.getParameter("Id")),
            req.getParameter("FileLanguage"));
    if (!doc.isPublic()) {
      context.addOption(UnlockOption.PRIVATE_VERSION);
    }
    if (StringUtil.getBooleanValue(req.getParameter("force_release")) && getMainSessionController(
        req).getCurrentUserDetail().isAccessAdmin()) {
      context.addOption(UnlockOption.FORCE);
    }
    if (!attachmentService.unlock(context)) {
      return "locked";
    }
    return "ok";
  }

  private String deleteAttachment(HttpRequest req) {
    String id = req.getParameter("id");
    String languagesToRemove = req.getParameter("languagesToDelete");
    boolean indexIt = isIndexable(req);

    StringTokenizer tokenizer = new StringTokenizer(languagesToRemove, ",");
    SimpleDocumentPK atPK = new SimpleDocumentPK(id, getForeignPK(req).getInstanceId());

    try {
      if (tokenizer.hasMoreElements()) {
        String lang = tokenizer.nextToken();
        if ("all".equals(lang)) {
          // suppresion de l'objet
          SimpleDocument doc = attachmentService.searchDocumentById(atPK, null);
          attachmentService.deleteAttachment(doc);
          return "attachmentRemoved";
        } else {
          SimpleDocument doc = attachmentService.searchDocumentById(atPK, lang);

          boolean hasMoreTranslations = true;
          while (hasMoreTranslations) {
            attachmentService.removeContent(doc, lang, indexIt);
            hasMoreTranslations = tokenizer.hasMoreTokens();
            if (hasMoreTranslations) {
              lang = tokenizer.nextToken();
            }
          }
          return "translationsRemoved";
        }
      } else {
        SimpleDocument doc = attachmentService.searchDocumentById(atPK, null);
        attachmentService.deleteAttachment(doc);
        return "attachmentRemoved";
      }
    } catch (Exception e) {
      return "failed";
    }
  }

  private String sort(HttpRequest req) {
    String orderedList = req.getParameter("orderedList");
    String componentId = getForeignPK(req).getInstanceId();

    StringTokenizer tokenizer = new StringTokenizer(orderedList, ",");
    List<SimpleDocument> attachments = new ArrayList<>();
    while (tokenizer.hasMoreTokens()) {
      String id = tokenizer.nextToken();
      SimpleDocumentPK pk;
      if (StringUtil.isLong(id)) {
        pk = new SimpleDocumentPK(null, componentId).setOldSilverpeasId(Long.valueOf(id));
      } else {
        pk = new SimpleDocumentPK(id, componentId);
      }
      attachments.
          add(attachmentService.searchDocumentById(pk, null));
    }
    attachmentService.reorderDocuments(attachments);
    return "ok";
  }

  private boolean isIndexable(HttpRequest req) {
    return ((Boolean) req.getSession().getAttribute("Silverpeas_Attachment_IndexIt"));
  }
}
