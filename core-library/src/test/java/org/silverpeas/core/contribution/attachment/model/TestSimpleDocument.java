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
package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Rule;
import org.junit.Test;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.contribution.attachment.webdav.WebdavService;
import org.silverpeas.core.test.rule.CommonAPI4Test;
import org.silverpeas.core.test.util.RandomGenerator;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.util.file.FileRepositoryManager.getUploadPath;

/**
 * @author ehugonnet
 */
public class TestSimpleDocument {

  @Rule
  public CommonAPI4Test commonAPI4Test = new CommonAPI4Test();

  private static final String instanceId = "kmelia36";

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
    instance.setAttachment(new SimpleAttachment());
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
    instance.setAttachment(new SimpleAttachment());
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
    instance.setAttachment(new SimpleAttachment());
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
    instance.setAttachment(new SimpleAttachment());
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

  @Test
  public void testGetWebdavContentEditionLanguage() throws Exception {
    WebdavService mock = commonAPI4Test.injectIntoMockedBeanContainer(mock(WebdavService.class));

    SimpleDocument document = new SimpleDocument();
    SimpleAttachment attachment = new SimpleAttachment();
    document.setAttachment(attachment);
    attachment.setFilename("file.mov");

      /*
      Current file is not an open office compatible one
       */

    reset(mock);
    document.release();
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

    // Trying a test case that must never happen but that shows that the open office compatible
    // condition is respected.
    reset(mock);
    document.release();
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

      /*
      Current file is now an open office compatible one
       */
    attachment.setFilename("file.odp");

    reset(mock);
    document.release();
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

    reset(mock);
    document.release();
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

    reset(mock);
    document.release();
    FieldUtils.writeField(document, "editedBy", "26", true);
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), is("fr"));
  }

  @Test
  public void testGetWebdavContentEditionLanguageWithoutRelease() {
    WebdavService mock = commonAPI4Test.injectIntoMockedBeanContainer(mock(WebdavService.class));

    SimpleDocument document = new SimpleDocument();
    SimpleAttachment attachment = new SimpleAttachment();
    document.setAttachment(attachment);
    attachment.setFilename("file.mov");

      /*
      Current file is not an open office compatible one
       */

    reset(mock);
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

    // Trying a test case that must never happen but that shows that the open office compatible
    // condition is respected.
    reset(mock);
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

      /*
      Current file is now an open office compatible one
       */
    attachment.setFilename("file.odp");

    reset(mock);
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());

    reset(mock);
    when(mock.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    // The result should be "fr", but as the getWebdavContentEditionLanguage method has a lazy
    // behavior, the previous tests has already made the information loaded.
    assertThat(document.getWebdavContentEditionLanguage(), isEmptyString());
  }

  /**
   * Test of getAttachmentPath method, of class SimpleDocument.
   */
  @Test
  public void testGetAttachmentPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    long oldSilverpeasId = RandomGenerator.getRandomInt(500);
    pk.setOldSilverpeasId(oldSilverpeasId);
    instance.setPK(pk);
    String nodeName = instance.computeNodeName();
    assertThat(nodeName, is("simpledoc_" + oldSilverpeasId));
    String expResult = (getUploadPath() + "kmelia36/" + nodeName +
        "/0_0/fr/myFile.odt").replace('/', File.separatorChar);
    String result = instance.getAttachmentPath().replace('/', File.separatorChar);
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = (getUploadPath() + "kmelia36/" + nodeName +
        "/0_0/en/myFile.odt").replace('/', File.separatorChar);
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
    String expResult = (getUploadPath() + "kmelia36/" + nodeName +
        "/0_0/fr/").replace('/', File.separatorChar);
    String result = instance.getDirectoryPath(null).replace('/', File.separatorChar);
    assertThat(result, is(expResult));
    expResult = (getUploadPath() + "kmelia36/" + nodeName +
        "/0_0/en/").replace('/', File.separatorChar);
    result = instance.getDirectoryPath("en").replace('/', File.separatorChar);
    assertThat(result, is(expResult));
  }

  /**
   * Test of getAttachmentURL method, of class SimpleDocument.
   */
  @Test
  public void testGetAttachmentURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult =
        "/attached_file/componentId/kmelia36/attachmentId/" + id + "/lang/fr/name/myFile.odt";
    String result = instance.getAttachmentURL();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult =
        "/attached_file/componentId/kmelia36/attachmentId/" + id + "/lang/en/name/myFile.odt";
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
    instance.setAttachment(new SimpleAttachment());
    instance.getAttachment().setLanguage("en");
    String expResult = "/silverpeas/File/" + id + "?ContentLanguage=en";
    String result = instance.getUniversalURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getOnlineURL method, of class SimpleDocument.
   */
  @Test
  public void testGetOnlineURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "/silverpeas/OnlineFileServer/myFile" +
        ".odt?ComponentId=kmelia36&SourceFile=&MimeType=null&Directory=";
    String result = instance.getOnlineURL();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/silverpeas/OnlineFileServer/myFile" +
        ".odt?ComponentId=kmelia36&SourceFile=&MimeType=null&Directory=";
    result = instance.getOnlineURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getAliasURL method, of class SimpleDocument.
   */
  @Test
  public void testGetAliasURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(new SimpleAttachment());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult =
        "/silverpeas/AliasFileServer/myFile.odt?ComponentId=kmelia36&AttachmentId=" + id;
    String result = instance.getAliasURL();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/silverpeas/AliasFileServer/myFile.odt?ComponentId=kmelia36&AttachmentId=" + id +
        "&lang=en";
    result = instance.getAliasURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getWebdavUrl method, of class SimpleDocument.
   */
  @Test
  public void testGetWebdavUrl() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(new SimpleAttachment());
    instance.setFilename("Mon fichier élève 2012 - fait à 80%.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "/silverpeas/repository/jackrabbit/webdav/attachments/kmelia36/" + id
        + "/fr/Mon fichier élève 2012 - fait à 80%.odt";
    String result = instance.getWebdavUrl();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/silverpeas/repository/jackrabbit/webdav/attachments/kmelia36/" + id
        + "/en/Mon fichier élève 2012 - fait à 80%.odt";
    result = instance.getWebdavUrl();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getWebdavJcrPath method, of class SimpleDocument.
   */
  @Test
  public void testGetWebdavJcrPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(new SimpleAttachment());
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

  @Test
  public void testForbiddenDownload() {
    UserDetailWithRoles adminUser = new UserDetailWithRoles("admin_user", SilverpeasRole.admin);
    UserDetailWithRoles adminReaderUser =
        new UserDetailWithRoles("admin_reader_user", SilverpeasRole.reader, SilverpeasRole.admin);
    UserDetailWithRoles readerUser = new UserDetailWithRoles("reader_user", SilverpeasRole.reader);
    UserDetailWithRoles readerAndPrivilegedUser =
        new UserDetailWithRoles("reader", SilverpeasRole.reader);

    SimpleDocument instance = new SimpleDocument();
    SimpleDocumentPK pk = new SimpleDocumentPK("dummyId", instanceId);
    instance.setPK(pk);
    assertThat(instance.getForbiddenDownloadForRoles(), nullValue());

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(true));

    // Forbid empty
    instance.addRolesForWhichDownloadIsForbidden();
    assertThat(instance.getForbiddenDownloadForRoles(), nullValue());

    // Forbid writers
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.writer);
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.writer));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));

    // Forbid empty
    instance.addRolesForWhichDownloadIsForbidden();
    instance.addRolesForWhichDownloadIsAllowed();
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.writer));

    // Allow writers
    instance.addRolesForWhichDownloadIsAllowed(SilverpeasRole.writer);
    assertThat(instance.getForbiddenDownloadForRoles(), empty());

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(true));

    // Forbid readers
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.reader);
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.reader));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));

    // Forbid writers
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.writer);
    assertThat(instance.getForbiddenDownloadForRoles(),
        contains(SilverpeasRole.writer, SilverpeasRole.reader));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));

    // Forbid admin & Allow readers and writers
    instance.addRolesForWhichDownloadIsAllowed(SilverpeasRole.reader, SilverpeasRole.writer);
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.admin);
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.admin));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));
  }

  private boolean isDownloadAllowedForRolesFrom(SimpleDocument instance, UserDetailWithRoles user) {
    return user != null && instance.isDownloadAllowedForRoles(user.getRoles());
  }

  /**
   * Class to define a User with some roles in the context of unit tests.
   */
  private class UserDetailWithRoles extends UserDetail {
    private static final long serialVersionUID = -1992488455131397859L;
    protected String[] roles = null;

    public UserDetailWithRoles() {
      super();
    }

    public UserDetailWithRoles(String userId, SilverpeasRole... roles) {
      setId(userId);
      setState(UserState.VALID);
      if (roles != null) {
        this.roles = new String[roles.length];
        int i = 0;
        for (SilverpeasRole role : roles) {
          this.roles[i++] = role.name();
        }
      }
    }

    public Set<SilverpeasRole> getRoles() {
      return SilverpeasRole.from(roles);
    }
  }
}