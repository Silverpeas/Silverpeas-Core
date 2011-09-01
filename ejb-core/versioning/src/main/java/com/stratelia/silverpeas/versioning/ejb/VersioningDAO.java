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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.versioning.model.Document;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.model.DocumentVersionPK;
import com.stratelia.silverpeas.versioning.model.Worker;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAPrimaryKey;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class VersioningDAO {
  private static final long serialVersionUID = 9119204206579998454L;

  /** Date format pattern constant. This patters is used in db operations */
  public final static String DATE_FORMAT = "yyyy/MM/dd";
  public final static String versionTableName = "SB_Version_Version";
  public final static String documentTableName = "SB_Version_Document";
  public final static String accessListTableName = "sb_doc_readers_acl";
  public final static String accessListContentTableName = "sb_doc_readers_acl_list";
  private final static int nameMaxLength = 100;
  public final static String GET_DOCUMENT_BYID_QUERY = "SELECT documentId, "
      + " documentName, documentDescription, documentStatus, documentOwnerId, "
      + " documentCheckoutDate, documentInfo, foreignId, instanceId, typeWorkList, "
      + " currentWorkListOrder, alertDate, expiryDate, documentordernum  FROM "
      + "sb_version_document WHERE documentId = ? ";
  public final static String GET_DOCUMENT_BYFOREIGNID_QUERY = "SELECT documentId, "
      + " documentName, documentDescription, documentStatus, documentOwnerId, "
      + " documentCheckoutDate, documentInfo, foreignId, instanceId, typeWorkList, "
      + " currentWorkListOrder, alertDate, expiryDate, documentordernum "
      + " FROM sb_version_document WHERE foreignId = ? and instanceId = ? ORDER BY documentordernum, documentId ";
  public final static String GET_DOCUMENTS_BYINSTANCEID_QUERY = "SELECT documentId, "
      + " documentName, documentDescription, documentStatus, documentOwnerId, "
      + " documentCheckoutDate, documentInfo, foreignId, instanceId, typeWorkList, "
      + " currentWorkListOrder, alertDate, expiryDate, documentordernum "
      + " FROM sb_version_document WHERE instanceId = ? ";
  public static final String GET_ALL_ALERT_FILES_RESERVED_BY_DATE_QUERY = "SELECT documentId, "
      + " documentName, documentDescription, documentStatus, documentOwnerId, "
      + " documentCheckoutDate, documentInfo, foreignId, instanceId, typeWorkList, "
      + " currentWorkListOrder, alertDate, expiryDate, documentordernum "
      + " FROM sb_version_document WHERE alertDate = ? ";
  public static final String GET_ALL_EXPIRY_FILES_RESERVED_BY_DATE_QUERY = "SELECT documentId, "
      + " documentName, documentDescription, documentStatus, documentOwnerId, "
      + " documentCheckoutDate, documentInfo, foreignId, instanceId, typeWorkList, "
      + " currentWorkListOrder, alertDate, expiryDate, documentordernum "
      + " FROM sb_version_document WHERE expiryDate = ? ";
  public static final String GET_ALL_FILES_RESERVED_BY_DATE_QUERY = "SELECT documentId, "
      + " documentName, documentDescription, documentStatus, documentOwnerId, "
      + " documentCheckoutDate, documentInfo, foreignId, instanceId, typeWorkList, "
      + " currentWorkListOrder, alertDate, expiryDate, documentordernum "
      + " FROM sb_version_document WHERE expiryDate < ? ";
  public final static String GET_DOCUMENT_VERSION_BYID_QUERY = "SELECT d.* FROM "
      + versionTableName + " d WHERE d.versionId = ? ";

  /**
   * @param conn
   * @param pk
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static Document getDocument(Connection conn, DocumentPK pk)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocument",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (pk == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocument",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    Document result = null;
    try {
      prepStmt = conn.prepareStatement(GET_DOCUMENT_BYID_QUERY);
      try {
        prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("VersioningDAO.getDocument",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", pk.toString(), e);
      }

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        result = getDocFormRS(rs, conn, pk.getSpace());
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }

  /**
   * Get documents
   * @param conn
   * @param foreignID
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static List<Document> getDocuments(Connection conn, WAPrimaryKey foreignID)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocuments",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (foreignID == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocuments",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Document> result = new ArrayList<Document>();

    try {
      prepStmt = conn.prepareStatement(GET_DOCUMENT_BYFOREIGNID_QUERY);
      try {
        prepStmt.setInt(1, Integer.parseInt(foreignID.getId()));
        prepStmt.setString(2, foreignID.getInstanceId());
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("VersioningDAO.getDocuments",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", foreignID.toString(), e);
      }

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        Document doc = getDocFormRS(rs, conn, foreignID.getSpace());
        result.add(doc);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }

  /**
   * @param con
   * @param instanceId
   * @return
   * @throws SQLException
   */
  public static List<Document> getDocumentsByInstanceId(Connection con,
      String instanceId) throws SQLException {
    SilverTrace.info("versioning", "VersioningDAO.getDocumentsByInstanceId()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId = " + instanceId);
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Document> result = new ArrayList<Document>();

    try {
      prepStmt = con.prepareStatement(GET_DOCUMENTS_BYINSTANCEID_QUERY);
      prepStmt.setString(1, instanceId);

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Document doc = getDocFormRS(rs, con, "useless");
        result.add(doc);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return result;
  }

  /**
   * @param rs
   * @param con
   * @param spaceID
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  private static Document getDocFormRS(ResultSet rs, Connection con,
      String spaceID) throws SQLException, VersioningRuntimeException {
    Document doc = new Document();
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    formatter.setLenient(false);
    doc.setPk(new DocumentPK(rs.getInt("documentId"), spaceID, rs.getString("instanceId")));
    doc.setName(rs.getString("documentName"));
    String description = "";
    if (rs.getString("documentDescription") != null) {
      description = rs.getString("documentDescription");
    }
    doc.setDescription(description);
    doc.setStatus(rs.getInt("documentStatus"));
    doc.setOwnerId(rs.getInt("documentOwnerId"));
    try {
      String checkoutDate = rs.getString("documentCheckoutDate");
      if (StringUtil.isDefined(checkoutDate)) {
        doc.setLastCheckOutDate(formatter.parse(checkoutDate));
      } else {
        doc.setLastCheckOutDate(null);
      }
    } catch (ParseException e) {
      throw new VersioningRuntimeException("VersioningDAO.getDocFormRS",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_CANT_PARSE_DATE", e);
    }
    String additionalInfo = "";
    if (rs.getString("documentInfo") != null) {
      additionalInfo = rs.getString("documentInfo");
    }
    doc.setAdditionalInfo(additionalInfo);
    doc.setForeignKey(new ForeignPK(String.valueOf(rs.getInt("foreignId")), ""));
    doc.setInstanceId(rs.getString("instanceId"));
    doc.setTypeWorkList(rs.getInt("typeWorkList"));
    doc.setCurrentWorkListOrder(rs.getInt("currentWorkListOrder"));
    doc.getPk().setComponentName(doc.getInstanceId());
    doc.getForeignKey().setComponentName(doc.getInstanceId());

    List<Worker> workers = WorkListDAO.getWorkers(con, doc.getPk());
    doc.setWorkList((ArrayList<Worker>) workers);
    try {
      String alertDate = rs.getString("alertDate");
      if (StringUtil.isDefined(alertDate)) {
        doc.setAlertDate(formatter.parse(alertDate));
      } else {
        doc.setAlertDate(null);
      }
    } catch (ParseException e) {
      throw new VersioningRuntimeException("VersioningDAO.getDocFormRS",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_CANT_PARSE_DATE", e);
    }
    try {
      String expiryDate = rs.getString("expiryDate");
      if (StringUtil.isDefined(expiryDate)) {
        doc.setExpiryDate(formatter.parse(expiryDate));
      } else {
        doc.setExpiryDate(null);
      }
    } catch (ParseException e) {
      throw new VersioningRuntimeException("VersioningDAO.getDocFormRS",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_CANT_PARSE_DATE", e);
    }
    doc.setOrderNumber(rs.getInt("documentordernum"));
    return doc;
  }
  public final static String CREATE_DOCUMENT_QUERY =
      "INSERT INTO sb_version_document (documentId, documentName, documentDescription, "
      + "documentStatus, documentOwnerId, documentCheckoutDate, documentInfo, foreignId, "
      + "instanceId, typeWorkList, currentWorkListOrder, alertDate, expiryDate, documentordernum) "
      + " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  /**
   * Create Document
   * @param conn
   * @param document
   * @param initialVersion
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static DocumentPK createDocument(Connection conn, Document document,
      DocumentVersion initialVersion) throws SQLException,
      VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.createDocument",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if ((document == null) || (document.getPk() == null) || (document.getForeignKey() == null)) {
      throw new VersioningRuntimeException("VersioningDAO.createDocument",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    int maxOrderNumber = getMaxOrderNumber(conn, document.getForeignKey());
    maxOrderNumber++;
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    formatter.setLenient(false);
    PreparedStatement prepStmt = null;
    DocumentPK result = null;
    int newId = -1;

    try {
      prepStmt = conn.prepareStatement(CREATE_DOCUMENT_QUERY);

      try {
        newId = DBUtil.getNextId(documentTableName, "documentId");
      } catch (Exception e) {
        throw new VersioningRuntimeException("VersioningDAO.createDocument",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_GET_NEXTID_FAILED",
            documentTableName, e);
      }

      prepStmt.setInt(1, newId);
      String name = document.getName();

      if (StringUtil.isDefined(name)) {
        if (name.length() > 100) {
          name = name.substring(0, 99);
        }
      }
      prepStmt.setString(2, StringUtil.truncate(document.getName(),
          nameMaxLength));
      prepStmt.setString(3, document.getDescription());
      prepStmt.setInt(4, document.getStatus());
      prepStmt.setInt(5, -1);

      if (document.getLastCheckOutDate() != null) {
        prepStmt.setString(6, formatter.format(document.getLastCheckOutDate()));
      } else {
        prepStmt.setString(6, "");
      }

      prepStmt.setString(7, document.getAdditionalInfo());
      prepStmt.setInt(8, Integer.parseInt(document.getForeignKey().getId()));
      prepStmt.setString(9, document.getPk().getComponentName());
      prepStmt.setInt(10, document.getTypeWorkList());
      prepStmt.setInt(11, document.getCurrentWorkListOrder());

      if (document.getAlertDate() != null) {
        prepStmt.setString(12, DateUtil.date2SQLDate(document.getAlertDate()));
      } else {
        prepStmt.setString(12, null);
      }
      if (document.getExpiryDate() != null) {
        prepStmt.setString(13, DateUtil.date2SQLDate(document.getExpiryDate()));
      } else {
        prepStmt.setString(13, null);
      }

      prepStmt.setInt(14, maxOrderNumber);
      int rownum = prepStmt.executeUpdate();
      if (rownum < 1) {
        throw new VersioningRuntimeException("VersioningDAO.createDocument",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_RECORD_INSERTION_FAILED");
      }
      List<Worker> workers = document.getWorkList();
      if (workers != null && !workers.isEmpty()) {
        for (int i = 0; i < workers.size(); i++) {
          Worker worker = workers.get(i);
          worker.setDocumentId(newId);
          worker.setInstanceId(document.getPk().getComponentName());
        }
        WorkListDAO.addWorkers(conn, workers);
      }

      result = document.getPk();
      result.setId(String.valueOf(newId));
      initialVersion.setDocumentPK((DocumentPK) result.clone());

      addDocumentVersion(conn, initialVersion);

    } finally {
      DBUtil.close(prepStmt);
    }

    return result;
  }
  public final static String UPDATE_DOCUMENT_QUERY =
      "UPDATE "
      + documentTableName
      + " SET documentName = ? , documentDescription = ? , documentStatus = ? , documentOwnerId = ? , "
      + " documentCheckoutDate = ?, documentInfo = ? , foreignId = ? , instanceId = ? , typeWorkList = ? ,"
      + " currentWorkListOrder = ?, alertDate = ?, expiryDate = ?  WHERE documentId = ? ";

  /**
   * @param conn
   * @param document
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void updateDocument(Connection conn, Document document)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.updateDocument",
          SilverTrace.TRACE_LEVEL_DEBUG, "VersioningDAO.EX_NO_CONNECTION");
    }
    if ((document == null) || (document.getPk() == null) || (document.getForeignKey() == null)) {
      throw new VersioningRuntimeException("VersioningDAO.updateDocument",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    formatter.setLenient(false);
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(UPDATE_DOCUMENT_QUERY);
      prepStmt.setString(1, StringUtil.truncate(document.getName(), nameMaxLength));
      prepStmt.setString(2, document.getDescription());
      prepStmt.setInt(3, document.getStatus());
      prepStmt.setInt(4, document.getOwnerId());

      if (document.getLastCheckOutDate() != null) {
        prepStmt.setString(5, formatter.format(document.getLastCheckOutDate()));
      } else {
        prepStmt.setString(5, "");
      }

      prepStmt.setString(6, document.getAdditionalInfo());
      prepStmt.setInt(7, Integer.parseInt(document.getForeignKey().getId()));
      prepStmt.setString(8, document.getPk().getComponentName());
      prepStmt.setInt(9, document.getTypeWorkList());
      prepStmt.setInt(10, document.getCurrentWorkListOrder());

      if (document.getAlertDate() != null) {
        prepStmt.setString(11, DateUtil.date2SQLDate(document.getAlertDate()));
      } else {
        prepStmt.setString(11, null);
      }
      if (document.getExpiryDate() != null) {
        prepStmt.setString(12, DateUtil.date2SQLDate(document.getExpiryDate()));
      } else {
        prepStmt.setString(12, null);
      }

      prepStmt.setInt(13, Integer.parseInt(document.getPk().getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }
  public final static String UPDATE_DOCUMENT_FOREIGNKEY_QUERY = "UPDATE "
      + documentTableName
      + " SET foreignId = ?, instanceId = ? WHERE documentId = ? ";

  /**
   * @param conn
   * @param documentPK
   * @param foreignKey
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void updateDocumentForeignKey(Connection conn,
      DocumentPK documentPK, ForeignPK foreignKey) throws SQLException,
      VersioningRuntimeException {
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(UPDATE_DOCUMENT_FOREIGNKEY_QUERY);
      prepStmt.setInt(1, Integer.parseInt(foreignKey.getId()));
      prepStmt.setString(2, foreignKey.getInstanceId());
      prepStmt.setInt(3, Integer.parseInt(documentPK.getId()));

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * @param conn
   * @param document
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void updateWorkList(Connection conn, Document document)
      throws SQLException, VersioningRuntimeException {
    List<Worker> workers = document.getWorkList();
    if (workers != null && !workers.isEmpty()) {
      for (int i = 0; i < workers.size(); i++) {
        Worker worker = workers.get(i);
        worker.setDocumentId(Integer.parseInt(document.getPk().getId()));
      }
      WorkListDAO.removeAllWorkers(conn, document.getPk());
      WorkListDAO.addWorkers(conn, workers);
    }
  }

  public static void deleteWorkList(Connection conn, Document document)
      throws SQLException, VersioningRuntimeException {
    WorkListDAO.removeAllWorkers(conn, document.getPk());
  }

  public static void deleteWorkList(Connection conn, Document document,
      boolean keepSaved) throws SQLException, VersioningRuntimeException {
    WorkListDAO.removeAllWorkers(conn, document.getPk(), keepSaved);
  }
  public static final String CHECKOUT_DOCUMENT_QUERY = "UPDATE "
      + documentTableName
      + " SET documentOwnerId = ? ,"
      + " documentStatus = 1 , documentCheckOutDate = ? , alertDate = ? , expiryDate = ?  "
      + " WHERE documentId = ?";

  public static void checkDocumentOut(Connection conn, DocumentPK documentPK,
      int ownerId, Date checkOutDate) throws SQLException,
      VersioningRuntimeException {
    checkDocumentOut(conn, documentPK, ownerId, checkOutDate, null, null);
  }

  /**
   * @param conn
   * @param documentPK
   * @param ownerId
   * @param checkOutDate
   * @param alertDate
   * @param expiryDate
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void checkDocumentOut(Connection conn, DocumentPK documentPK,
      int ownerId, Date checkOutDate, Date alertDate, Date expiryDate)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.checkDocumentOut",
          SilverTrace.TRACE_LEVEL_DEBUG, "VersioningDAO.EX_NO_CONNECTION");
    }
    if (documentPK == null || ownerId == -1 || checkOutDate == null) {
      throw new VersioningRuntimeException("VersioningDAO.checkDocumentOut",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    SilverTrace.debug("versioning", "DAO.checkDocumentOut",
        "root.MSG_GEN_PARAM_VALUE", "instanceId = " + documentPK.getId());

    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    formatter.setLenient(false);
    int rowCount = 0;
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(CHECKOUT_DOCUMENT_QUERY);
      prepStmt.setInt(1, ownerId);
      prepStmt.setString(2, formatter.format(checkOutDate));

      if (alertDate != null) {
        prepStmt.setString(3, DateUtil.date2SQLDate(alertDate));
      } else {
        prepStmt.setString(3, null);
      }
      if (expiryDate != null) {
        prepStmt.setString(4, DateUtil.date2SQLDate(expiryDate));
      } else {
        prepStmt.setString(4, null);
      }
      SilverTrace.debug("versioning", "DAO.checkDocumentOut",
          "root.MSG_GEN_PARAM_VALUE", "doc.getExpiryDate() = " + expiryDate);

      prepStmt.setInt(5, Integer.parseInt(documentPK.getId()));
      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new VersioningRuntimeException("VersioningDAO.checkDocumentOut",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = "
          + documentPK.getId());
    }
  }
  public static final String CHECKIN_DOCUMENT_QUERY = "UPDATE "
      + documentTableName + " SET documentownerid = -1, "
      + " documentStatus = 0 , alertDate = null , expiryDate = null "
      + " WHERE documentId = ?";

  /**
   * @param conn
   * @param documentPK
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void checkDocumentIn(Connection conn, DocumentPK documentPK)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.checkDocumentIn",
          SilverTrace.TRACE_LEVEL_DEBUG, "VersioningDAO.EX_NO_CONNECTION");
    }
    if (documentPK == null) {
      throw new VersioningRuntimeException("VersioningDAO.checkDocumentIn",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    int rowCount = 0;
    PreparedStatement prepStmt = null;
    try {
      prepStmt = conn.prepareStatement(CHECKIN_DOCUMENT_QUERY);
      prepStmt.setInt(1, Integer.parseInt(documentPK.getId()));
      rowCount = prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new VersioningRuntimeException("VersioningDAO.checkDocumentIn",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "NodeId = "
          + documentPK.getId());
    }
  }
  public static final String ADD_NEW_VERSION_QUERY =
      "INSERT INTO "
      + versionTableName
      + " (versionId, "
      + " documentId, versionMajorNumber, versionMinorNumber, versionAuthorId, "
      + " versionCreationDate, versionComments, versionType, versionStatus, versionPhysicalname, "
      + " versionLogicalName, versionMimeType, versionSize, instanceId, xmlForm) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";

  /**
   * @param conn
   * @param newVersion
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static DocumentVersionPK addDocumentVersion(Connection conn,
      DocumentVersion newVersion) throws SQLException,
      VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.addDocumentVersion",
          SilverTrace.TRACE_LEVEL_DEBUG, "VersioningDAO.EX_NO_CONNECTION");
    }
    if (newVersion == null || newVersion.getDocumentPK() == null) {
      throw new VersioningRuntimeException("VersioningDAO.addDocumentVersion",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    formatter.setLenient(false);
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(ADD_NEW_VERSION_QUERY);
      int newId = -1;

      try {
        newId = DBUtil.getNextId(versionTableName, "versionId");
      } catch (Exception e) {
        throw new VersioningRuntimeException("VersioningDAO.createDocument",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_GET_NEXTID_FAILED",
            versionTableName, e);
      }

      prepStmt.setInt(1, newId);
      prepStmt.setInt(2, Integer.parseInt(newVersion.getDocumentPK().getId()));
      prepStmt.setInt(3, newVersion.getMajorNumber());
      prepStmt.setInt(4, newVersion.getMinorNumber());
      prepStmt.setInt(5, newVersion.getAuthorId());

      if (newVersion.getCreationDate() != null) {
        prepStmt.setString(6, formatter.format(newVersion.getCreationDate()));
      } else {
        prepStmt.setString(6, "");
      }
      prepStmt.setString(7, newVersion.getComments());
      prepStmt.setInt(8, newVersion.getType());
      prepStmt.setInt(9, newVersion.getStatus());
      prepStmt.setString(10, newVersion.getPhysicalName());
      prepStmt.setString(11, newVersion.getLogicalName());
      prepStmt.setString(12, newVersion.getMimeType());
      prepStmt.setLong(13, newVersion.getSize());
      prepStmt.setString(14, newVersion.getInstanceId());
      prepStmt.setString(15, newVersion.getXmlForm());

      int rownum = prepStmt.executeUpdate();

      if (rownum < 1) {
        throw new VersioningRuntimeException(
            "VersioningDAO.addDocumentVersion", SilverTrace.TRACE_LEVEL_DEBUG,
            "root.EX_RECORD_INSERTION_FAILED", newVersion.toString());
      }
      DocumentPK docPK = newVersion.getDocumentPK();
      newVersion.setPk(new DocumentVersionPK(newId, docPK.getSpace(), docPK.getComponentName()));

    } finally {
      DBUtil.close(prepStmt);
    }

    return newVersion.getPk();
  }
  public static final String UPDATE_VERSION_QUERY =
      "UPDATE "
      + versionTableName
      + " SET "
      + " versionMajorNumber = ? , versionMinorNumber = ? , versionAuthorId = ? , "
      + " versionCreationDate = ? , versionComments = ? , versionType = ? , versionStatus = ?, versionPhysicalname = ?, "
      + " versionLogicalName = ? , versionMimeType = ? , versionSize = ? , instanceId = ?"
      + " WHERE versionId = ? ";

  /**
   * @param conn
   * @param version
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void updateDocumentVersion(Connection conn,
      DocumentVersion version) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.updateDocumentVersion", SilverTrace.TRACE_LEVEL_DEBUG,
          "root.EX_NO_CONNECTION");
    }
    if (version == null || version.getPk() == null || version.getDocumentPK() == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.updateDocumentVersion", SilverTrace.TRACE_LEVEL_DEBUG,
          "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    formatter.setLenient(false);
    PreparedStatement prepStmt = null;

    try {
      prepStmt = conn.prepareStatement(UPDATE_VERSION_QUERY);
      prepStmt.setInt(1, version.getMajorNumber());
      prepStmt.setInt(2, version.getMinorNumber());
      prepStmt.setInt(3, version.getAuthorId());
      if (version.getCreationDate() != null) {
        prepStmt.setString(4, formatter.format(version.getCreationDate()));
      } else {
        prepStmt.setString(4, "");
      }

      prepStmt.setString(5, version.getComments());
      prepStmt.setInt(6, version.getType());
      prepStmt.setInt(7, version.getStatus());
      prepStmt.setString(8, version.getPhysicalName());
      prepStmt.setString(9, version.getLogicalName());
      prepStmt.setString(10, version.getMimeType());
      prepStmt.setLong(11, version.getSize());
      prepStmt.setString(12, version.getInstanceId());
      prepStmt.setInt(13, Integer.parseInt(version.getPk().getId()));

      int rowNum = prepStmt.executeUpdate();

      if (rowNum < 1) {
        throw new VersioningRuntimeException(
            "VersioningDAO.updateDocumentVersion",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_RECORD_NOT_FOUND", version);
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }
  public static final String GET_VERSIONS_QUERY =
      "SELECT v.versionId, "
      + " v.documentId, v.versionMajorNumber , v.versionMinorNumber, v.versionAuthorId, "
      + " v.versionCreationDate, v.versionComments, v.versionType, v.versionStatus, v.versionPhysicalname, "
      + " v.versionLogicalName, v.versionMimeType, v.versionSize, v.instanceId, v.xmlForm "
      + " FROM " + versionTableName
      + " v WHERE v.documentId = ? ORDER BY v.versionId DESC";

  /**
   * @param conn
   * @param documentPK
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static List<DocumentVersion> getDocumentVersions(Connection conn,
      DocumentPK documentPK) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocumentVersions",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (documentPK == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocumentVersions",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<DocumentVersion> result = new ArrayList<DocumentVersion>();

    try {
      prepStmt = conn.prepareStatement(GET_VERSIONS_QUERY);
      try {
        prepStmt.setInt(1, Integer.parseInt(documentPK.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getDocumentVersions", SilverTrace.TRACE_LEVEL_DEBUG,
            "root.EX_WRONG_PK", documentPK.toString(), e);
      }

      rs = prepStmt.executeQuery();

      while (rs.next()) {
        DocumentVersion version = getDocVersionFormRS(rs);
        result.add(version);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }
  public static final String GET_LAST_VERSION_QUERY =
      "SELECT v.versionId, "
      + " v.documentId, v.versionMajorNumber , v.versionMinorNumber, v.versionAuthorId, "
      + " v.versionCreationDate, v.versionComments, v.versionType, v.versionStatus, v.versionPhysicalname, "
      + " v.versionLogicalName, v.versionMimeType, v.versionSize, v.instanceId, v.xmlForm "
      + " FROM " + versionTableName + " v WHERE  " + " (v.documentId = ?) AND "
      + " (versionMajorNumber = (select max(versionMajorNumber) FROM "
      + versionTableName + " a WHERE "
      + " a.documentId = ? AND a.versionType = 0 )  )";

  /**
   * @param conn
   * @param documentPK
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static DocumentVersion getLastPublicDocumentVersion(Connection conn,
      DocumentPK documentPK) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.getLastDocumentVersion",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (documentPK == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.getLastPublicDocumentVersion",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    DocumentVersion result = null;

    try {
      prepStmt = conn.prepareStatement(GET_LAST_VERSION_QUERY);
      try {
        int docID = Integer.parseInt(documentPK.getId());
        prepStmt.setInt(1, docID);
        prepStmt.setInt(2, docID);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getLastDocumentVersion",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", documentPK.toString(), e);
      }

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        result = getDocVersionFormRS(rs);
      }

    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /**
   * @param conn
   * @param pk
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static DocumentVersion getDocumentVersion(Connection conn,
      DocumentVersionPK pk) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocumentVersion",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_NO_CONNECTION");
    }
    if (pk == null) {
      throw new VersioningRuntimeException("VersioningDAO.getDocumentVersion",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    DocumentVersion result = null;

    try {
      prepStmt = conn.prepareStatement(GET_DOCUMENT_VERSION_BYID_QUERY);
      try {
        prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getDocumentVersion", SilverTrace.TRACE_LEVEL_ERROR,
            "root.EX_WRONG_PK", pk.toString(), e);
      }

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        result = getDocVersionFormRS(rs);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return result;
  }

  /**
   * Get document version from RseultSet
   * @param rs
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  private static DocumentVersion getDocVersionFormRS(ResultSet rs)
      throws SQLException, VersioningRuntimeException {
    SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
    DocumentVersion version = new DocumentVersion();

    version.setPk(new DocumentVersionPK(rs.getInt(1)));
    version.setDocumentPK(new DocumentPK(rs.getInt(2)));
    version.setMajorNumber(rs.getInt(3));
    version.setMinorNumber(rs.getInt(4));
    version.setAuthorId(rs.getInt(5));
    try {
      String creationDate = rs.getString(6);
      if (creationDate != null && !creationDate.equals("")) {
        version.setCreationDate(formatter.parse(creationDate));
      } else {
        version.setCreationDate(null);
      }
    } catch (ParseException e) {
      throw new VersioningRuntimeException("VersioningDAO.getDocVersionFormRS",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_CANT_PARSE_DATE", e);
    }
    String comments = "";
    if (rs.getString(7) != null) {
      comments = rs.getString(7);
    }
    version.setComments(comments);
    version.setType(rs.getInt(8));
    version.setStatus(rs.getInt(9));
    version.setPhysicalName(rs.getString(10));
    version.setLogicalName(rs.getString(11));
    version.setMimeType(rs.getString(12));
    version.setSize(rs.getInt(13));
    version.setInstanceId(rs.getString(14));
    version.setXmlForm(rs.getString(15));

    return version;
  }
  public final static String DELETE_DOCUMENT = "delete from "
      + documentTableName + " where documentId = ? ";
  public final static String DELETE_DOCUMENT_HEADER = "delete from "
      + versionTableName + " where documentId = ? ";

  /**
   * @param conn
   * @param documentPK
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void deleteDocument(Connection conn, DocumentPK documentPK)
      throws SQLException, VersioningRuntimeException {
    SilverTrace.info("versioning", "VersioningDAO.deleteDocument()",
        "root.MSG_GEN_ENTER_METHOD", "documentPK = " + documentPK.toString());
    if (conn == null) {
      throw new VersioningRuntimeException("WorkListDAO.deleteDocument",
          SilverTrace.TRACE_LEVEL_ERROR, "root.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;

    try {

      WorkListDAO.removeAllWorkers(conn, documentPK, true);

      prepStmt = conn.prepareStatement(DELETE_DOCUMENT);
      try {
        prepStmt.setInt(1, Integer.parseInt(documentPK.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.getWorkers",
            SilverTrace.TRACE_LEVEL_ERROR, "root.EX_WRONG_PK", documentPK.toString(), e);
      }

      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
    try {

      prepStmt = conn.prepareStatement(DELETE_DOCUMENT_HEADER);
      try {
        prepStmt.setInt(1, Integer.parseInt(documentPK.getId()));
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException("WorkListDAO.getWorkers",
            SilverTrace.TRACE_LEVEL_ERROR, "root.EX_WRONG_PK", documentPK.toString(), e);
      }

      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
  }
  public static final String GET_ALL_PUBLIC_VERSIONS_QUERY = "SELECT versionId, "
      + " documentId, versionMajorNumber , versionMinorNumber, versionAuthorId, "
      + " versionCreationDate, versionComments, versionType, versionStatus, versionPhysicalname, "
      + " versionLogicalName, versionMimeType, versionSize, instanceId, xmlForm "
      + " FROM "
      + versionTableName
      + " WHERE documentId = ? "
      + " AND versionMinorNumber = 0 " + " ORDER BY versionMajorNumber DESC";

  /**
   * @param conn
   * @param documentPK
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static List<DocumentVersion> getAllPublicDocumentVersions(Connection conn,
      DocumentPK documentPK) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.getAllPublicDocumentVersions",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }
    if (documentPK == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.getAllPublicDocumentVersions",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NULL_VALUE_OBJECT_OR_PK");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    DocumentVersion result = null;
    List<DocumentVersion> results = new ArrayList<DocumentVersion>();
    try {
      prepStmt = conn.prepareStatement(GET_ALL_PUBLIC_VERSIONS_QUERY);
      try {
        int docID = Integer.parseInt(documentPK.getId());
        prepStmt.setInt(1, docID);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getAllPublicDocumentVersions",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", documentPK.toString(), e);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        result = getDocVersionFormRS(rs);
        results.add(result);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return results;
  }

  /**
   * @param con
   * @param instanceId
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void deleteDocumentsByInstanceId(Connection con,
      String instanceId) throws SQLException, VersioningRuntimeException {
    SilverTrace.info("versioning",
        "VersioningDAO.deleteDocumentsByInstanceId()",
        "root.MSG_GEN_ENTER_METHOD", "instanceId = " + instanceId);
    List<Document> documentsOfInstance = getDocumentsByInstanceId(con, instanceId);
    Document document = null;
    for (int d = 0; d < documentsOfInstance.size(); d++) {
      document = documentsOfInstance.get(d);
      deleteDocument(con, document.getPk());
    }
  }
  public static final String GET_ALL_FILES_RESERVED_QUERY = "SELECT d.documentId, "
      + " d.documentName, d.documentDescription, d.documentStatus, d.documentOwnerId, "
      + " d.documentCheckoutDate, d.documentInfo, d.foreignId, d.instanceId, d.typeWorkList, "
      + " d.currentWorkListOrder, alertDate, expiryDate, documentordernum "
      + " FROM "
      + documentTableName
      + " d WHERE d.documentOwnerId = ? and d.documentStatus = 1 ";

  /**
   * @param conn
   * @param ownerId
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static List<Document> getAllFilesReserved(Connection conn, int ownerId)
      throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getAllFilesReserved",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Document> results = new ArrayList<Document>();
    try {
      prepStmt = conn.prepareStatement(GET_ALL_FILES_RESERVED_QUERY);
      try {
        prepStmt.setInt(1, ownerId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getAllFilesReserved", SilverTrace.TRACE_LEVEL_DEBUG,
            "root.EX_WRONG_PK", Integer.toString(ownerId), e);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Document doc = getDocFormRS(rs, conn, "useless");
        results.add(doc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return results;
  }

  /**
   * @param conn
   * @param date
   * @param alert
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static List<Document> getAllFilesReservedByDate(Connection conn, Date date,
      boolean alert) throws SQLException, VersioningRuntimeException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getAllFilesReserved",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Document> results = new ArrayList<Document>();
    try {
      if (alert) {
        prepStmt = conn.prepareStatement(GET_ALL_ALERT_FILES_RESERVED_BY_DATE_QUERY);
      } else {
        prepStmt = conn.prepareStatement(GET_ALL_EXPIRY_FILES_RESERVED_BY_DATE_QUERY);
      }
      try {
        if (date != null) {
          prepStmt.setString(1, DateUtil.date2SQLDate(date));
        } else {
          prepStmt.setString(1, null);
        }
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getAllFilesReservedByDate",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", date, e);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Document doc = getDocFormRS(rs, conn, "useless");
        results.add(doc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return results;
  }

  /**
   * @param conn
   * @param date
   * @return
   * @throws SQLException
   */
  public static List<Document> getAllDocumentsToLib(Connection conn, Date date)
      throws SQLException {
    if (conn == null) {
      throw new VersioningRuntimeException(
          "VersioningDAO.getAllDocumentsToLib", SilverTrace.TRACE_LEVEL_DEBUG,
          "root.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<Document> results = new ArrayList<Document>();
    try {
      prepStmt = conn.prepareStatement(GET_ALL_FILES_RESERVED_BY_DATE_QUERY);
      try {
        if (date != null) {
          prepStmt.setString(1, DateUtil.date2SQLDate(date));
        } else {
          prepStmt.setString(1, null);
        }
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getAllDocumentsToLib",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", date, e);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        Document doc = getDocFormRS(rs, conn, "useless");
        results.add(doc);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return results;
  }

  /**
   * @param conn
   * @param role
   * @param componentId
   * @param groupsIds
   * @param usersIds
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void insertReadersAccessList(Connection conn,
      String componentId, List<String> groupsIds, List<String> usersIds)
      throws SQLException, VersioningRuntimeException {
    SilverTrace.debug("versioning", "VersioningDAO.insertReadersAccessList",
        "root.MSG_GEN_ENTER_METHOD");

    int rowCount = 0;
    PreparedStatement prepStmt = null;
    int newId = -1;

    try {
      newId = DBUtil.getNextId(accessListTableName, "id");
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningDAO.insertReadersAccessList",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_GET_NEXTID_FAILED",
          accessListTableName, e);
    }

    try {
      prepStmt = conn.prepareStatement(INSERT_ACCESS_LIST);
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, componentId);
      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new VersioningRuntimeException("VersioningDAO.insertAccessList",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "componentId = "
          + componentId);
    } else {
      insertAccessListContent(conn, newId, groupsIds, usersIds);
    }
    SilverTrace.debug("versioning", "VersioningDAO.insertReadersAccessList",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * @param conn
   * @param componentId
   * @param groupsIds
   * @param usersIds
   * @return
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static String insertReadersAccessGroupsList(Connection conn,
      String componentId, List<String> groupsIds, List<String> usersIds)
      throws SQLException, VersioningRuntimeException {
    SilverTrace.debug("versioning",
        "VersioningDAO.insertReadersAccessGroupsList",
        "root.MSG_GEN_ENTER_METHOD");

    int rowCount = 0;
    PreparedStatement prepStmt = null;
    int newId = -1;

    try {
      newId = DBUtil.getNextId(accessListTableName, "id");
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningDAO.insertReadersAccessGroupsList",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_GET_NEXTID_FAILED",
          accessListTableName, e);
    }

    try {
      prepStmt = conn.prepareStatement(INSERT_ACCESS_LIST);
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, componentId);
      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new VersioningRuntimeException(
          "VersioningDAO.insertAccessGroupsList",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "componentId = "
          + componentId);
    } else {
      insertAccessGroupsListContent(conn, newId, groupsIds, usersIds);
    }
    SilverTrace.debug("versioning",
        "VersioningDAO.insertReadersAccessGroupsList",
        "root.MSG_GEN_EXIT_METHOD");
    return Integer.toString(newId);
  }

  /**
   * @param conn
   * @param role
   * @param componentId
   * @param groupsIds
   * @param usersIds
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  private static void insertAccessListContent(Connection conn, int accessId,
      List<String> groupsIds, List<String> usersIds) throws SQLException,
      VersioningRuntimeException {
    SilverTrace.debug("versioning", "VersioningDAO.insertAccessListContent",
        "root.MSG_GEN_PARAM_VALUE", "accessId = " + accessId);
    for (int i = 0; i < groupsIds.size(); i++) {
      int groupId = Integer.parseInt(groupsIds.get(i));
      insertAccessListContentRow(conn, "G", groupId, accessId);
    }

    for (int i = 0; i < usersIds.size(); i++) {
      int userId = Integer.parseInt(usersIds.get(i));
      insertAccessListContentRow(conn, "U", userId, accessId);
    }
  }

  /**
   * @param conn
   * @param accessId
   * @param groupsIds
   * @param usersIds
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  private static void insertAccessGroupsListContent(Connection conn,
      int accessId, List<String> groupsIds, List<String> usersIds)
      throws SQLException, VersioningRuntimeException {
    SilverTrace.debug("versioning",
        "VersioningDAO.insertAccessGroupsListContent",
        "root.MSG_GEN_PARAM_VALUE", "accessId = " + accessId);
    for (int i = 0; i < groupsIds.size(); i++) {
      int groupId = Integer.parseInt(groupsIds.get(i));
      insertAccessListContentRow(conn, "G", groupId, accessId);
    }
    for (int i = 0; i < usersIds.size(); i++) {
      int userId = Integer.parseInt(usersIds.get(i));
      insertAccessListContentRow(conn, "U", userId, accessId);
    }
  }

  /**
   * @param conn
   * @param setType
   * @param setTypeId
   * @param accessId
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void insertAccessListContentRow(Connection conn,
      String setType, int setTypeId, int accessId) throws SQLException,
      VersioningRuntimeException {
    SilverTrace.debug("versioning", "VersioningDAO.insertAccessListContentRow",
        "root.MSG_GEN_PARAM_VALUE", "accessId = " + accessId);

    int rowCount = 0;
    PreparedStatement prepStmt = null;
    int newId = -1;

    try {
      newId = DBUtil.getNextId(accessListContentTableName, "id");
    } catch (Exception e) {
      throw new VersioningRuntimeException(
          "VersioningDAO.insertAccessListContentRow",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_GET_NEXTID_FAILED",
          accessListContentTableName, e);
    }

    try {
      prepStmt = conn.prepareStatement(INSERT_ACCESS_LIST_CONTENT);
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, setType);
      prepStmt.setInt(3, setTypeId);
      prepStmt.setInt(4, accessId);
      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }

    if (rowCount == 0) {
      throw new VersioningRuntimeException(
          "VersioningDAO.insertAccessListContentRow",
          SilverpeasRuntimeException.ERROR,
          "root.EX_CANT_STORE_ENTITY_ATTRIBUTES", "id = " + accessId);
    }
  }

  /**
   * @param conn
   * @param role
   * @param componentId
   * @throws SQLException
   * @throws VersioningRuntimeException
   */
  public static void removeReadersAccessList(Connection conn, String componentId)
      throws SQLException, VersioningRuntimeException {
    SilverTrace.debug("versioning", "VersioningDAO.removeAccessList",
        "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = conn.prepareStatement(DELETE_ACCESS_LIST_CONTENT);
      try {
        prepStmt.setString(1, componentId);
        prepStmt.executeUpdate();

        prepStmt = conn.prepareStatement(DELETE_ACCESS_LIST);
        prepStmt.setString(1, componentId);
        prepStmt.executeUpdate();
      } catch (NumberFormatException e) {
        conn.rollback();
        throw new VersioningRuntimeException("VersioningDAO.removeAccessList",
            SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_WRONG_PK", componentId, e);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  /**
   * @param conn
   * @param profile
   * @param componentId
   * @return
   * @throws SQLException
   */
  public static List<String> getReadersAccessListGroups(Connection conn,
      String componentId) throws SQLException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getAccessListGroups",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<String> results = new ArrayList<String>();
    try {
      prepStmt = conn.prepareStatement(SELECT_ACCESS_LIST_GROUPS);
      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getAccessListGroups", SilverTrace.TRACE_LEVEL_DEBUG,
            "root.EX_WRONG_PK", componentId, e);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        results.add(new Integer(rs.getInt(1)).toString());
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return results;
  }

  /**
   * @param conn
   * @param profile
   * @param componentId
   * @return
   * @throws SQLException
   */
  public static List<String> getReadersAccessListUsers(Connection conn,
      String componentId) throws SQLException {
    if (conn == null) {
      throw new VersioningRuntimeException("VersioningDAO.getAccessListUsers",
          SilverTrace.TRACE_LEVEL_DEBUG, "root.EX_NO_CONNECTION");
    }

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    List<String> results = new ArrayList<String>();
    try {
      prepStmt = conn.prepareStatement(SELECT_ACCESS_LIST_USERS);

      try {
        prepStmt.setString(1, componentId);
      } catch (NumberFormatException e) {
        throw new VersioningRuntimeException(
            "VersioningDAO.getAccessListUsers", SilverTrace.TRACE_LEVEL_DEBUG,
            "root.EX_WRONG_PK", componentId, e);
      }

      rs = prepStmt.executeQuery();
      while (rs.next()) {
        results.add(Integer.toString(rs.getInt(1)));
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }

    return results;
  }
  /**
   *
   */
  public static final String SELECT_ACCESS_LIST_USERS = "select t2.settypeid from "
      + accessListTableName
      + " t1, "
      + accessListContentTableName
      + " t2"
      + " where t1.componentid = ? and t2.accessId = t1.id and t2.settype = 'U'";
  public static final String SELECT_ACCESS_LIST_USERS_BY_DOCUMENT = "select t2.settypeid from "
      + accessListTableName
      + " t1, "
      + accessListContentTableName
      + " t2"
      + " where t1.componentid = ? and t2.accessId = t1.id and t2.settype = 'U'";
  /**
   *
   */
  public static final String SELECT_ACCESS_LIST_GROUPS = "select t2.settypeid from "
      + accessListTableName
      + " t1, "
      + accessListContentTableName
      + " t2"
      + " where t1.componentid = ? and t2.accessId = t1.id and t2.settype = 'G'";
  public static final String SELECT_ACCESS_LIST_GROUPS_BY_DOCUMENT = "select t2.settypeid from "
      + accessListTableName
      + " t1, "
      + accessListContentTableName
      + " t2"
      + " where t1.componentid = ? and t2.accessId = t1.id and t2.settype = 'G'";
  /**
   *
   */
  public static final String INSERT_ACCESS_LIST = "insert into "
      + accessListTableName + " (id, componentid) values (?,?)";
  /**
   *
   */
  public static final String DELETE_ACCESS_LIST = "delete from "
      + accessListTableName + " where componentid = ?";
  /**
   *
   */
  public static final String INSERT_ACCESS_LIST_CONTENT = "insert into "
      + accessListContentTableName
      + " (id, settype, settypeid, accessid) values (?,?,?,?)";
  /**
   *
   */
  public static final String DELETE_ACCESS_LIST_CONTENT = "delete from "
      + accessListContentTableName + " where accessid in (select id from "
      + accessListTableName + " where componentId = ?)";

  public static int getMaxOrderNumber(Connection con, WAPrimaryKey foreignKey) throws SQLException {
    StringBuffer selectQuery = new StringBuffer();
    selectQuery.append("select max(documentOrderNum)");
    selectQuery.append(" from ").append(documentTableName);
    selectQuery.append(" where foreignId = ? ");
    selectQuery.append(" and instanceId = ? ");

    PreparedStatement prepStmt = null;
    ResultSet rs = null;

    try {
      prepStmt = con.prepareStatement(selectQuery.toString());

      prepStmt.setInt(1, Integer.parseInt(foreignKey.getId()));
      prepStmt.setString(2, foreignKey.getComponentName());

      rs = prepStmt.executeQuery();

      if (rs.next()) {
        return rs.getInt(1);
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
    return 0;
  }

  public static void sortDocuments(Connection con, List<DocumentPK> pks) throws SQLException {
    StringBuilder updateQuery = new StringBuilder();
    updateQuery.append("update ").append(documentTableName);
    updateQuery.append(" set documentOrderNum = ? ");
    updateQuery.append(" where documentId = ? ");
    updateQuery.append(" and instanceId = ? ");
    String query = updateQuery.toString();
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      DocumentPK pk;
      for (int i = 0; i < pks.size(); i++) {
        pk = pks.get(i);
        prepStmt.setInt(1, i);
        prepStmt.setInt(2, Integer.parseInt(pk.getId()));
        prepStmt.setString(3, pk.getInstanceId());

        prepStmt.executeUpdate();
      }

    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
