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

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.model.impl.AbstractJcrRegisteringTestCase;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.util.MimeTypes;
import com.stratelia.webactiv.util.DBUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.silverpeas.jcrutil.JcrConstants.NT_FOLDER;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pure-memory-jcr.xml"})
public class DocumentRepositoryTest {

  private static final String instanceId = "kmelia73";
  private boolean registred = false;
  @Resource
  private Repository repository;
  private static EmbeddedDatabase dataSource;
  private DocumentRepository instance = new DocumentRepository();

  public Repository getRepository() {
    return this.repository;
  }

  public DocumentRepositoryTest() {
  }

  @Before
  public void setUp() throws RepositoryException, ParseException, IOException, SQLException {
    if (!registred) {
      String cndFileName = AbstractJcrRegisteringTestCase.class.getClassLoader().getResource(
          "silverpeas-jcr.txt").getFile().toString().replaceAll("%20", " ");
      SilverpeasRegister.registerNodeTypes(cndFileName);
      registred = true;
      DBUtil.getInstanceForTest(dataSource.getConnection());
    } else {
      System.out.println(" -> node types already registered!");
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
  public static void tearDown() {
    dataSource.shutdown();
    DBUtil.clearTestInstance();
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
      String description = "This is a test document";
      String creatorId = "0";

      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = instance.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(1L));
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
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = instance.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getOldSilverpeasId(), is(not(0L)));
      assertThat(doc.getCreated(), is(creationDate));
      instance.deleteDocument(session, expResult);
      doc = instance.findDocumentById(session, expResult, language);
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
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = instance.findDocumentById(session, expResult, "en");
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
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      long oldSilverpeasId = document.getOldSilverpeasId();
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      foreignId = "node18";
      document = new SimpleDocument(emptyId, foreignId, 5, false, attachment);
      result = instance.createDocument(session, document, content);
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      session.save();
      SimpleDocument doc = instance.findLast(session, instanceId, foreignId);
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
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.updateDocument(session, document);
      session.save();
      SimpleDocument doc = instance.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(doc.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));

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
      instance.createDocument(session, docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.createDocument(session, docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      foreignId = "node25";
      SimpleDocument docNode25_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      instance.createDocument(session, docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.createDocument(session, docNode25_2, content);
      List<SimpleDocument> docs = instance.listDocumentsByForeignId(session, instanceId, "node18", "fr");
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
      instance.createDocument(session, docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.createDocument(session, docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      foreignId = "node25";
      SimpleDocument docNode25_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      instance.createDocument(session, docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.createDocument(session, docNode25_2, content);
      NodeIterator nodes = instance.selectDocumentsByForeignId(session, instanceId, "node18");
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), isOneOf(docNode18_1.getId(), docNode18_2.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), isOneOf(docNode18_1.getId(), docNode18_2.getId()));
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
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      instance.createDocument(session, docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.createDocument(session, docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      foreignId = "node25";
      SimpleDocument docNode25_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      instance.createDocument(session, docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      instance.createDocument(session, docNode25_2, content);
      NodeIterator nodes = instance.selectDocumentsByForeignId(session, instanceId, "node18");
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), isOneOf(docNode18_1.getId(), docNode18_2.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), isOneOf(docNode18_1.getId(), docNode18_2.getId()));
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
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      instance.addContent(session, result, attachment, content);
      session.save();
      SimpleDocument doc = instance.findDocumentById(session, result, "fr");
      checkFrenchSimpleDocument(doc);
      doc = instance.findDocumentById(session, result, "en");
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
      SimpleDocumentPK result = instance.createDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      instance.addContent(session, result, attachment, content);
      session.save();
      instance.removeContent(session, result, "fr");
      SimpleDocument doc = instance.findDocumentById(session, result, "fr");
      checkEnglishSimpleDocument(doc);
      doc = instance.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  private SimpleAttachment createEnglishSimpleAttachment() {
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String description = "This is a test document";
    String status = "my status";
    String formId = "18";
    String creatorId = "0";
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date reservationDate = RandomGenerator.getRandomCalendar().getTime();
    Date alertDate = RandomGenerator.getRandomCalendar().getTime();
    Date expiryDate = RandomGenerator.getRandomCalendar().getTime();
    return new SimpleAttachment(fileName, language, title, description,
        "This is a test".getBytes(Charsets.UTF_8).length, MimeTypes.PDF_MIME_TYPE, creatorId,
        creationDate, reservationDate, alertDate, expiryDate, status, formId);

  }

  private SimpleAttachment createFrenchSimpleAttachment() {
    String language = "fr";
    String fileName = "test.odp";
    String title = "Mon document de test";
    String description = "Ceci est un document de test";
    String status = "Mon status";
    String formId = "5";
    String creatorId = "10";
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date reservationDate = RandomGenerator.getRandomCalendar().getTime();
    Date alertDate = RandomGenerator.getRandomCalendar().getTime();
    Date expiryDate = RandomGenerator.getRandomCalendar().getTime();
    return new SimpleAttachment(fileName, language, title, description,
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        creatorId, creationDate, reservationDate, alertDate, expiryDate, status, formId);
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
}
