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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
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

import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.BetterRepositoryFactoryBean;
import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.model.impl.AbstractJcrRegisteringTestCase;
import com.silverpeas.jcrutil.security.impl.SilverpeasSystemCredentials;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.PathTestUtil;

import com.stratelia.webactiv.util.DBUtil;

import static com.silverpeas.jcrutil.JcrConstants.*;
import static javax.jcr.Property.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-pure-memory-jcr.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DocumentConverterTest {

  private static final String instanceId = "kmelia74";
  private static final DocumentConverter instance = new DocumentConverter();
  private boolean registred = false;
  private static BetterRepositoryFactoryBean shutdown;
  @Inject
  private BetterRepositoryFactoryBean helper;
  @Resource
  private Repository repository;
  private static EmbeddedDatabase dataSource;

  public DocumentConverterTest() {
  }

  public Repository getRepository() {
    return this.repository;
  }

  @Before
  public void setUp() throws RepositoryException, ParseException, IOException, SQLException {
    if (!registred) {
      Reader reader = null;
      try {
        reader = new InputStreamReader(AbstractJcrRegisteringTestCase.class.getClassLoader().
            getResourceAsStream("silverpeas-jcr.txt"));
        SilverpeasRegister.registerNodeTypes(reader);
      } finally {
        IOUtils.closeQuietly(reader);
      }
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
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));

  }

  @AfterClass
  public static void tearDown() throws Exception {
    shutdown.destroy();
    dataSource.shutdown();
    DBUtil.clearTestInstance();
    FileUtils.deleteQuietly(new File(PathTestUtil.TARGET_DIR + "tmp" + File.separatorChar
        + "temp_jackrabbit"));
  }

  /**
   * Test of convertNode method, of class SimpleDocumentConverter.
   */
  @Test
  public void testConvertNode() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleDocument expectedResult = new SimpleDocument(new SimpleDocumentPK("-1", instanceId),
        foreignId, order, versionned, owner, reservation.getTime(), alert.getTime(),
        expiry.getTime(), status,
        new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes("UTF-8").length, MimeTypes.PDF_MIME_TYPE, creatorId, creationDate,
        formId));
    expectedResult.setOldSilverpeasId(oldSilverpeasId);
    expectedResult.getFile().setUpdated(updateDate);
    expectedResult.getFile().setUpdatedBy(updatedBy);
    expectedResult.setMajorVersion(1);
    expectedResult.setMinorVersion(2);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.setProperty(SLV_PROPERTY_FOREIGN_KEY, foreignId);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, versionned);
      documentNode.setProperty(SLV_PROPERTY_ORDER, order);
      documentNode.setProperty(SLV_PROPERTY_OLD_ID, oldSilverpeasId);
      documentNode.setProperty(SLV_PROPERTY_INSTANCEID, instanceId);
      documentNode.setProperty(SLV_PROPERTY_OWNER, owner);
      documentNode.setProperty(SLV_PROPERTY_STATUS, status);
      documentNode.setProperty(SLV_PROPERTY_ALERT_DATE, alert);
      documentNode.setProperty(SLV_PROPERTY_EXPIRY_DATE, expiry);
      documentNode.setProperty(SLV_PROPERTY_RESERVATION_DATE, reservation);
      documentNode.setProperty(SLV_PROPERTY_MAJOR, 1);
      documentNode.setProperty(SLV_PROPERTY_MINOR, 2);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.addNode(attachmentNodeName, SLV_SIMPLE_ATTACHMENT);
      attachNode.setProperty(SLV_PROPERTY_NAME, fileName);
      attachNode.setProperty(SLV_PROPERTY_CREATOR, creatorId);
      attachNode.setProperty(JCR_LANGUAGE, language);
      attachNode.setProperty(JCR_TITLE, title);
      attachNode.setProperty(JCR_DESCRIPTION, description);
      Calendar calend = Calendar.getInstance();
      calend.setTime(creationDate);
      attachNode.setProperty(SLV_PROPERTY_CREATION_DATE, calend);
      attachNode.setProperty(SLV_PROPERTY_XMLFORM_ID, formId);
      attachNode.setProperty(JCR_LAST_MODIFIED_BY, updatedBy);
      calend.setTime(updateDate);
      attachNode.setProperty(JCR_LAST_MODIFIED, calend);
      Node contentNode = attachNode.addNode(JCR_CONTENT, NT_RESOURCE);
      InputStream in = new ByteArrayInputStream("my test content".getBytes("UTF-8"));
      Binary binaryContent = session.getValueFactory().createBinary(in);
      contentNode.setProperty(JCR_DATA, binaryContent);
      binaryContent.dispose();
      contentNode.setProperty(JCR_MIMETYPE, MimeTypes.PDF_MIME_TYPE);
      contentNode.setProperty(JCR_ENCODING, "UTF-8");
      SimpleDocument result = instance.convertNode(documentNode, language);
      expectedResult.setId(result.getId());
      assertThat(result, SimpleDocumentMatcher.matches(expectedResult));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of getAttachment method, of class SimpleDocumentConverter.
   */
  @Test
  public void testGetAttachment() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment expectedResult =
        new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes("UTF-8").length, MimeTypes.PDF_MIME_TYPE, creatorId, creationDate,
        formId);
    expectedResult.setUpdated(updateDate);
    expectedResult.setUpdatedBy(updatedBy);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.setProperty(SLV_PROPERTY_FOREIGN_KEY, foreignId);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, versionned);
      documentNode.setProperty(SLV_PROPERTY_ORDER, order);
      documentNode.setProperty(SLV_PROPERTY_OLD_ID, oldSilverpeasId);
      documentNode.setProperty(SLV_PROPERTY_INSTANCEID, instanceId);
      documentNode.setProperty(SLV_PROPERTY_OWNER, owner);
      documentNode.setProperty(SLV_PROPERTY_STATUS, status);
      documentNode.setProperty(SLV_PROPERTY_ALERT_DATE, alert);
      documentNode.setProperty(SLV_PROPERTY_EXPIRY_DATE, expiry);
      documentNode.setProperty(SLV_PROPERTY_RESERVATION_DATE, reservation);      
      documentNode.setProperty(SLV_PROPERTY_MAJOR, 1);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.addNode(attachmentNodeName, SLV_SIMPLE_ATTACHMENT);
      attachNode.setProperty(SLV_PROPERTY_NAME, fileName);
      attachNode.setProperty(SLV_PROPERTY_CREATOR, creatorId);
      attachNode.setProperty(JCR_LANGUAGE, language);
      attachNode.setProperty(JCR_TITLE, title);
      attachNode.setProperty(JCR_DESCRIPTION, description);
      Calendar calend = Calendar.getInstance();
      calend.setTime(creationDate);
      attachNode.setProperty(SLV_PROPERTY_CREATION_DATE, calend);
      attachNode.setProperty(SLV_PROPERTY_XMLFORM_ID, formId);
      attachNode.setProperty(JCR_LAST_MODIFIED_BY, updatedBy);
      calend.setTime(updateDate);
      attachNode.setProperty(JCR_LAST_MODIFIED, calend);
      Node contentNode = attachNode.addNode(JCR_CONTENT, NT_RESOURCE);
      InputStream in = new ByteArrayInputStream("my test content".getBytes("UTF-8"));
      Binary binaryContent = session.getValueFactory().createBinary(in);
      contentNode.setProperty(JCR_DATA, binaryContent);
      binaryContent.dispose();
      contentNode.setProperty(JCR_MIMETYPE, MimeTypes.PDF_MIME_TYPE);
      contentNode.setProperty(JCR_ENCODING, "UTF-8");
      SimpleAttachment result = instance.getAttachment(documentNode, language);
      assertThat(result, SimpleAttachmentMatcher.matches(expectedResult));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of getAttachment method, of class SimpleDocumentConverter.
   */
  @Test
  public void testGetNoAttachment() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String status = "My Status";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;


    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.setProperty(SLV_PROPERTY_FOREIGN_KEY, foreignId);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, versionned);
      documentNode.setProperty(SLV_PROPERTY_ORDER, order);
      documentNode.setProperty(SLV_PROPERTY_OLD_ID, oldSilverpeasId);
      documentNode.setProperty(SLV_PROPERTY_INSTANCEID, instanceId);
      documentNode.setProperty(SLV_PROPERTY_OWNER, owner);
      documentNode.setProperty(SLV_PROPERTY_STATUS, status);
      documentNode.setProperty(SLV_PROPERTY_ALERT_DATE, alert);
      documentNode.setProperty(SLV_PROPERTY_EXPIRY_DATE, expiry);
      documentNode.setProperty(SLV_PROPERTY_RESERVATION_DATE, reservation);
      SimpleAttachment result = instance.getAttachment(documentNode, language);
      assertThat(result, is(nullValue()));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of fillNode method, of class SimpleDocumentConverter.
   */
  @Test
  public void testFillNodeFromSimpleDocument() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK("-1", instanceId),
        foreignId, order, versionned, owner, reservation.getTime(), alert.getTime(),
        expiry.getTime(), status, new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes("UTF-8").length, MimeTypes.PDF_MIME_TYPE, creatorId, creationDate,
        formId));
    document.setMajorVersion(1);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getFile().setUpdated(updateDate);
    document.getFile().setUpdatedBy(updatedBy);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, documentNode);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FOREIGN_KEY).getString(), is(foreignId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_VERSIONED).getBoolean(), is(versionned));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ORDER).getLong(), is((long) order));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong(), is(oldSilverpeasId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_INSTANCEID).getString(), is(instanceId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OWNER).getString(), is(owner));
      assertThat(documentNode.getProperty(SLV_PROPERTY_STATUS).getString(), is(status));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ALERT_DATE).getDate().getTimeInMillis(),
          is(alert.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_EXPIRY_DATE).getDate().getTimeInMillis(),
          is(expiry.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_RESERVATION_DATE).getDate().getTimeInMillis(),
          is(reservation.getTimeInMillis()));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED_BY).getString(), is(updatedBy));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          is(updateDate.getTime()));
      assertThat(attachNode.hasNode(JCR_CONTENT), is(false));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of fillNode method, of class SimpleDocumentConverter.
   */
  @Test
  public void testFillNodeFromSimpleDocumentAndContent() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK("-1", instanceId),
        foreignId, order, versionned, owner, reservation.getTime(), alert.getTime(),
        expiry.getTime(), status, new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes(CharEncoding.UTF_8).length, MimeTypes.PDF_MIME_TYPE, creatorId,
        creationDate, formId));
    document.setMajorVersion(1);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getFile().setUpdated(updateDate);
    document.getFile().setUpdatedBy(updatedBy);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, new ByteArrayInputStream("my test content".getBytes(
          CharEncoding.UTF_8)), documentNode);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FOREIGN_KEY).getString(), is(foreignId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_VERSIONED).getBoolean(), is(versionned));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ORDER).getLong(), is((long) order));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong(), is(oldSilverpeasId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_INSTANCEID).getString(), is(instanceId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OWNER).getString(), is(owner));
      assertThat(documentNode.getProperty(SLV_PROPERTY_STATUS).getString(), is(status));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ALERT_DATE).getDate().getTimeInMillis(),
          is(alert.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_EXPIRY_DATE).getDate().getTimeInMillis(),
          is(expiry.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_RESERVATION_DATE).getDate().getTimeInMillis(),
          is(reservation.getTimeInMillis()));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED_BY).getString(), is(updatedBy));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          is(updateDate.getTime()));
      assertThat(attachNode.hasNode(JCR_CONTENT), is(true));
      Node contentNode = attachNode.getNode(JCR_CONTENT);
      assertThat(contentNode, is(notNullValue()));
      Binary binaryContent = contentNode.getProperty(JCR_DATA).getBinary();
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      InputStream in = binaryContent.getStream();
      IOUtils.copy(in, buffer);
      in.close();
      binaryContent.dispose();
      assertThat(new String(buffer.toByteArray(), CharEncoding.UTF_8), is("my test content"));
      assertThat(contentNode.getProperty(JCR_MIMETYPE).getString(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(contentNode.getProperty(JCR_ENCODING).getString(), is(CharEncoding.UTF_8));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of addAttachment method, of class SimpleDocumentConverter.
   * @throws Exception 
   */
  @Test
  public void testAddAttachment() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();
    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes(CharEncoding.UTF_8).length, MimeTypes.PDF_MIME_TYPE, creatorId,
        creationDate, formId);
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK("-1", instanceId),
        foreignId, order, versionned, owner, reservation.getTime(), alert.getTime(),
        expiry.getTime(), status, attachment);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getFile().setUpdated(updateDate);
    document.getFile().setUpdatedBy(updatedBy);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, new ByteArrayInputStream("my test content".getBytes(
          CharEncoding.UTF_8)), documentNode);
      fileName = "essai.odp";
      title = "Mon titre";
      description = "Ceci est un document de test";
      creatorId = "73";
      formId = "38";
      creationDate = new Date();
      attachment =
          new SimpleAttachment(fileName, language, title, description,
          "Contenu de test".getBytes(CharEncoding.UTF_8).length, MimeTypes.MIME_TYPE_OO_PRESENTATION,
          creatorId, creationDate, formId);
      instance.addAttachment(documentNode, attachment, new ByteArrayInputStream("Contenu de test".
          getBytes(CharEncoding.UTF_8)));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.hasProperty(JCR_LAST_MODIFIED_BY), is(false));
      assertThat(attachNode.hasProperty(JCR_LAST_MODIFIED), is(false));
      assertThat(attachNode.hasNode(JCR_CONTENT), is(true));
      Node contentNode = attachNode.getNode(JCR_CONTENT);
      assertThat(contentNode, is(notNullValue()));
      Binary binaryContent = contentNode.getProperty(JCR_DATA).getBinary();
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();
      InputStream in = binaryContent.getStream();
      IOUtils.copy(in, buffer);
      in.close();
      binaryContent.dispose();
      assertThat(new String(buffer.toByteArray(), CharEncoding.UTF_8), is("Contenu de test"));
      assertThat(contentNode.getProperty(JCR_MIMETYPE).getString(), is(
          MimeTypes.MIME_TYPE_OO_PRESENTATION));
      assertThat(contentNode.getProperty(JCR_ENCODING).getString(), is(CharEncoding.UTF_8));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  /**
   * Test of removeAttachment method, of class SimpleDocumentConverter.
   */
  @Test
  public void testRemoveAttachment() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();
    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK("-1", instanceId),
        foreignId, order, versionned, owner, reservation.getTime(), alert.getTime(),
        expiry.getTime(), status, new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes(CharEncoding.UTF_8).length, MimeTypes.PDF_MIME_TYPE, creatorId,
        creationDate, formId));
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getFile().setUpdated(updateDate);
    document.getFile().setUpdatedBy(updatedBy);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, new ByteArrayInputStream("my test content".getBytes(
          CharEncoding.UTF_8)), documentNode);
      instance.removeAttachment(documentNode, language);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      assertThat(documentNode.hasNode(attachmentNodeName), is(false));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
  
  
  /**
   * Test of fillNode method, of class SimpleDocumentConverter.
   */
  @Test
  public void testUpdateDocumentVersion() throws Exception {
    Session session = BasicDaoFactory.getSystemSession();
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versionned = true;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleDocument document = new SimpleDocument(new SimpleDocumentPK("-1", instanceId),
        foreignId, order, versionned, owner, reservation.getTime(), alert.getTime(),
        expiry.getTime(), status, new SimpleAttachment(fileName, language, title, description,
        "my test content".getBytes("UTF-8").length, MimeTypes.PDF_MIME_TYPE, creatorId, creationDate,
        formId));
    document.setMajorVersion(1);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getFile().setUpdated(updateDate);
    document.getFile().setUpdatedBy(updatedBy);
    try {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.addMixin(NodeType.MIX_SIMPLE_VERSIONABLE);
      instance.fillNode(document, documentNode);
      instance.updateVersion(documentNode, true);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FOREIGN_KEY).getString(), is(foreignId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_VERSIONED).getBoolean(), is(versionned));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ORDER).getLong(), is((long) order));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong(), is(oldSilverpeasId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_INSTANCEID).getString(), is(instanceId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OWNER).getString(), is(owner));
      assertThat(documentNode.getProperty(SLV_PROPERTY_STATUS).getString(), is(status));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ALERT_DATE).getDate().getTimeInMillis(),
          is(alert.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_EXPIRY_DATE).getDate().getTimeInMillis(),
          is(expiry.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_RESERVATION_DATE).getDate().getTimeInMillis(),
          is(reservation.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_MAJOR).getLong(), is(1L));
      assertThat(documentNode.getProperty(SLV_PROPERTY_MINOR).getLong(), is(0L));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED_BY).getString(), is(updatedBy));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          is(updateDate.getTime()));
      assertThat(attachNode.hasNode(JCR_CONTENT), is(false));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }
}
