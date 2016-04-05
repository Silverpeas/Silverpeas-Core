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
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationTest;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.MimeTypes;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;

@RunWith(Arquillian.class)
public class DocumentRepositoryIntegrationTest extends JcrIntegrationTest {

  private static final String instanceId = "kmelia73";
  private final DocumentRepository documentRepository = new DocumentRepository();

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
   */
  @Test
  public void testCreateDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreated(), is(creationDate));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of deleteDocument method, of class DocumentRepository.
   */
  @Test
  public void testDeleteDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
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
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   */
  @Test
  public void testFindDocumentById() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, "en");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(0L)));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc.getForbiddenDownloadForRoles(), nullValue());
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   */
  @Test
  public void testFindDocumentWithForbiddentDownloadForRolesById() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.reader, SilverpeasRole.user);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, "en");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(0L)));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc.getForbiddenDownloadForRoles(),
          contains(SilverpeasRole.user, SilverpeasRole.reader));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of findLast method, of class DocumentRepository.
   */
  @Test
  public void testFindLast() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      long oldSilverpeasId = document.getOldSilverpeasId();
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      foreignId = "node18";
      document = new SimpleDocument(emptyId, foreignId, 5, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
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
   */
  @Test
  public void testUpdateDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.updateDocument(session, document, true);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));

    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   */
  @Test
  public void testUpdateDocumentForbidDownloadToReaderRole() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.getForbiddenDownloadForRoles(), nullValue());
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.reader);
      documentRepository.updateDocument(session, document, true);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      assertThat(doc.getForbiddenDownloadForRoles(), contains(SilverpeasRole.reader));

    }
  }

  /**
   * Test of saveForbiddenDownloadForRoles method, of class DocumentRepository.
   */
  @Test
  public void testSaveForbiddenDownloadForRoles() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.getForbiddenDownloadForRoles(), nullValue());
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.reader);
      documentRepository.saveForbiddenDownloadForRoles(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc.getForbiddenDownloadForRoles(), contains(SilverpeasRole.reader));

    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   *
   * @throws Exception
   */
  @Test
  public void testChangeVersionStateOfSimpleDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.updateDocument(session, document, true);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is(28L));
      documentRepository.changeVersionState(session, result, "To versioned document");
      session.save();
      HistorisedDocument historisedDocument = (HistorisedDocument) documentRepository.
          findDocumentById(session, result, "fr");
      assertThat(historisedDocument, is(notNullValue()));
      assertThat(historisedDocument.getOrder(), is(15));
      assertThat(historisedDocument.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(historisedDocument.getSize(), is(28L));
      assertThat(historisedDocument.getHistory(), is(notNullValue()));
      assertThat(historisedDocument.getHistory(), hasSize(0));
      assertThat(historisedDocument.getMajorVersion(), is(1));
      assertThat(historisedDocument.getMinorVersion(), is(0));
      assertThat(historisedDocument.isVersioned(), is(true));
      assertThat(historisedDocument.getComment(), is("To versioned document"));
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsByForeignId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      String otherForeignId = "node25";
      SimpleDocument docNode25_1
          = new SimpleDocument(emptyId, otherForeignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode25_1);
      documentRepository.storeContent(docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2
          = new SimpleDocument(emptyId, otherForeignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode25_2);
      documentRepository.storeContent(docNode25_2, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsByForeignId(session, instanceId,
          foreignId, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2));
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsByForeignIdAndType() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docNode18_3 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      docNode18_3.setDocumentType(DocumentType.wysiwyg);
      documentRepository.createDocument(session, docNode18_3);
      documentRepository.storeContent(docNode18_3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_4 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      docNode18_4.setDocumentType(DocumentType.image);
      documentRepository.createDocument(session, docNode18_4);
      documentRepository.storeContent(docNode18_4, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsByForeignIdAndType(session,
          instanceId, foreignId, DocumentType.attachment, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2));
      docs = documentRepository.listDocumentsByForeignIdAndType(session, instanceId,
          foreignId, DocumentType.wysiwyg, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(1));
      assertThat(docs, containsInAnyOrder(docNode18_3));
      docs = documentRepository.listDocumentsByForeignIdAndType(session, instanceId,
          foreignId, DocumentType.image, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(1));
      assertThat(docs, containsInAnyOrder(docNode18_4));
      docs = documentRepository.listAllDocumentsByForeignId(session, instanceId, foreignId, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(4));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2, docNode18_3, docNode18_4));
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsByComponentIdAndType() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docNode18_3 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      docNode18_3.setDocumentType(DocumentType.wysiwyg);
      documentRepository.createDocument(session, docNode18_3);
      documentRepository.storeContent(docNode18_3, content);
      emptyId = new SimpleDocumentPK("-1", "kmelia38");
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_4 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      docNode18_4.setDocumentType(DocumentType.image);
      documentRepository.createDocument(session, docNode18_4);
      documentRepository.storeContent(docNode18_4, content);
      session.save();
      List<SimpleDocument> docs = documentRepository
          .listDocumentsByComponentIdAndType(session, instanceId, DocumentType.attachment, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2));
      docs = documentRepository
          .listDocumentsByComponentIdAndType(session, instanceId, DocumentType.wysiwyg, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(1));
      assertThat(docs, containsInAnyOrder(docNode18_3));
      docs = documentRepository
          .listDocumentsByComponentIdAndType(session, instanceId, DocumentType.image, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(0));
    }
  }

  /**
   * Test of listDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testListAllDocumentsByComponentId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docNode18_3 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      docNode18_3.setDocumentType(DocumentType.wysiwyg);
      documentRepository.createDocument(session, docNode18_3);
      documentRepository.storeContent(docNode18_3, content);
      emptyId = new SimpleDocumentPK("-1", "kmelia38");
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_4 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      docNode18_4.setDocumentType(DocumentType.image);
      documentRepository.createDocument(session, docNode18_4);
      documentRepository.storeContent(docNode18_4, content);
      session.save();
      List<SimpleDocument> docs =
          documentRepository.listAllDocumentsByComponentId(session, instanceId, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(3));
      assertThat(docs, containsInAnyOrder(docNode18_1, docNode18_2, docNode18_3));
    }
  }

  /**
   * Test of selectDocumentsByForeignId method, of class DocumentRepository.
   */
  @Test
  public void testSelectDocumentsByForeignId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument docNode18_1 = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      docNode18_1.setDocumentType(DocumentType.wysiwyg);
      documentRepository.createDocument(session, docNode18_1);
      documentRepository.storeContent(docNode18_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode18_2 = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode18_2);
      documentRepository.storeContent(docNode18_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      String otherForeignId = "node25";
      SimpleDocument docNode25_1
          = new SimpleDocument(emptyId, otherForeignId, 10, false, attachment);
      documentRepository.createDocument(session, docNode25_1);
      documentRepository.storeContent(docNode25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docNode25_2
          = new SimpleDocument(emptyId, otherForeignId, 15, false, attachment);
      documentRepository.createDocument(session, docNode25_2);
      documentRepository.storeContent(docNode25_2, content);
      session.save();
      NodeIterator nodes = documentRepository.selectDocumentsByForeignId(session, instanceId,
          foreignId);
      assertThat(nodes, is(notNullValue()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docNode18_1.getId()));
      assertThat(nodes.hasNext(), is(true));
      assertThat(nodes.nextNode().getIdentifier(), is(docNode18_2.getId()));
    }
  }

  /**
   * Test of selectDocumentsByOwnerId method, of class DocumentRepository.
   */
  @Test
  public void testSelectDocumentsByOwnerId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      SimpleDocument docOwn10_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_1);
      documentRepository.storeContent(docOwn10_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn10_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_2);
      documentRepository.storeContent(docOwn10_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docOwn25_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_1);
      documentRepository.storeContent(docOwn25_1, content);
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
   */
  @Test
  public void testAddContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      documentRepository.addContent(session, result, attachment);
      document.setLanguage(attachment.getLanguage());
      documentRepository.storeContent(document, content);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkFrenchSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of removeContent method, of class DocumentRepository.
   */
  @Test
  public void testRemoveContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      attachment = createFrenchSimpleAttachment();
      documentRepository.addContent(session, result, attachment);
      document.setLanguage(attachment.getLanguage());
      documentRepository.storeContent(document, content);
      session.save();
      documentRepository.removeContent(session, result, "fr");
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      checkEnglishSimpleDocument(doc);
      doc = documentRepository.findDocumentById(session, result, "en");
      checkEnglishSimpleDocument(doc);
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
  public void testListComponentDocumentsByOwner() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      SimpleDocument docOwn10_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_1);
      documentRepository.storeContent(docOwn10_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn10_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_2);
      documentRepository.storeContent(docOwn10_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docOwn25_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_1);
      documentRepository.storeContent(docOwn25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn25_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_2);
      documentRepository.storeContent(docOwn25_2, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listComponentDocumentsByOwner(session,
          instanceId, owner, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docOwn25_1, docOwn25_2));
    }
  }

  /**
   * Test of listDocumentsByOwner method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsLockedByUser() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      String owner = "10";
      SimpleDocument docOwn10_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_1);
      documentRepository.storeContent(docOwn10_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn10_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn10_2);
      documentRepository.storeContent(docOwn10_2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      owner = "25";
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docOwn25_1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_1);
      documentRepository.storeContent(docOwn25_1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docOwn25_2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      documentRepository.createDocument(session, docOwn25_2);
      documentRepository.storeContent(docOwn25_2, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsLockedByUser(session, owner, "fr");
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, containsInAnyOrder(docOwn25_1, docOwn25_2));
    }
  }

  /**
   * Test of listExpiringDocumentsByOwner method, of class DocumentRepository.
   */
  @Test
  public void testListExpiringDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(expiringDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notExpiringDoc2);
      documentRepository.storeContent(notExpiringDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument expiringDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      documentRepository.createDocument(session, expiringDoc3);
      documentRepository.storeContent(expiringDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, notExpiringDoc4);
      documentRepository.storeContent(notExpiringDoc4, content);
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
   */
  @Test
  public void testSelectExpiringDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(expiringDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notExpiringDoc2);
      documentRepository.storeContent(notExpiringDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument expiringDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      expiringDoc3.setExpiry(today.getTime());
      documentRepository.createDocument(session, expiringDoc3);
      documentRepository.storeContent(expiringDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notExpiringDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notExpiringDoc4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, notExpiringDoc4);
      documentRepository.storeContent(notExpiringDoc4, content);
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
   */
  @Test
  public void testListDocumentsToUnlock() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(docToLeaveLocked1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToUnlock2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock2);
      documentRepository.storeContent(docToUnlock2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docToUnlock3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock3);
      documentRepository.storeContent(docToUnlock3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, docToLeaveLocked4);
      documentRepository.storeContent(docToLeaveLocked4, content);
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
   */
  @Test
  public void testSelectDocumentsRequiringUnlocking() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(docToLeaveLocked1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToUnlock2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock2);
      documentRepository.storeContent(docToUnlock2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument docToUnlock3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
      documentRepository.createDocument(session, docToUnlock3);
      documentRepository.storeContent(docToUnlock3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
      docToLeaveLocked4.setExpiry(beforeDate.getTime());
      documentRepository.createDocument(session, docToLeaveLocked4);
      documentRepository.storeContent(docToLeaveLocked4, content);
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
   */
  @Test
  public void testSelectWarningDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(warningDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notWarningDoc2);
      documentRepository.storeContent(notWarningDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument warningDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc3);
      documentRepository.storeContent(warningDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notWarningDoc4.setAlert(beforeDate.getTime());
      documentRepository.createDocument(session, notWarningDoc4);
      documentRepository.storeContent(notWarningDoc4, content);
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
   */
  @Test
  public void testMoveDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
          Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
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
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   */
  @Test
  public void testMoveDocumentWithDocumentTypeChange() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      foreignId = "kmelia36";
      assertThat(document.getDocumentType(), is(DocumentType.attachment));
      document.setDocumentType(DocumentType.form);
      SimpleDocumentPK result =
          documentRepository.moveDocument(session, document, new ForeignPK("45", foreignId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), foreignId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, not(sameInstance(document)));
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc.getDocumentType(), is(DocumentType.form));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   */
  @Test
  public void testCopyDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(document, content);
      session.save();
      foreignId = "node36";
      SimpleDocumentPK result =
          documentRepository.copyDocument(session, document, new ForeignPK(foreignId, instanceId));
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
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   */
  @Test
  public void testCopyReservedDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      SimpleDocument document = new SimpleDocument(emptyId, "node18", 0, false, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      SimpleDocumentPK sourcePk = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();

      document = documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getEditedBy(), nullValue());

      document.edit("26");
      documentRepository.updateDocument(session, document, true);
      session.save();

      document = documentRepository.findDocumentById(session, sourcePk, language);

      assertThat(document, is(notNullValue()));
      assertThat(document.getOrder(), is(0));
      assertThat(document.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(document.getSize(), is(14L));
      assertThat(document.getMajorVersion(), is(0));
      assertThat(document.getMinorVersion(), is(0));
      assertThat(document.getVersionIndex(), is(0));
      assertThat(document.getRepositoryPath(), is("/kmelia73/attachments/simpledoc_1"));
      assertThat(document.getEditedBy(), is("26"));

      String targetInstanceId = "kmelia26";
      String targetForeignId = "node36";
      SimpleDocumentPK result = documentRepository
          .copyDocument(session, document, new ForeignPK(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      document.setForeignId(targetForeignId);
      document.setPK(result);
      document.setNodeName(doc.getNodeName());
      document.release();
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);

      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(0));
      assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(doc.getSize(), is(14L));
      assertThat(doc.getMajorVersion(), is(0));
      assertThat(doc.getMinorVersion(), is(0));
      assertThat(doc.getVersionIndex(), is(0));
      assertThat(doc.getRepositoryPath(), is("/kmelia26/attachments/simpledoc_2"));
      assertThat(doc.getEditedBy(), nullValue());

      String[] ignoredBeanPropertiesInComparison =
          new String[]{"created", "updated", "repositoryPath", "versionMaster"};
      assertSimpleDocumentsAreEquals(doc, document, ignoredBeanPropertiesInComparison);
    }
  }

  @SuppressWarnings("unchecked")
  private void assertSimpleDocumentsAreEquals(SimpleDocument result, SimpleDocument expected,
      String... ignoredBeanProperties) throws Exception {

    // SimpleDocument
    Map<String, String> expectedBeanProperties = BeanUtils.describe(expected);
    Map<String, String> resultBeanProperties = BeanUtils.describe(result);
    String resultRepoPath = resultBeanProperties.get("repositoryPath");
    List<String> toIgnoredBeanProperties = new ArrayList<>();
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
   * Test of copyDocument method, of class DocumentRepository.
   */
  @Test
  public void testCopyDocumentWithDocumentTypeChange() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreated();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      foreignId = "node36";
      assertThat(document.getDocumentType(), is(DocumentType.attachment));
      document.setDocumentType(DocumentType.form);
      SimpleDocumentPK result =
          documentRepository.copyDocument(session, document, new ForeignPK(foreignId, instanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      document = documentRepository.findDocumentById(session, document.getPk(), language);
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, not(sameInstance(document)));
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreated(), is(creationDate));
      assertThat(doc.getDocumentType(), is(DocumentType.form));
      document.setForeignId(foreignId);
      document.setPK(result);
      assertThat(doc, SimpleDocumentAttributesMatcher.matches(document));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of findDocumentByOldSilverpeasId method, of class DocumentRepository.
   */
  @Test
  public void testFindDocumentByOldSilverpeasId() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(512L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument otherDocument = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, otherDocument);
      documentRepository.storeContent(document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(236L);
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      SimpleDocument versionedDocument = new SimpleDocument(emptyId, foreignId, 0, true, attachment);
      versionedDocument.setNodeName(SimpleDocument.VERSION_PREFIX + versionedDocument.
          getOldSilverpeasId());
      documentRepository.createDocument(session, versionedDocument);
      documentRepository.storeContent(document, content);
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
    }
  }

  /**
   * Test of listDocumentsRequiringWarning method, of class DocumentRepository.
   */
  @Test
  public void testListDocumentsRequiringWarning() throws Exception {
    try (JcrSession session = openSystemSession()) {
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
      documentRepository.storeContent(warningDoc1, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
          attachment);
      notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
      documentRepository.createDocument(session, notWarningDoc2);
      documentRepository.storeContent(notWarningDoc2, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      SimpleDocument warningDoc3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
          attachment);
      warningDoc3.setAlert(today.getTime());
      documentRepository.createDocument(session, warningDoc3);
      documentRepository.storeContent(warningDoc3, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createFrenchSimpleAttachment();
      SimpleDocument notWarningDoc4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
          attachment);
      Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
      notWarningDoc4.setAlert(beforeDate.getTime());
      documentRepository.createDocument(session, notWarningDoc4);
      documentRepository.storeContent(notWarningDoc4, content);
      session.save();
      List<SimpleDocument> docs = documentRepository.listDocumentsRequiringWarning(session, today.
          getTime(),
          null);
      assertThat(docs, is(notNullValue()));
      assertThat(docs.size(), is(2));
      assertThat(docs, contains(warningDoc1, warningDoc3));
    }
  }

  @Test
  public void testStoreContent() throws RepositoryException, IOException {
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleAttachment attachment = createEnglishSimpleAttachment();
    String foreignId = "node18";
    String owner = "10";
    Calendar today = Calendar.getInstance();
    DateUtil.setAtBeginOfDay(today);
    SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, owner,
        attachment);
    document.setExpiry(today.getTime());
    try (JcrSession session = openSystemSession()) {
      documentRepository.createDocument(session, document);
    }
    ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
        Charsets.UTF_8));
    documentRepository.storeContent(document, content);
    content.close();
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    File contentFile = new File(document.getAttachmentPath());
    assertThat(contentFile.exists(), is(true));
    assertThat(contentFile.isFile(), is(true));
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    String storedContent = FileUtils.readFileToString(contentFile, Charsets.UTF_8);
    assertThat(storedContent, is("This is a test"));
  }

  @Test
  public void testCopyMultilangContent() throws RepositoryException, IOException {
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleAttachment attachment = createEnglishSimpleAttachment();
    String foreignId = "node18";
    String owner = "10";
    Calendar today = Calendar.getInstance();
    DateUtil.setAtBeginOfDay(today);
    SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, owner,
        attachment);
    document.setExpiry(today.getTime());
    try (JcrSession session = openSystemSession()) {
      documentRepository.createDocument(session, document);
    }
    ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
        Charsets.UTF_8));
    documentRepository.storeContent(document, content);
    document.setLanguage("fr");
    content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
    documentRepository.storeContent(document, content);
    content.close();
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    File contentFile = new File(document.getAttachmentPath());
    assertThat(contentFile.exists(), is(true));
    assertThat(contentFile.isFile(), is(true));
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    String storedContent = FileUtils.readFileToString(contentFile, Charsets.UTF_8);
    assertThat(storedContent, is("Ceci est un test"));
    document.setLanguage("en");
    contentFile = new File(document.getAttachmentPath());
    assertThat(contentFile.exists(), is(true));
    assertThat(contentFile.isFile(), is(true));
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    storedContent = FileUtils.readFileToString(contentFile, Charsets.UTF_8);
    assertThat(storedContent, is("This is a test"));
  }
}
