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

package com.stratelia.webactiv.searchEngine.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spell.SpellCheckerImpl;
import org.apache.lucene.store.FSDirectory;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer;
import com.stratelia.webactiv.util.indexEngine.model.IndexManager;

/**
 * 
 *
 */
public class DidYouMeanSearcher {

  private ArrayList<SpellCheckerImpl> spellCheckers = null;

  private String query = null;

  /**
   * 
   */
  public DidYouMeanSearcher() {

    spellCheckers = new ArrayList<SpellCheckerImpl>();

  }

  /**
   * @param queryDescription
   * @return
   * @throws com.stratelia.webactiv.searchEngine.model.ParseException
   * @throws ParseException
   */
  public String[] suggest(QueryDescription queryDescription)
      throws com.stratelia.webactiv.searchEngine.model.ParseException {

    String[] suggestions = null;
    // The variable field is only used to parse the query String and to obtain the words that will
    // be used for the search
    final String field = "content";
    if (queryDescription != null && StringUtils.isNotEmpty(queryDescription.getQuery())) {

      // parses the query string to prepare the search
      Analyzer analyzer = new IndexManager().getAnalyzer(queryDescription.getRequestedLanguage());
      QueryParser queryParser = new QueryParser(field, analyzer);

      Query parsedQuery = null;
      try {
        parsedQuery = queryParser.parse(queryDescription.getQuery());
      } catch (ParseException exception) {
        throw new com.stratelia.webactiv.searchEngine.model.ParseException(
            "DidYouMeanSearcher", exception);
      }

      // splits the query to realize a separated search with each word
      String query = parsedQuery.toString(field);
      this.query = query;
      StringTokenizer tokens = new StringTokenizer(query);

      // gets spelling index paths
      WAIndexSearcher waIndexSearcher = new WAIndexSearcher();
      Set<String> spellIndexPaths =
          waIndexSearcher.getIndexPathSet(queryDescription.getSpaceComponentPairSet());

      try {
        while (tokens.hasMoreTokens()) {
          SpellCheckerImpl spellCheck =
              new SpellCheckerImpl(FSDirectory.getDirectory(new File(FileRepositoryManager
              .getIndexUpLoadPath())));
          spellCheckers.add(spellCheck);
          String token = tokens.nextToken().replaceAll("\"", "");
          for (String path : spellIndexPaths) {

            // create a file object with given path
            File file = new File(path + "Spell");

            if (file != null && file.exists()) {

              // create a spellChecker with the file object
              FSDirectory directory = FSDirectory.getDirectory(file);
              spellCheck.setSpellIndex(directory);

              // if the word exist in the dictionary, we stop the current treatment and search the
              // next word
              // because the suggestSimilar method will return the same word than the given word
              if (spellCheck.exist(token)) {
                continue;
              }
              spellCheck.suggestSimilar(token, 1);

            }
          }
        }
      } catch (IOException e) {
        SilverTrace.error("searchEngine", DidYouMeanIndexer.class.toString(),
            "root.EX_LOAD_IO_EXCEPTION", e);
      }

      suggestions = buildFinalResult();

    }
    return suggestions;
  }

  private String[] buildFinalResult() {
    StringBuilder currentSentence = new StringBuilder();
    String currentToken = null;

    StringTokenizer tokens = new StringTokenizer(query);
    int countTokens = tokens.countTokens();
    // building one sentence
    for (SpellCheckerImpl spellCheck : spellCheckers) {
      currentToken = tokens.nextToken();
      String[] suggestWords = spellCheck.getSuggestWords(1);
      // quote and boolean operator managing
      // actions for suggested word
      if (!ArrayUtils.isEmpty(suggestWords)) {
        // beginning
        getPrefixOperator(currentSentence, currentToken, false);
        // word
        currentSentence.append(suggestWords[0]);
        // end
        getSuffixOperator(currentSentence, currentToken, false);
      } else if (countTokens > 1) {
        // actions if hasn't suggested word
        // begin
        getPrefixOperator(currentSentence, currentToken, true);
        // word
        currentSentence.append(currentToken);
        // end
        getSuffixOperator(currentSentence, currentToken, true);
      }

    }

    String[] suggestions =
        { currentSentence.toString().replaceAll("\\+", "").replaceAll("-", "")
        .replaceAll("  ", " ").trim() };

    return suggestions;

  }

  /**
   * @param currentSentence
   * @param currentToken
   */
  private void getSuffixOperator(StringBuilder currentSentence, String currentToken,
      boolean originalString) {
    if (currentToken.endsWith("*")) {
      currentSentence.append("* ");
    } else if (currentToken.endsWith("\"") && !originalString) {
      currentSentence.append("\" ");
    } else {
      currentSentence.append(" ");
    }
  }

  /**
   * @param currentSentence
   * @param currentToken
   */
  private void getPrefixOperator(StringBuilder currentSentence, String currentToken,
      boolean originalString) {
    if (currentToken.startsWith("-")) {
      currentSentence.append(" NOT ");
    } else if (currentToken.startsWith("+")) {
      currentSentence.append(" AND ");
    } else if (currentToken.startsWith("\"") && !originalString) {
      currentSentence.append("\"");
    } else if (currentToken.startsWith("OR")) {
      currentSentence.append(" OR ");
    } else {
      currentSentence.append(" ");
    }
  }

}
