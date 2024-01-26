/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.mylinks.dao;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyMap;

@Repository
public class LinkDAO {

  private static final String LINK_TABLE = "SB_MyLinks_Link";
  private static final String LINK_ID = "linkId";
  private static final String LINK_ID_CLAUSE = "linkId = ?";
  private static final String USER_ID_CLAUSE = "userId = ?";
  private static final String INSTANCE_ID_CLAUSE = "instanceId = ?";
  private static final String OBJECT_ID_CLAUSE = "objectId = ?";

  @Inject
  private LinkCategoryDAO linkCategoryDAO;

  protected static LinkDAO get() {
    return ServiceProvider.getService(LinkDAO.class);
  }

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
    linkCategoryDAO.deleteComponentInstanceData(componentInstanceId);
    JdbcSqlQuery.deleteFrom(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, componentInstanceId)
        .or("url like ?", "%" + componentInstanceId)
        .execute();
  }

  /**
   * Gets all the link identifier which references the identifier of a component instance (url,
   * instanceId, etc.).
   * <p>
   *   It is not the same behavior as {@link #getAllLinksByInstance(String)} method which is
   *   searching for data only against instanceId field.
   * </p>
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException on SQL problem
   */
  protected List<Integer> getLinkIdsByComponentInstance(String componentInstanceId)
      throws SQLException {
    return JdbcSqlQuery.select(LINK_ID)
        .from(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, componentInstanceId)
        .or("url like ?", "%" + componentInstanceId)
        .execute(r -> r.getInt(1));
  }

  /**
   * Delete all
   * @param userId the identifier of the user for which the resources must be deleted
   * @throws SQLException on SQL problem
   */
  public void deleteUserData(String userId) throws SQLException {
    linkCategoryDAO.deleteUserData(userId);
    JdbcSqlQuery.deleteFrom(LINK_TABLE)
        .where(USER_ID_CLAUSE, userId)
        .and("(instanceId IS NULL").or("instanceId = '')")
        .and("(objectId IS NULL").or("objectId = '')")
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
    final Map<Integer, CategoryDetail> categoriesByLinkId =
        linkCategoryDAO.getAllCategoriesByLinkOfUser(userId);
    return JdbcSqlQuery.select("*")
        .from(LINK_TABLE)
        .where(USER_ID_CLAUSE, userId)
        .and("(instanceId IS NULL").or("instanceId = '')")
        .and("(objectId IS NULL").or("objectId = '')")
        .execute(rs -> fetchLink(rs, categoriesByLinkId));
  }

  /**
   * Retrieve all links about a component instance id.
   * @param instanceId the component instance identifier
   * @return list of LinkDetail
   * @throws SQLException on SQL problem
   */
  public List<LinkDetail> getAllLinksByInstance(String instanceId)
      throws SQLException {
    return JdbcSqlQuery.select("*")
        .from(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .execute(rs -> fetchLink(rs, emptyMap()));
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
    return JdbcSqlQuery.select("*")
        .from(LINK_TABLE)
        .where(INSTANCE_ID_CLAUSE, instanceId)
        .and(OBJECT_ID_CLAUSE, objectId)
        .execute(rs -> fetchLink(rs, emptyMap()));
  }

  /**
   * Retrieve link from identifier
   * @param linkId the link identifier
   * @return the link detail
   * @throws SQLException on SQL problem
   */
  public LinkDetail getLink(int linkId) throws SQLException {
    final Map<Integer, CategoryDetail> categoriesByLinkId =
        linkCategoryDAO.getAllCategoriesByLink(linkId);
    return JdbcSqlQuery.select("*")
        .from(LINK_TABLE)
        .where(LINK_ID_CLAUSE, linkId)
        .executeUnique(rs -> fetchLink(rs, categoriesByLinkId));
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
    final JdbcSqlQuery insertQuery = JdbcSqlQuery.insertInto(LINK_TABLE);
    setupSaveQuery(insertQuery, linkToPersist, true).execute();
    linkCategoryDAO.saveByLink(linkToPersist);
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
    final JdbcSqlQuery updateQuery = JdbcSqlQuery.update(LINK_TABLE);
    setupSaveQuery(updateQuery, linkToUpdate, false).execute();
    linkCategoryDAO.saveByLink(linkToUpdate);
    return linkToUpdate;
  }

  /**
   * Remove a link
   * @param linkId the link identifier to remove
   * @throws SQLException on SQL problem
   */
  public void deleteLink(String linkId) throws SQLException {
    linkCategoryDAO.deleteByLink(parseInt(linkId));
    JdbcSqlQuery.deleteFrom(LINK_TABLE)
        .where(LINK_ID_CLAUSE, parseInt(linkId))
        .execute();
  }

  private LinkDetail fetchLink(final ResultSet rs,
      final Map<Integer, CategoryDetail> categoriesByLinkId) throws SQLException {
    final LinkDetail link = new LinkDetail();
    final int linkId = rs.getInt(LINK_ID);
    link.setLinkId(linkId);
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
    link.setCategory(categoriesByLinkId.getOrDefault(linkId, null));
    return link;
  }

  private static JdbcSqlQuery setupSaveQuery(final JdbcSqlQuery saveQuery, final LinkDetail link,
      final boolean isInsert) {
    if (isInsert) {
      saveQuery.withSaveParam(LINK_ID, link.getLinkId(), true);
    }
    final String name = StringUtil.truncate(link.getName(), 255);
    final String description = StringUtil.truncate(link.getDescription(), 255);
    final String url = StringUtil.truncate(link.getUrl(), 255);
    saveQuery
        .withSaveParam("name", name, isInsert)
        .withSaveParam("description", description, isInsert)
        .withSaveParam("url", url, isInsert)
        .withSaveParam("visible", link.isVisible() ? 1 : 0, isInsert)
        .withSaveParam("popup", link.isPopup() ? 1 : 0, isInsert)
        .withSaveParam("userId", link.getUserId(), isInsert)
        .withSaveParam("instanceId", link.getInstanceId(), isInsert)
        .withSaveParam("objectId", link.getObjectId(), isInsert);
    if (link.hasPosition()) {
      saveQuery.withSaveParam("position", link.getPosition(), isInsert);
    }
    if (!isInsert) {
      saveQuery.where(LINK_ID_CLAUSE, link.getLinkId());
    }
    return saveQuery;
  }
}
