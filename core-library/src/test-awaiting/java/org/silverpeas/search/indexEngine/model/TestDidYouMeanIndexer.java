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
package org.silverpeas.search.indexEngine.model;

import com.silverpeas.util.PathTestUtil;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test class for DidYouMeanIndexer
 */
public class TestDidYouMeanIndexer {

  public final static String spellingIndexpath = PathTestUtil.TARGET_DIR + "test-classes"
          + File.separatorChar + "index" + File.separatorChar + "kmelia1" + File.separatorChar
          + "index" + DidYouMeanIndexer.SUFFIX_SPELLING_INDEX_PATH;
  final private static String originalIndexpath = PathTestUtil.TARGET_DIR + "test-classes"
          + File.separatorChar + "index" + File.separatorChar + "kmelia1" + File.separatorChar
          + "index";
  final private static String secondSpellingIndexPath = PathTestUtil.TARGET_DIR + "test-classes"
          + File.separatorChar + "index" + File.separatorChar + "kmelia1" + File.separatorChar
          + "secondIndex";
  final private static String createSpellingIndexpath = spellingIndexpath + "create";
  final private static String clearSpellingIndexpath = spellingIndexpath + "clear";
  final private static String clear2SpellingIndexpath = spellingIndexpath + "clear2";
  final private static String createSpellingIndexpathAllLanguage = spellingIndexpath + "language";

  /**
   * @throws java.lang.Exception
   */
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    File originalIndexes = new File(originalIndexpath);
    if (originalIndexes.exists()) {
      FileUtils.forceDelete(originalIndexes);
    }
    originalIndexes.mkdirs();
    FSDirectory directory = FSDirectory.open(originalIndexes);
    IndexWriterConfig configuration = new IndexWriterConfig(Version.LUCENE_36,
            new StandardAnalyzer(Version.LUCENE_36));
    IndexWriter writer = new IndexWriter(directory, configuration);
    List<Document> docs = new ArrayList<Document>(6);
    docs.add(createDocument("2010/01/25", "0", "9999/99/99", "kmelia1|Node|3", null, "",
            "0000/00/00", "J2EE", new StringReader(("j2ee J2EE administrateur j2ee"))));
    docs.add(createDocument("2010/01/25", "0", "9999/99/99", "kmelia1|Publication|1", null, "",
            "2010/01/25", "Introduction au Java Framework",
            new StringReader(("1 introduction java framework Introduction au Java Framework "
            + "null administrateur java framework java" + "2 platform est compose trois "
            + "editions destinees usages differents j2me " + "java 2 micro edition est "
            + "prevu pour developpement applications embarquees "
            + "notamment sur assistants personnels terminaux " + "mobiles j2se java 2 standard "
            + "edition est destine developpement applications "
            + "pour ordinateurs personnels j2ee java " + "2 enterprise edition destine usage "
            + "professionnel avec mise en oeuvre "
            + "serveurs chaque edition propose environnement "
            + "complet pour developpement execution applications "
            + "basees sur java comprend notamment " + "machine virtuelle java java virtual "
            + "machine ainsi un ensemble classes " + "introduction java framework null"))));
    docs.add(createDocument("2010/01/25", "0", "9999/99/99", "kmelia1|Attachment1|1", null, null,
            "0000/00/00", "1wysiwyg.txt", new StringReader("1wysiwyg.txt java framework java 2 "
            + "platform est compose trois editions " + "destinees usages differents j2me java "
            + "2 micro edition est prevu " + "pour developpement applications embarquees notamment "
            + "sur assistants personnels terminaux mobiles " + "j2se java 2 standard edition "
            + "est destine developpement applications pour " + "ordinateurs personnels j2ee java 2 "
            + "enterprise edition destine usage professionnel " + "avec mise en oeuvre serveurs "
            + "chaque edition propose environnement complet "
            + "pour developpement execution applications basees "
            + "sur java comprend notamment machine " + "virtuelle java java virtual machine "
            + "ainsi un ensemble classes 1wysiwyg.txt")));
    docs.add(createDocument("2010/01/25", "0", "9999/99/99", "kmelia1|Publication|2",
            "java J2ee introduction", "", "2010/01/25", "Introduction à J2EE ", new StringReader(
            "2 introduction j2ee Introduction à J2EE  java "
            + "j2ee introduction null administrateur j2ee " + "java 2 enterprise edition est "
            + "norme proposee par societe sun " + "portee par consortium societes internationales "
            + "visant definir standard developpement applications "
            + "entreprises multi niveaux basees sur " + "composants on parle generalement plate "
            + "forme j2ee pour designer ensemble "
            + "constitue services api offerts infrastructure "
            + "execution j2ee comprend notamment specifications "
            + "serveur application est dire environnement "
            + "execution j2ee definit finement roles "
            + "interfaces pour applications ainsi environnement "
            + "dans seront executees ces recommandations "
            + "permettent ainsi entreprises tierces developper "
            + "serveurs application conformes specifications ainsi "
            + "definies sans avoir redevelopper principaux " + "services services travers api est "
            + "dire extensions java independantes permettant "
            + "offrir en standard certain nombre "
            + "fonctionnalites sun fournit implementation minimale " + "ces api appelee j2ee sdk "
            + "j2ee software development kit dans " + "mesure j2ee appuie entierement sur "
            + "java beneficie avantages inconvenients ce "
            + "langage en particulier bonne portabilite "
            + "maintenabilite code plus architecture j2ee "
            + "repose sur composants distincts interchangeables "
            + "distribues ce qui signifie notamment " + "il est simple etendre architecture "
            + "un systeme reposant sur j2ee " + "peut posseder mecanismes haute disponibilite "
            + "afin garantir bonne qualite service "
            + "maintenabilite applications est facilitee introduction "
            + "j2ee java j2ee introduction null")));
    docs.add(createDocument("2010/01/25", "0", "9999/99/99", "kmelia1|Attachment2|2", null, null,
            "0000/00/00", "2wysiwyg.txt", new StringReader("2wysiwyg.txt j2ee java 2 enterprise "
            + "edition est norme proposee par " + "societe sun portee par consortium "
            + "societes internationales visant definir standard "
            + "developpement applications entreprises multi niveaux "
            + "basees sur composants on parle " + "generalement plate forme j2ee pour "
            + "designer ensemble constitue services api "
            + "offerts infrastructure execution j2ee comprend "
            + "notamment specifications serveur application est "
            + "dire environnement execution j2ee definit "
            + "finement roles interfaces pour applications "
            + "ainsi environnement dans seront executees "
            + "ces recommandations permettent ainsi entreprises "
            + "tierces developper serveurs application conformes "
            + "specifications ainsi definies sans avoir "
            + "redevelopper principaux services services travers " + "api est dire extensions java ")));
    docs.add(createDocument("2010/01/27", "0", "9999/99/99", "kmelia1|Attachment7|3", null, null,
            "0000/00/00", "3wysiwyg.txt", new StringReader("3wysiwyg.txt api j2ee peuvent se "
            + "repartir en trois grandes categories "
            + "composants on distingue habituellement deux "
            + "familles composants composants web servlets " + "jsp java server pages agit "
            + "partie chargee interface avec utilisateur "
            + "on parle logique presentation composants " + "metier ejb enterprise java beans "
            + "agit composants specifiques charges traitements "
            + "donnees propres secteur activite on " + "parle logique metier logique applicative "
            + "interfacage avec bases donnees services " + "pouvant etre classes par categories "
            + "services infrastructures en existe grand " + "nombre definis ci dessous jdbc "
            + "java database connectivity est api " + "acces bases donnees relationnelles jndi "
            + "java naming directory interface est " + "api acces services nommage annuaires "
            + "entreprises tels dns nis ldap " + "etc jta jts java transaction "
            + "api java transaction services est " + "api definissant interfaces standard avec "
            + "gestionnaire transactions jca j2ee connector "
            + "architecture est api connexion systeme "
            + "information entreprise notamment systemes dits " + "legacy tels erp jmx java "
            + "management extension fournit extensions permettant "
            + "developper applications web supervision applications "
            + "services communication jaas java authentication "
            + "authorization service est api gestion "
            + "authentification droits acces javamail est "
            + "api permettant envoi courrier electronique " + "jms java message service fournit "
            + "fonctionnalites communication asynchrone appelees mom "
            + "pour middleware object message entre " + "applications rmi iiop est api "
            + "permettant communication synchrone entre objets "
            + "architecture j2ee permet ainsi separer "
            + "couche presentation correspondant interface homme "
            + "machine ihm couche metier contenant " + "essentiel traitements donnees en se "
            + "basant dans mesure possible sur " + "api existantes enfin couche donnees "
            + "correspondant informations entreprise stockees dans "
            + "fichiers dans bases donnees relationnelles "
            + "xml dans annuaires entreprise encore "
            + "dans systemes information complexes 3wysiwyg.txt")));
    docs.add(createDocument("2010/01/27", "0", "9999/99/99", "kmelia1|Publication|3", "", "",
            "2010/01/25", "Les API de J2EE", new StringReader(
            "3 api j2ee Les API de J2EE administrateur " + "api j2ee peuvent se repartir "
            + "en trois grandes categories composants "
            + "on distingue habituellement deux familles "
            + "composants composants web servlets jsp " + "java server pages agit partie "
            + "chargee interface avec utilisateur on "
            + "parle logique presentation composants metier " + "ejb enterprise java beans agit "
            + "composants specifiques charges traitements donnees "
            + "propres secteur activite on parle "
            + "logique metier logique applicative interfacage "
            + "avec bases donnees services pouvant " + "etre classes par categories services "
            + "infrastructures en existe grand nombre " + "definis ci dessous jdbc java "
            + "database connectivity est api acces " + "bases donnees relationnelles jndi java "
            + "naming directory interface est api "
            + "acces services nommage annuaires entreprises " + "tels dns nis ldap etc "
            + "jta jts java transaction api " + "java transaction services est api "
            + "definissant interfaces standard avec gestionnaire "
            + "transactions jca j2ee connector architecture "
            + "est api connexion systeme information "
            + "entreprise notamment systemes dits legacy " + "tels erp jmx java management "
            + "extension fournit extensions permettant developper "
            + "applications web supervision applications services "
            + "communication jaas java authentication authorization "
            + "service est api gestion authentification " + "droits acces javamail est api "
            + "permettant envoi courrier electronique jms "
            + "java message service fournit fonctionnalites "
            + "communication asynchrone appelees mom pour "
            + "middleware object message entre applications " + "rmi iiop est api permettant "
            + "communication synchrone entre objets architecture "
            + "j2ee permet ainsi separer couche "
            + "presentation correspondant interface homme machine "
            + "ihm couche metier contenant essentiel " + "traitements donnees en se basant "
            + "dans mesure possible sur api " + "existantes enfin couche donnees correspondant "
            + "informations entreprise stockees dans fichiers "
            + "dans bases donnees relationnelles xml " + "dans annuaires entreprise encore dans "
            + "systemes information complexes api j2ee")));
    writer.addDocuments(docs);
    writer.close();
  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    File file = new File(PathTestUtil.TARGET_DIR + "test-classes"
            + File.separatorChar + "index" + File.separatorChar + "kmelia1");
    FileUtils.forceDeleteOnExit(file);

  }

  private static Document createDocument(String creationDate, String creatorId, String endDate,
          String key, String keywords, String preview, String startDate, String title, Reader reader) {
    Document doc = new Document();
    doc.add(new Field("creationDate", creationDate, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("creationUser", creatorId, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("endDate", endDate, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("key", key, Field.Store.YES, Field.Index.ANALYZED));
    if (keywords != null) {
      doc.add(new Field("keywords", keywords, Field.Store.YES, Field.Index.NO));
    }
    if (preview != null) {
      doc.add(new Field("preview", preview, Field.Store.YES, Field.Index.ANALYZED));
    }
    doc.add(new Field("startDate", startDate, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("title", title, Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("content", reader));
    return doc;
  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {
  }

  /**
   * Test method for
   * {@link org.silverpeas.search.indexEngine.model.DidYouMeanIndexer#createSpellIndex(java.lang.String, java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testCreateSpellIndexThreeStringParameter() {
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, createSpellingIndexpath);
    checkIndexExistence(createSpellingIndexpath);
  }

  /**
   * Test method for
   * {@link org.silverpeas.search.indexEngine.model.DidYouMeanIndexer#createSpellIndex(java.lang.String, java.lang.String)}
   * .
   */
  @Test
  public final void testCreateSpellIndexTwoStringsParameter() {
    // creating the spelling index
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath);
    checkIndexExistence(spellingIndexpath);
  }

  /**
   * Test method for
   * {@link org.silverpeas.search.indexEngine.model.DidYouMeanIndexer#clearSpellIndex(java.lang.String)}
   * .
   *
   * @throws IOException
   */
  @Test
  public final void testClearSpellIndex() throws IOException {
    // creating the spelling index
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, clearSpellingIndexpath);
    // check the index creation
    checkIndexExistence(clearSpellingIndexpath);
    // check the existence of a word in the index
    assertTrue(checkExistingWord(clearSpellingIndexpath, "java"));
    // emptying the spelling index
    DidYouMeanIndexer.clearSpellIndex(clearSpellingIndexpath);
    // checks the existence of the same word after the emptying
    assertTrue(!checkExistingWord(clearSpellingIndexpath, "java"));
  }

  /**
   * Test method for
   * {@link org.silverpeas.search.indexEngine.model.DidYouMeanIndexer#clearSpellIdex(java.lang.String[])}
   * .
   *
   * @throws IOException
   */
  @Test
  public final void testClearSpellIndexWithArrayParameter() throws IOException {
    // creating the spelling indexes
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, secondSpellingIndexPath);
    DidYouMeanIndexer.createSpellIndex("content", originalIndexpath, clear2SpellingIndexpath);
    String[] paths = {clear2SpellingIndexpath, secondSpellingIndexPath};
    // check the index creation
    checkIndexExistence(clear2SpellingIndexpath);
    checkIndexExistence(secondSpellingIndexPath);
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
   * org.silverpeas.search.indexEngine.model.DidYouMeanIndexer.createSpellIndexForAllLanguage
   * (String, String)} .
   *
   * @throws IOException
   * @throws IOException
   */
  public final void testCreateSpellIndexForAllLanguage() throws IOException {
    DidYouMeanIndexer.createSpellIndexForAllLanguage("content", createSpellingIndexpathAllLanguage);
    // check the index creation
    checkIndexExistence(createSpellingIndexpathAllLanguage);
    // check the existence of a word in the index
    assertTrue(checkExistingWord(createSpellingIndexpathAllLanguage, "java"));
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
    String[] filesList = dir.list();
    assertThat(filesList.length, is(9));
    File segmentsGenFile = new File(path + File.separatorChar + "segments.gen");
    assertThat(segmentsGenFile.exists(), is(true));
  }
}
