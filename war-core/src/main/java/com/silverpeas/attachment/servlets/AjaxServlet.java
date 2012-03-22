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

package com.silverpeas.attachment.servlets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentException;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetailI18N;

public class AjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO Auto-generated method stub
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String action = getAction(req);
    String result = null;

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

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private ForeignPK getForeignPK(HttpServletRequest req) {
    HttpSession session = req.getSession();

    String id = (String) session.getAttribute("Silverpeas_Attachment_ObjectId");
    String componentId = (String) session.getAttribute("Silverpeas_Attachment_ComponentId");

    return new ForeignPK(id, componentId);
  }

  private String getUserId(HttpServletRequest req) {
    MainSessionController msc =
        (MainSessionController) req.getSession().getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    return msc.getCurrentUserDetail().getId();
  }

  private String checkout(HttpServletRequest req) {
    String idAttachment = req.getParameter("Id");
    String userId = getUserId(req);
    String fileLanguage = req.getParameter("FileLanguage");

    boolean checkOutOK = false;
    try {
      checkOutOK = AttachmentController.checkoutFile(idAttachment, userId, fileLanguage);
    } catch (AttachmentException e) {
      SilverTrace.error("attachment", "AjaxServlet.checkout", "root.MSG_GEN_PARAM_VALUE", e);
    }

    if (checkOutOK) {
      return "ok";
    }
    return "nok";
  }

  private String checkin(HttpServletRequest req) {
    String idAttachment = req.getParameter("Id");
    String fileLanguage = req.getParameter("FileLanguage");
    boolean update = Boolean.parseBoolean(req.getParameter("update_attachment"));
    boolean force =
        Boolean.parseBoolean(req.getParameter("force_release")) && getMainSessionController(
        req).getCurrentUserDetail().isAccessAdmin();
    try {
      if (!AttachmentController.checkinFile(idAttachment, getUserId(req), false, update, force,
          fileLanguage)) {
        return "locked";
      }
      return "ok";
    } catch (AttachmentException e) {
      SilverTrace.error("attachment", "AjaxServlet.checkin", "root.MSG_GEN_PARAM_VALUE", e);
    }
    return "error";
  }

  private String deleteAttachment(HttpServletRequest req) {
    String id = req.getParameter("id");
    String languagesToRemove = req.getParameter("languagesToDelete");
    boolean indexIt = isIndexable(req);

    StringTokenizer tokenizer = new StringTokenizer(languagesToRemove, ",");
    int nbTranslationsToDelete = tokenizer.countTokens();

    // create AttachmentPK with id and componentId
    AttachmentPK atPK = new AttachmentPK(id, getForeignPK(req).getInstanceId());

    try {
      if (tokenizer.hasMoreElements()) {
        String lang = tokenizer.nextToken();
        if (lang.equals("all")) {
          // suppresion de l'objet
          AttachmentController.deleteAttachment(atPK);
          return "attachmentRemoved";
        } else {
          AttachmentDetail attachment = AttachmentController.searchAttachmentByPK(atPK);
          if (nbTranslationsToDelete >= attachment.getTranslations().size()) {
            // suppresion de l'objet
            AttachmentController.deleteAttachment(atPK);

            return "attachmentRemoved";
          } else {
            boolean hasMoreTranslations = true;
            while (hasMoreTranslations) {
              attachment.setRemoveTranslation(true);

              // search translation id according to language
              AttachmentDetailI18N translation =
                  (AttachmentDetailI18N) attachment.getTranslation(lang);
              attachment.setLanguage(lang);
              attachment.setTranslationId(Integer.toString(translation.getId()));

              AttachmentController.updateAttachment(attachment, indexIt);

              hasMoreTranslations = tokenizer.hasMoreTokens();
              if (hasMoreTranslations) {
                lang = tokenizer.nextToken();
              }
            }

            return "translationsRemoved";
          }
        }
      } else {
        // suppresion de l'objet
        AttachmentController.deleteAttachment(atPK);

        return "attachmentRemoved";
      }
    } catch (Exception e) {
      return "failed";
    }
  }

  private String sort(HttpServletRequest req) {
    String orderedList = req.getParameter("orderedList");
    String componentId = getForeignPK(req).getInstanceId();

    StringTokenizer tokenizer = new StringTokenizer(orderedList, ",");
    List<AttachmentPK> attachmentPKs = new ArrayList<AttachmentPK>();
    while (tokenizer.hasMoreTokens()) {
      attachmentPKs.add(new AttachmentPK(tokenizer.nextToken(), componentId));
    }

    // Save attachment order
    try {
      AttachmentController.sortAttachments(attachmentPKs);
      return "ok";
    } catch (AttachmentException e) {
      SilverTrace.error("attachment", "AjaxServlet.sort", "root.MSG_GEN_PARAM_VALUE", e);
    }

    return "error";

  }

  private boolean isIndexable(HttpServletRequest req) {
    return ((Boolean) req.getSession().getAttribute("Silverpeas_Attachment_IndexIt"))
        .booleanValue();
  }

  private MainSessionController getMainSessionController(HttpServletRequest request) {
    return (MainSessionController) request.getSession().getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
  }
}
