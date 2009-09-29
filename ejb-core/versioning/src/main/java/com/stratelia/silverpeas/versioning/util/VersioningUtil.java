/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.versioning.util;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.versioning.VersioningIndexer;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
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
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;

public class VersioningUtil {
  private VersioningBm versioning_bm = null;
  private AdminController m_AdminCtrl = null;
  private HashMap noReaderMap = new HashMap();
  private VersioningIndexer indexer = new VersioningIndexer();
  private boolean isIndexable = true;

  private String componentId = null;
  private Document document = null;
  private String userId = null;
  private boolean topicRightsEnabled = false;
  private String topicId = null;
  private Selection selection;

  // For Office files direct update
  public final static String NO_UPDATE_MODE = "0";
  public final static String UPDATE_DIRECT_MODE = "1";
  public final static String UPDATE_SHORTCUT_MODE = "2";

  public static final String ADMIN = "admin";
  public static final String PUBLISHER = "publisher";
  public static final String READER = "user";
  public static final String WRITER = "writer";

  public VersioningUtil() {
  }

  public VersioningUtil(String componentId, Document doc, String userId,
      String topicId) {
    this.componentId = componentId;
    this.document = doc;
    this.userId = userId;
    ComponentInst compInst = getAdmin().getComponentInst(componentId);
    if ("yes".equalsIgnoreCase(compInst.getParameterValue("rightsOnTopics"))) {
      this.topicRightsEnabled = true;
      this.topicId = topicId;
    }
    this.selection = new Selection();
  }

  private String getComponentId() {
    return componentId;
  }

  private Document getDocument() {
    return document;
  }

  private String getUserId() {
    return userId;
  }

  private boolean isTopicRightsEnabled() {
    return topicRightsEnabled;
  }

  private String getTopicId() {
    return topicId;
  }

  private Selection getSelection() {
    return selection;
  }

  /**
   * 
   * @return
   */
  private AdminController getAdmin() {
    if (m_AdminCtrl == null)
      m_AdminCtrl = new AdminController(getUserId());

    return m_AdminCtrl;
  }

  public Document getDocument(DocumentPK pk) throws RemoteException {
    return getVersioningBm().getDocument(pk);
  }

  public ArrayList getDocuments(ForeignPK foreignID) throws RemoteException {
    ArrayList documents = new ArrayList();
    documents = getVersioningBm().getDocuments(foreignID);

    return documents;
  }

  public DocumentVersion getLastPublicVersion(DocumentPK documentPK)
      throws RemoteException {
    DocumentVersion version = null;
    version = getVersioningBm().getLastPublicDocumentVersion(documentPK);

    return version;
  }

  public DocumentVersion getLastVersion(DocumentPK documentPK)
      throws RemoteException {
    DocumentVersion version = null;
    version = getVersioningBm().getLastDocumentVersion(documentPK);

    return version;
  }

  /**
   * Get document version by id
   * 
   * @param documentVersionPK
   * @return DocumentVersion
   * @throws RemoteException
   */
  public DocumentVersion getDocumentVersion(DocumentVersionPK documentVersionPK)
      throws RemoteException {
    DocumentVersion version = null;
    version = getVersioningBm().getDocumentVersion(documentVersionPK);

    return version;
  }

  public HashMap getAllUsersReader(Document document, String nameProfile)
      throws RemoteException {
    HashMap mapRead = new HashMap();
    Reader reader;
    ArrayList no_readers = getAllNoReader(document);
    for (int i = 0; i < no_readers.size(); i++) {
      reader = (Reader) no_readers.get(i);
      mapRead.put(String.valueOf(reader.getUserId()), reader);
    }
    ArrayList readers = document.getReadList();
    for (int i = 0; i < readers.size(); i++) {
      reader = (Reader) readers.get(i);
      mapRead.put(String.valueOf(reader.getUserId()), reader);
    }

    return mapRead;
  }

  public ArrayList getAllNoReader(Document document) throws RemoteException {
    noReaderMap.clear();
    noReaderMap.putAll(getAllUsersForProfile(document, "publisher"));
    noReaderMap.putAll(getAllUsersForProfile(document, "admin"));

    ArrayList writers = new ArrayList();
    writers.addAll(getAllUsersForProfile(document, "writer").values());
    int creator_id = getDocumentCreator(document.getPk());
    for (int i = 0; i < writers.size(); i++) {
      Reader user = (Reader) writers.get(i);
      if (user.getUserId() == creator_id) {
        noReaderMap.put(String.valueOf(creator_id), user);
        break;
      }
    }

    ArrayList users = new ArrayList(noReaderMap.values());
    return users;
  }

  public String getDocumentVersionIconPath(String physicalName) {
    String icon_url = "";
    int pointIndex = physicalName.lastIndexOf(".");
    int theLength = physicalName.length();

    if ((pointIndex >= 0) && ((pointIndex + 1) < theLength)) {
      String fileType = physicalName.substring(pointIndex + 1);
      icon_url = FileRepositoryManager.getFileIcon(fileType);
    }

    return icon_url;
  }

  public String getDocumentVersionURL(String componentId, String logicalName,
      String documentId, String versionId) {
    return FileServerUtils.getUrl(componentId, logicalName) + "&DocumentId="
        + documentId + "&VersionId=" + versionId + "&Name="
        + FileServerUtils.replaceSpecialChars(logicalName);
  }

  public String getDownloadEstimation(int size) {
    return FileRepositoryManager.getFileDownloadTime(size);
  }

  public HashMap getAllUsersForProfile(Document document, String nameProfile) {
    OrganizationController orgCntr = new OrganizationController();

    ComponentInst componentInst = orgCntr.getComponentInst(document
        .getForeignKey().getComponentName());

    HashMap mapRead = new HashMap();
    ProfileInst profileInst = null;

    // Get profile instance for given profile
    profileInst = componentInst.getProfileInst(nameProfile);
    if (profileInst != null) {
      ArrayList groupIds = new ArrayList();
      ArrayList userIds = new ArrayList();
      Reader ru = null;
      groupIds.addAll(profileInst.getAllGroups());
      userIds.addAll(profileInst.getAllUsers());

      Group group = null;
      UserDetail ud = null;

      // For all users of all groups generate UserDetail and then put
      // pair (users id, UsersDetail) to HashTable
      for (int i = 0; i < groupIds.size(); i++) {
        group = orgCntr.getGroup((String) groupIds.get(i));
        String[] usersGroup = group.getUserIds();
        for (int j = 0; j < usersGroup.length; j++) {
          // This check avoids duplicate users
          if (!mapRead.containsKey(usersGroup[j])
              && !noReaderMap.containsKey(usersGroup[j])) {
            ud = orgCntr.getUserDetail(usersGroup[j]);
            ru = new Reader(Integer.parseInt(ud.getId()), 0, document
                .getInstanceId(), 0);
            mapRead.put(usersGroup[j], ru);
          }
        }
      }

      // For all users generate UserDetail and then put
      // pair (users id, UsersDetail) to HashTable
      for (int i = 0; i < userIds.size(); i++) {
        // This check avoids duplicate users
        if (!mapRead.containsKey(userIds.get(i))
            && !noReaderMap.containsKey(userIds.get(i))) {
          ud = orgCntr.getUserDetail((String) userIds.get(i));
          ru = new Reader(Integer.parseInt(ud.getId()), 0, document
              .getInstanceId(), 0);
          mapRead.put(userIds.get(i), ru);
        }
      }
    }

    return mapRead;
  }

  public int getDocumentCreator(DocumentPK pk) throws RemoteException {
    ArrayList versions = getDocumentVersions(pk);
    if (versions != null && versions.size() > 0) {
      DocumentVersion first_version = (DocumentVersion) versions.get(0);
      return first_version.getAuthorId();
    } else {
      return -1;
    }
  }

  public ArrayList getDocumentVersions(DocumentPK documentPK)
      throws RemoteException {
    return getDocumentFilteredVersions(documentPK, -1);
  }

  public ArrayList getDocumentFilteredVersions(DocumentPK documentPK,
      int user_id) throws RemoteException {
    ArrayList versions = null;

    // Get all versions for given document
    versions = getVersioningBm().getDocumentVersions(documentPK);

    ArrayList filtered_versions = new ArrayList(versions.size());

    if (user_id < 0) {
      // If users id is not seted, return all versions
      filtered_versions = versions;
    } else {
      // Filter versions
      // determine has current user writer rights
      Document document = getVersioningBm().getDocument(documentPK);
      boolean is_writer = isWriter(document, user_id);
      DocumentVersion version = null;
      if (versions != null) {
        version = (DocumentVersion) versions.get(0);
        if (version.getAuthorId() == user_id) {
          is_writer = true;
        }
      }

      if (is_writer) {
        // If writer - return all versions
        filtered_versions = versions;
      } else {
        // Filter versions (select only public ones)
        for (int i = 0; i < versions.size(); i++) {
          version = (DocumentVersion) versions.get(i);
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

  public boolean isWriter(Document document, int userID) {
    Worker worker;
    ArrayList writeList = document.getWorkList();

    for (int i = 0; i < writeList.size(); i++) {
      worker = (Worker) writeList.get(i);

      if (worker.getId() == userID) {
        return true;
      }
    }

    return false;
  }

  public void indexDocumentsByForeignKey(ForeignPK foreignPK)
      throws RemoteException {
    List documents = getVersioningBm().getDocuments(foreignPK);
    Document document = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = (Document) documents.get(d);
      version = getVersioningBm()
          .getLastPublicDocumentVersion(document.getPk());
      createIndex(document, version);
    }
  }

  public void unindexDocumentsByForeignKey(ForeignPK foreignPK)
      throws RemoteException {
    List documents = getVersioningBm().getDocuments(foreignPK);
    Document document = null;
    DocumentVersion version = null;
    for (int d = 0; d < documents.size(); d++) {
      document = (Document) documents.get(d);
      version = getVersioningBm()
          .getLastPublicDocumentVersion(document.getPk());
      indexer.removeIndex(document, version);
    }
  }

  public void createIndex(Document documentToIndex, DocumentVersion lastVersion)
      throws RemoteException {
    SilverTrace.info("versioningPeas", "VersioningUtil.createIndex()",
        "root.MSG_GEN_ENTER_METHOD", "documentToIndex = "
            + documentToIndex.toString());

    indexer.createIndex(documentToIndex, lastVersion);
  }

  public String createPath(String spaceId, String componentId, String context) {
    // context is always null !
    return indexer.createPath(spaceId, componentId);
  }

  public void updateDocumentForeignKey(DocumentPK documentPK, String foreignKey) {
    try {
      ForeignPK foreignPK = new ForeignPK(foreignKey, documentPK
          .getInstanceId());
      getVersioningBm().updateDocumentForeignKey(documentPK, foreignPK);
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningUtil.updateDocumentForeignKey()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          e);
    }
  }

  /**
   * Update document version
   * 
   * @param documentVersionPK
   * @return DocumentVersion
   */
  public void updateDocumentVersion(DocumentVersion documentVersion) {
    try {
      getVersioningBm().updateDocumentVersion(documentVersion);
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningUtil.updateDocumentVersion()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          e);
    }
  }

  private VersioningBm getVersioningBm() {
    if (versioning_bm == null) {
      try {
        VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.VERSIONING_EJBHOME,
                VersioningBmHome.class);
        versioning_bm = vscEjbHome.create();
      } catch (Exception e) {
        // NEED
        // throw new
        // ...RuntimeException("VersioningSessionController.initEJB()",SilverpeasRuntimeException.ERROR,"root.EX_CANT_GET_REMOTE_OBJECT",e);
      }
    }
    return versioning_bm;
  }

  public boolean checkinFile(String documentId, int versionType,
      String comment, String physicalFileName)
      throws VersioningRuntimeException {
    return checkinFile(documentId, versionType, comment, null, physicalFileName);
  }

  public boolean checkinFile(String documentId, int versionType,
      String comment, String userId, String physicalFileName)
      throws VersioningRuntimeException {
    try {
      SilverTrace.debug("versioning", "VersioningUtil.checkinFile()",
          "root.MSG_GEN_ENTER_METHOD", "documentId = " + documentId
              + "Type version=" + versionType);

      DocumentVersion documentVersion = getLastVersion(new DocumentPK(
          new Integer(documentId).intValue()));
      if (RepositoryHelper.getJcrDocumentService()
          .isNodeLocked(documentVersion)) {
        return false;
      }
      DocumentVersion newDocumentVersion = createNewDocumentVersion(documentVersion);

      String componentId = documentVersion.getInstanceId();

      newDocumentVersion.setPk(new DocumentVersionPK(new Integer(documentId)
          .intValue()));
      newDocumentVersion.setPhysicalName(physicalFileName);

      SilverTrace.debug("versioning", "VersioningUtil.checkinOfficeFile()",
          "root.MSG_GEN_ENTER_METHOD", "newDocumentVersion.getId() = "
              + newDocumentVersion.getPk().getId());

      String newVersionFile = FileRepositoryManager
          .getAbsolutePath(componentId)
          + DocumentVersion.CONTEXT_VERSIONING
          + newDocumentVersion.getPhysicalName();

      // Create new document version
      int newSize = FileRepositoryManager.getFileSize(newVersionFile);
      newDocumentVersion.setSize(newSize);
      newDocumentVersion.setAuthorId(Integer.parseInt(userId));
      newDocumentVersion.setCreationDate(new Date());
      newDocumentVersion = addNewDocumentVersion(newDocumentVersion,
          versionType, comment);
      return true;
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningUtil.checkinOfficeFile()",
          SilverpeasRuntimeException.ERROR, "versioning.CHECKIN_FAILED", e);
    }
  }

  /**
   * to check document out
   * 
   * @return void
   * @exception RemoteException
   */
  public void checkDocumentOut(DocumentPK documentPK, int ownerID,
      java.util.Date checkOutDate) throws RemoteException {
    getVersioningBm().checkDocumentOut(documentPK, ownerID, checkOutDate);
  }

  /**
   * Create a new version of a document
   * 
   * @param DocumentVersion
   * @return DocumentVersion
   */
  public DocumentVersion createNewDocumentVersion(
      DocumentVersion documentVersion) {
    SilverTrace.debug("versioning",
        "VersioningUtil.createNewDocumentVersion()",
        "root.MSG_GEN_ENTER_METHOD");
    // New Document version started from the previous version
    DocumentVersion newDocumentVersion = (DocumentVersion) documentVersion
        .clone();

    String logicalName = documentVersion.getLogicalName();
    String suffix = logicalName.substring(logicalName.indexOf(".") + 1,
        logicalName.length());
    String newPhysicalName = new Long(new Date().getTime()).toString() + "."
        + suffix;
    newDocumentVersion.setPhysicalName(newPhysicalName);
    return newDocumentVersion;
  }

  /**
   * to add new document version
   * 
   * @return DocumentVersion
   * @exception RemoteException
   */
  public DocumentVersion addNewDocumentVersion(DocumentVersion newVersion,
      int versionType) throws RemoteException {
    return addNewDocumentVersion(newVersion, versionType, "");
  }

  /**
   * to add new document version
   * 
   * @return DocumentVersion
   * @exception RemoteException
   */
  public DocumentVersion addNewDocumentVersion(DocumentVersion newVersion,
      int versionType, String comment) throws RemoteException {
    DocumentVersion version = null;

    DocumentPK document_pk = newVersion.getDocumentPK();
    Document document = getVersioningBm().getDocument(document_pk);
    newVersion.setType(versionType);
    newVersion.setComments(comment);
    version = getVersioningBm().addDocumentVersion(document, newVersion);

    if (versionType == DocumentVersion.TYPE_PUBLIC_VERSION) {
      CallBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE,
          newVersion.getAuthorId(), document.getForeignKey().getInstanceId(),
          document.getForeignKey().getId());
      if (isIndexable()) {
        createIndex(document, version);
      }
    }

    return version;
  }

  public void setIndexable(boolean indexIt) {
    isIndexable = indexIt;
  }

  public boolean isIndexable() {
    return isIndexable;
  }

  public void moveDocuments(ForeignPK fromPK, ForeignPK toPK, boolean indexIt)
      throws RemoteException {
    String toAbsolutePath = FileRepositoryManager.getAbsolutePath(toPK
        .getInstanceId());
    String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromPK
        .getInstanceId());

    // First, remove index
    unindexDocumentsByForeignKey(fromPK);

    // ensure directory exists
    createPath(null, toPK.getInstanceId(), null);

    List documents = getVersioningBm().getDocuments(fromPK);

    Document document = null;
    File fromFile = null;
    File toFile = null;
    for (int a = 0; documents != null && a < documents.size(); a++) {
      document = (Document) documents.get(a);

      List versions = getVersioningBm().getDocumentVersions(document.getPk());

      DocumentVersion version = null;
      for (int v = 0; versions != null && v < versions.size(); v++) {
        version = (DocumentVersion) versions.get(v);

        // move file on disk
        fromFile = new File(fromAbsolutePath + "Versioning" + File.separator
            + version.getPhysicalName());
        toFile = new File(toAbsolutePath + "Versioning" + File.separator
            + version.getPhysicalName());

        if (fromFile != null && fromFile.exists())
          fromFile.renameTo(toFile);

        // change foreignKey
        version.setInstanceId(toPK.getInstanceId());
        getVersioningBm().updateDocumentVersion(version);
      }

      getVersioningBm().updateDocumentForeignKey(document.getPk(), toPK);
    }

    if (indexIt) {
      // index documents
      indexDocumentsByForeignKey(toPK);
    }
  }

  /**
   * 
   * @throws RemoteException
   */
  public void setFileRights(Document document) throws RemoteException {
    // Set file rights in admin meaning
    setFileRights(READER);
    setFileRights(WRITER);

    // Set file rights in versioning rights
    ArrayList workers = new ArrayList();
    ArrayList workersUsers = new ArrayList();
    ArrayList workersGroups = new ArrayList();
    document
        .setCurrentWorkListOrder(getSavedListType(document.getInstanceId()));
    document.setWorkList(workers);

    if (isAccessListExist(WRITER)) {
      ArrayList savedWorkersGroups = getAccessListGroups(WRITER);
      ArrayList savedWorkersUsers = getAccessListUsers(WRITER);

      Iterator savedWorkersGroupsIterator = savedWorkersGroups.iterator();
      while (savedWorkersGroupsIterator.hasNext()) {
        Worker savedWorkerGroup = (Worker) savedWorkersGroupsIterator.next();
        Worker newWorkerGroup = (Worker) savedWorkerGroup.clone();
        newWorkerGroup.setDocumentId(new Integer(document.getPk().getId())
            .intValue());
        newWorkerGroup.setSaved(false);
        workersGroups.add(newWorkerGroup);
      }
      workers.addAll(workersGroups);

      Iterator savedWorkersUsersIterator = savedWorkersUsers.iterator();
      while (savedWorkersUsersIterator.hasNext()) {
        Worker savedWorkerUser = (Worker) savedWorkersUsersIterator.next();
        Worker newWorkerUser = (Worker) savedWorkerUser.clone();
        newWorkerUser.setDocumentId(new Integer(document.getPk().getId())
            .intValue());
        newWorkerUser.setSaved(false);
        workersUsers.add(newWorkerUser);
      }
      workers.addAll(workersUsers);
      document.setWorkList(workers);
    }
  }

  /**
   * 
   * @param role
   * @throws RemoteException
   */
  private void setFileRights(String role) throws RemoteException {
    // Access file saved rights
    Selection sel = getSelection();

    if (isAccessListExist(role)) {
      if (role.equals(READER)) {
        // We only get Ids
        sel.setSelectedElements((String[]) getAccessListUsers(role).toArray(
            new String[0]));
        sel.setSelectedSets((String[]) getAccessListGroups(role).toArray(
            new String[0]));
      } else {
        ArrayList workersUsers = getAccessListUsers(WRITER);
        ArrayList workersGroups = getAccessListGroups(WRITER);

        String[] usersIds = new String[workersUsers.size()];
        String[] groupsIds = new String[workersGroups.size()];
        // Extract only ids from Workers ArrayList
        int i = 0;
        Iterator workersUsersIterator = workersUsers.iterator();
        while (workersUsersIterator.hasNext()) {
          Worker workerUser = (Worker) workersUsersIterator.next();
          usersIds[i++] = new Integer(workerUser.getId()).toString();
        }

        i = 0;
        Iterator workersGroupsIterator = workersGroups.iterator();
        while (workersGroupsIterator.hasNext()) {
          Worker workerGroup = (Worker) workersGroupsIterator.next();
          groupsIds[i++] = new Integer(workerGroup.getId()).toString();
        }
        sel.setSelectedElements(usersIds);
        sel.setSelectedSets(groupsIds);
      }
      ProfileInst profile = getDocumentProfile(role);
      updateDocumentRole(profile);
    }
  }

  /**
   * 
   * @param role
   * @return
   */
  public ProfileInst getComponentProfile(String role) {
    ComponentInst componentInst = getAdmin().getComponentInst(getComponentId());
    ProfileInst profile = componentInst.getProfileInst(role);
    ProfileInst inheritedProfile = componentInst.getInheritedProfileInst(role);

    if (profile == null && inheritedProfile == null) {
      profile = new ProfileInst();
      profile.setName(role);
    } else if (profile != null && inheritedProfile == null) {
      // do nothing
    } else if (profile == null && inheritedProfile != null) {
      profile = inheritedProfile;
    } else {
      profile.addGroups(inheritedProfile.getAllGroups());
      profile.addUsers(inheritedProfile.getAllUsers());
    }
    return profile;
  }

  /**
   * 
   * @return
   * @throws RemoteException
   */
  public int getSavedListType(String componentId) throws RemoteException {
    return getVersioningBm().getSavedListType(componentId);
  }

  /**
   * 
   * @param role
   * @return
   */
  public ProfileInst getDocumentProfile(String role) throws RemoteException {
    ProfileInst profileInst = null;
    String documentId = getDocument().getPk().getId();
    List profiles = getAdmin().getProfilesByObject(documentId,
        ObjectType.DOCUMENT, getDocument().getInstanceId());
    if (profiles != null && !profiles.isEmpty()) {
      if (!profiles.isEmpty()) {
        // Rights by file exists ?
        profileInst = getProfile(profiles, role);
        if (profileInst == null) {
          profileInst = new ProfileInst();
          profileInst.setName(role);
        }
      } else {
        profileInst = new ProfileInst();
        profileInst.setName(role);
      }
    } else {
      profileInst = new ProfileInst();
      profileInst.setName(role);
    }
    return profileInst;
  }

  /**
   * 
   * @param role
   * @return
   * @throws RemoteException
   */
  public boolean isAccessListExist(String role) throws RemoteException {
    return (!getAccessListGroups(role).isEmpty() || !getAccessListUsers(role)
        .isEmpty());
  }

  /**
   * 
   * @param role
   * @throws RemoteException
   */
  public ArrayList getAccessListUsers(String role) throws RemoteException {
    if (role.equals(READER))
      return getVersioningBm().getReadersAccessListUsers(getComponentId());
    else
      return getVersioningBm().getWorkersAccessListUsers(getComponentId());
  }

  /**
   * 
   * @param role
   * @return
   * @throws RemoteException
   */
  public ArrayList getAccessListGroups(String role) throws RemoteException {
    if (role.equals(READER))
      return getVersioningBm().getReadersAccessListGroups(getComponentId());
    else
      return getVersioningBm().getWorkersAccessListGroups(getComponentId());
  }

  /**
   * 
   * @param profiles
   * @param role
   * @return
   */
  private ProfileInst getProfile(List profiles, String role) {
    ProfileInst profile = null;
    for (int p = 0; p < profiles.size(); p++) {
      profile = (ProfileInst) profiles.get(p);
      if (role.equals(profile.getName()))
        return profile;
    }
    return null;
  }

  /**
   * 
   * @param profile
   * @throws RemoteException
   */
  public void updateDocumentRole(ProfileInst profile) throws RemoteException {
    String documentId = getDocument().getPk().getId();
    Selection sel = getSelection();

    if (!StringUtil.isDefined(profile.getId())) {
      // Create the profile
      profile.setObjectId(Integer.parseInt(documentId));
      profile.setObjectType(ObjectType.DOCUMENT);
      profile.setComponentFatherId(getComponentId());
      profile.setGroupsAndUsers(sel.getSelectedSets(), sel
          .getSelectedElements());
      getAdmin().addProfileInst(profile);
    }
  }

  /**
   * 
   * @param role
   * @param topicId
   * @return
   */
  public ProfileInst getTopicProfile(String role, String topicId) {
    List profiles = getAdmin().getProfilesByObject(topicId, ObjectType.NODE,
        getComponentId());
    for (int p = 0; profiles != null && p < profiles.size(); p++) {
      ProfileInst profile = (ProfileInst) profiles.get(p);
      if (profile.getName().equals(role))
        return profile;
    }

    ProfileInst profile = new ProfileInst();
    profile.setName(role);
    return profile;
  }

  /**
   * 
   * @return
   */
  public NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
          JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningUtil.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return nodeBm;
  }

  public void updateProfileInst(ProfileInst profile) {
    getAdmin().updateProfileInst(profile);
  }

}