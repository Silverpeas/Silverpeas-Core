/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user;

import org.silverpeas.core.index.search.model.QueryDescription;

/**
 * A builder of queries on users that are indexed with the Silverpeas indexation engine.
 *
 * The builder provides several methods to specify the values of the fields the users to query must
 * have. Either the exact value can be passed or just a pattern the users must satisfy in order to
 * be sent back. Currently, only the patterns supported by the Lucene search engine are supported;
 * for example the more common pattern is the following: TEXT* where the star is used as a widcard
 * character and meaning starting by the text TEXT.
 */
public class IndexedUserQueryBuilder {

  private StringBuilder query = new StringBuilder();
  private String userId = null;

  public static IndexedUserQueryBuilder queriedBy(String userId) {
    return new IndexedUserQueryBuilder(userId);
  }

  public IndexedUserQueryBuilder or() {
    query.append(" OR ");
    return this;
  }

  public IndexedUserQueryBuilder and() {
    query.append(" AND ");
    return this;
  }

  /**
   * The query on the first name of a user.
   * @param name
   * @return
   */
  public IndexedUserQueryBuilder firstName(String name) {
    query.append("firstName:").append(name);
    return this;
  }

  public IndexedUserQueryBuilder lastName(String name) {
    query.append("lastName:").append(name);
    return this;
  }

  public QueryDescription build() {
    QueryDescription queryDescription = new QueryDescription(query.toString());
    queryDescription.addComponent(UserIndexation.COMPONENT_ID);
    if (userId != null) {
      queryDescription.setSearchingUser(userId);
    }
    return queryDescription;
  }

  protected IndexedUserQueryBuilder(String userId) {
    this.userId = userId;
  }
}
