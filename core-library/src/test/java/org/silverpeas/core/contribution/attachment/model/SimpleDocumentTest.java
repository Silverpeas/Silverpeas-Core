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
package org.silverpeas.core.contribution.attachment.model;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.webdav.WebdavService;
import org.silverpeas.core.i18n.I18n;
import org.silverpeas.core.test.unit.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.unit.extention.TestManagedMock;
import org.silverpeas.core.test.util.RandomGenerator;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;
import static org.silverpeas.core.util.file.FileRepositoryManager.getUploadPath;

/**
 * @author ehugonnet
 */
@EnableSilverTestEnv
class SimpleDocumentTest {
  
  private static final String instanceId = "kmelia36";
  
  @TestManagedMock
  I18n i18n;
  
  @BeforeEach
  void setDefaultI18nLanguage() {
    when(i18n.getDefaultLanguage()).thenReturn("fr");
  }

  /**
   * Test of computeNodeName method, of class SimpleDocument.
   */
  @Test
  void testComputeNodeName() {
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
  void testGetFullJcrContentPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
  void testGetFullJcrPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
  void testGetDisplayIcon() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
  void testIsOpenOfficeCompatible() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
  void testGetWebdavContentEditionLanguage(@TestManagedMock WebdavService webDavService)
      throws Exception {
    SimpleDocument document = new SimpleDocument();
    SimpleAttachment attachment = SimpleAttachment.builder().build();
    document.setAttachment(attachment);
    attachment.setFilename("file.mov");

      /*
      Current file is not an open office compatible one
       */

    reset(webDavService);
    document.release();
    when(webDavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

    // Trying a test case that must never happen but that shows that the open office compatible
    // condition is respected.
    reset(webDavService);
    document.release();
    when(webDavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

      /*
      Current file is now an open office compatible one
       */
    attachment.setFilename("file.odp");

    reset(webDavService);
    document.release();
    when(webDavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

    reset(webDavService);
    document.release();
    when(webDavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

    reset(webDavService);
    document.release();
    FieldUtils.writeField(document, "editedBy", "26", true);
    when(webDavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), is("fr"));
  }

  @Test
  void testGetWebdavContentEditionLanguageWithoutRelease(
      @TestManagedMock WebdavService webdavService) {
    SimpleDocument document = new SimpleDocument();
    SimpleAttachment attachment = SimpleAttachment.builder().build();
    document.setAttachment(attachment);
    attachment.setFilename("file.mov");

      /*
      Current file is not an open office compatible one
       */

    reset(webdavService);
    when(webdavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

    // Trying a test case that must never happen but that shows that the open office compatible
    // condition is respected.
    reset(webdavService);
    when(webdavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

      /*
      Current file is now an open office compatible one
       */
    attachment.setFilename("file.odp");

    reset(webdavService);
    when(webdavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn(null);

    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));

    reset(webdavService);
    when(webdavService.getContentEditionLanguage(any(SimpleDocument.class))).thenReturn("fr");

    // The result should be "fr", but as the getWebdavContentEditionLanguage method has a lazy
    // behavior, the previous tests has already made the information loaded.
    assertThat(document.getWebdavContentEditionLanguage(), is(emptyString()));
  }

  /**
   * Test of getAttachmentPath method, of class SimpleDocument.
   */
  @Test
  void testGetAttachmentPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
  void testGetDirectoryPath() {
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
  void testGetAttachmentURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
    instance.setFilename("myFile.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult =
        "/attached_file/componentId/kmelia36/attachmentId/" + id + "/lang/fr/name/myFile.odt?t_=";
    String result = instance.getAttachmentURL();
    assertThat(result, Matchers.startsWith(expResult));
    instance.setLanguage("en");
    expResult =
        "/attached_file/componentId/kmelia36/attachmentId/" + id + "/lang/en/name/myFile.odt?t_=";
    result = instance.getAttachmentURL();
    assertThat(result, Matchers.startsWith(expResult));
  }

  /**
   * Test of getUniversalURL method, of class SimpleDocument.
   */
  @Test
  void testGetUniversalURL() {
    SimpleDocument instance = new SimpleDocument();
    String id = UUID.randomUUID().toString();
    instance.setPK(new SimpleDocumentPK(id, instanceId));
    instance.setAttachment(SimpleAttachment.builder().build());
    instance.getAttachment().setLanguage("en");
    String expResult = "/silverpeas/File/" + id + "?ContentLanguage=en";
    String result = instance.getUniversalURL();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getOnlineURL method, of class SimpleDocument.
   */
  @Test
  void testGetOnlineURL() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
   * Test of getWebdavUrl method, of class SimpleDocument.
   */
  @Test
  void testGetWebdavUrl() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
    instance.setFilename("Mon fichier élève 2012 - fait à 80%.odt");
    String id = UUID.randomUUID().toString();
    SimpleDocumentPK pk = new SimpleDocumentPK(id, instanceId);
    instance.setPK(pk);
    String expResult = "/silverpeas/repository/jackrabbit/webdav/attachments/kmelia36/" + id
        + "/fr/Mon fichier élève 2012 - fait à 80 .odt";
    String result = instance.getWebdavUrl();
    assertThat(result, is(expResult));
    instance.setLanguage("en");
    expResult = "/silverpeas/repository/jackrabbit/webdav/attachments/kmelia36/" + id
        + "/en/Mon fichier élève 2012 - fait à 80 .odt";
    result = instance.getWebdavUrl();
    assertThat(result, is(expResult));
  }

  /**
   * Test of getWebdavJcrPath method, of class SimpleDocument.
   */
  @Test
  void testGetWebdavJcrPath() {
    SimpleDocument instance = new SimpleDocument();
    instance.setAttachment(SimpleAttachment.builder().build());
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
  void testGetFolder() {
    SimpleDocument instance = new SimpleDocument();
    String result = instance.getFolder();
    assertThat(result, is(DocumentType.attachment.getFolderName()));
    instance.setDocumentType(DocumentType.video);
    result = instance.getFolder();
    assertThat(result, is(DocumentType.video.getFolderName()));
  }

  @Test
  void testForbiddenDownload() {
    UserDetailWithRoles adminUser = new UserDetailWithRoles("admin_user", SilverpeasRole.ADMIN);
    UserDetailWithRoles adminReaderUser =
        new UserDetailWithRoles("admin_reader_user", SilverpeasRole.READER, SilverpeasRole.ADMIN);
    UserDetailWithRoles readerUser = new UserDetailWithRoles("reader_user", SilverpeasRole.READER);
    UserDetailWithRoles readerAndPrivilegedUser =
        new UserDetailWithRoles("reader", SilverpeasRole.READER);

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
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.WRITER);
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.WRITER));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));

    // Forbid empty
    instance.addRolesForWhichDownloadIsForbidden();
    instance.addRolesForWhichDownloadIsAllowed();
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.WRITER));

    // Allow writers
    instance.addRolesForWhichDownloadIsAllowed(SilverpeasRole.WRITER);
    assertThat(instance.getForbiddenDownloadForRoles(), empty());

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(true));

    // Forbid readers
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.READER);
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.READER));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));

    // Forbid writers
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.WRITER);
    assertThat(instance.getForbiddenDownloadForRoles(),
        contains(SilverpeasRole.WRITER, SilverpeasRole.READER));

    assertThat(isDownloadAllowedForRolesFrom(instance, null), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, new UserDetailWithRoles()), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, adminReaderUser), is(true));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerUser), is(false));
    assertThat(isDownloadAllowedForRolesFrom(instance, readerAndPrivilegedUser), is(false));

    // Forbid admin & Allow readers and writers
    instance.addRolesForWhichDownloadIsAllowed(SilverpeasRole.READER, SilverpeasRole.WRITER);
    instance.addRolesForWhichDownloadIsForbidden(SilverpeasRole.ADMIN);
    assertThat(instance.getForbiddenDownloadForRoles(), contains(SilverpeasRole.ADMIN));

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
  private static class UserDetailWithRoles extends UserDetail {
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
          this.roles[i++] = role.getName();
        }
      }
    }

    public Set<SilverpeasRole> getRoles() {
      return SilverpeasRole.fromStrings(roles);
    }
  }
}
