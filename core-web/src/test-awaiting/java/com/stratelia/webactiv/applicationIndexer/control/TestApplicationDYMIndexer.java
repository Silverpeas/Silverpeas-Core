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
package com.stratelia.webactiv.applicationIndexer.control;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;

import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import com.silverpeas.components.model.AbstractTestDao;

import static java.io.File.separatorChar;
import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests org.silverpeas.core.web.index.ApplicationDYMIndexer
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
    assertEquals(org.silverpeas.search.indexEngine.IndexFileManager.getIndexUpLoadPath(),
        indexDirectory + separatorChar);
    OrganizationControllerProvider.getFactory().clearFactory();
    OrganizationControllerProvider.getOrganisationController().reloadAdminCache();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPersonalComponent(java.lang.String)}
   * .
   *
   * @throws IOException
   */
  @Test
  public void testIndexPersonalComponent() throws IOException {
    String indexSpellcheckerpath = getIndexPath("user@0_agenda");
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();
    indexer.indexPersonalComponent("agenda");
    checkIndexExistence(indexSpellcheckerpath);
    assertTrue(checkExistingWord(indexSpellcheckerpath, "boulot"));
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPersonalComponents()}
   * .
   *
   * @throws IOException
   */
  @Test
  public void testIndexPersonalComponents() throws IOException {
    String indexSpellcheckerpath = getIndexPath("user@0_todo");
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();

    indexer.indexPersonalComponents();
    checkIndexExistence(indexSpellcheckerpath);
    assertTrue(checkExistingWord(indexSpellcheckerpath, "tester"));

    String indexSpellcheckerpath2 = getIndexPath("user@1_todo");
    checkIndexExistence(indexSpellcheckerpath2);
    assertTrue(checkExistingWord(indexSpellcheckerpath2, "commencer"));
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPdc()}.
   *
   * @throws IOException
   */
  @Test
  public void testIndexPdc() throws IOException {
    String indexSpellcheckerPath = getIndexPath("pdc");
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();
    indexer.indexPdc();
    checkIndexExistence(indexSpellcheckerPath);
    assertTrue(checkExistingWord(indexSpellcheckerPath, "java"));
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexAllSpaces()}
   * .
   *
   * @throws Exception
   */
  @Test
  public void testIndexAllSpaces() throws Exception {
    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();
    indexer.indexAllSpaces();
    // check one component of first space
    String indexSpellcheckerPath = getIndexPath("kmelia1");
    checkIndexExistence(indexSpellcheckerPath);
    assertTrue(checkExistingWord(indexSpellcheckerPath, "javamail"));
    // check one component of second space
    String indexSpellcheckerPath2 = getIndexPath("kmelia17");
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
   *
   * @param indexPath index path's
   * @param word String to check
   * @return
   * @throws IOException
   */
  private boolean checkExistingWord(String indexPath, String word) throws IOException {
    File file = new File(indexPath);
    FSDirectory directory = FSDirectory.open(file);
    SpellChecker spellChecker = new SpellChecker(directory);
    return spellChecker.exist(word);
  }

  /**
   *
   */
  private void checkIndexExistence(String path) {
    File dir = new File(path);
    assertNotNull(dir);
    assertThat(dir.exists(), is(true));
    String[] filesList = dir.list();
    assertNotNull("No files found in " + path, filesList);
    assertThat(filesList.length, is(9));
    File segmentsGenFile = new File(path + separatorChar + "segments.gen");
    assertTrue(segmentsGenFile.getPath() + "doesn't exist", segmentsGenFile.exists());
  }

  private String getIndexPath(String application) {
    return indexDirectory + separatorChar + application + separatorChar + "indexSpell";
  }

  @Override
  protected String getTableCreationFileName() {
    return "create-database.sql";
  }
}
