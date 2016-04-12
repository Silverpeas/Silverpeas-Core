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
package org.silverpeas.core.contribution.attachment;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.jackrabbit.commons.cnd.ParseException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.model.UnlockContext;
import org.silverpeas.core.contribution.attachment.repository.DocumentRepository;
import org.silverpeas.core.contribution.attachment.repository.DocumentRepositoryIntegrationTest;
import org.silverpeas.core.contribution.attachment.repository.SimpleDocumentMatcher;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationTest;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.WAPrimaryKey;

import javax.inject.Inject;
import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.NT_FOLDER;

/**
 *
 * @author ehugonnet
 */
@RunWith(Arquillian.class)
public class HistorisedAttachmentServiceIntegrationTest extends JcrIntegrationTest {

  private static final String instanceId = "kmelia974";
  private SimpleDocumentPK existingFrDoc;
  private SimpleDocumentPK existingEnDoc;
  private DocumentRepository documentRepository = new DocumentRepository();

  @Inject
  private AttachmentService instance;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DocumentRepositoryIntegrationTest.class)
        .addJcrFeatures()
        .testFocusedOn(war -> war.addAsResource("LibreOffice.odt"))
        .build();
  }

  @Before
  public void loadJcr() throws RepositoryException, ParseException, IOException, SQLException {
    try (JcrSession session = openSystemSession()) {
      if (!session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().addNode(instanceId, NT_FOLDER);
        Date creationDate = RandomGenerator.getRandomCalendar().getTime();
        SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
        String foreignId = "node18";
        SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
            new SimpleAttachment("test.odp", "fr", "Mon document de test",
            "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
            MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
        InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
        existingFrDoc = documentRepository.createDocument(session, document);
        document = documentRepository.findDocumentById(session, existingFrDoc, document.
            getLanguage());
        document.setPublicDocument(true);
        document = documentRepository.unlock(session, document, false);
        documentRepository.storeContent(document, content);
        content.close();
        document.setPK(existingFrDoc);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        content = documentRepository.getContent(session, existingFrDoc, "fr");
        IOUtils.copy(content, out);
        content.close();
        assertThat(out.toString(CharEncoding.UTF_8), is("Ceci est un test"));

        emptyId = new SimpleDocumentPK("-1", instanceId);
        foreignId = "node19";
        document = new HistorisedDocument(emptyId, foreignId, 0,
            new SimpleAttachment("test.docx", "en", "My test document",
            "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
            MimeTypes.WORD_2007_MIME_TYPE, "0", creationDate, "18"));
        content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
        existingEnDoc = documentRepository.createDocument(session, document);
        document = documentRepository.findDocumentById(session, existingEnDoc, document.
            getLanguage());
        document.setPublicDocument(true);
        document = documentRepository.unlock(session, document, false);
        documentRepository.storeContent(document, content);
        document.setPK(existingEnDoc);
        content.close();
        out = new ByteArrayOutputStream();
        content = documentRepository.getContent(session, existingEnDoc, "en");
        IOUtils.copy(content, out);
        content.close();
        assertThat(out.toString(CharEncoding.UTF_8), is("This is a test"));
      }
      session.save();
    }
  }

  /**
   * Test of addContent method, of class AttachmentService.
   */
  @Test
  public void testUpdateStreamContent() throws UnsupportedEncodingException {
    String currentLang = "fr";
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, currentLang);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("Ceci est un test"));
    InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
    instance.updateAttachment(document, content, false, false);
    out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("This is a test"));
  }

  /**
   * Test of addContent method, of class AttachmentService.
   *
   * @throws UnsupportedEncodingException
   */
  @Test
  public void testAddNewStreamContent() throws UnsupportedEncodingException {
    String currentLang = "fr";
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, currentLang);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("Ceci est un test"));
    currentLang = "en";
    InputStream content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
    document.setLanguage(currentLang);
    instance.updateAttachment(document, content, false, false);
    out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("This is a test"));
    currentLang = "fr";
    out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("Ceci est un test"));
  }

  /**
   * Test of addContent method, of class AttachmentService.
   */
  @Test
  public void testAddFileContent() throws URISyntaxException, IOException {
    File file = getDocumentNamed("LibreOffice.odt");
    String currentLang = "fr";
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, currentLang);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("Ceci est un test"));
    currentLang = "en";
    document.setLanguage(currentLang);
    instance.updateAttachment(document, file, false, false);
    File tempFile = File.createTempFile("LibreOffice", ".odt");
    instance.getBinaryContent(tempFile, existingFrDoc, currentLang);
    assertThat(FileUtils.contentEquals(file, tempFile), is(true));
    currentLang = "fr";
    out = new ByteArrayOutputStream();
    instance.getBinaryContent(out, existingFrDoc, currentLang);
    assertThat(out.toString(CharEncoding.UTF_8), is("Ceci est un test"));
  }

  /**
   * Test of addContent method, of class AttachmentService.
   */
  @Test
  public void testUpdateFileContent() throws URISyntaxException, IOException {
    File file = getDocumentNamed("/LibreOffice.odt");
    String currentLang = "fr";
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, currentLang);
    instance.updateAttachment(document, file, false, false);
    File tempFile = File.createTempFile("LibreOffice", ".odt");
    instance.getBinaryContent(tempFile, existingFrDoc, currentLang);
    assertThat(FileUtils.contentEquals(file, tempFile), is(true));
  }

  /**
   * Test of getBinaryContent method, of class AttachmentService.
   */
  @Test
  public void testGetBinaryContentIntoFile() throws IOException {
    File file = File.createTempFile("AttachmentServiceTest", "docx");
    SimpleDocumentPK pk = existingEnDoc;
    String lang = "en";
    instance.getBinaryContent(file, pk, lang);
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    assertThat(file.length(), is((long) "This is a test".getBytes(Charsets.UTF_8).length));
  }

  /**
   * Test of getBinaryContent method, of class AttachmentService.
   */
  @Test
  public void testGetBinaryContentIntoOutputStream() {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    SimpleDocumentPK pk = existingEnDoc;
    String lang = "en";
    instance.getBinaryContent(out, pk, lang);
    assertThat(out, is(notNullValue()));
    byte[] content = out.toByteArray();
    assertThat(content, is(notNullValue()));
    assertThat(content.length, is(14));
    assertThat(new String(content, Charsets.UTF_8), is("This is a test"));
    out = new ByteArrayOutputStream();
    lang = "fr";
    instance.getBinaryContent(out, pk, lang);
    assertThat(out, is(notNullValue()));
    content = out.toByteArray();
    assertThat(content, is(notNullValue()));
    assertThat(content.length, is(14));
    assertThat(new String(content, Charsets.UTF_8), is("This is a test"));
  }

  /**
   * Test of addXmlForm method, of class AttachmentService.
   */
  @Test
  public void testAddXmlForm() {
    String language = "fr";
    String xmlFormName = "15";
    SimpleDocument result = instance.searchDocumentById(existingFrDoc, language);
    assertThat(result, is(notNullValue()));
    assertThat(result.getXmlFormId(), is("5"));
    instance.lock(result.getId(), "10", language);
    instance.addXmlForm(existingFrDoc, language, xmlFormName);
    result = instance.searchDocumentById(existingFrDoc, language);
    assertThat(result, is(notNullValue()));
    assertThat(result.getXmlFormId(), is(xmlFormName));
    instance.addXmlForm(existingFrDoc, language, null);
    instance.unlock(new UnlockContext(result.getId(), "10", language));
    result = instance.searchDocumentById(existingFrDoc, language);
    assertThat(result, is(notNullValue()));
    assertThat(result.getXmlFormId(), is(nullValue()));
  }

  /**
   * Test of cloneDocument method, of class AttachmentService.
   */
  @Test
  public void testCloneDocument() throws IOException {
    String language = "fr";
    String foreignCloneId = "node59";
    SimpleDocument original = instance.searchDocumentById(existingFrDoc, language);
    SimpleDocumentPK clonePk = instance.cloneDocument(original, foreignCloneId);
    SimpleDocument clone = instance.searchDocumentById(clonePk, language);
    original.setCloneId(original.getId());
    SimpleDocument updatedOriginal = instance.searchDocumentById(existingFrDoc, language);
    assertThat(updatedOriginal, SimpleDocumentMatcher.matches(original));
    original.setCloneId(null);
    original.setForeignId(foreignCloneId);
    original.setPK(clonePk);
    assertThat(clone, SimpleDocumentMatcher.matches(original));
    assertThat(FileUtils.contentEquals(new File(original.getAttachmentPath()), new File(clone.
        getAttachmentPath())), is(true));
  }

  /**
   * Test of createAttachment method, of class AttachmentService.
   */
  @Test
  public void testCreateAttachmentFromInputStream() {
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
        MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
    InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
    SimpleDocument result = instance.createAttachment(document, content);
    assertThat(result, is(notNullValue()));
    checkFrenchSimpleDocument(result);
  }

  /**
   * Test of createAttachment method, of class AttachmentService.
   */
  @Test
  public void testCreateIndexedAttachmentFromInputStream() {
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
        MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
    InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
    SimpleDocument result = instance.createAttachment(document, content, true);
    assertThat(result, is(notNullValue()));
    checkFrenchSimpleDocument(result);
  }

  /**
   * Test of createAttachment method, of class AttachmentService.
   */
  @Test
  public void testCreateAttachmentFromInputStreamWithCallback() {
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleDocumentPK emptyId = new SimpleDocumentPK(null, instanceId);
    String foreignId = "node18";
    SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
        MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
    document.setPublicDocument(true);
    InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
    SimpleDocument result = instance.createAttachment(document, content, true, true);
    assertThat(result, is(notNullValue()));
    checkFrenchSimpleDocument(result);
  }

  /**
   * Test of createAttachment method, of class AttachmentService.
   */
  @Test
  public void testCreateAttachmentIndexedCallbackFromFile() throws URISyntaxException {
    File file = getDocumentNamed("/LibreOffice.odt");
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", file.length(), MimeTypes.MIME_TYPE_OO_PRESENTATION, "10",
        creationDate, "5"));
    document.setPublicDocument(false);
    SimpleDocument result = instance.createAttachment(document, file, true, true);
    assertThat(result, is(notNullValue()));
    checkFrenchFileSimpleDocument(result);
  }

  /**
   * Test of createAttachment method, of class AttachmentService.
   */
  @Test
  public void testCreateAttachmentNotIndexedFromFile() throws URISyntaxException {
    File file = getDocumentNamed("/LibreOffice.odt");
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", file.length(), MimeTypes.MIME_TYPE_OO_PRESENTATION, "10",
        creationDate, "5"));
    SimpleDocument result = instance.createAttachment(document, file, false);
    assertThat(result, is(notNullValue()));
    checkFrenchFileSimpleDocument(result);
  }

  /**
   * Test of createAttachment method, of class AttachmentService.
   */
  @Test
  public void testCreateAttachmentFromFile() throws Exception {
    File file = getDocumentNamed("/LibreOffice.odt");
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", file.length(), MimeTypes.MIME_TYPE_OO_PRESENTATION, "10",
        creationDate, "5"));
    SimpleDocument result = instance.createAttachment(document, file);
    assertThat(result, is(notNullValue()));
    checkFrenchFileSimpleDocument(result);
  }

  /**
   * Test of deleteAttachment method, of class AttachmentService.
   */
  @Test
  public void testDeleteAttachment() {
    String lang = "en";
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, lang);
    assertThat(document, is(notNullValue()));
    checkFrenchSimpleDocument(document);
    instance.deleteAttachment(document);
    document = instance.searchDocumentById(existingFrDoc, lang);
    assertThat(document, is(nullValue()));
  }

  /**
   * Test of deleteAttachment method, of class AttachmentService.
   */
  @Test
  public void testDeleteIndexedAttachment() {
    String lang = "en";
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, lang);
    assertThat(document, is(notNullValue()));
    checkFrenchSimpleDocument(document);
    instance.deleteAttachment(document, true);
    document = instance.searchDocumentById(existingFrDoc, lang);
    assertThat(document, is(nullValue()));
  }

  /**
   * Test of removeContent method, of class AttachmentService.
   */
  @Test
  public void testRemoveContent() {
    SimpleDocument document = instance.searchDocumentById(existingFrDoc, "fr");
    checkFrenchSimpleDocument(document);
    instance.removeContent(document, "fr", false);
    document = instance.searchDocumentById(existingFrDoc, "fr");
    assertThat(document, is(nullValue()));

    document = instance.searchDocumentById(existingEnDoc, "en");
    checkEnglishSimpleDocument(document);
    instance.removeContent(document, "fr", false);
    document = instance.searchDocumentById(existingEnDoc, "en");
    assertThat(document, is(notNullValue()));
    checkEnglishSimpleDocument(document);
  }

  /**
   * Test of reorderAttachments method, of class AttachmentService.
   *
   * @throws LoginException
   * @throws RepositoryException
   * @throws IOException
   */
  @Test
  public void testReorderAttachments() throws RepositoryException,
      IOException {
    WAPrimaryKey foreignKey = new ForeignPK("node36", instanceId);
    try (JcrSession session = openSystemSession()) {
      Date creationDate = RandomGenerator.getRandomCalendar().getTime();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = foreignKey.getId();
      SimpleDocument document1 = new HistorisedDocument(emptyId, foreignId, 10,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 1",
          "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
          MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK id = instance.createAttachment(document1, content).getPk();
      document1 = instance.searchDocumentById(id, "fr");

      emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleDocument document2 = new HistorisedDocument(emptyId, foreignId, 5,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 2",
          "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
          MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      id = instance.createAttachment(document2, content).getPk();
      document2  = instance.searchDocumentById(id, "fr");

      emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleDocument document3 = new HistorisedDocument(emptyId, foreignId, 100,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 3",
          "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
          MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      id = instance.createAttachment(document3, content).getPk();
      document3 = instance.searchDocumentById(id, "fr");

      emptyId = new SimpleDocumentPK("-1", instanceId);
      foreignId = "node49";
      SimpleDocument document4 = new HistorisedDocument(emptyId, foreignId, 0,
          new SimpleAttachment("test.docx", "en", "My test document 4",
          "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
          MimeTypes.WORD_2007_MIME_TYPE, "0", creationDate, "18"));
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      id = instance.createAttachment(document4, content).getPk();
      document4 = instance.searchDocumentById(id, "en");

      session.save();
      List<SimpleDocument> result = instance.listDocumentsByForeignKey(foreignKey, "fr");
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result.get(0), SimpleDocumentMatcher.matches(document2));
      assertThat(result.get(1), SimpleDocumentMatcher.matches(document1));
      assertThat(result.get(2), SimpleDocumentMatcher.matches(document3));
      List<SimpleDocumentPK> reorderedList = new ArrayList<>(3);
      reorderedList.add(document1.getPk());
      reorderedList.add(document2.getPk());
      reorderedList.add(document3.getPk());
      instance.reorderAttachments(reorderedList);
      result = instance.listDocumentsByForeignKey(foreignKey, "fr");
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      document1.setOrder(5);
      document2.setOrder(10);
      document3.setOrder(15);
      assertThat(result.get(0), SimpleDocumentMatcher.matches(document1));
      assertThat(result.get(1), SimpleDocumentMatcher.matches(document2));
      assertThat(result.get(2), SimpleDocumentMatcher.matches(document3));
    }
  }

  /**
   * Test of switchAllowingDownloadForReaders method, of class AttachmentService.
   *
   * @throws LoginException
   * @throws RepositoryException
   * @throws IOException
   */
  @Test
  public void testSwitchAllowingDownloadForReaders() throws RepositoryException, IOException {
    WAPrimaryKey foreignKey = new ForeignPK("node36", instanceId);
    SimpleDocumentPK documentPK;
    try (JcrSession session = openSystemSession()) {
      Date creationDate = RandomGenerator.getRandomCalendar().getTime();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = foreignKey.getId();
      SimpleDocument document = new HistorisedDocument(emptyId, foreignId, 10,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 1",
              "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
              MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      documentPK = instance.createAttachment(document, content).getPk();
      session.save();

    }
    // Simulate an other call ... closing old session and opening a new one
    try (JcrSession session = openSystemSession()) {

      // Verifying document is created.
      List<SimpleDocument> result = instance.listDocumentsByForeignKey(foreignKey, "fr");
      assertThat(result, notNullValue());
      assertThat(result, hasSize(1));
      SimpleDocument documentOfResult = result.get(0);
      assertThat(documentOfResult.getForbiddenDownloadForRoles(), nullValue());

      // Allowing readers (but nothing is saved in JCR)
      instance.switchAllowingDownloadForReaders(documentPK, true);
      documentOfResult = instance.searchDocumentById(documentPK, "fr");
      assertThat(documentOfResult, notNullValue());
      assertThat(documentOfResult.getForbiddenDownloadForRoles(), nullValue());

      // Forbidding readers
      instance.switchAllowingDownloadForReaders(documentPK, false);
      documentOfResult = instance.searchDocumentById(documentPK, "fr");
      assertThat(documentOfResult, notNullValue());
      assertThat(documentOfResult.getForbiddenDownloadForRoles(),
          contains(SilverpeasRole.user, SilverpeasRole.reader));

      // Forbidding again readers (but nothing is saved in JCR)
      instance.switchAllowingDownloadForReaders(documentPK, false);
      documentOfResult = instance.searchDocumentById(documentPK, "fr");
      assertThat(documentOfResult, notNullValue());
      assertThat(documentOfResult.getForbiddenDownloadForRoles(),
          contains(SilverpeasRole.user, SilverpeasRole.reader));

      // Allowing readers
      instance.switchAllowingDownloadForReaders(documentPK, true);
      documentOfResult = instance.searchDocumentById(documentPK, "fr");
      assertThat(documentOfResult, notNullValue());
      assertThat(documentOfResult.getForbiddenDownloadForRoles(), nullValue());

    }
  }

  /**
   * Test of searchDocumentById method, of class AttachmentService.
   */
  @Test
  public void testSearchAttachmentById() {
    SimpleDocument result = instance.searchDocumentById(existingFrDoc, null);
    checkFrenchSimpleDocument(result);
    assertThat(existingFrDoc.getOldSilverpeasId(), greaterThan(0L));
    String id = existingFrDoc.getId();
    existingFrDoc.setId(null);
    result = instance.searchDocumentById(existingFrDoc, null);
    checkFrenchSimpleDocument(result);
    existingFrDoc.setId(id);
    existingFrDoc.setOldSilverpeasId(-1L);
    result = instance.searchDocumentById(existingFrDoc, null);
    checkFrenchSimpleDocument(result);
  }

  /**
   * Test of listDocumentsByForeignKey method, of class AttachmentService.
   */
  @Test
  public void testSearchAttachmentsByExternalObject() throws RepositoryException,
      IOException {
    WAPrimaryKey foreignKey = new ForeignPK("node36", instanceId);
    try (JcrSession session = openSystemSession()) {
      Date creationDate = RandomGenerator.getRandomCalendar().getTime();
      SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
      String foreignId = foreignKey.getId();
      SimpleDocument document1 = new HistorisedDocument(emptyId, foreignId, 10,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 1",
          "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
          MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      InputStream content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      SimpleDocumentPK id = new DocumentRepository().createDocument(session, document1);
      document1.setPK(id);
      documentRepository.storeContent(document1, content);

      emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleDocument document2 = new HistorisedDocument(emptyId, foreignId, 5,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 2",
          "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
          MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      id = new DocumentRepository().createDocument(session, document2);
      document2.setPK(id);
      documentRepository.storeContent(document2, content);

      emptyId = new SimpleDocumentPK("-1", instanceId);
      SimpleDocument document3 = new HistorisedDocument(emptyId, foreignId, 100,
          new SimpleAttachment("test.odp", "fr", "Mon document de test 3",
          "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
          MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", creationDate, "5"));
      content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
      id = new DocumentRepository().createDocument(session, document3);
      document3.setPK(id);
      documentRepository.storeContent(document3, content);

      emptyId = new SimpleDocumentPK("-1", instanceId);
      foreignId = "node49";
      SimpleDocument document4 = new HistorisedDocument(emptyId, foreignId, 0,
          new SimpleAttachment("test.docx", "en", "My test document 4",
          "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
          MimeTypes.WORD_2007_MIME_TYPE, "0", creationDate, "18"));
      content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
      id = new DocumentRepository().createDocument(session, document4);
      document4.setPK(id);
      documentRepository.storeContent(document4, content);

      session.save();
      List<SimpleDocument> result = instance.listDocumentsByForeignKey(foreignKey, "fr");
      assertThat(result, is(notNullValue()));
      assertThat(result, hasSize(3));
      assertThat(result.get(0), SimpleDocumentMatcher.matches(document2));
      assertThat(result.get(1), SimpleDocumentMatcher.matches(document1));
      assertThat(result.get(2), SimpleDocumentMatcher.matches(document3));
    }
  }

  /**
   * Test of updateAttachment method, of class AttachmentService.
   */
  @Test
  public void testUpdateAttachment() {
    SimpleDocument result = instance.searchDocumentById(existingFrDoc, "fr");
    checkFrenchSimpleDocument(result);
    Date alertDate = RandomGenerator.getRandomCalendar().getTime();
    result.setAlert(alertDate);
    result.setContentType(MimeTypes.BZ2_ARCHIVE_MIME_TYPE);
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    result.setUpdated(creationDate);
    String creatorId = "150";
    result.setUpdatedBy(creatorId);
    String description = "Ceci est mon document de test mis à jour";
    result.setDescription(description);
    Date expiryDate = RandomGenerator.getRandomCalendar().getTime();
    result.setExpiry(expiryDate);
    result.setFilename("toto"); //shouldn't change
    int order = 5000;
    result.setOrder(order);
    String title = "Mon document de test mis à jour";
    result.setTitle(title);
    instance.lock(existingFrDoc.getId(), creatorId, "fr");
    instance.updateAttachment(result, false, false);
    instance.unlock(new UnlockContext(existingFrDoc.getId(), creatorId, "fr"));
    existingFrDoc.setId(null);
    result = instance.searchDocumentById(existingFrDoc, "fr");
    assertThat(result, is(notNullValue()));
    assertThat(result.getAlert(), is(nullValue()));
    assertThat(result.getTitle(), is(title));
    assertThat(result.getDescription(), is(description));
    assertThat(result.getContentType(), is(MimeTypes.BZ2_ARCHIVE_MIME_TYPE));
    assertThat(result.getMajorVersion(), is(2));
    assertThat(result.getMinorVersion(), is(0));
  }

  /**
   * Test of listDocumentsRequiringWarning method, of class AttachmentService.
   */
  @Test
  public void testListDocumentsRequiringWarning() {
    ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
        Charsets.UTF_8));
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    String owner = "10";
    Calendar today = Calendar.getInstance();
    DateUtil.setAtBeginOfDay(today);
    SimpleDocument warningDoc1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
        new SimpleAttachment("test.pdf", "en", "My test document",
        "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
        MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18"));
    warningDoc1 = instance.createAttachment(warningDoc1, content);
    instance.lock(warningDoc1.getId(), owner, warningDoc1.getLanguage());
    warningDoc1.setAlert(today.getTime());
    instance.updateAttachment(warningDoc1, false, false);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument notWarningDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        "10", RandomGenerator.getRandomCalendar().getTime(), "5"));
    content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
    notWarningDoc2 = instance.createAttachment(notWarningDoc2, content);
    instance.lock(notWarningDoc2.getId(), owner, notWarningDoc2.getLanguage());
    notWarningDoc2.setAlert(RandomGenerator.getCalendarAfter(today).getTime());
    instance.updateAttachment(notWarningDoc2, false, false);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument warningDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
        new SimpleAttachment("test.pdf", "en", "My test document",
        "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
        MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18"));
    content = new ByteArrayInputStream("This is a test".getBytes(Charsets.UTF_8));
    warningDoc3 = instance.createAttachment(warningDoc3, content);
    instance.lock(warningDoc3.getId(), owner, warningDoc3.getLanguage());
    warningDoc3.setAlert(today.getTime());
    instance.updateAttachment(warningDoc3, false, false);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument notWarningDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        "10", RandomGenerator.getRandomCalendar().getTime(), "5"));
    Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
    content = new ByteArrayInputStream("Ceci est un test".getBytes(Charsets.UTF_8));
    notWarningDoc4 = instance.createAttachment(notWarningDoc4, content);
    instance.lock(notWarningDoc4.getId(), owner, notWarningDoc4.getLanguage());
    notWarningDoc4.setAlert(beforeDate.getTime());
    instance.updateAttachment(notWarningDoc4, false, false);
    List<SimpleDocument> docs = instance.listDocumentsRequiringWarning(today.getTime(), null);
    assertThat(docs, is(notNullValue()));
    assertThat(docs.size(), is(2));
    warningDoc1.setMajorVersion(1);
    warningDoc3.setMajorVersion(1);
    assertThat(docs, contains(warningDoc1, warningDoc3));
  }

  /**
   * Test of listExpiringDocuments method, of class AttachmentService.
   */
  @Test
  public void testListExpiringDocuments() {
    ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
        Charsets.UTF_8));
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    String owner = "10";
    Calendar today = Calendar.getInstance();
    DateUtil.setAtBeginOfDay(today);
    SimpleDocument expiringDoc1 = new HistorisedDocument(emptyId, foreignId, 10, owner,
        new SimpleAttachment("test.pdf", "en", "My test document",
        "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
        MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18"));
    expiringDoc1 = instance.createAttachment(expiringDoc1, content);
    expiringDoc1.setExpiry(today.getTime());
    instance.lock(expiringDoc1.getId(), owner, expiringDoc1.getLanguage());
    instance.updateAttachment(expiringDoc1, false, false);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument notExpiringDoc2 = new HistorisedDocument(emptyId, foreignId, 15, owner,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        "10", RandomGenerator.getRandomCalendar().getTime(), "5"));
    notExpiringDoc2 = instance.createAttachment(notExpiringDoc2, content);
    instance.lock(notExpiringDoc2.getId(), owner, notExpiringDoc2.getLanguage());
    notExpiringDoc2.setExpiry(RandomGenerator.getCalendarAfter(today).getTime());
    instance.updateAttachment(notExpiringDoc2, false, false);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument expiringDoc3 = new HistorisedDocument(emptyId, foreignId, 20, owner,
        new SimpleAttachment("test.pdf", "en", "My test document",
        "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
        MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18"));
    expiringDoc3 = instance.createAttachment(expiringDoc3, content);
    instance.lock(expiringDoc3.getId(), owner, expiringDoc3.getLanguage());
    expiringDoc3.setExpiry(today.getTime());
    instance.updateAttachment(expiringDoc3, false, false);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument notExpiringDoc4 = new HistorisedDocument(emptyId, foreignId, 25, owner,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test", "Ceci est un test".getBytes(Charsets.UTF_8).length,
        MimeTypes.MIME_TYPE_OO_PRESENTATION, "10", RandomGenerator.getRandomCalendar().getTime(),
        "5"));
    Calendar beforeDate = RandomGenerator.getCalendarBefore(today);
    notExpiringDoc4 = instance.createAttachment(notExpiringDoc4, content);
    instance.lock(notExpiringDoc4.getId(), owner, notExpiringDoc4.getLanguage());
    notExpiringDoc4.setExpiry(beforeDate.getTime());
    instance.updateAttachment(notExpiringDoc4, false, false);
    List<SimpleDocument> docs = instance.listExpiringDocuments(today.getTime(), "fr");
    assertThat(docs, is(notNullValue()));
    assertThat(docs.size(), is(2));
    expiringDoc1.setMajorVersion(1);
    expiringDoc3.setMajorVersion(1);
    assertThat(docs, contains(expiringDoc1, expiringDoc3));
  }

  /**
   * Test of updateAttachment method, of class AttachmentService.
   */
  @Test
  public void testCopyAttachment() {
    HistorisedDocument result = (HistorisedDocument) instance.
        searchDocumentById(existingFrDoc, "fr");
    checkFrenchSimpleDocument(result);
    Date alertDate = RandomGenerator.getRandomCalendar().getTime();
    result.setAlert(alertDate);
    result.setContentType(MimeTypes.BZ2_ARCHIVE_MIME_TYPE);
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    result.setUpdated(creationDate);
    String creatorId = "150";
    result.setUpdatedBy(creatorId);
    String description = "Ceci est mon document de test mis à jour";
    result.setDescription(description);
    Date expiryDate = RandomGenerator.getRandomCalendar().getTime();
    result.setExpiry(expiryDate);
    int order = 5000;
    result.setOrder(order);
    String title = "Mon document de test mis à jour";
    result.setTitle(title);
    instance.lock(existingFrDoc.getId(), creatorId, "fr");
    instance.updateAttachment(result, false, false);
    instance.unlock(new UnlockContext(existingFrDoc.getId(), creatorId, "fr"));
    existingFrDoc.setId(null);
    result = (HistorisedDocument) instance.searchDocumentById(existingFrDoc, "fr");
    assertThat(result, is(notNullValue()));
    assertThat(result.getAlert(), is(nullValue()));
    assertThat(result.getTitle(), is(title));
    assertThat(result.getDescription(), is(description));
    assertThat(result.getContentType(), is(MimeTypes.BZ2_ARCHIVE_MIME_TYPE));
    assertThat(result.getMajorVersion(), is(2));
    assertThat(result.getMinorVersion(), is(0));
    assertThat(result.getHistory(), is(notNullValue()));
    assertThat(result.getHistory(), hasSize(1));
    String foreignId = "node56";
    SimpleDocumentPK copyPk = instance.copyDocument(result, new ForeignPK(foreignId, instanceId));
    HistorisedDocument copy = (HistorisedDocument) instance.searchDocumentById(copyPk, "fr");
    assertThat(copy, is(notNullValue()));
    assertThat(copy.getAlert(), is(nullValue()));
    assertThat(copy.getTitle(), is(title));
    assertThat(copy.getDescription(), is(description));
    assertThat(copy.getContentType(), is(MimeTypes.BZ2_ARCHIVE_MIME_TYPE));
    assertThat(copy.getMajorVersion(), is(2));
    assertThat(copy.getMinorVersion(), is(0));
    assertThat(copy.getHistory(), is(notNullValue()));
    assertThat(copy.getHistory(), hasSize(1));
  }

  /**
   * Test of listDocumentsToUnlock method, of class AttachmentService.
   */
  @Test
  public void testListDocumentsToUnlock() {
    ByteArrayInputStream content = new ByteArrayInputStream("This is a test".getBytes(
        Charsets.UTF_8));
    SimpleDocumentPK emptyId = new SimpleDocumentPK("-1", instanceId);
    String foreignId = "node18";
    String owner = "10";
    Calendar today = Calendar.getInstance();
    DateUtil.setAtBeginOfDay(today);
    SimpleDocument docToLeaveLocked1 = new SimpleDocument(emptyId, foreignId, 10, false, owner,
        new SimpleAttachment("test.pdf", "en", "My test document",
        "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
        MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18"));
    docToLeaveLocked1.setExpiry(today.getTime());
    instance.createAttachment(docToLeaveLocked1, content);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument docToUnlock2 = new SimpleDocument(emptyId, foreignId, 15, false, owner,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        "10", RandomGenerator.getRandomCalendar().getTime(), "5"));
    docToUnlock2.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
    instance.createAttachment(docToUnlock2, content);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument docToUnlock3 = new SimpleDocument(emptyId, foreignId, 20, false, owner,
        new SimpleAttachment("test.pdf", "en", "My test document",
        "This is a test document", "This is a test".getBytes(Charsets.UTF_8).length,
        MimeTypes.PDF_MIME_TYPE, "0", RandomGenerator.getRandomCalendar().getTime(), "18"));
    docToUnlock3.setExpiry(RandomGenerator.getCalendarBefore(today).getTime());
    instance.createAttachment(docToUnlock3, content);
    emptyId = new SimpleDocumentPK("-1", instanceId);
    SimpleDocument docToLeaveLocked4 = new SimpleDocument(emptyId, foreignId, 25, false, owner,
        new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un test".getBytes(Charsets.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
        "10", RandomGenerator.getRandomCalendar().getTime(), "5"));
    Calendar beforeDate = RandomGenerator.getCalendarAfter(today);
    docToLeaveLocked4.setExpiry(beforeDate.getTime());
    instance.createAttachment(docToLeaveLocked4, content);
    List<SimpleDocument> docs = instance.listDocumentsToUnlock(today.getTime(), "fr");
    assertThat(docs, is(notNullValue()));
    assertThat(docs.size(), is(2));
    assertThat(docs, contains(docToUnlock2, docToUnlock3));
  }

  private void checkFrenchSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
    assertThat(doc.getSize(), is((long) ("Ceci est un test".getBytes(Charsets.UTF_8).length)));
    assertThat(doc.getDescription(), is("Ceci est un document de test"));
    assertThat(doc.getNodeName(), is(notNullValue()));
    File file = new File(doc.getAttachmentPath());
    assertThat(file, is(notNullValue()));
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    assertThat(file.length(), is((long) ("Ceci est un test".getBytes(Charsets.UTF_8).length)));
  }

  private void checkFrenchFileSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.MIME_TYPE_OO_PRESENTATION));
    assertThat(doc.getSize(), is(12929L));
    assertThat(doc.getNodeName(), is(notNullValue()));
    assertThat(doc.getDescription(), is("Ceci est un document de test"));
    File file = new File(doc.getAttachmentPath());
    assertThat(file, is(notNullValue()));
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    assertThat(file.length(), is(12929L));
  }

  private void checkEnglishSimpleDocument(SimpleDocument doc) {
    assertThat(doc, is(notNullValue()));
    assertThat(doc.getContentType(), is(MimeTypes.WORD_2007_MIME_TYPE));
    assertThat(doc.getNodeName(), is(notNullValue()));
    assertThat(doc.getSize(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
    assertThat(doc.getDescription(), is("This is a test document"));
    assertThat(doc.getCreatedBy(), is("0"));
    File file = new File(doc.getAttachmentPath());
    assertThat(file, is(notNullValue()));
    assertThat(file.exists(), is(true));
    assertThat(file.isFile(), is(true));
    assertThat(file.length(), is((long) ("This is a test".getBytes(Charsets.UTF_8).length)));
  }

  protected File getDocumentNamed(final String name) {
    return FileUtils.getFile(getMavenTargetDirectory().getResourceTestDirFile(), name);
  }
}
