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
package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.index.indexing.analysis.SilverTokenizer;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.fr.ElisionFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.util.Version;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

/**
 * Extends lucene Analyzer : prunes from a tokens stream all the meaningless words and prunes all
 * the special characters.
 */
public final class WAAnalyzer extends Analyzer {

  /**
   * Returns the analyzer to be used with texts of the given language. The analyzers are cached.
   *
   * @param language
   * @return
   */
  static public Analyzer getAnalyzer(String language) {
    Analyzer analyzer = languageMap.get(language);

    if (analyzer == null) {
      analyzer = new WAAnalyzer(language);
      languageMap.put(language, analyzer);
    }

    return analyzer;
  }

  /**
   * Returns a tokens stream built on top of the given reader.
   *
   * @param reader
   * @return
   */
  public TokenStream tokenStream(Reader reader) {
    TokenStream result = new SilverTokenizer(reader);
    result = new StandardFilter(Version.LUCENE_36, result); // remove 's and . from token
    result = new LowerCaseFilter(Version.LUCENE_36, result);
    result = new StopFilter(Version.LUCENE_36, result, stopWords); // remove some unexplicit terms
    // according to the language
    result = new ElisionFilter(Version.LUCENE_36, result); // remove [cdjlmnst-qu]' from token
    if (snowballUsed) {
      // Important! Strings given to Snowball filter must contains accents
      // so accents must be removed after stemmer have done the job
      // ignoring singular/plural, male/female and conjugated forms
      result = new SnowballFilter(result, stemmer);
    }
    // remove accents
    result = new ASCIIFoldingFilter(result);
    return result;
  }

  @Override
  public TokenStream tokenStream(String arg0, Reader reader) {
    return tokenStream(reader);
  }

  /**
   * The constructor is private
   */
  private WAAnalyzer(String lang) {
    if (!StringUtil.isDefined(lang) || lang.length() != 2) {
      language = settings.getString("analyzer.language.default", "fr");
    } else {
      language = lang;
    }
    getStopWords(language);
    stemmer = getStemmer();
    snowballUsed = settings.getBoolean("snowball.active", false);
  }

  /**
   * Returns an array of words which are not usually usefull for searching.
   */
  private void getStopWords(String language) {
    stopWords = new HashSet<>();
    String currentLanguage = language;
    try {
      if (!StringUtil.isDefined(currentLanguage)) {
        currentLanguage = I18NHelper.defaultLanguage;
      }
      LocalizationBundle resource =
          ResourceLocator.getLocalizationBundle("org.silverpeas.index.indexing.StopWords", currentLanguage);
      stopWords.addAll(resource.keySet());
    } catch (MissingResourceException e) {
      SilverTrace.warn("indexing", "WAAnalyzer", "indexing.MSG_MISSING_STOPWORDS_DEFINITION");
    }
  }

  private String getStemmer() {
    return settings.getString("snowball.stemmer." + language, "French");
  }

  public String getLanguage() {
    return language;
  }
  static private final Map<String, Analyzer> languageMap = new HashMap<String, Analyzer>();
  static private final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.IndexEngine");
  /**
   * The words which are usually not useful for searching.
   */
  private String stemmer = null;
  private boolean snowballUsed = false;
  private String language = null;
  private Set<String> stopWords = null;
}
