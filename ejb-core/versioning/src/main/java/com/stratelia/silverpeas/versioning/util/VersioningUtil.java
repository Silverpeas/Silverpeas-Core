/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://www.silverpeas.com/legal/licensing"
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

import static com.stratelia.webactiv.SilverpeasRole.admin;
import static com.stratelia.webactiv.SilverpeasRole.publisher;
import static com.stratelia.webactiv.SilverpeasRole.user;
import static com.stratelia.webactiv.SilverpeasRole.writer;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.FullIndexEntry;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;

public class VersioningUtil {

  private VersioningBm versioning_bm = null;
  private AdminController m_AdminCtrl = null;
  private HashMap<String, Reader> noReaderMap = new HashMap<String, Reader>();
  private VersioningIndexer indexer = new VersioningIndexer();
  private boolean isIndexable = true;
  private String componentId = null;
  private Document document = null;
  private String userId = null;
  private Selection selection;
  // For Office files direct update
  public final static String NO_UPDATE_MODE = "0";
  public final static String UPDATE_DIRECT_MODE = "1";
  public final static String UPDATE_SHORTCUT_MODE = "2";
  public static final String ADMIN = admin.toString();
  public static final String PUBLISHER = publisher.toString();
  public static final String READER = user.toString();
  public static final String WRITER = writer.toString();
  
  private final static ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.attachment.Attachment", "");

  public VersioningUtil() {
  }

  public VersioningUtil(String componentId, Document doc, String userId, String topicId) {
    this.componentId = componentId;
    this.document = doc;
    this.userId = userId;
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

  private Selection getSelection() {
    return selection;
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

  public Document getDocument(DocumentPK pk) throws RemoteException {
    return getVersioningBm().getDocument(pk);
  }

  public List<Document> getDocuments(ForeignPK foreignID) throws RemoteException {
    List<Document> documents = new ArrayList<Document>(getVersioningBm().getDocuments(foreignID));
    return documents;
  }

  public DocumentVersion getLastPublicVersion(DocumentPK documentPK) throws RemoteException {
    return getVersioningBm().getLastPublicDocumentVersion(documentPK);
  }

  public DocumentVersion getLastVersion(DocumentPK documentPK)
      throws RemoteException {
    DocumentVersion version = null;
    version = getVersioningBm().getLastDocumentVersion(documentPK);

    return version;
  }

  /**
   * Get document version by id
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

  public HashMap<String, Reader> getAllUsersReader(Document document, String nameProfile)
      throws RemoteException {
    HashMap<String, Reader> mapRead = new HashMap<String, Reader>();
    List<Reader> no_readers = getAllNoReader(document);
    for (Reader reader : no_readers) {
      mapRead.put(String.valueOf(reader.getUserId()), reader);
    }
    List<Reader> readers = document.getReadList();
    for (Reader reader : readers) {
      mapRead.put(String.valueOf(reader.getUserId()), reader);
    }
    return mapRead;
  }

  public ArrayList<Reader> getAllNoReader(Document document) throws RemoteException {
    noReaderMap.clear();
    noReaderMap.putAll(getAllUsersForProfile(document, publisher.toString()));
    noReaderMap.putAll(getAllUsersForProfile(document, admin.toString()));

    List<Reader> writers = new ArrayList<Reader>();
    writers.addAll(getAllUsersForProfile(document, writer.toString()).values());
    int creator_id = getDocumentCreator(document.getPk());
    for (Reader user : writers) {
      if (user.getUserId() == creator_id) {
        noReaderMap.put(String.valueOf(creator_id), user);
        break;
      }
    }
    return new ArrayList<Reader>(noReaderMap.values());
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
    return FileServerUtils.getVersionedDocumentURL(componentId, logicalName, documentId, versionId);
  }

  public String getDownloadEstimation(long size) {
    return FileRepositoryManager.getFileDownloadTime(size);
  }

  public HashMap<String, Reader> getAllUsersForProfile(Document document, String nameProfile) {
    OrganizationController orgCntr = new OrganizationController();
    ComponentInst componentInst = orgCntr.getComponentInst(document.getForeignKey().getComponentName());

    HashMap<String, Reader> mapRead = new HashMap<String, Reader>();
    // Get profile instance for given profile
    ProfileInst profileInst = componentInst.getProfileInst(nameProfile);
    if (profileInst != null) {
      ArrayList<String> groupIds = new ArrayList<String>();
      ArrayList<String> userIds = new ArrayList<String>();
      groupIds.addAll(profileInst.getAllGroups());
      userIds.addAll(profileInst.getAllUsers());
      // For all users of all groups generate UserDetail and then put
      // pair (users id, UsersDetail) to HashTable
      for (String groupId : groupIds) {
        Group group = orgCntr.getGroup(groupId);
        String[] usersGroup = group.getUserIds();
        for (String anUsersGroup : usersGroup) {
          // This check avoids duplicate users
          if (!mapRead.containsKey(anUsersGroup) && !noReaderMap.containsKey(anUsersGroup)) {
            UserDetail ud = orgCntr.getUserDetail(anUsersGroup);
            Reader ru = new Reader(Integer.parseInt(ud.getId()), 0, document.getInstanceId(), 0);
            mapRead.put(anUsersGroup, ru);
          }
        }
      }

      // For all users generate UserDetail and then put
      // pair (users id, UsersDetail) to HashTable
      for (String userId : userIds) {
        // This check avoids duplicate users
        if (!mapRead.containsKey(userId) && !noReaderMap.containsKey(userId)) {
          UserDetail ud = orgCntr.getUserDetail(userId);
          Reader ru = new Reader(Integer.parseInt(ud.getId()), 0, document.getInstanceId(), 0);
          mapRead.put(userId, ru);
        }
      }
    }

    return mapRead;
  }

  public int getDocumentCreator(DocumentPK pk) throws RemoteException {
    ArrayList<DocumentVersion> versions = getDocumentVersions(pk);
    if (versions != null && versions.size() > 0) {
      DocumentVersion first_version = versions.get(0);
      return first_version.getAuthorId();
    }
    return -1;
  }

  public ArrayList<DocumentVersion> getDocumentVersions(DocumentPK documentPK) throws
      RemoteException {
    return getDocumentFilteredVersions(documentPK, -1);
  }

  public ArrayList<DocumentVersion> getDocumentFilteredVersions(DocumentPK documentPK, int user_id)
      throws
      RemoteException {
    // Get all versions for given document
    ArrayList<DocumentVersion> versions = getVersioningBm().getDocumentVersions(documentPK);
    ArrayList<DocumentVersion> filtered_versions = new ArrayList<DocumentVersion>(versions.size());
    if (user_id < 0) {
      // If users id is not seted, return all versions
      filtered_versions = versions;
    } else {
      // Filter versions
      // determine has current user writer rights
      Document doc = getVersioningBm().getDocument(documentPK);
      boolean is_writer = isWriter(doc, user_id);
      if (versions != null) {
        DocumentVersion version = versions.get(0);
        if (version.getAuthorId() == user_id) {
          is_writer = true;
        }
      }
      if (is_writer) {
        // If writer - return all versions
        filtered_versions = versions;
      } else {
        // Filter versions (select only public ones)
        for (DocumentVersion version : versions) {
          if (version != null && version.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
            filtered_versions.add(version);
          }
        }
      }
    }

    return filtered_versions;
  }

  public boolean isWriter(Document document, int userID) {
    ArrayList<Worker> writeList = document.getWorkList();
    for (Worker worker : writeList) {
      if (worker.getId() == userID) {
        return true;
      }
    }
    return false;
  }

  public boolean isReader(Document document, int userID) {
    if (isWriter(document, userID)) {
      return true;
    }
    List<Reader> readList = document.getReadList();
    for (Reader reader : readList) {
      if (reader.getUserId() == userID) {
        return true;
      }
    }
    return false;
  }

  public void indexDocumentsByForeignKey(ForeignPK foreignPK) throws RemoteException {
    indexDocumentsByForeignKey(foreignPK, null, null);
  }
  
  public void indexDocumentsByForeignKey(ForeignPK foreignPK, Date startOfVisibilityPeriod,
      Date endOfVisibilityPeriod) throws RemoteException {
    List<Document> documents = getVersioningBm().getDocuments(foreignPK);
    for (Document currentDocument : documents) {
      DocumentVersion version = getVersioningBm().getLastPublicDocumentVersion(
          currentDocument.getPk());
      createIndex(currentDocument, version, startOfVisibilityPeriod, endOfVisibilityPeriod);
    }
  }
  
  public void updateIndexEntryWithDocuments(FullIndexEntry indexEntry) {
    if (resources.getBoolean("attachment.index.incorporated", true)) {
      ForeignPK pk = new ForeignPK(indexEntry.getObjectId(), indexEntry.getComponent());
      try {
        List<Document> documents = getVersioningBm().getDocuments(pk);
        for (Document currentDocument : documents) {
          DocumentVersion version =
              getVersioningBm().getLastPublicDocumentVersion(currentDocument.getPk());
          indexEntry.addFileContent(version.getDocumentPath(), null, version.getMimeType(), null);
        }
      } catch (RemoteException e) {
        SilverTrace.error("versioning", "VersioningUtil.updateIndexEntryWithDocuments",
            "versioning.CANT_INDEX_DOCUMENTS",
            "objectId = " + pk.getId() + ", component = " + pk.getInstanceId(), e);
      }
    }
  }

  public void unindexDocumentsByForeignKey(ForeignPK foreignPK)
      throws RemoteException {
    List<Document> documents = getVersioningBm().getDocuments(foreignPK);
    for (Document doc : documents) {
      DocumentVersion version = getVersioningBm().getLastPublicDocumentVersion(doc.getPk());
      indexer.removeIndex(doc, version);
    }
  }

  public void createIndex(Document documentToIndex, DocumentVersion lastVersion)
      throws RemoteException {
    createIndex(documentToIndex, lastVersion, null, null);
  }
  
  public void createIndex(Document documentToIndex, DocumentVersion lastVersion,
      Date startOfVisibilityPeriod, Date endOfVisibilityPeriod) throws RemoteException {
    if (resources.getBoolean("attachment.index.separately", true)) {
      SilverTrace.info("versioningPeas", "VersioningUtil.createIndex()",
          "root.MSG_GEN_ENTER_METHOD", "documentToIndex = " + documentToIndex.toString());
      indexer.createIndex(documentToIndex, lastVersion, startOfVisibilityPeriod,
          endOfVisibilityPeriod);
    }
  }

  public String createPath(String spaceId, String componentId, String context) {
    return indexer.createPath(spaceId, componentId);
  }

  public void updateDocumentForeignKey(DocumentPK documentPK, String foreignKey) {
    try {
      ForeignPK foreignPK = new ForeignPK(foreignKey, documentPK.getInstanceId());
      getVersioningBm().updateDocumentForeignKey(documentPK, foreignPK);
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningUtil.updateDocumentForeignKey()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED", e);
    }
  }

  /**
   * Update document version
   * @param documentVersion
   */
  public void updateDocumentVersion(DocumentVersion documentVersion) {
    try {
      getVersioningBm().updateDocumentVersion(documentVersion);
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningUtil.updateDocumentVersion()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_INSERTION_FAILED", e);
    }
  }

  private VersioningBm getVersioningBm() {
    if (versioning_bm == null) {
      try {
        VersioningBmHome vscEjbHome = (VersioningBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.VERSIONING_EJBHOME, VersioningBmHome.class);
        versioning_bm = vscEjbHome.create();
      } catch (Exception e) {
      }
    }
    return versioning_bm;
  }

  public boolean checkinFile(String documentId, int versionType,
      String comment, String physicalFileName)
      throws VersioningRuntimeException {
    return checkinFile(documentId, versionType, comment, null, physicalFileName);
  }

  public boolean checkinFile(String documentId, int versionType, String comment, String userId, 
      String physicalFileName) throws VersioningRuntimeException {
    try {
      SilverTrace.debug("versioning", "VersioningUtil.checkinFile()",
          "root.MSG_GEN_ENTER_METHOD", "documentId = " + documentId + "Type version=" + versionType);
      DocumentVersion documentVersion = getLastVersion(new DocumentPK(Integer.parseInt(documentId)));
      if (RepositoryHelper.getJcrDocumentService().isNodeLocked(documentVersion)) {
        return false;
      }
      DocumentVersion newDocumentVersion = createNewDocumentVersion(documentVersion);
      String theComponentId = documentVersion.getInstanceId();
      newDocumentVersion.setPk(new DocumentVersionPK(Integer.parseInt(documentId)));
      newDocumentVersion.setPhysicalName(physicalFileName);

      SilverTrace.debug("versioning", "VersioningUtil.checkinOfficeFile()",
          "root.MSG_GEN_ENTER_METHOD", "newDocumentVersion.getId() = "
          + newDocumentVersion.getPk().getId());

      String newVersionFile = FileRepositoryManager.getAbsolutePath(theComponentId)
          + DocumentVersion.CONTEXT_VERSIONING + newDocumentVersion.getPhysicalName();

      // Create new document version
      long newSize = FileRepositoryManager.getFileSize(newVersionFile);
      newDocumentVersion.setSize(newSize);
      newDocumentVersion.setAuthorId(Integer.parseInt(userId));
      newDocumentVersion.setCreationDate(new Date());
      newDocumentVersion = addNewDocumentVersion(newDocumentVersion, versionType, comment);
      return true;
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningUtil.checkinOfficeFile()",
          SilverpeasRuntimeException.ERROR, "versioning.CHECKIN_FAILED", e);
    }
  }

  /**
   * to check document out
   * @return void
   * @exception RemoteException
   */
  public boolean checkDocumentOut(DocumentPK documentPK, int ownerID, Date checkOutDate) 
      throws RemoteException {
    return getVersioningBm().checkDocumentOut(documentPK, ownerID, checkOutDate);
  }

  /**
   * Create a new version of a document
   * @param DocumentVersion
   * @return DocumentVersion
   */
  public DocumentVersion createNewDocumentVersion(DocumentVersion documentVersion) {
    SilverTrace.debug("versioning", "VersioningUtil.createNewDocumentVersion()",
        "root.MSG_GEN_ENTER_METHOD");
    // New Document version started from the previous version
    DocumentVersion newDocumentVersion = (DocumentVersion) documentVersion.clone();
    String logicalName = documentVersion.getLogicalName();
    String suffix = logicalName.substring(logicalName.indexOf('.') + 1, logicalName.length());
    String newPhysicalName = String.valueOf(System.currentTimeMillis()) + '.' + suffix;
    newDocumentVersion.setPhysicalName(newPhysicalName);
    return newDocumentVersion;
  }

  /**
   * to add new document version
   * @return DocumentVersion
   * @exception RemoteException
   */
  public DocumentVersion addNewDocumentVersion(DocumentVersion newVersion, int versionType) 
      throws RemoteException {
    return addNewDocumentVersion(newVersion, versionType, "");
  }

  /**
   * to add new document version
   * @return DocumentVersion
   * @exception RemoteException
   */
  public DocumentVersion addNewDocumentVersion(DocumentVersion newVersion, int versionType, 
      String comment) throws RemoteException {
    DocumentVersion version = null;

    DocumentPK document_pk = newVersion.getDocumentPK();
    Document theDocument = getVersioningBm().getDocument(document_pk);
    newVersion.setType(versionType);
    newVersion.setComments(comment);
    version = getVersioningBm().addDocumentVersion(theDocument, newVersion);

    if (versionType == DocumentVersion.TYPE_PUBLIC_VERSION) {
      CallBackManager callBackManager = CallBackManager.get();
      callBackManager.invoke(CallBackManager.ACTION_VERSIONING_UPDATE,
          newVersion.getAuthorId(), theDocument.getForeignKey().getInstanceId(),
          theDocument.getForeignKey().getId());
      if (isIndexable()) {
        createIndex(theDocument, version);
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
    String toAbsolutePath = FileRepositoryManager.getAbsolutePath(toPK.getInstanceId());
    String fromAbsolutePath = FileRepositoryManager.getAbsolutePath(fromPK.getInstanceId());

    // First, remove index
    unindexDocumentsByForeignKey(fromPK);

    // ensure directory exists
    createPath(null, toPK.getInstanceId(), null);

    List<Document> documents = getVersioningBm().getDocuments(fromPK);

    File fromFile = null;
    File toFile = null;
    for (Document aDocument : documents){
      List<DocumentVersion> versions = getVersioningBm().getDocumentVersions(aDocument.getPk());
      for (DocumentVersion version : versions) {
        // move file on disk
        fromFile = new File(fromAbsolutePath + "Versioning" + File.separatorChar
            + version.getPhysicalName());
        toFile = new File(toAbsolutePath + "Versioning" + File.separatorChar
            + version.getPhysicalName());

        if (fromFile != null && fromFile.exists()) {
          fromFile.renameTo(toFile);
        }

        // change foreignKey
        version.setInstanceId(toPK.getInstanceId());
        getVersioningBm().updateDocumentVersion(version);
      }

      getVersioningBm().updateDocumentForeignKey(aDocument.getPk(), toPK);
    }

    if (indexIt) {
      // index documents
      indexDocumentsByForeignKey(toPK);
    }
  }

  /**
   * @throws RemoteException
   */
  public void setFileRights(Document document) throws RemoteException {
    // Set file rights in admin meaning
    setFileRights(READER);
    setFileRights(WRITER);

    // Set file rights in versioning rights
    ArrayList<Worker> workers = new ArrayList<Worker>();
    ArrayList<Worker> workersUsers = new ArrayList<Worker>();
    ArrayList<Worker> workersGroups = new ArrayList<Worker>();
    document.setCurrentWorkListOrder(getSavedListType(document.getInstanceId()));
    document.setWorkList(workers);

    if (isAccessListExist(WRITER)) {
      List<Worker> savedWorkersGroups = getWorkersAccessListGroups();
      List<Worker> savedWorkersUsers = getWorkersAccessListUsers();

      for (Worker savedWorkerGroup : savedWorkersGroups) {
        Worker newWorkerGroup = (Worker) savedWorkerGroup.clone();
        newWorkerGroup.setDocumentId(Integer.parseInt(document.getPk().getId()));
        newWorkerGroup.setSaved(false);
        workersGroups.add(newWorkerGroup);
      }
      workers.addAll(workersGroups);

      for (Worker savedWorkerUser : savedWorkersUsers) {
        Worker newWorkerUser = (Worker) savedWorkerUser.clone();
        newWorkerUser.setDocumentId(Integer.parseInt(document.getPk().getId()));
        newWorkerUser.setSaved(false);
        workersUsers.add(newWorkerUser);
      }
      workers.addAll(workersUsers);
      document.setWorkList(workers);
    }
  }

  /**
   * @param role
   * @throws RemoteException
   */
  private void setFileRights(String role) throws RemoteException {
    // Access file saved rights
    Selection sel = getSelection();

    if (isAccessListExist(role)) {
      if (READER.equals(role)) {
        // We only get Ids
        List<String> users = getReadersAccessListUsers();
        sel.setSelectedElements(users.toArray(new String[users.size()]));
        List<String> groups = getReadersAccessListUsers();
        sel.setSelectedSets(groups.toArray(new String[groups.size()]));
      } else {
        List<Worker> workersUsers = getWorkersAccessListUsers();
        List<Worker> workersGroups = getWorkersAccessListGroups();

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
      ProfileInst profile = getDocumentProfile(role);
      updateDocumentRole(profile);
    }
  }

  /**
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
    } else if (profile == null && inheritedProfile != null) {
      profile = inheritedProfile;
    } else if(profile != null && inheritedProfile != null) {
      profile.addGroups(inheritedProfile.getAllGroups());
      profile.addUsers(inheritedProfile.getAllUsers());
    }
    return profile;
  }

  /**
   * @return
   * @throws RemoteException
   */
  public int getSavedListType(String componentId) throws RemoteException {
    return getVersioningBm().getSavedListType(componentId);
  }

  /**
   * @param role
   * @return
   */
  public ProfileInst getDocumentProfile(String role) throws RemoteException {
    ProfileInst profileInst = null;
    String documentId = getDocument().getPk().getId();
    List<ProfileInst> profiles = getAdmin().getProfilesByObject(documentId, ObjectType.DOCUMENT.getCode(),
        getDocument().getInstanceId());
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
   * @param role
   * @return
   * @throws RemoteException
   */
  public boolean isAccessListExist(String role) throws RemoteException {
    if (role.equals(READER)) {
      return !getReadersAccessListGroups().isEmpty() || !getReadersAccessListUsers().isEmpty();
    }
    return !getWorkersAccessListGroups().isEmpty() || !getWorkersAccessListUsers().isEmpty();
  }

  /**
   * @param role
   * @throws RemoteException
   */
  private List<String> getReadersAccessListUsers() throws RemoteException {
    return getVersioningBm().getReadersAccessListUsers(getComponentId());
  }
  
  private List<Worker> getWorkersAccessListUsers() throws RemoteException {
    return getVersioningBm().getWorkersAccessListUsers(getComponentId());
  }
  
  private List<String> getReadersAccessListGroups() throws RemoteException {
    return getVersioningBm().getReadersAccessListGroups(getComponentId());
  }
  
  private List<Worker> getWorkersAccessListGroups() throws RemoteException {
    return getVersioningBm().getWorkersAccessListGroups(getComponentId());
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
   * @param profile
   * @throws RemoteException
   */
  public void updateDocumentRole(ProfileInst profile) throws RemoteException {
    String documentId = getDocument().getPk().getId();
    Selection sel = getSelection();

    if (!StringUtil.isDefined(profile.getId())) {
      // Create the profile
      profile.setObjectId(Integer.parseInt(documentId));
      profile.setObjectType(ObjectType.DOCUMENT.getCode());
      profile.setComponentFatherId(getComponentId());
      profile.setGroupsAndUsers(sel.getSelectedSets(), sel.getSelectedElements());
      getAdmin().addProfileInst(profile);
    }
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
