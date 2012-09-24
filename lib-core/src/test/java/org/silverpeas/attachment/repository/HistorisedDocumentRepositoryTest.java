/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.attachment.repository;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.BetterRepositoryFactoryBean;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;

import static com.silverpeas.jcrutil.JcrConstants.NT_FOLDER;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pure-memory-jcr.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class HistorisedDocumentRepositoryTest {

  private static final String instanceId = "kmelia73";
  private static EmbeddedDatabase dataSource;
  private boolean registred = false;
  private static BetterRepositoryFactoryBean shutdown;
  @Inject
  private BetterRepositoryFactoryBean helper;
  @Resource
  private Repository repository;
  private DocumentRepository documentRepository = new DocumentRepository();

  public Repository getRepository() {
    return this.repository;
  }

  public HistorisedDocumentRepositoryTest() {
  }

  @Before
  public void setUp() throws RepositoryException, ParseException, IOException, SQLException {
    if (!registred) {
      Reader reader = new InputStreamReader(HistorisedDocumentRepositoryTest.class.getClassLoader().
          getResourceAsStream("silverpeas-jcr.txt"), Charsets.UTF_8);
      try {
        SilverpeasRegister.registerNodeTypes(reader);
      } finally {
        IOUtils.closeQuietly(reader);
      }
      registred = true;
      DBUtil.getInstanceForTest(dataSource.getConnection());
    }
    Session session = null;
    try {
      session = getRepository().login(new SilverpeasSystemCredentials());
      if (!session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().addNode(instanceId, NT_FOLDER);
      }
      session.save();
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    if (shutdown == null) {
      shutdown = helper;
    }
  }

  @After
  public void cleanRepository() throws RepositoryException {
    Session session = null;
    try {
      session = getRepository().login(new SilverpeasSystemCredentials());
      if (session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().getNode(instanceId).remove();
      }
      session.save();
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @BeforeClass
  public static void prepareDatabase() {
    EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
    dataSource = builder.setType(EmbeddedDatabaseType.H2).addScript(
        "classpath:/org/silverpeas/attachment/repository/create-database.sql").build();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    dataSource.shutdown();
    DBUtil.clearTestInstance();
    shutdown.destroy();
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
  }

  /**
   * Test of createDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCreateDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      HistorisedDocument doc = (HistorisedDocument) documentRepository.findDocumentById(session,
          expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc.getMajorVersion(), is(1));
      assertThat(doc.getMinorVersion(), is(0));
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of deleteDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testDeleteDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreated(), is(creationDate));
      attachment = createFrenchVersionnedAttachment();
      document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document);
      session.save();
      documentRepository.unlock(session, document, false);
      documentRepository.deleteDocument(session, expResult);
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(nullValue()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testFindDocumentById() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, "en");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(0L)));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of findLast method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testFindLast() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      long oldSilverpeasId = document.getOldSilverpeasId();
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      foreignId = "node78";
      document = new HistorisedDocument(emptyId, foreignId, 5, attachment);
      result = createVersionedDocument(session, document, content);
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      session.save();
      SimpleDocument doc = documentRepository.findLast(session, instanceId, foreignId);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(oldSilverpeasId));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testUpdateDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      document.setPublicDocument(false);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      HistorisedDocument docCreated = (HistorisedDocument) documentRepository.findDocumentById(
          session, result, "fr");
      assertThat(docCreated, is(notNullValue()));
      assertThat(docCreated.getOrder(), is(10));
      assertThat(docCreated.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(docCreated.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
      assertThat(docCreated.getHistory(), is(notNullValue()));
      assertThat(docCreated.getHistory(), hasSize(0));
      assertThat(docCreated.getMajorVersion(), is(0));
      assertThat(docCreated.getMinorVersion(), is(1));
      attachment = createFrenchVersionnedAttachment();
      document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document);
      session.save();
      documentRepository.unlock(session, document, false);
      HistorisedDocument doc = (HistorisedDocument) documentRepository.findDocumentById(session,
          result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(doc.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(2));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListDocumentsByForeignId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument docNode18_1 = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      createVersionedDocument(session, docNode18_1, content);
      docNode18_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docNode18_2 = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      createVersionedDocument(session, docNode18_2, content);
      docNode18_2.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      foreignId = "node25";
      SimpleDocument docNode25_1 = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      createVersionedDocument(session, docNode25_1, content);
      docNode25_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docNode25_2 = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      createVersionedDocument(session, docNode25_2, content);
      docNode25_2.setMajorVersion(1);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsByForeignId(session, instanceId,
          "node78", "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of selectDocumentsByForeignId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectDocumentsByForeignId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument docNode18_1 = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      createVersionedDocument(session, docNode18_1, content);
      docNode18_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      SimpleDocument docNode18_2 = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      createVersionedDocument(session, docNode18_2, content);
      docNode18_2.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      foreignId = "node25";
      SimpleDocument docNode25_1 = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      createVersionedDocument(session, docNode25_1, content);
      docNode25_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      SimpleDocument docNode25_2 = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      createVersionedDocument(session, docNode25_2, content);
      docNode25_2.setMajorVersion(2);
      session.save();
      NodeIterator nodes = documentRepository.selectDocumentsByForeignId(session, instanceId,
          "node78");
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docNode18_1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docNode18_2.getId()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of selectDocumentsByOwnerId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectDocumentsByOwnerId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      HistorisedDocument docOwn10_1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
          attachment);
      createVersionedDocument(session, docOwn10_1, content);
      docOwn10_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      HistorisedDocument docOwn10_2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      createVersionedDocument(session, docOwn10_2, content);
      docOwn10_2.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishVersionnedAttachment();
      HistorisedDocument docOwn25_1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
          attachment);
      createVersionedDocument(session, docOwn25_1, content);
      docOwn25_1.setMajorVersion(1);
      session.save();
      documentRepository.lock(session, docOwn10_1, docOwn10_1.getEditedBy());
      documentRepository.lock(session, docOwn10_2, docOwn10_2.getEditedBy());
      documentRepository.lock(session, docOwn25_1, docOwn25_1.getEditedBy());
      session.save();
      NodeIterator nodes = documentRepository.selectDocumentsByOwnerId(session, instanceId, "10");
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docOwn10_1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docOwn10_2.getId()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of addContent method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testAddContent() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchVersionnedAttachment();
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.addContent(session, result, attachment, content);
      session.save();
      documentRepository.unlock(session, document, false);
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkFrenchSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of removeContent method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveContent() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.addContent(session, result, attachment, content);
      session.save();
      documentRepository.unlock(session, document, false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.removeContent(session, result, "fr");
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkEnglishSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private SimpleAttachment createEnglishVersionnedAttachment() {
    return new SimpleAttachment("test.pdf", "en", "My test document", "This is a test document",
        "This is a test".getBytes(Charsets.UTF_8).length, MimeTypes.PDF_MIME_TYPE, "0",
        RandomGenerator.getRandomCalendar().getTime(), "18");

  }

  private SimpleAttachment createFrenchVersionnedAttachment() {
    return new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
        MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", RandomGenerator.getRandomCalendar().getTime(),
        "5");
  }

  private void checkEnglishSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
    assertThat(doc.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
    assertThat(doc.getDescription(), is("This is a test document"));
    assertThat(doc.getCreatedBy(), is("0"));
  }

  private void checkFrenchSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
    assertThat(doc.getSize(), is((long) ("Ceci est un test".getBytes(Charsets.UTF_8).length)));
    assertThat(doc.getDescription(), is("Ceci est un document de test"));
  }

  /**
   * Test of listDocumentsByOwner method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListDocumentsByOwner() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      SimpleDocument docOwn10_1 = new HistorisedDocument(emptyId, foreignId, 10, owner, attachment);
      createVersionedDocument(session, docOwn10_1, content);
      docOwn10_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docOwn10_2 = new HistorisedDocument(emptyId, foreignId, 15, owner, attachment);
      createVersionedDocument(session, docOwn10_2, content);
      docOwn10_2.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument docOwn25_1 = new HistorisedDocument(emptyId, foreignId, 10, owner, attachment);
      createVersionedDocument(session, docOwn25_1, content);
      docOwn25_1.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docOwn25_2 = new HistorisedDocument(emptyId, foreignId, 15, owner, attachment);
      createVersionedDocument(session, docOwn25_2, content);
      docOwn25_2.setMajorVersion(1);
      session.save();
      documentRepository.lock(session, docOwn10_1, docOwn10_1.getEditedBy());
      documentRepository.lock(session, docOwn10_2, docOwn10_2.getEditedBy());
      documentRepository.lock(session, docOwn25_1, docOwn25_1.getEditedBy());
      documentRepository.lock(session, docOwn25_2, docOwn25_2.getEditedBy());
      session.save();
      List<SimpleDocument> docs = documentRepository
          .listDocumentsByOwner(session, instanceId, owner, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docOwn25_1, docOwn25_2));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of listExpiringDocumentsByOwner method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListExpiringDocuments() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument expiringDoc1 =
          new HistorisedDocument(emptyId, foreignId, 10, owner, attachment);
      expiringDoc1.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc1, content);
      expiringDoc1.setMajorVersion(1);
      documentRepository.lock(session, expiringDoc1, owner);
      documentRepository.updateDocument(session, expiringDoc1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notExpiringDoc2, content);
      notExpiringDoc2.setMajorVersion(1);
      documentRepository.lock(session, notExpiringDoc2, owner);
      documentRepository.updateDocument(session, notExpiringDoc2);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument expiringDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc3, content);
      expiringDoc3.setMajorVersion(1);
      documentRepository.lock(session, expiringDoc3, owner);
      documentRepository.updateDocument(session, expiringDoc3);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      createVersionedDocument(session, notExpiringDoc4, content);
      notExpiringDoc4.setMajorVersion(1);
      documentRepository.lock(session, notExpiringDoc4, owner);
      documentRepository.updateDocument(session, notExpiringDoc4);
      session.save();
      List<SimpleDocument> docs = documentRepository.listExpiringDocuments(session, today.getTime(),
          "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(expiringDoc1, expiringDoc3));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of selectExpiringDocuments method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectExpiringDocuments() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument expiringDoc1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
          attachment);
      expiringDoc1.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc1, content);
      documentRepository.lock(session, expiringDoc1, owner);
      documentRepository.updateDocument(session, expiringDoc1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notExpiringDoc2, content);
      documentRepository.lock(session, notExpiringDoc2, owner);
      documentRepository.updateDocument(session, notExpiringDoc2);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      SimpleDocument expiringDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc3, content);
      documentRepository.lock(session, expiringDoc3, owner);
      documentRepository.updateDocument(session, expiringDoc3);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      createVersionedDocument(session, notExpiringDoc4, content);
      documentRepository.lock(session, notExpiringDoc4, owner);
      documentRepository.updateDocument(session, notExpiringDoc4);
      session.save();
      NodeIterator nodes = documentRepository.selectExpiringDocuments(session, today.getTime());
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(expiringDoc1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(expiringDoc3.getId()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of listDocumentsToUnlock method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListDocumentsToUnlock() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument docToLeaveLocked1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
          attachment);
      docToLeaveLocked1.setExpiry(today.getTime());
      createVersionedDocument(session, docToLeaveLocked1, content);
      docToLeaveLocked1.setMajorVersion(1);
      documentRepository.lock(session, docToLeaveLocked1, owner);
      documentRepository.updateDocument(session, docToLeaveLocked1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock2 =
          new HistorisedDocument(emptyId, foreignId, 15, owner, attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock2, content);
      docToUnlock2.setMajorVersion(1);
      documentRepository.lock(session, docToUnlock2, owner);
      documentRepository.updateDocument(session, docToUnlock2);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock3, content);
      docToUnlock3.setMajorVersion(1);      
      documentRepository.lock(session, docToUnlock3, owner);
      documentRepository.updateDocument(session, docToUnlock3);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToLeaveLocked4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      createVersionedDocument(session, docToLeaveLocked4, content);
      docToLeaveLocked4.setMajorVersion(1);
      documentRepository.lock(session, docToLeaveLocked4, owner);
      documentRepository.updateDocument(session, docToLeaveLocked4);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsToUnlock(session, today.getTime(),
          "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(docToUnlock2, docToUnlock3));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of selectDocumentsRequiringUnlocking method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectDocumentsRequiringUnlocking() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument docToLeaveLocked1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
          attachment);
      docToLeaveLocked1.setExpiry(today.getTime());
      createVersionedDocument(session, docToLeaveLocked1, content);
      docToLeaveLocked1.setMajorVersion(1);
      documentRepository.lock(session, docToLeaveLocked1, owner);
      documentRepository.updateDocument(session, docToLeaveLocked1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock2, content);
      docToUnlock2.setMajorVersion(1);      
      documentRepository.lock(session, docToUnlock2, owner);
      documentRepository.updateDocument(session, docToUnlock2);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock3, content);
      docToUnlock3.setMajorVersion(1);
      documentRepository.lock(session, docToUnlock3, owner);
      documentRepository.updateDocument(session, docToUnlock3);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, docToLeaveLocked4, content);
      documentRepository.lock(session, docToLeaveLocked4, owner);
      documentRepository.updateDocument(session, docToLeaveLocked4);
      session.save();
      NodeIterator nodes = documentRepository.selectDocumentsRequiringUnlocking(session, today.
          getTime());
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docToUnlock2.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docToUnlock3.getId()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of selectWarningDocuments method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectWarningDocuments() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument warningDoc1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      warningDoc1.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notWarningDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notWarningDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      SimpleDocument warningDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notWarningDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notWarningDoc4.setAlert(beforeDate.getTime());
      createVersionedDocument(session, notWarningDoc4, content);
      session.save();
      NodeIterator nodes = documentRepository.selectWarningDocuments(session, today.getTime());
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(warningDoc1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(warningDoc3.getId()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testMoveDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      createVersionedDocument(session, document, content);
      foreignId = "kmelia36";
      SimpleDocumentPK result = documentRepository.moveDocument(session, document, new ForeignPK(
          "45", foreignId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), foreignId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCopyDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      createVersionedDocument(session, document, content);
      session.save();
      foreignId = "node36";
      SimpleDocumentPK result = documentRepository.copyDocument(session, document, new ForeignPK(
          foreignId, instanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(foreignId);
      document.setPK(result);
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of findDocumentByOldSilverpeasId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testFindDocumentByOldSilverpeasId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      long oldSilverpeasId = 2048L;
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(2048L);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(1024L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument otherDocument =
          new HistorisedDocument(emptyId, foreignId, 0, attachment);
      createVersionedDocument(session, otherDocument, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(2048L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument versionedDocument = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      createVersionedDocument(session, versionedDocument, content);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId,
          oldSilverpeasId, false, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(2048L));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc, SimpleDocumentMatcher.matches(document));
      doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId, oldSilverpeasId,
          true, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(2048L));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc, SimpleDocumentMatcher.matches(versionedDocument));
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of listDocumentsRequiringWarning method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsRequiringWarning() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument warningDoc1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
          attachment);
      warningDoc1.setAlert(today.getTime());
      createVersionedDocument(session, warningDoc1, content);
      warningDoc1.setMajorVersion(1);
      documentRepository.lock(session, warningDoc1, owner);
      documentRepository.updateDocument(session, warningDoc1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notWarningDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notWarningDoc2, content);
      notWarningDoc2.setMajorVersion(1);
      documentRepository.lock(session, notWarningDoc2, owner);
      documentRepository.updateDocument(session, notWarningDoc2);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument warningDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      createVersionedDocument(session, warningDoc3, content);
      documentRepository.lock(session, warningDoc3, owner);
      documentRepository.updateDocument(session, warningDoc3);
      warningDoc3.setMajorVersion(1);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notWarningDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notWarningDoc4.setAlert(beforeDate.getTime());
      createVersionedDocument(session, notWarningDoc4, content);
      notWarningDoc4.setMajorVersion(1);
      documentRepository.lock(session, notWarningDoc4, owner);
      documentRepository.updateDocument(session, notWarningDoc4);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsRequiringWarning(session, today.
          getTime(), null);
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(warningDoc1, warningDoc3));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testChangeVersionStateOfVersionedDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      document.setPublicDocument(false);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      HistorisedDocument docCreated = (HistorisedDocument) documentRepository.findDocumentById(
          session, result, "fr");
      assertThat(docCreated, is(notNullValue()));
      assertThat(docCreated.getOrder(), is(10));
      assertThat(docCreated.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(docCreated.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
      assertThat(docCreated.getHistory(), is(notNullValue()));
      assertThat(docCreated.getHistory(), hasSize(0));
      assertThat(docCreated.getMajorVersion(), is(0));
      assertThat(docCreated.getMinorVersion(), is(1));
      attachment = createFrenchVersionnedAttachment();
      document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document);
      session.save();
      documentRepository.unlock(session, document, false);
      HistorisedDocument doc = (HistorisedDocument) documentRepository.findDocumentById(session,
          result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(doc.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(2));
      documentRepository.changeVersionState(session, result);
      SimpleDocument simplifiedDocument = documentRepository.findDocumentById(session, result, "fr");
      assertThat(simplifiedDocument, is(notNullValue()));
      assertThat(simplifiedDocument.getClass().getName(), is(SimpleDocument.class.getName()));
      assertThat(simplifiedDocument.getOrder(), is(15));
      assertThat(simplifiedDocument.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(simplifiedDocument.getSize(),
          is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
      assertThat(simplifiedDocument.getMajorVersion(), is(0));
      assertThat(simplifiedDocument.getMinorVersion(), is(2));
      assertThat(simplifiedDocument.isVersioned(), is(false));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private SimpleDocumentPK createVersionedDocument(Session session, SimpleDocument document,
      InputStream content) throws RepositoryException {
    SimpleDocumentPK result = documentRepository.createDocument(session, document, content);
    document.setPK(result);
    documentRepository.unlock(session, document, false);
    return result;
  }
}
