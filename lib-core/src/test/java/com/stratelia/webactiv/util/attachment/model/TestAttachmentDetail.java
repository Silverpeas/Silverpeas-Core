package com.stratelia.webactiv.util.attachment.model;

import java.util.Calendar;

import junit.framework.TestCase;
import static com.silverpeas.util.PathTestUtil.*;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;

public class TestAttachmentDetail extends TestCase {

  private static final String instanceId = "kmelia57";
  private static final String UPLOAD_DIR = BUILD_PATH + SEPARATOR + "uploads" +
      SEPARATOR + instanceId + SEPARATOR + "Attachment" + SEPARATOR + "tests" +
      SEPARATOR + "simpson" + SEPARATOR + "bart" + SEPARATOR;
  private Calendar calend;

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
    assertFalse(attachment.isOfficeDocument());
    assertFalse(attachment.isOfficeDocument("fr"));
    assertTrue(attachment.isOpenOfficeCompatible(null));
    assertTrue(attachment.isOpenOfficeCompatible("fr"));
    assertEquals("kmelia57", attachment.getInstanceId());
    assertEquals(
        "/silverpeas//OnlineFileServer/frenchScrum.odp?ComponentId=kmelia57&Sour" +
        "ceFile=abf562dee7d07e1b5af50a2d1b3d724ef5a88869&MimeType=application/vnd" +
        ".oasis.opendocument.presentation&Directory=Attachment\\tests\\simpson\\" +
        "bart\\", attachment.getOnlineURL());
    assertEquals(
        "/silverpeas/attached/file/componentId/kmelia57/attachmentId/100/lang/fr" +
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
    attachment.setLogicalName("frenchScrum.odp");
    attachment.setPhysicalName("abf562dee7d07e1b5af50a2d1b3d724ef5a88869");
    attachment.setOrderNum(2);
    attachment.setSize(975048);
    attachment.setType("application/vnd.oasis.opendocument.presentation");
    attachment.setTitle("Test OpenOffice");
    assertFalse(attachment.isOfficeDocument());
    assertFalse(attachment.isOfficeDocument("fr"));
    assertTrue(attachment.isOpenOfficeCompatible(null));
    assertTrue(attachment.isOpenOfficeCompatible("fr"));
    assertEquals("kmelia57", attachment.getInstanceId());
    assertEquals(
        "/silverpeas//OnlineFileServer/frenchScrum.odp?ComponentId=kmelia57&Sour" +
        "ceFile=abf562dee7d07e1b5af50a2d1b3d724ef5a88869&MimeType=application/vnd" +
        ".oasis.opendocument.presentation&Directory=Attachment\\tests\\simpson\\" +
        "bart\\", attachment.getOnlineURL());
    assertEquals(
        "/silverpeas/attached/file/componentId/kmelia57/attachmentId/100/lang/fr" +
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
        "attachments/kmelia57/Attachment/tests/simpson/bart/100/frenchScrum.odp",
        attachment.getJcrPath("  "));
    assertEquals(
        "/silverpeas/repository/jackrabbit/attachments/kmelia57/Attachment/tests" +
        "/simpson/bart/100/frenchScrum.odp",
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
        "/silverpeas//OnlineFileServer/Spec-Silverpeas-p3-1_2.doc?ComponentId=kmelia57&Sour" +
        "ceFile=abf562dee7d07e1b5af50a2d1b3d724ef5a88378&MimeType=" +
        "application/msword&Directory=Attachment\\tests\\simpson\\" + "bart\\",
        attachment.getOnlineURL());
    assertEquals(
        "/silverpeas/attached/file/componentId/kmelia57/attachmentId/100/lang/fr" +
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
