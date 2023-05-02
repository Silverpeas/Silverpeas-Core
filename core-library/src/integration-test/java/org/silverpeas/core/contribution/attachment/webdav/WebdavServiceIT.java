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
package org.silverpeas.core.contribution.attachment.webdav;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.AttachmentException;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.webdav.impl.WebdavDocumentRepository;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationIT;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.jcr.JCRSession;

import javax.inject.Inject;
import javax.jcr.Node;

import static javax.jcr.Property.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.jcr.util.SilverpeasProperty.SLV_OWNABLE_MIXIN;
import static org.silverpeas.jcr.util.SilverpeasProperty.SLV_PROPERTY_OWNER;

@RunWith(Arquillian.class)
public class WebdavServiceIT extends JcrIntegrationIT {

  @Inject
  private WebdavService webdavService;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(WebdavServiceIT.class)
        .addJcrFeatures()
        .build();
  }

  @Test(expected = AttachmentException.class)
  public void updateDocumentContentFromUnexeitingWebdavDocument() throws Exception {
    SimpleAttachment enDocumentContent = getJcr().defaultENContent();
    SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
    document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");

    webdavService.updateDocumentContent(document);
  }

  @Test
  public void updateDocumentContent() throws Exception {
    SimpleAttachment enDocumentContent = getJcr().defaultENContent();
    SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
    document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");
    setDocumentIntoWebdav(document);

    getJcr().assertContent(document.getId(), "fr", null);
    getJcr().assertContent(document.getId(), "en", "Whaou !");
    assertWebdavContent(document, "Whaou !", document.getWebdavJcrPath());

    setWebdavBinaryContent(document, "Updated webdav content.");

    getJcr().assertContent(document.getId(), "fr", null);
    getJcr().assertContent(document.getId(), "en", "Whaou !");
    assertWebdavContent(document, "Updated webdav content.", document.getWebdavJcrPath());

    webdavService.updateDocumentContent(document);

    getJcr().assertContent(document.getId(), "fr", null);
    getJcr().assertContent(document.getId(), "en", "Updated webdav content.");
    assertWebdavContent(document, "Updated webdav content.", document.getWebdavJcrPath());
  }

  @Test
  public void getContentEditionLanguage() throws Exception {
    SimpleAttachment frDocumentContent = getJcr().defaultFRContent();
    SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
    document = getJcr().createAttachmentForTest(document, frDocumentContent, "FR content");
    document.setAttachment(getJcr().defaultENContent());
    getJcr().updateAttachmentForTest(document, "en", "EN content");

    assertThat(webdavService.getContentEditionLanguage(document), nullValue());

    setDocumentIntoWebdav(document);
    assertWebdavContent(document, "EN content", document.getWebdavJcrPath());

    assertThat(webdavService.getContentEditionLanguage(document), is("en"));

    document = getJcr().assertContent(document.getId(), "fr", "FR content");
    assertThat(webdavService.getContentEditionLanguage(document), is("en"));
  }

  protected void assertWebdavContent(SimpleDocument document, String documentContent,
      String relativeWebdavJcrPath) throws Exception {
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node rootNode = session.getRootNode();
      Node webdavDocumentNode = getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath());
      assertThat(webdavDocumentNode, notNullValue());
      assertThat(webdavDocumentNode.getPath(), is("/" + document.getWebdavJcrPath()));

      // No user editor has been specified
      assertThat(webdavDocumentNode.canAddMixin(SLV_OWNABLE_MIXIN), is(true));
      assertThat(webdavDocumentNode.hasProperty(SLV_PROPERTY_OWNER), is(false));
      // Single child node must exist
      Node contentFileNode = getJcr().getSingleChildNode(webdavDocumentNode);
      assertThat(contentFileNode.getPath(), is("/" + relativeWebdavJcrPath + "/jcr:content"));
      assertThat(contentFileNode.getProperty(JCR_MIMETYPE).getString(),
          is(document.getContentType()));
      assertThat(contentFileNode.getProperty(JCR_ENCODING).getString(), is(""));
      assertThat(contentFileNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          greaterThan(getJcr().getTestStartDate().getTime()));
      // Content
      assertThat(getJcr().getBinaryContentAsString(contentFileNode.getProperty(JCR_DATA)),
          is(documentContent));
    }
  }

  protected void setDocumentIntoWebdav(SimpleDocument document) throws Exception {
    try (JCRSession session = JCRSession.openSystemSession()) {
      ServiceProvider.getService(WebdavDocumentRepository.class)
          .createAttachmentNode(session, document);
      session.save();
    }
  }

  protected void setWebdavBinaryContent(SimpleDocument document, @SuppressWarnings(
      "SameParameterValue")
  String content) throws Exception {
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node webdavJcrNode =
          getJcr().getRelativeNode(session.getRootNode(), document.getWebdavJcrPath());
      getJcr().setBinaryContent(webdavJcrNode.getNode(JCR_CONTENT).getProperty(JCR_DATA),
          content.getBytes());
      session.save();
    }
  }
}