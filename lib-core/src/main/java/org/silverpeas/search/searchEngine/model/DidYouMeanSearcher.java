/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package org.silverpeas.search.searchEngine.model;

import com.silverpeas.util.ArrayUtil;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.silverpeas.search.indexEngine.IndexFileManager;
import org.silverpeas.search.indexEngine.model.DidYouMeanIndexer;
import org.silverpeas.search.indexEngine.model.IndexManager;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * 
 *
 */
public class DidYouMeanSearcher {
  private List<SpellChecker> spellCheckers = null;
  private String query = null;  
  private final File uploadIndexDir;

  /**
   * 
   */
  public DidYouMeanSearcher() {
    spellCheckers = new ArrayList<SpellChecker>();
    uploadIndexDir =  new File(IndexFileManager.getIndexUpLoadPath());
  }
  
  /**
   * @param queryDescription
   * @return
   * @throws org.silverpeas.search.searchEngine.model.ParseException
   * @throws ParseException
   */
  public String[] suggest(QueryDescription queryDescription)
      throws org.silverpeas.search.searchEngine.model.ParseException, IOException {

    String[] suggestions = null;
    // The variable field is only used to parse the query String and to obtain the words that will
    // be used for the search
    final String field = "content";
    if (StringUtil.isDefined(queryDescription.getQuery())) {

      // parses the query string to prepare the search
      Analyzer analyzer = new IndexManager().getAnalyzer(queryDescription.getRequestedLanguage());
      QueryParser queryParser = new QueryParser(Version.LUCENE_36, field, analyzer);

      Query parsedQuery = null;
      try {
        parsedQuery = queryParser.parse(queryDescription.getQuery());
      } catch (ParseException exception) {
        throw new org.silverpeas.search.searchEngine.model.ParseException("DidYouMeanSearcher",
                exception);
      }

      // splits the query to realize a separated search with each word
      this.query = parsedQuery.toString(field);
      StringTokenizer tokens = new StringTokenizer(query);

      // gets spelling index paths
      WAIndexSearcher waIndexSearcher = new WAIndexSearcher();
      Set<String> spellIndexPaths =
          waIndexSearcher.getIndexPathSet(queryDescription.getSpaceComponentPairSet());

      try {
        while (tokens.hasMoreTokens()) {
          SpellChecker spellCheck = new SpellChecker(FSDirectory.open(uploadIndexDir));
          spellCheckers.add(spellCheck);
          String token = tokens.nextToken().replaceAll("\"", "");
          for (String path : spellIndexPaths) {

            // create a file object with given path
            File file = new File(path + "Spell");

            if (file != null && file.exists()) {

              // create a spellChecker with the file object
              FSDirectory directory = FSDirectory.open(file);
              spellCheck.setSpellIndex(directory);

              // if the word exist in the dictionary, we stop the current treatment and search the
              // next word because the suggestSimilar method will return the same word than the given word
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

  private String[] buildFinalResult() throws IOException {
    StringBuilder currentSentence = new StringBuilder();
    StringTokenizer tokens = new StringTokenizer(query);
    int countTokens = tokens.countTokens();
    // building one sentence
    for (SpellChecker spellCheck : spellCheckers) {
      String currentToken = tokens.nextToken();
      String[] suggestWords = spellCheck.suggestSimilar(currentToken, 1);
      // quote and boolean operator managing
      // actions for suggested word
      if (!ArrayUtil.isEmpty(suggestWords)) {
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
