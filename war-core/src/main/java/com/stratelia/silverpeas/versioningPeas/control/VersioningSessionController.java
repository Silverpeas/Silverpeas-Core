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
package com.stratelia.silverpeas.versioningPeas.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.CreateException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang3.CharEncoding;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.WebdavServiceFactory;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.attachment.model.UnlockContext;
import org.silverpeas.attachment.model.UnlockOption;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Form;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordSet;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationPK;

/**
 * @author Michael Nikolaenko
 * @version 1.0
 */
public class VersioningSessionController extends AbstractComponentSessionController {

  private final static String APPLICATION_CONTEXT = URLManager.getApplicationURL();
  private static final String DOCUMENT_VERSION_SHOW_VERSIONS_URL = APPLICATION_CONTEXT
      + "/RVersioningPeas/jsp/versions.jsp";
  private static final String DOCUMENT_VERSION_NEW_DOCUMENT_URL = APPLICATION_CONTEXT
      + "/RVersioningPeas/jsp/newDocument.jsp";
  private static final String DOCUMENT_VERSION_NEW_VERSION_URL = APPLICATION_CONTEXT
      + "/RVersioningPeas/jsp/newVersion.jsp";
  private static final String ICON_LOCKED_PATH = APPLICATION_CONTEXT
      + "/util/icons/lock.gif";
  private static final String ICON_UNLOCKED_PATH = APPLICATION_CONTEXT
      + "/util/icons/unlock.gif";
  private static final String DOCUMENT_VERSION_DELETE_DOCUMENT_URL = APPLICATION_CONTEXT
      + "/RVersioningPeas/jsp/deleteDocument.jsp";
  // For Office files direct update
  public final static String NO_UPDATE_MODE = "0";
  public final static String UPDATE_DIRECT_MODE = "1";
  public final static String UPDATE_SHORTCUT_MODE = "2";
  private int creator_id = -1;
  private boolean indexable = true;
  private SimpleDocument document = null;
  private String nodeId = null;
  private boolean topicRightsEnabled = false;
  private String xmlForm = null;
  private AdminController m_AdminCtrl = null;
  private String currentProfile = null;
  public static final String ADMIN = SilverpeasRole.admin.toString();
  public static final String PUBLISHER = SilverpeasRole.publisher.toString();
  public static final String READER = SilverpeasRole.user.toString();
  public static final String WRITER = SilverpeasRole.writer.toString();
  public static final int PUBLIC_VERSION = 0;
  public static final int WORK_VERSION = 1;

  /**
   * To set attributes for UserPanel
   *
   * @param spaceId
   * @param componentId
   * @param spaceLabel
   * @param componentLabel
   */
  public void setAttributesContext(String spaceId, String componentId, String spaceLabel,
      String componentLabel) {
    this.context.setCurrentSpaceId(spaceId);
    this.context.setCurrentComponentId(componentId);
    this.context.setCurrentSpaceName(spaceLabel);
    this.context.setCurrentComponentLabel(componentLabel);
  }

  public void setAttributesContext(String nodeId, boolean topicRightsEnabled) {
    this.nodeId = nodeId;
    this.topicRightsEnabled = topicRightsEnabled;
  }

  /**
   * to set attributes for UserPanel
   *
   * @param spaceId
   * @param componentLabel
   * @param componentId
   * @param spaceLabel
   * @param topicRightsEnabled
   * @param nodeId
   *
   *
   */
  public void setAttributesContext(String spaceId, String componentId,
      String spaceLabel, String componentLabel, String nodeId,
      boolean topicRightsEnabled) {
    this.context.setCurrentSpaceId(spaceId);
    this.context.setCurrentComponentId(componentId);
    this.context.setCurrentSpaceName(spaceLabel);
    this.context.setCurrentComponentLabel(componentLabel);
    this.nodeId = nodeId;
    this.topicRightsEnabled = topicRightsEnabled;
  }

  public void setComponentId(String compomentId) {
    this.context.setCurrentComponentId(compomentId);
  }

  public void setProfile(String profile) {
    currentProfile = profile;
  }

  public String getProfile() {
    return currentProfile;
  }

  public String getNodeId() {
    return nodeId;
  }

  private boolean isTopicRightsEnabled() {
    return topicRightsEnabled;
  }

  /**
   * To generate path to icon of document
   *
   * @param physicalName
   * @return
   */
  public String getDocumentVersionIconPath(String physicalName) {
    return getDocumentVersionIconPath(physicalName, false);
  }

  /**
   * To generate path to icon of document
   *
   * @param physicalName
   * @param isReadOnly
   * @return
   */
  public String getDocumentVersionIconPath(String physicalName,
      boolean isReadOnly) {
    String icon_url = "";
    int pointIndex = physicalName.lastIndexOf('.');
    int theLength = physicalName.length();

    if ((pointIndex >= 0) && ((pointIndex + 1) < theLength)) {
      String fileType = physicalName.substring(pointIndex + 1);
      icon_url = FileRepositoryManager.getFileIcon(fileType, isReadOnly);
    }

    return icon_url;
  }

  /**
   * To generate path to icon for status of document
   *
   * @param status
   * @return
   */
  public String getDocumentVersionStatusIconPath(int status) {
    if (status == 0) {
      return ICON_UNLOCKED_PATH;
    }
    return ICON_LOCKED_PATH;
  }

  /**
   * to get user detail for given user id
   *
   * @return UserDetail
   * @exception
   *
   *
   */
  private UserDetail getUserDetailByID(int user_id) {
    return getOrganizationController().getUserDetail(String.valueOf(user_id));
  }

  /**
   * to get user name for given user id
   *
   * @param user_id
   * @return the user name for given user id
   */
  public String getUserNameByID(int user_id) {
    UserDetail user = getUserDetailByID(user_id);
    String name = user.getFirstName() + " " + user.getLastName();
    return name.trim();
  }

  public String getGroupNameById(int id) {
    Group group = getOrganizationController().getGroup(String.valueOf(id));
    return group.getName();
  }

  /**
   * To get alt message for status icon of document
   *
   * @param msg
   * @param owner_id
   * @param date
   * @param status
   * @return
   */
  public String getDocumentVersionStatusIconAlt(String msg, int owner_id, String date, int status) {
    String message = msg;
    if (msg == null) {
      message = "";
    }

    String name = "";
    if (owner_id >= 0) {
      name = getUserNameByID(owner_id);
    }

    String currentDate = date;
    if (date == null) {
      currentDate = "";
    }

    if (status == 1) {
      return message.trim() + " " + name.trim() + " - " + currentDate.trim();
    }
    return "";
  }

  /**
   * To get url for version
   *
   * @param space
   * @param component_name
   * @param context
   * @param logicalName
   * @param physicalName
   * @param mimeType
   * @return
   * @deprecated
   */
  public String getDocumentVersionURL(String space, String component_name,
      String context, String logicalName, String physicalName, String mimeType) {
    return getDocumentVersionURL(context, logicalName, physicalName, mimeType);
  }

  /**
   * @param context
   * @param logicalName
   * @param physicalName
   * @param mimeType
   * @deprecated
   * @return
   */
  public String getDocumentVersionURL(String context, String logicalName,
      String physicalName, String mimeType) {
    return FileServerUtils.getUrl(null, getComponentId(), logicalName,
        physicalName, mimeType, context);
  }

  public String getDocumentVersionURL(String logicalName, String documentId,
      String versionId) {
    return FileServerUtils.getUrl(getComponentId(), logicalName)
        + "&DocumentId=" + documentId + "&VersionId=" + versionId;
  }

  /**
   * To get path to jsp page for showing versions.
   *
   * @return the path to jsp page for showing versions
   */
  public String getDocumentVersionShowVersionsURL() {
    return DOCUMENT_VERSION_SHOW_VERSIONS_URL;
  }

  /**
   * to get path to jsp page for creating document
   *
   * @return String
   * @exception
   *
   *
   */
  public static String getDocumentVersionNewDocumentURL() {
    return DOCUMENT_VERSION_NEW_DOCUMENT_URL;
  }

  /**
   * to get path to jsp page for creating new version
   *
   * @return String
   * @exception
   *
   *
   */
  public static String getDocumentVersionNewVersionURL() {
    return DOCUMENT_VERSION_NEW_VERSION_URL;
  }

  /**
   * @param pk
   * @return
   * @throws RemoteException
   */
  public int getDocumentCreator(SimpleDocumentPK pk) {
    if (creator_id != -1) {
      return creator_id;
    }
    String creator = AttachmentServiceFactory.getAttachmentService().searchAttachmentById(pk, null)
        .getCreatedBy();
    if (StringUtil.isInteger(creator)) {
      return Integer.parseInt(creator);
    }
    return -1;
  }

  /**
   * Constructor
   *
   * @param mainSessionCtrl
   * @param componentContext
   *
   *
   */
  public VersioningSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.versioningPeas.multilang.versioning", null);
    setComponentRootName(URLManager.CMP_VERSIONINGPEAS);
  }

  /**
   * to get attributes for userpanel
   *
   * @return UserDetail
   *
   *
   */
  public List<String> getAttributeContext() {
    List<String> al = new ArrayList<String>();
    al.add(getSpaceId());
    al.add(getComponentId());
    al.add(getSpaceLabel());
    al.add(getComponentLabel());
    return al;
  }

  /**
   * to get document from DB
   *
   * @param documentPK
   * @return SimpleDocument
   *
   *
   */
  public SimpleDocument getDocument(SimpleDocumentPK documentPK) {
    return AttachmentServiceFactory.getAttachmentService().searchAttachmentById(documentPK, null);
  }

  /**
   * to get all documents for given publication id
   *
   * @param foreignID
   * @return ArrayList
   *
   *
   */
  public List<SimpleDocument> getDocuments(ForeignPK foreignID) {
    return AttachmentServiceFactory.getAttachmentService().searchAttachmentsByExternalObject(
        foreignID, null);
  }

  public List<SimpleDocument> getDocuments(ForeignPK foreignID, boolean mergeGroups) {
    List<SimpleDocument> documents = AttachmentServiceFactory.getAttachmentService().
        searchAttachmentsByExternalObject(foreignID, null);
    for (SimpleDocument doc : documents) {
      setEditingDocument(doc);
    }
    return documents;
  }

  /**
   * to create new document
   *
   * @return DocumentPK
   */
  public SimpleDocumentPK createDocument(SimpleDocument document, File content) {
    SimpleDocument createdDocument = AttachmentServiceFactory.getAttachmentService().
        createAttachment(document, content);
    document.setPK(createdDocument.getPk());
    setEditingDocument(createdDocument);
    if (createdDocument.isPublic()) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_VERSIONING_ADD, Integer.
          parseInt(createdDocument.getCreatedBy()), createdDocument.getInstanceId(),
          createdDocument.getForeignId());
    }
    return createdDocument.getPk();
  }

  /**
   * to add new document version
   *
   * @param newVersion
   * @param in
   * @return the updated document.
   */
  public SimpleDocument addNewDocumentVersion(SimpleDocument newVersion, InputStream in) {
    if (StringUtil.isDefined(newVersion.getUpdatedBy())) {
      newVersion.setUpdatedBy(getUserId());
      newVersion.setUpdated(new Date());
    }
    AttachmentServiceFactory.getAttachmentService().addContent(newVersion, in, isIndexable(),
        true);
    return AttachmentServiceFactory.getAttachmentService().searchAttachmentById(newVersion.getPk(),
        newVersion.getLanguage());
  }

  /**
   * to update document (Save it to DB)
   *
   * @param documentToUpdate
   */
  public void updateDocument(SimpleDocument documentToUpdate) {
    if (StringUtil.isDefined(documentToUpdate.getUpdatedBy())) {
      documentToUpdate.setUpdatedBy(getUserId());
      documentToUpdate.setUpdated(new Date());
    }
    AttachmentServiceFactory.getAttachmentService().
        updateAttachment(documentToUpdate, isIndexable(), true);
  }

  public void checkDocumentOut(SimpleDocumentPK documentPK, String ownerID) throws RemoteException {
    AttachmentServiceFactory.getAttachmentService().lock(documentPK.getId(), ownerID, null);
  }

  /**
   *
   * @param documentPK
   * @param ownerID
   * @param force
   * @return
   */
  public boolean checkDocumentIn(SimpleDocumentPK documentPK, String ownerID, boolean force) {
    UnlockContext context = new UnlockContext(nodeId, ownerID, null);
    context.addOption(UnlockOption.FORCE);
    if (force && getUserDetail("" + ownerID).isAccessAdmin()) {
      context.addOption(UnlockOption.FORCE);
    }
    return AttachmentServiceFactory.getAttachmentService().unlock(context);
  }

  /**
   *
   * @param documentPK
   * @return
   */
  public boolean isDocumentLocked(SimpleDocumentPK documentPK) {
    return !AttachmentServiceFactory.getAttachmentService().searchAttachmentById(documentPK, null)
        .isReadOnly();
  }

  /**
   * to get last public version of document
   *
   * @param documentPK
   * @return SimpleDocument
   */
  public SimpleDocument getLastPublicVersion(SimpleDocumentPK documentPK) {
    return AttachmentServiceFactory.getAttachmentService().searchAttachmentById(documentPK, null)
        .getLastPublicVersion();
  }

  /**
   * to get last version of document
   *
   * @param documentPK
   * @return SimpleDocument
   */
  public SimpleDocument getLastVersion(SimpleDocumentPK documentPK) {
    return AttachmentServiceFactory.getAttachmentService().searchAttachmentById(documentPK, null);
  }

  /**
   * To get all versions of document.
   *
   * @param documentPK
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getDocumentVersions(SimpleDocumentPK documentPK) {
    return ((HistorisedDocument) AttachmentServiceFactory.getAttachmentService()
        .searchAttachmentById(documentPK, null)).getHistory();
  }

  /**
   * To get only public versions of document.
   *
   * @param documentPK
   * @return List<SimpleDocument>
   */
  public List<SimpleDocument> getPublicDocumentVersions(SimpleDocumentPK documentPK) {
    return ((HistorisedDocument) AttachmentServiceFactory.getAttachmentService()
        .searchAttachmentById(documentPK, null)).getPublicVersions();
  }

  /**
   * @param documentPK
   * @param user_id
   * @return
   * @throws RemoteException
   */
  public List<SimpleDocument> getDocumentFilteredVersions(SimpleDocumentPK documentPK, int user_id)
      throws RemoteException {
    // Get all versions for given document
    HistorisedDocument currentDocument = ((HistorisedDocument) AttachmentServiceFactory
        .getAttachmentService().searchAttachmentById(documentPK, null));
    List<SimpleDocument> versions = currentDocument.getHistory();
    List<SimpleDocument> filtered_versions;
    if (user_id < 0 || !useRights()) {
      // If users id is not set, return all versions
      filtered_versions = versions;
    } else {
      // determine has current user writer rights
      boolean is_writer = isWriter(currentDocument, user_id);
      if (!versions.isEmpty()) {
        SimpleDocument version = versions.get(0);
        is_writer = (Integer.parseInt(version.getEditedBy()) == user_id || Integer.parseInt(version
            .getCreatedBy()) == user_id || Integer.parseInt(version.getUpdatedBy()) == user_id
            || ADMIN.equals(getProfile()) || PUBLISHER.equals(getProfile()));
      }
      if (is_writer) {
        filtered_versions = versions;
      } else {
        filtered_versions = currentDocument.getPublicVersions();
      }
    }
    return filtered_versions;
  }

  /**
   * Delete document.
   *
   * @param documentPK
   * @throws RemoteException
   */
  public void deleteDocument(SimpleDocumentPK documentPK) throws RemoteException {
    // Delete specific file rights
    SimpleDocument doc = getDocument(documentPK);
    setEditingDocument(doc);
    AttachmentServiceFactory.getAttachmentService().deleteAttachment(doc, true);
  }

  /**
   * Store in controller the current edited document.
   *
   * @param document
   */
  public void setEditingDocument(SimpleDocument document) {
    this.document = document;
  }

  /**
   * To get current document.
   *
   * @return SimpleDocument
   */
  public SimpleDocument getEditingDocument() {
    return this.document;
  }

  public void setIndexable(boolean indexIt) {
    indexable = indexIt;
  }

  public boolean isIndexable() {
    return indexable;
  }

  /**
   *
   * @param document
   */
  public void setEditingDocumentWithDefaultLists(SimpleDocument document) {
    setEditingDocument(document);
  }

  /**
   * to get user status
   *
   * @param document
   * @param userId
   * @return boolean true if users reader.
   * @throws RemoteException
   */
  public boolean isReader(SimpleDocument document, int userId) throws RemoteException {
    return isReader(document, Integer.toString(userId));
  }

  /**
   * @param document
   * @param userId
   * @return
   * @throws RemoteException
   */
  public boolean isReader(SimpleDocument document, String userId) throws RemoteException {
    if (!useRights()) {
      return true;
    }
    setEditingDocument(document);
    if (isWriter(document, userId)) {
      return true;
    }
    // No specific rights activated on document
    // Check rights according to rights of component (or topic)
    if (isTopicRightsEnabled()) {
      ProfileInst profile = getInheritedProfile(READER);
      if (profile.getObjectId() != -1) {
        // topic have no rights defined (on itself or by its fathers) check if user have access to this component
        return isComponentAvailable(userId);
      }
      return false;
    } else {
      // check if user have access to this component
      return isComponentAvailable(userId);
    }

  }

  private boolean isComponentAvailable(String userId) {
    return getOrganizationController().isComponentAvailable(getComponentId(), userId);
  }

  public boolean isWriter(SimpleDocument document, int userId) throws RemoteException {
    if (!useRights()) {
      return true;
    }
    return isWriter(document, Integer.toString(userId));
  }

  /**
   * Checks if the specified userId is publisher for the current component.
   *
   * @param userId the unique id of the user checked for publisher role.
   * @return true if the user has publisher role - false otherwise.
   */
  public boolean isPublisher(String userId) {
    return isUserInRole(userId, SilverpeasRole.publisher);
  }

  /**
   * Checks if the specified userId is admin for the current component.
   *
   * @param userId the unique id of the user checked for admin role.
   * @return true if the user has admin role - false otherwise.
   */
  public boolean isAdmin(String userId) {
    return isUserInRole(userId, SilverpeasRole.admin);
  }

  public boolean isUserInRole(String userId, SilverpeasRole role) {
    ProfileInst profile = getComponentProfile(role.toString());
    return isUserInRole(userId, profile);
  }

  /**
   * Indicates if the specified user has reading access to the document.
   *
   * @param document the document for which access is checked.
   * @param userId the unique id of the user
   * @return true if the user has access - false otherwise.
   * @throws RemoteException
   */
  public boolean hasAccess(SimpleDocument document, String userId) throws RemoteException {
    if (!useRights()) {
      return true;
    }
    return isReader(document, userId);
  }

  private boolean useRights() {
    return getComponentId() == null || getComponentId().startsWith("kmelia");
  }

  /**
   * @param document
   * @param userId
   * @return
   * @throws RemoteException
   */
  public boolean isWriter(SimpleDocument document, String userId) throws RemoteException {
    setEditingDocument(document);
    // check document profiles
    boolean isWriter = isUserInRole(userId, getCurrentProfile(WRITER));
    if (!isWriter) {
      isWriter = isUserInRole(userId, getCurrentProfile(PUBLISHER));
      if (!isWriter) {
        isWriter = isUserInRole(userId, getCurrentProfile(ADMIN));
      }
    }
    return isWriter;
  }

  private boolean isUserInRole(String userId, ProfileInst profile) {
    SilverTrace.info("versioningPeas",
        "VersioningSessionController.isUserInRole()", "root.MSG_GEN_ENTER_METHOD",
        "document = " + document.getPk().getId() + ", userId = " + userId
        + "profile=" + profile.getName());
    boolean userInRole = false;
    if (profile.getAllUsers() != null) {
      userInRole = profile.getAllUsers().contains(userId);
    }
    if (!userInRole) {
      // check in groups
      List<String> groupsIds = profile.getAllGroups();
      for (String groupId : groupsIds) {
        UserDetail[] users = getOrganizationController().getAllUsersOfGroup(groupId);
        for (UserDetail user : users) {
          if (user != null && user.getId().equals(userId)) {
            return true;
          }
        }
      }
    }
    return userInRole;
  }

  public String getDocumentVersionDeleteDocumentURL() {
    return DOCUMENT_VERSION_DELETE_DOCUMENT_URL;
  }

  public int getCreator_id() {
    return creator_id;
  }

  public void setCreator_id(int creator_id) {
    this.creator_id = creator_id;
  }

  /**
   * @return
   */
  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME,
          NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (RemoteException ex) {
      throw new VersioningRuntimeException("VersioningSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    } catch (CreateException ex) {
      throw new VersioningRuntimeException("VersioningSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    } catch (UtilException ex) {
      throw new VersioningRuntimeException("VersioningSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
    return nodeBm;
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getComponentProfile(String role) {
    ComponentInst componentInst = getAdmin().getComponentInst(getComponentId());
    ProfileInst profile = componentInst.getProfileInst(role);
    ProfileInst inheritedProfile = componentInst.getInheritedProfileInst(role);
    if (inheritedProfile == null) {
      if (profile == null) {
        profile = new ProfileInst();
        profile.setName(role);
      }
    } else {
      if (profile == null) {
        profile = inheritedProfile;
      } else {
        profile.addGroups(inheritedProfile.getAllGroups());
        profile.addUsers(inheritedProfile.getAllUsers());
      }
    }
    return profile;
  }

  /**
   * @param groupIds
   * @return
   */
  public List<Group> groupIds2Groups(List<String> groupIds) {
    List<Group> res = new ArrayList<Group>();
    for (String groupId : groupIds) {
      Group theGroup = getAdmin().getGroupById(groupId);
      if (theGroup != null) {
        res.add(theGroup);
      }
    }
    return res;
  }

  /**
   * @param userIds
   * @return
   */
  public List<String> userIds2Users(List<String> userIds) {
    List<String> res = new ArrayList<String>();
    for (String userId : userIds) {
      UserDetail user = getUserDetail(userId);
      if (user != null) {
        res.add(user.getDisplayedName());
      }
    }
    return res;
  }

  /**
   * @return
   */
  private AdminController getAdmin() {
    if (m_AdminCtrl == null) {
      m_AdminCtrl = new AdminController(getUserId());
    }

    return m_AdminCtrl;
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getCurrentProfile(String role) throws RemoteException {
    // Rights of the node
    if (isTopicRightsEnabled()) {
      NodeDetail nodeDetail = getNodeBm().getDetail(new NodePK(nodeId, getComponentId()));
      if (nodeDetail.haveRights()) {
        return getTopicProfile(role, Integer.toString(nodeDetail.getRightsDependsOn()));
      }
    }
    // Rights of the component
    return getComponentProfile(role);
  }

  public ProfileInst getInheritedProfile(String role) throws RemoteException {
    ProfileInst profileInst;
    // Rights of the node
    if (isTopicRightsEnabled()) {
      NodeDetail nodeDetail = getNodeBm().getDetail(new NodePK(nodeId, getComponentId()));
      if (nodeDetail.haveRights()) {
        profileInst = getTopicProfile(role, Integer.toString(nodeDetail.getRightsDependsOn()));
      } else {
        // Rights of the component
        profileInst = getComponentProfile(role);
      }
    } else {
      // Rights of the component
      profileInst = getComponentProfile(role);
    }
    return profileInst;
  }

  /**
   * @param role
   * @param topicId
   * @return
   */
  public ProfileInst getTopicProfile(String role, String topicId) {
    List<ProfileInst> profiles = getAdmin().getProfilesByObject(topicId, ObjectType.NODE.getCode(),
        getComponentId());
    for (ProfileInst profile : profiles) {
      if (profile.getName().equals(role)) {
        return profile;
      }
    }
    ProfileInst profile = new ProfileInst();
    profile.setName(role);
    return profile;
  }

  /**
   *
   * @param document
   * @param attachmentSettings
   * @throws IOException
   */
  public void saveFileForActify(SimpleDocument document, ResourceLocator attachmentSettings) throws
      IOException {
    String[] extensions = StringUtil.split(attachmentSettings.getString("Actify3dFiles"), ',');

    // 3d native file ?
    boolean fileForActify = false;
    SilverTrace.info("versioningPeas", "VersioningSessionController.saveFileForActify",
        "root.MSG_GEN_PARAM_VALUE", "nb tokenizer =" + extensions.length);
    for (int i = 0; i < extensions.length && !fileForActify; i++) {
      String extension = extensions[i];
      String type = FileRepositoryManager.getFileExtension(document.getFilename());
      fileForActify = type.equalsIgnoreCase(extension);
    }
    if (fileForActify) {
      String dirDestName = "v_" + getComponentId() + "_" + document.getId();
      String actifyWorkingPath = attachmentSettings.getString("ActifyPathSource")
          + File.separatorChar + dirDestName;

      String destPath = FileRepositoryManager.getTemporaryPath() + actifyWorkingPath;
      if (!new File(destPath).exists()) {
        FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);
      }
      File destFile = new File(FileRepositoryManager.getTemporaryPath() + actifyWorkingPath
          + File.separatorChar + document.getFilename());
      AttachmentServiceFactory.getAttachmentService().getBinaryContent(destFile, document
          .getPk(), document.getLanguage());
    }
  }

  public String getXmlForm() {
    return xmlForm;
  }

  public void setXmlForm(String xmlForm) {
    this.xmlForm = xmlForm;
  }

  public void sortDocuments(List<SimpleDocumentPK> pks) {
    AttachmentServiceFactory.getAttachmentService().reorderAttachments(pks);
  }

  public SimpleDocument saveOnline(SimpleDocument document, InputStream in, String comments,
      String radio,
      String userId, boolean force, boolean addXmlForm) throws RemoteException {
    boolean isPublic = DocumentVersion.TYPE_PUBLIC_VERSION == Integer.parseInt(radio);
    document.setPublicDocument(isPublic);
    if (document.isOpenOfficeCompatible()) {
      WebdavServiceFactory.getWebdavService().getUpdatedDocument(document);
    }
    if (addXmlForm) {
      document.setXmlFormId(getXmlForm());
    }
    checkDocumentIn(document.getPk(), userId, force);
    return addNewDocumentVersion(document, in);
  }

  private boolean isXMLFormEmpty(List<FileItem> items) throws PublicationTemplateException,
      FormException {
    boolean isEmpty = true;
    String xmlFormName = getXmlForm();
    if (StringUtil.isDefined(xmlFormName)) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1, xmlFormName
          .indexOf('.'));
      String objectId = "unknown";
      String objectType = "Versioning";

      String externalId = getComponentId() + ":" + objectType + ":" + xmlFormShortName;
      PublicationTemplateManager.getInstance()
          .addDynamicPublicationTemplate(externalId, xmlFormName);
      PublicationTemplate pub = PublicationTemplateManager.getInstance().getPublicationTemplate(
          externalId);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();
      DataRecord data = set.getRecord(objectId);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(objectId);
      }
      PagesContext pagesContext = new PagesContext("myForm", "3", getLanguage(), false,
          getComponentId(), getUserId());
      pagesContext.setObjectId(objectId);
      isEmpty = form.isEmpty(items, data, pagesContext);
    }
    return isEmpty;
  }

  private void saveXMLData(SimpleDocument newVersion, List<FileItem> items) throws FormException,
      PublicationTemplateException {
    String xmlFormName = getXmlForm();
    if (StringUtil.isDefined(xmlFormName) && newVersion != null) {
      String xmlFormShortName = xmlFormName.substring(xmlFormName.indexOf('/') + 1,
          xmlFormName.indexOf('.'));
      String objectId = newVersion.getId();
      String objectType = "Versioning";
      String externalId = getComponentId() + ":" + objectType + ":" + xmlFormShortName;
      // register xmlForm to object
      PublicationTemplateManager.getInstance()
          .addDynamicPublicationTemplate(externalId, xmlFormName);
      PublicationTemplate pub = PublicationTemplateManager.getInstance().getPublicationTemplate(
          externalId);
      RecordSet set = pub.getRecordSet();
      Form form = pub.getUpdateForm();

      DataRecord data = set.getRecord(objectId);
      if (data == null) {
        data = set.getEmptyRecord();
        data.setId(objectId);
      }
      PagesContext pagesContext = new PagesContext("myForm", "3", getLanguage(), false,
          getComponentId(), getUserId());
      pagesContext.setObjectId(objectId);
      form.update(items, data, pagesContext);
      set.save(data);
    }
  }

  public SimpleDocument saveDocument(int versionType, String documentId, String title,
      String description, String publicationId, List<FileItem> items,
      FileItem fileItem) throws Exception {
    SilverTrace.debug("versioningPeas", "VersioningRequestRooter.saveNewDocument()",
        "root.MSG_GEN_ENTER_METHOD");

    SimpleDocumentPK docPK = new SimpleDocumentPK(documentId, getComponentId());
    /*  if (!StringUtil.isDefined(request.getCharacterEncoding())) {
     request.setCharacterEncoding(CharEncoding.UTF_8);
     }
     String encoding = request.getCharacterEncoding();

     List<FileItem> items = FileUploadUtil.parseRequest(request);
     String comments = FileUploadUtil.getParameter(items, "comments", "", encoding);
     int versionType = Integer.parseInt(FileUploadUtil.getParameter(items, "versionType", "0",
     encoding));
     FileItem fileItem = FileUploadUtil.getFile(items, "file_upload");*/
    String filename = fileItem.getName();
    if (filename != null) {
      filename = filename.replace('\\', File.separatorChar);
      filename = filename.replace('/', File.separatorChar);
      filename = filename.substring(filename.lastIndexOf(File.separator) + 1, filename.length());
    }
    String mimeType = FileUtil.getMimeType(filename);
    SimpleDocument documentVersion;
    long size = fileItem.getSize();
    boolean isPublic = versionType == VersioningSessionController.PUBLIC_VERSION;
    boolean update = StringUtil.isDefined(documentId) && !"-1".equals(documentId);
    boolean addXmlForm = !isXMLFormEmpty(items);
    if (update) {
      documentVersion = getDocument(docPK);
      documentVersion.setPublicDocument(isPublic);
      documentVersion.setFilename(filename);
      documentVersion.setSize(size);
      documentVersion.setContentType(mimeType);
      documentVersion.setDescription(description);
      documentVersion.setPublicDocument(isPublic);
      documentVersion.setTitle(title);

      if (addXmlForm) {
        documentVersion.setXmlFormId(getXmlForm());
      }
      documentVersion = addNewDocumentVersion(documentVersion, fileItem.getInputStream());
    } else {
      /*String publicationId = FileUploadUtil.getParameter(items, "publicationId", "-1", encoding);
       String description = FileUploadUtil.getParameter(items, "description", "", encoding);*/
      String xmlFormId = null;
      if (addXmlForm) {
        xmlFormId = getXmlForm();
      }
      documentVersion = new HistorisedDocument(docPK, documentId, -1, getUserId(),
          new SimpleAttachment(filename, null, title, description, size, mimeType, getUserId(),
          new Date(), xmlFormId));
      documentVersion.setPublicDocument(isPublic);
      documentVersion = AttachmentServiceFactory.getAttachmentService().createAttachment(document,
          fileItem.getInputStream());
      /*String name = FileUploadUtil.getParameter(items, "name", "", encoding);
       String publicationId = FileUploadUtil.getParameter(items, "publicationId", "-1", encoding);
       String description = FileUploadUtil.getParameter(items, "description", "", encoding);*/
      setEditingDocument(documentVersion);
    }
    if (addXmlForm) {
      saveXMLData(documentVersion, items);
    }
    // Specific case: 3d file to convert by Actify Publisher
    ResourceLocator attachmentSettings = new ResourceLocator(
        "org.silverpeas.util.attachment.Attachment", "");
    boolean actifyPublisherEnable = attachmentSettings.getBoolean("ActifyPublisherEnable", false);
    if (actifyPublisherEnable) {
      saveFileForActify(documentVersion, attachmentSettings);
    }
    SilverTrace.debug("versioningPeas", "VersioningRequestRooter.saveNewDocument()",
        "root.MSG_GEN_EXIT_METHOD");
    return documentVersion;
  }
}
