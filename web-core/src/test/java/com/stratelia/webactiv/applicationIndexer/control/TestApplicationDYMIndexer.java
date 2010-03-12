/**
 * 
 */
package com.stratelia.webactiv.applicationIndexer.control;

import com.silverpeas.components.model.AbstractTestDao;
import com.silverpeas.util.PathTestUtil;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

/**
 * Tests com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer
 */
public class TestApplicationDYMIndexer extends AbstractTestDao {

  private String indexDirectory = "";

  @Override
  public void setUp() throws Exception {
    Properties props = new Properties();
    props.load(this.getClass().getClassLoader().getResourceAsStream("com/stratelia/webactiv/general.properties"));
    indexDirectory = props.getProperty("uploadsIndexPath");
    super.setUp();
  }

  /**
   * Test method for
   * {@link com.stratelia.webactiv.applicationIndexer.control.ApplicationDYMIndexer#indexPersonalComponent(java.lang.String)}
   * .
   * @throws IOException
   */
  @Test
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
  @Test
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
  @Test
  public final void testIndexPdc() throws IOException {
    String indexSpellcheckerPath =
            indexDirectory + File.separatorChar + "pdc" + File.separatorChar
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
  @Test
  public final void testIndexAllSpaces() throws Exception {

    ApplicationDYMIndexer indexer = new ApplicationDYMIndexer();

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
    return DatabaseOperation.DELETE;
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
    assertTrue(dir.exists());
    String[] filesList = dir.list();
    assertNotNull(filesList);
    assertEquals(filesList.length, 3);
    File segmentsGenFile = new File(path + File.separatorChar + "segments.gen");
    assertTrue(segmentsGenFile.exists());
  }
}
