/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
package org.silverpeas.core.contribution.publication.dao;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.contribution.publication.model.Location;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.MapUtil;
import org.silverpeas.core.util.StringUtil;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is the Publication Father Data Access Object.
 * @author Nicolas Eysseric
 */
public class PublicationFatherDAO {

  private static final String NODE_ID = "nodeId";
  private static final String ALIAS_USER_ID = "aliasUserId";
  private static final String ALIAS_DATE = "aliasDate";
  private static final String PUB_ORDER = "pubOrder";
  private static final String PUB_ID = "pubId";
  private static final String INSTANCE_ID = "instanceId";
  private static final String EQUALITY = " = ?";
  private static final String INSTANCE_ID_SET = INSTANCE_ID + EQUALITY;
  private static final String PUB_ID_SET = PUB_ID + EQUALITY;
  private static final String NODE_ID_SET = NODE_ID + EQUALITY;
  private static final String LOCATION_FIELDS = "nodeId, instanceId, aliasUserId, aliasDate, pubOrder";
  static final String PUBLICATION_FATHER_TABLE_NAME = "SB_Publication_PubliFather";

  private PublicationFatherDAO() {
  }

  /**
   * Deletes all locations of publications linked to the component instance represented by the
   * given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static void deleteComponentInstanceData(String componentInstanceId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(PUBLICATION_FATHER_TABLE_NAME).where("pubId in (" +
        JdbcSqlQuery.createSelect(PUB_ID).from(PublicationDAO.PUBLICATION_TABLE_NAME)
            .where(INSTANCE_ID_SET).getSqlQuery() + ")", componentInstanceId).execute();
    JdbcSqlQuery.createDeleteFor(PUBLICATION_FATHER_TABLE_NAME)
        .where(INSTANCE_ID_SET, componentInstanceId).execute();
  }

  /**
   * Adds a new father to this publication.
   * @param con Connection to the database
   * @param pubPK the persistence identifier of the publication.
   * @param fatherPK the father the persistence identifier of the new father of the publication.
   * @see NodePK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException if an error occurs while requesting the data source.
   */
  public static void addFather(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    JdbcSqlQuery.createInsertFor(PUBLICATION_FATHER_TABLE_NAME)
        .addInsertParam(PUB_ID, Integer.parseInt(pubPK.getId()))
        .addInsertParam(NODE_ID, Integer.parseInt(fatherPK.getId()))
        .addInsertParam(INSTANCE_ID, pubPK.getInstanceId())
        .addInsertParam(ALIAS_USER_ID, null)
        .addInsertParam(ALIAS_DATE, null)
        .addInsertParam(PUB_ORDER, 0)
        .executeWith(con);
  }

  /**
   * Updates the order of the publication among the children of the specified father with the given
   * order value.
   * @param con the connection to the data source
   * @param pubPK the identifier of the publication in the data source.
   * @param fatherPK the identifier of the father in the data source.
   * @param order the new order of the publication.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static void updateOrder(Connection con, PublicationPK pubPK, NodePK fatherPK, int order)
      throws SQLException {
    JdbcSqlQuery.createUpdateFor(PUBLICATION_FATHER_TABLE_NAME)
        .addUpdateParam(PUB_ORDER, order)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .and(NODE_ID_SET, Integer.parseInt(fatherPK.getId()))
        .and(INSTANCE_ID_SET, pubPK.getInstanceId())
        .executeWith(con);
  }

  /**
   * Adds a new alias to the specified publication. If the given location isn't an alias, then
   * an {@link IllegalArgumentException} exception is thrown.
   * @param con the connection to the data source.
   * @param pubPK the identifier of the publication in the data source.
   * @param location the new location of the publication as alias.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static void addAlias(Connection con, PublicationPK pubPK, Location location)
      throws SQLException {
    if (!location.isAlias()) {
      throw new IllegalArgumentException("Location " + location.getId() + " isn't an alias!");
    }

    final Location.Alias alias = location.getAlias();
    final String userId =
        alias.getUserId() != null ? alias.getUserId() : User.getCurrentRequester().getId();
    final Date date = alias.getDate() != null ? alias.getDate() : new Date();

     JdbcSqlQuery.createInsertFor(PUBLICATION_FATHER_TABLE_NAME)
         .addInsertParam(PUB_ID, Integer.parseInt(pubPK.getId()))
         .addInsertParam(NODE_ID, Integer.parseInt(location.getId()))
         .addInsertParam(INSTANCE_ID, location.getInstanceId())
         .addInsertParam(ALIAS_USER_ID, Integer.parseInt(userId))
         .addInsertParam(ALIAS_DATE, Long.toString(date.getTime()))
         .addInsertParam(PUB_ORDER, location.getPubOrder())
         .executeWith(con);
  }

  /**
   * Removes the specified alias among the aliases of the given publication. If the given
   * location isn't an alias, then an {@link IllegalArgumentException} exception is thrown.
   * @param con the connection to the data source.
   * @param pubPK the unique identifier of the publication in the data source.
   * @param location the alias to remove.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static void removeAlias(Connection con, PublicationPK pubPK, Location location)
      throws SQLException {

    if (!location.isAlias()) {
      throw new IllegalArgumentException(
          "Location " + location.getId() + " isn't an alias for publication " + pubPK.getId());
    }

    JdbcSqlQuery.createDeleteFor(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .and(NODE_ID_SET, Integer.parseInt(location.getId()))
        .and(INSTANCE_ID_SET, location.getInstanceId())
        .andNotNull(ALIAS_DATE)
        .andNotNull(ALIAS_USER_ID)
        .executeWith(con);
  }

  /**
   * Selects massively simple data about main locations.
   * <p>
   *   This method is designed for process performance needs.
   * </p>
   * @param con the database connection.
   * @param ids the instance ids aimed.
   * @return a list of {@link Location} instances.
   * @throws SQLException on database error.
   */
  public static Map<String, List<Location>> getAllMainLocationsByPublicationIds(Connection con,
      Collection<PublicationPK> ids) throws SQLException {
    return getAllLocationsByPublicationIds(con, ids, true);
  }

  /**
   * Selects massively simple data about all locations (main or aliases).
   * <p>
   *   This method is designed for process performance needs.
   * </p>
   * @param con the database connection.
   * @param ids the instance ids aimed.
   * @return a list of {@link Location} instances.
   * @throws SQLException on database error.
   */
  public static Map<String, List<Location>> getAllLocationsByPublicationIds(Connection con,
      Collection<PublicationPK> ids) throws SQLException {
    return getAllLocationsByPublicationIds(con, ids, false);
  }

  private static Map<String, List<Location>> getAllLocationsByPublicationIds(Connection con,
      Collection<PublicationPK> ids, boolean onlyMainLocations) throws SQLException {
    final List<String> pubIds = ids.stream().map(PublicationPK::getId).collect(Collectors.toList());
    return JdbcSqlQuery.executeBySplittingOn(pubIds, (pubIdBatch, result) -> {
      final JdbcSqlQuery query = JdbcSqlQuery.createSelect(LOCATION_FIELDS + ", " + PUB_ID)
          .from(PUBLICATION_FATHER_TABLE_NAME)
          .where(PUB_ID).in(pubIdBatch.stream().map(Integer::parseInt).collect(Collectors.toList()));
      if (onlyMainLocations) {
        query.andNull(ALIAS_USER_ID);
        query.andNull(ALIAS_DATE);
      }
      query.executeWith(con, r -> {
        final Location location = fetchLocation(r);
        MapUtil.putAddList(result, Integer.toString(r.getInt(6)), location);
        return null;
      });
    });
  }

  /**
   * Gets all the locations (the original one and the aliases) of the specified publication.
   * @param con a connection to the data source.
   * @param pubPK the unique identifying key of the publication.
   * @return a collection of the locations of the publication.
   * @throws SQLException if an error occurs while executing the SQL request.
   */
  public static List<Location> getLocations(Connection con, PublicationPK pubPK) throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect(LOCATION_FIELDS)
        .from(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()));
    return findLocations(con, query);
  }

  /**
   * Gets the locations of the specified publication in the given component instance.
   * @param con a connection to the data source.
   * @param pubPK the unique identifying key of the publication.
   * @param compoId the unique identifier of a component instance.
   * @return a collection of the locations of the publication in the component instance.
   * @throws SQLException if an error occurs while executing the SQL request.
   */
  public static List<Location> getLocations(Connection con, PublicationPK pubPK, String compoId)
      throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect(LOCATION_FIELDS)
        .from(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .and(INSTANCE_ID_SET, compoId);
    return findLocations(con, query);
  }

  private static List<Location> findLocations(final Connection con, final JdbcSqlQuery query)
      throws SQLException {
    return query.executeWith(con, PublicationFatherDAO::fetchLocation);
  }

  private static Location fetchLocation(final ResultSet rs) throws SQLException {
    String id = Integer.toString(rs.getInt(1));
    String instanceId = rs.getString(2);
    Location location = new Location(id, instanceId);
    String sDate = rs.getString(4);
    if (StringUtil.isDefined(sDate)) {
      Date date = new Date(Long.parseLong(sDate));
      String userId = Integer.toString(rs.getInt(3));
      if (!rs.wasNull()) {
        location.setAsAlias(userId, date);
      }
    }
    int pubOrder = rs.getInt(5);
    location.setPubOrder(pubOrder);
    return location;
  }

  /**
   * Gets the main location of the specified publication.
   * @param con a connection to the data source.
   * @param pubPK the unique identifying key of the publication.
   * @return the main location of the specified publication or null.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static Location getMainLocation(Connection con, PublicationPK pubPK) throws SQLException {
    return JdbcSqlQuery.createSelect("nodeId, instanceId, pubOrder")
        .from(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .and(INSTANCE_ID_SET, pubPK.getInstanceId())
        .andNull(ALIAS_USER_ID)
        .andNull(ALIAS_DATE)
        .executeUniqueWith(con, rs -> {
          String id = Integer.toString(rs.getInt(1));
          String instanceId = rs.getString(2);
          Location location = new Location(id, instanceId);
          int pubOrder = rs.getInt(3);
          location.setPubOrder(pubOrder);
          return location;
        });
  }

  /**
   * Removes a father of this publication.
   * @param con Connection to database
   * @param pubPK the unique identifier of the publication in the data source.
   * @param fatherPK the unique identifier of the father to delete in the data source.
   * @see NodePK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException if an error occurs while requesting the data source.
   */
  public static void removeFather(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    removeLink(con, pubPK, fatherPK);
  }

  private static void removeFatherToPublications(Connection con, NodePK fatherPK)
      throws SQLException {
    // get all publications linked to fatherPK
    List<PublicationPK> pubPKs = (List<PublicationPK>) getPubPKsInFatherPK(con, fatherPK);

    // for each publication, remove link into table
    for (PublicationPK publicationPK : pubPKs) {
      removeLink(con, publicationPK, fatherPK);
    }
  }

  private static void removeLink(Connection con, PublicationPK pubPK,
      NodePK fatherPK) throws SQLException {
    JdbcSqlQuery.createDeleteFor(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .and(NODE_ID_SET, Integer.parseInt(fatherPK.getId()))
        .executeWith(con);
  }

  public static void removeFathersToPublications(Connection con,
      PublicationPK pubPK, Collection<String> fatherIds) throws SQLException {
    for (final String fatherId : fatherIds) {
      NodePK fatherPK = new NodePK(fatherId, pubPK);
      removeFatherToPublications(con, fatherPK);
    }
  }

  /**
   * Deletes all the fathers of this publication. The publication will be then orphaned.
   * @param con connection to the  database
   * @param pubPK the unique identifier of the publication.
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @exception java.sql.SQLException if an error occurs while requesting the database.
   */
  public static void removeAllFathers(Connection con, PublicationPK pubPK)
      throws SQLException {
    JdbcSqlQuery.createDeleteFor(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .executeWith(con);
  }

  /**
   * Gets the identifiers of all the fathers of the specified publication and that are in the same
   * component instance the publication is.
   * @param con the connection to the database
   * @param pubPK the unique identifier of the publication.
   * @return a collection of all of the persistence identifiers of the publication's fathers.
   * @see NodePK
   * @see org.silverpeas.core.contribution.publication.model.PublicationPK
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static List<NodePK> getAllFatherPKInSamePublicationComponentInstance(Connection con,
      PublicationPK pubPK) throws SQLException {
    return JdbcSqlQuery.createSelect(NODE_ID).from(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .and(INSTANCE_ID_SET, pubPK.getInstanceId())
        .executeWith(con, row -> {
          String id = Integer.toString(row.getInt(1));
          return new NodePK(id, pubPK);
        });
  }

  /**
   * Gets the identifiers of all of the publications that have as father at least one of the
   * specified ones.
   * @param con the connection to the data source.
   * @param fatherPK the unique identifier of the fathers in the data source.
   * @return a collection of the publication's identifiers in the data source.
   * @throws SQLException if an error occurs while requesting the data source.
   */
  public static Collection<PublicationPK> getPubPKsInFatherPK(Connection con, NodePK fatherPK)
      throws SQLException {
    PublicationPK pubPK = new PublicationPK("unknown", fatherPK);
    return JdbcSqlQuery.createSelect("P.pubId, P.instanceId")
        .from(PUBLICATION_FATHER_TABLE_NAME + " F", pubPK.getTableName() + " P")
        .where("F.instanceId = ?", fatherPK.getInstanceId())
        .and("F.nodeId = ?", Integer.parseInt(fatherPK.getId()))
        .and("F.pubId = P.pubId")
        .executeWith(con, row -> {
          String id = Integer.toString(row.getInt(1));
          String instanceId = row.getString(2);
          return new PublicationPK(id, instanceId);
        });
  }

  /**
   * Gets all the aliases of the specified publication.
   * @param con a connection to the data source.
   * @param pubPK the unique identifying key of the publication.
   * @return a collection of the aliases of the publication, each of them being a location.
   * @throws SQLException if an error occurs while executing the SQL request.
   */
  public static List<Location> getAliases(final Connection con, final PublicationPK pubPK)
      throws SQLException {
    final JdbcSqlQuery query = JdbcSqlQuery.createSelect(LOCATION_FIELDS)
        .from(PUBLICATION_FATHER_TABLE_NAME)
        .where(PUB_ID_SET, Integer.parseInt(pubPK.getId()))
        .andNotNull(ALIAS_DATE)
        .andNotNull(ALIAS_USER_ID);
    return findLocations(con, query);
  }
}