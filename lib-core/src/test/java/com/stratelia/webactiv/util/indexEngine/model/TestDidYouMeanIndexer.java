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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer
 */
public class TestDidYouMeanIndexer {

  final private static String base = System.getProperty("basedir");
  public static String spellingIndexpath =
      base + File.separatorChar + "target" + File.separatorChar + "test-classes" +
          File.separatorChar + "index" + File.separatorChar + "kmelia1" +
          File.separatorChar + "index" + DidYouMeanIndexer.SUFFIX_SPELLING_INDEX_PATH;

  final private static String originalIndexpath =
      base + File.separatorChar + "target" + File.separatorChar + "test-classes" +
      File.separatorChar + "index" + File.separatorChar + "kmelia1" +
      File.separatorChar + "index";


  final private static String secondSpellingIndexPath =
      base + File.separatorChar + "target" + File.separatorChar + "test-classes" +
          File.separatorChar + "index" + File.separatorChar + "kmelia1" +
          File.separatorChar + "secondIndex";

  final private static String createSpellingIndexpath = spellingIndexpath + "create";
  final private static String clearSpellingIndexpath = spellingIndexpath + "clear";
  final private static String clear2SpellingIndexpath = spellingIndexpath + "clear2";
  final private static String createSpellingIndexpathAllLanguage = spellingIndexpath + "language";

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    File file =
        new File(base + File.separatorChar + "target" + File.separatorChar + "test-classes" +
        File.separatorChar + "index" + File.separatorChar + "kmelia1");
    if (file.exists())
    {
      FileUtils.forceDelete(file);
    }
    File fileDest = new File(TestDidYouMeanIndexer.originalIndexpath);
    File fileSrc =
        new File(base + File.separatorChar + "src" + File.separatorChar + "test" +
            File.separatorChar +
            "resources" + File.separatorChar + "index" + File.separatorChar + "kmelia1" +
            File.separatorChar + "index");

    FileUtils.copyDirectory(fileSrc, fileDest);


  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File file =
        new File(base + File.separatorChar + "target" + File.separatorChar + "test-classes" +
            File.separatorChar + "index" + File.separatorChar + "kmelia1");
    FileUtils.forceDeleteOnExit(file);

  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer#createSpellIndex(java.lang.String, java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testCreateSpellIndexThreeStringParameter() {
    assertNotNull(base);
    // creating the spelling index

    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, createSpellingIndexpath);
    checkIndexExistence(new String[] { createSpellingIndexpath });
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer#createSpellIndex(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testCreateSpellIndexTwoStringsParameter() {
    assertNotNull(base);
    // creating the spelling index
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath);
    checkIndexExistence(new String[] { spellingIndexpath });
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer#clearSpellIndex(java.lang.String)}
   * .
   * @throws IOException
   */
  @Test
  public final void testClearSpellIndex() throws IOException {
    assertNotNull(base);


    // creating the spelling index
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, clearSpellingIndexpath);
    // check the index creation
    checkIndexExistence(new String[] { clearSpellingIndexpath });

    // check the existence of a word in the index

    assertTrue(checkExistingWord(clearSpellingIndexpath, "java"));

    // emptying the spelling index
    DidYouMeanIndexer.clearSpellIndex(clearSpellingIndexpath);

    // checks the existence of the same word after the emptying
    assertTrue(!checkExistingWord(clearSpellingIndexpath, "java"));

  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer#clearSpellIdex(java.lang.String[])}
   * .
   * @throws IOException
   */
  @Test
  public final void testClearSpellIndexWithArrayParameter() throws IOException {
    assertNotNull(base);

    // creating the spelling indexes


    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, secondSpellingIndexPath);
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, clear2SpellingIndexpath);
    String[] paths = { clear2SpellingIndexpath, secondSpellingIndexPath };
    // check the index creation
    checkIndexExistence(paths);

    // check the existence of a word in the first index
    assertTrue(checkExistingWord(clear2SpellingIndexpath, "java"));

    // check the existence of a word in the second index
    assertTrue(checkExistingWord(secondSpellingIndexPath, "java"));

    // emptying the spelling indexes


    DidYouMeanIndexer.clearSpellIndex(paths);

    // checks the existence of the same word after the emptying
    assertTrue(!checkExistingWord(clear2SpellingIndexpath, "java"));
    assertTrue(!checkExistingWord(secondSpellingIndexPath, "java"));
  }

  /**
   * Test method for {@link 
   * com.stratelia.webactiv.util.indexEngine.model.DidYouMeanIndexer.createSpellIndexForAllLanguage
   * (String, String)} .
   * @throws IOException
   * @throws IOException
   */
  public final void testCreateSpellIndexForAllLanguage() throws IOException {
    DidYouMeanIndexer.createSpellIndexForAllLanguage("content", createSpellingIndexpathAllLanguage);

    String[] paths = { createSpellingIndexpathAllLanguage };
    // check the index creation
    checkIndexExistence(paths);
    // check the existence of a word in the index
    assertTrue(checkExistingWord(createSpellingIndexpathAllLanguage, "java"));

  }

  /**
   * check the existence of the given word in the index
   * @param indexPath index path's
   * @param word String to check
   * @return
   * @throws IOException
   */
  private boolean checkExistingWord(String indexPath, String word) throws IOException {

    File file = new File(indexPath);
    FSDirectory directory = FSDirectory.getDirectory(file);
    SpellChecker spellChecker = new SpellChecker(directory);
    return spellChecker.exist(word);

  }

  /**
   * 
   */
  private void checkIndexExistence(String[] paths) {
    for (String path : paths) {
      // check if the index spelling have been created
      File dir = new File(path);
      String[] filesList = dir.list();
      assertTrue(filesList.length == 3);
      File segmentsGenFile = new File(path + File.separatorChar + "segments.gen");
      assertTrue(segmentsGenFile.exists());

    }
  }

}
