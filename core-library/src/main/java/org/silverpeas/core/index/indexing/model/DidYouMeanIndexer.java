/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.StringUtil;

import java.io.File;
import java.io.IOException;

import static org.silverpeas.core.index.indexing.IndexingLogger.indexingLogger;

/**
 * This class allows to manage the specific index of "did you mean" functionality. <br>
 * creates new entry into index <br>
 * clear all the entries from index
 */
public class DidYouMeanIndexer {

  /**
   * Suffix used to differentiate the standard index path's and the spelling index path's
   */
  private static final String SUFFIX_SPELLING_INDEX_PATH = "Spell";
  private static final String DEFAULT_LANGUAGE = "fr";

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
      indexingLogger().error("Invalid argument passed to create a spell index");
      return;
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

      // create a file object with given path
      File file = new File(pathSpellChecker);
      if (file.exists()) {
        // create a spellChecker with the file object
        try(SpellChecker spell = new SpellChecker(FSDirectory.open(file.toPath()))) {
          // if index exists, clears his content
            spell.clearIndex();
            isCleared = true;
        } catch (IOException e) {
          indexingLogger().error(e);
        }
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
    StringBuilder localizedField = new StringBuilder(field);
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      if (!language.equalsIgnoreCase(DEFAULT_LANGUAGE)) {
        localizedField.append("_").append(language);
      }
      DidYouMeanIndexer.createSpellIndex(localizedField.toString(), originalIndexDirectory,
          originalIndexDirectory + SUFFIX_SPELLING_INDEX_PATH);
    }

  }
}
