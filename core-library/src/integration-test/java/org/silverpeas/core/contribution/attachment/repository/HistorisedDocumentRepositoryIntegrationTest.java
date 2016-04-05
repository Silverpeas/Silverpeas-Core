/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package org.silverpeas.core.contribution.attachment.repository;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentVersion;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationTest;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.MimeTypes;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;

@RunWith(Arquillian.class)
public class HistorisedDocumentRepositoryIntegrationTest extends JcrIntegrationTest {

  private static final String instanceId = "kmelia73";
  private DocumentRepository documentRepository = new DocumentRepository();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DocumentRepositoryIntegrationTest.class)
        .addJcrFeatures()
        .build();
  }

  @Before
  public void loadJcr() throws Exception {
    try (JcrSession session = openSystemSession()) {
      if (!session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().addNode(instanceId, NT_FOLDER);
      }
      session.save();
    }
  }

  /**
   * Test of createDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCreateDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
    }
  }

  /**
   * Test of deleteDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testDeleteDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      documentRepository.deleteDocument(session, expResult);
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(nullValue()));
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testFindDocumentById() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
    }
  }

  /**
   * Test of findLast method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testFindLast() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testUpdateDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      HistorisedDocument doc = (HistorisedDocument) documentRepository.findDocumentById(session,
          result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(2));
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListDocumentsByForeignId() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
    }
  }

  /**
   * Test of selectDocumentsByForeignId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectDocumentsByForeignId() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      NodeIterator nodes = documentRepository.selectDocumentsByForeignIdAndType(session, instanceId,
          "node78", DocumentType.attachment);
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docNode18_1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docNode18_2.getId()));
    }
  }

  /**
   * Test of selectDocumentsByOwnerId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectDocumentsByOwnerId() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      NodeIterator nodes = documentRepository.selectDocumentsByOwnerIdAndComponentId(session,
          instanceId, "10");
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docOwn10_1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docOwn10_2.getId()));
    }
  }

  /**
   * Test of addContent method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testAddContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      document.setPK(result);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.addContent(session, result, attachment);
      documentRepository.storeContent(document, content);
      session.save();
      documentRepository.unlock(session, document, false);
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkFrenchSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of removeContent method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testRemoveContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchVersionnedAttachment();
      document.setPK(result);
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.addContent(session, result, attachment);
      documentRepository.storeContent(document, content);
      session.save();
      documentRepository.unlock(session, document, false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.removeContent(session, result, "fr");
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkEnglishSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    }
  }

  private SimpleAttachment createEnglishVersionnedAttachment() {
    return new SimpleAttachment("test.pdf", "en", "My test document", "This is a test document",
        14L, MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18");
  }

  private SimpleAttachment createFrenchVersionnedAttachment() {
    return new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", 28L, MimeTypes.MIME_TYPE_OO_PRESENTATION, "10",
        RandomGenerator.getRandomCalendar().getTime(), "5");
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
   *
   * @throws Exception
   */
  @Test
  public void testListDocumentsByOwner() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
          .listComponentDocumentsByOwner(session, instanceId, owner, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docOwn25_1, docOwn25_2));
    }
  }

  /**
   * Test of listExpiringDocumentsByOwner method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListExpiringDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      String owner = "10";
      Calendar today = Calendar.getInstance();
      DateUtil.setAtBeginOfDay(today);
      SimpleDocument expiringDoc1
          = new HistorisedDocument(emptyId, foreignId, 10, owner, attachment);
      expiringDoc1.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc1, content);
      expiringDoc1.setMajorVersion(1);
      documentRepository.lock(session, expiringDoc1, owner);
      documentRepository.updateDocument(session, expiringDoc1, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notExpiringDoc2, content);
      notExpiringDoc2.setMajorVersion(1);
      documentRepository.lock(session, notExpiringDoc2, owner);
      documentRepository.updateDocument(session, notExpiringDoc2, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument expiringDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc3, content);
      expiringDoc3.setMajorVersion(1);
      documentRepository.lock(session, expiringDoc3, owner);
      documentRepository.updateDocument(session, expiringDoc3, true);
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
      documentRepository.updateDocument(session, notExpiringDoc4, true);
      session.save();
      List<SimpleDocument> docs = documentRepository.listExpiringDocuments(session, today.getTime(),
          "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(expiringDoc1, expiringDoc3));
    }
  }

  /**
   * Test of selectExpiringDocuments method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectExpiringDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.updateDocument(session, expiringDoc1, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notExpiringDoc2, content);
      documentRepository.lock(session, notExpiringDoc2, owner);
      documentRepository.updateDocument(session, notExpiringDoc2, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      SimpleDocument expiringDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      createVersionedDocument(session, expiringDoc3, content);
      documentRepository.lock(session, expiringDoc3, owner);
      documentRepository.updateDocument(session, expiringDoc3, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notExpiringDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      createVersionedDocument(session, notExpiringDoc4, content);
      documentRepository.lock(session, notExpiringDoc4, owner);
      documentRepository.updateDocument(session, notExpiringDoc4, true);
      session.save();
      NodeIterator nodes = documentRepository.selectExpiringDocuments(session, today.getTime());
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(expiringDoc1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(expiringDoc3.getId()));
    }
  }

  /**
   * Test of listDocumentsToUnlock method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testListDocumentsToUnlock() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.updateDocument(session, docToLeaveLocked1, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock2
          = new HistorisedDocument(emptyId, foreignId, 15, owner, attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock2, content);
      docToUnlock2.setMajorVersion(1);
      documentRepository.lock(session, docToUnlock2, owner);
      documentRepository.updateDocument(session, docToUnlock2, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock3, content);
      docToUnlock3.setMajorVersion(1);
      documentRepository.lock(session, docToUnlock3, owner);
      documentRepository.updateDocument(session, docToUnlock3, true);
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
      documentRepository.updateDocument(session, docToLeaveLocked4, true);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsToUnlock(session, today.getTime(),
          "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(docToUnlock2, docToUnlock3));
    }
  }

  /**
   * Test of selectDocumentsRequiringUnlocking method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectDocumentsRequiringUnlocking() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.updateDocument(session, docToLeaveLocked1, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock2, content);
      docToUnlock2.setMajorVersion(1);
      documentRepository.lock(session, docToUnlock2, owner);
      documentRepository.updateDocument(session, docToUnlock2, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument docToUnlock3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      createVersionedDocument(session, docToUnlock3, content);
      docToUnlock3.setMajorVersion(1);
      documentRepository.lock(session, docToUnlock3, owner);
      documentRepository.updateDocument(session, docToUnlock3, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, docToLeaveLocked4);
      documentRepository.storeContent(docToLeaveLocked4, content);
      documentRepository.lock(session, docToLeaveLocked4, owner);
      documentRepository.updateDocument(session, docToLeaveLocked4, true);
      session.save();
      NodeIterator nodes = documentRepository.selectDocumentsRequiringUnlocking(session, today.
          getTime());
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docToUnlock2.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docToUnlock3.getId()));
    }
  }

  /**
   * Test of selectWarningDocuments method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testSelectWarningDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.createDocument(session, warningDoc1);
      documentRepository.storeContent(warningDoc1, content);
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
      documentRepository.createDocument(session, warningDoc3);
      documentRepository.storeContent(warningDoc3, content);
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
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testMoveDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
       */

      String targetInstanceId = "kmelia36";
      String targetForeignId = "foreignId45";
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(false));
      SimpleDocumentPK result = documentRepository
          .moveDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      try {
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        session.getNode(document.getFullJcrPath());
        fail("The JCR path " + document.getFullJcrPath() + " should not longer exist...");
      } catch (PathNotFoundException ex) {
        // OK
      }
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(1));
      assertThat(doc.getRepositoryPath(), is("/kmelia36/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(doc.getId()).isCheckedOut(), is(false));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      document.getPk().setComponentName(targetInstanceId);
      document.setForeignId(targetForeignId);
      document.setVersionIndex(1);
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Version path pattern
      assertThat(document.getVersionMaster().getId(), is(doc.getVersionMaster().getId()));
      String masterUuid = doc.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" + masterUuid.substring(4, 6) + "/" + masterUuid +
              "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getFunctionalHistory(), hasSize(0));


      SimpleDocumentVersion resultHistory = doc.getHistory().remove(0);
      assertThat(resultHistory.getVersionIndex(), is(0));
      assertThat(resultHistory.getRepositoryPath(), is(String.format(versionPathPattern, "1.0")));
      assertThat(resultHistory.getPk(), not(sameInstance(resultHistory.getRealVersionPk())));
      assertThat(resultHistory.getPk().getId(), is(resultHistory.getRealVersionPk().getId()));
      assertThat(resultHistory.getPk().getComponentName(), is(targetInstanceId));
      assertThat(resultHistory.getRealVersionPk().getComponentName(), is(instanceId));
      assertThat(resultHistory.getPk().getOldSilverpeasId(),
          is(resultHistory.getRealVersionPk().getOldSilverpeasId()));
      assertThat(resultHistory.getForeignId(), is(targetForeignId));
      assertThat(resultHistory.getRealVersionForeignId(), is(foreignId));
      document.getPk().setId(resultHistory.getId());
      document.setVersionIndex(resultHistory.getVersionIndex());
      document.setOrder(resultHistory.getOrder());
      assertSimpleDocumentsAreEquals(resultHistory, document, ArrayUtils
          .addAll(ignoredBeanPropertiesInComparison, "class", "universalURL", "webdavUrl",
              "webdavJcrPath", "onlineURL"));
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testMoveDocumentWithCheckOutStateAtTrue() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(false));

      documentRepository.lock(session, document, document.getEditedBy());
      session.save();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(true));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
       */

      String targetInstanceId = "kmelia36";
      String targetForeignId = "foreignId45";
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(true));
      SimpleDocumentPK result = documentRepository
          .moveDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      try {
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        session.getNode(document.getFullJcrPath());
        fail("The JCR path " + document.getFullJcrPath() + " should not longer exist...");
      } catch (PathNotFoundException ex) {
        // OK
      }
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(0));
      assertThat(doc.getRepositoryPath(), is("/kmelia36/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(doc.getId()).isCheckedOut(), is(true));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      document.getPk().setComponentName(targetInstanceId);
      document.setForeignId(targetForeignId);
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Version path pattern
      assertThat(document.getVersionMaster().getId(), is(doc.getVersionMaster().getId()));
      String masterUuid = doc.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" + masterUuid.substring(4, 6) + "/" + masterUuid +
              "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(0));
      assertThat(doc.getFunctionalHistory(), hasSize(0));

    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   * @throws Exception
   */
  @Test
  public void testMoveDocumentChangingFunctionalVersion() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      // Version path pattern
      String masterUuid = document.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(1));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         0.1         update private
       */

      String targetInstanceId = "kmelia36";
      String targetForeignId = "foreignId45";
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(false));
      SimpleDocumentPK result = documentRepository
          .moveDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      try {
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        session.getNode(document.getFullJcrPath());
        fail("The JCR path " + document.getFullJcrPath() + " should not longer exist...");
      } catch (PathNotFoundException ex) {
        // OK
      }
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(2));
      assertThat(doc.getVersionIndex(), is(2));
      assertThat(doc.getRepositoryPath(), is("/kmelia36/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(doc.getId()).isCheckedOut(), is(false));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      document.getPk().setComponentName(targetInstanceId);
      document.setForeignId(targetForeignId);
      document.setVersionIndex(2);
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Version path pattern
      assertThat(document.getVersionMaster().getId(), is(doc.getVersionMaster().getId()));
      masterUuid = doc.getVersionMaster().getId();
      versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
          masterUuid.substring(2, 4) + "/" +
          masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(2));
      assertThat(doc.getFunctionalHistory(), hasSize(1));


      SimpleDocumentVersion currentVersion = doc.getHistory().remove(0);
      assertThat(currentVersion.getVersionIndex(), is(1));
      assertThat(currentVersion.getRepositoryPath(), is(String.format(versionPathPattern, "1.1")));
      assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
      assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
      assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
      assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
      assertThat(currentVersion.getPk().getOldSilverpeasId(),
          is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
      assertThat(currentVersion.getForeignId(), is(targetForeignId));
      assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
      document.getPk().setId(currentVersion.getId());
      document.setVersionIndex(currentVersion.getVersionIndex());
      document.setOrder(currentVersion.getOrder());
      assertSimpleDocumentsAreEquals(currentVersion, document, ArrayUtils
          .addAll(ignoredBeanPropertiesInComparison, "class", "universalURL", "webdavUrl",
              "webdavJcrPath", "onlineURL"));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      int versionIndex = doc.getHistory().size();
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
        assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   * @throws Exception
   */
  @Test
  public void testMoveDocumentWithoutChangingFunctionalVersion() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      // Version path pattern
      String masterUuid = document.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      document.setOrder(10);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getFullJcrPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         0.1         change order
       */

      String targetInstanceId = "kmelia36";
      String targetForeignId = "foreignId45";
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(false));
      SimpleDocumentPK result = documentRepository
          .moveDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      try {
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        session.getNode(document.getFullJcrPath());
        fail("The JCR path " + document.getFullJcrPath() + " should not longer exist...");
      } catch (PathNotFoundException ex) {
        // OK
      }
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(2));
      assertThat(doc.getRepositoryPath(), is("/kmelia36/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(doc.getId()).isCheckedOut(), is(false));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      document.getPk().setComponentName(targetInstanceId);
      document.setForeignId(targetForeignId);
      document.setVersionIndex(2);
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Version path pattern
      assertThat(document.getVersionMaster().getId(), is(doc.getVersionMaster().getId()));
      masterUuid = doc.getVersionMaster().getId();
      versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
          masterUuid.substring(2, 4) + "/" +
          masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(2));
      assertThat(doc.getFunctionalHistory(), hasSize(0));


      SimpleDocumentVersion currentVersion = doc.getHistory().remove(0);
      assertThat(currentVersion.getVersionIndex(), is(1));
      assertThat(currentVersion.getRepositoryPath(), is(String.format(versionPathPattern, "1.1")));
      assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
      assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
      assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
      assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
      assertThat(currentVersion.getPk().getOldSilverpeasId(),
          is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
      assertThat(currentVersion.getForeignId(), is(targetForeignId));
      assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
      document.getPk().setId(currentVersion.getId());
      document.setVersionIndex(currentVersion.getVersionIndex());
      document.setOrder(currentVersion.getOrder());
      assertSimpleDocumentsAreEquals(currentVersion, document, ArrayUtils
          .addAll(ignoredBeanPropertiesInComparison, "class", "universalURL", "webdavUrl",
              "webdavJcrPath", "onlineURL"));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      int versionIndex = doc.getHistory().size();
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
        assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   * @throws Exception
   */
  @Test
  public void testMoveDocumentWithHugeHistory() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      // Version path pattern
      String masterUuid = document.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      document.setOrder(10);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getFullJcrPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(2));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(1));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(2));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.1")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(3));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(2));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(3));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.2")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(4));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(3));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(4));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.3")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(5));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(5));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.4")));

      document.setOrder(0);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(6));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(6));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.5")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(7));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(5));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(7));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.6")));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         0.1         change order
      1.2         0.1         update private
      1.3         0.2         update public
      1.4         1.0         update private
      1.5         1.1         update private
      1.6         1.1         change order
      1.7         1.2         update public
       */

      String targetInstanceId = "kmelia36";
      String targetForeignId = "foreignId45";
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(false));
      SimpleDocumentPK result = documentRepository
          .moveDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      try {
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        session.getNode(document.getFullJcrPath());
        fail("The JCR path " + document.getFullJcrPath() + " should not longer exist...");
      } catch (PathNotFoundException ex) {
        // OK
      }
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(2));
      assertThat(doc.getMinorVersion(), is(0));
      assertThat(doc.getVersionIndex(), is(8));
      assertThat(doc.getRepositoryPath(), is("/kmelia36/attachments/simpledoc_1"));
      assertThat(session.getNodeByIdentifier(doc.getId()).isCheckedOut(), is(false));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "versionMaster", "realVersionPk", "realVersionForeignId",
              "previousVersion"};
      document.getPk().setComponentName(targetInstanceId);
      document.setForeignId(targetForeignId);
      document.setVersionIndex(8);
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Version path pattern
      assertThat(document.getVersionMaster().getId(), is(doc.getVersionMaster().getId()));
      masterUuid = doc.getVersionMaster().getId();
      versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
          masterUuid.substring(2, 4) + "/" +
          masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(8));
      assertThat(doc.getFunctionalHistory(), hasSize(5));


      SimpleDocumentVersion currentVersion = doc.getHistory().remove(0);
      assertThat(currentVersion.getVersionIndex(), is(7));
      assertThat(currentVersion.getRepositoryPath(), is(String.format(versionPathPattern, "1.7")));
      assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
      assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
      assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
      assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
      assertThat(currentVersion.getPk().getOldSilverpeasId(),
          is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
      assertThat(currentVersion.getForeignId(), is(targetForeignId));
      assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
      document.getPk().setId(currentVersion.getId());
      document.setVersionIndex(currentVersion.getVersionIndex());
      assertSimpleDocumentsAreEquals(currentVersion, document, ArrayUtils
          .addAll(ignoredBeanPropertiesInComparison, "class", "universalURL", "webdavUrl",
              "webdavJcrPath", "onlineURL"));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      int versionIndex = doc.getHistory().size();
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
        assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   * @throws Exception
   */
  @Test
  public void testMoveDocumentWithHugeHistoryTwoLanguagesBeforeVersions() throws Exception {
    FileUtils.deleteQuietly(new File(FileRepositoryManager.getAbsolutePath("")));
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Map<String, Date> creationDateByLanguage = new HashMap<String, Date>();
      creationDateByLanguage.put("en", attachment.getCreated());
      String foreignId = "node78";
      SimpleDocument initialDocument = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      initialDocument.setContentType(MimeTypes.PDF_MIME_TYPE);
      initialDocument.setPublicDocument(false);
      createVersionedDocument(session, initialDocument, content);
      // French content before version
      initialDocument.setLanguage("fr");
      initialDocument.setAttachment(createFrenchVersionnedAttachment());
      creationDateByLanguage.put("fr", initialDocument.getAttachment().getCreated());
      documentRepository.updateDocument(session, initialDocument, true);
      session.save();

      SimpleDocumentPK sourcePk = initialDocument.getPk();
      SimpleDocument frDocument = documentRepository.findDocumentById(session, sourcePk, "fr");
      SimpleDocument enDocument = documentRepository.findDocumentById(session, sourcePk, "en");
      assertThat(frDocument, not(instanceOf(HistorisedDocument.class)));
      assertThat(frDocument.getLanguage(), is("fr"));
      assertThat(frDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_fr"));
      assertThat(frDocument.getVersion(), is("0.0"));
      assertThat(enDocument, not(instanceOf(HistorisedDocument.class)));
      assertThat(enDocument.getLanguage(), is("en"));
      assertThat(enDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_en"));
      assertThat(enDocument.getVersion(), is("0.0"));

      // Changing document to version management
      documentRepository.changeVersionState(session, sourcePk, "Changing to version...");
      session.save();

      HistorisedDocument frHistorisedDocument =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "fr");
      HistorisedDocument enHistorisedDocument =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "en");
      assertThat(frHistorisedDocument.getLanguage(), is("fr"));
      assertThat(frHistorisedDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_fr"));
      assertThat(frHistorisedDocument.getVersion(), is("1.0"));
      assertThat(enHistorisedDocument.getLanguage(), is("en"));
      assertThat(enHistorisedDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_en"));
      assertThat(enHistorisedDocument.getVersion(), is("1.0"));

      // Version path pattern
      String masterUuid = initialDocument.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      HistorisedDocument document = enHistorisedDocument;
      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      document.setOrder(10);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getFullJcrPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(2));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(1));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(2));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.1")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(3));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(2));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(3));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.2")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(4));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(3));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(4));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.3")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(5));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(5));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.4")));

      document.setOrder(0);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(6));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(6));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.5")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(7));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(5));
      assertThat(document.getMajorVersion(), is(3));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(7));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.6")));

      Map<String, HistorisedDocument> historisedDocumentByLanguage =
          new HashMap<String, HistorisedDocument>();
      historisedDocumentByLanguage.put("fr",
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "fr"));
      historisedDocumentByLanguage.put("en",
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "en"));
      assertThat(historisedDocumentByLanguage.get("fr").getLanguage(), is("fr"));
      assertThat(historisedDocumentByLanguage.get("en").getLanguage(), is("en"));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         1.1         change order
      1.2         1.1         update private
      1.3         1.2         update public
      1.4         2.0         update private
      1.5         2.1         update private
      1.6         2.1         change order
      1.7         2.2         update public
       */

      String targetInstanceId = "kmelia36";
      String targetForeignId = "foreignId45";
      assertThat(session.getNodeByIdentifier(document.getId()).isCheckedOut(), is(false));
      SimpleDocumentPK result = documentRepository
          .moveDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      try {
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        session.getNode(document.getFullJcrPath());
        fail("The JCR path " + document.getFullJcrPath() + " should not longer exist...");
      } catch (PathNotFoundException ex) {
        // OK
      }
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      for (String lang : new String[]{"fr", "en"}) {
        document = historisedDocumentByLanguage.get(lang);
        HistorisedDocument doc =
            (HistorisedDocument) documentRepository.findDocumentById(session, expResult, lang);
        assertThat(doc.getLanguage(), is(lang));
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
        assertThat(doc.getCreated(), is(creationDateByLanguage.get(lang)));
        if (lang.equals("fr")) {
          checkFrenchSimpleDocument(doc);
        } else {
          checkEnglishSimpleDocument(doc);
        }
        assertThat(doc.getMajorVersion(), is(3));
        assertThat(doc.getMinorVersion(), is(0));
        assertThat(doc.getVersionIndex(), is(8));
        assertThat(doc.getRepositoryPath(), is("/kmelia36/attachments/simpledoc_1"));
        assertThat(session.getNodeByIdentifier(doc.getId()).isCheckedOut(), is(false));

        String[] ignoredBeanPropertiesInComparison =
            new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
                "publicVersions", "versionMaster", "realVersionPk", "realVersionForeignId",
                "previousVersion"};
        document.getPk().setComponentName(targetInstanceId);
        document.setForeignId(targetForeignId);
        document.setVersionIndex(8);
        assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

        // Version path pattern
        assertThat(document.getVersionMaster().getId(), is(doc.getVersionMaster().getId()));
        masterUuid = doc.getVersionMaster().getId();
        versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
            masterUuid.substring(2, 4) + "/" +
            masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

        // History
        assertThat(doc.getHistory(), hasSize(8));
        assertThat(doc.getFunctionalHistory(), hasSize(5));


        SimpleDocumentVersion currentVersion = doc.getHistory().remove(0);
        assertThat(currentVersion.getVersionIndex(), is(7));
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1.7")));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
        assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
        document.getPk().setId(currentVersion.getId());
        document.setVersionIndex(currentVersion.getVersionIndex());
        assertSimpleDocumentsAreEquals(currentVersion, document, ArrayUtils
            .addAll(ignoredBeanPropertiesInComparison, "class", "universalURL", "webdavUrl",
                "webdavJcrPath", "onlineURL"));

        for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
          expectedVersion.setVersionMaster(doc.getVersionMaster());
        }
        int versionIndex = doc.getHistory().size();
        Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
        for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
          currentVersion = resultHistoryIt.next();
          assertThat(currentVersion.getRepositoryPath(), currentVersion.getLanguage(), is(lang));
          assertThat(currentVersion.getRepositoryPath(),
              is(String.format(versionPathPattern, "1." + (--versionIndex))));
          assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
          assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
          assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
          assertThat(currentVersion.getPk().getComponentName(), is(targetInstanceId));
          assertThat(currentVersion.getRealVersionPk().getComponentName(), is(instanceId));
          assertThat(currentVersion.getPk().getOldSilverpeasId(),
              is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
          assertThat(currentVersion.getForeignId(), is(targetForeignId));
          assertThat(currentVersion.getRealVersionForeignId(), is(foreignId));
          assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
              ignoredBeanPropertiesInComparison);
        }
      }
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCopyDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
       */

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(targetForeignId);
      document.setPK(result);
      document.setNodeName(doc.getNodeName());
      document.setUpdatedBy(null);
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(0));
      assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Reloading original for following assertions.
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);
      assertThat(document.getVersionMaster().getId(), is(not(doc.getVersionMaster().getId())));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      // History
      assertThat(doc.getHistory(), hasSize(0));
      assertThat(doc.getFunctionalHistory(), hasSize(0));
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCopyReservedDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getEditedBy(), nullValue());

      // Simulating a reservation
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      versionManager.checkout(document.getFullJcrPath());
      document.edit("26");
      documentRepository.updateDocument(session, document, true);
      session.save();
      versionManager.checkin(document.getFullJcrPath());

      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getEditedBy(), is("26"));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         1.0         reservation
       */

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(targetForeignId);
      document.setPK(result);
      document.setNodeName(doc.getNodeName());
      document.setUpdatedBy("26");
      document.release();
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(1));
      assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));
      assertThat(doc.getEditedBy(), nullValue());

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Reloading original for following assertions.
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);
      assertThat(document.getVersionMaster().getId(), is(not(doc.getVersionMaster().getId())));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      // Version path pattern
      String masterUuid = doc.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getFunctionalHistory(), hasSize(0));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      int versionIndex = doc.getHistory().size();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        SimpleDocumentVersion currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(),
            is(currentVersion.getRealVersionPk().getComponentName()));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getForeignId(), is(currentVersion.getRealVersionForeignId()));
        expectedVersion.setId(currentVersion.getId());
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   * @throws Exception
   */
  @Test
  public void testCopyDocumentChangingFunctionalVersion() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      // Version path pattern
      String masterUuid = document.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(1));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         0.1         update private
       */

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(targetForeignId);
      document.setPK(result);
      document.setNodeName(doc.getNodeName());
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(2));
      assertThat(doc.getVersionIndex(), is(1));
      assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Reloading original for following assertions.
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getVersionMaster().getId(), is(not(doc.getVersionMaster().getId())));

      // Version path pattern
      masterUuid = doc.getVersionMaster().getId();
      versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
          masterUuid.substring(2, 4) + "/" +
          masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getFunctionalHistory(), hasSize(1));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      int versionIndex = doc.getHistory().size();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        SimpleDocumentVersion currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(),
            is(currentVersion.getRealVersionPk().getComponentName()));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getForeignId(), is(currentVersion.getRealVersionForeignId()));
        expectedVersion.setId(currentVersion.getId());
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCopyDocumentWithoutChangingFunctionalVersion() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      // Version path pattern
      String masterUuid = document.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      document.setOrder(10);
      documentRepository.setOrder(session, document);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getFullJcrPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         0.1         change order
       */

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(targetForeignId);
      document.setPK(result);
      document.setNodeName(doc.getNodeName());
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(1));
      assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Reloading original for following assertions.
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getVersionMaster().getId(), is(not(doc.getVersionMaster().getId())));

      // Version path pattern
      masterUuid = doc.getVersionMaster().getId();
      versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
          masterUuid.substring(2, 4) + "/" +
          masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getFunctionalHistory(), hasSize(0));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      int versionIndex = doc.getHistory().size();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        SimpleDocumentVersion currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(),
            is(currentVersion.getRealVersionPk().getComponentName()));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getForeignId(), is(currentVersion.getRealVersionForeignId()));
        expectedVersion.setId(currentVersion.getId());
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testCopyDocumentWithHugeHistory() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node78";
      HistorisedDocument document = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      document.setPublicDocument(false);
      createVersionedDocument(session, document, content);
      SimpleDocumentPK sourcePk = document.getPk();
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      // Version path pattern
      String masterUuid = document.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      document.setOrder(10);
      documentRepository.setOrder(session, document);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getFullJcrPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(2));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(1));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(2));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.1")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(3));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(2));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(3));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.2")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(4));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(3));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(4));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.3")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(5));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(5));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.4")));

      document.setOrder(0);
      documentRepository.setOrder(session, document);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(6));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(6));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.5")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document = (HistorisedDocument) documentRepository
          .findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(7));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(5));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(7));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.6")));

      /*
      History :
      Technical   Functional  Action
      1.0                     initialization
      1.1         0.1         change order
      1.2         0.1         update private
      1.3         0.2         update public
      1.4         1.0         update private
      1.5         1.1         update private
      1.6         1.1         change order
      1.7         1.2         update public
       */

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(targetForeignId);
      document.setPK(result);
      document.setNodeName(doc.getNodeName());
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
      assertThat(doc.getMajorVersion(), is(2));
      assertThat(doc.getMinorVersion(), is(0));
      assertThat(doc.getVersionIndex(), is(7));
      assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
              "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
              "realVersionForeignId", "previousVersion"};
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

      // Reloading original for following assertions.
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getVersionMaster().getId(), is(not(doc.getVersionMaster().getId())));

      // Version path pattern
      masterUuid = doc.getVersionMaster().getId();
      versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
          masterUuid.substring(2, 4) + "/" +
          masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // History
      assertThat(doc.getHistory(), hasSize(7));
      assertThat(doc.getFunctionalHistory(), hasSize(5));

      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        expectedVersion.setVersionMaster(doc.getVersionMaster());
      }
      Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
      int versionIndex = doc.getHistory().size();
      for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
        SimpleDocumentVersion currentVersion = resultHistoryIt.next();
        assertThat(currentVersion.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (--versionIndex))));
        assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
        assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
        assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
        assertThat(currentVersion.getPk().getComponentName(),
            is(currentVersion.getRealVersionPk().getComponentName()));
        assertThat(currentVersion.getPk().getOldSilverpeasId(),
            is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
        assertThat(currentVersion.getForeignId(), is(targetForeignId));
        assertThat(currentVersion.getForeignId(), is(currentVersion.getRealVersionForeignId()));
        expectedVersion.setId(currentVersion.getId());
        assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
            ignoredBeanPropertiesInComparison);
      }
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   * @throws Exception
   */
  @Test
  public void testCopyDocumentWithHugeHistoryTwoLanguagesBeforeVersions() throws Exception {
    FileUtils.deleteQuietly(new File(FileRepositoryManager.getAbsolutePath("")));
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      Map<String, Date> creationDateByLanguage = new HashMap<String, Date>();
      creationDateByLanguage.put("en", attachment.getCreated());
      String foreignId = "node78";
      SimpleDocument initialDocument = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      initialDocument.setContentType(MimeTypes.PDF_MIME_TYPE);
      initialDocument.setPublicDocument(false);
      createVersionedDocument(session, initialDocument, content);
      // French content before version
      initialDocument.setLanguage("fr");
      initialDocument.setAttachment(createFrenchVersionnedAttachment());
      creationDateByLanguage.put("fr", initialDocument.getAttachment().getCreated());
      documentRepository.updateDocument(session, initialDocument, true);
      session.save();

      SimpleDocumentPK sourcePk = initialDocument.getPk();
      SimpleDocument frDocument = documentRepository.findDocumentById(session, sourcePk, "fr");
      SimpleDocument enDocument = documentRepository.findDocumentById(session, sourcePk, "en");
      assertThat(frDocument, not(instanceOf(HistorisedDocument.class)));
      assertThat(frDocument.getLanguage(), is("fr"));
      assertThat(frDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_fr"));
      assertThat(frDocument.getVersion(), is("0.0"));
      assertThat(enDocument, not(instanceOf(HistorisedDocument.class)));
      assertThat(enDocument.getLanguage(), is("en"));
      assertThat(enDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_en"));
      assertThat(enDocument.getVersion(), is("0.0"));

      // Changing document to version management
      documentRepository.changeVersionState(session, sourcePk, "Changing to version...");
      session.save();

      HistorisedDocument frHistorisedDocument =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "fr");
      HistorisedDocument enHistorisedDocument =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "en");
      assertThat(frHistorisedDocument.getLanguage(), is("fr"));
      assertThat(frHistorisedDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_fr"));
      assertThat(frHistorisedDocument.getVersion(), is("1.0"));
      assertThat(enHistorisedDocument.getLanguage(), is("en"));
      assertThat(enHistorisedDocument.getFullJcrContentPath(),
          is("/kmelia73/attachments/simpledoc_1/file_en"));
      assertThat(enHistorisedDocument.getVersion(), is("1.0"));

      // Version path pattern
      String masterUuid = initialDocument.getVersionMaster().getId();
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      HistorisedDocument document = enHistorisedDocument;
      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(0));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));

      document.setOrder(10);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(1));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(0));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(1));
      assertThat(document.getFullJcrPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.0")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(2));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(1));
      assertThat(document.getMajorVersion(), is(1));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(2));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.1")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(3));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(2));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(3));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.2")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(4));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(3));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(1));
      assertThat(document.getVersionIndex(), is(4));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.3")));

      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(10));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(5));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(5));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.4")));

      document.setOrder(0);
      documentRepository.setOrder(session, document);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(6));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(4));
      assertThat(document.getMajorVersion(), is(2));
      assertThat(document.getMinorVersion(), is(2));
      assertThat(document.getVersionIndex(), is(6));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.5")));

      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      document =
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getHistory(), is(notNullValue()));
      assertThat(document.getHistory(), hasSize(7));
      assertThat(document.getFunctionalHistory(), is(notNullValue()));
      assertThat(document.getFunctionalHistory(), hasSize(5));
      assertThat(document.getMajorVersion(), is(3));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(7));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getHistory().get(0).getRepositoryPath(),
          is(String.format(versionPathPattern, "1.6")));

      Map<String, HistorisedDocument> historisedDocumentByLanguage =
          new HashMap<String, HistorisedDocument>();
      historisedDocumentByLanguage.put("fr",
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "fr"));
      historisedDocumentByLanguage.put("en",
          (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, "en"));
      assertThat(historisedDocumentByLanguage.get("fr").getLanguage(), is("fr"));
      assertThat(historisedDocumentByLanguage.get("en").getLanguage(), is("en"));

      /*
      History :
      Technical   Functional  Action
      1.0         1.0         initialization
      1.1         1.1         change order
      1.2         1.1         update private
      1.3         1.2         update public
      1.4         2.0         update private
      1.5         2.1         update private
      1.6         2.1         change order
      1.7         2.2         update public
       */

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      for (String lang : new String[]{"fr", "en"}) {
        document = historisedDocumentByLanguage.get(lang);
        HistorisedDocument doc =
            (HistorisedDocument) documentRepository.findDocumentById(session, expResult, lang);
        assertThat(doc.getLanguage(), is(lang));
        assertThat(doc, is(notNullValue()));
        assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
        assertThat(doc.getCreated(), is(creationDateByLanguage.get(lang)));
        document.setForeignId(targetForeignId);
        document.setPK(result);
        document.setNodeName(doc.getNodeName());
        assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
        if (lang.equals("fr")) {
          checkFrenchSimpleDocument(doc);
        } else {
          checkEnglishSimpleDocument(doc);
        }
        assertThat(doc.getMajorVersion(), is(3));
        assertThat(doc.getMinorVersion(), is(0));
        assertThat(doc.getVersionIndex(), is(7));
        assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));

        String[] ignoredBeanPropertiesInComparison =
            new String[]{"history", "functionalHistory", "created", "updated", "repositoryPath",
                "publicVersions", "lastPublicVersion", "versionMaster", "realVersionPk",
                "realVersionForeignId", "previousVersion"};
        assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);

        // Reloading original for following assertions.
        document =
            (HistorisedDocument) documentRepository.findDocumentById(session, sourcePk, lang);
        assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
        assertThat(document.getVersionMaster().getId(), is(not(doc.getVersionMaster().getId())));

        // Version path pattern
        masterUuid = doc.getVersionMaster().getId();
        versionPathPattern = "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
            masterUuid.substring(2, 4) + "/" +
            masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

        // History
        assertThat(doc.getHistory(), hasSize(7));
        assertThat(doc.getFunctionalHistory(), hasSize(5));

        for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
          expectedVersion.setVersionMaster(doc.getVersionMaster());
        }
        Iterator<SimpleDocumentVersion> resultHistoryIt = doc.getHistory().iterator();
        int versionIndex = doc.getHistory().size();
        for (SimpleDocumentVersion expectedVersion : document.getHistory()) {
          SimpleDocumentVersion currentVersion = resultHistoryIt.next();
          assertThat(currentVersion.getRepositoryPath(), currentVersion.getLanguage(), is(lang));
          assertThat(currentVersion.getRepositoryPath(),
              is(String.format(versionPathPattern, "1." + (--versionIndex))));
          assertThat(currentVersion.getFullJcrPath(), is(doc.getFullJcrPath()));
          assertThat(currentVersion.getPk(), not(sameInstance(currentVersion.getRealVersionPk())));
          assertThat(currentVersion.getPk().getId(), is(currentVersion.getRealVersionPk().getId()));
          assertThat(currentVersion.getPk().getComponentName(),
              is(currentVersion.getRealVersionPk().getComponentName()));
          assertThat(currentVersion.getPk().getOldSilverpeasId(),
              is(currentVersion.getRealVersionPk().getOldSilverpeasId()));
          assertThat(currentVersion.getForeignId(), is(targetForeignId));
          assertThat(currentVersion.getForeignId(), is(currentVersion.getRealVersionForeignId()));
          expectedVersion.setId(currentVersion.getId());
          assertSimpleDocumentsAreEquals(currentVersion, expectedVersion,
              ignoredBeanPropertiesInComparison);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void assertSimpleDocumentsAreEquals(SimpleDocument result, SimpleDocument expected,
      String... ignoredBeanProperties) throws Exception {

    // SimpleDocument
    Map<String, String> expectedBeanProperties = BeanUtils.describe(expected);
    Map<String, String> resultBeanProperties = BeanUtils.describe(result);
    String resultRepoPath = resultBeanProperties.get("repositoryPath");
    List<String> toIgnoredBeanProperties = new ArrayList<String>();
    Collections.addAll(toIgnoredBeanProperties, ignoredBeanProperties);
    toIgnoredBeanProperties.add("file");
    for (String keyToRemove : toIgnoredBeanProperties) {
      expectedBeanProperties.remove(keyToRemove);
      resultBeanProperties.remove(keyToRemove);
    }
    assertThat(resultBeanProperties.size(), is(expectedBeanProperties.size()));
    for (Map.Entry<String, String> expectedEntry : expectedBeanProperties.entrySet()) {
      assertThat("ResultRepoPath (simpledoc): " + resultRepoPath, resultBeanProperties,
          hasEntry(expectedEntry.getKey(), expectedEntry.getValue()));
    }

    // Attachment
    expectedBeanProperties = BeanUtils.describe(expected.getAttachment());
    resultBeanProperties = BeanUtils.describe(result.getAttachment());
    for (String keyToRemove : toIgnoredBeanProperties) {
      expectedBeanProperties.remove(keyToRemove);
      resultBeanProperties.remove(keyToRemove);
    }
    assertThat(resultBeanProperties.size(), is(expectedBeanProperties.size()));
    for (Map.Entry<String, String> expectedEntry : expectedBeanProperties.entrySet()) {
      assertThat("ResultRepoPath (attachment): " + resultRepoPath, resultBeanProperties,
          hasEntry(expectedEntry.getKey(), expectedEntry.getValue()));
    }
  }

  /**
   * Test of findDocumentByOldSilverpeasId method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testFindDocumentByOldSilverpeasId() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      document.setNodeName(SimpleDocument.ATTACHMENT_PREFIX + document.getOldSilverpeasId());
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(1024L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument otherDocument = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      createVersionedDocument(session, otherDocument, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(2048L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument versionedDocument = new HistorisedDocument(emptyId, foreignId, 0, attachment);
      versionedDocument.setNodeName(SimpleDocument.VERSION_PREFIX + versionedDocument.
          getOldSilverpeasId());
      createVersionedDocument(session, versionedDocument, content);
      session.save();
      versionedDocument.setUpdatedBy(versionedDocument.getCreatedBy());
      versionedDocument.setMajorVersion(versionedDocument.getMajorVersion() + 1);
      SimpleDocument doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId,
          oldSilverpeasId, false, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(2048L));
      assertThat(doc.getCreated(), is(creationDate));
      document.setUpdatedBy(document.getCreatedBy());
      assertThat(doc, SimpleDocumentMatcher.matches(document));
      doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId, oldSilverpeasId,
          true, language);
      document.setUpdatedBy(null);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(2048L));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc, SimpleDocumentMatcher.matches(versionedDocument));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of listDocumentsRequiringWarning method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsRequiringWarning() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.updateDocument(session, warningDoc1, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchVersionnedAttachment();
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocument notWarningDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      createVersionedDocument(session, notWarningDoc2, content);
      notWarningDoc2.setMajorVersion(1);
      documentRepository.lock(session, notWarningDoc2, owner);
      documentRepository.updateDocument(session, notWarningDoc2, true);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishVersionnedAttachment();
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument warningDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      createVersionedDocument(session, warningDoc3, content);
      documentRepository.lock(session, warningDoc3, owner);
      documentRepository.updateDocument(session, warningDoc3, true);
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
      documentRepository.updateDocument(session, notWarningDoc4, true);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsRequiringWarning(session, today.
          getTime(), null);
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(warningDoc1, warningDoc3));
    }
  }

  /**
   * Test of saveForbiddenDownloadForRoles method, of class DocumentRepository.
   * Testing also history, functional history, repository path and version index.
   */
  @Test
  public void testSaveForbiddenDownloadForRoles() throws Exception {
    try (JcrSession session = openSystemSession()) {

      /*
      Context of this test
       */

      // Create a versioned work document
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      document.setPublicDocument(false);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.getForbiddenDownloadForRoles(), nullValue());
      HistorisedDocument docCreated =
          (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");
      assertThat(docCreated, is(notNullValue()));
      assertThat(docCreated.getOrder(), is(10));
      assertThat(docCreated.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(docCreated.getSize(), is(14L));
      assertThat(docCreated.getHistory(), is(notNullValue()));
      assertThat(docCreated.getHistory(), hasSize(0));
      assertThat(docCreated.getFunctionalHistory(), is(notNullValue()));
      assertThat(docCreated.getFunctionalHistory(), hasSize(0));
      assertThat(docCreated.getMajorVersion(), is(0));
      assertThat(docCreated.getMinorVersion(), is(1));
      assertThat(docCreated.getVersionIndex(), is(0));
      assertThat(docCreated.getVersionIndex(), is(docCreated.getVersionMaster().getVersionIndex()));

      // Verifying data are ok
      String masterUuid = docCreated.getVersionMaster().getId();
      String masterPath = docCreated.getVersionMaster().getRepositoryPath();
      assertThat(masterUuid, is(docCreated.getId()));
      assertThat(masterPath, is("/kmelia73/attachments/" + docCreated.getNodeName()));

      // Update the versioned document to a public one
      attachment = createFrenchVersionnedAttachment();
      document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      document.setPublicDocument(true);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getFunctionalHistory(), is(notNullValue()));
      assertThat(doc.getFunctionalHistory(), hasSize(1));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(1));
      assertThat(doc.getMinorVersion(), is(0));
      assertThat(doc.getVersionIndex(), is(1));
      assertThat(doc.getVersionIndex(), is(doc.getVersionMaster().getVersionIndex()));

      // Update the versioned document to a working one
      attachment = createFrenchVersionnedAttachment();
      document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      doc = (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(2));
      assertThat(doc.getFunctionalHistory(), is(notNullValue()));
      assertThat(doc.getFunctionalHistory(), hasSize(2));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(1));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(2));
      assertThat(doc.getVersionIndex(), is(doc.getVersionMaster().getVersionIndex()));

      /*
      Test starts here
       */
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.writer, SilverpeasRole.admin);
      documentRepository.saveForbiddenDownloadForRoles(session, document);

      doc = (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(3));
      assertThat(doc.getFunctionalHistory(), is(notNullValue()));
      assertThat(doc.getFunctionalHistory(), hasSize(2));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(1));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(3));
      assertThat(doc.getVersionIndex(), is(doc.getVersionMaster().getVersionIndex()));
      assertThat(doc.getForbiddenDownloadForRoles(),
          contains(SilverpeasRole.admin, SilverpeasRole.writer));
      for (SimpleDocumentVersion version : doc.getHistory()) {
        assertThat(version.getForbiddenDownloadForRoles(),
            contains(SilverpeasRole.admin, SilverpeasRole.writer));
      }

      document.addRolesForWhichDownloadIsAllowed(SilverpeasRole.writer, SilverpeasRole.admin);
      documentRepository.saveForbiddenDownloadForRoles(session, document);

      doc = (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(4));
      assertThat(doc.getFunctionalHistory(), is(notNullValue()));
      assertThat(doc.getFunctionalHistory(), hasSize(2));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(1));
      assertThat(doc.getMinorVersion(), is(1));
      assertThat(doc.getVersionIndex(), is(4));
      assertThat(doc.getVersionIndex(), is(doc.getVersionMaster().getVersionIndex()));
      assertThat(doc.getForbiddenDownloadForRoles(), nullValue());
      for (SimpleDocumentVersion version : doc.getHistory()) {
        assertThat(version.getForbiddenDownloadForRoles(), nullValue());
      }

    }
  }

  /**
   * Testing history, functional history, repository path and version index.
   */
  @Test
  public void testHistoryAndVersions() throws Exception {
    try (JcrSession session = openSystemSession()) {

      /*
      Context of this test
       */

      // Create a versioned work document
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishVersionnedAttachment();
      String foreignId = "node78";
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10, attachment);
      document.setPublicDocument(false);
      SimpleDocumentPK result = createVersionedDocument(session, document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.getForbiddenDownloadForRoles(), nullValue());
      HistorisedDocument docCreated =
          (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");
      assertThat(docCreated, is(notNullValue()));
      assertThat(docCreated.getOrder(), is(10));
      assertThat(docCreated.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(docCreated.getSize(), is(14L));
      assertThat(docCreated.getHistory(), is(notNullValue()));
      assertThat(docCreated.getHistory(), hasSize(0));
      assertThat(docCreated.getFunctionalHistory(), is(notNullValue()));
      assertThat(docCreated.getFunctionalHistory(), hasSize(0));
      assertThat(docCreated.getMajorVersion(), is(0));
      assertThat(docCreated.getMinorVersion(), is(1));
      assertThat(docCreated.getVersionIndex(), is(0));
      assertThat(docCreated.getVersionIndex(), is(docCreated.getVersionMaster().getVersionIndex()));
      assertThat(docCreated.getPreviousVersion(), nullValue());

      // Verifying data are ok
      String masterUuid = docCreated.getVersionMaster().getId();
      String masterPath = docCreated.getVersionMaster().getRepositoryPath();
      assertThat(masterUuid, is(docCreated.getId()));
      assertThat(masterPath, is("/kmelia73/attachments/" + docCreated.getNodeName()));

      // Version path pattern
      String versionPathPattern =
          "/jcr:system/jcr:versionStorage/" + masterUuid.substring(0, 2) + "/" +
              masterUuid.substring(2, 4) + "/" +
              masterUuid.substring(4, 6) + "/" + masterUuid + "/%s/jcr:frozenNode";

      // Massive data
      int minor = 0;
      for (int i = 0; i < 1100; i++) {
        if (i % 50 == 0) {
          // Functional version number is incremented (and the technical too)
          attachment = createFrenchVersionnedAttachment();
          document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
          if (minor >= 4) {
            document.setPublicDocument(true);
            minor = 0;
          } else {
            document.setPublicDocument(false);
            minor++;
          }
          documentRepository.lock(session, document, document.getEditedBy());
          documentRepository.updateDocument(session, document, true);
          session.save();
          documentRepository.unlock(session, document, false);
        }

        // Functional version number is not incremented (but still the technical)
        if (i % 2 == 0) {
          document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.writer, SilverpeasRole.admin);
        } else {
          document.addRolesForWhichDownloadIsAllowed(SilverpeasRole.writer, SilverpeasRole.admin);
        }
        documentRepository.saveForbiddenDownloadForRoles(session, document);
      }

      HistorisedDocument doc =
          (HistorisedDocument) documentRepository.findDocumentById(session, result, "fr");

      List<String> publicDocumentsIdsFromLastToFirst = new ArrayList<String>();
      VersionHistory versionHistory =
          session.getWorkspace().getVersionManager().getVersionHistory(doc.getFullJcrPath());
      NodeIterator frozenNodeIt = versionHistory.getAllFrozenNodes();
      DocumentConverter converter = new DocumentConverter();
      Version rootNode = versionHistory.getRootVersion();
      while (frozenNodeIt.hasNext()) {
        Node frozenNode = frozenNodeIt.nextNode();
        if (!frozenNode.getPath().startsWith(rootNode.getPath())) {
          SimpleDocument simpleDocument = converter.fillDocument(frozenNode, doc.getLanguage());
          if (simpleDocument.isPublic()) {
            publicDocumentsIdsFromLastToFirst.add(0, simpleDocument.getId());
          }
        }
      }

      // Testing the historised document.
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(1122));
      assertThat(doc.getFunctionalHistory(), is(notNullValue()));
      assertThat(doc.getFunctionalHistory(), hasSize(22));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(4));
      assertThat(doc.getMinorVersion(), is(2));
      assertThat(doc.getVersionIndex(), is(1122));
      assertThat(doc.getVersionIndex(), is(doc.getVersionMaster().getVersionIndex()));

      // Testing last public version.
      Iterator<String> expectedPublicVersionIt = publicDocumentsIdsFromLastToFirst.iterator();
      SimpleDocument lastPublicVersion = doc.getLastPublicVersion();
      while (lastPublicVersion != null) {
        assertThat(lastPublicVersion.getId(), is(expectedPublicVersionIt.next()));
        expectedPublicVersionIt.remove();
        assertThat(lastPublicVersion.getLastPublicVersion(), sameInstance(lastPublicVersion));
        if (lastPublicVersion instanceof HistorisedDocument) {
          lastPublicVersion =
              ((HistorisedDocument) lastPublicVersion).getPreviousVersion().getLastPublicVersion();
        } else {
          lastPublicVersion = ((SimpleDocumentVersion) lastPublicVersion).getPreviousVersion()
              .getLastPublicVersion();
        }
      }
      assertThat(publicDocumentsIdsFromLastToFirst, hasSize(0));

      // Testing technical version history.
      assertThat(doc.getPreviousVersion(), is(doc.getHistory().get(0)));
      int versionIndexExpected = doc.getVersionIndex();
      SimpleDocumentVersion previousVersion = null;
      for (SimpleDocumentVersion documentInHistory : doc.getHistory()) {
        assertThat(documentInHistory.getVersionMaster(), sameInstance(doc.getVersionMaster()));
        assertThat(documentInHistory.getVersionIndex(), is(--versionIndexExpected));
        assertThat(documentInHistory.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + (versionIndexExpected))));
        assertThat(documentInHistory.getFullJcrPath(), is(doc.getFullJcrPath()));
        if (previousVersion != null) {
          assertThat(previousVersion, is(documentInHistory));
        }
        previousVersion = documentInHistory.getPreviousVersion();
      }
      assertThat(previousVersion, nullValue());

      // Testing functional version history.
      versionIndexExpected = 1071;
      int expectedMajorVersion = 4;
      int expectedMinorVersion = 1;
      for (SimpleDocumentVersion documentInFunctionalHistory : doc.getFunctionalHistory()) {
        assertThat(documentInFunctionalHistory.getVersionMaster(),
            sameInstance(doc.getVersionMaster()));
        assertThat(documentInFunctionalHistory.getVersion(),
            is(expectedMajorVersion + "." + expectedMinorVersion));
        assertThat(documentInFunctionalHistory.getMajorVersion(), is(expectedMajorVersion));
        assertThat(documentInFunctionalHistory.getMinorVersion(), is(expectedMinorVersion--));
        if (expectedMinorVersion < 0) {
          expectedMajorVersion--;
          expectedMinorVersion = (expectedMajorVersion == 0) ? 5 : 4;
        }
        assertThat(documentInFunctionalHistory.getVersionIndex(), is(versionIndexExpected));
        assertThat(documentInFunctionalHistory.getRepositoryPath(),
            is(String.format(versionPathPattern, "1." + versionIndexExpected)));
        versionIndexExpected -= 51;
      }

    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testChangeVersionStateOfVersionedDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      assertThat(docCreated.getSize(), is(14L));
      assertThat(docCreated.getHistory(), is(notNullValue()));
      assertThat(docCreated.getHistory(), hasSize(0));
      assertThat(docCreated.getMajorVersion(), is(0));
      assertThat(docCreated.getMinorVersion(), is(1));
      attachment = createFrenchVersionnedAttachment();
      document = new HistorisedDocument(emptyId, foreignId, 15, attachment);
      document.setPublicDocument(false);
      documentRepository.lock(session, document, document.getEditedBy());
      documentRepository.updateDocument(session, document, true);
      session.save();
      documentRepository.unlock(session, document, false);
      HistorisedDocument doc = (HistorisedDocument) documentRepository.findDocumentById(session,
          result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getHistory(), is(notNullValue()));
      assertThat(doc.getHistory(), hasSize(1));
      assertThat(doc.getHistory().get(0).getOrder(), is(0));
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(2));
      documentRepository.changeVersionState(session, result, "To simple document");
      SimpleDocument simplifiedDocument = documentRepository.findDocumentById(session, result, "fr");
      assertThat(simplifiedDocument, is(notNullValue()));
      assertThat(simplifiedDocument.getClass().getName(), is(SimpleDocument.class.getName()));
      assertThat(simplifiedDocument.getOrder(), is(15));
      assertThat(simplifiedDocument.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(simplifiedDocument.getSize(), is(28L));
      assertThat(simplifiedDocument.getMajorVersion(), is(0));
      assertThat(simplifiedDocument.getMinorVersion(), is(0));
      assertThat(simplifiedDocument.isVersioned(), is(false));
      assertThat(simplifiedDocument.getComment(), is("To simple document"));
    }
  }

  private SimpleDocumentPK createVersionedDocument(Session session, SimpleDocument document,
      InputStream content) throws RepositoryException, IOException {
    SimpleDocumentPK result = documentRepository.createDocument(session, document);
    documentRepository.storeContent(document, content);
    document.setPK(result);
    session.save();
    documentRepository.unlock(session, document, false);
    return result;
  }
}
