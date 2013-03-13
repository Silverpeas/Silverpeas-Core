/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.webactiv.util.attachment.model.jcr.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.naming.InitialContext;

import org.silverpeas.util.Charsets;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.jcrutil.model.SilverpeasRegister;
import com.silverpeas.jcrutil.security.jaas.TestAccessAuthentified;
import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.util.i18n.I18NHelper;

import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.jcr.JcrAttachmentService;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static com.silverpeas.util.PathTestUtil.SEPARATOR;
import static com.silverpeas.util.PathTestUtil.TARGET_DIR;
import static org.junit.Assert.*;

public class TestJcrAttachmentService {
  private static JcrAttachmentService service;
  private Calendar calend;
  private static final String instanceId = "kmelia57";
  private static final String UPLOAD_DIR = TARGET_DIR + SEPARATOR + "temp" + SEPARATOR + "uploads"
      + SEPARATOR + instanceId + SEPARATOR + "Attachment" + SEPARATOR + "tests" + SEPARATOR
      + "simpson" + SEPARATOR + "bart" + SEPARATOR;
  private static ClassPathXmlApplicationContext context;
  private static JackrabbitRepository repository;
  private static BasicDataSource datasource;

  @BeforeClass
  public static void loadSpringContext() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
    context = new ClassPathXmlApplicationContext("/spring-in-memory-jcr.xml");
    repository = context.getBean("repository", JackrabbitRepository.class);
    String cndFileName = TestAccessAuthentified.class.getClassLoader().getResource(
        "silverpeas-jcr.txt").getFile().replaceAll("%20", " ");
    BasicDaoFactory.getInstance().setApplicationContext(context);
    SilverpeasRegister.registerNodeTypes(cndFileName);
    datasource = context.getBean("dataSource", BasicDataSource.class);
    service = context.getBean("jcrAttachmentManager", JcrAttachmentService.class);
    InitialContext ic = new InitialContext();
    ic.rebind(JNDINames.DATABASE_DATASOURCE, datasource);
    ic.rebind(JNDINames.ADMIN_DATASOURCE, datasource);
    System.out.println(" -> node types registered");
  }

  @AfterClass
  public static void tearAlldown() throws Exception {
    repository.shutdown();
    datasource.close();
    context.close();
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  protected void prepareUploadedFile(String fileName, String physicalName) throws IOException,
      URISyntaxException {
    File origin = new File(this.getClass().getClassLoader().getResource(fileName).toURI().getPath());
    File destinationDir = new File(UPLOAD_DIR);
    File destinationFile = new File(destinationDir, physicalName);
    FileUtils.copyFile(origin, destinationFile);
  }

  @Before
  public void onSetUp() throws Exception {
    calend = Calendar.getInstance();
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.HOUR, 9);
    calend.set(Calendar.DAY_OF_MONTH, 12);
    calend.set(Calendar.MONTH, Calendar.MARCH);
    calend.set(Calendar.YEAR, 2008);
    setUpDatabase();
  }

  public void setUpDatabase() throws Exception {
    IDatabaseConnection connection = null;
    try {
      connection = new DatabaseConnection(datasource.getConnection());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
    } catch (Exception ex) {
      throw ex;
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          throw e;
        }
      }
    }
  }
  public void tearDownDatabase() throws Exception {
    clearRepository();
    IDatabaseConnection connection = null;
    try {
      connection = new DatabaseConnection(datasource.getConnection());
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
    } catch (Exception ex) {
      throw ex;
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          throw e;
        }
      }
    }
  }

  protected IDataSet getDataSet() throws Exception {
    InputStream in = this.getClass().getResourceAsStream("test-attachment-dataset.xml");
    try {
      ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(in));
      dataSet.addReplacementObject("[NULL]", null);
      return dataSet;
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  @After
  public void onTearDown() throws Exception {
    File uploadDir = new File(UPLOAD_DIR);
    FileUtils.deleteDirectory(uploadDir);
    tearDownDatabase();
  }

  @Test
  public void testCreateAttachmentWithLanguage() throws Exception {
    prepareUploadedFile("FrenchScrum.odp", "abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    AttachmentPK pk = new AttachmentPK("100", instanceId);
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId(instanceId);
    attachment.setPK(pk);
    attachment.setContext("tests,simpson,bart");
    attachment.setCreationDate(calend.getTime());
    attachment.setDescription("Attachment for tests");
    attachment.setLanguage("fr");
    attachment.setLogicalName("frenchScrum.odp");
    attachment.setPhysicalName("abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/vnd.oasis.opendocument.presentation");
    attachment.setTitle("Test OpenOffice");
    attachment.setWorkerId("worker");
    service.createAttachment(attachment, "en");
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node pathNode = session.getRootNode().getNode("attachments");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode("attachments");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode("attachments/kmelia57");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson/bart");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId
          + "/Attachment/tests/simpson/bart/100/en");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      Node fileNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/"
          + "Attachment/tests/simpson/bart/100/en/frenchScrum.odp");
      assertNotNull(fileNode);
      assertEquals("nt:file", fileNode.getPrimaryNodeType().getName());
      Node content = fileNode.getNode("jcr:content");
      assertEquals("nt:resource", content.getPrimaryNodeType().getName());
      assertNotNull(content);
      assertEquals("nt:resource", content.getPrimaryNodeType().getName());
      assertNotNull(content.getProperty("jcr:mimeType"));
      assertEquals("application/vnd.oasis.opendocument.presentation", content.getProperty(
          "jcr:mimeType").getString());
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Test
  public void testCreateAttachmentWithoutLanguage() throws Exception {
    prepareUploadedFile("FrenchScrum.odp", "abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    AttachmentPK pk = new AttachmentPK("100", instanceId);
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId(instanceId);
    attachment.setPK(pk);
    attachment.setContext("tests,simpson,bart");
    attachment.setCreationDate(calend.getTime());
    attachment.setDescription("Attachment for tests");
    attachment.setLanguage("fr");
    attachment.setLogicalName("frenchScrum.odp");
    attachment.setPhysicalName("abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/vnd.oasis.opendocument.presentation");
    attachment.setTitle("Test OpenOffice");
    attachment.setWorkerId("worker");
    service.createAttachment(attachment, null);
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node pathNode = session.getRootNode().getNode("attachments");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode("attachments");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode("attachments/kmelia57");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson/bart");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson/bart");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      Node fileNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/"
          + "Attachment/tests/simpson/bart/100/frenchScrum.odp");
      assertNotNull(fileNode);
      assertEquals("nt:file", fileNode.getPrimaryNodeType().getName());
      Node content = fileNode.getNode("jcr:content");
      assertEquals("nt:resource", content.getPrimaryNodeType().getName());
      assertNotNull(content);
      assertEquals("nt:resource", content.getPrimaryNodeType().getName());
      assertNotNull(content.getProperty("jcr:mimeType"));
      assertEquals("application/vnd.oasis.opendocument.presentation", content.getProperty(
          "jcr:mimeType").getString());
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Test
  public void testGetUpdatedDocument() throws Exception {
    createTempFile(UPLOAD_DIR + "test_update.txt", "Ceci est un test.");
    AttachmentPK pk = new AttachmentPK("100", instanceId);
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId(instanceId);
    attachment.setPK(pk);
    attachment.setContext("tests,simpson,bart");
    attachment.setCreationDate(calend.getTime());
    attachment.setDescription("Attachment for tests");
    attachment.setLanguage("fr");
    attachment.setLogicalName("test_update.txt");
    attachment.setPhysicalName("test_update.txt");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/vnd.oasis.opendocument.presentation");
    attachment.setTitle("Test OpenOffice");
    attachment.setWorkerId("worker");
    service.createAttachment(attachment, I18NHelper.defaultLanguage);
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node content = session.getRootNode().getNode(
          "attachments/" + instanceId + "/"
          + "Attachment/tests/simpson/bart/100/test_update.txt/jcr:content");
      assertNotNull(content);
      assertEquals("nt:resource", content.getPrimaryNodeType().getName());
      assertEquals("application/vnd.oasis.opendocument.presentation", content.getProperty(
          "jcr:mimeType").getString());
      ByteArrayInputStream in = new ByteArrayInputStream("Ce test fonctionne.".getBytes(Charsets.UTF_8));
      content.setProperty("jcr:data", in);
      session.save();
    } finally {
      if (session != null) {
        session.logout();
      }
      FileUtils.deleteQuietly(new File(UPLOAD_DIR, "test_update.txt"));
    }
    service.getUpdatedDocument(attachment, I18NHelper.defaultLanguage);
    String result = FileUtils.readFileToString(new File(UPLOAD_DIR, "test_update.txt"));
    assertEquals("Ce test fonctionne.", result);
  }

  @Test
  public void testDeleteAttachment() throws Exception {
    prepareUploadedFile("FrenchScrum.odp", "abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    AttachmentPK pk = new AttachmentPK("100", instanceId);
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId(instanceId);
    attachment.setPK(pk);
    attachment.setContext("tests,simpson,bart");
    attachment.setCreationDate(calend.getTime());
    attachment.setDescription("Attachment for tests");
    attachment.setLanguage("fr");
    attachment.setLogicalName("scrum.odp");
    attachment.setPhysicalName("abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/vnd.oasis.opendocument.presentation");
    attachment.setTitle("Test OpenOffice");
    attachment.setWorkerId("worker");
    service.createAttachment(attachment, null);
    service.deleteAttachment(attachment, null);
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node pathNode = session.getRootNode().getNode("attachments");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode("attachments");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode("attachments/kmelia57");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson/bart");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      pathNode = session.getRootNode().getNode(
          "attachments/" + instanceId + "/Attachment/tests/simpson/bart");
      assertNotNull(pathNode);
      assertEquals("nt:folder", pathNode.getPrimaryNodeType().getName());
      try {
        session.getRootNode().getNode(
            "attachments/" + instanceId + "/"
            + "Attachment/tests/simpson/bart/scrum.odp");
        fail("Node not deleted");
      } catch (PathNotFoundException ex) {
      }
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected void clearRepository() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      session.getRootNode().getNode("attachments").remove();
      session.save();
    } catch (PathNotFoundException pex) {
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  protected void createTempFile(String path, String content) throws IOException {
    File attachmentFile = new File(path);
    if (!attachmentFile.getParentFile().exists()) {
      attachmentFile.getParentFile().mkdirs();
    }
    FileUtils.write(attachmentFile, content);
  }
}