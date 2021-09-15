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
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.mylinks.model.LinkDetail;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.silverpeas.core.util.MapUtil.putAddList;

@Repository
public class LinkCategoryDAO {

  private static final String LINK_CATEGORY_TABLE = "SB_MyLinks_LinkCat";
  private static final String LINK_ID = "linkId";
  private static final String CAT_ID = "catId";
  private static final String LINK_ID_CLAUSE = "linkId = ?";
  private static final String CAT_ID_CLAUSE = "catId = ?";

  @Inject
  private CategoryDAO categoryDAO;

  protected static LinkCategoryDAO get() {
    return ServiceProvider.getService(LinkCategoryDAO.class);
  }

  /**
   * Hide constructor of utility class
   */
  protected LinkCategoryDAO() {
  }

  /**
   * Deletes all links about the component instance represented by the given identifier.
   * @param componentInstanceId the identifier of the component instance for which the resources
   * must be deleted.
   * @throws SQLException on SQL problem
   */
  protected void deleteComponentInstanceData(String componentInstanceId)
      throws SQLException {
    JdbcSqlQuery.executeBySplittingOn(
        LinkDAO.get().getLinkIdsByComponentInstance(componentInstanceId), (linkIdBatch, ignored) ->
          JdbcSqlQuery.createDeleteFor(LINK_CATEGORY_TABLE)
              .where(LINK_ID).in(linkIdBatch)
              .execute());
  }

  /**
   * Deletes all couples of link / category of a user.
   * @param userId the identifier of the user for which data must be deleted.
   * @throws SQLException on SQL problem
   */
  protected void deleteUserData(String userId) throws SQLException {
    JdbcSqlQuery.executeBySplittingOn(categoryDAO.getAllCategoriesByUser(userId)
            .stream()
            .map(CategoryDetail::getId)
            .collect(toList()), (catIdBatch, ignored) ->
        JdbcSqlQuery.createDeleteFor(LINK_CATEGORY_TABLE)
            .where(CAT_ID).in(catIdBatch)
            .execute());
  }

  /**
   * Retrieve all couple of link / category of given user.
   * @param userId the user identifier.
   * @return couples of link / category indexed by link id.
   * @throws SQLException on SQL problem
   */
  protected Map<Integer, CategoryDetail> getAllCategoriesByLinkOfUser(String userId)
      throws SQLException {
    final Map<Integer, CategoryDetail> categoriesById = categoryDAO.getAllCategoriesByUser(userId)
        .stream()
        .collect(toMap(CategoryDetail::getId, c -> c));
    return JdbcSqlQuery.executeBySplittingOn(categoriesById.keySet(), (catIdBatch, result) ->
          JdbcSqlQuery.createSelect("*")
              .from(LINK_CATEGORY_TABLE)
              .where(CAT_ID).in(catIdBatch)
              .execute(rs -> {
                putAddList(result, rs.getInt(CAT_ID), rs.getInt(LINK_ID));
                return null;
              }))
        .entrySet()
        .stream()
        .flatMap(e -> e.getValue()
            .stream()
            .map(l -> Pair.of((Integer) l, categoriesById.get(e.getKey()))))
        .collect(toMap(Pair::getFirst, Pair::getSecond));
  }

  /**
   * Retrieve all couple of link / category of given link.
   * @param linkId the link identifier
   * @return couples of link / category
   * @throws SQLException on SQL problem
   */
  protected Map<Integer, CategoryDetail> getAllCategoriesByLink(int linkId)
      throws SQLException {
    final List<Integer> categoryIds = JdbcSqlQuery.createSelect(CAT_ID)
        .from(LINK_CATEGORY_TABLE)
        .where(LINK_ID_CLAUSE, linkId)
        .execute(rs -> rs.getInt(1));
    return categoryDAO.getCategories(categoryIds)
        .stream()
        .collect(toMap(c -> linkId, c -> c));
  }

  /**
   * Save a couple of link / category.
   * <p>
   *   If {@link LinkDetail#getCategory()} returns null value, then the deletion is also handled.
   * </p>
   * @param link link a link.
   * @throws SQLException on SQL problem.
   */
  protected void saveByLink(LinkDetail link) throws SQLException {
    final Integer previousCat = JdbcSqlQuery.createSelect(CAT_ID)
        .from(LINK_CATEGORY_TABLE)
        .where(LINK_ID_CLAUSE, link.getLinkId())
        .executeUnique(rs -> rs.getInt(1));
    if (link.getCategory() == null) {
      if (previousCat != null) {
        deleteByLink(link.getLinkId());
      }
    } else if (previousCat == null || !previousCat.equals(link.getCategory().getId())) {
      if (!link.getUserId().equals(link.getCategory().getUserId())) {
        throw new IllegalArgumentException("user id mismatches between link and category");
      }
      final JdbcSqlQuery saveQuery;
      final boolean isInsert = previousCat == null;
      if (isInsert) {
        saveQuery = JdbcSqlQuery.createInsertFor(LINK_CATEGORY_TABLE);
        saveQuery.addSaveParam(LINK_ID, link.getLinkId(), true);
      } else {
        saveQuery = JdbcSqlQuery.createUpdateFor(LINK_CATEGORY_TABLE);
      }
      saveQuery.addSaveParam(CAT_ID, link.getCategory().getId(), isInsert);
      if (!isInsert) {
        saveQuery.where(LINK_ID_CLAUSE, link.getLinkId());
      }
      saveQuery.execute();
    }
  }

  /**
   * Remove couples of link / category.
   * @param linkId the link identifier from which to remove couples.
   * @throws SQLException on SQL problem
   */
  protected void deleteByLink(int linkId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(LINK_CATEGORY_TABLE)
        .where(LINK_ID_CLAUSE, linkId)
        .execute();
  }

  /**
   * Remove couples of link / category.
   * @param catId the identifier of the category to remove from couples.
   * @throws SQLException on SQL problem
   */
  protected void deleteByCategory(int catId) throws SQLException {
    JdbcSqlQuery.createDeleteFor(LINK_CATEGORY_TABLE)
        .where(CAT_ID_CLAUSE, catId)
        .execute();
  }
}
