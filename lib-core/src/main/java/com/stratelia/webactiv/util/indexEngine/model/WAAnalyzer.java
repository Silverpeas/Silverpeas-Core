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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.indexEngine.model;

import com.silverpeas.util.StringUtil;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.indexEngine.analysis.ElisionFilter;
import com.stratelia.webactiv.util.indexEngine.analysis.SilverTokenizer;

/**
 * Extends lucene Analyzer : prunes from a tokens stream all the meaningless words and prunes all
 * the special characters.
 */
public final class WAAnalyzer extends Analyzer {

  /**
   * Returns the analyzer to be used with texts of the given language. The analyzers are cached.
   * @param language
   * @return
   */
  static public Analyzer getAnalyzer(String language) {
    WAAnalyzer analyzer = languageMap.get(language);

    if (analyzer == null) {
      analyzer = new WAAnalyzer(language);
      languageMap.put(analyzer.getLanguage(), analyzer);
    }

    return analyzer;
  }

  /**
   * Returns a tokens stream built on top of the given reader.
   * @param reader
   * @return
   */
  public TokenStream tokenStream(Reader reader) {
    TokenStream result = new SilverTokenizer(reader);
    result = new StandardFilter(result); // remove 's and . from token
    result = new LowerCaseFilter(result);
    // remove some unexplicit terms according to the language
    result = new StopFilter(result, stopWords);
    // remove [cdjlmnst-qu]' from token
    result = new ElisionFilter(result);
    // remove accents
    result = new ISOLatin1AccentFilter(result);
    // ignoring singular/plural, male/female and conjugated forms
    result = new SnowballFilter(result, stemmer);  
    return result;
  }

  @Override
  public TokenStream tokenStream(String arg0, Reader reader) {
    return tokenStream(reader);
  }

  /**
   * The constructor is private : use @link #getAnalyzer().
   */
  private WAAnalyzer(String language) {
    if (!StringUtil.isDefined(language) || language.length() != 2) {
      language = settings.getString("analyzer.language.default", "fr");
    }
    this.language = language; 
    stopWords = getStopWords();
    stemmer = getStemmer();
  }

  /**
   * Returns an array of words which are not usually usefull for searching.
   */
  private String[] getStopWords() {
    List<String> wordList = new ArrayList<String>();
    try {
      ResourceLocator resource = new ResourceLocator(
          "com.stratelia.webactiv.util.indexEngine.StopWords", language);

      Enumeration<String> stopWord = resource.getKeys();

      while (stopWord.hasMoreElements()) {
        wordList.add(stopWord.nextElement());
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexEngine", "WAAnalyzer", "indexEngine.MSG_MISSING_STOPWORDS_DEFINITION");
      return new String[0];
    }
    return wordList.toArray(new String[wordList.size()]);
  }
  
  private String getStemmer() {
    return settings.getString("snowball.stemmer."+language, "French");
  }
  
  public String getLanguage() {
    return language;
  }

  static private final Map<String, WAAnalyzer> languageMap = new HashMap<String, WAAnalyzer>();
  static private final ResourceLocator settings = new ResourceLocator(
      "com.stratelia.webactiv.util.indexEngine.IndexEngine", "");
  /**
   * The words which are usually not useful for searching.
   */
  private String[] stopWords = null;
  private String stemmer = null;
  private String language = null;
  
}
