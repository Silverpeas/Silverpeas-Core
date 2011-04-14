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

package com.stratelia.silverpeas.versioning.model;

import java.util.Date;

import com.silverpeas.util.MimeTypes;
import java.io.File;

import junit.framework.TestCase;

public class TestDocumentVersion extends TestCase {

  private static final String instanceId = "kmelia60";

  private static final String UPLOAD_DIR = System.getProperty("basedir") + File.separatorChar + "target"
          + File.separatorChar + "uploads" + File.separatorChar + instanceId + File.separatorChar
          + "Versioning"  + File.separatorChar;

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
    String documentPath = doc.getDocumentPath().replace('\\',  File.separatorChar);
    documentPath = documentPath.replace('/',  File.separatorChar);
    assertEquals(UPLOAD_DIR + "1210692002788.odp", documentPath);
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertEquals(UPLOAD_DIR + "1210692002788.odp", documentPath);
    doc.setMinorNumber(2);
    assertEquals(UPLOAD_DIR + "1210692002788.odp", documentPath);
  }

}
