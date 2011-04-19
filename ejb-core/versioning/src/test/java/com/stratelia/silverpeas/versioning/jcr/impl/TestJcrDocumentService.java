/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.silverpeas.versioning.jcr.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.apache.jackrabbit.JcrConstants;

import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.util.MimeTypes;
import com.stratelia.silverpeas.versioning.jcr.JcrDocumentService;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import javax.annotation.Resource;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestJcrDocumentService extends AbstractJcrRegisteringTestCase {

  private JcrDocumentService service;

  private static final String instanceId = "kmelia60";
  private static final String UPLOAD_DIR = System.getProperty("basedir") + File.separatorChar + "target"
          + File.separatorChar + "uploads" + File.separatorChar + instanceId + File.separatorChar
          + "Versioning"  + File.separatorChar;



  @Override
  protected void clearRepository() throws Exception {
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      session.getRootNode().getNode(instanceId).remove();
      session.save();
    } catch (PathNotFoundException pex) {

    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Override
  public void onTearDown() throws Exception {
    super.onTearDown();
    File uploadDir = new File(UPLOAD_DIR);
    uploadDir.delete();
  }

  @Resource
  public void setJcrDocumentManager(JcrDocumentService service) {
    this.service = service;
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

  @Test
  public void testCreateDocument() throws Exception {
    registerSilverpeasNodeTypes();
    prepareUploadedFile("FrenchScrum.odp", "1210692002788.odp");
    DocumentVersion doc = new DocumentVersion();
    doc.setDocumentPK(new DocumentPK(10, instanceId));
    doc.setAuthorId(5);
    doc.setCreationDate(new Date());
    doc.setComments("commentaires");
    doc.setInstanceId(instanceId);
    doc.setLogicalName("FrenchScrum.odp");
    doc.setMajorNumber(1);
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    doc.setMinorNumber(1);
    doc.setPhysicalName("1210692002788.odp");
    assertFalse(doc.isOfficeDocument());
    assertTrue(doc.isOpenOfficeCompatibleDocument());
    doc.setMimeType(MimeTypes.EXCEL_MIME_TYPE1);
    assertTrue(doc.isOfficeDocument());
    assertTrue(doc.isOpenOfficeCompatibleDocument());
    service.createDocument(doc);
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      Node pathNode = session.getRootNode().getNode(instanceId);
      assertNotNull(pathNode);
      assertEquals(JcrConstants.NT_FOLDER, pathNode.getPrimaryNodeType()
          .getName());
      pathNode = session.getRootNode().getNode(instanceId + "/Versioning");
      assertNotNull(pathNode);
      assertEquals(JcrConstants.NT_FOLDER, pathNode.getPrimaryNodeType()
          .getName());
      pathNode = session.getRootNode().getNode(instanceId + "/Versioning/10");
      assertNotNull(pathNode);
      assertEquals(JcrConstants.NT_FOLDER, pathNode.getPrimaryNodeType()
          .getName());
      pathNode = session.getRootNode().getNode(
          instanceId + "/Versioning/10/1.1");
      assertNotNull(pathNode);
      assertEquals(JcrConstants.NT_FOLDER, pathNode.getPrimaryNodeType()
          .getName());
      Node fileNode = session.getRootNode().getNode(
          instanceId + "/Versioning/10/1.1/FrenchScrum.odp");
      assertNotNull(fileNode);
      assertEquals(JcrConstants.NT_FILE, fileNode.getPrimaryNodeType()
          .getName());
      Node content = fileNode.getNode(JcrConstants.JCR_CONTENT);
      assertEquals(JcrConstants.NT_RESOURCE, content.getPrimaryNodeType()
          .getName());
      assertNotNull(content);
      assertEquals(JcrConstants.NT_RESOURCE, content.getPrimaryNodeType()
          .getName());
      assertNotNull(content.getProperty(JcrConstants.JCR_MIMETYPE));
      assertEquals(MimeTypes.EXCEL_MIME_TYPE1, content.getProperty(
          JcrConstants.JCR_MIMETYPE).getString());
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Test
  public void testGetUpdatedDocument() throws Exception {
    createTempFile(UPLOAD_DIR + "test.txt", "Ceci est un test.");
    DocumentVersion doc = new DocumentVersion();
    doc.setDocumentPK(new DocumentPK(11, instanceId));
    doc.setAuthorId(5);
    doc.setCreationDate(new Date());
    doc.setComments("commentaires");
    doc.setInstanceId(instanceId);
    doc.setLogicalName("test_updated_doc.txt");
    doc.setMajorNumber(1);
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_PRESENTATION);
    doc.setPhysicalName("test.txt");
    service.createDocument(doc);
    Session session = null;
    try {
      createTempFile(UPLOAD_DIR + "test.txt", "Ceci est un test.");
      session = BasicDaoFactory.getSystemSession();
      // update of the content
      Node content = session.getRootNode().getNode(
          instanceId + "/Versioning/11/1.0/test_updated_doc.txt/jcr:content");
      assertNotNull(content);
      assertEquals(JcrConstants.NT_RESOURCE, content.getPrimaryNodeType()
          .getName());
      assertEquals(MimeTypes.MIME_TYPE_OO_PRESENTATION, content.getProperty(
          JcrConstants.JCR_MIMETYPE).getString());
      ByteArrayInputStream in = new ByteArrayInputStream("Ce test fonctionne."
          .getBytes());
      content.setProperty(JcrConstants.JCR_DATA, in);
      session.save();
      service.getUpdatedDocument(doc);
      String result = readFile(UPLOAD_DIR + "test.txt");
      assertEquals("Ce test fonctionne.", result);
      service.createDocument(doc);
      in = new ByteArrayInputStream(new byte[0]);
      content = session.getRootNode().getNode(
          instanceId + "/Versioning/11/1.0/test_updated_doc.txt/jcr:content");
      content.setProperty(JcrConstants.JCR_DATA, in);
      session.save();
      service.getUpdatedDocument(doc);
      result = readFile(UPLOAD_DIR + "test.txt");
      assertEquals("", result);
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

  @Test
  public void testDeleteDocument() throws Exception {
    createTempFile(UPLOAD_DIR + "test.txt", "Ceci est un test.");
    DocumentVersion doc = new DocumentVersion();
    doc.setDocumentPK(new DocumentPK(12, instanceId));
    doc.setAuthorId(5);
    doc.setCreationDate(new Date());
    doc.setComments("commentaires");
    doc.setInstanceId(instanceId);
    doc.setLogicalName("test_delete.txt");
    doc.setMajorNumber(1);
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_PRESENTATION);
    doc.setPhysicalName("test.txt");
    service.createDocument(doc);
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      // update of the content
      Node content = session.getRootNode().getNode(
          instanceId + "/Versioning/12/1.0/test_delete.txt/jcr:content");
      assertNotNull(content);
      assertEquals(JcrConstants.NT_RESOURCE, content.getPrimaryNodeType()
          .getName());
      assertEquals(MimeTypes.MIME_TYPE_OO_PRESENTATION, content.getProperty(
          JcrConstants.JCR_MIMETYPE).getString());
      assertEquals("Ceci est un test.", readFileFromNode(session.getRootNode()
          .getNode(instanceId + "/Versioning/12/1.0/test_delete.txt")));
      service.deleteDocument(doc);
      try {
        content = session.getRootNode().getNode(
            instanceId + "/Versioning/12/1.0/test_delete.txt");
        fail();
      } catch (PathNotFoundException pnfex) {
      }
    } finally {
      if (session != null) {
        session.logout();
      }
    }
  }

  @Test
  public void testUpdateDocument() throws Exception {
    createTempFile(UPLOAD_DIR + "test.txt", "Ceci est un test.");
    DocumentVersion doc = new DocumentVersion();
    doc.setDocumentPK(new DocumentPK(13, instanceId));
    doc.setAuthorId(5);
    doc.setCreationDate(new Date());
    doc.setComments("commentaires");
    doc.setInstanceId(instanceId);
    doc.setLogicalName("test_update.txt");
    doc.setMajorNumber(1);
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_PRESENTATION);
    doc.setPhysicalName("test.txt");
    service.createDocument(doc);
    Session session = null;
    try {
      session = BasicDaoFactory.getSystemSession();
      // update of the content
      Node content = session.getRootNode().getNode(
          instanceId + "/Versioning/13/1.0/test_update.txt/jcr:content");
      assertNotNull(content);
      assertEquals(JcrConstants.NT_RESOURCE, content.getPrimaryNodeType()
          .getName());
      assertEquals(MimeTypes.MIME_TYPE_OO_PRESENTATION, content.getProperty(
          JcrConstants.JCR_MIMETYPE).getString());
      assertEquals("Ceci est un test.", readFileFromNode(session.getRootNode()
          .getNode(instanceId + "/Versioning/13/1.0/test_update.txt")));
      createTempFile(UPLOAD_DIR + "test.txt", "Le test fonctionne.");
      service.updateDocument(doc);
      String result = readFile(UPLOAD_DIR + "test.txt");
      assertEquals("Le test fonctionne.", result);
      assertEquals("Le test fonctionne.", readFileFromNode(session
          .getRootNode().getNode(
              instanceId + "/Versioning/13/1.0/test_update.txt")));
    } finally {
      BasicDaoFactory.logout(session);
    }
  }

}
