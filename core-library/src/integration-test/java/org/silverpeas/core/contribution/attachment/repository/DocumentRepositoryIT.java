
/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.repository;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationIT;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.wbe.StubbedWbeHostManager;

import javax.inject.Inject;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.contribution.attachment.util.AttachmentSettings.YOUNGEST_TO_OLDEST_MANUAL_REORDER_START;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;

@RunWith(Arquillian.class)
public class DocumentRepositoryIT extends JcrIntegrationIT {

  private static final String instanceId = "kmelia73";
  private static final byte[] ENGLISH_CONTENT = "This is a test".getBytes(StandardCharsets.UTF_8);
  private static final byte[] FRENCH_CONTENT = "Ceci est un test".getBytes(StandardCharsets.UTF_8);
  private final DocumentRepository documentRepository = new DocumentRepository();

  @Inject
  private StubbedWbeHostManager wbeManager;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DocumentRepositoryIT.class)
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

  @After
  public void clear() {
    wbeManager.handled = true;
  }

  /**
   * Test of createDocument method, of class DocumentRepository.
   */
  @Test
  public void createDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      InputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getOrder(), is(0));
      checkEnglishSimpleDocument(doc);
      // second document
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      creationDate = attachment.getCreationDate();
      document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getOrder(), is(1));
      checkEnglishSimpleDocument(doc);
      // third document
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      creationDate = attachment.getCreationDate();
      document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getOrder(), is(2));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of createDocument method, of class DocumentRepository.
   */
  @Test
  public void createDocumentWhenSortingFromYoungestToOldestOnUI() throws Exception {
    attachmentSettings.put("attachment.list.order", "-1");
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      InputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getOrder(), is(0));
      checkEnglishSimpleDocument(doc);
      // second document
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      creationDate = attachment.getCreationDate();
      document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getOrder(), is(1));
      checkEnglishSimpleDocument(doc);
      // third document
      emptyId = new SimpleDocumentPK("-1", instanceId);
      attachment = createEnglishSimpleAttachment();
      creationDate = attachment.getCreationDate();
      document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), greaterThan(0L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getOrder(), is(2));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of deleteDocument method, of class DocumentRepository.
   */
  @Test
  public void deleteDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
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
      assertThat(doc.getCreationDate(), is(creationDate));
      documentRepository.deleteDocument(session, expResult);
      doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(nullValue()));
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   */
  @Test
  public void findDocumentById() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, "en");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(0L)));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getForbiddenDownloadForRoles(), nullValue());
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of findDocumentById method, of class DocumentRepository.
   */
  @Test
  public void findDocumentWithForbiddentDownloadForRolesById() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content =
          new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER, SilverpeasRole.USER);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, "en");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(0L)));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getForbiddenDownloadForRoles(),
          contains(SilverpeasRole.USER, SilverpeasRole.READER));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of findLast method, of class DocumentRepository.
   */
  @Test
  public void findLast() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      long oldSilverpeasId = document.getOldSilverpeasId();
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream(FRENCH_CONTENT);
      attachment = createFrenchSimpleAttachment();
      foreignId = "node18";
      document = new SimpleDocument(emptyId, foreignId, 5, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      session.save();
      SimpleDocument doc = documentRepository
          .findLast(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(oldSilverpeasId));
    }
  }

  /**
   * Test of findLast method, of class DocumentRepository.
   */
  @Test
  public void getMinMaxIndexes() throws Exception {
    final String foreignId = "node18";
    try (JcrSession session = openSystemSession()) {
      // can not get MIN MAX because it does not exist document into JCR for the foreignId
      Optional<Pair<Integer, Integer>> minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(false));
      // registering now the first document into JCR
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      // getting MIN MAX with only one document
      minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(true));
      assertThat(minMax.get().getFirst(), is(10));
      assertThat(minMax.get().getSecond(), is(10));
      // registering now a second document into JCR
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream(FRENCH_CONTENT);
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 5, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      // getting MIN MAX with two documents
      minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(true));
      assertThat(minMax.get().getFirst(), is(5));
      assertThat(minMax.get().getSecond(), is(10));
      // registering now several documents into JCR
      IntStream.of(0, -6, 30, 40, 10000000, -3890000, 78, 1, 10).forEach(i -> {
        final SimpleDocumentPK _emptyId = new SimpleDocumentPK("-1", instanceId);
        final ByteArrayInputStream _content = new ByteArrayInputStream(("With index order " + i).getBytes(Charsets.UTF_8));
        final SimpleAttachment _attachment = createFrenchSimpleAttachment();
        final SimpleDocument _document = new SimpleDocument(_emptyId, foreignId, i, false, _attachment);
        try {
          final SimpleDocumentPK _result = documentRepository.createDocument(session, _document);
          documentRepository.storeContent(_document, _content);
          session.save();
          final SimpleDocumentPK _expResult = new SimpleDocumentPK(_result.getId(), instanceId);
          assertThat(_result, is(_expResult));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      // getting MIN MAX with two documents
      minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(true));
      assertThat(minMax.get().getFirst(), is(1));
      // because of order index increment rule, after registering document with order 10000000
      // the next one with index -3890000 is indeed registered with the max order + 1
      assertThat(minMax.get().getSecond(), is(10000001));
    }
  }

  /**
   * Test of findLast method, of class DocumentRepository.
   */
  @Test
  public void getMinMaxIndexesIntoContextWhereDocumentsAreSortedFromYoungestToOldestOnTheUI()
      throws Exception {
    attachmentSettings.put("attachment.list.order", "-1");
    final String foreignId = "node18";
    try (JcrSession session = openSystemSession()) {
      // can not get MIN MAX because it does not exist document into JCR for the foreignId
      Optional<Pair<Integer, Integer>> minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(false));
      // registering now the first document into JCR
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocument document = new SimpleDocument(emptyId, foreignId,
          YOUNGEST_TO_OLDEST_MANUAL_REORDER_START, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      // getting MIN MAX with only one document
      minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(true));
      assertThat(minMax.get().getFirst(), is(YOUNGEST_TO_OLDEST_MANUAL_REORDER_START));
      assertThat(minMax.get().getSecond(), is(YOUNGEST_TO_OLDEST_MANUAL_REORDER_START));
      // registering now a second document into JCR
      emptyId = new SimpleDocumentPK("-1", instanceId);
      content = new ByteArrayInputStream(FRENCH_CONTENT);
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, YOUNGEST_TO_OLDEST_MANUAL_REORDER_START - 5, false, attachment);
      result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      // getting MIN MAX with two documents
      minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(true));
      assertThat(minMax.get().getFirst(), is(YOUNGEST_TO_OLDEST_MANUAL_REORDER_START - 5));
      assertThat(minMax.get().getSecond(), is(YOUNGEST_TO_OLDEST_MANUAL_REORDER_START));
      // registering now several documents into JCR
      IntStream.of(-1, -2, -3, -4, -5, -6, -7, -8).forEach(i -> {
        final SimpleDocumentPK _emptyId = new SimpleDocumentPK("-1", instanceId);
        final ByteArrayInputStream _content = new ByteArrayInputStream(("With index order " + i).getBytes(Charsets.UTF_8));
        final SimpleAttachment _attachment = createFrenchSimpleAttachment();
        final SimpleDocument _document = new SimpleDocument(_emptyId, foreignId, i, false, _attachment);
        try {
          final SimpleDocumentPK _result = documentRepository.createDocument(session, _document);
          documentRepository.storeContent(_document, _content);
          session.save();
          final SimpleDocumentPK _expResult = new SimpleDocumentPK(_result.getId(), instanceId);
          assertThat(_result, is(_expResult));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });
      // getting MIN MAX with two documents
      minMax = documentRepository
          .getMinMaxOrderIndexes(session, instanceId, foreignId, DocumentType.attachment);
      assertThat(minMax, notNullValue());
      assertThat(minMax.isPresent(), is(true));
      assertThat(minMax.get().getFirst(), is(YOUNGEST_TO_OLDEST_MANUAL_REORDER_START - 5 - 8));
      assertThat(minMax.get().getSecond(), is(YOUNGEST_TO_OLDEST_MANUAL_REORDER_START));
    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   */
  @Test
  public void updateDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
      assertThat(doc.getSize(), is((long)FRENCH_CONTENT.length));

    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   */
  @Test
  public void updateDocumentForbidDownloadToReaderRole() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER);
      documentRepository.updateDocument(session, document, true);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOrder(), is(15));
      assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(doc.getSize(), is((long)FRENCH_CONTENT.length));
      assertThat(doc.getForbiddenDownloadForRoles(), contains(SilverpeasRole.READER));

    }
  }

  /**
   * Test of saveForbiddenDownloadForRoles method, of class DocumentRepository.
   */
  @Test
  public void saveForbiddenDownloadForRoles() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER);
      documentRepository.saveForbiddenDownloadForRoles(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc.getForbiddenDownloadForRoles(), contains(SilverpeasRole.READER));
    }
  }

  /**
   * Test of saveDisplayableAsContent method, of class DocumentRepository.
   */
  @Test
  public void saveDisplayableAsContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.isDisplayableAsContent(), is(true));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      document.setDisplayableAsContent(false);
      documentRepository.saveDisplayableAsContent(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc.isDisplayableAsContent(), is(false));
    }
  }

  /**
   * Test of saveEditableSimultaneously method, of class DocumentRepository.
   */
  @Test
  public void saveEditableSimultaneouslyOtherDocumentThanAnOpenOfficeCompatible()
      throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.editableSimultaneously().isPresent(), is(false));
      attachment = createEnglishSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      document.setEditableSimultaneously(true);
      documentRepository.saveEditableSimultaneously(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc.editableSimultaneously().isPresent(), is(false));
    }
  }

  /**
   * Test of saveEditableSimultaneously method, of class DocumentRepository.
   */
  @Test
  public void saveEditableSimultaneouslyOpenOfficeCompatibleDocument()
      throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createFrenchSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.editableSimultaneously().isPresent(), is(true));
      assertThat(document.editableSimultaneously().get(), is(true));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      document.setEditableSimultaneously(false);
      documentRepository.saveEditableSimultaneously(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc.editableSimultaneously().isPresent(), is(true));
      assertThat(doc.editableSimultaneously().get(), is(false));
    }
  }

  /**
   * Test of saveEditableSimultaneously method, of class DocumentRepository.
   */
  @Test
  public void saveEditableSimultaneouslyOpenOfficeCompatibleDocumentButWbeNotHandled()
      throws Exception {
    wbeManager.handled = false;
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createFrenchSimpleAttachment();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      assertThat(document.editableSimultaneously().isPresent(), is(false));
      attachment = createFrenchSimpleAttachment();
      document = new SimpleDocument(emptyId, foreignId, 15, false, attachment);
      document.setEditableSimultaneously(true);
      documentRepository.saveEditableSimultaneously(session, document);
      session.save();
      SimpleDocument doc = documentRepository.findDocumentById(session, result, "fr");
      assertThat(doc.editableSimultaneously().isPresent(), is(false));
    }
  }

  /**
   * Test of updateDocument method, of class DocumentRepository.
   *
   * @throws Exception if an error occurs
   */
  @Test
  public void changeVersionStateOfSimpleDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
      assertThat(doc.getSize(), is((long)FRENCH_CONTENT.length));
      documentRepository.changeVersionState(session, result, "To versioned document");
      session.save();
      HistorisedDocument historisedDocument = (HistorisedDocument) documentRepository.
          findDocumentById(session, result, "fr");
      assertThat(historisedDocument, is(notNullValue()));
      assertThat(historisedDocument.getOrder(), is(15));
      assertThat(historisedDocument.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(historisedDocument.getSize(), is((long)FRENCH_CONTENT.length));
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
  public void listDocumentsByForeignId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void listDocumentsByForeignIdAndType() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void listDocumentsByComponentIdAndType() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void listAllDocumentsByComponentId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content =
          new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void selectDocumentsByForeignId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void selectDocumentsByOwnerId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void addContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      InputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream(FRENCH_CONTENT);
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
  public void removeContent() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 10, false, attachment);
      InputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleDocumentPK result = documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      assertThat(result, is(expResult));
      content = new ByteArrayInputStream(FRENCH_CONTENT);
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
    return SimpleAttachment.builder("en")
        .setFilename("test.pdf")
        .setTitle("My test document")
        .setDescription("This is a test document")
        .setSize(ENGLISH_CONTENT.length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData("0", RandomGenerator.getRandomCalendar().getTime())
        .setFormId("18")
        .build();
  }

  private SimpleAttachment createFrenchSimpleAttachment() {
    return SimpleAttachment.builder("fr")
        .setFilename("test.odp")
        .setTitle("Mon document de test")
        .setDescription("Ceci est un document de test")
        .setSize(FRENCH_CONTENT.length)
        .setContentType(MimeTypes.MIME_TYPE_OO_PRESENTATION)
        .setCreationData("10", RandomGenerator.getRandomCalendar().getTime())
        .setFormId("5")
        .build();
  }

  private void checkEnglishSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.PDF_MIME_TYPE));
    assertThat(doc.getSize(), is((long)ENGLISH_CONTENT.length));
    assertThat(doc.getDescription(), is("This is a test document"));
    assertThat(doc.getCreatedBy(), is("0"));
  }

  private void checkFrenchSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
    assertThat(doc.getSize(), is((long)FRENCH_CONTENT.length));
    assertThat(doc.getDescription(), is("Ceci est un document de test"));
  }

  /**
   * Test of listDocumentsByOwner method, of class DocumentRepository.
   */
  @Test
  public void listComponentDocumentsByOwner() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void listDocumentsLockedByUser() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void listExpiringDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void selectExpiringDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void listDocumentsToUnlock() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void selectDocumentsRequiringUnlocking() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void selectWarningDocuments() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void moveDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      foreignId = "kmelia36";
      SimpleDocumentPK result = documentRepository.moveDocument(session, document, new ResourceReference(
          "45", foreignId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), foreignId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreationDate(), is(creationDate));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of moveDocument method, of class DocumentRepository.
   */
  @Test
  public void moveDocumentWithDocumentTypeChange() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      foreignId = "kmelia36";
      assertThat(document.getDocumentType(), is(DocumentType.attachment));
      document.setDocumentType(DocumentType.form);
      SimpleDocumentPK result =
          documentRepository.moveDocument(session, document, new ResourceReference("45", foreignId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), foreignId);
      expResult.setOldSilverpeasId(document.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, not(sameInstance(document)));
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(document.getOldSilverpeasId()));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc.getDocumentType(), is(DocumentType.form));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of copyDocument method, of class DocumentRepository.
   */
  @Test
  public void copyDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.setContentType(MimeTypes.PDF_MIME_TYPE);
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      session.save();
      foreignId = "node36";
      SimpleDocumentPK result =
          documentRepository.copyDocument(session, document, new ResourceReference(foreignId, instanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreationDate(), is(creationDate));
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
  public void copyReservedDocument() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
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
          .copyDocument(session, document, new ResourceReference(targetForeignId, targetInstanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), targetInstanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreationDate(), is(creationDate));
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
  public void copyDocumentWithDocumentTypeChange() throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String language = "en";
      ByteArrayInputStream content =
          new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
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
          documentRepository.copyDocument(session, document, new ResourceReference(foreignId, instanceId));
      SimpleDocumentPK expResult = new SimpleDocumentPK(result.getId(), instanceId);
      expResult.setOldSilverpeasId(result.getOldSilverpeasId());
      assertThat(result, is(expResult));
      document = documentRepository.findDocumentById(session, document.getPk(), language);
      SimpleDocument doc = documentRepository.findDocumentById(session, expResult, language);
      assertThat(doc, not(sameInstance(document)));
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(not(document.getOldSilverpeasId())));
      assertThat(doc.getCreationDate(), is(creationDate));
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
  public void findDocumentByOldSilverpeasId() throws Exception {
    try (JcrSession session = openSystemSession()) {
      long oldSilverpeasId = 236L;
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(236L);
      String language = "en";
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleAttachment attachment = createEnglishSimpleAttachment();
      Date creationDate = attachment.getCreationDate();
      String foreignId = "node18";
      SimpleDocument document = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      document.setNodeName(SimpleDocument.ATTACHMENT_PREFIX + document.getOldSilverpeasId());
      documentRepository.createDocument(session, document);
      documentRepository.storeContent(document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(512L);
      content = new ByteArrayInputStream(ENGLISH_CONTENT);
      SimpleDocument otherDocument = new SimpleDocument(emptyId, foreignId, 0, false, attachment);
      documentRepository.createDocument(session, otherDocument);
      documentRepository.storeContent(document, content);
      emptyId = new SimpleDocumentPK("-1", instanceId);
      emptyId.setOldSilverpeasId(236L);
      content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc, SimpleDocumentMatcher.matches(document));
      doc = documentRepository.findDocumentByOldSilverpeasId(session, instanceId, oldSilverpeasId,
          true,
          language);
      assertThat(doc, is(notNullValue()));
      assertThat(doc.getOldSilverpeasId(), is(236L));
      assertThat(doc.getCreationDate(), is(creationDate));
      assertThat(doc, SimpleDocumentMatcher.matches(versionedDocument));
      checkEnglishSimpleDocument(doc);
    }
  }

  /**
   * Test of listDocumentsRequiringWarning method, of class DocumentRepository.
   */
  @Test
  public void listDocumentsRequiringWarning() throws Exception {
    try (JcrSession session = openSystemSession()) {
      ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
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
  public void storeContent() throws RepositoryException, IOException {
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
    ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
    documentRepository.storeContent(document, content);
    content.close();
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    File contentFile = new File(document.getAttachmentPath());
    assertThat(contentFile.exists(), is(true));
    assertThat(contentFile.isFile(), is(true));
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    String storedContent = FileUtils.readFileToString(contentFile, Charsets.UTF_8);
    assertThat(storedContent, is(new String(ENGLISH_CONTENT, StandardCharsets.UTF_8)));
  }

  @Test
  public void copyMultilangContent() throws RepositoryException, IOException {
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
    ByteArrayInputStream content = new ByteArrayInputStream(ENGLISH_CONTENT);
    documentRepository.storeContent(document, content);
    document.setLanguage("fr");
    content = new ByteArrayInputStream(FRENCH_CONTENT);
    documentRepository.storeContent(document, content);
    content.close();
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    File contentFile = new File(document.getAttachmentPath());
    assertThat(contentFile.exists(), is(true));
    assertThat(contentFile.isFile(), is(true));
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    String storedContent = FileUtils.readFileToString(contentFile, Charsets.UTF_8);
    assertThat(storedContent, is(new String(FRENCH_CONTENT, StandardCharsets.UTF_8)));
    document.setLanguage("en");
    contentFile = new File(document.getAttachmentPath());
    assertThat(contentFile.exists(), is(true));
    assertThat(contentFile.isFile(), is(true));
    assertThat(document.getAttachmentPath(), is(notNullValue()));
    storedContent = FileUtils.readFileToString(contentFile, Charsets.UTF_8);
    assertThat(storedContent, is(new String(ENGLISH_CONTENT, StandardCharsets.UTF_8)));
  }
}
