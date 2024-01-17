/*
 * Copyright (C) 2000 - 2024 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.cmis.CMISEnvForTests;
import org.silverpeas.cmis.util.CmisDateConverter;
import org.silverpeas.cmis.util.CmisProperties;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.cmis.model.CmisObject;
import org.silverpeas.core.cmis.model.DocumentFile;
import org.silverpeas.core.cmis.model.TypeId;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.util.MimeTypes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.exparity.hamcrest.date.DateMatchers.within;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;

/**
 * Unit tests about the content stream of the objects in the CMIS tree.
 * @author mmoquillon
 */
@DisplayName("Test the creation of Silverpeas objects in the CMIS tree through the walkers")
class ObjectCreationInTreeWalkerTest extends CMISEnvForTests {

  @Test
  @DisplayName("Collaborative space doesn't support creation of spaces")
  void createSpaceInToSpace() {
    final String spaceId = "WA1";
    CmisProperties cmisProperties = new CmisProperties();
    cmisProperties.setObjectTypeId(TypeId.SILVERPEAS_SPACE)
        .setName("My space")
        .setDescription("A space")
        .setDefaultProperties()
        .setCreationData(User.getCurrentRequester().getDisplayedName(), new Date().getTime());
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisNotSupportedException.class,
        () -> walker.createChildData(spaceId, cmisProperties, null, "fr"));
  }

  @Test
  @DisplayName("Collaborative space doesn't support creation of applications")
  void createApplicationInToSpace() {
    final String spaceId = "WA1";
    CmisProperties cmisProperties = new CmisProperties();
    cmisProperties.setObjectTypeId(TypeId.SILVERPEAS_APPLICATION)
        .setName("My app")
        .setDescription("An application")
        .setDefaultProperties()
        .setCreationData(User.getCurrentRequester().getDisplayedName(), new Date().getTime());
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisNotSupportedException.class,
        () -> walker.createChildData(spaceId, cmisProperties, null, "fr"));
  }

  @Test
  @DisplayName("Application takes the creation of a folder as the creation of a publication")
  void createAFolderIntoApplication() {
    final String appId = "kmelia1";
    Date now = new Date();
    CmisProperties cmisProperties = new CmisProperties();
    cmisProperties.setObjectTypeId(TypeId.SILVERPEAS_FOLDER)
        .setName("My folder")
        .setDescription("A folder")
        .setDefaultProperties()
        .setCreationData(User.getCurrentRequester()
            .getDisplayedName(), now.getTime());
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    CmisObject publication = walker.createChildData(appId, cmisProperties, null, "fr");
    assertThat(publication, notNullValue());
    assertThat(publication.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(publication.getTypeId(), is(TypeId.SILVERPEAS_PUBLICATION));
    assertThat(publication.getName(), is("My folder"));
    assertThat(publication.getDescription(), is("A folder"));
    assertThat(publication.getCreator(), is(User.getCurrentRequester()
        .getDisplayedName()));

    long dateTimeInMillis = CmisDateConverter.millisToCalendar(now.getTime()).getTimeInMillis();
    assertThat(new Date(publication.getCreationDate()),
        within(1, ChronoUnit.SECONDS, new Date(dateTimeInMillis)));
  }

  @Test
  @DisplayName("Application supports the creation of publications")
  void createAPublicationIntoApplication() {
    final String appId = "kmelia1";
    Date now = new Date();
    CmisProperties cmisProperties = new CmisProperties();
    cmisProperties.setObjectTypeId(TypeId.SILVERPEAS_PUBLICATION)
        .setName("My publication")
        .setDescription("A publication")
        .setDefaultProperties()
        .setCreationData(User.getCurrentRequester().getDisplayedName(), now.getTime());
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    CmisObject publication = walker.createChildData(appId, cmisProperties, null, "fr");
    assertThat(publication, notNullValue());
    assertThat(publication.getBaseTypeId(), is(BaseTypeId.CMIS_FOLDER));
    assertThat(publication.getTypeId(), is(TypeId.SILVERPEAS_PUBLICATION));
    assertThat(publication.getName(), is("My publication"));
    assertThat(publication.getDescription(), is("A publication"));
    assertThat(publication.getCreator(), is(User.getCurrentRequester()
        .getDisplayedName()));

    long dateTimeInMillis = CmisDateConverter.millisToCalendar(now.getTime())
        .getTimeInMillis();
    assertThat(new Date(publication.getCreationDate()),
        within(1, ChronoUnit.SECONDS, new Date(dateTimeInMillis)));
  }

  @Test
  @DisplayName("Publication supports the creation of documents")
  void createADocumentIntoPublication() {
    CmisObject object = createDocument();
    assertThat(object, notNullValue());
    assertThat(object instanceof DocumentFile, is(true));

    verify(attachmentService).createAttachment(any(SimpleDocument.class), any(File.class),
        anyBoolean(), anyBoolean());

    DocumentFile document = (DocumentFile) object;
    assertThat(document.getBaseTypeId(), is(BaseTypeId.CMIS_DOCUMENT));
    assertThat(document.getTypeId(), is(TypeId.SILVERPEAS_DOCUMENT));
    assertThat(document.getTitle(), is("History Of Smalltalk"));
    assertThat(document.getName(), is("HistoryOfSmalltalk.pdf"));
    assertThat(document.getDescription(), is("How smalltalk has been created"));
    assertThat(document.getCreator(), is(User.getCurrentRequester()
        .getDisplayedName()));
    assertThat(document.getSize(), is(889449L));
  }

  @Test
  @DisplayName("Document's content can be updated")
  void updateDocumentContent() {
    CmisObject document = createDocument();
    long previousSize = ((DocumentFile) document).getSize();

    InputStream content = getClass().getResourceAsStream("/Seaside.pdf");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.PDF_MIME_TYPE);
    contentStream.setFileName("HistoryOfSmalltalk.pdf");
    contentStream.setLength(BigInteger.valueOf(1390098L));

    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    CmisObject object =
        walker.updateObjectData(document.getId(), new CmisProperties(), contentStream, "en");

    assertThat(object, notNullValue());
    assertThat(object instanceof DocumentFile, is(true));

    verify(attachmentService).updateAttachment(any(SimpleDocument.class), any(File.class),
        anyBoolean(), anyBoolean());

    DocumentFile updatedDocument = (DocumentFile) object;
    assertThat(updatedDocument.getBaseTypeId(), is(BaseTypeId.CMIS_DOCUMENT));
    assertThat(updatedDocument.getTypeId(), is(TypeId.SILVERPEAS_DOCUMENT));
    assertThat(updatedDocument.getTitle(), is("History Of Smalltalk"));
    assertThat(updatedDocument.getName(), is("HistoryOfSmalltalk.pdf"));
    assertThat(updatedDocument.getDescription(), is("How smalltalk has been created"));
    assertThat(updatedDocument.getLastModifier(), is(User.getCurrentRequester()
        .getDisplayedName()));
    assertThat(updatedDocument.getSize(), not(is(previousSize)));
    assertThat(updatedDocument.getSize(), is(795247L));
  }

  @Test
  @DisplayName("Document's content cannot be updated from a content of another MIME type")
  void updateDocumentWithInvalidMIMETypeContent() {
    CmisObject document = createDocument();

    InputStream content =
        getClass().getResourceAsStream("/computer_programming_using_gnu_smalltalk.odt");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.MIME_TYPE_OO_FORMATTED_TEXT);
    contentStream.setFileName("computer_programming_using_gnu_smalltalk.odt");
    contentStream.setLength(BigInteger.valueOf(795247L));

    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();

    String cmisDocId = document.getId();
    CmisProperties properties = new CmisProperties();
    assertThrows(CmisStreamNotSupportedException.class,
        () -> walker.updateObjectData(cmisDocId, properties, contentStream, "en"));

  }

  @Test
  @DisplayName("Currently edited document's content cannot be updated")
  void updateEditedDocumentContent() {
    CmisObject document = createDocument();

    ContributionIdentifier docId = ContributionIdentifier.decode(document.getId());
    SimpleDocument translation = attachmentService.searchDocumentById(
        new SimpleDocumentPK(docId.getLocalId(), docId.getComponentInstanceId()),
        document.getLanguage());
    translation.edit("0");

    InputStream content = getClass().getResourceAsStream("/Seaside.pdf");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.PDF_MIME_TYPE);
    contentStream.setFileName("HistoryOfSmalltalk.pdf");
    contentStream.setLength(BigInteger.valueOf(1390098L));

    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();

    CmisProperties properties = new CmisProperties();
    String cmisDocId = document.getId();
    assertThrows(CmisPermissionDeniedException.class,
        () -> walker.updateObjectData(cmisDocId, properties, contentStream, "en"));
  }

  @Test
  @DisplayName("Non edited versioned document's content cannot be updated")
  void updateNonEditedVersionedDocumentContent() {
    CmisObject document = createDocument();

    ContributionIdentifier docId = ContributionIdentifier.decode(document.getId());
    SimpleDocument translation = attachmentService.searchDocumentById(
        new SimpleDocumentPK(docId.getLocalId(), docId.getComponentInstanceId()),
        document.getLanguage());
    translation.edit("0");

    InputStream content = getClass().getResourceAsStream("/Seaside.pdf");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.PDF_MIME_TYPE);
    contentStream.setFileName("HistoryOfSmalltalk.pdf");
    contentStream.setLength(BigInteger.valueOf(1390098L));

    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();

    CmisProperties properties = new CmisProperties();
    String cmisDocId = document.getId();
    assertThrows(CmisPermissionDeniedException.class,
        () -> walker.updateObjectData(cmisDocId, properties, contentStream, "en"));
  }

  @Test
  @DisplayName("Document that is currently being edited by the current user can be updated in the" +
      " webDAV storage")
  void updateDocumentContentBeingEditedByCurrentUser() throws IOException {
    CmisObject document = createDocument();
    long previousSize = ((DocumentFile) document).getSize();

    ContributionIdentifier docId = ContributionIdentifier.decode(document.getId());
    SimpleDocument translation = attachmentService.searchDocumentById(
        new SimpleDocumentPK(docId.getLocalId(), docId.getComponentInstanceId()),
        document.getLanguage());
    translation.edit(User.getCurrentRequester()
        .getId());

    InputStream content = getClass().getResourceAsStream("/Seaside.pdf");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.PDF_MIME_TYPE);
    contentStream.setFileName("HistoryOfSmalltalk.pdf");
    contentStream.setLength(BigInteger.valueOf(795247L));

    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    CmisObject object =
        walker.updateObjectData(document.getId(), new CmisProperties(), contentStream, "en");

    assertThat(object, notNullValue());
    assertThat(object instanceof DocumentFile, is(true));

    verify(webdavService).updateContentFrom(any(SimpleDocument.class), any(InputStream.class));

    DocumentFile updatedDocument = (DocumentFile) object;
    assertThat(updatedDocument.getBaseTypeId(), is(BaseTypeId.CMIS_DOCUMENT));
    assertThat(updatedDocument.getTypeId(), is(TypeId.SILVERPEAS_DOCUMENT));
    assertThat(updatedDocument.getTitle(), is("History Of Smalltalk"));
    assertThat(updatedDocument.getName(), is("HistoryOfSmalltalk.pdf"));
    assertThat(updatedDocument.getDescription(), is("How smalltalk has been created"));
    assertThat(updatedDocument.getLastModifier(), is(User.getCurrentRequester()
        .getDisplayedName()));
    assertThat(updatedDocument.getSize(), not(is(previousSize)));
    assertThat(updatedDocument.getSize(), is(795247L));
  }

  @Test
  @DisplayName("Versioned document can be updated only if it is edited by the current user")
  void updateVersionedDocumentContent() throws IOException {
    CmisObject document = createDocument();
    long previousSize = ((DocumentFile) document).getSize();

    ContributionIdentifier docId = ContributionIdentifier.decode(document.getId());
    SimpleDocument translation = attachmentService.searchDocumentById(
        new SimpleDocumentPK(docId.getLocalId(), docId.getComponentInstanceId()),
        document.getLanguage());
    translation.setMajorVersion(1);
    translation.setMinorVersion(0);
    translation.edit(User.getCurrentRequester()
        .getId());

    InputStream content = getClass().getResourceAsStream("/Seaside.pdf");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.PDF_MIME_TYPE);
    contentStream.setFileName("HistoryOfSmalltalk.pdf");
    contentStream.setLength(BigInteger.valueOf(1390098L));

    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    CmisObject object =
        walker.updateObjectData(document.getId(), new CmisProperties(), contentStream, "en");

    assertThat(object, notNullValue());
    assertThat(object instanceof DocumentFile, is(true));

    verify(webdavService).updateContentFrom(any(SimpleDocument.class), any(InputStream.class));

    DocumentFile updatedDocument = (DocumentFile) object;
    assertThat(updatedDocument.getBaseTypeId(), is(BaseTypeId.CMIS_DOCUMENT));
    assertThat(updatedDocument.getTypeId(), is(TypeId.SILVERPEAS_DOCUMENT));
    assertThat(updatedDocument.getTitle(), is("History Of Smalltalk"));
    assertThat(updatedDocument.getName(), is("HistoryOfSmalltalk.pdf"));
    assertThat(updatedDocument.getDescription(), is("How smalltalk has been created"));
    assertThat(updatedDocument.getLastModifier(), is(User.getCurrentRequester()
        .getDisplayedName()));
    assertThat(updatedDocument.getSize(), not(is(previousSize)));
    assertThat(updatedDocument.getSize(), is(795247L));
  }

  private CmisObject createDocument() {
    final ContributionIdentifier pubId =
        ContributionIdentifier.from("kmelia1", "1", PublicationDetail.TYPE);

    InputStream content = getClass().getResourceAsStream("/HistoryOfSmalltalk.pdf");
    ContentStreamImpl contentStream = new ContentStreamImpl();
    contentStream.setStream(content);
    contentStream.setMimeType(MimeTypes.PDF_MIME_TYPE);
    contentStream.setFileName("HistoryOfSmalltalk.pdf");
    contentStream.setLength(BigInteger.valueOf(1629872L));

    CmisProperties cmisProperties = new CmisProperties();
    cmisProperties.setObjectTypeId(TypeId.SILVERPEAS_DOCUMENT)
        .setName("History Of Smalltalk")
        .setDescription("How smalltalk has been created")
        .setDefaultProperties()
        .setContent(null, MimeTypes.PDF_MIME_TYPE, 1629872L, "HistoryOfSmalltalk.pdf");
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    return walker.createChildData(pubId.asString(), cmisProperties, contentStream, "en");
  }
}
