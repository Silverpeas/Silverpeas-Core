package com.stratelia.silverpeas.versioning.model;

import java.util.Date;

import com.silverpeas.util.MimeTypes;

import junit.framework.TestCase;

public class TestDocumentVersion extends TestCase {

  private static final String instanceId = "kmelia60";

  private static final String UPLOAD_DIR = "c:\\tmp\\uploads\\" + instanceId
      + "\\Versioning\\";

  public void testIsOfficeDocument() {
    DocumentVersion doc = new DocumentVersion();
    doc.setAuthorId(5);
    doc.setDocumentPK(new DocumentPK(10, instanceId));
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
  }

  public void testGetJcrPath() {
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
    assertEquals(instanceId + "/Versioning/10/1.1/FrenchScrum.odp", doc
        .getJcrPath());
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertEquals(instanceId + "/Versioning/10/1.1/FrenchScrum.odp", doc
        .getJcrPath());
    doc.setMinorNumber(2);
    assertEquals(instanceId + "/Versioning/10/1.2/FrenchScrum.odp", doc
        .getJcrPath());
  }

  public void testGetWebdavUrl() {
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
    assertEquals("/silverpeas/repository/jackrabbit/" + instanceId
        + "/Versioning/10/1.1/FrenchScrum.odp", doc.getWebdavUrl());
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertEquals("/silverpeas/repository/jackrabbit/" + instanceId
        + "/Versioning/10/1.1/FrenchScrum.odp", doc.getWebdavUrl());
    doc.setMinorNumber(2);
    assertEquals("/silverpeas/repository/jackrabbit/" + instanceId
        + "/Versioning/10/1.2/FrenchScrum.odp", doc.getWebdavUrl());
  }

  public void testGetDocumentPath() {
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
    assertEquals(UPLOAD_DIR + "1210692002788.odp", doc.getDocumentPath());
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertEquals(UPLOAD_DIR + "1210692002788.odp", doc.getDocumentPath());
    doc.setMinorNumber(2);
    assertEquals(UPLOAD_DIR + "1210692002788.odp", doc.getDocumentPath());
  }

}
