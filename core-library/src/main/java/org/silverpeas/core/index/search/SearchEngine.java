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
package org.silverpeas.core.index.search;

import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.security.authorization.ComponentAuthorization;

import java.util.Set;

/**
 * A SearchEngine search the web'activ index and give access to the retrieved index entries.
 */
public interface SearchEngine {

  /**
   * Search the index for the required documents.
   * <p>
   * If {@link QueryDescription#setAdminScope(boolean)} as been called with a true value (it MUST
   * be only set by administration features), no filtering is performed on index search result.
   * </p>
   * <p>
   * In other context than the one of administration scope, a filtering is performed on the index
   * search result. The filtering is using {@link ComponentAuthorization} API which permits each
   * component to apply theirs access security rules.
   * </p>
   * @param query the search query.
   * @return the results.
   */
  PlainSearchResult search(QueryDescription query) throws ParseException;

  /**
   * gets a list of suggestion from a partial String
   * @param keywordFragment string to execute the search
   * @return a set of result sorted by alphabetic order
   */
  Set<String> suggestKeywords(String keywordFragment);
}
