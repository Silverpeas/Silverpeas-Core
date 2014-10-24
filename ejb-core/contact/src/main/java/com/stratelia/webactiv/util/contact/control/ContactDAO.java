/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.contact.control;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.contact.model.Contact;
import com.stratelia.webactiv.util.contact.model.ContactDetail;
import com.stratelia.webactiv.util.contact.model.ContactFatherDetail;
import com.stratelia.webactiv.util.contact.model.ContactPK;
import com.stratelia.webactiv.util.contact.model.ContactRuntimeException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * This is the Contact Data Access Object.
 *
 * @author Nicolas Eysseric
 */
public class ContactDAO {

  /**
   * This class must not be instanciated
   *
   * @since 1.0
   */
  public ContactDAO() {
  }

  /**
   * Add a new father to this contact
   *
   * @param con Connection to database
   * @param pubPK the contact ContactPK
   * @param fatherPK the father NodePK to add
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.contact.model.ContactPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void addFather(Connection con, ContactPK pubPK, NodePK fatherPK)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement("INSERT INTO  sb_contact_contactfather VALUES (?, ?)");
      prepStmt.setInt(1, Integer.parseInt(pubPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Remove a father to this contact
   *
   * @param con Connection to database
   * @param pubPK the contact ContactPK
   * @param fatherPK the father NodePK to delete
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.contact.model.ContactPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeFather(Connection con, ContactPK pubPK,
      NodePK fatherPK) throws SQLException {
    String insertStatement = "delete from " + pubPK.getTableName() + "Father "
        + "where contactId = ? and nodeId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      prepStmt.setInt(2, new Integer(fatherPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete all fathers to this contact
   *
   * @param con Connection to database
   * @param pubPK the contact ContactPK
   * @see com.stratelia.webactiv.util.contact.model.ContactPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeAllFather(Connection con, ContactPK pubPK)
      throws SQLException {
    String insertStatement = "delete from " + pubPK.getTableName() + "Father "
        + "where contactId = ? ";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete links between contact and father when contacts are linked to a father which is a
   * descendant of a node
   *
   * @param con Connection to database
   * @param pubPK the contact ContactPK
   * @param originPK the node which is deleted
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.contact.model.ContactPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static void removeAllIssue(Connection con, NodePK originPK, ContactPK pubPK) throws
      SQLException {
    ResultSet rs = null;
    String path = null;

    String selectStatement = "select nodePath from " + originPK.getTableName()
        + " where nodeId = ? and instanceId = ?";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(originPK.getId()).intValue());
      prepStmt.setString(2, originPK.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        path = rs.getString(1);
      }
    } finally {
      DBUtil.close(prepStmt);
    }

    path = path + "%";

    String removeStatement = "DELETE FROM " + pubPK.getTableName()
        + "Father WHERE nodeId IN " + "(SELECT nodeId FROM "
        + originPK.getTableName() + " WHERE nodePath like '" + path
        + "' and instanceId = ?" + " and nodeId= ?)";

    try {
      prepStmt = con.prepareStatement(removeStatement);
      prepStmt.setString(1, originPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(originPK.getId()));
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  /**
   * Delete links between contact and father when contacts are linked to a father which is a
   * descendant of a node
   *
   * @param con Connection to database
   * @param pubPK the contact ContactPK
   * @param originPK the node which is deleted
   * @see com.stratelia.webactiv.util.node.model.NodePK
   * @see com.stratelia.webactiv.util.contact.model.ContactPK
   * @exception java.sql.SQLException
   * @since 1.0
   */
  public static Collection<NodePK> getAllFatherPK(Connection con, ContactPK pubPK) throws
      SQLException {
    ResultSet rs = null;
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(
          "SELECT nodeId FROM sb_contact_contactfather WHERE contactId = ?");
      prepStmt.setInt(1, new Integer(pubPK.getId()).intValue());
      rs = prepStmt.executeQuery();
      List<NodePK> list = new ArrayList<NodePK>();
      while (rs.next()) {
        NodePK nodePK = new NodePK(String.valueOf(rs.getInt(1)), pubPK);
        list.add(nodePK);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static int getNbPubInFatherPKs(Connection con, Collection<NodePK> fatherPKs)
      throws SQLException {
    ResultSet rs = null;
    int result = 0;
    ContactPK pubPK = null;
    String nodeId = null;

    if (fatherPKs.isEmpty()) {
      // Debug.println("ContactDAO : getNbPubInFatherPKs : collection fatherPKs is empty");
      return 0;
    } else {
      Iterator<NodePK> iterator = fatherPKs.iterator();
      if (iterator.hasNext()) {
        NodePK nodePK = iterator.next();
        pubPK = new ContactPK("unknown", nodePK);
        nodeId = nodePK.getId();
      }

      String selectStatement = "select count(contactId) " + " from "
          + pubPK.getTableName() + "Father" + " where nodeId = " + nodeId;

      while (iterator.hasNext()) {
        NodePK nodePK = iterator.next();
        nodeId = nodePK.getId();
        selectStatement += " or nodeId = " + nodeId;
      }
      selectStatement += " )";

      PreparedStatement prepStmt = null;

      try {
        prepStmt = con.prepareStatement(selectStatement);
        rs = prepStmt.executeQuery();
        if (rs.next()) {
          result = rs.getInt(1);
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }

      return result;
    }
  }

  public static int getNbPubByFatherPath(Connection con, NodePK fatherPK,
      String fatherPath) throws SQLException {
    ResultSet rs = null;
    int result = 0;
    ContactPK pubPK = new ContactPK("unknown", fatherPK);

    if (fatherPath.length() <= 0) {
      // Debug.println("ContactDAO : getNbPubInFatherPKs : collection fatherPKs is empty");
      return 0;
    } else {
      String selectStatement = "select count(F.contactId) " + " from "
          + pubPK.getTableName() + "Father F, " + fatherPK.getTableName()
          + " N " + " where F.nodeId = N.nodeId " + " and N.instanceId = ?"
          + " and N.nodeId = ?" + " and N.nodePath like '" + fatherPath + "%'";

      PreparedStatement prepStmt = null;
      try {
        prepStmt = con.prepareStatement(selectStatement);
        prepStmt.setString(1, fatherPK.getComponentName());
        prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
        rs = prepStmt.executeQuery();
        if (rs.next()) {
          result = rs.getInt(1);
        }
      } finally {
        DBUtil.close(rs, prepStmt);
      }
      return result;
    }
  }

  public static void insertRow(Connection con, Contact detail)
      throws SQLException {
    String insertStatement = "INSERT INTO " + detail.getPK().getTableName()
        + " VALUES ( ? , ? , ? , ? , ? , ? , ? , ?, ? , ?)";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(insertStatement);
      int id = new Integer(detail.getPK().getId()).intValue();
      prepStmt.setInt(1, id);
      prepStmt.setString(2, detail.getFirstName());
      prepStmt.setString(3, detail.getLastName());
      prepStmt.setString(4, detail.getEmail());
      prepStmt.setString(5, detail.getPhone());
      prepStmt.setString(6, detail.getFax());
      prepStmt.setString(7, detail.getUserId());
      prepStmt.setString(8, DateUtil.today2SQLDate());
      prepStmt.setString(9, detail.getCreatorId());
      prepStmt.setString(10, detail.getPK().getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static void deleteRow(Connection con, ContactPK pk)
      throws SQLException {
    removeAllFather(con, pk); // Delete associations between pub and nodes
    String deleteStatement = "delete from " + pk.getTableName()
        + " where contactId = ? and instanceId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.setString(2, pk.getComponentName());
      prepStmt.executeUpdate();

    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static ContactPK selectByPrimaryKey(Connection con,
      ContactPK primaryKey) throws SQLException, ParseException {
    ContactDetail detail = loadRow(con, primaryKey);
    ContactPK primary = new ContactPK(primaryKey.getId(), primaryKey);
    primary.pubDetail = detail;
    return primary;
  }

  private static ContactDetail resultSet2ContactDetail(ResultSet rs,
      ContactPK pubPK) throws SQLException, ParseException {
    int id = rs.getInt(1);
    ContactPK pk = new ContactPK(String.valueOf(id), pubPK);
    String firstName = rs.getString(2);
    String lastName = rs.getString(3);
    String email = rs.getString(4);
    String phone = rs.getString(5);
    String fax = rs.getString(6);
    String userId = rs.getString(7);
    java.util.Date creationDate;
    creationDate = DateUtil.parse(rs.getString(8));
    String creatorId = rs.getString(9);
    return new ContactDetail(pk, firstName, lastName, email, phone, fax,
        userId, creationDate, creatorId);
  }

  public static Collection<ContactDetail> selectByLastName(Connection con, ContactPK pk,
      String query) throws SQLException, ParseException {
    ResultSet rs = null;
    String selectStatement = "select P.* " + "from " + pk.getTableName()
        + " P, " + pk.getTableName() + "father F "
        + "where F.contactId = P.contactId "
        + " and LOWER(P.contactLastName) LIKE LOWER(?)"
        + " and P.instanceId = ? ";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, query);
      prepStmt.setString(2, pk.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactDetail> list = new ArrayList<ContactDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pk);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ContactDetail> selectByLastNameOrFirstName(Connection con,
      ContactPK pk, String query) throws SQLException, ParseException {
    ResultSet rs = null;
    String selectStatement = "select P.* " + "from " + pk.getTableName()
        + " P, " + pk.getTableName() + "father F "
        + "where F.contactId = P.contactId "
        + " and (LOWER(P.contactLastName) LIKE LOWER(?)"
        + " or LOWER(P.contactFirstName) LIKE LOWER(?))"
        + " and P.instanceId = ? ";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, query);
      prepStmt.setString(2, query);
      prepStmt.setString(3, pk.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactDetail> list = new ArrayList<ContactDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pk);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ContactDetail> selectByFatherPK(Connection con, NodePK fatherPK)
      throws SQLException, ParseException {
    ResultSet rs = null;
    ContactPK pubPK = new ContactPK("unknown", fatherPK);
    String selectStatement = "select  P.* " + "from " + pubPK.getTableName()
        + " P, " + pubPK.getTableName() + "father F "
        + "where F.nodeId = ? and " + " F.contactId = P.contactId "
        + " and P.instanceId = ? ";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(fatherPK.getId()).intValue());
      prepStmt.setString(2, fatherPK.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactDetail> list = new ArrayList<ContactDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ContactFatherDetail> selectByFatherPKs(Connection con,
      Collection<NodePK> fatherPKs, ContactPK pubPK, NodePK nodePK) throws SQLException, ParseException {
    ResultSet rs = null;
    String fatherId = "";
    String whereClause = "";
    if (fatherPKs != null) {
      Iterator<NodePK> it = fatherPKs.iterator();
      whereClause += "(";

      while (it.hasNext()) {
        fatherId = it.next().getId();
        whereClause += " F.nodeId = " + fatherId;
        if (it.hasNext()) {
          whereClause += " OR ";
        } else {
          whereClause += " ) ";
        }
      }
    }

    String selectStatement = "select  P.*, N.nodeId, N.nodeName "
        + "from "
        + pubPK.getTableName()
        + " P, "
        + pubPK.getTableName()
        + "father F, "
        + nodePK.getTableName()
        + " N "
        + "where "
        + whereClause
        + " AND F.contactId = P.contactId AND F.nodeId = N.nodeId AND N.instanceId = ?"
        + " AND N.instanceId = P.instanceId ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, nodePK.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactFatherDetail> list = new ArrayList<ContactFatherDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pubPK);
        ContactFatherDetail contactFather = new ContactFatherDetail(pub, rs
            .getString(11), rs.getString(12));
        list.add(contactFather);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ContactDetail> selectByContactPKs(Connection con,
      Collection<ContactPK> contactPKs) throws SQLException, ParseException {
    List<ContactDetail> contacts = new ArrayList<ContactDetail>();
    Iterator iterator = contactPKs.iterator();
    while (iterator.hasNext()) {
      ContactPK pubPK = (ContactPK) iterator.next();
      ContactDetail pub = loadRow(con, pubPK);
      contacts.add(pub);
    }
    return contacts;
  }

  public static Collection<ContactDetail> getOrphanContacts(Connection con, ContactPK pubPK)
      throws SQLException, ParseException {
    ResultSet rs = null;

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement("SELECT * FROM SB_Contact_Contact WHERE contactId NOT IN "
          + "(SELECT contactId FROM SB_Contact_Contactfather) AND instanceId = ? ");
      prepStmt.setString(1, pubPK.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactDetail> list = new ArrayList<ContactDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static void deleteOrphanContactsByCreatorId(Connection con,
      ContactPK pubPK, String creatorId) throws SQLException {
    String deleteStatement = "delete from " + pubPK.getTableName()
        + " where contactCreatorId = ? " + " and instanceId = ? "
        + " and contactId NOT IN (Select contactId from "
        + pubPK.getTableName() + "father) ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(deleteStatement);
      prepStmt.setString(1, creatorId);
      prepStmt.setString(2, pubPK.getComponentName());
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static ContactDetail loadRow(Connection con, ContactPK pk)
      throws SQLException, ParseException {
    // Debug.println("ContactDAO : loadRow()");
    ResultSet rs = null;
    ContactDetail pub = null;
    String selectStatement = "select  * from " + pk.getTableName()
        + " where contactId = ? and instanceId = ?";
    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, new Integer(pk.getId()).intValue());
      prepStmt.setString(2, pk.getComponentName());
      rs = prepStmt.executeQuery();
      if (rs.next()) {
        pub = resultSet2ContactDetail(rs, pk);
        return pub;
      } else {
        throw new ContactRuntimeException("ContactDAO.loadRow()",
            SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND",
            "id = " + pk.getId().toString());
      }
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static void storeRow(Connection con, Contact detail)
      throws SQLException {
    int rowCount = 0;
    String insertStatement = "update " + detail.getPK().getTableName()
        + " set " + "contactFirstName = ? , contactLastName = ? , "
        + "contactEmail = ? , contactPhone = ? , contactFax = ? , userId = ? ,"
        + "contactCreationDate = ? , contactCreatorId = ? "
        + "where contactId = ? and instanceId = ?";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, detail.getFirstName());
      prepStmt.setString(2, detail.getLastName());
      prepStmt.setString(3, detail.getEmail());
      prepStmt.setString(4, detail.getPhone());
      prepStmt.setString(5, detail.getFax());
      prepStmt.setString(6, detail.getUserId());
      prepStmt.setString(7, DateUtil.date2SQLDate(detail.getCreationDate()));
      prepStmt.setString(8, detail.getCreatorId());
      prepStmt.setInt(9, new Integer(detail.getPK().getId()).intValue());
      prepStmt.setString(10, detail.getPK().getComponentName());

      rowCount = prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
    if (rowCount == 0) {
      throw new ContactRuntimeException("ContactDAO.storeRow()",
          SilverpeasRuntimeException.ERROR, "root.EX_RECORD_UPDATE_FAILED",
          "id = " + detail.getPK().getId().toString());
    }
  }

  public static Collection<ContactDetail> getUnavailableContactsByPublisherId(Connection con,
      ContactPK pubPK, String publisherId, String nodeId) throws SQLException,
      ParseException {
    ResultSet rs = null;
    String selectStatement = "SELECT  P.* " + "FROM " + pubPK.getTableName()
        + " P, " + pubPK.getTableName() + "father F " + "WHERE F.nodeId = ? AND "
        + "contactCreatorId = ? AND F.contactId = P.contactId AND P.instanceId = ? ";

    PreparedStatement prepStmt = null;

    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setInt(1, Integer.parseInt(nodeId));
      prepStmt.setString(2, publisherId);
      prepStmt.setString(3, pubPK.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactDetail> list = new ArrayList<ContactDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection<ContactDetail> selectByLastNameAndFirstName(Connection con,
      ContactPK pk, String lastName, String firstName) throws SQLException,
      ParseException {
    ResultSet rs = null;

    String selectStatement = "select P.* " + "from " + pk.getTableName()
        + " P, " + pk.getTableName() + "father F "
        + "where F.contactId = P.contactId "
        + " and LOWER(P.contactLastName) LIKE LOWER(?)"
        + " and LOWER(P.contactFirstName) LIKE LOWER(?)"
        + " and P.instanceId = ? ";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, lastName);
      prepStmt.setString(2, firstName);
      prepStmt.setString(3, pk.getComponentName());
      rs = prepStmt.executeQuery();
      List<ContactDetail> list = new ArrayList<ContactDetail>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pk);
        list.add(pub);
      }
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }
}