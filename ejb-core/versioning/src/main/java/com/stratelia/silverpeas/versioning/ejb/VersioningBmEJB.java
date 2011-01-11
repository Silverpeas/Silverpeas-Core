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
 * FLOSS exception.  You should have received a copy of the text describing
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

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBm;
import com.stratelia.webactiv.personalization.control.ejb.PersonalizationBmHome;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;

public class VersioningBmEJB implements SessionBean {

  private static final long serialVersionUID = 4838684224693087726L;
  public final static String DATE_FORMAT = "yyyy/MM/dd";
  private static ResourceLocator resources = new ResourceLocator(
      "com.stratelia.webactiv.util.versioning.Versioning", "");

  public Document getDocument(DocumentPK pk) {
    Connection con = openConnection();
    Document result = null;

    try {
      result = VersioningDAO.getDocument(con, pk);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.getDocument",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_DOCUMENT_FAILED", pk, re);
    } finally {
      closeConnection(con);
    }

    if (result == null) {
      throw new VersioningRuntimeException("VersioningBmEJB.getDocument",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_DOCUMENT_FAILED", "No document found" + pk);
    }

    return result;
  }

  public List<Document> getDocuments(ForeignPK foreignPK) {
    Connection con = openConnection();
    List<Document> result = null;
    try {
      result = VersioningDAO.getDocuments(con, foreignPK);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.getDocuments",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_DOCUMENT_FAILED", foreignPK, re);
    } finally {
      closeConnection(con);
    }

    return result;
  }

  public DocumentPK createDocument(Document docToCreate,
      DocumentVersion initialVersion) {
    Connection con = openConnection();
    DocumentPK documentPK = null;
    if (docToCreate == null) {
      throw new VersioningRuntimeException("VersioningBmEJB.createDocument",
          SilverpeasRuntimeException.ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK",
          docToCreate);
    }
    docToCreate.setStatus(Document.STATUS_CHECKINED);

    try {
      documentPK = VersioningDAO.createDocument(con, docToCreate,
          initialVersion);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.createDocument",
          SilverpeasRuntimeException.ERROR,
          "versioning.CREATING_NEW_DOCUMENT_FAILED", docToCreate, re);
    } finally {
      closeConnection(con);
    }

    if (documentPK == null) {
      throw new VersioningRuntimeException("VersioningBmEJB.createDocument",
          SilverpeasRuntimeException.ERROR,
          "versioning.CREATING_NEW_DOCUMENT_FAILED", docToCreate);
    }
    return documentPK;
  }

  private void addDays(Calendar calendar, int nbDay) {
    SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
        "nbDay = " + nbDay);
    int nb = 0;
    while (nb < nbDay) {
      SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
          "time = " + calendar.getTime());
      SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
          "nbDay = " + nbDay + " nb = " + nb);
      calendar.add(Calendar.DATE, 1);
      if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
          && calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
        nb = nb + 1;
      }
      SilverTrace.debug("versioning", "addDays", "root.MSG_GEN_PARAM_VALUE",
          "time = " + calendar.getTime());
    }
  }

  private void updateDates(Document doc) {
    // mise à jour de la date d'expiration

    // 1. rechercher le nombre de jours avant expiration dans le composant
    OrganizationController orga = new OrganizationController();
    String day = orga.getComponentParameterValue(doc.getInstanceId(),
        "nbDayForReservation");
    if (StringUtil.isDefined(day)) {
      int nbDay = Integer.parseInt(day);
      SilverTrace.debug("versioning", "updateDates",
          "root.MSG_GEN_PARAM_VALUE", "nbDay = " + nbDay);
      // 2. calcul la date d'expiration en fonction de la date d'aujourd'hui et
      // de la durée de réservation
      Calendar calendar = Calendar.getInstance(Locale.FRENCH);
      // calendar.add(Calendar.DATE, nbDay);
      SilverTrace.debug("versioning", "updateDates",
          "root.MSG_GEN_PARAM_VALUE", "calendar.getTime() AVANT = "
          + calendar.getTime());
      addDays(calendar, nbDay);
      SilverTrace.debug("versioning", "updateDates",
          "root.MSG_GEN_PARAM_VALUE", "calendar.getTime() APRES = "
          + calendar.getTime());
      doc.setExpiryDate(calendar.getTime());
    }

    // mise à jour de la date d'alerte

    // 1. rechercher le % dans le properties
    int delayReservedFile = Integer.parseInt(resources.getString("DelayReservedFile"));
    if (delayReservedFile >= 0 && delayReservedFile <= 100) {
      // calculer le nombre de jours
      if (StringUtil.isDefined(day)) {
        int nbDay = Integer.parseInt(day);
        int result = (nbDay * delayReservedFile) / 100;
        SilverTrace.debug("versioning", "updateDates",
            "root.MSG_GEN_PARAM_VALUE", "delayReservedFile = "
            + delayReservedFile);
        SilverTrace.debug("versioning", "updateDates",
            "root.MSG_GEN_PARAM_VALUE", "result = " + result);
        if (result > 2) {
          Calendar calendar = Calendar.getInstance(Locale.FRENCH);
          addDays(calendar, result);
          doc.setAlertDate(calendar.getTime());
        }
      }
    }
  }

  public boolean checkDocumentOut(DocumentPK documentPK, int ownerId, java.util.Date checkOutDate) {
    Connection con = openConnection();
    SilverTrace.debug("versioning", "checkDocumentOut", "root.MSG_GEN_PARAM_VALUE",
        "instanceId = " + documentPK.getId());
    try {
      Document doc = getDocument(documentPK);
      if (doc.getOwnerId() >= 0) {
        return false;
      }
      updateDates(doc);
      SilverTrace.debug("versioning", "checkDocumentOut",
          "root.MSG_GEN_PARAM_VALUE", "expiryDate = " + doc.getExpiryDate());
      VersioningDAO.checkDocumentOut(con, documentPK, ownerId, checkOutDate,
          doc.getAlertDate(), doc.getExpiryDate());
      List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, documentPK);
      DocumentVersion lastVersion = versions.get(0);
      if (lastVersion.isOpenOfficeCompatibleDocument()) {
        lastVersion.setAuthorId(ownerId);
        RepositoryHelper.getJcrDocumentService().createDocument(lastVersion);
      }
      return true;
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.checkDocumentOut",
          SilverpeasRuntimeException.ERROR, "versioning.CHECK_DOCUMENT_OUT",
          documentPK, re);
    } finally {
      closeConnection(con);
    }
  }

  public void checkDocumentIn(DocumentPK documentPK, int userId) {
    Connection con = openConnection();
    SilverTrace.debug("versioning", "checkDocumentIn", "root.MSG_GEN_PARAM_VALUE",
        "instanceId = " + documentPK.getId());
    try {
      Document doc = VersioningDAO.getDocument(con, documentPK);

      SilverTrace.debug("versioning", "checkDocumentIn", "root.MSG_GEN_PARAM_VALUE",
          "doc.getTypeWorkList() = " + doc.getTypeWorkList() + " / doc.getTypeWorkList() = "
          + doc.getTypeWorkList());

      if (doc.getTypeWorkList() == 1) {
        checkDocumentInNonOrdered(con, doc);

      } else if (doc.getTypeWorkList() == 2) {
        checkDocumentInOrdered(con, doc, userId);

      } else {
        VersioningDAO.checkDocumentIn(con, documentPK);
      }
      List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, documentPK);
      Iterator<DocumentVersion> iter = versions.iterator();
      while (iter.hasNext()) {
        RepositoryHelper.getJcrDocumentService().deleteDocument(
            iter.next());
      }
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.checkDocumentIn",
          SilverpeasRuntimeException.ERROR, "versioning.CHECK_DOCUMENT_IN",
          documentPK, re);
    } finally {
      closeConnection(con);
    }
  }

  protected void checkDocumentInNonOrdered(Connection con, Document doc) throws SQLException {
    DocumentPK documentPK = doc.getPk();
    List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, doc.getPk());
    DocumentVersion lastVersion = versions.get(0);
    VersioningDAO.checkDocumentIn(con, doc.getPk());
    if (lastVersion.getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED) {
      lastVersion.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
      VersioningDAO.updateDocumentVersion(con, lastVersion);
    }
    VersioningDAO.checkDocumentIn(con, documentPK);
  }

  protected void checkDocumentInOrdered(Connection con, Document doc, int userId)
      throws SQLException {
    VersioningDAO.checkDocumentIn(con, doc.getPk());
    List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, doc.getPk());
    DocumentVersion lastVersion = versions.get(0);
    if (lastVersion.getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED) {
      lastVersion.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
      VersioningDAO.updateDocumentVersion(con, lastVersion);
    }

    DocumentVersion newVersion = (DocumentVersion) lastVersion.clone();
    newVersion.setCreationDate(new java.util.Date());
    newVersion.setAuthorId(userId);
    newVersion.setComments("");

    addOrderedValidatedVersion(doc, newVersion);
  }

  public void updateDocument(Document documentToUpdate) {
    Connection con = openConnection();
    try {
      VersioningDAO.updateDocument(con, documentToUpdate);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.updateDocument",
          SilverpeasRuntimeException.ERROR,
          "versioning.UPDATING_DOCUMENT_FAILED", documentToUpdate, re);
    } finally {
      closeConnection(con);
    }
  }

  public void updateDocumentVersion(DocumentVersion documentVersion) {
    Connection con = openConnection();
    try {
      VersioningDAO.updateDocumentVersion(con, documentVersion);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.updateDocumentVersion",
          SilverpeasRuntimeException.ERROR,
          "versioning.UPDATING_DOCUMENT_VERSION_FAILED", documentVersion, re);
    } finally {
      closeConnection(con);
    }
  }

  public void updateDocumentForeignKey(DocumentPK documentPK,
      ForeignPK foreignKey) {
    Connection con = openConnection();
    try {
      VersioningDAO.updateDocumentForeignKey(con, documentPK, foreignKey);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.updateDocument",
          SilverpeasRuntimeException.ERROR,
          "versioning.UPDATING_DOCUMENT_FAILED", documentPK, re);
    } finally {
      closeConnection(con);
    }
  }

  public void updateWorkList(Document document) {
    Connection con = openConnection();
    try {
      VersioningDAO.updateWorkList(con, document);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.updateWorkList",
          SilverpeasRuntimeException.ERROR,
          "versioning.UPDATING_WORKER_LIST_FAILED", document, re);
    } finally {
      closeConnection(con);
    }
  }

  public void deleteWorkList(Document document) {
    deleteWorkList(document, false);
  }

  public void deleteWorkList(Document document, boolean keepSaved) {
    Connection con = openConnection();
    try {
      VersioningDAO.deleteWorkList(con, document, keepSaved);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.deleteWorkList",
          SilverpeasRuntimeException.ERROR,
          "versioning.DELETING_WORKER_LIST_FAILED", document, re);
    } finally {
      closeConnection(con);
    }
  }

  public ArrayList<DocumentVersion> getDocumentVersions(DocumentPK documentPK) {
    Connection con = openConnection();
    ArrayList<DocumentVersion> result = null;

    try {
      result = (ArrayList<DocumentVersion>) VersioningDAO.getDocumentVersions(con, documentPK);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getDocumentVersions",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_ALL_DOCUMENT_VERSIONS_FAILED", documentPK, re);
    } finally {
      closeConnection(con);
    }

    if (result == null) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getDocumentVersions",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_ALL_DOCUMENT_VERSIONS_FAILED",
          "No versions found" + documentPK);
    }

    return result;
  }

  public List<DocumentVersion> getAllPublicDocumentVersions(DocumentPK documentPK) {
    Connection con = openConnection();

    try {
      return VersioningDAO.getAllPublicDocumentVersions(con,
          documentPK);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getAllPublicDocumentVersions",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_ALL_PUBLIC_DOCUMENT_VERSIONS_FAILED", documentPK,
          re);
    } finally {
      closeConnection(con);
    }
  }

  public DocumentVersion getLastPublicDocumentVersion(DocumentPK documentPK) {
    Connection con = openConnection();

    try {
      return VersioningDAO.getLastPublicDocumentVersion(con, documentPK);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getLastDocumentVersion",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_LAST_DOCUMENT_VERSION_FAILED", documentPK, re);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * From Ejb remote interface. Containing business logic for adding new version
   * 
   * @param doc
   * @param newVersion
   * @return 
   */
  public DocumentVersion addDocumentVersion(Document doc, DocumentVersion newVersion) {
    newVersion.setDocumentPK(doc.getPk());
    int typeWorkList = doc.getTypeWorkList();
    if (typeWorkList == 0) {
      return addNonOrderedNonValidatedVersion(doc, newVersion);
    } else if (typeWorkList == 1) {
      return addNonOrderedValidatedVersion(doc, newVersion);
    } else if (typeWorkList == 2) {
      return addOrderedValidatedVersion(doc, newVersion);
    } else {
      return null;
    }
  }

  /**
   * Adding new <code>DocumentVersion</code> in a case of Non-ordered and Non-validated document
   * type
   * @param doc
   * @param newVersion
   * @return 
   */
  public DocumentVersion addNonOrderedNonValidatedVersion(Document doc, DocumentVersion newVersion) {
    if (newVersion.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
      newVersion.setMinorNumber(0);
      newVersion.setMajorNumber(newVersion.getMajorNumber() + 1);
    } else {
      newVersion.setMinorNumber(newVersion.getMinorNumber() + 1);
    }
    Connection con = null;

    try {
      con = openConnection();
      DocumentVersionPK pk = addVersion(newVersion);
      if (pk == null) {
        throw new VersioningRuntimeException("VersioninBmEJB.addNonOrderedNonValidatedVersion",
            SilverpeasRuntimeException.ERROR, "versioning.CANNOT_CREATE_NEWVERSION", newVersion);
      }
      newVersion.setPk(pk);
      VersioningDAO.checkDocumentIn(con, newVersion.getDocumentPK());
    } catch (SQLException e) {
      throw new VersioningRuntimeException("VersioninBmEJB.addNonOrderedNonValidatedVersion",
          SilverpeasRuntimeException.ERROR, "versioning.ADDVERSION_EXCEPTION", newVersion, e);
    } finally {
      closeConnection(con);
    }

    return newVersion;
  }

  /**
   * Adding new <code>DocumentVersion</code> in a case of Non-ordered and Validated document type.
   * @param doc
   * @param newVersion
   * @return 
   */
  public DocumentVersion addNonOrderedValidatedVersion(Document doc, DocumentVersion newVersion) {
    boolean validate = false;
    int currUser = doc.getOwnerId();
    Worker worker = findValidator(doc.getWorkList());
    Connection con = null;

    try {
      con = openConnection();
      if (worker.getId() == newVersion.getAuthorId()
          && newVersion.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
        con = openConnection();
        newVersion.setDocumentPK(doc.getPk());

        newVersion.setMinorNumber(0);
        newVersion.setMajorNumber(newVersion.getMajorNumber() + 1);
        newVersion.setType(DocumentVersion.TYPE_PUBLIC_VERSION);

        newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
        try {
          DocumentVersionPK pk = VersioningDAO.addDocumentVersion(con, newVersion);
          if (pk == null) {
            throw new VersioningRuntimeException(
                "VersioninBmEJB.addNonOrderedValidatedVersion",
                SilverpeasRuntimeException.ERROR,
                "versioning.CANNOT_CREATE_NEWVERSION", newVersion);
          }
        } catch (SQLException e) {
          throw new VersioningRuntimeException(
              "VersioninBmEJB.addNonOrderedNonValidatedVersion",
              SilverpeasRuntimeException.ERROR,
              "versioning.ADDVERSION_EXCEPTION", newVersion, e);
        }

      } else {
        if (newVersion.getType() == DocumentVersion.TYPE_PUBLIC_VERSION) {
          newVersion.setType(DocumentVersion.TYPE_DEFAULT_VERSION);
          newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_REQUIRED);
          validate = true;
        }
        newVersion.setMinorNumber(newVersion.getMinorNumber() + 1);

        DocumentVersionPK pk = null;

        pk = addVersion(newVersion);
        if (pk == null) {
          throw new VersioningRuntimeException(
              "VersioninBmEJB.addNonOrderedValidatedVersion",
              SilverpeasRuntimeException.ERROR,
              "versioning.CANNOT_CREATE_NEWVERSION", newVersion);
        }

        newVersion.setPk(pk);
      }
      VersioningDAO.checkDocumentIn(con, newVersion.getDocumentPK());

      if (!validate) {
        return newVersion;
      }

      if (worker == null) {
        throw new VersioningRuntimeException(
            "VersioninBmEJB.addNonOrderedValidatedVersion",
            SilverpeasRuntimeException.ERROR,
            "versioning.EX_NOVALIDATOR_IN_VALIDATED_LIST", doc);
      }
      String docUrl = getDocumentUrl(doc);

      try {
        sendNotification(worker.getId(), "notification.pleaseValidate", docUrl,
            currUser, doc);
      } catch (NotificationManagerException e) {
        SilverTrace.error("versioning",
            "VersioningBmEJB.addNonOrderedValidatedVersion",
            "versioning.VALIDATE_DOCUMENT_FAILED", e);
      }

      checkDocumentOut(doc.getPk(), worker.getId(), new Date());

    } catch (SQLException e) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.addNonOrderedValidatedVersion",
          SilverpeasRuntimeException.ERROR, "versioning.ADDVERSION_EXCEPTION",
          newVersion, e);
    } finally {
      closeConnection(con);
    }

    return newVersion;
  }

  /**
   * Finds first validator in list of workers
   */
  private Worker findValidator(ArrayList<Worker> workList) {
    for (int i = 0; i < workList.size(); i++) {
      Worker worker = workList.get(i);
      if (worker.isApproval()) {
        return worker;
      }
    }
    return null;
  }

  /**
   * Adding new <code>DocumentVersion</code> in a case of Ordered and Validated document type
   */
  public DocumentVersion addOrderedValidatedVersion(Document doc, DocumentVersion newVersion) {

    int currUserNum = doc.getCurrentWorkListOrder();
    List<Worker> workList = doc.getWorkList();
    Worker nextWorker = null;
    int nextUserID = -1;
    Connection con = openConnection();

    try {
      Worker currWorker = workList.get(currUserNum);
      nextWorker = getNextWorker(workList, currUserNum, true);

      if (nextWorker != null) {
        nextUserID = nextWorker.getId();
        if (currWorker.getOrder() == nextWorker.getOrder() || !nextWorker.isWriter()) {
          newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_REQUIRED);
        } else {
          newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
        }
        doc.setCurrentWorkListOrder(nextWorker.getOrder());

      } else {
        // end of list
        doc.setCurrentWorkListOrder(0);
        nextUserID = -1;
      }
      updateDocument(doc);

      if (nextWorker == null) {
        newVersion.setMinorNumber(0);
        newVersion.setMajorNumber(newVersion.getMajorNumber() + 1);
        newVersion.setType(DocumentVersion.TYPE_PUBLIC_VERSION);
      } else {
        newVersion.setMinorNumber(newVersion.getMinorNumber() + 1);
        newVersion.setType(DocumentVersion.TYPE_DEFAULT_VERSION);
      }

      DocumentVersionPK pk = addVersion(newVersion);
      if (pk == null) {
        throw new VersioningRuntimeException(
            "VersioninBmEJB.addOrderedValidatedVersion",
            SilverpeasRuntimeException.ERROR,
            "versioning.CANNOT_CREATE_NEWVERSION", newVersion);
      }
      newVersion.setPk(pk);

      if (nextUserID != -1 && nextUserID != doc.getOwnerId()) {
        VersioningDAO.checkDocumentIn(con, doc.getPk());
        checkDocumentOut(doc.getPk(), nextUserID, new java.util.Date());

        String docUrl = getDocumentUrl(doc);
        try {
          String message =
              (newVersion.getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED)
              ? "notification.pleaseValidate": "notification.processWork";
          sendNotification(nextUserID, message, docUrl, currWorker.getId(), doc);
        } catch (NotificationManagerException e) {
          SilverTrace.error("versioning", "VersioningBmEJB.sendNotification",
              "root.EX_NOTIFY_USERS_FAILED", Integer.toString(nextUserID), e);
        }

      } else if (nextUserID == -1) {
        VersioningDAO.checkDocumentIn(con, doc.getPk());
      }

    } catch (SQLException e) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.addOrderedValidatedVersion",
          SilverpeasRuntimeException.ERROR, "versioning.ADDVERSION_EXCEPTION",
          newVersion, e);

    } catch (ArrayIndexOutOfBoundsException e) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.addOrderedValidatedVersion",
          SilverpeasRuntimeException.ERROR,
          "versioning.EX_CURRENT_WORKLIST_ORDER_OUTOFBOUNDS", newVersion, e);

    } catch (VersioningRuntimeException e) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.addOrderedValidatedVersion",
          SilverpeasRuntimeException.ERROR, "versioning.ADDVERSION_EXCEPTION",
          newVersion, e);

    } finally {
      closeConnection(con);
    }

    return newVersion;
  }

  /**
   * @param workList <code>ArrayList</code> of WorkList
   * @param skipThisValidator should the next worker be a validator or we interested only in writes
   * @param currentUserNum number of currently working user
   * @return nextWorker to work with document. Null if the end of document reached
   * @throws ArrayIndexOutOfBoundsException in a case of incorrect currentUserNum index provided
   */
  protected Worker getNextWorker(List<Worker> workList, int currentUserNum,
      boolean skipThisValidator) throws ArrayIndexOutOfBoundsException {
    Worker currWorker = workList.get(currentUserNum);
    if (currWorker.isApproval() && !skipThisValidator) {
      return currWorker;
    }

    int currentOrder = currentUserNum + 1;
    Worker nextWorker = null;

    for (; currentOrder < workList.size(); currentOrder++) {
      nextWorker = workList.get(currentOrder);
      if (nextWorker.isApproval() || nextWorker.isWriter()) {
        return nextWorker;
      }
    }

    return null;
  }

  /**
   * @param workList vector of WorkList
   * @param currentUserNum number of currently working user
   * @param skipThisWorker should the next worker be a validator or we interested only in writes
   * @return prevWorker to work with document. Null if it was the first user of document
   * @throws ArrayIndexOutOfBoundsException in a case of incorrect currentUserNum index provided
   */
  protected Worker getPrevWorker(ArrayList<Worker> workList, int currentUserNum,
      boolean skipThisWorker) throws ArrayIndexOutOfBoundsException {
    Worker currWorker = workList.get(currentUserNum);
    if (currWorker.isWriter() && !skipThisWorker) {
      return currWorker;
    }

    int currentOrder = currentUserNum - 1;
    Worker nextWorker = null;

    for (; currentOrder >= 0; currentOrder--) {
      nextWorker = workList.get(currentOrder);
      if (nextWorker.isWriter()) {
        return nextWorker;
      }
    }

    return null;
  }

  public DocumentVersionPK addVersion(DocumentVersion newVersion) {
    Connection con = openConnection();

    DocumentVersionPK documentVersionPK = null;
    try {
      documentVersionPK = VersioningDAO.addDocumentVersion(con, newVersion);
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningBmEJB.addVersion",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", "", e);
    } finally {
      closeConnection(con);
    }
    return documentVersionPK;
  }

  public void deleteDocument(DocumentPK document_pk) {
    Connection con = openConnection();

    try {
      Document document = getDocument(document_pk);

      // remove all versions
      List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, document_pk);
      deleteDocumentFiles(versions, document_pk);

      // remove document itself
      VersioningDAO.deleteDocument(con, document_pk);

      // remove index
      deleteIndex(document);
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningBmEJB.deleteDocument",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", "", e);
    } finally {
      closeConnection(con);
    }
  }

  public void deleteDocumentsByForeignPK(ForeignPK foreignPK) {
    try {
      List<Document> documents = getDocuments(foreignPK);
      Document document = null;
      for (int d = 0; d < documents.size(); d++) {
        document = documents.get(d);
        deleteDocument(document.getPk());
        deleteIndex(document);
      }
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.deleteDocumentsByForeignPK",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED", "", e);
    }
  }

  public void deleteDocumentsByInstanceId(String instanceId) {
    SilverTrace.info("versioning",
        "VersioningBmEJB.deleteDocumentsByInstanceId",
        "root.MSG_GEN_ENTER_METHOD", "instanceId = " + instanceId);
    Connection con = openConnection();
    try {
      VersioningDAO.deleteDocumentsByInstanceId(con, instanceId);
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.deleteDocumentsByInstanceId",
          SilverpeasRuntimeException.ERROR, "root.EX_SQL_QUERY_FAILED",
          "instanceId = " + instanceId, e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param currDoc affected document
   * @param validatorID validator user Id
   * @param comment associated comment
   * @param validationDate a date when validation process occured.
   */
  public void validateDocument(Document currDoc, int validatorID,
      String comment, java.util.Date validationDate) {
    Connection con = openConnection();
    try {
      if (isDocumentClosed(currDoc)) {
        throw new VersioningRuntimeException("VersioninBmEJB.validateDocument",
            SilverpeasRuntimeException.ERROR, "versioning.DOCUMENT_CLOSED", currDoc);
      }

      if (!isCheckoutedBy(currDoc, validatorID)) {
        throw new VersioningRuntimeException("VersioninBmEJB.validateDocument",
            SilverpeasRuntimeException.ERROR, "versioning.DOCUMENT_NOT_CHECKOUTED", currDoc);
      }
      if (currDoc.getTypeWorkList() == 1) {
        validateApprovalNonOrdered(currDoc, validatorID, comment, validationDate, con);

      } else if (currDoc.getTypeWorkList() == 2) {
        validateApprovalOrdered(currDoc, validatorID, comment, validationDate, con);

      } else {
        throw new VersioningRuntimeException("VersioninBmEJB.validateDocument",
            SilverpeasRuntimeException.ERROR,
            "versioning.UNKNOWN_DOC_WORKLIST_TYPE", currDoc);
      }

    } finally {
      closeConnection(con);
    }
  }

  /**
   * Process validation in a case of Approval, Ordered list
   * @param doc
   * @param validatorID
   * @param comment
   * @param validationDate
   * @param conn 
   */
  protected void validateApprovalOrdered(Document doc, int validatorID, String comment, 
      Date validationDate, Connection conn) {

    if (!isCurrentOrderedValidator(doc, validatorID)) {
      throw new VersioningRuntimeException("VersioninBmEJB.validateApprovalOrdered",
          SilverpeasRuntimeException.ERROR, "versioning.VALIDATION_NOT_REQUIED", doc);
    }

    boolean isPublic = false;
    int nextUserID = -1;
    int currWorker = doc.getOwnerId();

    Worker nextWorker = getNextWorker(doc.getWorkList(), doc.getCurrentWorkListOrder(), true);

    if (nextWorker == null) {
      isPublic = true;
      nextUserID = -1;
      doc.setCurrentWorkListOrder(0);
    } else {
      isPublic = false;
      nextUserID = nextWorker.getId();
      doc.setCurrentWorkListOrder(nextWorker.getOrder());
    }
    updateDocument(doc);

    DocumentVersion version = getCurrentDocumentVersion(doc);

    if (nextWorker != null && !nextWorker.isWriter() && nextWorker.isApproval()) {
      validateDocumentVersion(version, comment, validationDate, validatorID, isPublic, true, conn);
    } else {
      validateDocumentVersion(version, comment, validationDate, validatorID, isPublic, false, conn);
    }

    if (nextUserID == -1) {
      // close work with document
      try {
        VersioningDAO.checkDocumentIn(conn, doc.getPk());
      } catch (Exception e) {
        throw new VersioningRuntimeException(
            "VersioningBmEJB.validateApprovalOrdered",
            SilverpeasRuntimeException.ERROR,
            "versioning.VALIDATE_DOCUMENT__FAILED", doc, e);
      }

    } else {
      // prepare document for next user
      try {
        VersioningDAO.checkDocumentIn(conn, doc.getPk());
      } catch (Exception e) {
        throw new VersioningRuntimeException(
            "VersioningBmEJB.validateApprovalOrdered",
            SilverpeasRuntimeException.ERROR,
            "versioning.VALIDATE_DOCUMENT_FAILED", doc, e);
      }

      checkDocumentOut(doc.getPk(), nextUserID, validationDate);

      String docUrl = getDocumentUrl(doc);
      try {
        sendNotification(nextUserID, "notification.processWork", docUrl,
            currWorker, doc); // notification

      } catch (NotificationManagerException e) {
        SilverTrace.error("versioning",
            "VersioningBmEJB.validateApprovalOrdered()",
            "root.EX_NOTIFY_USERS_FAILED", Integer.toString(nextUserID), e);
      }
    }
  }

  /**
   * Perform work on creating validated version from version provided
   * @param docVersion version to be validated
   */
  protected DocumentVersion validateDocumentVersion(DocumentVersion docVersion,
      String comment, java.util.Date validationDate, int validatorId,
      boolean isPublic, boolean needValidation, Connection conn) {
    DocumentVersion newVersion = (DocumentVersion) docVersion.clone();

    if (isPublic) {
      newVersion.setMinorNumber(0);
      newVersion.setMajorNumber(newVersion.getMajorNumber() + 1);
      newVersion.setType(DocumentVersion.TYPE_PUBLIC_VERSION);
    } else {
      newVersion.setMinorNumber(newVersion.getMinorNumber() + 1);
      newVersion.setType(DocumentVersion.TYPE_DEFAULT_VERSION);
    }

    if (needValidation) {
      newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_REQUIRED);
    } else {
      newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);
    }

    newVersion.setAuthorId(validatorId);
    newVersion.setCreationDate(validationDate);
    newVersion.setComments(comment);

    try {
      DocumentVersionPK pk = VersioningDAO.addDocumentVersion(conn, newVersion);
      if (pk == null) {
        throw new VersioningRuntimeException(
            "VersioninBmEJB.addNonOrderedNonValidatedVersion",
            SilverpeasRuntimeException.ERROR,
            "versioning.CANNOT_CREATE_NEWVERSION", newVersion);
      }

      docVersion.setStatus(DocumentVersion.STATUS_VERSION_VALIDATED);
      VersioningDAO.updateDocumentVersion(conn, docVersion);
      return newVersion;
    } catch (SQLException e) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.addNonOrderedNonValidatedVersion",
          SilverpeasRuntimeException.ERROR, "versioning.ADDVERSION_EXCEPTION",
          newVersion, e);
    }
  }

  /**
   * Process validation in a case of Approval non Ordered validation
   */
  protected void validateApprovalNonOrdered(Document doc, int validatorID,
      String comment, java.util.Date validationDate, Connection conn) {
    DocumentVersion currVersion = getCurrentDocumentVersion(doc);
    int currWorker = doc.getOwnerId();

    if (!isNonOrderedValidator(doc, currVersion, validatorID)) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.validateApprovalNonOrdered",
          SilverpeasRuntimeException.ERROR,
          "versioning.VALIDATION_NOT_REQUIED", doc);
    }

    validateDocumentVersion(currVersion, comment, validationDate, validatorID,
        true, false, conn);

    try {
      VersioningDAO.checkDocumentIn(conn, doc.getPk());
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.validateApprovalNonOrdered",
          SilverpeasRuntimeException.ERROR,
          "versioning.VALIDATION_NOT_REQUIED", doc, e);
    }

    int versionCreatorID = currVersion.getAuthorId();

    String documentUrl = getDocumentUrl(doc);
    try {
      sendNotification(versionCreatorID, "notification.workValidated",
          documentUrl, currWorker, doc);
    } catch (NotificationManagerException e) {
      SilverTrace.error("versioning", "VersioningBmEJB.sendNotification",
          "root.EX_NOTIFY_USERS_FAILED", Integer.toString(versionCreatorID), e);
    }
  }

  /**
   * @param doc document to be checked
   * @param userID to be checked to be the current document validator
   * @return true if userID provided can validate current version of document
   */
  protected boolean isCurrentOrderedValidator(Document doc, int userID) {
    if (doc.getTypeWorkList() != 2) {
      throw new IllegalArgumentException(
          "Type of work list in provided document isn't correct");
    }
    int order = doc.getCurrentWorkListOrder();

    List<Worker> wList = doc.getWorkList();
    try {
      Worker worker = wList.get(order);
      if (worker.getId() == userID) {
        return true;
      }
      return false;
    } catch (ArrayIndexOutOfBoundsException ex) {
      SilverTrace.error("versioning", "VersioningBmEJB.isCurrentValidator",
          "versioning.DOCUMENT_CURRENTORDER_UNCONSTENT");
      return false;
    }
  }

  /**
   * @param doc document to be checked
   * @param userID to be checket to be the current document validator
   * @param currentVersion of the document to be checked
   * @return true if userID provided can validate current version of document
   */
  boolean isNonOrderedValidator(Document doc, DocumentVersion currentVersion,
      int userID) {
    if (doc.getTypeWorkList() != 1) {
      throw new IllegalArgumentException(
          "Type of work list in provided document isn't correct");
    }

    List<Worker> wList = doc.getWorkList();
    DocumentVersion version = getCurrentDocumentVersion(doc);

    if (!(version.getStatus() == DocumentVersion.STATUS_VALIDATION_REQUIRED)) {
      return false;
    }

    for (int i = 0; i < wList.size(); i++) {
      Worker worker = wList.get(i);

      if (worker.isApproval() && worker.getId() == userID) {
        return true; // providede user can validate
      }
    }
    return false; // we cannot find provied user in work list with validation
    // rights
  }

  /**
   * @param doc document to be checked to be checkedout
   * @param userID to be checked to be checjedout user of document
   * @return true if document provided has been checked out by provided userID
   */
  protected boolean isCheckoutedBy(Document doc, int userID) {
    int ownerID = doc.getOwnerId();
    int status = doc.getStatus();
    if (status == 0 || ownerID != userID) {
      return false;
    }
    return true;
  }

  /**
   * @return true if document is closed and we cannot make any opertion with it
   */
  protected boolean isDocumentClosed(Document doc) {
    // return (currDoc.getStatus() == 1)? true : false;
    return false; // our documents are never closed
  }

  /**
   * @param currDoc affected document
   * @param validatorID validator user Id
   * @param comment associated comment
   * @param validationDate a date when validation process occured.
   */
  public void refuseDocument(Document currDoc, int validatorID, String comment,
      java.util.Date validationDate) {
    Connection con = openConnection();
    try {
      if (isDocumentClosed(currDoc)) {
        throw new VersioningRuntimeException("VersioninBmEJB.refuseDocument",
            SilverpeasRuntimeException.ERROR, "versioning.DOCUMENT_CLOSED",
            currDoc);
      }

      if (!isCheckoutedBy(currDoc, validatorID)) {
        throw new VersioningRuntimeException("VersioninBmEJB.refuseDocument",
            SilverpeasRuntimeException.ERROR,
            "versioning.DOCUMENT_NOT_CHECKOUTED", currDoc);
      }

      if (currDoc.getTypeWorkList() == 1) {
        refuseApprovalNonOrdered(currDoc, validatorID, comment, validationDate,
            con);
      } else if (currDoc.getTypeWorkList() == 2) {
        refuseApprovalOrdered(currDoc, validatorID, comment, validationDate,
            con);
      } else {
        throw new VersioningRuntimeException("VersioninBmEJB.refuseDocument",
            SilverpeasRuntimeException.ERROR,
            "versioning.UNKNOWN_DOC_WORKLIST_TYPE", currDoc);
      }

    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.refuseDocument()",
          SilverpeasRuntimeException.ERROR,
          "versioning.REFUSE_DOCUMENT_FAILED", re);
    } finally {
      closeConnection(con);
    }

  }

  /**
   * Process validation refuse for Approval non Ordered validation
   */
  protected void refuseApprovalNonOrdered(Document doc, int validatorID,
      String comment, java.util.Date validationDate, Connection conn) {
    DocumentVersion currVersion = getCurrentDocumentVersion(doc);
    int currWorkerid = doc.getOwnerId();

    if (!isNonOrderedValidator(doc, currVersion, validatorID)) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.refuseApprovalNonOrdered",
          SilverpeasRuntimeException.ERROR,
          "versioning.VALIDATION_NOT_REQUIED", doc);
    }

    int versionCreator = currVersion.getAuthorId();
    refuseDocumentVersion(currVersion, comment, validationDate, validatorID,
        conn);

    try {
      VersioningDAO.checkDocumentIn(conn, doc.getPk());
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.refuseApprovalNonOrdered",
          SilverpeasRuntimeException.ERROR,
          "versioning.VALIDATION_NOT_REQUIED", doc, e);
    }

    int versionCreatorID = currVersion.getAuthorId();

    String documentUrl = getDocumentUrl(doc);
    try {
      sendNotification(versionCreator, "notification.refuseWork", documentUrl,
          currWorkerid, doc);
    } catch (NotificationManagerException e) {
      SilverTrace.error("versioning", "VersioningBmEJB.sendNotification",
          "root.EX_NOTIFY_USERS_FAILED", Integer.toString(versionCreatorID), e);
    }
  }

  /**
   * Process validation refuse for Approval, Ordered list
   */
  protected void refuseApprovalOrdered(Document doc, int validatorID,
      String comment, java.util.Date validationDate, Connection conn) {

    if (!isCurrentOrderedValidator(doc, validatorID)) {
      throw new VersioningRuntimeException("VersioninBmEJB.refuseApprovalOrdered",
          SilverpeasRuntimeException.ERROR, "versioning.VALIDATION_NOT_REQUIED", doc);
    }
    int currWorkerid = doc.getOwnerId();

    Worker prevWorker = getPrevWorker(doc.getWorkList(), doc.getCurrentWorkListOrder(), true);
    int nextUserID = -1;
    if (prevWorker != null) {
      nextUserID = prevWorker.getId();
    }

    DocumentVersion version = getCurrentDocumentVersion(doc);
    refuseDocumentVersion(version, comment, validationDate, validatorID, conn);
    try {
      VersioningDAO.checkDocumentIn(conn, doc.getPk());
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.refuseApprovalOrdered",
          SilverpeasRuntimeException.ERROR,
          "versioning.VALIDATION_NOT_REQUIED", doc, e);
    }

    if (nextUserID != -1) {
      // checkout document for previouse user
      doc.setCurrentWorkListOrder(prevWorker.getOrder());
      updateDocument(doc);
      checkDocumentOut(doc.getPk(), nextUserID, validationDate);
      String documentUrl = getDocumentUrl(doc);
      try {
        sendNotification(nextUserID, "notification.refuseWork", documentUrl,
            currWorkerid, doc); // notification
      } catch (NotificationManagerException e) {
        SilverTrace.error("versioning", "VersioningBmEJB.sendNotification",
            "root.EX_NOTIFY_USERS_FAILED", Integer.toString(nextUserID), e);
      }
    }
  }

  /**
   * Perform work on creating validated version from version provided
   * @param docVersion version to be validated
   */
  protected DocumentVersion refuseDocumentVersion(DocumentVersion docVersion,
      String comment, java.util.Date validationDate, int validatorId,
      Connection conn) {
    DocumentVersion newVersion = (DocumentVersion) docVersion.clone();
    ;
    newVersion.setType(DocumentVersion.TYPE_DEFAULT_VERSION);
    newVersion.setStatus(DocumentVersion.STATUS_VALIDATION_NOT_REQ);

    newVersion.setMinorNumber(newVersion.getMinorNumber() + 1);

    newVersion.setAuthorId(validatorId);
    newVersion.setCreationDate(validationDate);
    newVersion.setComments(comment);
    try {
      DocumentVersionPK pk = VersioningDAO.addDocumentVersion(conn, newVersion);
      if (pk == null) {
        throw new VersioningRuntimeException("VersioninBmEJB.refuseDocument",
            SilverpeasRuntimeException.ERROR,
            "versioning.CANNOT_CREATE_NEWVERSION", newVersion);
      }

      docVersion.setStatus(DocumentVersion.STATUS_VERSION_REFUSED);
      VersioningDAO.updateDocumentVersion(conn, docVersion);
      return newVersion;
    } catch (SQLException e) {
      throw new VersioningRuntimeException("VersioninBmEJB.refuseDocument",
          SilverpeasRuntimeException.ERROR, "versioning.ADDVERSION_EXCEPTION",
          newVersion, e);
    }
  }

  protected DocumentVersion getCurrentDocumentVersion(Document doc) {
    Connection con = openConnection();
    DocumentVersion result = null;

    try {
      List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, doc.getPk());
      result = versions.get(0);
    } catch (SQLException e) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.getCurrentDocumentVersion",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_SQL_QUERY_FAILED", doc.getPk());
    } finally {
      closeConnection(con);
    }

    if (result == null) {
      throw new VersioningRuntimeException(
          "VersioninBmEJB.getCurrentDocumentVersion",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_SQL_QUERY_FAILED", doc.getPk());
    }
    return result;
  }

  /**
   * Utility method. Allows us to send notification to specified user
   */
  protected void sendNotification(int userID, String notificationMessageKey,
      String documentUrl, int fromUser, Document doc)
      throws NotificationManagerException {
    String language = getDefaultUserLanguage(userID);
    ResourceLocator resources = new ResourceLocator(
        "com.stratelia.webactiv.util.versioning.multilang.versioning", language);
    StringBuffer message = new StringBuffer();
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);

    OrganizationController orgCtr = new OrganizationController();

    String space_label = "";
    String component_label = "";
    String spaceId = doc.getPk().getSpace();

    SpaceInst spaceInst = orgCtr.getSpaceInstById(spaceId);

    if (spaceInst != null) {
      space_label = spaceInst.getName();
    } else {
      space_label = spaceId;
    }

    ComponentInst componentInst = null;
    componentInst = orgCtr.getComponentInst(doc.getPk().getComponentName());

    if (componentInst != null) {
      if (componentInst.getLabel().length() > 0) {
        component_label = componentInst.getLabel();
      } else {
        component_label = componentInst.getName();
      }
    } else {
      component_label = doc.getPk().getComponentName();
    }

    message.append(resources.getString(notificationMessageKey));
    message.append("\n");

    message.append(resources.getString("versioning.DocumentName"));
    message.append(" - ");
    message.append(doc.getName());
    message.append("\n");

    message.append(resources.getString("versioning.Space"));
    message.append(" - ");
    message.append(space_label);
    message.append("\n");

    message.append(resources.getString("versioning.Component"));
    message.append(" - ");
    message.append(component_label);
    message.append("\n");

    message.append(resources.getString("versioning.Date"));
    message.append(" - ");
    message.append(formatter.format(new java.util.Date()));
    message.append("\n");

    message.append(resources.getString("versioning.URL"));
    message.append(" - ");
    message.append(documentUrl);

    String[] notifUserList = new String[1];
    notifUserList[0] = Integer.toString(userID);

    NotificationSender notifSender = new NotificationSender(doc.getPk().getComponentName());
    NotificationMetaData notifMetaData = new NotificationMetaData(
        NotificationParameters.NORMAL, resources.getString("notification.title"), message.toString());

    int adminId = getFirstAdministrator(orgCtr, userID);

    notifMetaData.setSender(String.valueOf(adminId));
    notifMetaData.addUserRecipients(notifUserList);
    notifMetaData.setSource(resources.getString("notification.title"));

    if ((documentUrl != null)
        && (documentUrl.length() > URLManager.getApplicationURL().length())) {
      notifMetaData.setLink(documentUrl.substring(URLManager.getApplicationURL().length())); // Remove the application element
    }
    notifSender.notifyUser(notifMetaData);
  }

  /**
   * @return first administrator id
   */
  private int getFirstAdministrator(
      OrganizationController organizationController, int userId) {
    int fromUserID = -1;
    String[] admins = organizationController.getAdministratorUserIds(Integer.toString(userId));
    if (admins != null && admins.length > 0) {
      fromUserID = Integer.parseInt(admins[0]);
    }
    return fromUserID;
  }

  /**
   * Utility method
   * @return an URL for provided document
   */
  protected String getDocumentUrl(Document doc) {
    String url = URLManager.getApplicationURL() + "/RVersioningPeas/"
        + doc.getPk().getSpace() + "_" + doc.getPk().getComponentName()
        + "/versions.jsp?DocId=" + doc.getPk().getId() + "&Id="
        + doc.getForeignKey().getId() + "&SpaceId=" + doc.getPk().getSpace()
        + "&ComponentId=" + doc.getPk().getComponentName() + "&Context=Images";
    return url;
  }

  /**
   * Utility method
   * @return language by provided user id
   */
  protected String getDefaultUserLanguage(int userID) {
    String lang = "fr";
    try {
      PersonalizationBmHome personalizationBmHome = (PersonalizationBmHome) EJBUtilitaire.
          getEJBObjectRef(JNDINames.PERSONALIZATIONBM_EJBHOME,
          PersonalizationBmHome.class);
      PersonalizationBm personalizationBm = personalizationBmHome.create();
      personalizationBm.setActor(String.valueOf(userID));
      lang = personalizationBm.getFavoriteLanguage();
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getDefaultUserLanguage()",
          VersioningRuntimeException.ERROR,
          "root.EX_CANT_GET_PREFERRED_USER_LANG", e);
    }
    return lang;
  }

  /**
   * Get the last version
   * @param documentPK
   * @return DocumentVersion
   */
  public DocumentVersion getLastDocumentVersion(DocumentPK documentPK) {
    Connection con = openConnection();
    DocumentVersion result = null;
    try {
      List<DocumentVersion> versions = VersioningDAO.getDocumentVersions(con, documentPK);
      result = versions.get(0);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getLastDocumentVersion",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_LAST_DOCUMENT_VERSION_FAILED", documentPK, re);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  /**
   * Get a document version
   * @param documentVersionPK
   * @return DocumentVersion
   */
  public DocumentVersion getDocumentVersion(DocumentVersionPK documentVersionPK) {
    Connection con = openConnection();
    DocumentVersion result = null;
    try {
      result = VersioningDAO.getDocumentVersion(con, documentVersionPK);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getDocumentVersion",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_DOCUMENT_VERSION_FAILED", documentVersionPK, re);
    } finally {
      closeConnection(con);
    }
    return result;
  }

  /**
   * Utility method
   * @return Connection to use in all ejb db operations
   */
  protected Connection openConnection() {
    Connection con = null;
    try {
      con = DBUtil.makeConnection(JNDINames.VERSIONING_DATASOURCE);
    } catch (Exception e) {
      throw new VersioningRuntimeException("VersioningBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  /**
   * Utility method. Closes a connection opened by @link #openConnection() method
   */
  protected void closeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("versioning", "VersioningBmEJB.closeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private void deleteIndex(Document document) {
    SilverTrace.info("versioning", "VersioningBmEJB.deleteIndex()",
        "root.MSG_GEN_ENTER_METHOD", "documentPK = "
        + document.getPk().toString());

    IndexEntryPK indexEntry = new IndexEntryPK(document.getPk().getComponentName(), "Versioning" + document.
        getPk().getId(), document.getForeignKey().getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  private void deleteDocumentFiles(List<DocumentVersion> versions, DocumentPK documentPK) {
    String[] ctx = {"Versioning"};
    String path = FileRepositoryManager.getAbsolutePath(documentPK.getInstanceId(), ctx);

    // for each version, we remove according file
    DocumentVersion version = null;
    for (int v = 0; v < versions.size(); v++) {
      version = versions.get(v);
      deleteVersionFile(version, path);
    }
  }

  private void deleteVersionFile(DocumentVersion version, String path) {
    String fileName = version.getPhysicalName();
    try {
      FileFolderManager.deleteFile(path + fileName);
    } catch (Exception e) {
      SilverTrace.warn("versioning", "VersioningBmEJB.deleteVersionFile()",
          "root.EX_CANT_DELETE_FILE", "path = " + path);
    }
  }

  public List<Document> getAllFilesReserved(int ownerId) {
    Connection con = openConnection();
    try {
      return VersioningDAO.getAllFilesReserved(con, ownerId);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getAllFilesReserved",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_ALL_PUBLIC_DOCUMENT_VERSIONS_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  public List<Document> getAllFilesReservedByDate(Date date, boolean alert) {
    Connection con = openConnection();

    try {
      return VersioningDAO.getAllFilesReservedByDate(con, date, alert);
    } catch (Exception re) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getAllFilesReservedByDate",
          SilverpeasRuntimeException.ERROR,
          "versioning.GETTING_ALL_PUBLIC_DOCUMENT_VERSIONS_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }

  public Collection<Document> getAllDocumentsToLib(Date date)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      return VersioningDAO.getAllDocumentsToLib(con, date);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getAllAttachmentToLib()", SilverpeasException.ERROR,
          "versioning.EX_RECORD_NOT_LOAD", se);
    } finally {
      closeConnection(con);
    }
  }

  public void notifyUser(NotificationMetaData notifMetaData, String senderId,
      String componentId) throws VersioningRuntimeException {
    Connection con = openConnection();
    SilverTrace.info("versioning", "VersioningBmEJB.notifyUser()",
        "root.MSG_GEN_EXIT_METHOD");
    try {
      notifMetaData.setConnection(con);
      if (notifMetaData.getSender() == null
          || notifMetaData.getSender().length() == 0) {
        notifMetaData.setSender(senderId);
      }
      NotificationSender notifSender = new NotificationSender(componentId);
      notifSender.notifyUser(notifMetaData);
    } catch (NotificationManagerException e) {
      throw new VersioningRuntimeException("VersioningBmEJB.notifyUser()",
          SilverpeasRuntimeException.ERROR,
          "versioning.MSG_DOCUMENT_NOT_EXIST", e);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * EJB Required method
   */
  public void ejbCreate() throws CreateException {
  }

  /**
   * EJB Required method
   */
  public void ejbRemove() {
  }

  /**
   * EJB Required method
   */
  public void ejbActivate() {
  }

  /**
   * EJB Required method
   */
  public void ejbPassivate() {
  }

  /**
   * EJB Required method
   */
  public void setSessionContext(SessionContext sc) {
  }

  /**
   * @param role
   * @param userId
   * @param groupsIds
   * @param usersIds
   * @return
   * @throws VersioningRuntimeException
   */
  public void saveReadersAccessList(String componentId, ArrayList<String> groupIds,
      ArrayList<String> userIds) throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      VersioningDAO.insertReadersAccessList(con, componentId, groupIds, userIds);
    } catch (SQLException se) {
      throw new VersioningRuntimeException("VersioningBmEJB.saveAccessList()",
          SilverpeasException.ERROR, "versioning.EX_SAVE_ACCESSLIST_FAILED", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param componentId
   * @param documentId
   * @param listType
   * @throws VersioningRuntimeException
   */
  public void saveWorkersAccessList(String componentId, String documentId,
      int listType) throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      WorkListDAO.saveWorkersAccessList(con, componentId, documentId, listType);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.saveWorkersAccessList()", SilverpeasException.ERROR,
          "versioning.EX_SAVE_ACCESSLIST_FAILED", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param role
   * @param componentId
   * @throws VersioningRuntimeException
   */
  public void removeReadersAccessList(String componentId)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      VersioningDAO.removeReadersAccessList(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.removeAccessList()", SilverpeasException.ERROR,
          "versioning.EX_REMOVE_ACCESSLIST_FAILED", se);
    } finally {
      closeConnection(con);
    }
  }

  public void removeWorkersAccessList(String componentId)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      WorkListDAO.removeAccessList(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.removeAccessList()", SilverpeasException.ERROR,
          "versioning.EX_REMOVE_ACCESSLIST_FAILED", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param documentId
   * @param role
   * @param userId
   * @return
   * @throws VersioningRuntimeException
   */
  public ArrayList<String> getReadersAccessListUsers(String componentId)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      return (ArrayList<String>) VersioningDAO.getReadersAccessListUsers(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getReadersAccessListUsers()",
          SilverpeasException.ERROR, "versioning.EX_GET_ACCESSLIST_USERS", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param componentId
   * @return
   * @throws VersioningRuntimeException
   */
  public ArrayList<Worker> getWorkersAccessListUsers(String componentId)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      return (ArrayList<Worker>) WorkListDAO.getWorkersAccessListUsers(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getWorkersAccessListUsers()",
          SilverpeasException.ERROR, "versioning.EX_GET_ACCESSLIST_USERS", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param documentId
   * @param role
   * @param userId
   * @return
   * @throws VersioningRuntimeException
   */
  public ArrayList<String> getReadersAccessListGroups(String componentId)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      return (ArrayList<String>) VersioningDAO.getReadersAccessListGroups(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getReadersAccessListGroups()",
          SilverpeasException.ERROR, "versioning.EX_GET_ACCESSLIST_GROUPS", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param componentId
   * @return
   * @throws VersioningRuntimeException
   */
  public ArrayList<Worker> getWorkersAccessListGroups(String componentId)
      throws VersioningRuntimeException {
    Connection con = openConnection();
    try {
      return (ArrayList<Worker>) WorkListDAO.getWorkersAccessListGroups(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getWorkersAccessListGroups()",
          SilverpeasException.ERROR, "versioning.EX_GET_ACCESSLIST_USERS", se);
    } finally {
      closeConnection(con);
    }
  }

  /**
   * @param componentId
   * @return
   */
  public int getSavedListType(String componentId) {
    Connection con = openConnection();
    int listType = 0;
    try {
      listType = WorkListDAO.getSavedListType(con, componentId);
    } catch (SQLException se) {
      throw new VersioningRuntimeException(
          "VersioningBmEJB.getSavedListType()", SilverpeasException.ERROR,
          "versioning.EX_GET_SAVE_LIST_TYPE", se);
    } finally {
      closeConnection(con);
    }
    return listType;
  }

  public void sortDocuments(List<DocumentPK> pks) {
    Connection con = openConnection();
    try {
      VersioningDAO.sortDocuments(con, pks);
    } catch (Exception re) {
      throw new VersioningRuntimeException("VersioningBmEJB.sortDocuments",
          SilverpeasRuntimeException.ERROR, "versioning.GETTING_ALL_DOCUMENT_VERSIONS_FAILED", re);
    } finally {
      closeConnection(con);
    }
  }
}
