/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.versioningPeas.servlets;

import com.silverpeas.util.StringUtil;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioningPeas.control.VersioningSessionController;

public class AjaxServlet extends HttpServlet {

  private static final long serialVersionUID = 1972204681347444151L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    doPost(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    String action = getAction(req);
    String result = "";
    VersioningSessionController versioningSC =
        (VersioningSessionController) req.getSession().getAttribute("Silverpeas_versioningPeas");
    if ("Delete".equals(action)) {
      result = deleteAttachment(req, versioningSC);
    } else if ("Checkout".equals(action)) {
      result = checkout(req, versioningSC);
    } else if ("IsLocked".equals(action)) {
      result = isLocked(req, versioningSC);
    } else if ("Checkin".equals(action)) {
      result = checkin(req, versioningSC);
    } else if ("Sort".equals(action)) {
      result = sort(req, versioningSC);
    }
    Writer writer = resp.getWriter();
    writer.write(result);
  }

  private String getAction(HttpServletRequest req) {
    return req.getParameter("Action");
  }

  private String getUserId(HttpServletRequest req) {
    MainSessionController msc = (MainSessionController) req.getSession().getAttribute(
        MainSessionController.MAIN_SESSION_CONTROLLER_ATT);
    return msc.getCurrentUserDetail().getId();
  }

  private String checkout(HttpServletRequest req, VersioningSessionController versioningSC) {
    String documentId = req.getParameter("DocId");
    String userId = getUserId(req);
    try {
      SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId, versioningSC.getComponentId());
      SimpleDocument document = versioningSC.getDocument(documentPK);
      if (document.isReadOnly()) {
        if (userId.equals(document.getEditedBy())) {
          return "ok";
        }
        return "alreadyCheckouted";
      }
      document.setStatus("" + Document.STATUS_CHECKOUTED);
      versioningSC.checkDocumentOut(documentPK, userId);
      document = versioningSC.getDocument(documentPK);
      versioningSC.setEditingDocument(document);
      return "ok";
    } catch (Exception e) {
      SilverTrace.error("versioningPeas", "AjaxServlet.checkout", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String isLocked(HttpServletRequest req, VersioningSessionController versioningSC) {
    String docId = req.getParameter("DocId");
    try {
      SimpleDocumentPK documentPK = new SimpleDocumentPK(docId, versioningSC.getComponentId());
      SimpleDocument document = versioningSC.getEditingDocument();
      if (document == null) {
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
      }
      if (!versioningSC.isDocumentLocked(documentPK)) {
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        return "ok";
      }
      return "locked";
    } catch (Exception e) {
      SilverTrace.error("versioningPeas", "AjaxServlet.checkin", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String checkin(HttpServletRequest req, VersioningSessionController versioningSC) {
    String docId = req.getParameter("DocId");
    boolean force = StringUtil.getBooleanValue(req.getParameter("force_release"));
    try {
      SimpleDocumentPK documentPK = new SimpleDocumentPK(docId, versioningSC.getComponentId());
      SimpleDocument document = versioningSC.getEditingDocument();
      if (document == null) {
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
      }
      if (versioningSC.checkDocumentIn(documentPK, getUserId(req), force)) {
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        return "ok";
      }
      return "locked";
    } catch (Exception e) {
      SilverTrace.error("versioningPeas", "AjaxServlet.checkin", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String deleteAttachment(HttpServletRequest req, VersioningSessionController versioningSC) {
    String id = req.getParameter("Id");
    try {
      versioningSC.deleteDocument(new SimpleDocumentPK(id, versioningSC.getComponentId()));
      return "ok";
    } catch (Exception e) {
      SilverTrace
          .error("versioningPeas", "AjaxServlet.deleteAttachment", "root.MSG_GEN_PARAM_VALUE", e);
      return e.getMessage();
    }
  }

  private String sort(HttpServletRequest req, VersioningSessionController versioningSC) {
    String orderedList = req.getParameter("orderedList");
    String componentId = req.getParameter("ComponentId");
    String[] ids = StringUtil.split(orderedList, ',');
    List<SimpleDocumentPK> pks = new ArrayList<SimpleDocumentPK>(ids.length);
    for(String documentId : ids) {
      pks.add(new SimpleDocumentPK(documentId, componentId));
    }
    versioningSC.sortDocuments(pks);
    return "ok";
  }
}
