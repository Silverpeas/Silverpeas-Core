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
package com.stratelia.silverpeas.versioning.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;

public interface VersioningBm extends javax.ejb.EJBObject {

  public Document getDocument(DocumentPK pk) throws RemoteException;

  public ArrayList getDocuments(ForeignPK foreignPK) throws RemoteException;

  public DocumentPK createDocument(Document docToCreate,
      DocumentVersion initialVersion) throws RemoteException;

  public DocumentVersion addDocumentVersion(Document doc,
      DocumentVersion newVersion) throws RemoteException;

  public void updateDocument(Document document) throws RemoteException;

  public void updateDocumentForeignKey(DocumentPK documentPK,
      ForeignPK foreignKey) throws RemoteException;

  public void updateWorkList(Document document) throws RemoteException;

  public void deleteWorkList(Document document) throws RemoteException;

  public void deleteWorkList(Document document, boolean keepSaved)
      throws RemoteException;

  public void deleteDocument(DocumentPK document_pk) throws RemoteException;

  public void deleteDocumentsByForeignPK(ForeignPK foreignPK)
      throws RemoteException;

  public void deleteDocumentsByInstanceId(String instanceId)
      throws RemoteException;

  public void checkDocumentOut(DocumentPK documentPK, int ownerId,
      java.util.Date checkOutDate) throws RemoteException;

  public void checkDocumentIn(DocumentPK documentPK, int userId)
      throws RemoteException;

  public void validateDocument(Document document, int validatorID,
      String comment, java.util.Date validationDate) throws RemoteException;

  public void refuseDocument(Document document, int validatorID,
      String comment, java.util.Date validationDate) throws RemoteException;

  public ArrayList getDocumentVersions(DocumentPK documentPK)
      throws RemoteException;

  public DocumentVersion getLastPublicDocumentVersion(DocumentPK documentPK)
      throws RemoteException;

  public DocumentVersion getLastDocumentVersion(DocumentPK documentPK)
      throws RemoteException;

  public List getAllPublicDocumentVersions(DocumentPK documentPK)
      throws RemoteException;

  public DocumentVersionPK addVersion(DocumentVersion newVersion)
      throws RemoteException;

  public List getAllFilesReserved(int ownerId) throws RemoteException;

  public DocumentVersion getDocumentVersion(DocumentVersionPK docVersionPK)
      throws RemoteException;

  public void updateDocumentVersion(DocumentVersion documentVersion)
      throws RemoteException;

  public List getAllFilesReservedByDate(Date date, boolean alert)
      throws RemoteException;

  public Collection getAllDocumentsToLib(Date date) throws RemoteException;

  public void notifyUser(NotificationMetaData notifMetaData, String senderId,
      String componentId) throws RemoteException;

  public void saveReadersAccessList(String componentId, ArrayList groupIds,
      ArrayList userIds) throws RemoteException;

  public void saveWorkersAccessList(String componentId, String documentId,
      int listType) throws RemoteException;

  public int getSavedListType(String componentId) throws RemoteException;

  public void removeReadersAccessList(String componentId)
      throws RemoteException;

  public void removeWorkersAccessList(String componentId)
      throws RemoteException;

  public ArrayList getReadersAccessListGroups(String componentId)
      throws RemoteException;

  public ArrayList getWorkersAccessListGroups(String componentId)
      throws RemoteException;

  public ArrayList getReadersAccessListUsers(String componentId)
      throws RemoteException;

  public ArrayList getWorkersAccessListUsers(String componentId)
      throws RemoteException;

}
