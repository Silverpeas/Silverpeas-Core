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
/**
 *
 */
package com.stratelia.webactiv.applicationIndexer.control;

import com.silverpeas.components.model.AbstractTestDao;
import com.stratelia.webactiv.util.FileRepositoryManager;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.dbunit.operation.DatabaseOperation;

/**
 * Tests com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer
 */
public class TestApplicationDYMIndexer extends AbstractTestDao {

  private String indexDirectory = "";

  @Override
  public void setUp() throws Exception {
    super.setUp();
    Properties props = new Properties();
    props.load(this.getClass().getClassLoader().getResourceAsStream(
        "com/stratelia/webactiv/general.properties"));
    indexDirectory = props.getProperty("uploadsIndexPath");
    assertEquals(FileRepositoryManager.getIndexUpLoadPath(), indexDirectory + File.separatorChar);
    super.setUp();
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPersonalComponent(java.lang.String)}
   * .
   * @throws IOException
   */
  public final void testIndexPersonalComponent() throws IOException {
    String indexSpellcheckerpath =
        indexDirectory + File.separatorChar + "user@0_agenda" + File.separatorChar
        + "indexSpell";
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();

    indexer.indexPersonalComponent("agenda");
    checkIndexExistence(indexSpellcheckerpath);
    assertTrue(checkExistingWord(indexSpellcheckerpath, "boulot"));
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPersonalComponents()}
   * .
   * @throws IOException
   */
  public final void testIndexPersonalComponents() throws IOException {
    String indexSpellcheckerpath =
        indexDirectory + File.separatorChar + "user@0_todo" + File.separatorChar
        + "indexSpell";
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();

    indexer.indexPersonalComponents();
    checkIndexExistence(indexSpellcheckerpath);
    assertTrue(checkExistingWord(indexSpellcheckerpath, "tester"));

    String indexSpellcheckerpath2 =
        indexDirectory + File.separatorChar + "user@1_todo" + File.separatorChar
        + "indexSpell";
    checkIndexExistence(indexSpellcheckerpath2);
    assertTrue(checkExistingWord(indexSpellcheckerpath2, "commencer"));
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPdc()}.
   * @throws IOException
   */
  public final void testIndexPdc() throws IOException {
    String indexSpellcheckerPath = indexDirectory + File.separatorChar + "pdc" + File.separatorChar
        + "indexSpell";
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();
    indexer.indexPdc();
    checkIndexExistence(indexSpellcheckerPath);
    assertTrue(checkExistingWord(indexSpellcheckerPath, "java"));
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexAllSpaces()}
   * .
   * @throws Exception
   */
  public final void testIndexAllSpaces() throws Exception {
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();
    indexer.organizationController.reloadAdminCache();
    indexer.indexAllSpaces();
    // check one component of first space
    String indexSpellcheckerPath =
        indexDirectory + File.separatorChar + "kmelia1" + File.separatorChar
        + "indexSpell";
    checkIndexExistence(indexSpellcheckerPath);
    assertTrue(checkExistingWord(indexSpellcheckerPath, "javamail"));
    // check one component of second space
    String indexSpellcheckerPath2 =
        indexDirectory + File.separatorChar + "kmelia17" + File.separatorChar
        + "indexSpell";
    checkIndexExistence(indexSpellcheckerPath2);
    assertTrue(checkExistingWord(indexSpellcheckerPath2, "reseau"));
  }

  @Override
  protected DatabaseOperation getTearDownOperation() throws Exception {
    return DatabaseOperation.DELETE_ALL;
  }

  @Override
  protected String getDatasetFileName() {
    return "dym-indexer-dataset.xml";
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
  private void checkIndexExistence(String path) {
    File dir = new File(path);
    assertNotNull(dir);
    assertTrue("Directory doesn't exist " + path, dir.exists());
    String[] filesList = dir.list();
    assertNotNull("No files found in " + path, filesList);
    assertEquals("There are mor or less than 3 files in " + path, filesList.length, 3);
    File segmentsGenFile = new File(path + File.separatorChar + "segments.gen");
    assertTrue(segmentsGenFile.getPath() + "doesn't exist", segmentsGenFile.exists());
  }
}
