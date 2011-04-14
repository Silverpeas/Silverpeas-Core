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

package com.stratelia.webactiv.searchEngine.control.ejb;

import java.rmi.RemoteException;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.searchEngine.model.DidYouMeanSearcher;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.ParseException;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.searchEngine.model.SearchCompletion;
import com.stratelia.webactiv.searchEngine.model.WAIndexSearcher;
import com.stratelia.webactiv.searchEngine.model.ParseRuntimeException;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

/**
 * A SearchEngineEJB search the web'activ index and give access to the retrieved index entries.
 */
public class SearchEngineEJB implements SessionBean, SearchEngineBmBusinessSkeleton {
  /**
   * array which contains the spelling words.
   */
  private String[] spellingWords = null;

  ResourceLocator pdcSettings = new ResourceLocator(
      "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings", "fr");

  /**
   * Create a SearchEngineBm. The results set is initialized empty.
   */
  public void ejbCreate() throws CreateException, RemoteException {
    results = new MatchingIndexEntry[0];
  }

  /**
   * Search the index for the required documents.
   */
  public void search(QueryDescription query) throws RemoteException {
      try{
          results = new WAIndexSearcher().search(query);
        // spelling word functionality is triggered by a threshold defined in pdcPeasSettings.properties
        // (wordSpellingMinScore).
        if (SilverpeasSettings.readBoolean(pdcSettings, "enableWordSpelling", false) &&
            isSpellingNeeded()) {
          DidYouMeanSearcher searcher = new DidYouMeanSearcher();
          spellingWords = searcher.suggest(query);
        }
      }catch(ParseException pe){
          throw new ParseRuntimeException("SearchEngineEJB.search",
                  SilverpeasRuntimeException.ERROR, "searchEngine.EXP_PARSE_EXCEPTION", pe);
      }
  }

  /**
   * Return the count of matching document retrived by the last search. Return 0 if called before
   * the fisrt search.
   */
  public int getResultLength() throws RemoteException {
    return results.length;
  }

  /**
   * Return the index entry at the given position i (counted from 0) from all the entries retrived
   * by the last search. Return null if the given position i is out of bounds.
   */
  public MatchingIndexEntry get(int i) throws RemoteException {
    if (0 <= i && i < results.length)
      return results[i];
    else
      return null;
  }

  /**
   * Return the entries comprised between the position min upto the position max, from all the
   * entries retrived by the last search. The positions min and max are counted from 0 and included
   * in the range. If min or max is out of bounds : the range is resize to fit the results set.
   */
  public MatchingIndexEntry[] getRange(int min, int max) throws RemoteException {
    if (min < 0)
      min = 0;
    if (max >= results.length)
      max = results.length - 1;
    if (min <= max) {
      MatchingIndexEntry[] extraction = new MatchingIndexEntry[max - min + 1];
      for (int i = 0; i < max - min + 1; i++) {
        extraction[i] = results[min + i];
      }
      return extraction;
    } else
      return new MatchingIndexEntry[0];
  }

  /**
   * The no parameters constructor is required for an EJB.
   */
  public SearchEngineEJB() {
  }

  /**
   * The last results set is released.
   */
  public void ejbRemove() {
    results = null;
  }

  /**
   * The session context is useless.
   */
  public void setSessionContext(SessionContext sc) {
  }

  /**
   * There is no ressources to be released.
   */
  public void ejbPassivate() {
  }

  /**
   * There is no ressources to be restored.
   */
  public void ejbActivate() {
  }

  /**
   * The results of the last search.
   */
  private MatchingIndexEntry[] results = null;

  /**
   * check if the results score is low enough to suggest spelling words
   * @return true if the max results score is under the defined threshold
   */
  private boolean isSpellingNeeded() {
    float minScore = SilverpeasSettings.readFloat(pdcSettings, "wordSpellingMinScore", 0.5f);
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
  public Set<String> suggestKeywords(String keywordFragment) throws RemoteException {

    SearchCompletion completion = new SearchCompletion();
    return completion.getSuggestions(keywordFragment);
  }

  /**
   * gets suggestions or spelling words if a search doesn't return satisfying result. A minimal
   * score trigger the suggestions search (0.5 by default)
   * @return array that contains suggestions.
   */
  public String[] getSpellingWords() throws RemoteException {
    return spellingWords;
  }

  /**
   * @param spellingWords the spellingWords to set
   */
  public void setSpellingWords(String[] spellingWords) {
    this.spellingWords = spellingWords;
  }
}
