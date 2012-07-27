/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.search;

import com.stratelia.webactiv.searchEngine.model.DidYouMeanSearcher;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.ParseException;
import com.stratelia.webactiv.searchEngine.model.ParseRuntimeException;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.searchEngine.model.SearchCompletion;
import com.stratelia.webactiv.searchEngine.model.WAIndexSearcher;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.silverpeas.annotation.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A SimpleSearchEngine search silverpeas indexes index and give access to the retrieved index entries.
 */
@Service
public class SimpleSearchEngine implements SearchEngine{

  ResourceLocator pdcSettings = new ResourceLocator(
      "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "fr");

  private final float minScore = pdcSettings.getFloat("wordSpellingMinScore", 0.5f);
  private final boolean enableWordSpelling = pdcSettings.getBoolean("enableWordSpelling", false);

  /**
   * Search the index for the required documents.
   * @param query the search query.
   * @return  the results.
   */
  @Override
  public PlainSearchResult search(QueryDescription query) {
    try {
      List<MatchingIndexEntry> results = Arrays.asList(new WAIndexSearcher().search(query));
      // spelling word functionality is triggered by a threshold defined in pdcPeasSettings.properties
      // (wordSpellingMinScore).
      @SuppressWarnings("unchecked")
      Set<String> spellingWords = Collections.EMPTY_SET;
      if (enableWordSpelling && isSpellingNeeded(results)) {
        DidYouMeanSearcher searcher = new DidYouMeanSearcher();
        
        String[] suggestions = searcher.suggest(query);
        if(suggestions != null && suggestions.length > 0) {
          spellingWords = new HashSet<String>(suggestions.length);
          Collections.addAll(spellingWords, suggestions);
        }      
      }
      return new PlainSearchResult(new ArrayList<String>(spellingWords), results);
    } catch (ParseException pe) {
      throw new ParseRuntimeException("SimpleSearchEngine.search",
          SilverpeasRuntimeException.ERROR, "searchEngine.EXP_PARSE_EXCEPTION", pe);
    }
  }


  /**
   * The no parameters constructor is required for an EJB.
   */
  public SimpleSearchEngine() {
  }

  /**
   * check if the results score is low enough to suggest spelling words
   *
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
   *
   * @param keywordFragment string to execute the search
   * @return a set of result sorted by alphabetic order
   */
  public Set<String> suggestKeywords(String keywordFragment)  {
    SearchCompletion completion = new SearchCompletion();
    return completion.getSuggestions(keywordFragment);
  }
}
