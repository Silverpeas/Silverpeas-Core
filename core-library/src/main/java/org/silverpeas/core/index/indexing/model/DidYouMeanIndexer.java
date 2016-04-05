/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.index.indexing.model;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.silvertrace.SilverTrace;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 * This class allows to manage the specific index of "did you mean" functionality. <br/>
 * creates new entry into index <br/>
 * clear all the entries from index
 */
public class DidYouMeanIndexer {

  /**
   * Suffix used to differentiate the standard index path's and the spelling index path's
   */
  public final static String SUFFIX_SPELLING_INDEX_PATH = "Spell";

  /**
   * default constructor is private this class contains only static method
   */
  private DidYouMeanIndexer() {
  }

  /**
   * creates or updates a spelling index. The spelling index is created or updated from an existing
   * index. The spelling index is used to suggest words when an user executes a query that returns
   * unsatisfactory results. if a spelling index already exists, only the new words contained in the
   * index source will be added. otherwise a new index will be created
   * @param field name of the field of the index source that will be used to feed the spelling index
   * @param originalIndexDirectory represents the source index path
   * @param spellIndexDirectory represents the spelling index path
   */
  public static void createSpellIndex(String field, String originalIndexDirectory,
      String spellIndexDirectory) {
    // stop the process if method parameters is null or empty
    if (!StringUtil.isDefined(field) || !StringUtil.isDefined(originalIndexDirectory) ||
        !StringUtil.isDefined(spellIndexDirectory)) {
      SilverTrace.error("indexing", DidYouMeanIndexer.class.toString(), "root.EX_INVALID_ARG");
      return;
    }
    // initializes local variable
    IndexReader indexReader = null;

    try {
      // create a file object with given path
      File file = new File(spellIndexDirectory);
      // open original index
      FSDirectory directory = FSDirectory.open(file);
      indexReader = IndexReader.open(FSDirectory.open(new File(originalIndexDirectory)));
      // create a Lucene dictionary with the original index
      Dictionary dictionary = new LuceneDictionary(indexReader, field);
      // index the dictionary into the spelling index
      SpellChecker spellChecker = new SpellChecker(directory);
      IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
              new StandardAnalyzer(Version.LUCENE_36));
      spellChecker.indexDictionary(dictionary, config, true);
      spellChecker.close();
    } catch (CorruptIndexException e) {
      SilverTrace.error("indexing", DidYouMeanIndexer.class.toString(),
          "root.EX_INDEX_FAILED", e);
    } catch (IOException e) {
      SilverTrace.error("indexing", DidYouMeanIndexer.class.toString(),
          "root.EX_LOAD_IO_EXCEPTION", e);
    } finally {
      IOUtils.closeQuietly(indexReader);
    }

  }

  /**
   * creates or updates a spelling index. The spelling index is created or updated from an existing
   * index. The spelling index is used to suggest words when an user executes a query that returns
   * unsatisfactory results. if a spelling index already exists, only the new words contained in the
   * index source will be added. otherwise a new index will be created. <br>
   * This method create an spelling index from the original index path by the adding a suffix to the
   * original path index. The suffix is already the same
   * @see DidYouMeanIndexer
   * @param field name of the field of the index source that will be used to feed the spelling index
   * @param originalIndexDirectory represents the source index path
   */
  public static void createSpellIndex(String field, String originalIndexDirectory) {
    DidYouMeanIndexer.createSpellIndex(field, originalIndexDirectory,
        originalIndexDirectory + SUFFIX_SPELLING_INDEX_PATH);
  }

  /**
   * Clears all the entries from given spelling index
   * @param pathSpellChecker The SpellChecker's path to clear. The path must be a directory path.
   * @return true whether the index have been cleared otherwise false.
   */
  public static boolean clearSpellIndex(String pathSpellChecker) {
    boolean isCleared = false;
    try {
      // create a file object with given path
      File file = new File(pathSpellChecker);
      if (file != null && file.exists()) {
        // create a spellChecker with the file object
        SpellChecker spell = new SpellChecker(FSDirectory.open(file));
        // if index exists, clears his content
        if (spell != null) {
          spell.clearIndex();
          isCleared = true;
        }
      }
    } catch (IOException e) {
      SilverTrace.error("indexing", DidYouMeanIndexer.class.toString(),
          "root.EX_LOAD_IO_EXCEPTION", e);
    }
    return isCleared;
  }

  /**
   * Allows to empty several indexes at once. if the operation fails for an index, it will continue
   * for the other indexes
   * @param paths array of index path. List of index path to empty
   */
  public static void clearSpellIndex(String[] paths) {
    for (String path : paths) {
      clearSpellIndex(path);
    }
  }

  /**
   * Tries to creates or updates a spelling index for all language manage by the application
   * instance. <br>
   * The spelling index is created or updated from an existing index. The spelling index is used to
   * suggest words when an user executes a query that returns unsatisfactory results. if a spelling
   * index already exists, only the new words contained in the index source will be added. otherwise
   * a new index will be created. <br>
   * This method create an spelling index from the original index path by the adding a suffix to the
   * original path index. The suffix is already the same
   * @see DidYouMeanIndexer
   * @param field name of the field of the index source that will be used to feed the spelling index
   * @param originalIndexDirectory represents the source index path
   */
  public static void createSpellIndexForAllLanguage(String field, String originalIndexDirectory) {
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      if (!language.equalsIgnoreCase("fr")) {
        field = field + "_" + language;
      }
      DidYouMeanIndexer.createSpellIndex(field, originalIndexDirectory,
          originalIndexDirectory + SUFFIX_SPELLING_INDEX_PATH);
    }

  }
}
