/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.util.attachment.model;

import java.util.Calendar;

import junit.framework.TestCase;
import static com.silverpeas.util.PathTestUtil.*;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import java.io.File;

public class TestAttachmentDetail extends TestCase {

  private static final String instanceId = "kmelia57";
  private static final String UPLOAD_DIR = BUILD_PATH + SEPARATOR + "uploads" +
      SEPARATOR + instanceId + SEPARATOR + "Attachment" + SEPARATOR + "tests" +
      SEPARATOR + "simpson" + SEPARATOR + "bart" + SEPARATOR;
  private Calendar calend;

  @Override
  protected void setUp() throws Exception {
    calend = Calendar.getInstance();
    calend.set(Calendar.MILLISECOND, 0);
    calend.set(Calendar.SECOND, 0);
    calend.set(Calendar.MINUTE, 15);
    calend.set(Calendar.HOUR, 9);
    calend.set(Calendar.DAY_OF_MONTH, 12);
    calend.set(Calendar.MONTH, Calendar.MARCH);
    calend.set(Calendar.YEAR, 2008);
  }

  public void testOpenOffice() {
    AttachmentPK pk = new AttachmentPK("100", "kmelia57");
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId("kmelia57");
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
    assertTrue(attachment.isOfficeDocument());
    assertTrue(attachment.isOfficeDocument("fr"));
    assertTrue(attachment.isOpenOfficeCompatible(null));
    assertTrue(attachment.isOpenOfficeCompatible("fr"));
    assertEquals("kmelia57", attachment.getInstanceId());
    assertEquals(
        "/silverpeas/OnlineFileServer/frenchScrum.odp?ComponentId=kmelia57&Sour" +
        "ceFile=abf562dee7d07e1b5af50a2d1b3d724ef5a88869&MimeType=application/vnd" +
        ".oasis.opendocument.presentation&Directory=Attachment" + File.separatorChar +"tests"
        + File.separatorChar + "simpson" + File.separatorChar + "bart" + File.separatorChar,
        attachment.getOnlineURL());
    assertEquals(
        "/attached_file/componentId/kmelia57/attachmentId/100/lang/fr" +
        "/name/frenchScrum.odp",
        attachment.getWebURL());
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/frenchScrum.odp",
        attachment.getJcrPath(null));
    assertEquals(
        "/silverpeas/repository/jackrabbit/attachments/kmelia57/Attachment/tests" +
        "/simpson/bart/100/frenchScrum.odp",
        attachment.getWebdavUrl(null));
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/frenchScrum.odp",
        attachment.getJcrPath("fr"));
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/en/frenchScrum.odp",
        attachment.getJcrPath("en"));
    assertEquals(
        "/silverpeas/repository/jackrabbit/attachments/kmelia57/Attachment/tests" +
        "/simpson/bart/100/en/frenchScrum.odp",
        attachment.getWebdavUrl("en"));
    assertEquals(
        UPLOAD_DIR + "abf562dee7d07e1b5af50a2d1b3d724ef5a88869",
        attachment.getAttachmentPath(null));
    assertEquals(
        UPLOAD_DIR + "abf562dee7d07e1b5af50a2d1b3d724ef5a88869",
        attachment.getAttachmentPath("de"));
  }

  public void testJcrPath() {
    AttachmentPK pk = new AttachmentPK("100", "kmelia57");
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId("kmelia57");
    attachment.setPK(pk);
    attachment.setContext("tests,simpson,bart");
    attachment.setCreationDate(calend.getTime());
    attachment.setDescription("Attachment for tests");
    attachment.setLanguage("fr");
    attachment.setLogicalName("frenchScrum[1].odp");
    attachment.setPhysicalName("abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/vnd.oasis.opendocument.presentation");
    attachment.setTitle("Test OpenOffice");
    assertTrue(attachment.isOfficeDocument());
    assertTrue(attachment.isOfficeDocument("fr"));
    assertTrue(attachment.isOpenOfficeCompatible(null));
    assertTrue(attachment.isOpenOfficeCompatible("fr"));
    assertEquals("kmelia57", attachment.getInstanceId());
    assertEquals(
        "/silverpeas/OnlineFileServer/frenchScrum[1].odp?ComponentId=kmelia57&Sour" +
        "ceFile=abf562dee7d07e1b5af50a2d1b3d724ef5a88869&MimeType=application/vnd" +
        ".oasis.opendocument.presentation&Directory=Attachment"+ File.separatorChar + "tests"
        + File.separatorChar + "simpson" + File.separatorChar + "bart" + File.separatorChar,
        attachment.getOnlineURL());
    assertEquals(
        "/attached_file/componentId/kmelia57/attachmentId/100/lang/fr" +
        "/name/frenchScrum[1].odp",
        attachment.getWebURL());
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/frenchScrum 1 .odp",
        attachment.getJcrPath(null));
    assertEquals(
        "/silverpeas/repository/jackrabbit/attachments/kmelia57/Attachment/tests" +
        "/simpson/bart/100/frenchScrum 1 .odp",
        attachment.getWebdavUrl(null));
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/frenchScrum 1 .odp",
        attachment.getJcrPath("fr"));
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/frenchScrum 1 .odp",
        attachment.getJcrPath("  "));
    assertEquals(
        "/silverpeas/repository/jackrabbit/attachments/kmelia57/Attachment/tests" +
        "/simpson/bart/100/frenchScrum 1 .odp",
        attachment.getWebdavUrl("  "));
    assertEquals(
        UPLOAD_DIR + "abf562dee7d07e1b5af50a2d1b3d724ef5a88869",
        attachment.getAttachmentPath(null));
    assertEquals(
        UPLOAD_DIR + "abf562dee7d07e1b5af50a2d1b3d724ef5a88869",
        attachment.getAttachmentPath("de"));
  }

  public void testMsOffice() {
    AttachmentPK pk = new AttachmentPK("100", "kmelia57");
    AttachmentDetail attachment = new AttachmentDetail();
    attachment.setAuthor("1");
    attachment.setInstanceId("kmelia57");
    attachment.setPK(pk);
    attachment.setContext("tests,simpson,bart");
    attachment.setCreationDate(calend.getTime());
    attachment.setDescription("Attachment for tests");
    attachment.setLanguage("fr");
    attachment.setLogicalName("Spec-Silverpeas-p3-1_2.doc");
    attachment.setPhysicalName("abf562dee7d07e1b5af50a2d1b3d724ef5a88378");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/msword");
    attachment.setTitle("Test M$ Office");
    assertTrue(attachment.isOfficeDocument());
    assertTrue(attachment.isOfficeDocument("fr"));
    assertTrue(attachment.isOpenOfficeCompatible(null));
    assertTrue(attachment.isOpenOfficeCompatible("fr"));
    assertEquals("kmelia57", attachment.getInstanceId());
    assertEquals(
        "/silverpeas/OnlineFileServer/Spec-Silverpeas-p3-1_2.doc?ComponentId=kmelia57&Sour" +
        "ceFile=abf562dee7d07e1b5af50a2d1b3d724ef5a88378&MimeType=application/msword&Directory=Attachment" 
        + File.separatorChar + "tests" + File.separatorChar + "simpson"+ File.separatorChar + "bart"
        + File.separatorChar, attachment.getOnlineURL());
    assertEquals(
        "/attached_file/componentId/kmelia57/attachmentId/100/lang/fr" +
        "/name/Spec-Silverpeas-p3-1_2.doc",
        attachment.getWebURL());
    assertEquals(
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/Spec-Silverpeas-p3-1_2.doc",
        attachment.getJcrPath(null));
    assertEquals(
        "/silverpeas/repository/jackrabbit/attachments/kmelia57/Attachment/tests" +
        "/simpson/bart/100/Spec-Silverpeas-p3-1_2.doc",
        attachment.getWebdavUrl(null));
  }
}
