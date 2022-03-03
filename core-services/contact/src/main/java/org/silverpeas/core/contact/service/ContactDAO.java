/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contact.service;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.contact.model.CompleteContact;
import org.silverpeas.core.contact.model.Contact;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.contact.model.ContactFatherDetail;
import org.silverpeas.core.contact.model.ContactPK;
import org.silverpeas.core.contact.model.ContactRuntimeException;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * This is the Contact Data Access Object.
 * @author Nicolas Eysseric
 */
@Repository
public class ContactDAO {

  private static final String DELETE_FROM = "delete from ";
  private static final String UNKNOWN = "unknown";
  private static final String SELECT_P = "select P.* ";
  private static final String FROM = "from ";
  private static final String FATHER_F = "father F ";
  private static final String WHERE_F_CONTACT_ID_EQUAL_TO_P_CONTACT_ID =
      "where F.contactId = P.contactId ";
  private static final String AND_P_INSTANCE_ID_EQUAL_TO_GIVEN_VALUE = " and P.instanceId = ? ";

  /**
   * Add a new father to this contact
   * @param con Connection to database
   * @param contactPK the contact ContactPK
   * @param fatherPK the father NodePK to add
   * @throws java.sql.SQLException if an error occurs
   * @see NodePK
   * @see ContactPK
   * @since 1.0
   */
  public void addFather(Connection con, ContactPK contactPK, NodePK fatherPK)
      throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(
        "INSERT INTO  sb_contact_contactfather VALUES (?, ?)")) {
      prepStmt.setInt(1, Integer.parseInt(contactPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Remove a father to this contact
   * @param con Connection to database
   * @param contactPK the contact ContactPK
   * @param fatherPK the father NodePK to delete
   * @throws java.sql.SQLException if an error occurs
   * @see NodePK
   * @see ContactPK
   * @since 1.0
   */
  void removeFather(Connection con, ContactPK contactPK, NodePK fatherPK)
      throws SQLException {
    final String insertStatement = DELETE_FROM + contactPK.getTableName() + "Father " +
        "where contactId = ? and nodeId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setInt(1, Integer.parseInt(contactPK.getId()));
      prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Delete all fathers to this contact
   * @param con Connection to database
   * @param contactPK the contact ContactPK
   * @throws java.sql.SQLException if an error occurs
   * @see ContactPK
   * @since 1.0
   */
  void removeAllFather(Connection con, ContactPK contactPK) throws SQLException {
    final String insertStatement =
        DELETE_FROM + contactPK.getTableName() + "Father " + "where contactId = ? ";
    try (final PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setInt(1, Integer.parseInt(contactPK.getId()));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Delete links between contact and father when contacts are linked to a father which is a
   * descendant of a node
   * @param con Connection to database
   * @param contactPK the contact ContactPK
   * @param originPK the node which is deleted
   * @throws java.sql.SQLException if an error occurs
   * @see NodePK
   * @see ContactPK
   * @since 1.0
   */
  void removeAllIssue(Connection con, NodePK originPK, ContactPK contactPK)
      throws SQLException {
    String path = null;

    final String selectStatement =
        "select nodePath from " + originPK.getTableName() + " where nodeId = ? and instanceId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(originPK.getId()));
      prepStmt.setString(2, originPK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          path = rs.getString(1);
        }
      }
    }
    path = path + "%";

    final String removeStatement =
        "DELETE FROM " + contactPK.getTableName() + "Father WHERE nodeId IN " +
        "(SELECT nodeId FROM " +
        originPK.getTableName() + " WHERE nodePath like '" + path + "' and instanceId = ?" +
        " and nodeId= ?)";

    try (final PreparedStatement prepStmt = con.prepareStatement(removeStatement)) {
      prepStmt.setString(1, originPK.getComponentName());
      prepStmt.setInt(2, Integer.parseInt(originPK.getId()));
      prepStmt.executeUpdate();
    }
  }

  /**
   * Delete links between contact and father when contacts are linked to a father which is a
   * descendant of a node
   * @param con Connection to database
   * @param contactPK the contact ContactPK
   * @throws java.sql.SQLException if an error occurs
   * @see NodePK
   * @see ContactPK
   * @since 1.0
   */
  Collection<NodePK> getAllFatherPK(Connection con, ContactPK contactPK)
      throws SQLException {
    try (final PreparedStatement prepStmt = con.prepareStatement(
        "SELECT nodeId FROM sb_contact_contactfather WHERE contactId = ?")) {
      prepStmt.setInt(1, Integer.parseInt(contactPK.getId()));
      try (final ResultSet rs = prepStmt.executeQuery()) {
        List<NodePK> list = new ArrayList<>();
        while (rs.next()) {
          NodePK nodePK = new NodePK(String.valueOf(rs.getInt(1)), contactPK);
          list.add(nodePK);
        }
        return list;
      }
    }
  }

  int getNbPubInFatherPKs(Connection con, Collection<NodePK> fatherPKs) throws SQLException {
    int result = 0;
    ContactPK pubPK;
    if (fatherPKs.isEmpty()) {
      return 0;
    } else {
      Iterator<NodePK> iterator = fatherPKs.iterator();
      if (iterator.hasNext()) {
        NodePK nodePK = iterator.next();
        pubPK = new ContactPK(UNKNOWN, nodePK);
        String nodeId = nodePK.getId();

        final StringBuilder selectStatement =
            new StringBuilder("select count(contactId) from ").append(pubPK.getTableName())
                .append("Father")
                .append(" where nodeId = ")
                .append(nodeId);

        while (iterator.hasNext()) {
          nodePK = iterator.next();
          nodeId = nodePK.getId();
          selectStatement.append(" or nodeId = ").append(nodeId);
        }
        selectStatement.append(" )");

        try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement.toString());
             final ResultSet rs = prepStmt.executeQuery()) {
          if (rs.next()) {
            result = rs.getInt(1);
          }
        }
      }

      return result;
    }
  }

  int getNbPubByFatherPath(Connection con, NodePK fatherPK, String fatherPath) throws SQLException {
    int result = 0;
    ContactPK pubPK = new ContactPK(UNKNOWN, fatherPK);
    if (fatherPath.length() <= 0) {
      return 0;
    } else {
      String selectStatement =
          "select count(F.contactId) from " + pubPK.getTableName() + "Father F, " +
              fatherPK.getTableName() + " N " + " where F.nodeId = N.nodeId " +
              " and N.instanceId = ?" + " and N.nodeId = ?" + " and N.nodePath like '" +
              fatherPath + "%'";

      try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
        prepStmt.setString(1, fatherPK.getComponentName());
        prepStmt.setInt(2, Integer.parseInt(fatherPK.getId()));
        try (final ResultSet rs = prepStmt.executeQuery()) {
          if (rs.next()) {
            result = rs.getInt(1);
          }
        }
      }
      return result;
    }
  }

  public void insertRow(Connection con, Contact detail) throws SQLException {
    final String insertStatement = "INSERT INTO " + detail.getPK().getTableName() +
        " VALUES ( ? , ? , ? , ? , ? , ? , ? , ?, ? , ?)";
    try (final PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      int id = Integer.parseInt(detail.getPK().getId());
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
    }
  }

  void deleteContact(Connection con, ContactPK pk) throws SQLException {
    removeAllFather(con, pk); // Delete associations between pub and nodes
    final String deleteStatement =
        DELETE_FROM + pk.getTableName() + " where contactId = ? and instanceId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.setString(2, pk.getComponentName());
      prepStmt.executeUpdate();

    }
  }

  void deleteAllContacts(Connection con, String instanceId) throws SQLException {
    final String allFatherDeletion =
        "DELETE FROM SB_Contact_ContactFather WHERE contactId in (SELECT contactId FROM " +
            "SB_Contact_Contact WHERE instanceId = ?)";
    final String allContactsDeletion = "DELETE FROM SB_Contact_Contact WHERE instanceId = ?";
    try (PreparedStatement deletion = con.prepareStatement(allFatherDeletion)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
    try (PreparedStatement deletion = con.prepareStatement(allContactsDeletion)) {
      deletion.setString(1, instanceId);
      deletion.execute();
    }
  }

  ContactPK selectByPrimaryKey(Connection con, ContactPK primaryKey)
      throws SQLException, ParseException {
    ContactDetail detail = loadRow(con, primaryKey);
    ContactPK primary = new ContactPK(primaryKey.getId(), primaryKey);
    primary.contactDetail = detail;
    return primary;
  }

  private static ContactDetail resultSet2ContactDetail(ResultSet rs, ContactPK contactPK)
      throws SQLException, ParseException {
    int id = rs.getInt(1);
    ContactPK pk = new ContactPK(String.valueOf(id), contactPK);
    String firstName = rs.getString(2);
    String lastName = rs.getString(3);
    String email = rs.getString(4);
    String phone = rs.getString(5);
    String fax = rs.getString(6);
    String userId = rs.getString(7);
    java.util.Date creationDate;
    creationDate = DateUtil.parse(rs.getString(8));
    String creatorId = rs.getString(9);
    return new ContactDetail(pk, firstName, lastName, email, phone, fax, userId, creationDate,
        creatorId);
  }

  Collection<ContactDetail> selectByLastName(Connection con, ContactPK pk,
      String query) throws SQLException, ParseException {
    final String selectStatement =
        SELECT_P + FROM + pk.getTableName() + " P, " + pk.getTableName() + FATHER_F +
            WHERE_F_CONTACT_ID_EQUAL_TO_P_CONTACT_ID +
            " and LOWER(P.contactLastName) LIKE LOWER(?)" +
            AND_P_INSTANCE_ID_EQUAL_TO_GIVEN_VALUE;

    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, query);
      prepStmt.setString(2, pk.getComponentName());
      return fetchContactDetails(pk, prepStmt);
    }
  }

  Collection<ContactDetail> selectByLastNameOrFirstName(Connection con, ContactPK pk,
      String query) throws SQLException, ParseException {
    final String selectStatement =
        SELECT_P + FROM + pk.getTableName() + " P, " + pk.getTableName() + FATHER_F +
            WHERE_F_CONTACT_ID_EQUAL_TO_P_CONTACT_ID + " and (LOWER(P.contactLastName) LIKE LOWER(?)" +
            " or LOWER(P.contactFirstName) LIKE LOWER(?))" + AND_P_INSTANCE_ID_EQUAL_TO_GIVEN_VALUE;

    return fetchContactDetails(con, selectStatement, pk, query, query);
  }

  Collection<ContactDetail> selectByFatherPK(Connection con, NodePK fatherPK)
      throws SQLException, ParseException {
    final ContactPK pubPK = new ContactPK(UNKNOWN, fatherPK);
    final String selectStatement =
        "select  P.* " + FROM + pubPK.getTableName() + " P, " + pubPK.getTableName() + FATHER_F
            + "where F.nodeId = ? and " + " F.contactId = P.contactId " +
            AND_P_INSTANCE_ID_EQUAL_TO_GIVEN_VALUE;

    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(fatherPK.getId()));
      prepStmt.setString(2, fatherPK.getComponentName());
      return fetchContactDetails(pubPK, prepStmt);
    }
  }

  private Collection<ContactDetail> fetchContactDetails(final ContactPK pubPK,
      final PreparedStatement prepStmt) throws SQLException, ParseException {
    try (final ResultSet rs = prepStmt.executeQuery()) {
      List<ContactDetail> list = new ArrayList<>();
      while (rs.next()) {
        ContactDetail pub = resultSet2ContactDetail(rs, pubPK);
        list.add(pub);
      }
      return list;
    }
  }

  Collection<ContactFatherDetail> selectByFatherPKs(Connection con,
      Collection<NodePK> fatherPKs, ContactPK contactPK, NodePK nodePK)
      throws SQLException, ParseException {
    String fatherId;
    StringBuilder whereClause = new StringBuilder();
    if (fatherPKs != null) {
      Iterator<NodePK> it = fatherPKs.iterator();
      whereClause.append("(");

      while (it.hasNext()) {
        fatherId = it.next().getId();
        whereClause.append(" F.nodeId = ").append(fatherId);
        if (it.hasNext()) {
          whereClause.append(" OR ");
        } else {
          whereClause.append(" ) ");
        }
      }
    }

    final String selectStatement =
        "select  P.*, N.nodeId, N.nodeName " + FROM + contactPK.getTableName() + " P, " +
            contactPK.getTableName() + "father F, " + nodePK.getTableName() + " N " + "where " +
            whereClause.toString() +
            " AND F.contactId = P.contactId AND F.nodeId = N.nodeId AND N.instanceId = ?" +
            " AND N.instanceId = P.instanceId ";

    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, nodePK.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        List<ContactFatherDetail> list = new ArrayList<>();
        while (rs.next()) {
          ContactDetail pub = resultSet2ContactDetail(rs, contactPK);
          ContactFatherDetail contactFather =
              new ContactFatherDetail(pub, rs.getString(11), rs.getString(12));
          list.add(contactFather);
        }
        return list;
      }
    }
  }

  Collection<ContactDetail> selectByContactPKs(Connection con,
      Collection<ContactPK> contactPKs) throws SQLException, ParseException {
    List<ContactDetail> contacts = new ArrayList<>();
    for (final ContactPK contactPK : contactPKs) {
      ContactDetail contactDetail = loadRow(con, contactPK);
      contacts.add(contactDetail);
    }
    return contacts;
  }

  Collection<ContactDetail> getOrphanContacts(Connection con, ContactPK contactPK)
      throws SQLException, ParseException {
    try (final PreparedStatement prepStmt = con.prepareStatement(
        "SELECT * FROM SB_Contact_Contact WHERE contactId NOT IN " +
            "(SELECT contactId FROM SB_Contact_Contactfather) AND instanceId = ? ")) {
      prepStmt.setString(1, contactPK.getComponentName());
      return fetchContactDetails(contactPK, prepStmt);
    }
  }

  void deleteOrphanContactsByCreatorId(Connection con, ContactPK contactPK,
      String creatorId) throws SQLException {
    final String deleteStatement =
        DELETE_FROM + contactPK.getTableName() + " where contactCreatorId = ? " +
            " and instanceId = ? " + " and contactId NOT IN (Select contactId from " +
            contactPK.getTableName() + "father) ";

    try (final PreparedStatement prepStmt = con.prepareStatement(deleteStatement)) {
      prepStmt.setString(1, creatorId);
      prepStmt.setString(2, contactPK.getComponentName());
      prepStmt.executeUpdate();
    }
  }

  ContactDetail loadRow(Connection con, ContactPK pk)
      throws SQLException, ParseException {
    final String selectStatement =
        "select  * from " + pk.getTableName() + " where contactId = ? and instanceId = ?";

    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(pk.getId()));
      prepStmt.setString(2, pk.getComponentName());
      try (final ResultSet rs = prepStmt.executeQuery()) {
        if (rs.next()) {
          return resultSet2ContactDetail(rs, pk);
        } else {
          throw new ContactRuntimeException("Contact not found with id = " + pk.getId());
        }
      }
    }
  }

  void storeRow(Connection con, Contact detail) throws SQLException {
    final String insertStatement = "update " + detail.getPK().getTableName() + " set " +
        "contactFirstName = ? , contactLastName = ? , " +
        "contactEmail = ? , contactPhone = ? , contactFax = ? , userId = ? ," +
        "contactCreationDate = ? , contactCreatorId = ? " +
        "where contactId = ? and instanceId = ?";
    try (final PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setString(1, detail.getFirstName());
      prepStmt.setString(2, detail.getLastName());
      prepStmt.setString(3, detail.getEmail());
      prepStmt.setString(4, detail.getPhone());
      prepStmt.setString(5, detail.getFax());
      prepStmt.setString(6, detail.getUserId());
      prepStmt.setString(7, DateUtil.date2SQLDate(detail.getCreationDate()));
      prepStmt.setString(8, detail.getCreatorId());
      prepStmt.setInt(9, Integer.parseInt(detail.getPK().getId()));
      prepStmt.setString(10, detail.getPK().getComponentName());

      int rowCount = prepStmt.executeUpdate();
      if (rowCount == 0) {
        throw new ContactRuntimeException("Contact update failure. Id = " + detail.getPK().getId());
      }
    }
  }

  Collection<ContactDetail> getUnavailableContactsByPublisherId(Connection con,
      ContactPK contactPK, String publisherId, String nodeId) throws SQLException, ParseException {
    final String selectStatement =
        "SELECT  P.* " + "FROM " + contactPK.getTableName() + " P, " + contactPK.getTableName() +
            FATHER_F + "WHERE F.nodeId = ? AND " +
            "contactCreatorId = ? AND F.contactId = P.contactId AND P.instanceId = ? ";

    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(nodeId));
      prepStmt.setString(2, publisherId);
      prepStmt.setString(3, contactPK.getComponentName());
      return fetchContactDetails(contactPK, prepStmt);
    }
  }

  Collection<ContactDetail> selectByLastNameAndFirstName(Connection con, ContactPK pk,
      String lastName, String firstName) throws SQLException, ParseException {
    final String selectStatement =
        SELECT_P + FROM + pk.getTableName() + " P, " + pk.getTableName() + FATHER_F +
            WHERE_F_CONTACT_ID_EQUAL_TO_P_CONTACT_ID + " and LOWER(P.contactLastName) LIKE LOWER(?)" +
            " and LOWER(P.contactFirstName) LIKE LOWER(?)" + AND_P_INSTANCE_ID_EQUAL_TO_GIVEN_VALUE;

    return fetchContactDetails(con, selectStatement, pk, lastName, firstName);
  }

  private Collection<ContactDetail> fetchContactDetails(final Connection con,
      final String selectStatement, final ContactPK pk, final String lastName,
      final String firstName) throws SQLException, ParseException {
    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, lastName);
      prepStmt.setString(2, firstName);
      prepStmt.setString(3, pk.getComponentName());
      return fetchContactDetails(pk, prepStmt);
    }
  }

  /**
   * Gets all non transitive contacts from an component represented by the given identifier that are
   * not in basket.
   * A transitive contact is a contact of user type linked directly by a group.
   * @param con the database connection.
   * @param instanceId the identifier of the component instance into which contact are retrieved.
   * @return the list of complete contact data. Empty list if none.
   * @throws SQLException if an error occurs
   * @throws ParseException if an error occurs
   */
  List<CompleteContact> getVisibleContacts(Connection con, String instanceId)
      throws SQLException, ParseException {
    final ContactPK pubPK = new ContactPK(UNKNOWN, instanceId);
    final String selectStatement = "select P.*, I.modelId" +
            " from " + pubPK.getTableName() + " P" +
            " join " + pubPK.getTableName() + "father F on F.contactId = P.contactId" +
            " left outer join sb_contact_info I on P.contactId = I.contactId" +
            " where F.nodeId <> ? and P.instanceId = ? ";

    try (final PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setInt(1, Integer.parseInt(NodePK.BIN_NODE_ID));
      prepStmt.setString(2, instanceId);
      try (final ResultSet rs = prepStmt.executeQuery()) {
        List<CompleteContact> list = new ArrayList<>();
        while (rs.next()) {
          ContactDetail pub = resultSet2ContactDetail(rs, pubPK);
          CompleteContact contact = new CompleteContact(pub, rs.getString("modelId"));
          list.add(contact);
        }
        return list;
      }
    }
  }
}