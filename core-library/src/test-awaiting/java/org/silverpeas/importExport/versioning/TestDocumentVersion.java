/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.importExport.versioning;

import org.silverpeas.util.MimeTypes;
import org.junit.Test;

import java.util.Date;

import static java.io.File.separatorChar;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TestDocumentVersion {

  private static final String instanceId = "kmelia60";
  private static final String UPLOAD_DIR = System.getProperty("basedir") + separatorChar + "target"
      + separatorChar + "temp" + separatorChar + "uploads" + separatorChar + instanceId
      + separatorChar
      + "Versioning" + separatorChar;

  @Test
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
    assertThat(doc.isOfficeDocument(), is(true));
    assertThat(doc.isOpenOfficeCompatibleDocument(), is(true));
    doc.setMimeType(MimeTypes.EXCEL_MIME_TYPE1);
    assertThat(doc.isOfficeDocument(), is(true));
    assertThat(doc.isOpenOfficeCompatibleDocument(), is(true));
  }

  @Test
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
    assertThat(doc.getJcrPath(), is(instanceId + "/Versioning/10/1.1/FrenchScrum.odp"));
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertThat(doc.getJcrPath(), is(instanceId + "/Versioning/10/1.1/FrenchScrum.odp"));
    doc.setMinorNumber(2);
    assertThat(doc.getJcrPath(), is(instanceId + "/Versioning/10/1.2/FrenchScrum.odp"));
  }

  @Test
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
    assertThat(doc.getWebdavUrl(), is("/silverpeas/repository/jackrabbit/" + instanceId
        + "/Versioning/10/1.1/FrenchScrum.odp"));
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertThat(doc.getWebdavUrl(), is("/silverpeas/repository/jackrabbit/" + instanceId
        + "/Versioning/10/1.1/FrenchScrum.odp"));
    doc.setMinorNumber(2);
    assertThat(doc.getWebdavUrl(), is("/silverpeas/repository/jackrabbit/" + instanceId
        + "/Versioning/10/1.2/FrenchScrum.odp"));
  }

  @Test
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
    String documentPath = doc.getDocumentPath().replace('\\', separatorChar);
    documentPath = documentPath.replace('/', separatorChar);
    assertThat(documentPath, is(UPLOAD_DIR + "1210692002788.odp"));
    doc.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    assertThat(documentPath, is(UPLOAD_DIR + "1210692002788.odp"));
    doc.setMinorNumber(2);
    assertThat(documentPath, is(UPLOAD_DIR + "1210692002788.odp"));
  }
}
