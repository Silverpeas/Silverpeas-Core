/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.attachment.repository;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationIT;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.jcr.JCRSession;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;
import java.util.Calendar;
import java.util.Date;

import static javax.jcr.nodetype.NodeType.NT_FOLDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.silverpeas.core.jcr.util.SilverpeasProperty.*;

@RunWith(Arquillian.class)
public class DocumentConverterIT extends JcrIntegrationIT {

  private static final String instanceId = "kmelia74";
  private static final DocumentConverter instance = new DocumentConverter();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DocumentConverterIT.class)
        .addJcrFeatures()
        .build();
  }

  @Before
  public void loadJcr() throws Exception {
    try (JCRSession session = JCRSession.openSystemSession()) {
      if (!session.getRootNode().hasNode(instanceId)) {
        session.getRootNode().addNode(instanceId, NT_FOLDER);
      }
      session.save();
    }
  }

  @Test
  public void convertNode() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    SimpleDocument expectedResult =
        new SimpleDocument(new SimpleDocumentPK("-1", instanceId), foreignId, order, versioned,
            owner, attachment);
    expectedResult.setReservation(reservation.getTime());
    expectedResult.setAlert(alert.getTime());
    expectedResult.setExpiry(expiry.getTime());
    expectedResult.setComment(comment);
    expectedResult.setOldSilverpeasId(oldSilverpeasId);
    expectedResult.getAttachment()
        .setLastUpdateDate(updateDate);
    expectedResult.getAttachment()
        .setUpdatedBy(updatedBy);
    expectedResult.setMajorVersion(1);
    expectedResult.setMinorVersion(2);
    expectedResult.setNodeName("attach_" + oldSilverpeasId);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode()
          .getNode(instanceId)
          .addNode(SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.setProperty(SLV_PROPERTY_FOREIGN_KEY, foreignId);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, versioned);
      documentNode.setProperty(SLV_PROPERTY_ORDER, order);
      documentNode.setProperty(SLV_PROPERTY_OLD_ID, oldSilverpeasId);
      documentNode.setProperty(SLV_PROPERTY_INSTANCEID, instanceId);
      documentNode.setProperty(SLV_PROPERTY_OWNER, owner);
      documentNode.setProperty(SLV_PROPERTY_COMMENT, comment);
      documentNode.setProperty(SLV_PROPERTY_ALERT_DATE, alert);
      documentNode.setProperty(SLV_PROPERTY_EXPIRY_DATE, expiry);
      documentNode.setProperty(SLV_PROPERTY_RESERVATION_DATE, reservation);
      documentNode.setProperty(SLV_PROPERTY_MAJOR, 1);
      documentNode.setProperty(SLV_PROPERTY_MINOR, 2);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.addNode(attachmentNodeName, SLV_SIMPLE_ATTACHMENT);
      attachNode.setProperty(SLV_PROPERTY_NAME, fileName);
      attachNode.setProperty(SLV_PROPERTY_CREATOR, creatorId);
      attachNode.setProperty(JCR_LANGUAGE, language);
      attachNode.setProperty(JCR_TITLE, title);
      attachNode.setProperty(JCR_DESCRIPTION, description);
      Calendar calend = Calendar.getInstance();
      calend.setTime(creationDate);
      attachNode.setProperty(SLV_PROPERTY_CREATION_DATE, calend);
      attachNode.setProperty(SLV_PROPERTY_XMLFORM_ID, formId);
      attachNode.setProperty(JCR_LAST_MODIFIED_BY, updatedBy);
      calend.setTime(updateDate);
      attachNode.setProperty(JCR_LAST_MODIFIED, calend);
      attachNode.setProperty(JCR_MIMETYPE, MimeTypes.PDF_MIME_TYPE);
      attachNode.setProperty(SLV_PROPERTY_SIZE, "my test content".getBytes(Charsets.UTF_8).length);
      SimpleDocument result = instance.convertNode(documentNode, language);
      expectedResult.setId(result.getId());
      assertThat(result, SimpleDocumentMatcher.matches(expectedResult));

      // Adding forbidden download for some roles
      expectedResult.addRolesForWhichDownloadIsForbidden(SilverpeasRole.WRITER,
          SilverpeasRole.READER);
      documentNode.addMixin(SLV_DOWNLOADABLE_MIXIN);
      documentNode.setProperty(SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES, "writer,reader");
      result = instance.convertNode(documentNode, language);
      expectedResult.setId(result.getId());
      assertThat(result, SimpleDocumentMatcher.matches(expectedResult));
    }
  }

  @Test
  public void getAttachment() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment expectedResult = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    expectedResult.setLastUpdateDate(updateDate);
    expectedResult.setUpdatedBy(updatedBy);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode()
          .getNode(instanceId)
          .addNode(SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.setProperty(SLV_PROPERTY_FOREIGN_KEY, foreignId);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, versioned);
      documentNode.setProperty(SLV_PROPERTY_ORDER, order);
      documentNode.setProperty(SLV_PROPERTY_OLD_ID, oldSilverpeasId);
      documentNode.setProperty(SLV_PROPERTY_INSTANCEID, instanceId);
      documentNode.setProperty(SLV_PROPERTY_OWNER, owner);
      documentNode.setProperty(SLV_PROPERTY_COMMENT, comment);
      documentNode.setProperty(SLV_PROPERTY_ALERT_DATE, alert);
      documentNode.setProperty(SLV_PROPERTY_EXPIRY_DATE, expiry);
      documentNode.setProperty(SLV_PROPERTY_RESERVATION_DATE, reservation);
      documentNode.setProperty(SLV_PROPERTY_MAJOR, 1);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.addNode(attachmentNodeName, SLV_SIMPLE_ATTACHMENT);
      attachNode.setProperty(SLV_PROPERTY_NAME, fileName);
      attachNode.setProperty(SLV_PROPERTY_CREATOR, creatorId);
      attachNode.setProperty(JCR_LANGUAGE, language);
      attachNode.setProperty(JCR_TITLE, title);
      attachNode.setProperty(JCR_DESCRIPTION, description);
      Calendar calend = Calendar.getInstance();
      calend.setTime(creationDate);
      attachNode.setProperty(SLV_PROPERTY_CREATION_DATE, calend);
      attachNode.setProperty(SLV_PROPERTY_XMLFORM_ID, formId);
      attachNode.setProperty(JCR_LAST_MODIFIED_BY, updatedBy);
      calend.setTime(updateDate);
      attachNode.setProperty(JCR_LAST_MODIFIED, calend);
      attachNode.setProperty(JCR_MIMETYPE, MimeTypes.PDF_MIME_TYPE);
      attachNode.setProperty(SLV_PROPERTY_SIZE, "my test content".getBytes(Charsets.UTF_8).length);
      SimpleAttachment result = instance.getAttachment(documentNode, language);
      assertThat(result, SimpleAttachmentMatcher.matches(expectedResult));
    }
  }

  @Test
  public void getNoAttachment() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String comment = "My Status";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;


    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.setProperty(SLV_PROPERTY_FOREIGN_KEY, foreignId);
      documentNode.setProperty(SLV_PROPERTY_VERSIONED, versioned);
      documentNode.setProperty(SLV_PROPERTY_ORDER, order);
      documentNode.setProperty(SLV_PROPERTY_OLD_ID, oldSilverpeasId);
      documentNode.setProperty(SLV_PROPERTY_INSTANCEID, instanceId);
      documentNode.setProperty(SLV_PROPERTY_OWNER, owner);
      documentNode.setProperty(SLV_PROPERTY_COMMENT, comment);
      documentNode.setProperty(SLV_PROPERTY_ALERT_DATE, alert);
      documentNode.setProperty(SLV_PROPERTY_EXPIRY_DATE, expiry);
      documentNode.setProperty(SLV_PROPERTY_RESERVATION_DATE, reservation);
      SimpleAttachment result = instance.getAttachment(documentNode, language);
      assertThat(result, is(nullValue()));
    }
  }

  @Test
  public void fillNodeFromSimpleDocument() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    SimpleDocument document =
        new SimpleDocument(new SimpleDocumentPK("-1", instanceId), foreignId, order, versioned,
            owner, attachment);
    document.setReservation(reservation.getTime());
    document.setAlert(alert.getTime());
    document.setExpiry(expiry.getTime());
    document.setComment(comment);
    document.setMajorVersion(1);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getAttachment().setLastUpdateDate(updateDate);
    document.getAttachment().setUpdatedBy(updatedBy);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, documentNode);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FOREIGN_KEY).getString(), is(foreignId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_VERSIONED).getBoolean(), is(versioned));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ORDER).getLong(), is((long) order));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong(), is(oldSilverpeasId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_INSTANCEID).getString(), is(instanceId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OWNER).getString(), is(owner));
      assertThat(documentNode.getProperty(SLV_PROPERTY_COMMENT).getString(), is(comment));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ALERT_DATE).getDate().getTimeInMillis(),
          is(alert.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_EXPIRY_DATE).getDate().getTimeInMillis(),
          is(expiry.getTimeInMillis()));
      assertThat(
          documentNode.getProperty(SLV_PROPERTY_RESERVATION_DATE).getDate().getTimeInMillis(),
          is(reservation.getTimeInMillis()));
      assertThat(documentNode.hasProperty(SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES), is(false));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED_BY).getString(), is(updatedBy));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          is(updateDate.getTime()));
      assertThat(attachNode.hasNode(JCR_CONTENT), is(false));

      // Adding forbidden download for some roles
      document.addRolesForWhichDownloadIsForbidden(SilverpeasRole.PRIVILEGED_USER,
          SilverpeasRole.PRIVILEGED_USER, SilverpeasRole.PUBLISHER);
      instance.fillNode(document, documentNode);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FORBIDDEN_DOWNLOAD_FOR_ROLES).getString(),
          is("publisher,privilegedUser"));
    }
  }

  @Test
  public void fillNodeFromSimpleDocumentAndContent() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    SimpleDocument document =
        new SimpleDocument(new SimpleDocumentPK("-1", instanceId), foreignId, order, versioned,
            owner, attachment);
    document.setReservation(reservation.getTime());
    document.setAlert(alert.getTime());
    document.setExpiry(expiry.getTime());
    document.setComment(comment);
    document.setMajorVersion(1);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getAttachment().setLastUpdateDate(updateDate);
    document.getAttachment().setUpdatedBy(updatedBy);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, documentNode);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FOREIGN_KEY).getString(), is(foreignId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_VERSIONED).getBoolean(), is(versioned));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ORDER).getLong(), is((long) order));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong(), is(oldSilverpeasId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_INSTANCEID).getString(), is(instanceId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OWNER).getString(), is(owner));
      assertThat(documentNode.getProperty(SLV_PROPERTY_COMMENT).getString(), is(comment));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ALERT_DATE).getDate().getTimeInMillis(),
          is(alert.getTimeInMillis()));
      assertThat(documentNode.getProperty(SLV_PROPERTY_EXPIRY_DATE).getDate().getTimeInMillis(),
          is(expiry.getTimeInMillis()));
      assertThat(
          documentNode.getProperty(SLV_PROPERTY_RESERVATION_DATE).getDate().getTimeInMillis(),
          is(reservation.getTimeInMillis()));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED_BY).getString(), is(updatedBy));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          is(updateDate.getTime()));
      assertThat(attachNode.getProperty(JCR_MIMETYPE).getString(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(attachNode.getProperty(SLV_PROPERTY_SIZE).getLong(), is(15L));
    }
  }

  @Test
  public void addAttachment() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My comment";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    SimpleDocument document =
        new SimpleDocument(new SimpleDocumentPK("-1", instanceId), foreignId, order, versioned,
            owner, attachment);
    document.setReservation(reservation.getTime());
    document.setAlert(alert.getTime());
    document.setExpiry(expiry.getTime());
    document.setComment(comment);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setStatus(status);
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getAttachment().setLastUpdateDate(updateDate);
    document.getAttachment().setUpdatedBy(updatedBy);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode()
          .getNode(instanceId)
          .addNode(SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, documentNode);
      fileName = "essai.odp";
      title = "Mon titre";
      description = "Ceci est un document de test";
      creatorId = "73";
      formId = "38";
      creationDate = new Date();
      attachment = SimpleAttachment.builder(language)
          .setFilename(fileName)
          .setTitle(title)
          .setDescription(description)
          .setSize(18)
          .setContentType(MimeTypes.MIME_TYPE_OO_PRESENTATION)
          .setCreationData(creatorId, creationDate)
          .setFormId(formId)
          .build();
      instance.addAttachment(documentNode, attachment);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME)
          .getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR)
          .getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE)
          .getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE)
          .getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION)
          .getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE)
          .getDate()
          .getTimeInMillis(), is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.hasProperty(JCR_LAST_MODIFIED_BY), is(false));
      assertThat(attachNode.hasProperty(JCR_LAST_MODIFIED), is(false));
      assertThat(attachNode.getProperty(SLV_PROPERTY_SIZE).getLong(), is(18L));
      assertThat(attachNode.getProperty(JCR_MIMETYPE).getString(), is(
          MimeTypes.MIME_TYPE_OO_PRESENTATION));
    }
  }

  @Test
  public void removeAttachment() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My comment";
    String status = "My Status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = false;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    SimpleDocument document =
        new SimpleDocument(new SimpleDocumentPK("-1", instanceId), foreignId, order, versioned,
            owner, attachment);
    document.setReservation(reservation.getTime());
    document.setAlert(alert.getTime());
    document.setExpiry(expiry.getTime());
    document.setComment(comment);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setStatus(status);
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getAttachment().setLastUpdateDate(updateDate);
    document.getAttachment().setUpdatedBy(updatedBy);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      instance.fillNode(document, documentNode);
      instance.removeAttachment(documentNode, language);
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      assertThat(documentNode.hasNode(attachmentNodeName), is(false));
    }
  }

  @Test
  public void updateDocumentVersion() throws Exception {
    long oldSilverpeasId = 100L;
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String comment = "My comment";
    String status = "My status";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";
    String foreignId = "node36";
    boolean versioned = true;
    String owner = "25";
    int order = 10;
    Date creationDate = RandomGenerator.getRandomCalendar()
        .getTime();
    Date updateDate = RandomGenerator.getRandomCalendar()
        .getTime();

    Calendar alert = RandomGenerator.getRandomCalendar();
    Calendar expiry = RandomGenerator.getRandomCalendar();
    Calendar reservation = RandomGenerator.getRandomCalendar();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize("my test content".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData(creatorId, creationDate)
        .setFormId(formId)
        .build();
    SimpleDocument document =
        new SimpleDocument(new SimpleDocumentPK("-1", instanceId), foreignId, order, versioned,
            owner, attachment);
    document.setReservation(reservation.getTime());
    document.setAlert(alert.getTime());
    document.setExpiry(expiry.getTime());
    document.setComment(comment);
    document.setStatus(status);
    document.setMajorVersion(1);
    alert.setTime(document.getAlert());
    expiry.setTime(document.getExpiry());
    reservation.setTime(document.getReservation());
    document.setOldSilverpeasId(oldSilverpeasId);
    document.getAttachment().setLastUpdateDate(updateDate);
    document.getAttachment().setUpdatedBy(updatedBy);
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getRootNode().getNode(instanceId).addNode(
          SimpleDocument.ATTACHMENT_PREFIX + oldSilverpeasId, SLV_SIMPLE_DOCUMENT);
      documentNode.addMixin(NodeType.MIX_VERSIONABLE);
      instance.fillNode(document, documentNode);
      instance.updateVersion(documentNode, document.getLanguage(), true);
      assertThat(documentNode.getProperty(SLV_PROPERTY_FOREIGN_KEY).getString(), is(foreignId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_VERSIONED).getBoolean(), is(versioned));
      assertThat(documentNode.getProperty(SLV_PROPERTY_ORDER).getLong(), is((long) order));
      assertThat(documentNode.getProperty(SLV_PROPERTY_OLD_ID).getLong(), is(oldSilverpeasId));
      assertThat(documentNode.getProperty(SLV_PROPERTY_INSTANCEID).getString(), is(instanceId));
      assertThat(documentNode.hasProperty(SLV_PROPERTY_OWNER), is(false));
      assertThat(documentNode.getProperty(SLV_PROPERTY_COMMENT).getString(), is(comment));
      assertThat(documentNode.getProperty(SLV_PROPERTY_STATUS).getString(), is(status));
      assertThat(documentNode.hasProperty(SLV_PROPERTY_ALERT_DATE), is(false));
      assertThat(documentNode.hasProperty(SLV_PROPERTY_EXPIRY_DATE), is(false));
      assertThat(documentNode.hasProperty(SLV_PROPERTY_RESERVATION_DATE), is(false));
      assertThat(documentNode.getProperty(SLV_PROPERTY_MAJOR).getLong(), is(1L));
      assertThat(documentNode.getProperty(SLV_PROPERTY_MINOR).getLong(), is(0L));
      String attachmentNodeName = SimpleDocument.FILE_PREFIX + language;
      Node attachNode = documentNode.getNode(attachmentNodeName);
      assertThat(attachNode.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATOR).getString(), is(creatorId));
      assertThat(attachNode.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(attachNode.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(attachNode.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(attachNode.getProperty(SLV_PROPERTY_CREATION_DATE).getDate().getTimeInMillis(),
          is(creationDate.getTime()));
      assertThat(attachNode.getProperty(SLV_PROPERTY_XMLFORM_ID).getString(), is(formId));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED_BY).getString(), is(owner));
      assertThat(attachNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          is(updateDate.getTime()));
      assertThat(attachNode.hasNode(JCR_CONTENT), is(false));
    }
  }
}
