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

package com.stratelia.silverpeas.versioningPeas.control;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.ejb.RemoveException;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.versioning.ejb.RepositoryHelper;
import com.stratelia.silverpeas.versioning.ejb.VersioningBm;
import com.stratelia.silverpeas.versioning.ejb.VersioningBmHome;
import com.stratelia.silverpeas.versioning.ejb.VersioningRuntimeException;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.model.Reader;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.ObjectType;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.ProfileInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * @author Michael Nikolaenko
 * @version 1.0
 */
public class VersioningSessionController extends AbstractComponentSessionController {

  private final static ResourceLocator generalSettings = new ResourceLocator(
      "com.stratelia.webactiv.general", "");
  private final static String APPLICATION_CONTEXT = generalSettings.getString("ApplicationURL");
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
  // Versioning options
  public final static String VER_USE_WRITERS_AND_READERS = "0";
  public final static String VER_USE_WRITERS = "1";
  public final static String VER_USE_READERS = "2";
  public final static String VER_USE_NONE = "3";
  private String fileRightsMode = VER_USE_NONE;
  // Type of list in Writer's Panel
  public final static String WRITERS_LIST_SIMPLE = "0";
  public final static String WRITERS_LIST_APPROUVAL = "1";
  public final static String WRITERS_LIST_ORDERED = "2";
  public final static String SET_TYPE_USER = "U";
  public final static String SET_TYPE_GROUP = "G";
  private VersioningBm versioning_bm = null;
  private int creator_id = -1;
  private boolean indexable = true;
  private Document document = null;
  private HashMap<String, Reader> noReaderMap = new HashMap<String, Reader>();
  private String nodeId = null;
  private boolean topicRightsEnabled = false;
  private String xmlForm = null;
  private VersioningUtil versioningUtil = null;
  private AdminController m_AdminCtrl = null;
  private String currentProfile = null;
  public static final String ADMIN = SilverpeasRole.admin.toString();
  public static final String PUBLISHER = SilverpeasRole.publisher.toString();
  public static final String READER = SilverpeasRole.user.toString();
  public static final String WRITER = SilverpeasRole.writer.toString();
  public static final int PUBLIC_VERSION = 0;
  public static final int WORK_VERSION = 1;
  // Groups merged with users (for typeList ordered)
  private boolean alreadyMerged = false;

  /**
   * to initilize the Versioning EJB
   * @exception VersioningRuntimeException when can't initialized the home object of EJB Versioning
   */
  private void initEJB() {
    if (versioning_bm == null) {
      try {
        VersioningBmHome vscEjbHome = EJBUtilitaire.getEJBObjectRef(
            JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
        versioning_bm = vscEjbHome.create();
      } catch (Exception e) {
        throw new VersioningRuntimeException(
            "VersioningSessionController.initEJB",
            SilverTrace.TRACE_LEVEL_ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
  }

  /**
   * To set attributes for UserPanel
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
   * @param spaceId
   * @param componentLabel
   * @param componentId
   * @param spaceLabel
   * @param topicRightsEnabled
   * @param nodeId
   * @author Michael Nikolaenko
   * @version 1.0
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

  public void setFileRightsMode(String mode) {
    this.fileRightsMode = mode;
  }

  public String getFileRightsMode() {
    return fileRightsMode;
  }

  public boolean tabReadersToDisplay() {
    return VER_USE_READERS.equals(fileRightsMode)
        || VER_USE_WRITERS_AND_READERS.equals(fileRightsMode);
  }

  public boolean tabWritersToDisplay() {
    return VER_USE_WRITERS.equals(fileRightsMode)
        || VER_USE_WRITERS_AND_READERS.equals(fileRightsMode);
  }

  /**
   * @param role
   * @return
   * @throws RemoteException
   */
  public boolean isAccessListExist(String role) throws RemoteException {
    return (!getAccessListGroups(role).isEmpty() || !getAccessListUsers(role).isEmpty());
  }

  /**
   * To generate path to icon of document
   * @param physicalName
   * @return
   */
  public String getDocumentVersionIconPath(String physicalName) {
    return getDocumentVersionIconPath(physicalName, false);
  }

  /**
   * To generate path to icon of document
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
   * @return UserDetail
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  private UserDetail getUserDetailByID(int user_id) {
    return getOrganizationController().getUserDetail(String.valueOf(user_id));
  }

  /**
   * to get user name for given user id
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
   * @return the path to jsp page for showing versions
   */
  public String getDocumentVersionShowVersionsURL() {
    return DOCUMENT_VERSION_SHOW_VERSIONS_URL;
  }

  /**
   * to get path to jsp page for creating document
   * @return String
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public static String getDocumentVersionNewDocumentURL() {
    return DOCUMENT_VERSION_NEW_DOCUMENT_URL;
  }

  /**
   * to get path to jsp page for creating new version
   * @return String
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public static String getDocumentVersionNewVersionURL() {
    return DOCUMENT_VERSION_NEW_VERSION_URL;
  }

  /**
   * Create path to version
   * @param spaceId
   * @param componentId
   * @param context
   * @return
   */
  public String createPath(String spaceId, String componentId, String context) {
    return getVersioningUtil().createPath(spaceId, componentId, context);
  }

  /**
   * Create path to version
   * @param componentId
   * @param context
   * @return
   */
  public String createPath(String componentId, String context) {
    return getVersioningUtil().createPath(null, componentId, context);
  }

  /**
   * To get all users for given profile
   * @param document
   * @param nameProfile
   * @return HashMap Stored pair user id and Reader
   */
  public HashMap<String, Reader> getAllUsersForProfile(Document document, String nameProfile) {
    OrganizationController orgCntr = getOrganizationController();

    ComponentInst componentInst = orgCntr.getComponentInst(
        document.getForeignKey().getComponentName());

    HashMap<String, Reader> mapRead = new HashMap<String, Reader>();
    ProfileInst profileInst = null;

    // Get profile instance for given profile
    profileInst = componentInst.getProfileInst(nameProfile);
    if (profileInst != null) {
      ArrayList<String> groupIds = new ArrayList<String>();
      ArrayList<String> userIds = new ArrayList<String>();
      Reader ru = null;
      groupIds.addAll(profileInst.getAllGroups());
      userIds.addAll(profileInst.getAllUsers());

      // For all users of all groups generate UserDetail and then put
      // pair (users id, UsersDetail) to HashTable
      for (String groupId : groupIds) {
        UserDetail[] usersGroup = orgCntr.getAllUsersOfGroup(groupId);
        for (UserDetail anUsersGroup : usersGroup) {
          String userId = anUsersGroup.getId();
          // This check avoids duplicate users
          if (!mapRead.containsKey(userId) && !noReaderMap.containsKey(userId)) {
            UserDetail ud = anUsersGroup;
            ru = new Reader(Integer.parseInt(userId), 0, document.getInstanceId(), 0);
            mapRead.put(ud.getId(), ru);
          }
        }
      }
      // For all users generate UserDetail and then put
      // pair (users id, UsersDetail) to HashTable
      for (String userId : userIds) {
        // This check avoids duplicate users
        if (!mapRead.containsKey(userId) && !noReaderMap.containsKey(userId)) {
          UserDetail ud = orgCntr.getUserDetail(userId);
          ru = new Reader(Integer.parseInt(ud.getId()), 0, null, 0);
          mapRead.put(userId, ru);
        }
      }
    }

    return mapRead;
  }

  /**
   * Get all users with <b>no</b> reader rights.
   * @param document
   * @return
   * @throws RemoteException
   */
  public List<Reader> getAllNoReader(Document document) throws RemoteException {
    noReaderMap.clear();
    noReaderMap.putAll(getAllUsersForProfile(document, PUBLISHER));
    noReaderMap.putAll(getAllUsersForProfile(document, ADMIN));
    ArrayList<Reader> writers = new ArrayList<Reader>();
    writers.addAll(getAllUsersForProfile(document, WRITER).values());
    int creatorId = getDocumentCreator(document.getPk());
    for (Reader user : writers) {
      if (user.getUserId() == creatorId) {
        noReaderMap.put(String.valueOf(creatorId), user);
        break;
      }
    }
    List<Reader> users = new ArrayList<Reader>(noReaderMap.values());
    return users;
  }

  /**
   * @param pk
   * @return
   * @throws RemoteException
   */
  public int getDocumentCreator(DocumentPK pk) throws RemoteException {
    if (creator_id != -1) {
      return creator_id;
    }
    List<DocumentVersion> versions = getDocumentVersions(pk);
    if (versions != null && !versions.isEmpty()) {
      DocumentVersion first_version = versions.get(0);
      return first_version.getAuthorId();
    }
    return -1;
  }

  /**
   * to get versionig EJB
   * @return VersioningBm
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public VersioningBm getVersioningBm() {
    if (versioning_bm == null) {
      initEJB();
    }
    return versioning_bm;
  }

  /**
   * Constructor
   * @param mainSessionCtrl
   * @param componentContext
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public VersioningSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.versioningPeas.multilang.versioning", null);
    setComponentRootName(URLManager.CMP_VERSIONINGPEAS);
  }

  /**
   * to get attributes for userpanel
   * @return UserDetail
   * @author Michael Nikolaenko
   * @version 1.0
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
   * to get all readers
   * @return HashMap
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public Map<String, Reader> getUsersReader() {
    OrganizationController orgCntr = getOrganizationController();
    ComponentInst componentInst = orgCntr.getComponentInst(getComponentId());
    Map<String, Reader> mapRead = new HashMap<String, Reader>();
    ProfileInst profileInst = null;
    // Get profile instance for "user" profile
    profileInst = componentInst.getProfileInst("user");
    if (profileInst != null) {
      List<String> userIds = new ArrayList<String>();
      userIds.addAll(profileInst.getAllUsers());
      // For all userscreate Readers and put them to HashMap
      for (String userId : userIds) {
        if (!mapRead.containsKey(userId) && !noReaderMap.containsKey(userId)) {
          UserDetail ud = orgCntr.getUserDetail(userId);
          Reader ru = new Reader(Integer.parseInt(ud.getId()), Integer.parseInt(document.getPk().
              getId()), document.getInstanceId(), 0);
          mapRead.put(userId, ru);
        }
      }
    }
    return mapRead;
  }

  /**
   * to get document from DB
   * @param documentPK
   * @return Document
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public Document getDocument(DocumentPK documentPK) throws RemoteException {
    initEJB();
    return versioning_bm.getDocument(documentPK);
  }

  /**
   * to get all documents for given publication id
   * @param foreignID
   * @return ArrayList
   * @throws RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public List<Document> getDocuments(ForeignPK foreignID) throws RemoteException {
    return getDocuments(foreignID, true);
  }

  public List<Document> getDocuments(ForeignPK foreignID, boolean mergeGroups)
      throws RemoteException {
    initEJB();
    List<Document> documents = versioning_bm.getDocuments(foreignID);
    for (Document doc : documents) {
      setEditingDocument(doc);
    }
    return documents;
  }

  /**
   * to create new document
   * @return DocumentPK
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public DocumentPK createDocument(Document document, DocumentVersion initialVersion)
      throws RemoteException {
    initEJB();
    DocumentPK documentPK = versioning_bm.createDocument(document, initialVersion);
    document.setPk(documentPK);
    setEditingDocument(document);
    if (initialVersion.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_VERSIONING_ADD, document.getOwnerId(), document
          .
          getForeignKey().getInstanceId(), document.getForeignKey().getId());

      if (isIndexable()) {
        initialVersion.setMajorNumber(1);
        initialVersion.setMinorNumber(0);
        createIndex(document, initialVersion);
      }
    }
    return documentPK;
  }

  /**
   * to add new document version
   * @return DocumentVersion
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public DocumentVersion addNewDocumentVersion(DocumentVersion newVersion) throws RemoteException {
    DocumentVersion version = null;
    initEJB();
    DocumentPK document_pk = newVersion.getDocumentPK();
    Document doc = versioning_bm.getDocument(document_pk);
    version = versioning_bm.addDocumentVersion(doc, newVersion);
    if (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE,
          newVersion.getAuthorId(), document.getForeignKey().getInstanceId(),
          doc.getForeignKey().getId());
      if (isIndexable()) {
        createIndex(doc, version);
      }
    }
    return version;
  }

  /**
   * to update document (Save it to DB)
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void updateDocument(Document documentToUpdate) throws RemoteException {
    initEJB();
    versioning_bm.updateDocument(documentToUpdate);
  }

  public void updateWorkList(Document documentToUpdate) throws RemoteException {
    initEJB();
    versioning_bm.updateWorkList(documentToUpdate);
  }

  /**
   * to check document out
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void checkDocumentOut(DocumentPK documentPK, int ownerID,
      java.util.Date checkOutDate) throws RemoteException {
    initEJB();
    versioning_bm.checkDocumentOut(documentPK, ownerID, checkOutDate);
  }

  /**
   * to check document in
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public boolean checkDocumentIn(DocumentPK documentPK, int user_id, boolean force)
      throws RemoteException {
    initEJB();
    boolean forcing = force && getUserDetail("" + user_id).isAccessAdmin();
    DocumentVersion lastVersion = getLastVersion(documentPK);
    if (forcing || !RepositoryHelper.getJcrDocumentService().isNodeLocked(lastVersion)) {
      versioning_bm.checkDocumentIn(documentPK, user_id);
      return true;
    }
    return false;
  }

  /**
   * to check document in
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public boolean isDocumentLocked(DocumentPK documentPK)
      throws RemoteException {
    initEJB();
    DocumentVersion lastVersion = getLastVersion(documentPK);
    return RepositoryHelper.getJcrDocumentService().isNodeLocked(lastVersion);
  }

  /**
   * to get last public version of document
   * @return DocumentVersion
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public DocumentVersion getLastPublicVersion(DocumentPK documentPK)
      throws RemoteException {
    DocumentVersion version = null;
    initEJB();
    version = versioning_bm.getLastPublicDocumentVersion(documentPK);
    return version;
  }

  /**
   * to get last version of document
   * @return DocumentVersion
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public DocumentVersion getLastVersion(DocumentPK documentPK)
      throws RemoteException {
    DocumentVersion version = null;
    initEJB();
    version = versioning_bm.getLastDocumentVersion(documentPK);
    return version;
  }

  /**
   * to get all versions of document. If user id is set filter will be applied
   * @return ArrayList
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public List<DocumentVersion> getDocumentVersions(DocumentPK documentPK)
      throws RemoteException {
    return getDocumentFilteredVersions(documentPK, -1);
  }

  /**
   * to get only public versions of document.
   * @return ArrayList
   * @author Nicolas Eysseric
   * @version 3.0
   */
  public List<DocumentVersion> getPublicDocumentVersions(DocumentPK documentPK)
      throws RemoteException {
    initEJB();
    List<DocumentVersion> publicVersions = versioning_bm.getAllPublicDocumentVersions(documentPK);
    return publicVersions;
  }

  /**
   * @param documentPK
   * @param user_id
   * @return
   * @throws RemoteException
   */
  public List<DocumentVersion> getDocumentFilteredVersions(
      DocumentPK documentPK, int user_id) throws RemoteException {
    initEJB();
    // Get all versions for given document
    ArrayList<DocumentVersion> versions = versioning_bm.getDocumentVersions(documentPK);
    List<DocumentVersion> filtered_versions = new ArrayList<DocumentVersion>(versions.size());
    if (user_id < 0 || !useRights()) {
      // If users id is not set, return all versions
      filtered_versions = versions;
    } else {
      // Filter versions
      // determine has current user writer rights
      Document doc = versioning_bm.getDocument(documentPK);
      boolean is_writer = isWriter(doc, user_id);
      if (versions != null) {
        DocumentVersion version = versions.get(0);
        is_writer = (version.getAuthorId() == user_id || ADMIN.equals(getProfile())
            || PUBLISHER.equals(getProfile()));
      }
      if (is_writer) {
        // If writer - return all versions
        filtered_versions = versions;
      } else {
        // Filter versions (select only public ones)
        for (DocumentVersion version : versions) {
          if (version != null) {
            if (version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
              filtered_versions.add(version);
            }
          }
        }
      }
    }
    return filtered_versions;
  }

  /**
   * to get one version of a document.
   * @return DocumentVersion
   * @author David Lesimple
   * @version 3.0
   */
  public DocumentVersion getDocumentVersion(DocumentVersionPK docVersionPK)
      throws RemoteException {
    initEJB();
    DocumentVersion docVersion = versioning_bm.getDocumentVersion(docVersionPK);
    return docVersion;
  }

  /**
   * to validate document
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void validateDocument(DocumentPK documentPK, int validatorID, String comment,
      Date validationDate) throws RemoteException {
    initEJB();
    Document theDocument = versioning_bm.getDocument(documentPK);
    versioning_bm.validateDocument(theDocument, validatorID, comment,
        validationDate);
  }

  /**
   * to delete document
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void deleteDocument(DocumentPK documentPK) throws RemoteException {
    // Delete specific file rights
    Document doc = getDocument(documentPK);
    setEditingDocument(doc);

    ProfileInst profile = getDocumentProfile(VersioningSessionController.WRITER);
    if (profile != null) {
      if (ObjectType.DOCUMENT.equals(profile.getObjectType())
          && profile.getObjectId() == Integer.parseInt(doc.getPk().getId())) {
        profile.removeAllGroups();
        profile.removeAllUsers();
        updateProfileInst(profile);
        deleteRole(profile.getId());
      }
    }
    initEJB();
    versioning_bm.deleteDocument(documentPK);
    CallBackManager callBackManager = CallBackManager.get();
    callBackManager.invoke(CallBackManager.ACTION_VERSIONING_REMOVE, doc.getOwnerId(), doc.
        getForeignKey().getInstanceId(), doc);
  }

  /**
   * to refuse document
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void refuseDocument(DocumentPK documentPK, int validatorID, String comment,
      Date validationDate) throws RemoteException {
    initEJB();
    Document doc = versioning_bm.getDocument(documentPK);
    versioning_bm.refuseDocument(doc, validatorID, comment, validationDate);
  }

  /**
   * to create index for search engeen
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  private void createIndex(Document documentToIndex, DocumentVersion lastVersion)
      throws RemoteException {
    getVersioningUtil().createIndex(documentToIndex, lastVersion);
  }

  /**
   * to store in controller editing document
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void setEditingDocument(Document document) {
    this.document = document;
  }

  /**
   * to get current document
   * @return Document
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public Document getEditingDocument() {
    return this.document;
  }

  public void setIndexable(boolean indexIt) {
    indexable = indexIt;
  }

  public boolean isIndexable() {
    return indexable;
  }

  /**
   * to store in controller editing document and set lists of readers and workers
   * @exception RemoteException
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public void setEditingDocumentWithDefaultLists(Document document)
      throws RemoteException {
    setEditingDocument(document);
  }

  /**
   * to get user status
   * @return boolean true if users reader
   * @exception
   * @author Michael Nikolaenko
   * @version 1.0
   */
  public boolean isReader(Document document, int userId) throws RemoteException {
    return isReader(document, Integer.toString(userId));
  }

  /**
   * @param document
   * @param userId
   * @return
   * @throws RemoteException
   */
  public boolean isReader(Document document, String userId) throws RemoteException {
    if (!useRights()) {
      return true;
    }
    ProfileInst profile = null;
    setEditingDocument(document);
    boolean isReader = false;
    if (isWriter(document, userId)) {
      return true;
    }

    if (VER_USE_NONE.equals(fileRightsMode)) {
      // No specific rights activated on document
      // Check rights according to rights of component (or topic)
      if (isTopicRightsEnabled()) {
        profile = getInheritedProfile(READER);
        if (profile.getObjectId() != -1) {
          // topic have no rights defined (on itself or by its fathers)
          // check if user have access to this component
          return isComponentAvailable(userId);
        }
      } else {
        // check if user have access to this component
        return isComponentAvailable(userId);
      }
    } else if (VER_USE_WRITERS.equals(fileRightsMode)) {
      profile = getInheritedProfile(READER);
    } else {
      profile = getCurrentProfile(READER);
      if (profile.getObjectType() == null) {
        // profile is from the application (no rights specified on document, nor on topic)
        return isComponentAvailable(userId);
      }
    }

    SilverTrace.info("versioningPeas",
        "VersioningSessionController.isReader()", "root.MSG_GEN_ENTER_METHOD",
        "document = " + document.getPk().getId() + ", userId = " + userId
        + "profile=" + getProfile());
    if (profile == null) {
      return false;
    }

    if (profile.getAllUsers() != null) {
      isReader = profile.getAllUsers().contains(userId);
    }
    if (!isReader) {
      for (String groupId : profile.getAllGroups()) {
        UserDetail[] users = getOrganizationController().getAllUsersOfGroup(groupId);
        for (UserDetail user : users) {
          if (user != null && user.getId().equals(userId)) {
            return true;
          }
        }
      }
    }
    return isReader;
  }

  private boolean isComponentAvailable(String userId) {
    return getOrganizationController().isComponentAvailable(getComponentId(), userId);
  }

  public boolean isWriter(Document document, int userId) throws RemoteException {
    if (!useRights()) {
      return true;
    }
    return isWriter(document, Integer.toString(userId));
  }

  /**
   * Checks if the specified userId is publisher for the current component.
   * @param userId the unique id of the user checked for publisher role.
   * @return true if the user has publisher role - false otherwise.
   */
  public boolean isPublisher(String userId) {
    return isUserInRole(userId, SilverpeasRole.publisher);
  }

  /**
   * Checks if the specified userId is admin for the current component.
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
   * @param document the document for which access is checked.
   * @param userId the unique id of the user
   * @return true if the user has access - false otherwise.
   * @throws RemoteException
   */
  public boolean hasAccess(Document document, String userId)
      throws RemoteException {
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
  public boolean isWriter(Document document, String userId)
      throws RemoteException {
    boolean isWriter = false;
    setEditingDocument(document);

    if (VER_USE_NONE.equals(fileRightsMode) || VER_USE_READERS.equals(fileRightsMode)) {
      // check component profiles or topic profiles (kmelia case with rights on topic)
      isWriter = isUserInRole(userId, getInheritedProfile(WRITER));
      if (!isWriter) {
        isWriter = isUserInRole(userId, getInheritedProfile(PUBLISHER));
        if (!isWriter) {
          isWriter = isUserInRole(userId, getInheritedProfile(ADMIN));
        }
      }
    } else {
      // check document profiles
      isWriter = isUserInRole(userId, getCurrentProfile(WRITER));
      if (!isWriter) {
        isWriter = isUserInRole(userId, getCurrentProfile(PUBLISHER));
        if (!isWriter) {
          isWriter = isUserInRole(userId, getCurrentProfile(ADMIN));
        }
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

  /**
   * @param role
   * @return
   * @throws RemoteException
   */
  public String initUserPanelInstanceForGroupsUsers(String role)
      throws RemoteException {
    String documentId = getEditingDocument().getPk().getId();
    SilverTrace.info("versioningPeas",
        "VersioningController.initUserPanelInstanceForGroupsUsers()",
        "root.MSG_GEN_ENTER_METHOD", "role = " + role + ", componentId = "
        + getComponentId() + ", documentId = " + documentId);
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    PairObject[] hostPath = new PairObject[1];

    Selection sel = getSelection();
    sel.resetAll();
    sel.setSetSelectable(true);
    if (role.equals(WRITER)) {
      hostPath[0] = new PairObject(getString("versioning.SelectWriters"), "");
      if (Integer.toString(getEditingDocument().getCurrentWorkListOrder()).equals(
          WRITERS_LIST_ORDERED)) {
        sel.setSetSelectable(false);
      }
    } else {
      hostPath[0] = new PairObject(getString("versioning.SelectReaders"), "");
    }

    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentName(new PairObject(getComponentLabel(), ""));
    sel.setHostPath(hostPath);

    String hostUrl = m_context
        + "/RVersioningPeas/jsp/DocumentProfileSetUsersAndGroups?Role=" + role
        + "&DocumentId=" + documentId;
    String cancelUrl = m_context + "/RVersioningPeas/jsp/CloseWindow";

    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(cancelUrl);
    SelectionUsersGroups sug = new SelectionUsersGroups();
    sug.setComponentId(getComponentId());

    if (!StringUtil.isDefined(getNodeId())) {
      // Selectable users and groups are component's ones.
      ArrayList<String> profileNames = new ArrayList<String>();
      profileNames.add(ADMIN);
      profileNames.add(PUBLISHER);
      profileNames.add(role);
      if (READER.equals(role)) {
        profileNames.add(WRITER);
      }
      sug.setProfileNames(profileNames);
    } else {
      NodeDetail node = getNodeBm().getHeader(new NodePK(getNodeId(), getComponentId()));
      if (node.haveLocalRights()) {
        // Selectable users and groups are topic's ones.
        ProfileInst topicProfile = getTopicProfile(ADMIN, getNodeId());
        sug.addProfileId(topicProfile.getId());
        topicProfile = getTopicProfile(PUBLISHER, getNodeId());
        sug.addProfileId(topicProfile.getId());
        topicProfile = getTopicProfile(WRITER, getNodeId());
        sug.addProfileId(topicProfile.getId());
        if (role.equals(READER)) {
          topicProfile = getTopicProfile(READER, getNodeId());
          sug.addProfileId(topicProfile.getId());
        }
      } else {
        // Selectable users and groups are topic's ones.
        ProfileInst topicProfile = getInheritedProfile(ADMIN);
        sug.addProfileId(topicProfile.getId());
        topicProfile = getInheritedProfile(PUBLISHER);
        sug.addProfileId(topicProfile.getId());
        topicProfile = getInheritedProfile(WRITER);
        sug.addProfileId(topicProfile.getId());
        if (role.equals(READER)) {
          topicProfile = getInheritedProfile(READER);
          sug.addProfileId(topicProfile.getId());
        }
      }
    }
    sel.setExtraParams(sug);
    ProfileInst fileProfile = getCurrentProfile(role);

    if (fileProfile != null) {
      List<String> users = fileProfile.getAllUsers();
      sel.setSelectedElements(users.toArray(new String[users.size()]));
      if (READER.equals(role) || !WRITERS_LIST_ORDERED.equals(String.valueOf(
          getEditingDocument().getCurrentWorkListOrder()))) {
        List<String> groups = fileProfile.getAllGroups();
        sel.setSelectedSets(groups.toArray(new String[groups.size()]));
      }
    }
    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
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

  private VersioningUtil getVersioningUtil() {
    if (versioningUtil == null) {
      versioningUtil = new VersioningUtil();
    }
    return versioningUtil;
  }

  @Override
  public void close() {
    try {
      if (versioning_bm != null) {
        versioning_bm.remove();
      }
    } catch (RemoteException e) {
      SilverTrace.error("versioningSession",
          "VersioningSessionController.close", "", e);
    } catch (RemoveException e) {
      SilverTrace.error("versioningSession",
          "VersioningSessionController.close", "", e);
    }
  }

  /**
   * @return
   */
  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return nodeBm;
  }

  /**
   * @param profiles
   * @param role
   * @return
   */
  private ProfileInst getProfile(List<ProfileInst> profiles, String role) {
    for (ProfileInst profile : profiles) {
      if (role.equals(profile.getName())) {
        return profile;
      }
    }
    return null;
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getProfile(String role) throws RemoteException {
    ProfileInst profile = getDocumentProfile(role);
    return profile;
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
   * @param profileId
   * @throws RemoteException
   */
  public void deleteRole(String profileId) throws RemoteException {
    // Remove the profile
    getAdmin().deleteProfileInst(profileId);
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
   * @param profile
   * @throws RemoteException
   */
  public void updateDocumentProfile(ProfileInst profile) throws RemoteException {
    profile.removeAllGroups();
    profile.removeAllUsers();
    profile.setGroupsAndUsers(getSelection().getSelectedSets(), getSelection()
        .getSelectedElements());
    updateProfileInst(profile);
  }

  public void updateProfileInst(ProfileInst profile) {
    getAdmin().updateProfileInst(profile);
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getDocumentProfile(String role) throws RemoteException {
    ProfileInst profileInst = null;
    String documentId = getEditingDocument().getPk().getId();
    List<ProfileInst> profiles = getAdmin().getProfilesByObject(documentId,
        ObjectType.DOCUMENT.getCode(), getComponentId());
    if (profiles != null && !profiles.isEmpty()) {
      if (!profiles.isEmpty()) {
        profileInst = getProfile(profiles, role);
      }
    }
    if (profileInst == null) {
      profileInst = new ProfileInst();
      profileInst.setObjectType(ObjectType.DOCUMENT.getCode());
      profileInst.setObjectId(Integer.parseInt(documentId));
      profileInst.setComponentFatherId(getComponentId());
      profileInst.setName(role);
      getAdmin().addProfileInst(profileInst);
    }
    return profileInst;
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getCurrentProfile(String role) throws RemoteException {
    ProfileInst profileInst = null;
    String documentId = getEditingDocument().getPk().getId();
    List<ProfileInst> profiles = getAdmin().getProfilesByObject(documentId,
        ObjectType.DOCUMENT.getCode(), getComponentId());
    if (profiles != null && !profiles.isEmpty()) {
      profileInst = getProfile(profiles, role);
    }
    if (profileInst != null) {
      // Rights by file
      return profileInst;
    }
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
    ProfileInst profileInst = null;
    // Rights of the node
    if (isTopicRightsEnabled()) {
      NodeDetail nodeDetail = getNodeBm().getDetail(
          new NodePK(nodeId, getComponentId()));
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
    List<ProfileInst> profiles = getAdmin().getProfilesByObject(topicId,
        ObjectType.NODE.getCode(), getComponentId());
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
   * @param role
   * @throws RemoteException
   */
  public void saveAccessList(String role) throws RemoteException {
    ProfileInst profileInst = getDocumentProfile(role);
    if (profileInst != null) {
      ArrayList<String> groupsIds = profileInst.getAllGroups();
      ArrayList<String> usersIds = profileInst.getAllUsers();
      if (READER.equals(role)) {
        getVersioningBm().saveReadersAccessList(getComponentId(), groupsIds,
            usersIds);
      } else {
        getVersioningBm().saveWorkersAccessList(getComponentId(),
            getEditingDocument().getPk().getId(),
            getEditingDocument().getCurrentWorkListOrder());
      }
    }
  }

  /**
   * @param role
   * @throws RemoteException
   */
  public void removeAccessList(String role) throws RemoteException {
    if (role.equals(READER)) {
      getVersioningBm().removeReadersAccessList(getComponentId());
    } else {
      getVersioningBm().removeWorkersAccessList(getComponentId());
    }
  }

  /**
   * @param role
   * @throws RemoteException
   */
  public List getAccessListUsers(String role) throws RemoteException {
    if (READER.equals(role)) {
      return getVersioningBm().getReadersAccessListUsers(getComponentId());
    }
    return getVersioningBm().getWorkersAccessListUsers(getComponentId());
  }

  /**
   * @param role
   * @return
   * @throws RemoteException
   */
  private List getAccessListGroups(String role) throws RemoteException {
    if (READER.equals(role)) {
      return getVersioningBm().getReadersAccessListGroups(getComponentId());
    }
    return getVersioningBm().getWorkersAccessListGroups(getComponentId());
  }

  /**
   * @throws RemoteException
   */
  @SuppressWarnings("unchecked")
  public void setFileRights() throws RemoteException {
    Document doc = getEditingDocument();

    // Set file rights in admin meaning
    setFileRights(READER);
    setFileRights(WRITER);

    // Set file rights in versioning rights
    ArrayList<Worker> workers = new ArrayList<Worker>();
    List<Worker> workersUsers = new ArrayList<Worker>();
    List<Worker> workersGroups = new ArrayList<Worker>();
    doc.setCurrentWorkListOrder(getSavedListType());

    if (isAccessListExist(WRITER)) {
      List<Worker> savedWorkersGroups = getAccessListGroups(WRITER);
      List<Worker> savedWorkersUsers = getAccessListUsers(WRITER);

      for (Worker savedWorkerGroup : savedWorkersGroups) {
        Worker newWorkerGroup = (Worker) savedWorkerGroup.clone();
        newWorkerGroup.setDocumentId(Integer.parseInt(doc.getPk().getId()));
        newWorkerGroup.setSaved(false);
        workersGroups.add(newWorkerGroup);
      }
      workers.addAll(workersGroups);

      for (Worker savedWorkerUser : savedWorkersUsers) {
        Worker newWorkerUser = (Worker) savedWorkerUser.clone();
        newWorkerUser.setDocumentId(Integer.parseInt(doc.getPk().getId()));
        newWorkerUser.setSaved(false);
        workersUsers.add(newWorkerUser);
      }
      workers.addAll(workersUsers);
      doc.setWorkList(workers);
    }
  }

  /**
   * @param role
   * @throws RemoteException
   */
  @SuppressWarnings("unchecked")
  private void setFileRights(String role) throws RemoteException {
    // Access file saved rights
    Selection sel = getSelection();
    if (isAccessListExist(role)) {
      if (role.equals(READER)) {
        // We only get Ids
        List<String> usersAuthorized = getAccessListUsers(role);
        List<String> groupsAuthorized = getAccessListGroups(role);
        sel.setSelectedElements(usersAuthorized.toArray(new String[usersAuthorized.size()]));
        sel.setSelectedSets(groupsAuthorized.toArray(new String[groupsAuthorized.size()]));
      } else {
        List<Worker> workersUsers = getAccessListUsers(WRITER);
        List<Worker> workersGroups = getAccessListGroups(WRITER);

        String[] usersIds = new String[workersUsers.size()];
        String[] groupsIds = new String[workersGroups.size()];
        // Extract only ids from Workers ArrayList
        int i = 0;
        for (Worker workerUser : workersUsers) {
          usersIds[i++] = String.valueOf(workerUser.getId());
        }

        i = 0;
        for (Worker workerGroup : workersGroups) {
          groupsIds[i++] = String.valueOf(workerGroup.getId());
        }
        sel.setSelectedElements(usersIds);
        sel.setSelectedSets(groupsIds);
      }
      updateDocumentProfile(getDocumentProfile(role));
    }
  }

  /**
   * @param docId
   * @param docVersion
   * @param attachmentSettings
   */
  public void saveFileForActify(String docId, DocumentVersion docVersion,
      ResourceLocator attachmentSettings) throws IOException, Exception {
    String extensions = attachmentSettings.getString("Actify3dFiles");
    StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
    // 3d native file ?
    boolean fileForActify = false;
    SilverTrace.info("versioningPeas",
        "VersioningSessionController.saveFileForActify",
        "root.MSG_GEN_PARAM_VALUE", "nb tokenizer =" + tokenizer.countTokens());
    while (tokenizer.hasMoreTokens() && !fileForActify) {
      String extension = tokenizer.nextToken();
      String type = FileRepositoryManager.getFileExtension(docVersion.getLogicalName());
      if (type.equalsIgnoreCase(extension)) {
        fileForActify = true;
      }
    }
    if (fileForActify) {
      String dirDestName = "v_" + getComponentId() + "_" + docId;
      String actifyWorkingPath = attachmentSettings.getString("ActifyPathSource")
          + File.separator + dirDestName;

      String destPath = FileRepositoryManager.getTemporaryPath()
          + actifyWorkingPath;
      if (!new File(destPath).exists()) {
        FileRepositoryManager.createGlobalTempPath(actifyWorkingPath);
      }

      String destFile = FileRepositoryManager.getTemporaryPath()
          + actifyWorkingPath + File.separator + docVersion.getLogicalName();
      FileRepositoryManager.copyFile(createPath(getComponentId(), null)
          + File.separator + docVersion.getPhysicalName(), destFile);
    }
  }

  /**
   * @param workers
   * @param usersIds
   * @return
   */
  public ArrayList<Worker> convertUsersToWorkers(List<Worker> workers, List<String> usersIds) {
    // For all users generate Workers and put them to HashMap
    TreeMap<String, Worker> mapWorkers = new TreeMap<String, Worker>();
    TreeMap<String, Worker> mapCurrentWorkers = new TreeMap<String, Worker>();

    UserDetail userDetail = null;

    ArrayList<Worker> newListWorkers = new ArrayList<Worker>();

    // Remove workers (users) not in usersIds
    for (Worker worker : workers) {
      if (usersIds.contains(String.valueOf(worker.getId()))
          && worker.getType().equals(SET_TYPE_USER) && worker.isUsed()) {
        mapCurrentWorkers.put(
            worker.getType() + String.valueOf(worker.getId()), worker);
      }
    }
    newListWorkers.addAll(mapCurrentWorkers.values());

    // Add new workers (users) from usersIds
    boolean isUsed = true;
    boolean isSaved = false;
    int lastOrder = workers.size();
    Worker worker = null;
    for (String userId : usersIds) {
      userDetail = getOrganizationController().getUserDetail(userId);
      if (!mapCurrentWorkers.containsKey(SET_TYPE_USER + userDetail.getId())) {
        worker = new Worker(Integer.parseInt(userDetail.getId()), 0,
            lastOrder++, false, true, document.getInstanceId(), SET_TYPE_USER,
            isSaved, isUsed, document.getCurrentWorkListOrder());
        mapWorkers.put(SET_TYPE_USER + userDetail.getId(), worker);
      }
    }
    newListWorkers.addAll(mapWorkers.values());
    return newListWorkers;
  }

  /**
   * @param workers
   * @param groupIds
   * @return
   */
  public ArrayList<Worker> convertGroupsToWorkers(List<Worker> workers, List<String> groupIds) {
    // For all users generate Workers and put them to HashMap
    TreeMap<String, Worker> mapWork = new TreeMap<String, Worker>();
    TreeMap<String, Worker> mapCurrentWorkers = new TreeMap<String, Worker>();

    ArrayList<Worker> newListWorkers = new ArrayList<Worker>();

    // Remove workers (groups) not in groupIds
    for (Worker worker : workers) {
      if (groupIds.contains(String.valueOf(worker.getId()))
          && worker.getType().equals(SET_TYPE_GROUP) && worker.isUsed()) {
        mapCurrentWorkers.put(
            worker.getType() + String.valueOf(worker.getId()), worker);
      }
    }
    newListWorkers.addAll(mapCurrentWorkers.values());

    Worker worker = null;
    Group groupDetail = null;

    // Add new workers (groups) from groupIds
    boolean isUsed = true;
    boolean isSaved = false;
    int lastOrder = workers.size();
    for (String groupId : groupIds) {
      groupDetail = getOrganizationController().getGroup(groupId);
      if (!mapCurrentWorkers.containsKey(SET_TYPE_GROUP + groupDetail.getId())) {
        worker = new Worker(Integer.parseInt(groupDetail.getId()), lastOrder++,
            0, false, true, document.getInstanceId(), SET_TYPE_GROUP, isSaved,
            isUsed, document.getCurrentWorkListOrder());
        mapWork.put(SET_TYPE_GROUP + groupDetail.getId(), worker);
      }
    }
    newListWorkers.addAll(mapWork.values());
    return newListWorkers;
  }

  /**
   * @param groups
   * @param workers
   * @return workers
   */
  public ArrayList<Worker> mergeUsersFromGroupsWithWorkers(List groups, List<Worker> workers) {
    ArrayList<Worker> mergedWorkers = new ArrayList<Worker>();

    TreeMap<Integer, Worker> mapWork = new TreeMap<Integer, Worker>();

    int lastIndexWorkers = workers.size();

    // 1. Keep users from Workers
    for (Worker worker : workers) {
      if (worker.getType().equals(SET_TYPE_USER)) {
        mapWork.put(worker.getId(), worker);
      } else {
        // Extract users from group
        UserDetail[] usersFromGroup = getOrganizationController().getAllUsersOfGroup(Integer.
            toString(worker.getId()));
        int i = 0;
        Integer userId = null;
        while (i < usersFromGroup.length) {
          userId = Integer.getInteger(usersFromGroup[i].getId());
          if (!mapWork.containsKey(userId)) {
            Worker newWorker = new Worker(userId,
                Integer.parseInt(getEditingDocument().getPk().getId()),
                lastIndexWorkers + i, worker.isApproval(), worker.isWriter(),
                document.getInstanceId(), SET_TYPE_USER, worker.isSaved(),
                worker.isUsed(), document.getCurrentWorkListOrder());
            mapWork.put(userId, newWorker);
          }
          i++;
        }
      }
    }
    mergedWorkers.addAll(mapWork.values());
    return mergedWorkers;
  }

  /**
   * @throws RemoteException
   */
  public void deleteWorkers() throws RemoteException {
    getVersioningBm().deleteWorkList(getEditingDocument(), false);
    getEditingDocument().setWorkList(new ArrayList<Worker>());
  }

  /**
   * @param keepSaved
   * @throws RemoteException
   */
  public void deleteWorkers(boolean keepSaved) throws RemoteException {
    Document doc = getEditingDocument();
    ArrayList<Worker> workers = doc.getWorkList();
    ArrayList<Worker> updatedWorkers = new ArrayList<Worker>();
    for (Worker worker : workers) {
      worker.setUsed(false);
      updatedWorkers.add(worker);
    }
    getEditingDocument().setWorkList(updatedWorkers);
    getVersioningBm().updateWorkList(doc);

    // Delete workers non used and non saved
    getVersioningBm().deleteWorkList(doc, keepSaved);
    doc.setWorkList(new ArrayList<Worker>());
    getVersioningBm().updateWorkList(doc);
  }

  /**
   * @param workers
   * @param setTypeId
   * @param setType
   * @throws RemoteException
   */
  public void setWorkerValidator(List<Worker> workers, int setTypeId, String setType)
      throws RemoteException {
    for (Worker worker : workers) {
      if (worker.getId() == setTypeId && worker.getType().equals(setType)) {
        worker.setApproval(true);
      }
      if (worker.getId() != setTypeId && worker.isApproval()
          && WRITERS_LIST_APPROUVAL.equals(String.valueOf(document.getCurrentWorkListOrder()))) {
        worker.setApproval(false);
      }
    }
    updateWorkList(document);
  }

  /**
   * @param value
   */
  public void setAlreadyMerged(boolean value) {
    this.alreadyMerged = value;
  }

  /**
   * @return
   */
  public boolean isAlreadyMerged() {
    return alreadyMerged;
  }

  /**
   * @return
   * @throws RemoteException
   */
  public int getSavedListType() throws RemoteException {
    return getVersioningBm().getSavedListType(getComponentId());
  }

  public String getXmlForm() {
    return xmlForm;
  }

  public void setXmlForm(String xmlForm) {
    this.xmlForm = xmlForm;
  }

  public String sortDocuments(List<DocumentPK> pks) {
    try {
      getVersioningBm().sortDocuments(pks);
      return "ok";
    } catch (RemoteException e) {
      SilverTrace.error("versioningPeas", "VersioningSessionController.sortDocuments",
          "MSG_ERROR_CANT_SORT_DOCUMENTS", e);
      return e.getMessage();
    }
  }

  public DocumentVersionPK saveOnline(Document document,
      String comments, String radio,
      int userId, boolean force, boolean addXmlForm) throws RemoteException {
    DocumentVersion lastVersion = getLastVersion(document.getPk());
    String physicalName = new Date().getTime() + "." + lastVersion.getLogicalName().substring(
        lastVersion.getLogicalName().indexOf(".") + 1, lastVersion.getLogicalName().length());
    DocumentVersion newVersion = new DocumentVersion(null, document.getPk(),
        lastVersion.getMajorNumber(), lastVersion.getMinorNumber(), userId,
        new Date(), comments, Integer.parseInt(radio), lastVersion.getStatus(),
        physicalName, lastVersion.getLogicalName(), lastVersion.getMimeType(),
        lastVersion.getSize(), lastVersion.getInstanceId());
    if (lastVersion.isOpenOfficeCompatibleDocument()) {
      RepositoryHelper.getJcrDocumentService().getUpdatedDocument(newVersion);
    }
    if (addXmlForm) {
      newVersion.setXmlForm(getXmlForm());
    }
    checkDocumentIn(document.getPk(), userId, force);
    newVersion = addNewDocumentVersion(newVersion);
    return newVersion.getPk();
  }
}
