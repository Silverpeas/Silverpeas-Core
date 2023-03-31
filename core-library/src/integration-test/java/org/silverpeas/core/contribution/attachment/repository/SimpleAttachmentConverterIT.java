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
package org.silverpeas.core.contribution.attachment.repository;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationIT;
import org.silverpeas.core.test.util.RandomGenerator;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.jcr.JCRSession;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.Date;

import static javax.jcr.nodetype.NodeType.NT_FOLDER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.silverpeas.core.jcr.util.SilverpeasProperty.*;

@RunWith(Arquillian.class)
public class SimpleAttachmentConverterIT extends JcrIntegrationIT {

  private static final String instanceId = "kmelia73";
  private static final SimpleAttachmentConverter instance = new SimpleAttachmentConverter();

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(SimpleAttachmentConverterIT.class)
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
    String language = "en";
    String fileName = "test.pdf";
    String title = "My test document";
    String description = "This is a test document";
    String formId = "18";
    String updatedBy = "5";
    String creatorId = "0";

    String nodeName = SimpleDocument.FILE_PREFIX + language;
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    Date updateDate = RandomGenerator.getRandomCalendar().getTime();
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node node = session.getRootNode().getNode(instanceId).addNode(nodeName, SLV_SIMPLE_ATTACHMENT);
      SimpleAttachment expResult = SimpleAttachment.builder(language)
          .setFilename(fileName)
          .setTitle(title)
          .setDescription(description)
          .setSize(15L)
          .setContentType(MimeTypes.PDF_MIME_TYPE)
          .setCreationData(creatorId, creationDate)
          .setFormId(formId)
          .build();
      expResult.setLastUpdateDate(updateDate);
      expResult.setUpdatedBy(updatedBy);
      node.setProperty(SLV_PROPERTY_NAME, fileName);
      node.setProperty(SLV_PROPERTY_CREATOR, creatorId);
      node.setProperty(JCR_LANGUAGE, language);
      node.setProperty(JCR_TITLE, title);
      node.setProperty(JCR_DESCRIPTION, description);
      Calendar calend = Calendar.getInstance();
      calend.setTime(creationDate);
      node.setProperty(SLV_PROPERTY_CREATION_DATE, calend);
      node.setProperty(SLV_PROPERTY_XMLFORM_ID, formId);
      node.setProperty(JCR_LAST_MODIFIED_BY, updatedBy);
      calend.setTime(updateDate);
      node.setProperty(JCR_LAST_MODIFIED, calend);
      node.setProperty(JCR_MIMETYPE, MimeTypes.PDF_MIME_TYPE);
      node.setProperty(SLV_PROPERTY_SIZE, 15L);
      session.save();
      SimpleAttachment result = instance.convertNode(node);
      assertThat(result, is(notNullValue()));
      assertThat(result, is(SimpleAttachmentMatcher.matches(expResult)));
    }
  }

  @Test
  public void fillNode() throws Exception {
    String language = "fr";
    String fileName = "test.pdf";
    String title = "Mon document de test";
    String description = "Ceci est un document de test";
    Date creationDate = RandomGenerator.getRandomCalendar().getTime();
    SimpleAttachment attachment = SimpleAttachment.builder(language)
        .setFilename(fileName)
        .setTitle(title)
        .setDescription(description)
        .setSize(12L)
        .setContentType(MimeTypes.PDF_MIME_TYPE)
        .setCreationData("0", creationDate)
        .build();
    String nodeName = attachment.getNodeName();
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node node = session.getRootNode().getNode(instanceId).addNode(nodeName, SLV_SIMPLE_ATTACHMENT);
      instance.fillNode(attachment, node);
      session.save();
      assertThat(node.getName(), is(nodeName));
      assertThat(node.getPath(), is("/" + instanceId + "/" + nodeName));
      assertThat(node.hasProperty(SLV_PROPERTY_ALERT_DATE), is(false));
      assertThat(node.hasProperty(JCR_LANGUAGE), is(true));
      assertThat(node.getProperty(JCR_LANGUAGE).getString(), is(language));
      assertThat(node.hasProperty(JCR_CREATED), is(true));
      assertThat(DateUtil.date2SQLDate(node.getProperty(JCR_CREATED).getDate().getTime()),
          is(DateUtil.date2SQLDate(new Date())));
      assertThat(node.hasProperty(JCR_CREATED_BY), is(true));
      assertThat(node.hasProperty(SLV_PROPERTY_CLONE), is(false));
      assertThat(node.hasProperty(JCR_DESCRIPTION), is(true));
      assertThat(node.getProperty(JCR_DESCRIPTION).getString(), is(description));
      assertThat(node.hasProperty(SLV_PROPERTY_NAME), is(true));
      assertThat(node.getProperty(SLV_PROPERTY_NAME).getString(), is(fileName));
      assertThat(node.hasProperty(JCR_TITLE), is(true));
      assertThat(node.getProperty(JCR_TITLE).getString(), is(title));
      assertThat(node.hasProperty(JCR_LAST_MODIFIED), is(false));
      assertThat(node.hasProperty(JCR_LAST_MODIFIED_BY), is(false));
      assertThat(node.hasProperty(SLV_PROPERTY_XMLFORM_ID), is(false));

      assertThat(node.hasProperty(JCR_MIMETYPE), is(true));
      assertThat(node.getProperty(JCR_MIMETYPE).getString(), is(MimeTypes.PDF_MIME_TYPE));
      assertThat(node.hasProperty(SLV_PROPERTY_SIZE), is(true));
      assertThat(node.getProperty(SLV_PROPERTY_SIZE).getLong(), is(12L));
    }
  }
}
