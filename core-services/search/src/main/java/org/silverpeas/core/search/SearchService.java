/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.search;

import org.silverpeas.core.index.search.PlainSearchResult;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.SearchQueryProcessor;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchEngineException;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.index.search.qualifiers.TaxonomySearch;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.time.DurationFormatUtils.formatDurationHMS;
import static org.silverpeas.core.util.CollectionUtil.isNotEmpty;

@Singleton
public class SearchService {

  @Inject
  @TaxonomySearch
  SearchQueryProcessor taxonomySearchProcessor;

  public static SearchService get() {
    return ServiceProvider.getService(SearchService.class);
  }

  public List<SearchResult> search(QueryDescription queryDescription)
      throws SearchEngineException {
    final long startTime = System.currentTimeMillis();
    try {
      final boolean taxonomySearch = queryDescription.isTaxonomyUsed();
      final boolean fullTextSearch = !queryDescription.isEmpty();
      List<SearchResult> fullTextResults = Collections.emptyList();
      List<SearchResult> taxonomyResults;
      if (taxonomySearch) {
        taxonomyResults = taxonomySearchProcessor.process(queryDescription, null);
      } else {
        taxonomyResults = Collections.emptyList();
      }
      if (fullTextSearch) {
        if (!taxonomyResults.isEmpty()) {
          // restrains full-text search to components of taxonomy search
          Set<String> componentIds = extractComponentIds(taxonomyResults);
          queryDescription.getWhereToSearch().clear();
          queryDescription.getWhereToSearch().addAll(componentIds);
        }
        fullTextResults = searchOnIndexes(queryDescription);
      }
      if (fullTextSearch && taxonomySearch) {
        // mixed search : retains only common results
        return getResultsFromMixedSearch(taxonomyResults, fullTextResults);
      } else if (fullTextSearch) {
        return fullTextResults;
      } else {
        return taxonomyResults;
      }
    } finally {
      final long endTime = System.currentTimeMillis();
      SilverLogger.getLogger(this).debug(() -> MessageFormat
          .format(" search service duration of {0}", formatDurationHMS(endTime - startTime)));
    }
  }

  /**
   * Returns common results. Results returned are from fullTextResults list because they are most
   * complete than taxonomyResults ones.
   * @param taxonomyResults Results from taxonomy search
   * @param fullTextResults Results from fulltext search
   * @return the common results from both lists
   */
  private List<SearchResult> getResultsFromMixedSearch(List<SearchResult> taxonomyResults,
      List<SearchResult> fullTextResults) {
    final List<SearchResult> results;
    if (isNotEmpty(fullTextResults)) {
      final Map<SearchResult, SearchResult> indexedSearchResult = new HashMap<>(fullTextResults.size());
      fullTextResults.forEach(f -> indexedSearchResult.put(f, f));
      results = taxonomyResults.stream()
          .map(indexedSearchResult::get)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    } else {
      results = new ArrayList<>();
    }
    return results;
  }

  private Set<String> extractComponentIds(List<SearchResult> results) {
    Set<String> distinctComponentIds = new HashSet<>();
    for (SearchResult result : results) {
      distinctComponentIds.add(result.getInstanceId());
    }
    return distinctComponentIds;
  }

  private List<SearchResult> searchOnIndexes(QueryDescription fullTextRequest)
      throws SearchEngineException {
    try {
      PlainSearchResult searchResult =
          SearchEngineProvider.getSearchEngine().search(fullTextRequest);
      List<SearchResult> results = new ArrayList<>();
      for (MatchingIndexEntry mie : searchResult.getEntries()) {
        results.add(SearchResult.fromIndexEntry(mie));
      }
      return results;
    } catch (Exception e) {
      throw new SearchEngineException(e);
    }
  }

}