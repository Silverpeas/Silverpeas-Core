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

import org.silverpeas.attachment.model.DocumentType;
import org.silverpeas.attachment.model.HistorisedDocument;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.BetterRepositoryFactoryBean;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.model.impl.AbstractJcrRegisteringTestCase;
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
public class DocumentRepositoryTest {

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

  public DocumentRepositoryTest() {
  }

  @Before
  public void setUp() throws RepositoryException, ParseException, IOException, SQLException {
    if (!registred) {
      Reader reader = new InputStreamReader(AbstractJcrRegisteringTestCase.class.getClassLoader().
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
      if (session.getRootNode().hasNodes()) {
        NodeIterator iter = session.getRootNode().getNodes(instanceId);
        while (iter.hasNext()) {
          iter.nextNode().remove();
        }
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
   */
  @Test
  public void testCreateDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of deleteDocument method, of class DocumentRepository.
   */
  @Test
  public void testDeleteDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreated(), is(creationDate));
      documentRepository.deleteDocument(session, expResult);
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(nullValue()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   */
  @Test
  public void testFindDocumentById() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String creatorId = "0";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
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
   */
  @Test
  public void testFindLast() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      long oldSilverpeasId = document.getOldSilverpeasId();
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      foreignId = "node18";
      document = new SimpleDocument(emptyId, foreignId, 5, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
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
   */
  @Test
  public void testUpdateDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.updateDocument(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));

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
  public void testChangeVersionStateOfSimpleDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.updateDocument(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      documentRepository.changeVersionState(session, result);
      session.save();
      HistorisedDocument historisedDocument = (HistorisedDocument) documentRepository.
          findDocumentById(session, result, "fr");
      assertThat(historisedDocument, is(notNullValue()));
      assertThat(historisedDocument.getOrder(), is(15));
      assertThat(historisedDocument.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(historisedDocument.getSize(), is(28L));
      assertThat(historisedDocument.getHistory(), is(notNullValue()));
      assertThat(historisedDocument.getHistory(), hasSize(0));
      assertThat(historisedDocument.getMajorVersion(), is(2));
      assertThat(historisedDocument.getMinorVersion(), is(0));
      assertThat(historisedDocument.isVersioned(), is(true));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsByForeignId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(session, docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(session, docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      foreignId = "node25";
      SimpleDocument docNode25_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode25_1);
      documentRepository.storeContent(session, docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode25_2);
      documentRepository.storeContent(session, docNode25_2, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsByForeignId(session, instanceId,
          "node18", "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of selectDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testSelectDocumentsByForeignId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(session, docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(session, docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      foreignId = "node25";
      SimpleDocument docNode25_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode25_1);
      documentRepository.storeContent(session, docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode25_2);
      documentRepository.storeContent(session, docNode25_2, content);
      session.save();
      NodeIterator nodes = documentRepository.selectDocumentsByForeignIdAndType(session, instanceId,
          "node18", DocumentType.attachment);
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
   */
  @Test
  public void testSelectDocumentsByOwnerId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      SimpleDocument docOwn10_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_1);
      documentRepository.storeContent(session, docOwn10_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn10_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_2);
      documentRepository.storeContent(session, docOwn10_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docOwn25_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_1);
      documentRepository.storeContent(session, docOwn25_1, content);
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
   */
  @Test
  public void testAddContent() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      documentRepository.addContent(session, result, attachment);
      document.setLanguage(attachment.getLanguage());
      documentRepository.storeContent(session, document, content);
      session.save();
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
   */
  @Test
  public void testRemoveContent() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      documentRepository.addContent(session, result, attachment);
      document.setLanguage(attachment.getLanguage());
      documentRepository.storeContent(session, document, content);
      session.save();
      documentRepository.removeContent(session, result, "fr");
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkEnglishSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private SimpleAttachment createEnglishSimpleAttachment() {
    return new SimpleAttachment("test.pdf", "en", "My test document", "This is a test document",
        "This is a test".getBytes(Charsets.UTF_8).length, MimeTypes.PDF_MIME_TYPE, "0",
        RandomGenerator.getRandomCalendar().getTime(), "18");

  }

  private SimpleAttachment createFrenchSimpleAttachment() {
    return new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", 28L, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        "10", RandomGenerator.getRandomCalendar().getTime(), "5");
  }

  private void checkEnglishSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
    assertThat(doc.getSize(), is(14L));
    assertThat(doc.getDescription(), is("This is a test document"));
    assertThat(doc.getCreatedBy(), is("0"));
  }

  private void checkFrenchSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
    assertThat(doc.getSize(), is(28L));
    assertThat(doc.getDescription(), is("Ceci est un document de test"));
  }

  /**
   * Test of listDocumentsByOwner method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsByOwner() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      SimpleDocument docOwn10_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_1);
      documentRepository.storeContent(session, docOwn10_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn10_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_2);
      documentRepository.storeContent(session, docOwn10_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docOwn25_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_1);
      documentRepository.storeContent(session, docOwn25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn25_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_2);
      documentRepository.storeContent(session, docOwn25_2, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.
          listDocumentsByOwner(session, instanceId, owner, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docOwn25_1, docOwn25_2));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of listExpiringDocumentsByOwner method, of class DocumentRepository.
   */
  @Test
  public void testListExpiringDocuments() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument expiringDoc1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      expiringDoc1.setExpiry(today.getTime());
      documentRepository.createDocument(session, expiringDoc1);
      documentRepository.storeContent(session, expiringDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notExpiringDoc2);
      documentRepository.storeContent(session, notExpiringDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument expiringDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      documentRepository.createDocument(session, expiringDoc3);
      documentRepository.storeContent(session, expiringDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, notExpiringDoc4);
      documentRepository.storeContent(session, notExpiringDoc4, content);
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
   */
  @Test
  public void testSelectExpiringDocuments() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument expiringDoc1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      expiringDoc1.setExpiry(today.getTime());
      documentRepository.createDocument(session, expiringDoc1);
      documentRepository.storeContent(session, expiringDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notExpiringDoc2);
      documentRepository.storeContent(session, notExpiringDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument expiringDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      documentRepository.createDocument(session, expiringDoc3);
      documentRepository.storeContent(session, expiringDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, notExpiringDoc4);
      documentRepository.storeContent(session, notExpiringDoc4, content);
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
   */
  @Test
  public void testListDocumentsToUnlock() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument docToLeaveLocked1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      docToLeaveLocked1.setExpiry(today.getTime());
      documentRepository.createDocument(session, docToLeaveLocked1);
      documentRepository.storeContent(session, docToLeaveLocked1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToUnlock2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock2);
      documentRepository.storeContent(session, docToUnlock2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docToUnlock3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock3);
      documentRepository.storeContent(session, docToUnlock3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, docToLeaveLocked4);
      documentRepository.storeContent(session, docToLeaveLocked4, content);
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
   */
  @Test
  public void testSelectDocumentsRequiringUnlocking() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument docToLeaveLocked1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      docToLeaveLocked1.setExpiry(today.getTime());
      documentRepository.createDocument(session, docToLeaveLocked1);
      documentRepository.storeContent(session, docToLeaveLocked1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToUnlock2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock2);
      documentRepository.storeContent(session, docToUnlock2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docToUnlock3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock3);
      documentRepository.storeContent(session, docToUnlock3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, docToLeaveLocked4);
      documentRepository.storeContent(session, docToLeaveLocked4, content);
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
   */
  @Test
  public void testSelectWarningDocuments() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument warningDoc1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      warningDoc1.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc1);
      documentRepository.storeContent(session, warningDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notWarningDoc2);
      documentRepository.storeContent(session, notWarningDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument warningDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc3);
      documentRepository.storeContent(session, warningDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notWarningDoc4.setAlert(beforeDate.getTime());
      documentRepository.createDocument(session, notWarningDoc4);
      documentRepository.storeContent(session, notWarningDoc4, content);
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
   */
  @Test
  public void testMoveDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
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
   */
  @Test
  public void testCopyDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
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
   */
  @Test
  public void testFindDocumentByOldSilverpeasId() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      long oldSilverpeasId = 236L;
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(236L);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.setNodeName(SimpleDocument.ATTACHMENT_PREFIX + document.getOldSilverpeasId());
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(session, document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(512L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument otherDocument = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, otherDocument);
      documentRepository.storeContent(session, document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(236L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument versionedDocument = new SimpleDocument(emptyId, foreignId, 0, true, attachment);
      versionedDocument.setNodeName(SimpleDocument.VERSION_PREFIX + versionedDocument.getOldSilverpeasId());
      documentRepository.createDocument(session, versionedDocument);
      documentRepository.storeContent(session, document, content);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId,
          oldSilverpeasId, false, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(236L));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc, SimpleDocumentMatcher.matches(document));
      doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId, oldSilverpeasId,
          true,
          language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(236L));
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
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument warningDoc1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      warningDoc1.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc1);
      documentRepository.storeContent(session, warningDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notWarningDoc2);
      documentRepository.storeContent(session, notWarningDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument warningDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc3);
      documentRepository.storeContent(session, warningDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notWarningDoc4.setAlert(beforeDate.getTime());
      documentRepository.createDocument(session, notWarningDoc4);
      documentRepository.storeContent(session, notWarningDoc4, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsRequiringWarning(session, today.
          getTime(),
          null);
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(warningDoc1, warningDoc3));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
}
