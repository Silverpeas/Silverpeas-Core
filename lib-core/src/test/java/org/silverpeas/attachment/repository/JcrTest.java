/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.FileRepositoryManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.tika.mime.MimeTypes;
import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.util.Charsets;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This class handle a JCR test with using a Spring context.
 * @author: Yohann Chastagnier
 */
public abstract class JcrTest {

  private ClassPathXmlApplicationContext appContext;

  private DocumentRepository documentRepository = new DocumentRepository();
  private final DocumentConverter converter = new DocumentConverter();

  public ClassPathXmlApplicationContext getAppContext() {
    return appContext;
  }

  public void setAppContext(final ClassPathXmlApplicationContext appContext) {
    this.appContext = appContext;
  }

  private DocumentRepository getDocumentRepository() {
    return documentRepository;
  }

  private JackrabbitRepository getRepository() {
    return ((JackrabbitRepository) getAppContext().getBean(BasicDaoFactory.JRC_REPOSITORY));
  }

  public abstract void run() throws Exception;


  /**
   * Execute the test with its context.
   * @throws Exception
   */
  public void execute() throws Exception {
    setAppContext(new ClassPathXmlApplicationContext("/spring-pure-memory-jcr.xml",
        "/spring-uniqueid-datasource.xml"));
    DataSource dbDataSource = (DataSource) getAppContext().getBean("dataSource");
    Reader reader = new InputStreamReader(
        JcrTest.class.getClassLoader().getResourceAsStream("silverpeas-jcr.txt"), Charsets.UTF_8);
    File file = new File(FileRepositoryManager.getAbsolutePath(""));
    FileUtils.deleteQuietly(file);
    try {
      DBUtil.getInstanceForTest(dbDataSource.getConnection());
      try {
        SilverpeasRegister.registerNodeTypes(reader);
      } finally {
        IOUtils.closeQuietly(reader);
      }
      run();
    } finally {
      DBUtil.clearTestInstance();
      file = new File(FileRepositoryManager.getAbsolutePath(""));
      FileUtils.deleteQuietly(file);
      getRepository().shutdown();
      getAppContext().close();
    }
  }

  /**
   * Common method to assert a document existence.
   * @param uuId the uuId to retrieve the document.
   */
  public void assertDocumentExists(String uuId) throws Exception {
    SimpleDocument document = getDocumentById(uuId);
    assertThat(document, notNullValue());
  }

  /**
   * Common method to assert a document existence.
   * @param uuId the uuId to retrieve the document.
   */
  public void assertDocumentDoesNotExist(String uuId) throws Exception {
    SimpleDocument document = getDocumentById(uuId);
    assertThat(document, nullValue());
  }

  /**
   * Common method to assert the content for a language of a document.
   * @param uuId the uuId to retrieve the document.
   * @param language the language in which the content must be verified.
   * @param expectedContent if null, the content for the language does not exist.
   * @return the document loaded for assertions.
   */
  public SimpleDocument assertContent(String uuId, String language, String expectedContent)
      throws Exception {
    SimpleDocument document = getDocumentById(uuId, language);
    assertThat(document, notNullValue());
    final File physicalContent;
    if (document.getFile() != null) {
      physicalContent = new File(FileRepositoryManager.getAbsolutePath(document.getInstanceId()),
          document.getNodeName() + "/" +
              document.getMajorVersion() + "_" + document.getMinorVersion() + "/" + language + "/" +
              document.getFilename()
      );
    } else {
      physicalContent = new File(FileRepositoryManager.getAbsolutePath(document.getInstanceId()),
          document.getNodeName() + "/" +
              document.getMajorVersion() + "_" + document.getMinorVersion() + "/" + language
      );
    }
    if (expectedContent == null) {
      assertThat(document.getFile(), nullValue());
      assertThat(physicalContent.exists(), is(false));
    } else {
      assertThat(document.getFile(), notNullValue());
      assertThat(document.getLanguage(), is(language));
      assertThat(physicalContent.exists(), is(true));
      ByteArrayOutputStream content = new ByteArrayOutputStream();
      AttachmentServiceFactory.getAttachmentService()
          .getBinaryContent(content, document.getPk(), language);
      assertThat(content.toString(org.apache.commons.io.Charsets.UTF_8.name()),
          is(expectedContent));
    }
    return document;
  }

  public SimpleDocument getDocumentById(String uuId, String language) throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocument document =
          getDocumentRepository().findDocumentById(session, new SimpleDocumentPK(uuId), language);
      if (StringUtil.isDefined(language) && document != null &&
          !language.equals(document.getLanguage())) {
        document.setFile(null);
      }
      return document;
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  protected SimpleDocument getDocumentById(String uuId) throws Exception {
    return getDocumentById(uuId, null);
  }

  protected SimpleDocument defaultDocument(String instanceId, String foreignId,
      SimpleAttachment file) {
    SimpleDocument document = new SimpleDocument();
    document.setPK(new SimpleDocumentPK("-1", instanceId));
    document.setForeignId(foreignId);
    document.setFile(file);
    return document;
  }

  public SimpleDocument defaultDocument(String instanceId, String foreignId) {
    return defaultDocument(instanceId, foreignId, null);
  }

  protected SimpleAttachment defaultENContent() {
    return new SimpleAttachment("test.pdf", "en", "My test document", "This is a test document",
        "This is a test document".getBytes(org.apache.commons.io.Charsets.UTF_8).length,
        MimeTypes.OCTET_STREAM, "0", randomDate(), "18");

  }

  public SimpleAttachment defaultFRContent() {
    return new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un document de test".getBytes(org.apache.commons.io.Charsets.UTF_8).length,
        MimeTypes.PLAIN_TEXT, "10", randomDate(), "5");
  }

  public SimpleAttachment defaultDEContent() {
    return new SimpleAttachment("test.docx", "de", "Mein Test-Dokument",
        "Dies ist ein Testdokument",
        "Dies ist ein Testdokument".getBytes(org.apache.commons.io.Charsets.UTF_8).length,
        MimeTypes.XML, "10", randomDate(), "2");
  }

  protected Date randomDate() {
    long offset = Timestamp.valueOf("2014-01-01 00:00:00").getTime();
    long end = Timestamp.valueOf("2014-04-30 00:00:00").getTime();
    long diff = end - offset + 1;
    return new Timestamp(offset + (long) (Math.random() * diff));
  }

  /**
   * Creates an Image Master Node into the JCR
   * @param document
   * @param attachment
   * @param content
   * @return
   * @throws Exception
   */
  public SimpleDocument createAttachmentForTest(SimpleDocument document,
      SimpleAttachment attachment, String content) throws Exception {
    document.setFile(attachment);
    return createDocumentIntoJcr(document, content);
  }

  /**
   * Creates an Image Master Node into the JCR
   * @param document
   * @param language
   * @param content
   * @return
   * @throws Exception
   */
  public SimpleDocument updateAttachmentForTest(SimpleDocument document, String language,
      String content) throws Exception {
    SimpleDocument documentToUpdate = new SimpleDocument(document);
    documentToUpdate.setLanguage(language);
    return updateDocumentIntoJcr(documentToUpdate, content);
  }

  /**
   * Creates an Image Master Node into the JCR
   * @param document
   * @return
   * @throws Exception
   */
  protected SimpleDocument updateAttachmentForTest(SimpleDocument document) throws Exception {
    SimpleDocument documentToUpdate = new SimpleDocument(document);
    return updateDocumentIntoJcr(documentToUpdate, null);
  }

  /**
   * Creates a master document NODE into the JCR.
   * @param document
   * @return
   * @throws Exception
   */
  private SimpleDocument createDocumentIntoJcr(SimpleDocument document, String content)
      throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    try {
      SimpleDocumentPK createdPk = getDocumentRepository().createDocument(session, document);
      session.save();
      long contentSizeWritten = getDocumentRepository().storeContent(document,
          new ByteArrayInputStream(content.getBytes(org.apache.commons.io.Charsets.UTF_8)));
      assertThat(contentSizeWritten, is((long) content.length()));
      SimpleDocument createdDocument =
          getDocumentRepository().findDocumentById(session, createdPk, document.getLanguage());
      assertThat(createdDocument, notNullValue());
      assertThat(createdDocument.getOrder(), is(document.getOrder()));
      assertThat(createdDocument.getLanguage(), is(document.getLanguage()));
      return createdDocument;
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Creates a master document NODE into the JCR.
   * @param document
   * @return
   * @throws Exception
   */
  private SimpleDocument updateDocumentIntoJcr(SimpleDocument document, String content)
      throws Exception {
    assertThat(document.getPk(), notNullValue());
    assertThat(document.getId(), not(isEmptyString()));
    assertThat(document.getInstanceId(), not(isEmptyString()));
    assertThat(document.getOldSilverpeasId(), greaterThan(0L));
    Session session = BasicDaoFactory.getSystemSession();
    try {
      Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
      converter.fillNode(document, documentNode);
      session.save();
      if (content != null) {
        long contentSizeWritten = getDocumentRepository().storeContent(document,
            new ByteArrayInputStream(content.getBytes(org.apache.commons.io.Charsets.UTF_8)));
        assertThat(contentSizeWritten, is((long) content.length()));
      }
      SimpleDocument updatedDocument = getDocumentRepository()
          .findDocumentById(session, document.getPk(), document.getLanguage());
      assertThat(updatedDocument, notNullValue());
      assertThat(updatedDocument.getLanguage(), is(document.getLanguage()));
      return updatedDocument;
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
}
