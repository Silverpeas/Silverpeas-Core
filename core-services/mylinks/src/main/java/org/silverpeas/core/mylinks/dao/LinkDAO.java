/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.mylinks.dao;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.lang.Integer.parseInt;

@Repository
public class LinkDAO {

  private static final String LINK_TABLE = "SB_MyLinks_Link";
  private static final String LINK_ID = "linkId";
  private static final String LINK_ID_CLAUSE = "linkId = ?";
  private static final String USER_ID_CLAUSE = "userId = ?";
  private static final String INSTANCE_ID_CLAUSE = "instanceId = ?";
  private static final String OBJECT_ID_CLAUSE = "objectId = ?";

  /**
   * Hide constructor of utility class
   */
  protected LinkDAO() {
  }

  /**
   * Deletes all links about the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException on SQL problem
   */
  public void deleteComponentInstanceData(String componentInstanceId)
      throws SQLException {
    JdbcSqlQuery.createDeleteFor(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, componentInstanceId)
        .or("url like ?", "%" + componentInstanceId)
        .execute();
  }

  /**
   * Retrieve user links
   * @param userId the user identifier
   * @return list of user links
   * @throws SQLException on SQL problem
   */
  public List<LinkDetail> getAllLinksByUser(String userId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("*")
        .from(LINK_TABLE)
        .where(USER_ID_CLAUSE, userId)
        .and("(instanceId IS NULL").or("instanceId = '')")
        .and("(objectId IS NULL").or("objectId = '')")
        .execute(LinkDAO::fetchLink);
  }

  /**
   * Retrieve all links about a component instance id.
   * @param instanceId the component instance identifier
   * @return list of LinkDetail
   * @throws SQLException on SQL problem
   */
  public List<LinkDetail> getAllLinksByInstance(String instanceId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("*")
        .from(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .execute(LinkDAO::fetchLink);
  }

  /**
   * Retrieve all links about an object id on a component instance id
   * @param instanceId the component instance identifier which hosts the object
   * @param objectId the identifier of the object
   * @return list of LinkDetail
   * @throws SQLException on SQL problem
   */
  public List<LinkDetail> getAllLinksByObject(String instanceId, String objectId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("*")
        .from(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .and(OBJECT_ID_CLAUSE, objectId)
        .execute(LinkDAO::fetchLink);
  }

  /**
   * Retrieve link from identifier
   * @param linkId the link identifier
   * @return the link detail
   * @throws SQLException on SQL problem
   */
  public LinkDetail getLink(String linkId) throws SQLException {
    return JdbcSqlQuery.createSelect("*")
        .from(LINK_TABLE)
        .where(LINK_ID_CLAUSE, parseInt(linkId))
        .executeUnique(LinkDAO::fetchLink);
  }

  /**
   * Create new link
   * @param link link detail to create
   * @return new link instance
   * @throws SQLException on SQL problem
   */
  public LinkDetail createLink(LinkDetail link) throws SQLException {
    final LinkDetail linkToPersist = new LinkDetail(link);
    linkToPersist.setLinkId(DBUtil.getNextId(LINK_TABLE, LINK_ID));
    linkToPersist.setHasPosition(false);
    final JdbcSqlQuery insertQuery = JdbcSqlQuery.createInsertFor(LINK_TABLE);
    setupSaveQuery(insertQuery, linkToPersist, true).execute();
    return linkToPersist;
  }

  /**
   * Update a link
   * @param link link detail to update
   * @return updated link instance
   * @throws SQLException on SQL problem
   */
  public LinkDetail updateLink(LinkDetail link) throws SQLException {
    final LinkDetail linkToUpdate = new LinkDetail(link);
    final JdbcSqlQuery updateQuery = JdbcSqlQuery.createUpdateFor(LINK_TABLE);
    setupSaveQuery(updateQuery, linkToUpdate, false).execute();
    return linkToUpdate;
  }

  /**
   * Remove a link
   * @param linkId the link identifier to remove
   * @throws SQLException on SQL problem
   */
  public void deleteLink(String linkId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(LINK_TABLE)
        .where(LINK_ID_CLAUSE, parseInt(linkId))
        .execute();
  }

  private static LinkDetail fetchLink(final ResultSet rs) throws SQLException {
    final LinkDetail link = new LinkDetail();
    link.setLinkId(rs.getInt(LINK_ID));
    link.setPosition(rs.getInt("position"));
    link.setHasPosition(!rs.wasNull());
    link.setName(rs.getString("name"));
    link.setDescription(rs.getString("description"));
    link.setUrl(rs.getString("url"));
    link.setVisible(rs.getInt("visible") == 1);
    link.setPopup(rs.getInt("popup") == 1);
    link.setUserId(rs.getString("userId"));
    link.setInstanceId(rs.getString("instanceId"));
    link.setObjectId(rs.getString("objectId"));
    return link;
  }

  private static JdbcSqlQuery setupSaveQuery(final JdbcSqlQuery saveQuery, final LinkDetail link,
      final boolean isInsert) {
    if (isInsert) {
      saveQuery.addSaveParam(LINK_ID, link.getLinkId(), true);
    }
    final String name = StringUtil.truncate(link.getName(), 255);
    final String description = StringUtil.truncate(link.getDescription(), 255);
    final String url = StringUtil.truncate(link.getUrl(), 255);
    saveQuery
        .addSaveParam("name", name, isInsert)
        .addSaveParam("description", description, isInsert)
        .addSaveParam("url", url, isInsert)
        .addSaveParam("visible", link.isVisible() ? 1 : 0, isInsert)
        .addSaveParam("popup", link.isPopup() ? 1 : 0, isInsert)
        .addSaveParam("userId", link.getUserId(), isInsert)
        .addSaveParam("instanceId", link.getInstanceId(), isInsert)
        .addSaveParam("objectId", link.getObjectId(), isInsert);
    if (link.hasPosition()) {
      saveQuery.addSaveParam("position", link.getPosition(), isInsert);
    }
    if (!isInsert) {
      saveQuery.where(LINK_ID_CLAUSE, link.getLinkId());
    }
    return saveQuery;
  }
}
