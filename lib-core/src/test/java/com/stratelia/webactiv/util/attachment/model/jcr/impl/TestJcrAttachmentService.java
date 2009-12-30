/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.attachment.model.jcr.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.attachment.model.jcr.JcrAttachmentService;
import static com.silverpeas.util.PathTestUtil.*;

public class TestJcrAttachmentService extends AbstractJcrTestCase {

  private JcrAttachmentService service;

  private static final String instanceId = "kmelia57";

  private static final String UPLOAD_DIR = BUILD_PATH + SEPARATOR + "uploads" +
      SEPARATOR + instanceId + SEPARATOR + "Attachment" + SEPARATOR + "tests" +
      SEPARATOR + "simpson" + SEPARATOR + "bart" + SEPARATOR;

  public void setJcrAttachmentManager(JcrAttachmentService service) {
    this.service = service;
  }

  @Override
  protected void onTearDown() throws Exception {
    super.onTearDown();
    clearRepository();
    File uploadDir = new File(UPLOAD_DIR);
    uploadDir.delete();
  }

  protected void prepareUploadedFile(String fileName, String physicalName)
      throws IOException {
    InputStream in = null;
    FileOutputStream out = null;
    try {
      in = this.getClass().getClassLoader().getResourceAsStream(fileName);
      File destinationDir = new File(UPLOAD_DIR);
      destinationDir.mkdirs();
      File destinationFile = new File(destinationDir, physicalName);
      out = new FileOutputStream(destinationFile);
      int c = 0;
      byte[] buffer = new byte[8];
      while ((c = in.read(buffer)) >= 0) {
        out.write(buffer, 0, c);
      }
      out.close();
      out = null;
    } finally {
      if (in != null) {
        in.close();
      }
      if (out != null) {
        out.close();
      }
    }
  }

  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();
    calend = Calendar.getInstance();
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.HOUR, 9);
    calend.set(Calendar.DAY_OF_MONTH, 12);
    calend.set(Calendar.MONTH, Calendar.MARCH);
    calend.set(Calendar.YEAR, 2008);
  }

  public void testCreateAttachmentWithLanguage() throws Exception {
    registerSilverpeasNodeTypes();
    prepareUploadedFile("FrenchScrum.odp",
        "abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
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
      pathNode = session.getRootNode()
          .getNode(
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
      assertEquals("application/vnd.oasis.opendocument.presentation", content
          .getProperty("jcr:mimeType").getString());
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void testCreateAttachmentWithoutLanguage() throws Exception {
    registerSilverpeasNodeTypes();
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
      assertEquals("application/vnd.oasis.opendocument.presentation", content
          .getProperty("jcr:mimeType").getString());
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  public void testGetUpdatedDocument() throws Exception {
    registerSilverpeasNodeTypes();
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
      assertEquals("application/vnd.oasis.opendocument.presentation", content
          .getProperty("jcr:mimeType").getString());
      ByteArrayInputStream in = new ByteArrayInputStream("Ce test fonctionne."
          .getBytes());
      content.setProperty("jcr:data", in);
      session.save();
    } finally {
      if (session != null) {
        session.logout();
      }
    }
    service.getUpdatedDocument(attachment, I18NHelper.defaultLanguage);
    String result = readFile(UPLOAD_DIR + "test_update.txt");
    assertEquals("Ce test fonctionne.", result);
  }

  public void testDeleteAttachment() throws Exception {
    registerSilverpeasNodeTypes();
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

  @Override
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
}