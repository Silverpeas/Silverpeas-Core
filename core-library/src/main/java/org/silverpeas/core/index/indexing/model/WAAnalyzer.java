/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.index.indexing.model;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.fr.FrenchAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.ElisionFilter;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

/**
 * Extends lucene Analyzer : prunes from a tokens stream all the meaningless words and prunes all
 * the special characters.
 */
public final class WAAnalyzer extends Analyzer {

  private static final int LANGUAGE_CODE_LENGTH = 2;
  private static final Map<String, WAAnalyzer> languageMap = new ConcurrentHashMap<>();
  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.index.indexing.IndexEngine");
  /**
   * The words which are usually not useful for searching.
   */
  private String stemmer = null;
  private boolean snowballUsed = false;
  private String language = null;

  /**
   * The constructor is private
   */
  private WAAnalyzer(String lang) {
    language = lang;
    stemmer = getStemmer();
    snowballUsed = settings.getBoolean("snowball.active", false);
  }

  /**
   * Returns the analyzer to be used with texts of the given language. The analyzers are cached.
   *
   * @param language
   * @return
   */
  public static Analyzer getAnalyzer(String language) {
    return ofNullable(languageMap.get(language)).orElseGet(() -> {
      final String computedLanguage = ofNullable(language)
          .filter(l -> l.length() == LANGUAGE_CODE_LENGTH)
          .orElseGet(() -> settings.getString("analyzer.language.default", I18n.get().getDefaultLanguage()));
      final WAAnalyzer analyzer = languageMap.computeIfAbsent(computedLanguage, WAAnalyzer::new);
      return languageMap.computeIfAbsent(language, l -> analyzer);
    });
  }

  /**
   * Returns a tokens stream built on top of the given reader.
   *
   */
  @Override
  protected TokenStreamComponents createComponents(final String s) {
    final Tokenizer source = new StandardTokenizer();
    // remove 's and . from token
    TokenStream result = new StandardFilter(source);
    result = new LowerCaseFilter(result);
    // remove some unexplicit terms
    result = new StopFilter(result, FrenchAnalyzer.getDefaultStopSet());
    // remove [cdjlmnst-qu]' from token
    result = new ElisionFilter(result, FrenchAnalyzer.DEFAULT_ARTICLES);
    if (snowballUsed) {
      // Important! Strings given to Snowball filter must contains accents
      // so accents must be removed after stemmer have done the job
      // ignoring singular/plural, male/female and conjugated forms
      result = new SnowballFilter(result, stemmer);
    }
    // remove accents
    result = new ASCIIFoldingFilter(result);
    return new TokenStreamComponents(source, result);
  }

  private String getStemmer() {
    return settings.getString("snowball.stemmer." + language, "French");
  }

  public String getLanguage() {
    return language;
  }

}
