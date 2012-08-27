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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.versioningPeas.servlets;

import java.io.IOException;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioningPeas.control.VersioningSessionController;
import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.List;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.WebdavServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;

public class VersioningRequestRouter extends ComponentRequestRouter<VersioningSessionController> {

  private static final long serialVersionUID = 4808952397898736028L;

  @Override
  public VersioningSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new VersioningSessionController(mainSessionCtrl, componentContext);
  }

  @Override
  public String getSessionControlBeanName() {
    return "versioningPeas";
  }

  @Override
  public String getDestination(String function,
      VersioningSessionController versioningSC, HttpServletRequest request) {
    String destination = "";
    SilverTrace.info("versioningPeas",
        "VersioningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "User=" + versioningSC.getUserId() + " Function=" + function);
    String rootDestination = "/versioningPeas/jsp/";
    ResourceLocator messages = new ResourceLocator(
        "com.stratelia.silverpeas.versioningPeas.multilang.versioning",
        versioningSC.getLanguage());
    try {
      String flag = versioningSC.getProfile();

      request.setAttribute("Profile", flag);
      if (function.startsWith("Update")) {
        String docId = request.getParameter("DocId");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String comments = request.getParameter("comments");
        SimpleDocument document = versioningSC.getDocument(new SimpleDocumentPK(docId, versioningSC
            .getComponentId()));
        document.setDescription(description);
        document.setTitle(name);
        document.setDescription(comments);
        versioningSC.updateDocument(document);
        versioningSC.setEditingDocument(document);
        destination = getDestination("ViewVersions", versioningSC, request);
      } else if (function.equals("CloseWindow")) {
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.equals("AddNewVersion")) {
        // Display xmlForm if used
        if (StringUtil.isDefined(versioningSC.getXmlForm())) {
          setXMLFormIntoRequest(request.getParameter("documentId"), versioningSC, request);
        }
        destination = rootDestination + "newVersion.jsp";
      } else if (function.equals("AddNewOnlineVersion")) {
        String documentId = request.getParameter("documentId");

        request.setAttribute("DocumentId", documentId);
        // Display xmlForm if used
        if (StringUtil.isDefined(versioningSC.getXmlForm())) {
          setXMLFormIntoRequest(documentId, versioningSC, request);
        }

        destination = rootDestination + "newOnlineVersion.jsp";
      } else if (function.equals("ListPublicVersionsOfDocument")) {
        String documentId = request.getParameter("DocId");
        String isAlias = request.getParameter("Alias");
        SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId,
            versioningSC.getComponentId());
        SimpleDocument document = versioningSC.getDocument(documentPK);
        List<SimpleDocument> publicVersions = versioningSC.getPublicDocumentVersions(documentPK);

        request.setAttribute("Document", document);
        request.setAttribute("PublicVersions", publicVersions);
        request.setAttribute("Alias", isAlias);
        destination = "/versioningPeas/jsp/publicVersions.jsp";
      } else if ("ViewAllVersions".equals(function)) {
        return viewVersions(request, versioningSC);
      } else if ("saveOnline".equals(function)) {
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding("UTF-8");
        }
        String encoding = request.getCharacterEncoding();
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String documentId = FileUploadUtil.getParameter(items, "documentId", "-1", encoding);
        FileItem fileItem = FileUploadUtil.getFile(request);
        SimpleDocumentPK documentPK =
            new SimpleDocumentPK(documentId, versioningSC.getComponentId());
        SimpleDocument document = versioningSC.getDocument(documentPK);
        String userId = versioningSC.getUserId();
        String radio = FileUploadUtil.getParameter(items, "radio", "", encoding);
        String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);
        boolean force = "true".equalsIgnoreCase(request.getParameter("force_release"));
        String callback = FileUploadUtil.getParameter(items, "Callback");
        request.setAttribute("Callback", callback);
        destination = "/versioningPeas/jsp/documentSaved.jsp";
        boolean addXmlForm = !isXMLFormEmpty(versioningSC, items);
        SimpleDocument newVersion = versioningSC.saveOnline(document, fileItem.getInputStream(),
            comments, radio, userId, force, addXmlForm);
        if (newVersion != null) {
          request.setAttribute("DocumentId", documentId);
          SimpleDocument version = versioningSC.getLastVersion(documentPK);
          request.setAttribute("Version", version);
          if (addXmlForm) {
            saveXMLData(versioningSC, newVersion, items);
          }
        } else {
          if ("admin".equals(versioningSC.getUserRoleLevel())) {
            // TODO MANU ecrire la page pour ressoumettre en forcant
            destination = "/versioningPeas/jsp/forceDocumentLocked.jsp";
          } else {
            destination = "/versioningPeas/jsp/documentLocked.jsp";
          }
        }
      } else if ("Checkout".equals(function)) {
        String documentId = request.getParameter("DocId");
        SimpleDocumentPK documentPK =
            new SimpleDocumentPK(documentId, versioningSC.getComponentId());
        SimpleDocument document = versioningSC.getDocument(documentPK);
        document.setStatus("1");
        versioningSC.checkDocumentOut(documentPK, versioningSC.getUserId());
        document = versioningSC.getDocument(documentPK);
        versioningSC.setEditingDocument(document);
        request.setAttribute("Document", document);
        destination = rootDestination + "versions.jsp";
      } else if ("DeleteDocumentRequest".equals(function)) {
        String documentId = request.getParameter("DocId");
        String url = request.getParameter("Url");
        request.setAttribute("DocId", documentId);
        request.setAttribute("Url", url);
        destination = rootDestination + "deleteDocument.jsp";
      } else if (function.equals("DeleteDocument")) {
        String documentId = request.getParameter("DocId");
        String url = request.getParameter("Url");
        SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId,
            versioningSC.getComponentId());
        versioningSC.deleteDocument(documentPK);
        SilverTrace.info("versioningPeas", "VersioningRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "url=" + url);
        request.setAttribute("urlToReload", url);
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.equals("AddNewDocument")) {
        String pubId = request.getParameter("PubId");
        request.setAttribute("PubId", pubId);
        if (StringUtil.isDefined(versioningSC.getXmlForm())) {
          setXMLFormIntoRequest(null, versioningSC, request);
        }

        destination = rootDestination + "newDocument.jsp";
      } else if (function.equals("SaveNewDocument")) {
        saveNewDocument(request, versioningSC);
        destination = getDestination("ViewVersions", versioningSC, request);
      } else if (function.equals("SaveNewVersion")) {
        if (!StringUtil.isDefined(request.getCharacterEncoding())) {
          request.setCharacterEncoding(CharEncoding.UTF_8);
        }
        String encoding = request.getCharacterEncoding();
        List<FileItem> items = FileUploadUtil.parseRequest(request);
        String documentId = FileUploadUtil.getParameter(items, "documentId", "-1", encoding);
        String componentId = versioningSC.getComponentId();
        SimpleDocumentPK docPK = new SimpleDocumentPK(documentId, componentId);

        SimpleDocument documentVersion = versioningSC.getDocument(docPK);
        if (WebdavServiceFactory.getWebdavService().isNodeLocked(documentVersion)) {
          destination = rootDestination + "documentLocked.jsp";
        } else {
          int versionType = Integer.parseInt(FileUploadUtil.getParameter(items, "radio", "0",
              encoding));
          FileItem fileItem = FileUploadUtil.getFile(items, "file_upload");
          String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);
          versioningSC.saveDocument(versionType, documentId, documentVersion.getTitle(), comments,
              null, items, fileItem);
          String returnURL = FileUploadUtil.getParameter(items, "ReturnURL");
          if (!StringUtil.isDefined(returnURL)) {
            destination = getDestination("ViewVersions", versioningSC, request);
          } else {
            request.setAttribute("urlToReload", returnURL);
            destination = rootDestination + "closeWindow.jsp";
          }
        }
      } else {
        destination = rootDestination + function;
      }
    } catch (Exception e) {
      SilverTrace.error("versioning", "VersioningRequestRouter.getDestination",
          "root.EX_CANT_GET_REQUEST_DESTINATION", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }
    SilverTrace.info("versioningPeas",
        "VersioningRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE",
        "Destination=" + destination);
    return destination;
  }

  /**
   * @param document
   * @param versioningSC
   * @param comments
   * @param radio
   * @param userId
   * @param addXmlForm
   * @return
   * @throws RemoteException
   */
  protected SimpleDocument saveOnline(SimpleDocument document, InputStream in,
      VersioningSessionController versioningSC, String comments, String radio,
      String userId, boolean addXmlForm) throws RemoteException {
    return versioningSC.saveOnline(document, in, comments, radio, userId, false, addXmlForm);
  }

  /**
   * Process request form
   *
   * @param request
   * @param versioningSC
   * @return
   * @throws Exception
   * @throws IOException
   */
  private void saveNewDocument(HttpServletRequest request,
      VersioningSessionController versioningSC) throws Exception {
    SilverTrace.debug("versioningPeas", "VersioningRequestRooter.saveNewDocument()",
        "root.MSG_GEN_ENTER_METHOD");

    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding(CharEncoding.UTF_8);
    }
    String encoding = request.getCharacterEncoding();

    List<FileItem> items = FileUploadUtil.parseRequest(request);
    /*String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);*/
    int versionType = Integer.parseInt(FileUploadUtil.getParameter(items, "versionType", "0",
        encoding));

    FileItem fileItem = FileUploadUtil.getFile(items, "file_upload");
    String name = FileUploadUtil.getParameter(items, "name", "", encoding);
    String publicationId = FileUploadUtil.getParameter(items, "publicationId", "-1", encoding);
    String description = FileUploadUtil.getParameter(items, "description", "", encoding);
    versioningSC.saveDocument(versionType, "-1", name, description, publicationId,
        items, fileItem);
    SilverTrace.debug("versioningPeas", "VersioningRequestRooter.saveNewDocument()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void saveXMLData(VersioningSessionController versioningSC,
      SimpleDocument newVersion, List<FileItem> items) throws FormException,
      PublicationTemplateException {
    String xmlFormName = versioningSC.getXmlForm();
    if (StringUtil.isDefined(xmlFormName) && newVersion != null) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
          xmlFormName.indexOf('.'));
      String objectId = newVersion.getId();
      String objectType = "Versioning";
      String externalId = versioningSC.getComponentId() + ":" + objectType + ":" + xmlFormShortName;
      // register xmlForm to object
      getPublicationTemplateManager().addDynamicPublicationTemplate(externalId, xmlFormName);
      PublicationTemplate pub = getPublicationTemplateManager().getPublicationTemplate(externalId);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();

      DataRecord data = set.getRecord(objectId);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(objectId);
      }
      PagesContext context = new PagesContext("myForm", "3", versioningSC.getLanguage(), false,
          versioningSC.getComponentId(), versioningSC.getUserId());
      context.setObjectId(objectId);
      form.update(items, data, context);
      set.save(data);
    }
  }

  private boolean isXMLFormEmpty(VersioningSessionController versioningSC, List<FileItem> items)
      throws PublicationTemplateException, FormException {
    boolean isEmpty = true;
    String xmlFormName = versioningSC.getXmlForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
          xmlFormName.indexOf('.'));
      String objectId = "unknown";
      String objectType = "Versioning";

      String externalId = versioningSC.getComponentId() + ":" + objectType + ":" + xmlFormShortName;
      // register xmlForm to object
      getPublicationTemplateManager().addDynamicPublicationTemplate(externalId, xmlFormName);

      PublicationTemplate pub = getPublicationTemplateManager().getPublicationTemplate(externalId);

      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();

      DataRecord data = set.getRecord(objectId);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(objectId);
      }

      PagesContext context =
          new PagesContext("myForm", "3", versioningSC.getLanguage(), false, versioningSC.
          getComponentId(), versioningSC.getUserId());
      context.setObjectId(objectId);

      isEmpty = form.isEmpty(items, data, context);
    }
    return isEmpty;
  }

  private void setXMLFormIntoRequest(String documentId,
      VersioningSessionController versioningSC, HttpServletRequest request)
      throws PublicationTemplateException, NumberFormatException, RemoteException, FormException {
    String componentId = versioningSC.getComponentId();
    String objectId = request.getParameter("ObjectId");
    String objectType = "Versioning";
    String xmlFormName = versioningSC.getXmlForm();

    String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName
        .indexOf('.'));

    // register xmlForm to object
    getPublicationTemplateManager().addDynamicPublicationTemplate(componentId + ":"
        + objectType + ":" + xmlFormShortName, xmlFormName);

    PublicationTemplateImpl pubTemplate =
        (PublicationTemplateImpl) getPublicationTemplateManager().getPublicationTemplate(
        componentId
        + ":" + objectType + ":" + xmlFormShortName, xmlFormName);
    Form formUpdate = pubTemplate.getUpdateForm();
    RecordSet recordSet = pubTemplate.getRecordSet();

    if (StringUtil.isDefined(documentId)) {
      // Get last version to display its additional informations instead of
      // blank fields
      SimpleDocument lastVersion = versioningSC.getLastVersion(new SimpleDocumentPK(documentId,
          versioningSC.getComponentId()));
      if (lastVersion != null) {
        objectId = lastVersion.getPk().getId();
      }
    }

    DataRecord data = recordSet.getRecord(objectId);
    if (data == null) {
      data = recordSet.getEmptyRecord();
      data.setId(objectId);
    }

    PagesContext pageContext = new PagesContext("toDefine", "toDefine",
        versioningSC.getLanguage(), false, componentId, versioningSC.getUserId());
    pageContext.setObjectId(objectId);

    request.setAttribute("XMLForm", formUpdate);
    request.setAttribute("XMLData", data);
    request.setAttribute("XMLFormName", xmlFormName);
    request.setAttribute("PagesContext", pageContext);
  }

  private String viewVersions(HttpServletRequest request, VersioningSessionController versioningSC)
      throws RemoteException {
    String documentId = request.getParameter("DocId");
    String isAlias = request.getParameter("Alias");
    SimpleDocumentPK documentPK = new SimpleDocumentPK(documentId, versioningSC.getComponentId());

    SimpleDocument document = versioningSC.getDocument(documentPK);

    List<SimpleDocument> versions = null;
    if ("user".equals(versioningSC.getProfile())) {
      versions = versioningSC.getPublicDocumentVersions(documentPK);
    } else {
      versions = versioningSC.getDocumentVersions(documentPK);
    }
    request.setAttribute("Document", document);
    request.setAttribute("Versions", versions);
    request.setAttribute("Alias", isAlias);
    return "/versioningPeas/jsp/publicVersions.jsp";
  }

  /**
   * Gets an instance of PublicationTemplateManager.
   *
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
}
