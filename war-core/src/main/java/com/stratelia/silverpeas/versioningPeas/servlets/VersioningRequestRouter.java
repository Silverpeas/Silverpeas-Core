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

package com.stratelia.silverpeas.versioningPeas.servlets;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioningPeas.control.VersioningSessionController;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

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
      if (function.startsWith("ViewReadersList")) {
        List<Group> groups = new ArrayList<Group>();
        List<String> users = new ArrayList<String>();
        ProfileInst profile = versioningSC.getCurrentProfile(VersioningSessionController.READER);
        if (profile != null) {
          groups = versioningSC.groupIds2Groups(profile.getAllGroups());
          users = versioningSC.userIds2Users(profile.getAllUsers());
        }
        request.setAttribute("Groups", groups);
        request.setAttribute("Users", users);
        destination = rootDestination + "ReadList.jsp";
      } else if (function.startsWith("ViewWritersList")) {
        Document document = versioningSC.getEditingDocument();

        ProfileInst profile = versioningSC.getCurrentProfile(VersioningSessionController.WRITER);
        ArrayList<Worker> workers = new ArrayList<Worker>();
        if (profile != null) {
          workers = document.getWorkList();
          if (document.getCurrentWorkListOrder() == Integer.parseInt(
              VersioningSessionController.WRITERS_LIST_ORDERED)
              && !versioningSC.isAlreadyMerged()
              && !profile.getAllGroups().isEmpty()) {
            // Need to merge users from groups with other users
            workers = versioningSC.mergeUsersFromGroupsWithWorkers(profile.getAllGroups(), workers);
          }
        }
        request.setAttribute("Workers", workers);
        destination = rootDestination + "WorkList.jsp";
      } else if (function.startsWith("ChangeOrder")) {
        String lines = request.getParameter("lines");
        Document document = versioningSC.getEditingDocument();
        ArrayList<Worker> users = document.getWorkList();
        if (lines != null) {
          int users_count = Integer.parseInt(lines);
          if (users_count == users.size()) {
            ArrayList<Worker> new_users = new ArrayList<Worker>(users_count);

            for (int i = 0; i < users_count; i++) {
              Worker user = users.get(i);
              boolean v_value = false;

              // Validator
              String chvi = request.getParameter("chv" + i);
              if (chvi != null) {
                v_value = true;
              }
              user.setApproval(v_value);
              new_users.add(user);
            }

            // Sorting begin
            int upIndex = Integer.parseInt(request.getParameter("up"));
            int downIndex = Integer.parseInt(request.getParameter("down"));
            int addIndex = Integer.parseInt(request.getParameter("add"));

            // Remove user to change order
            if (upIndex > 0 && upIndex < users_count) {
              Worker user = new_users.remove(upIndex);
              new_users.add(upIndex - 1, user);
            }
            if (downIndex >= 0 && downIndex < users_count - 1) {
              Worker user = new_users.remove(downIndex);
              new_users.add(downIndex + 1, user);
            }

            if (addIndex >= 0 && addIndex < users_count) {
              Worker user = new_users.get(addIndex);
              Worker new_user =
                  new Worker(user.getUserId(), Integer.parseInt(versioningSC.getEditingDocument().
                  getPk().getId()),
                  0, user.isApproval(), true, versioningSC.getComponentId(),
                  user.getType(), user.isSaved(), user.isUsed(), user.getListType());
              new_users.add(addIndex + 1, new_user);
              users_count++;
            }

            for (int i = 0; i < users_count; i++) {
              new_users.get(i).setOrder(i);
            }
            document.setWorkList(new_users);
          }
        }
        destination = getDestination("ViewWritersList", versioningSC, request);
      } else if (function.startsWith("ChangeListType")) {
        Document document = versioningSC.getEditingDocument();
        String listType = request.getParameter("ListType");
        if (!StringUtil.isDefined(listType)) {
          listType = VersioningSessionController.WRITERS_LIST_SIMPLE;
        }
        document.setCurrentWorkListOrder(Integer.parseInt(listType));
        ProfileInst profile = versioningSC.getProfile(VersioningSessionController.WRITER);
        ArrayList<Worker> workers = new ArrayList<Worker>();
        if (profile != null) {
          if (listType.equals(VersioningSessionController.WRITERS_LIST_ORDERED)) {
            // Need to merge users from groups with other users
            workers = document.getWorkList();
            workers = versioningSC.mergeUsersFromGroupsWithWorkers(profile.getAllGroups(), workers);
            versioningSC.setAlreadyMerged(true);
          } else {
            ArrayList<Worker> workersUsers = new ArrayList<Worker>();
            ArrayList<Worker> workersGroups = new ArrayList<Worker>();

            workersGroups = versioningSC.convertGroupsToWorkers(workers,
                profile.getAllGroups());
            workers.addAll(workersGroups);

            workersUsers = versioningSC.convertUsersToWorkers(workers, profile.getAllUsers());
            workers.addAll(workersUsers);
          }
        }
        document.setWorkList(workers);
        versioningSC.updateWorkList(document);
        versioningSC.updateDocument(document);
        destination = getDestination("ViewWritersList", versioningSC, request);
      } else if (function.startsWith("SaveListType")) {
        Document document = versioningSC.getEditingDocument();
        ArrayList<Worker> users = document.getWorkList();
        ArrayList<Worker> updateUsers = new ArrayList<Worker>();
        for (int i = 0; i < users.size(); i++) {
          Worker user = users.get(i);
          // Set approval rights to users
          String chvi = request.getParameter("chv" + i);
          boolean v_value = false;
          if (chvi != null) {
            v_value = true;
          }
          user.setApproval(v_value);
          updateUsers.add(user);
        }
        versioningSC.updateWorkList(document);
        versioningSC.updateDocument(document);
        destination = getDestination("ViewWritersList", versioningSC, request);
      } else if (function.startsWith("ViewVersions")) {
        request.setAttribute("Document", versioningSC.getEditingDocument());
        destination = rootDestination + "versions.jsp";
      } else if (function.equals("SelectUsersGroupsProfileInstance")) {
        String role = request.getParameter("Role");
        String listType = request.getParameter("ListType");
        if (StringUtil.isDefined(listType)) {
          versioningSC.getEditingDocument().setCurrentWorkListOrder(Integer.parseInt(listType));
        }
        versioningSC.initUserPanelInstanceForGroupsUsers(role);
        destination = Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
      } else if (function.startsWith("DocumentProfileSetUsersAndGroups")) {
        String role = request.getParameter("Role");
        ProfileInst profile = versioningSC.getProfile(role);
        versioningSC.updateDocumentProfile(profile);
        if (role.equals(VersioningSessionController.WRITER)) {

          ArrayList<Worker> oldWorkers = versioningSC.getEditingDocument().getWorkList();
          ArrayList<Worker> workers = new ArrayList<Worker>();
          ArrayList<Worker> workersUsers = new ArrayList<Worker>();
          ArrayList<Worker> workersGroups = new ArrayList<Worker>();

          workersGroups = versioningSC.convertGroupsToWorkers(oldWorkers,
              profile.getAllGroups());
          workers.addAll(workersGroups);

          workersUsers = versioningSC.convertUsersToWorkers(oldWorkers, profile.getAllUsers());
          workers.addAll(workersUsers);
          ArrayList<Worker> sortedWorkers = new ArrayList<Worker>();
          if (workers != null) {
            for (int i = 0; i < workers.size(); i++) {
              Worker sortedWorker = workers.get(i);
              sortedWorker.setOrder(i);
              sortedWorkers.add(sortedWorker);
            }
            workers = sortedWorkers;
          }

          versioningSC.getEditingDocument().setWorkList(workers);
          versioningSC.updateWorkList(versioningSC.getEditingDocument());
          request.setAttribute("urlToReload", "ViewWritersList");
        } else {
          request.setAttribute("urlToReload", "ViewReadersList");
        }
        destination = rootDestination + "closeWindow.jsp";
      } else if (function.startsWith("SaveList")) {
        String role = request.getParameter("Role");
        String fromFunction = request.getParameter("From");
        if (versioningSC.isAccessListExist(role)) {
          versioningSC.removeAccessList(role);
        }
        versioningSC.saveAccessList(role);
        request.setAttribute("Message", messages.getString(
            "versioning.ListSaved", ""));
        destination = getDestination(fromFunction, versioningSC, request);
      } else if (function.startsWith("DeleteReaderProfile")) {
        ProfileInst profile = versioningSC.getDocumentProfile(VersioningSessionController.READER);
        if (profile != null) {
          profile.removeAllGroups();
          profile.removeAllUsers();
          versioningSC.updateProfileInst(profile);
        }
        destination = getDestination("ViewReadersList", versioningSC, request);
      } else if (function.startsWith("DeleteWriterProfile")) {
        ProfileInst profile = versioningSC.getDocumentProfile(VersioningSessionController.WRITER);
        if (profile != null) {
          profile.removeAllGroups();
          profile.removeAllUsers();
          versioningSC.updateProfileInst(profile);
        }
        versioningSC.deleteWorkers(true);
        versioningSC.setAlreadyMerged(false);
        destination = getDestination("ViewWritersList", versioningSC, request);
      } else if (function.startsWith("Update")) {
        String docId = request.getParameter("DocId");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String comments = request.getParameter("comments");
        Document document = versioningSC.getDocument(new DocumentPK(Integer.parseInt(docId),
            versioningSC.getComponentId()));
        document.setDescription(description);
        document.setName(name);
        document.setAdditionalInfo(comments);
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
      } else if (function.equals("ChangeValidator")) {
        String setTypeId = request.getParameter("VV");
        String setType = request.getParameter("SetType"); // 'U'for users or 'G'
        // for groups
        versioningSC.setWorkerValidator(versioningSC.getEditingDocument().getWorkList(),
            Integer.parseInt(setTypeId), setType);
        destination = getDestination("ViewWritersList", versioningSC, request);
      } else if (function.equals("ListPublicVersionsOfDocument")) {
        String documentId = request.getParameter("DocId");
        String isAlias = request.getParameter("Alias");
        DocumentPK documentPK = new DocumentPK(Integer.parseInt(documentId),
            versioningSC.getSpaceId(), versioningSC.getComponentId());

        Document document = versioningSC.getDocument(documentPK);
        List<DocumentVersion> publicVersions = versioningSC.getPublicDocumentVersions(documentPK);

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
        DocumentPK documentPK = new DocumentPK(Integer.parseInt(documentId),
            versioningSC.getSpaceId(), versioningSC.getComponentId());
        Document document = versioningSC.getDocument(documentPK);
        String userId = versioningSC.getUserId();
        String radio = FileUploadUtil.getParameter(items, "radio", "", encoding);
        String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);
        boolean force = "true".equalsIgnoreCase(request.getParameter("force_release"));

        String callback = FileUploadUtil.getParameter(items, "Callback");
        request.setAttribute("Callback", callback);
        destination = "/versioningPeas/jsp/documentSaved.jsp";

        boolean addXmlForm = !isXMLFormEmpty(versioningSC, items);

        DocumentVersionPK newVersionPK =
            versioningSC.saveOnline(document, comments, radio, Integer.parseInt(userId), force,
            addXmlForm);
        if (newVersionPK != null) {
          request.setAttribute("DocumentId", documentId);
          DocumentVersion version = versioningSC.getLastVersion(documentPK);
          request.setAttribute("Version", version);
          if (addXmlForm) {
            saveXMLData(versioningSC, newVersionPK, items);
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
        DocumentPK documentPK = new DocumentPK(Integer.parseInt(documentId),
            versioningSC.getSpaceId(), versioningSC.getComponentId());
        Document document = versioningSC.getDocument(documentPK);
        document.setStatus(1);
        document.setLastCheckOutDate(new Date());
        versioningSC.checkDocumentOut(documentPK, Integer.parseInt(versioningSC.getUserId()),
            new Date());
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
        DocumentPK documentPK = new DocumentPK(Integer.parseInt(documentId),
            versioningSC.getSpaceId(), versioningSC.getComponentId());
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
          request.setCharacterEncoding("UTF-8");
        }
        String encoding = request.getCharacterEncoding();
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String type = FileUploadUtil.getParameter(items, "type", "", encoding);
        String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);
        String radio = FileUploadUtil.getParameter(items, "radio", "", encoding);
        String documentId = FileUploadUtil.getParameter(items, "documentId", "-1", encoding);

        // Save file on disk
        FileItem fileItem = FileUploadUtil.getFile(items, "file_upload");
        boolean runOnUnix = !FileUtil.isWindows();
        String logicalName = fileItem.getName();
        String physicalName = "dummy";
        String mimeType = "dummy";
        File dir = null;
        int size = 0;
        if (logicalName != null) {

          if (runOnUnix) {
            logicalName = logicalName.replace('\\', File.separatorChar);
          }

          logicalName =
              logicalName.substring(logicalName.lastIndexOf(File.separator) + 1,
              logicalName.length());
          type = logicalName.substring(logicalName.lastIndexOf(".") + 1,
              logicalName.length());
          physicalName = new Long(new Date().getTime()).toString() + "." + type;
          mimeType = FileUtil.getMimeType(logicalName);
          if (!StringUtil.isDefined(mimeType)) {
            mimeType = "unknown";
          }
          dir = new File(versioningSC.createPath(versioningSC.getComponentId(),
              null)
              + physicalName);
          size = new Long(fileItem.getSize()).intValue();
          fileItem.write(dir);
        }

        // create DocumentVersion
        String componentId = versioningSC.getComponentId();
        DocumentPK docPK = new DocumentPK(Integer.parseInt(documentId), "useless", componentId);
        int userId = Integer.parseInt(versioningSC.getUserId());

        DocumentVersion documentVersion = null;
        DocumentVersion lastVersion = versioningSC.getLastVersion(docPK);
        if (com.stratelia.silverpeas.versioning.ejb.RepositoryHelper.getJcrDocumentService().
            isNodeLocked(lastVersion)) {
          destination = rootDestination + "documentLocked.jsp";
        } else {

          List<DocumentVersion> versions = versioningSC.getDocumentVersions(docPK);
          int majorNumber = 0;
          int minorNumber = 1;
          if (versions != null && versions.size() > 0) {
            documentVersion = versions.get(0);
            majorNumber = documentVersion.getMajorNumber();
            minorNumber = documentVersion.getMinorNumber();
            DocumentVersion newVersion =
                new DocumentVersion(null, docPK, majorNumber, minorNumber, userId, new Date(),
                comments, Integer.parseInt(radio), documentVersion.getStatus(),
                physicalName, logicalName, mimeType, size, componentId);

            boolean addXmlForm = !isXMLFormEmpty(versioningSC, items);
            if (addXmlForm) {
              newVersion.setXmlForm(versioningSC.getXmlForm());
            }

            newVersion = versioningSC.addNewDocumentVersion(newVersion);
            ResourceLocator settings = new ResourceLocator(
                "com.stratelia.webactiv.util.attachment.Attachment", "");
            boolean actifyPublisherEnable = settings.getBoolean("ActifyPublisherEnable", false);
            // Specific case: 3d file to convert by Actify Publisher
            if (actifyPublisherEnable) {
              String extensions = settings.getString("Actify3dFiles");
              StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
              // 3d native file ?
              boolean fileForActify = false;
              SilverTrace.info("versioningPeas", "saveFile.jsp", "root.MSG_GEN_PARAM_VALUE",
                  "nb tokenizer =" + tokenizer.countTokens());
              while (tokenizer.hasMoreTokens() && !fileForActify) {
                String extension = tokenizer.nextToken();
                if (type.equalsIgnoreCase(extension)) {
                  fileForActify = true;
                }
              }
              if (fileForActify) {
                String dirDestName = "v_" + componentId + "_" + documentId;
                String actifyWorkingPath =
                    settings.getString("ActifyPathSource") + File.separator + dirDestName;

                String destPath = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath;
                if (!new File(destPath).exists()) {
                  FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);
                }

                String destFile =
                    FileRepositoryManager.getTemporaryPath() + actifyWorkingPath + File.separator
                    + logicalName;
                FileRepositoryManager.copyFile(versioningSC.createPath(componentId, null)
                    + File.separator + physicalName, destFile);
              }
            }
            if (addXmlForm) {
              saveXMLData(versioningSC, newVersion.getPk(), items);
            }
          }

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
  protected DocumentVersionPK saveOnline(Document document,
      VersioningSessionController versioningSC, String comments, String radio,
      int userId, boolean addXmlForm) throws RemoteException {
    return versioningSC.saveOnline(document, comments, radio, userId, false, addXmlForm);
  }

  /**
   * Process request form
   * @param request
   * @param versioningSC
   * @return
   * @throws Exception
   * @throws IOException
   */
  private void saveNewDocument(HttpServletRequest request,
      VersioningSessionController versioningSC) throws Exception {
    SilverTrace.debug("versioningPeas",
        "VersioningRequestRooter.saveNewDocument()",
        "root.MSG_GEN_ENTER_METHOD");
    int majorNumber = 0;
    int minorNumber = 1;
    String type = "dummy";
    String physicalName = "dummy";
    String logicalName = "dummy";
    String mimeType = "dummy";
    File dir = null;
    int size = 0;
    DocumentPK docPK = new DocumentPK(-1, versioningSC.getComponentId());
    if (!StringUtil.isDefined(request.getCharacterEncoding())) {
      request.setCharacterEncoding("UTF-8");
    }
    String encoding = request.getCharacterEncoding();

    List<FileItem> items = FileUploadUtil.parseRequest(request);
    String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);
    int versionType = Integer.parseInt(FileUploadUtil.getParameter(items, "versionType", "0",
        encoding));

    FileItem fileItem = FileUploadUtil.getFile(items, "file_upload");
    boolean runOnUnix = !FileUtil.isWindows();
    logicalName = fileItem.getName();
    if (logicalName != null) {

      if (runOnUnix) {
        logicalName = logicalName.replace('\\', File.separatorChar);
      }

      logicalName =
          logicalName.substring(logicalName.lastIndexOf(File.separator) + 1, logicalName.length());
      type = logicalName.substring(logicalName.lastIndexOf(".") + 1,
          logicalName.length());
      physicalName = new Long(new Date().getTime()).toString() + "." + type;
      mimeType = FileUtil.getMimeType(logicalName);
      if (!StringUtil.isDefined(mimeType)) {
        mimeType = "unknown";
      }
      dir = new File(versioningSC.createPath(versioningSC.getComponentId(), null) + physicalName);
      size = new Long(fileItem.getSize()).intValue();
      fileItem.write(dir);
    }

    if (versionType == VersioningSessionController.PUBLIC_VERSION) {
      majorNumber = 1;
      minorNumber = 0;
    }
    if (size == 0) {
      majorNumber = 0;
      minorNumber = 0;
    }
    DocumentVersion documentVersion =
        new DocumentVersion(null, docPK, majorNumber, minorNumber, Integer.parseInt(versioningSC.
        getUserId()), new Date(), comments, versionType, DocumentVersion.STATUS_VALIDATION_NOT_REQ,
        physicalName, logicalName, mimeType, size, versioningSC.getComponentId());

    boolean addXmlForm = !isXMLFormEmpty(versioningSC, items);
    if (addXmlForm) {
      documentVersion.setXmlForm(versioningSC.getXmlForm());
    }

    // Document
    docPK = new DocumentPK(-1, versioningSC.getComponentId());

    String name = FileUploadUtil.getParameter(items, "name", "", encoding);
    String publicationId = FileUploadUtil.getParameter(items, "publicationId", "-1", encoding);
    String description = FileUploadUtil.getParameter(items, "description", "", encoding);

    PublicationPK pubPK = new PublicationPK(publicationId, versioningSC.getComponentId());
    Document document = new Document(docPK, pubPK, name, description, Document.STATUS_CHECKINED,
        -1, new Date(), comments, versioningSC.getComponentId(), null, null, 0, Integer.parseInt(
        VersioningSessionController.WRITERS_LIST_SIMPLE));

    String docId = versioningSC.createDocument(document, documentVersion).getId();

    if (addXmlForm) {
      // Save additional informations
      saveXMLData(versioningSC, documentVersion.getPk(), items);
    }

    versioningSC.setEditingDocument(document);
    versioningSC.setFileRights();
    versioningSC.updateWorkList(document);
    versioningSC.updateDocument(document);

    // Specific case: 3d file to convert by Actify Publisher
    ResourceLocator attachmentSettings = new ResourceLocator(
        "com.stratelia.webactiv.util.attachment.Attachment", "");
    boolean actifyPublisherEnable = attachmentSettings.getBoolean(
        "ActifyPublisherEnable", false);
    if (actifyPublisherEnable) {
      versioningSC.saveFileForActify(docId, documentVersion, attachmentSettings);
    }

    SilverTrace.debug("versioningPeas", "VersioningRequestRooter.saveNewDocument()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void saveXMLData(VersioningSessionController versioningSC,
      DocumentVersionPK newVersionPK, List<FileItem> items) throws FormException,
      PublicationTemplateException {
    String xmlFormName = versioningSC.getXmlForm();
    if (StringUtil.isDefined(xmlFormName) && newVersionPK != null) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
          xmlFormName.indexOf('.'));
      String objectId = newVersionPK.getId();
      String objectType = "Versioning";
      String externalId = versioningSC.getComponentId() + ":" + objectType + ":" + xmlFormShortName;

      // register xmlForm to object
      getPublicationTemplateManager().addDynamicPublicationTemplate(externalId,
          xmlFormName);

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

    String xmlFormShortName = xmlFormName.substring(
        xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));

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
      DocumentVersion lastVersion = versioningSC.getLastVersion(new DocumentPK(
          Integer.parseInt(documentId), versioningSC.getComponentId()));
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
    DocumentPK documentPK = new DocumentPK(Integer.parseInt(documentId),
        versioningSC.getSpaceId(), versioningSC.getComponentId());

    Document document = versioningSC.getDocument(documentPK);

    List<DocumentVersion> versions = null;
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
   * @return an instance of PublicationTemplateManager.
   */
  private PublicationTemplateManager getPublicationTemplateManager() {
    return PublicationTemplateManager.getInstance();
  }
}
