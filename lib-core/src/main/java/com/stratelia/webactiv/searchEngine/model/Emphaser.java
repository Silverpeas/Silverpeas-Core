package com.stratelia.webactiv.searchEngine.model;

import java.util.*;
import java.io.*;
import org.apache.lucene.analysis.*;
import com.stratelia.webactiv.util.indexEngine.model.*;

/**
 * Class used to set emphase arround the matching text in a result's display.
 */
public class Emphaser {

  /**
   * Constructor declaration
   * 
   * 
   * @param beginEmphase
   * @param endEmphase
   * 
   * @see
   */
  public Emphaser(String beginEmphase, String endEmphase) {
    if (beginEmphase != null) {
      this.beginEmphase = beginEmphase;
    }
    if (endEmphase != null) {
      this.endEmphase = endEmphase;
    }
  }

  /**
   * Emphase in a result String all the word matching the query string.
   */
  public String emphaseMatchingResult(String query, String result,
      String language) {
    Set queryWords = getQueryWords(query, language);

    StringBuffer emphasedResult = new StringBuffer();
    int firstUncopied = 0;
    int matchStart;
    int matchEnd;

    Analyzer analyzer = WAAnalyzer.getAnalyzer(language);
    TokenStream ts = analyzer.tokenStream(null, new StringReader(result));
    Token token;

    try {
      while ((token = ts.next()) != null) {
        if (queryWords.contains(token.termText())) {
          matchStart = token.startOffset();
          matchEnd = token.endOffset();
          if (matchStart > firstUncopied) {
            emphasedResult.append(result.substring(firstUncopied, matchStart));
          }
          emphasedResult.append(emphaseWord(result.substring(matchStart,
              matchEnd)));
          firstUncopied = matchEnd;
        }
      }
    } catch (IOException e) { /* ignored */
    }
    if (firstUncopied < result.length()) {
      emphasedResult.append(result.substring(firstUncopied));
    }

    return emphasedResult.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @param query
   * @param language
   * 
   * @return
   * 
   * @see
   */
  private Set getQueryWords(String query, String language) {
    Analyzer analyzer = WAAnalyzer.getAnalyzer(language);
    TokenStream ts = analyzer.tokenStream(null, new StringReader(query));
    Token token;

    Set queryWords = new HashSet();

    try {
      while ((token = ts.next()) != null) {
        queryWords.add(token.termText());
      }
    } catch (IOException e) { /* ignored */
    }
    return queryWords;
  }

  /**
   * Method declaration
   * 
   * 
   * @param word
   * 
   * @return
   * 
   * @see
   */
  public String emphaseWord(String word) {
    return beginEmphase + word + endEmphase;
  }

  static private String beginEmphase = "<em>";
  static private String endEmphase = "</em>";
}
