/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.core.index.search;

import org.silverpeas.core.index.search.model.DidYouMeanSearcher;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchCompletion;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A SimpleSearchEngine search silverpeas indexes index and give access to the retrieved index
 * entries.
 */
@Singleton
public class SimpleSearchEngine implements SearchEngine {

  @Inject
  private DidYouMeanSearcher didYouMeanSearcher;
  @Inject
  private IndexSearcher indexSearcher;

  SettingBundle pdcSettings =
      ResourceLocator.getSettingBundle("org.silverpeas.pdcPeas.settings.pdcPeasSettings");
  private final float minScore = pdcSettings.getFloat("wordSpellingMinScore", 0.5f);
  private final boolean enableWordSpelling = pdcSettings.getBoolean("enableWordSpelling", false);

  /**
   * Search the index for the required documents.
   * @param query the search query.
   * @return the results.
   */
  @Override
  public PlainSearchResult search(QueryDescription query) throws ParseException {
    try {
      List<MatchingIndexEntry> results = Arrays.asList(indexSearcher.search(query));
      @SuppressWarnings("unchecked") Set<String> spellingWords = Collections.EMPTY_SET;
      if (enableWordSpelling && isSpellingNeeded(results)) {
        String[] suggestions = didYouMeanSearcher.suggest(query);
        if (suggestions != null && suggestions.length > 0) {
          spellingWords = new HashSet<>(suggestions.length);
          Collections.addAll(spellingWords, suggestions);
        }
      }
      return new PlainSearchResult(new ArrayList<>(spellingWords), results);
    } catch (IOException ioex) {
      throw new ParseException("SimpleSearchEngine.search", ioex);
    }
  }

  /**
   * Hide constructor.
   */
  private SimpleSearchEngine() {
  }

  /**
   * check if the results score is low enough to suggest spelling words
   * @return true if the max results score is under the defined threshold
   */
  private boolean isSpellingNeeded(List<MatchingIndexEntry> results) {
    for (MatchingIndexEntry match : results) {
      if (minScore < match.getScore()) {
        return false;
      }
    }
    return true;
  }

  /**
   * gets a list of suggestion from a partial String
   * @param keywordFragment string to execute the search
   * @return a set of result sorted by alphabetic order
   */
  @Override
  public Set<String> suggestKeywords(String keywordFragment) {
    SearchCompletion completion = new SearchCompletion();
    return completion.getSuggestions(keywordFragment);
  }
}
