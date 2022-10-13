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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.mylinks.dao;

import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.mylinks.model.CategoryDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.StringUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.empty;

@Repository
public class CategoryDAO {

  private static final String CATEGORY_TABLE = "SB_MyLinks_Cat";
  private static final String CAT_ID = "catId";
  private static final String CAT_ID_CLAUSE = "catId = ?";
  private static final String USER_ID_CLAUSE = "userId = ?";

  /**
   * Hide constructor of utility class
   */
  protected CategoryDAO() {
  }

  /**
   * Deletes all categories created by a user.
   * @param userId the identifier of the user for which the resources must be deleted.
   * @throws SQLException on SQL problem
   */
  public void deleteUserData(String userId) throws SQLException {
    LinkCategoryDAO.get().deleteUserData(userId);
    JdbcSqlQuery.createDeleteFor(CATEGORY_TABLE)
        .where(USER_ID_CLAUSE, userId)
        .execute();
  }

  /**
   * Retrieve all user categories.
   * @param userId the user identifier
   * @return categories of user links
   * @throws SQLException on SQL problem
   */
  public List<CategoryDetail> getAllCategoriesByUser(String userId)
      throws SQLException {
    return JdbcSqlQuery.createSelect("*")
        .from(CATEGORY_TABLE)
        .where(USER_ID_CLAUSE, userId)
        .execute(CategoryDAO::fetchCategory);
  }

  /**
   * Retrieve category from its identifier
   * @param id the category identifier
   * @return the category detail
   * @throws SQLException on SQL problem
   */
  public CategoryDetail getCategory(int id) throws SQLException {
    return JdbcSqlQuery.unique(getCategories(singleton(id)));
  }

  /**
   * Retrieve category from its identifier
   * @param ids the category identifiers
   * @return the category detail
   * @throws SQLException on SQL problem
   */
  public List<CategoryDetail> getCategories(Collection<Integer> ids) throws SQLException {
    final Mutable<Stream<CategoryDetail>> categories = Mutable.of(empty());
    JdbcSqlQuery.executeBySplittingOn(ids, (idBatch, ignore) -> categories.set(
        concat(categories.get(), JdbcSqlQuery.createSelect("*")
            .from(CATEGORY_TABLE)
            .where(CAT_ID).in(idBatch)
            .execute(CategoryDAO::fetchCategory)
            .stream()))
    );
    return categories.get().collect(toList());
  }

  /**
   * Create new category
   * @param category category detail to create
   * @return new category instance
   * @throws SQLException on SQL problem
   */
  public CategoryDetail create(CategoryDetail category) throws SQLException {
    final CategoryDetail categoryToPersist = new CategoryDetail(category);
    categoryToPersist.setId(DBUtil.getNextId(CATEGORY_TABLE, CAT_ID));
    categoryToPersist.setHasPosition(false);
    final JdbcSqlQuery insertQuery = JdbcSqlQuery.createInsertFor(CATEGORY_TABLE);
    setupSaveQuery(insertQuery, categoryToPersist, true).execute();
    return categoryToPersist;
  }

  /**
   * Update a category
   * @param category category detail to update
   * @return updated category instance
   * @throws SQLException on SQL problem
   */
  public CategoryDetail update(CategoryDetail category) throws SQLException {
    final CategoryDetail categoryToUpdate = new CategoryDetail(category);
    final JdbcSqlQuery updateQuery = JdbcSqlQuery.createUpdateFor(CATEGORY_TABLE);
    setupSaveQuery(updateQuery, categoryToUpdate, false).execute();
    return categoryToUpdate;
  }

  /**
   * Remove a category
   * @param id the category identifier to remove
   * @throws SQLException on SQL problem
   */
  public void deleteCategory(int id) throws SQLException {
    LinkCategoryDAO.get().deleteByCategory(id);
    JdbcSqlQuery.createDeleteFor(CATEGORY_TABLE)
        .where(CAT_ID_CLAUSE, id)
        .execute();
  }

  private static CategoryDetail fetchCategory(final ResultSet rs) throws SQLException {
    final CategoryDetail category = new CategoryDetail();
    category.setId(rs.getInt(CAT_ID));
    category.setPosition(rs.getInt("position"));
    category.setHasPosition(!rs.wasNull());
    category.setName(rs.getString("name"));
    category.setDescription(rs.getString("description"));
    category.setUserId(rs.getString("userId"));
    return category;
  }

  private static JdbcSqlQuery setupSaveQuery(final JdbcSqlQuery saveQuery,
      final CategoryDetail category, final boolean isInsert) {
    if (isInsert) {
      saveQuery.addSaveParam(CAT_ID, category.getId(), true);
    }
    final String name = StringUtil.truncate(category.getName(), 255);
    final String description = StringUtil.truncate(category.getDescription(), 255);
    saveQuery
        .addSaveParam("name", name, isInsert)
        .addSaveParam("description", description, isInsert)
        .addSaveParam("userId", category.getUserId(), isInsert);
    if (category.hasPosition()) {
      saveQuery.addSaveParam("position", category.getPosition(), isInsert);
    }
    if (!isInsert) {
      saveQuery.where(CAT_ID_CLAUSE, category.getId());
    }
    return saveQuery;
  }
}
