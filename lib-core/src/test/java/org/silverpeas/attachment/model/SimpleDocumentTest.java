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
package org.silverpeas.attachment.model;

import java.io.File;
import java.util.UUID;

import org.junit.Test;

import com.silverpeas.jcrutil.RandomGenerator;
import com.silverpeas.util.PathTestUtil;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentTest {

  private static final String instanceId = "kmelia36";

  public SimpleDocumentTest() {
  }

  /**
   * Test of computeNodeName method, of class SimpleDocument.
   */
  @Test
  public void testComputeNodeName() {
    SimpleDocument instance = new SimpleDocument();
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    long oldSilverpeasId = RandomGenerator.getRandomInt(500);
    pk.setOldSilverpeasId(oldSilverpeasId);
    instance.setPK(pk);
    String nodeName = instance.computeNodeName();
    assertThat(nodeName, is("simpledoc_" + oldSilverpeasId));
  }

  /**
   * Test of getFullJcrContentPath method, of class SimpleDocument.
   */
  @Test
  public void testGetFullJcrContentPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    long oldSilverpeasId = RandomGenerator.getRandomInt(500);
    pk.setOldSilverpeasId(oldSilverpeasId);
    instance.setPK(pk);
    String nodeName = instance.computeNodeName();
    assertThat(nodeName, is("simpledoc_" + oldSilverpeasId));
    String expResult = "/kmelia36/attachments/" + nodeName + "/file_fr";
    String result = instance.getFullJcrContentPath();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/kmelia36/attachments/" + nodeName + "/file_en";
    result = instance.getFullJcrContentPath();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getFullJcrPath method, of class SimpleDocument.
   */
  @Test
  public void testGetFullJcrPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    long oldSilverpeasId = RandomGenerator.getRandomInt(500);
    pk.setOldSilverpeasId(oldSilverpeasId);
    instance.setPK(pk);
    String nodeName = instance.computeNodeName();
    assertThat(nodeName, is("simpledoc_" + oldSilverpeasId));
    String expResult = "/kmelia36/attachments/" + nodeName;
    String result = instance.getFullJcrPath();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    result = instance.getFullJcrPath();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getDisplayIcon method, of class SimpleDocument.
   */
  @Test
  public void testGetDisplayIcon() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("toto.docx");
    String result = instance.getDisplayIcon();
    assertThat(result, is("/silverpeas//util/icons/fileType/word2007.gif"));
    instance.setFilename("toto.odp");
    result = instance.getDisplayIcon();
    assertThat(result, is("/silverpeas//util/icons/fileType/oo_presentation.gif"));
    instance.setFilename("toto.tar.bz2");
    result = instance.getDisplayIcon();
    assertThat(result, is("/silverpeas//util/icons/fileType/unknown.gif"));
  }

  /**
   * Test of isOpenOfficeCompatible method, of class SimpleDocument.
   */
  @Test
  public void testIsOpenOfficeCompatible() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("toto.docx");
    boolean result = instance.isOpenOfficeCompatible();
    assertThat(result, is(true));
    instance.setFilename("toto.odp");
    result = instance.isOpenOfficeCompatible();
    assertThat(result, is(true));
    instance.setFilename("toto.tar.bz2");
    result = instance.isOpenOfficeCompatible();
    assertThat(result, is(false));
  }

  /**
   * Test of getAttachmentPath method, of class SimpleDocument.
   */
  @Test
  public void testGetAttachmentPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    long oldSilverpeasId = RandomGenerator.getRandomInt(500);
    pk.setOldSilverpeasId(oldSilverpeasId);
    instance.setPK(pk);
    String nodeName = instance.computeNodeName();
    assertThat(nodeName, is("simpledoc_" + oldSilverpeasId));
    String expResult = PathTestUtil.TARGET_DIR + "temp/uploads/kmelia36/" + nodeName
        + "/0_0/fr/myFile.odt".replace('/', File.separatorChar);
    String result = instance.getAttachmentPath().replace('/', File.separatorChar);
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = PathTestUtil.TARGET_DIR + "temp/uploads/kmelia36/" + nodeName
        + "/0_0/en/myFile.odt".replace('/', File.separatorChar);
    result = instance.getAttachmentPath().replace('/', File.separatorChar);
    assertThat(result, is(expResult));
  }

  /**
   * Test of getDirectoryPath method, of class SimpleDocument.
   */
  @Test
  public void testGetDirectoryPath() {
    SimpleDocument instance = new SimpleDocument();
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    long oldSilverpeasId = RandomGenerator.getRandomInt(500);
    pk.setOldSilverpeasId(oldSilverpeasId);
    instance.setPK(pk);
    String nodeName = instance.computeNodeName();
    assertThat(nodeName, is("simpledoc_" + oldSilverpeasId));
    String expResult = PathTestUtil.TARGET_DIR + "temp/uploads/kmelia36/" + nodeName + "/0_0/fr/".
        replace('/', File.separatorChar);
    String result = instance.getDirectoryPath(null).replace('/', File.separatorChar);
    assertThat(result, is(expResult));
    expResult = PathTestUtil.TARGET_DIR + "temp/uploads/kmelia36/" + nodeName + "/0_0/en/".replace(
        '/', File.separatorChar);
    result = instance.getDirectoryPath("en").replace('/', File.separatorChar);
    assertThat(result, is(expResult));
  }

  /**
   * Test of getAttachmentURL method, of class SimpleDocument.
   */
  @Test
  public void testGetAttachmentURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "/attached_file/componentId/kmelia36/attachmentId/" + id
        + "/lang/fr/name/myFile.odt";
    String result = instance.getAttachmentURL();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/attached_file/componentId/kmelia36/attachmentId/" + id
        + "/lang/en/name/myFile.odt";
    result = instance.getAttachmentURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getUniversalURL method, of class SimpleDocument.
   */
  @Test
  public void testGetUniversalURL() {
    SimpleDocument instance = new SimpleDocument();
    String id = UUID.randomUUID().toString();
    instance.setPK(new SimpleDocumentPK(id, instanceId));
    String expResult = "/silverpeas/File/" + id;
    String result = instance.getUniversalURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getOnlineURL method, of class SimpleDocument.
   */
  @Test
  public void testGetOnlineURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult =
        "/silverpeas/OnlineFileServer/myFile.odt?ComponentId=kmelia36&SourceFile=&MimeType=null&Directory=";
    String result = instance.getOnlineURL();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult =
        "/silverpeas/OnlineFileServer/myFile.odt?ComponentId=kmelia36&SourceFile=&MimeType=null&Directory=";
    result = instance.getOnlineURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getAliasURL method, of class SimpleDocument.
   */
  @Test
  public void testGetAliasURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "/silverpeas/AliasFileServer/myFile.odt?ComponentId=kmelia36&AttachmentId="
        + id;
    String result = instance.getAliasURL();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/silverpeas/AliasFileServer/myFile.odt?ComponentId=kmelia36&AttachmentId="
        + id + "&lang=en";
    result = instance.getAliasURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getWebdavUrl method, of class SimpleDocument.
   */
  @Test
  public void testGetWebdavUrl() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("Mon fichier élève 2012 - fait à 80%.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "/silverpeas/repository/jackrabbit/webdav/attachments/kmelia36/" + id
        + "/fr/Mon%20fichier%20%C3%A9l%C3%A8ve%202012%20-%20fait%20%C3%A0%2080%25.odt";
    String result = instance.getWebdavUrl();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/silverpeas/repository/jackrabbit/webdav/attachments/kmelia36/" + id
        + "/en/Mon%20fichier%20%C3%A9l%C3%A8ve%202012%20-%20fait%20%C3%A0%2080%25.odt";
    result = instance.getWebdavUrl();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getWebdavJcrPath method, of class SimpleDocument.
   */
  @Test
  public void testGetWebdavJcrPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setFile(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "webdav/attachments/kmelia36/" + id + "/fr/myFile.odt";
    String result = instance.getWebdavJcrPath();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "webdav/attachments/kmelia36/" + id + "/en/myFile.odt";
    result = instance.getWebdavJcrPath();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getFolder method, of class SimpleDocument.
   */
  @Test
  public void testGetFolder() {
    SimpleDocument instance = new SimpleDocument();
    String result = instance.getFolder();
    assertThat(result, is(DocumentType.attachment.getFolderName()));
    instance.setDocumentType(DocumentType.video);
    result = instance.getFolder();
    assertThat(result, is(DocumentType.video.getFolderName()));
  }
}