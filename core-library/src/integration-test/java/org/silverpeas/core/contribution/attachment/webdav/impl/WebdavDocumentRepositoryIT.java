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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.webdav.impl;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.jcr.JcrIntegrationIT;
import org.silverpeas.core.util.ServiceProvider;

import javax.jcr.ItemExistsException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.jcr.Property.*;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.persistence.jcr.util.JcrConstants.*;
import static org.silverpeas.core.test.util.TestRuntime.awaitUntil;

@RunWith(Arquillian.class)
public class WebdavDocumentRepositoryIT extends JcrIntegrationIT {

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(WebdavDocumentRepositoryIT.class)
        .addJcrFeatures()
        .build();
  }

  @Test
  public void testCreateAttachmentNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      String relativeWebdavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      Node rootNode = session.getRootNode();
      assertThat(document.getWebdavJcrPath(), is(relativeWebdavJcrPath));
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertWebdavDocumentDoesNotExist(session, document);

      webdavRepository.createAttachmentNode(session, document);

      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      webdavRepository.createAttachmentNode(session, document);

      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      document.setAttachment(getJcr().defaultFRContent());
      document = getJcr().updateAttachmentForTest(document, "fr", "Whaou FR!");

      webdavRepository.createAttachmentNode(session, document);

      assertWebdavContent(session, document, "Whaou FR!", document.getWebdavJcrPath());
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/fr/test.odp/jcr:content"));
    });
  }

  @Test
  public void testUpdateAttachmentNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      String relativeWebdavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      Node rootNode = session.getRootNode();
      assertThat(document.getWebdavJcrPath(), is(relativeWebdavJcrPath));
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertWebdavDocumentDoesNotExist(session, document);

      webdavRepository.updateNodeAttachment(session, document);

      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      webdavRepository.updateNodeAttachment(session, document);

      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      document.setAttachment(getJcr().defaultFRContent());
      document = getJcr().updateAttachmentForTest(document, "fr", "Whaou FR!");

      webdavRepository.updateNodeAttachment(session, document);

      assertWebdavContent(session, document, "Whaou FR!", document.getWebdavJcrPath());
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/fr/test.odp/jcr:content"));
    });
  }

  @Test
  public void testUpdateAttachment() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      String relativeWebdavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      Node rootNode = session.getRootNode();
      assertThat(document.getWebdavJcrPath(), is(relativeWebdavJcrPath));
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertWebdavDocumentDoesNotExist(session, document);

      webdavRepository.createAttachmentNode(session, document);

      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      Node webdavJcrNode = getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath());
      getJcr().setBinaryContent(webdavJcrNode.getNode(JCR_CONTENT).getProperty(JCR_DATA),
          "Updated webdav content.".getBytes());

      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");
      assertWebdavContent(session, document, "Updated webdav content.", relativeWebdavJcrPath);

      webdavRepository.updateNodeAttachment(session, document);

      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      getJcr().setBinaryContent(webdavJcrNode.getNode(JCR_CONTENT).getProperty(JCR_DATA),
          "Updated webdav content.".getBytes());

      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");
      assertWebdavContent(session, document, "Updated webdav content.", relativeWebdavJcrPath);

      Date dateOfCreateOrUpdate = document.getLastUpdateDate();
      await().atLeast(10, TimeUnit.MILLISECONDS).timeout(1, TimeUnit.SECONDS).until(() -> true);
      webdavRepository.updateAttachmentBinaryContent(session, document);

      getJcr().assertContent(document.getId(), "fr", null);
      document = getJcr().assertContent(document.getId(), "en", "Updated webdav content.");
      assertThat(document.getLastUpdateDate(), is(dateOfCreateOrUpdate));
      assertWebdavContent(session, document, "Updated webdav content.", relativeWebdavJcrPath);

      webdavRepository.updateNodeAttachment(session, document);

      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Updated webdav content.");
      assertWebdavContent(session, document, "Updated webdav content.", relativeWebdavJcrPath);

      document = getJcr().updateAttachmentForTest(document, "fr", "Whaou FR!");

      webdavRepository.updateNodeAttachment(session, document);

      getJcr().assertContent(document.getId(), "fr", "Whaou FR!");
      getJcr().assertContent(document.getId(), "en", "Updated webdav content.");
      assertWebdavContent(session, document, "Whaou FR!",
          relativeWebdavJcrPath.replace("/en/", "/fr/"));
    });
  }

  @Test
  public void testMoveAttachmentNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      String relativeWebdavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      Node rootNode = session.getRootNode();
      assertThat(document.getWebdavJcrPath(), is(relativeWebdavJcrPath));
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());

      webdavRepository.moveNodeAttachment(session, new SimpleDocument(), "targetInstanceId");

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());

      webdavRepository.moveNodeAttachment(session, document, document.getInstanceId());

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());

      webdavRepository.moveNodeAttachment(session, document, null);

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());

      webdavRepository.moveNodeAttachment(session, document, "");

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());

      webdavRepository.moveNodeAttachment(session, document, "targetInstanceId");

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());

      webdavRepository.createAttachmentNode(session, document);

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      webdavRepository.moveNodeAttachment(session, document, document.getInstanceId());

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      webdavRepository.moveNodeAttachment(session, document, null);

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      webdavRepository.moveNodeAttachment(session, document, "");

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      webdavRepository.moveNodeAttachment(session, document, "targetInstanceId");

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode),
          contains("/webdav/attachments/targetInstanceId/" + document.getId() +
              "/en/test.pdf/jcr:content"));

      document.setAttachment(getJcr().defaultFRContent());
      document = getJcr().updateAttachmentForTest(document, "fr", "FR content");
      webdavRepository.createAttachmentNode(session, document);
      getJcr().assertContent(document.getId(), "fr", "FR content");
      getJcr().assertContent(document.getId(), "en", "Whaou !");
      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), containsInAnyOrder(
          "/webdav/attachments/kmelia26/" + document.getId() + "/fr/test.odp/jcr:content",
          "/webdav/attachments/targetInstanceId/" + document.getId() +
              "/en/test.pdf/jcr:content"));
    });
  }

  @Test(expected = ItemExistsException.class)
  public void testMoveAttachmentNodeToExistingNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "Whaou !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      webdavRepository.createAttachmentNode(session, document);

      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));

      webdavRepository.moveNodeAttachment(session, document, "targetInstanceId");

      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode),
          contains("/webdav/attachments/targetInstanceId/" + document.getId() +
              "/en/test.pdf/jcr:content"));

      document.setAttachment(getJcr().defaultFRContent());
      document = getJcr().updateAttachmentForTest(document, "fr", "FR content");
      webdavRepository.createAttachmentNode(session, document);
      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), containsInAnyOrder(
          "/webdav/attachments/kmelia26/" + document.getId() + "/fr/test.odp/jcr:content",
          "/webdav/attachments/targetInstanceId/" + document.getId() +
              "/en/test.pdf/jcr:content"));

      webdavRepository.moveNodeAttachment(session, document, "targetInstanceId");
    });
  }

  @Test
  public void testDeleteAttachmentContentNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment frDocumentContent = getJcr().defaultFRContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, frDocumentContent, "FR content");
      document.setAttachment(getJcr().defaultENContent());
      document = getJcr().updateAttachmentForTest(document, "en", "Whaou !");
      getJcr().assertContent(document.getId(), "fr", "FR content");
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      String relativeWebdavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      Node rootNode = session.getRootNode();
      assertThat(document.getWebdavJcrPath(), is(relativeWebdavJcrPath));
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertWebdavDocumentDoesNotExist(session, document);

      webdavRepository.createAttachmentNode(session, document);

      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      // Delete FR language (nothing is deleted as the aimed webdavDocument is EN one)
      webdavRepository.deleteAttachmentContentNode(session, document, "fr");
      // Verifying webdav EN content already existing
      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      document = getJcr().assertContent(document.getId(), "en", "Whaou !");
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      // Delete EN language
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), notNullValue());
      webdavRepository.deleteAttachmentContentNode(session, document, "en");
      getJcr().assertContent(document.getId(), "en", "Whaou !");
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), nullValue());

      // Two languages in webdav
      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      webdavRepository.createAttachmentNode(session, document);
      assertWebdavContent(session, document, "FR content", document.getWebdavJcrPath());
      document = getJcr().assertContent(document.getId(), "en", "Whaou !");
      webdavRepository.createAttachmentNode(session, document);
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      // Delete EN language
      webdavRepository.deleteAttachmentContentNode(session, document, "en");
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), nullValue());
      getJcr().assertContent(document.getId(), "en", "Whaou !");
      getJcr().assertContent(document.getId(), "fr", "FR content");
    });
  }

  @Test
  public void testDeleteAttachmentNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment frDocumentContent = getJcr().defaultFRContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, frDocumentContent, "FR content");
      document.setAttachment(getJcr().defaultENContent());
      document = getJcr().updateAttachmentForTest(document, "en", "Whaou !");
      getJcr().assertContent(document.getId(), "fr", "FR content");
      getJcr().assertContent(document.getId(), "en", "Whaou !");

      Node rootNode = session.getRootNode();
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), nullValue());
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), nullValue());

      String relativeWebdavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      assertThat(document.getWebdavJcrPath(), is(relativeWebdavJcrPath));
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertWebdavDocumentDoesNotExist(session, document);

      webdavRepository.createAttachmentNode(session, document);

      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      // Delete FR language (deletes content all indeed)
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), notNullValue());
      webdavRepository.deleteAttachmentNode(session, document);
      // Verifying webdav EN content already existing
      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      document = getJcr().assertContent(document.getId(), "en", "Whaou !");
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), nullValue());

      // Two languages in webdav
      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      webdavRepository.createAttachmentNode(session, document);
      assertWebdavContent(session, document, "FR content", document.getWebdavJcrPath());
      document = getJcr().assertContent(document.getId(), "en", "Whaou !");
      webdavRepository.createAttachmentNode(session, document);
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);

      // Delete EN language
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), notNullValue());
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().getRelativeNode(rootNode, SimpleDocument.WEBDAV_FOLDER), nullValue());

      // Two languages in webdav
      document = getJcr().assertContent(document.getId(), "fr", "FR content");
      webdavRepository.createAttachmentNode(session, document);
      assertWebdavContent(session, document, "FR content", document.getWebdavJcrPath());
      document = getJcr().assertContent(document.getId(), "en", "Whaou !");
      webdavRepository.createAttachmentNode(session, document);
      assertWebdavContent(session, document, "Whaou !", relativeWebdavJcrPath);
      // Adding an other node for "kmelia26"
      Node kmelia26Node = getJcr().getRelativeNode(rootNode, "webdav/attachments/kmelia26");
      kmelia26Node.addNode("en", NT_FOLDER).addNode("Test", NT_FOLDER);
      assertThat(getJcr().listPathesFrom(kmelia26Node), containsInAnyOrder(
              "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content",
              "/webdav/attachments/kmelia26/en/Test")
      );

      // Delete EN language
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(kmelia26Node),
          containsInAnyOrder("/webdav/attachments/kmelia26/en/Test"));

      // Delete Test (no id and no language)
      document.setFilename("Test");
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(kmelia26Node),
          containsInAnyOrder("/webdav/attachments/kmelia26/en/Test"));

      document.setLanguage("fr");
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(kmelia26Node),
          containsInAnyOrder("/webdav/attachments/kmelia26/en/Test"));

      document.setFilename("xxxxx");
      document.setLanguage("en");
      document.setId(null);
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(kmelia26Node),
          containsInAnyOrder("/webdav/attachments/kmelia26/en/Test"));

      document.setFilename("Test");
      document.setLanguage("fr");
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(kmelia26Node),
          containsInAnyOrder("/webdav/attachments/kmelia26/en/Test"));

      document.setLanguage("en");
      webdavRepository.deleteAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(kmelia26Node), nullValue());
    });
  }

  @Test
  public void testGetContentEditionLanguageAndSizeAndDescriptor() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "EN content");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "EN content");

      /*
      No WEBDAV data
       */

      // initialization FR no data
      SimpleDocument frDocumentFromEnCopy = new SimpleDocument(document);
      frDocumentFromEnCopy.setLanguage("fr");
      assertNoWebdavDesc(session, webdavRepository, frDocumentFromEnCopy);
      // no identifier FR
      frDocumentFromEnCopy.setPK(frDocumentFromEnCopy.getPk().clone());
      frDocumentFromEnCopy.setId(null);
      assertNoWebdavDesc(session, webdavRepository, frDocumentFromEnCopy);
      // no identifier DE
      frDocumentFromEnCopy.setLanguage("de");
      assertNoWebdavDesc(session, webdavRepository, frDocumentFromEnCopy);

      /*
      Registering into WEBDAV a new EN content (wait 100ms for date checking)
       */

      final OffsetDateTime beforeDate = OffsetDateTime.now();
      await().atLeast(100, MILLISECONDS).until(() -> true);
      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());
      document = getJcr().assertContent(document.getId(), "en", "EN content");
      webdavRepository.createAttachmentNode(session, document);
      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));
      // content EN exists
      final WebdavContentDescriptor enDesc =
          assertWebdavDesc(session, webdavRepository, document, "en", 10L, beforeDate);
      document.setLanguage("fr");
      // content FR is indeed the EN
      assertWebdavDesc(session, webdavRepository, document, "en", 10L, beforeDate, enDesc);
      // no identifier
      final String documentId = document.getId();
      document.setId(null);
      assertNoWebdavDesc(session, webdavRepository, document);
      // checking EN content
      document = getJcr().assertContent(documentId, "en", "EN content");
      assertWebdavContent(session, document, "EN content", document.getWebdavJcrPath());

      /*
      Registering into WEBDAV a FR content to the same document (wait 100ms for date checking)
       */

      OffsetDateTime beforeDate2 = OffsetDateTime.now();
      await().atLeast(100, MILLISECONDS).until(() -> true);
      document.setAttachment(getJcr().defaultFRContent());
      document = getJcr().updateAttachmentForTest(document, "fr", "FR content");
      SimpleDocument frDocument = getJcr().assertContent(document.getId(), "fr", "FR content");
      SimpleDocument enDocument = getJcr().assertContent(document.getId(), "en", "EN content");
      webdavRepository.createAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/fr/test.odp/jcr:content"));
      // content FR exists
      final WebdavContentDescriptor frDesc2 =
          assertWebdavDesc(session, webdavRepository, frDocument, "fr", 10L, beforeDate2);
      assertWebdavDesc(session, webdavRepository, enDocument, "fr", 10L, beforeDate2, frDesc2);

      /*
      Updating into WEBDAV the EN content to the same document (wait 100ms for date checking)
       */

      OffsetDateTime beforeDate3 = OffsetDateTime.now();
      await().atLeast(100, MILLISECONDS).until(() -> true);
      document.setAttachment(getJcr().defaultENContent());
      getJcr().updateAttachmentForTest(document, "en", "EN content updated");
      webdavRepository.createAttachmentNode(session, document);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));
      final WebdavContentDescriptor frDesc3 =
          assertWebdavDesc(session, webdavRepository, frDocument, "en", 18L, beforeDate3);
      assertWebdavDesc(session, webdavRepository, enDocument, "en", 18L, beforeDate3, frDesc3);
    });
  }

  @Test
  public void testReadAndWriteContentEdition() throws Exception {
    execute((session, webdavRepository) -> {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "EN content");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "EN content");

      SimpleDocument frDocumentFromEnCopy = new SimpleDocument(document);
      frDocumentFromEnCopy.setLanguage("fr");
      assertNoWebdavDesc(session, webdavRepository, frDocumentFromEnCopy);

      /*
      Registering into WEBDAV a new EN content (wait 100ms for date checking)
       */

      final OffsetDateTime beforeDate = OffsetDateTime.now();
      await().atLeast(100, MILLISECONDS).until(() -> true);
      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());
      document = getJcr().assertContent(document.getId(), "en", "EN content");
      webdavRepository.createAttachmentNode(session, document);
      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));
      // content EN exists
      final WebdavContentDescriptor docDesc =
          assertWebdavDesc(session, webdavRepository, document, "en", 10L, beforeDate);
      assertStreamedContent(session, webdavRepository, document, baos, "EN content");
      // content FR is indeed the EN
      document.setLanguage("fr");
      assertWebdavDesc(session, webdavRepository, document, "en", 10L, beforeDate, docDesc);
      assertStreamedContent(session, webdavRepository, document, baos, "EN content");
      // no identifier
      final String documentId = document.getId();
      document.setId(null);
      assertNoWebdavDesc(session, webdavRepository, document);
      assertStreamedContent(session, webdavRepository, document, baos, "");
      // checking EN content
      document = getJcr().assertContent(documentId, "en", "EN content");
      assertWebdavContent(session, document, "EN content", document.getWebdavJcrPath());

      /*
      Updating into WEBDAV the EN content to the same document (wait 100ms for date checking)
       */

      await().atLeast(100, MILLISECONDS).until(() -> true);
      document.setLanguage("fr");
      document.setId(documentId);
      webdavRepository.updateContentFrom(session, document, new ByteArrayInputStream("A new content!!!".getBytes()));
      document = getJcr().assertContent(documentId, "en", "EN content");
      assertWebdavContent(session, document, "A new content!!!", document.getWebdavJcrPath());
      // content EN exists and is updated
      final WebdavContentDescriptor docDesc2 = assertWebdavDesc(session, webdavRepository, document,
          "en", 16L, docDesc.getLastModificationDate());
      assertStreamedContent(session, webdavRepository, document, baos, "A new content!!!");
      // content FR is indeed the EN (so updated)
      document.setLanguage("fr");
      assertWebdavDesc(session, webdavRepository, document, "en", 16L,
          docDesc.getLastModificationDate(), docDesc2);
      assertStreamedContent(session, webdavRepository, document, baos, "A new content!!!");
      // no identifier
      document.setId(null);
      assertNoWebdavDesc(session, webdavRepository, document);
      assertStreamedContent(session, webdavRepository, document, baos, "");
    });
  }

  private void assertStreamedContent(final Session session,
      final WebdavDocumentRepository webdavRepository, final SimpleDocument document,
      final ByteArrayOutputStream baos, final String expContent)
      throws RepositoryException, IOException {
    webdavRepository.loadContentInto(session, document, baos);
    assertThat(baos.toString(), is(expContent));
    baos.reset();
  }

  private void assertNoWebdavDesc(final Session session,
      final WebdavDocumentRepository repo, final SimpleDocument document)
      throws RepositoryException {
    final Optional<WebdavContentDescriptor> descriptor = repo.getDescriptor(session, document);
    final String contentEditionLanguage = repo.getContentEditionLanguage(session, document);
    final long contentEditionSize = repo.getContentEditionSize(session, document);
    assertThat(descriptor.isPresent(), is(false));
    assertThat(contentEditionLanguage, nullValue());
    assertThat(contentEditionSize, is(-1L));
  }

  private WebdavContentDescriptor assertWebdavDesc(final Session session,
      final WebdavDocumentRepository repo, final SimpleDocument document,
      final String expContentEditionLanguage, final long expContentEditionSize,
      final OffsetDateTime greaterThan) throws RepositoryException {
    return assertWebdavDesc(session, repo, document, expContentEditionLanguage,
        expContentEditionSize, greaterThan, null);
  }

  private WebdavContentDescriptor assertWebdavDesc(final Session session,
      final WebdavDocumentRepository repo, final SimpleDocument document,
      final String expContentEditionLanguage, final long expContentEditionSize,
      final OffsetDateTime greaterThan, final WebdavContentDescriptor same)
      throws RepositoryException {
    final Optional<WebdavContentDescriptor> descriptor = repo.getDescriptor(session, document);
    final String contentEditionLanguage = repo.getContentEditionLanguage(session, document);
    final long contentEditionSize = repo.getContentEditionSize(session, document);
    assertThat(descriptor.isPresent(), is(true));
    assertThat(contentEditionLanguage, is(expContentEditionLanguage));
    assertThat(contentEditionSize, is(expContentEditionSize));
    assertThat(greaterThan, notNullValue());
    final WebdavContentDescriptor descriptorData = descriptor.orElse(null);
    assertThat(descriptorData, notNullValue());
    assertThat(descriptorData.getDocument(), is(document));
    assertThat(descriptorData.getId(), notNullValue());
    assertThat(descriptorData.getLanguage(), is(expContentEditionLanguage));
    assertThat(descriptorData.getSize(), is(expContentEditionSize));
    assertThat(descriptorData.getLastModificationDate(), greaterThan(greaterThan));
    if (same != null) {
      assertThat(descriptorData.getLastModificationDate(), equalTo(same.getLastModificationDate()));
    }
    return descriptorData;
  }

  @Test
  public void testGetDocumentIdentifierNode() throws Exception {
    execute((session, webdavRepository) -> {

      // No attachment identifier
      assertThat(webdavRepository.getDocumentIdentifierNode(session, new SimpleDocument()),
          nullValue());

      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "EN content");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "EN content");

      // Attachment is not registred into webdav
      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());
      assertThat(webdavRepository.getDocumentIdentifierNode(session, document), nullValue());

      // Attachment is registred into webdav
      webdavRepository.createAttachmentNode(session, document);
      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));
      Node documentIdentifierNode = webdavRepository.getDocumentIdentifierNode(session, document);
      assertThat(documentIdentifierNode, notNullValue());
      assertThat(documentIdentifierNode.getPath(),
          is("/webdav/attachments/kmelia26/" + document.getId()));
    });
  }

  @Test
  public void testGetDocumentContentLanguageNode() throws Exception {
    execute((session, webdavRepository) -> {

      // No attachment identifier
      assertThat(
          webdavRepository.getDocumentContentLanguageNode(session, new SimpleDocument(), null),
          nullValue());
      assertThat(
          webdavRepository.getDocumentContentLanguageNode(session, new SimpleDocument(), ""),
          nullValue());
      assertThat(
          webdavRepository.getDocumentContentLanguageNode(session, new SimpleDocument(), "en"),
          nullValue());

      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "EN content");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "EN content");

      // Attachment is not registred into webdav
      Node webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(webdavNode, nullValue());
      assertThat(webdavRepository.getDocumentContentLanguageNode(session, document, null),
          nullValue());
      assertThat(webdavRepository.getDocumentContentLanguageNode(session, document, ""),
          nullValue());
      assertThat(webdavRepository.getDocumentContentLanguageNode(session, document, "en"),
          nullValue());

      // Attachment is registred into webdav
      webdavRepository.createAttachmentNode(session, document);
      webdavNode = getJcr().getRelativeNode(session.getRootNode(), SimpleDocument.WEBDAV_FOLDER);
      assertThat(getJcr().listPathesFrom(webdavNode), contains(
          "/webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf/jcr:content"));
      Node documentIdentifierNode =
          webdavRepository.getDocumentContentLanguageNode(session, document, null);
      assertThat(documentIdentifierNode, nullValue());
      documentIdentifierNode =
          webdavRepository.getDocumentContentLanguageNode(session, document, "");
      assertThat(documentIdentifierNode, nullValue());
      documentIdentifierNode =
          webdavRepository.getDocumentContentLanguageNode(session, document, "fr");
      assertThat(documentIdentifierNode, nullValue());
      documentIdentifierNode =
          webdavRepository.getDocumentContentLanguageNode(session, document, "en");
      assertThat(documentIdentifierNode, notNullValue());
      assertThat(documentIdentifierNode.getPath(),
          is("/webdav/attachments/kmelia26/" + document.getId() + "/en"));
    });
  }

  @Test
  public void testAddFolder() throws Exception {
    execute((session, webdavRepository) -> {
      String nodeNameForTest = "a_node_for_test";
      Node rootTestNode = session.getRootNode().addNode("test");
      assertThat(rootTestNode.hasNode(nodeNameForTest), is(false));
      webdavRepository.addFolder(rootTestNode, nodeNameForTest);
      webdavRepository.addFolder(rootTestNode, nodeNameForTest + "_2");
      webdavRepository.addFolder(rootTestNode, nodeNameForTest + "_3");
      webdavRepository.addFolder(rootTestNode, nodeNameForTest + "_4");
      assertThat(getJcr().listPathesFrom(rootTestNode),
          containsInAnyOrder("/test/a_node_for_test", "/test/a_node_for_test_2",
              "/test/a_node_for_test_3", "/test/a_node_for_test_4")
      );
    });
  }

  @Test
  public void testAddExclusiveFolder() throws Exception {
    execute((session, webdavRepository) -> {
      String nodeNameForTest = "a_node_for_test";
      Node rootTestNode = session.getRootNode().addNode("test");
      assertThat(rootTestNode.hasNode(nodeNameForTest), is(false));
      webdavRepository.addExclusiveFolder(rootTestNode, nodeNameForTest);
      webdavRepository.addExclusiveFolder(rootTestNode, nodeNameForTest + "_2");
      webdavRepository.addExclusiveFolder(rootTestNode, nodeNameForTest + "_3");
      webdavRepository.addExclusiveFolder(rootTestNode, nodeNameForTest + "_4");
      assertThat(getJcr().listPathesFrom(rootTestNode), contains("/test/a_node_for_test_4"));
    });
  }

  @Test
  public void testIsNodeLocked() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "A super content !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "A super content !");

      Node rootNode = session.getRootNode();
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      String relativeWebDavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      assertThat(document.getWebdavJcrPath(), is(relativeWebDavJcrPath));
      webdavRepository.createAttachmentNode(session, document);
      session.save();
      String fullWebDavJcrPath = "/" + relativeWebDavJcrPath;
      Node webdavDocumentNode = session.getNode(fullWebDavJcrPath);

      assertThat(webdavDocumentNode, notNullValue());
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()).getPath(),
          is(webdavDocumentNode.getPath()));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      webdavDocumentNode.addMixin(NodeType.MIX_LOCKABLE);
      session.save();

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), false, true, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      session.getWorkspace().getLockManager().unlock(webdavDocumentNode.getPath());
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), true, false, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      session.getWorkspace().getLockManager().unlock(webdavDocumentNode.getPath());
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), false, false, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      session.getWorkspace().getLockManager().unlock(webdavDocumentNode.getPath());
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), true, true, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      session.getWorkspace().getLockManager().unlock(webdavDocumentNode.getPath());
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));
    });
  }

  @Test
  public void testUnlockLockedNode() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "A super content !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "A super content !");

      Node rootNode = session.getRootNode();
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()), nullValue());
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      String relativeWebDavJcrPath =
          "webdav/attachments/kmelia26/" + document.getId() + "/en/test.pdf";
      assertThat(document.getWebdavJcrPath(), is(relativeWebDavJcrPath));
      webdavRepository.createAttachmentNode(session, document);
      session.save();
      String fullWebDavJcrPath = "/" + relativeWebDavJcrPath;
      Node webdavDocumentNode = session.getNode(fullWebDavJcrPath);

      assertThat(webdavDocumentNode, notNullValue());
      assertThat(getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath()).getPath(),
          is(webdavDocumentNode.getPath()));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      webdavDocumentNode.addMixin(NodeType.MIX_LOCKABLE);
      session.save();

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), false, true, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      webdavRepository.unlockLockedNode(session, document);
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), true, false, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      webdavRepository.unlockLockedNode(session, document);
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), false, false, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      webdavRepository.unlockLockedNode(session, document);
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));

      session.getWorkspace().getLockManager()
          .lock(webdavDocumentNode.getPath(), true, true, 60, "26");
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(true));
      assertThat(webdavRepository.isNodeLocked(session, document), is(true));

      webdavRepository.unlockLockedNode(session, document);
      assertThat(session.getWorkspace().getLockManager().isLocked(fullWebDavJcrPath), is(false));
      assertThat(webdavRepository.isNodeLocked(session, document), is(false));
    });
  }

  @Test
  public void testAddFile() throws Exception {
    execute((session, webdavRepository) -> {
      SimpleAttachment enDocumentContent = getJcr().defaultENContent();
      SimpleDocument document = getJcr().defaultDocument("kmelia26", "foreignId38");
      document = getJcr().createAttachmentForTest(document, enDocumentContent, "A super content !");
      getJcr().assertContent(document.getId(), "fr", null);
      getJcr().assertContent(document.getId(), "en", "A super content !");

      String nodeNameForTest = "a_node_for_test";
      Node rootNode = session.getRootNode();
      assertThat(rootNode.hasNode(nodeNameForTest), is(false));
      Node nodeForTest = webdavRepository.addFolder(rootNode, nodeNameForTest);
      assertThat(rootNode.hasNode(nodeNameForTest), is(true));
      assertThat(nodeForTest.getPath(), is("/a_node_for_test"));
      assertThat(nodeForTest.hasNodes(), is(false));

      Node fileNameNode = webdavRepository.addFile(nodeForTest, document);
      assertThat(getJcr().listPathesFrom(nodeForTest), contains("/a_node_for_test/test.pdf/jcr:content"));

      // Existence
      assertThat(fileNameNode.getPath(), is("/a_node_for_test/test.pdf"));
      // No user editor has been specified
      assertThat(fileNameNode.canAddMixin(SLV_OWNABLE_MIXIN), is(true));
      assertThat(fileNameNode.hasProperty(SLV_PROPERTY_OWNER), is(false));
      // Single child node must exists
      Node contentFileNode = getJcr().getSingleChildNode(fileNameNode);
      assertThat(contentFileNode.getPath(), is("/a_node_for_test/test.pdf/jcr:content"));
      assertThat(contentFileNode.getProperty(JCR_MIMETYPE).getString(),
          is(document.getContentType()));
      assertThat(contentFileNode.getProperty(JCR_ENCODING).getString(), is(""));
      assertThat(contentFileNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          greaterThan(getJcr().getTestStartDate().getTime()));
      // Content
      assertThat(getJcr().getBinaryContentAsString(contentFileNode.getProperty(JCR_DATA)),
          is("A super content !"));

      // Setting a user editor.
      getJcr().updateAttachmentForTest(document, "fr", "Un super contenu !");
      document.setFilename("newEnFileName");
      document = getJcr().updateAttachmentForTest(document, "en", "A super mega content !");
      getJcr().assertContent(document.getId(), "fr", "Un super contenu !");
      getJcr().assertContent(document.getId(), "en", "A super mega content !");
      document.edit("26");
      webdavRepository.addFile(nodeForTest, document);
      assertThat(getJcr().listPathesFrom(nodeForTest),
          contains("/a_node_for_test/newEnFileName/jcr:content"));
      fileNameNode = getJcr().getSingleChildNode(nodeForTest);

      // Existence
      assertThat(fileNameNode.getPath(), is("/a_node_for_test/newEnFileName"));
      // User editor has been specified
      assertThat(fileNameNode.canAddMixin(SLV_OWNABLE_MIXIN), is(true));
      assertThat(fileNameNode.hasProperty(SLV_PROPERTY_OWNER), is(true));
      assertThat(fileNameNode.getProperty(SLV_PROPERTY_OWNER).getString(), is("26"));
      // Single child node must exists
      contentFileNode = getJcr().getSingleChildNode(fileNameNode);
      assertThat(contentFileNode.getPath(), is("/a_node_for_test/newEnFileName/jcr:content"));
      assertThat(contentFileNode.getProperty(JCR_MIMETYPE).getString(),
          is(document.getContentType()));
      assertThat(contentFileNode.getProperty(JCR_ENCODING).getString(), is(""));
      assertThat(contentFileNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
          greaterThan(getJcr().getTestStartDate().getTime()));
      // Content
      assertThat(getJcr().getBinaryContentAsString(contentFileNode.getProperty(JCR_DATA)),
          is("A super mega content !"));
    });
  }

  /*
  TEST TOOLS
   */

  private interface WebdavTest {
    void execute(Session session, WebdavDocumentRepository webdavRepository) throws Exception;
  }

  private void execute(WebdavTest test) throws Exception {
    try (JcrSession session = openSystemSession()) {
      test.execute(session, ServiceProvider.getService(WebdavDocumentRepository.class));
    }
  }

  /**
   * Assertion of a JCR webdav content ...
   * @param session the current JCR session.
   * @param document the document to assert into the webdav space.
   * @param documentContent the document content to verify.
   * @param relativeWebdavJcrPath the relative path into webdav space.
   * @throws Exception
   */
  protected void assertWebdavContent(Session session, SimpleDocument document,
      String documentContent, String relativeWebdavJcrPath) throws Exception {

    Node rootNode = session.getRootNode();
    Node webdavDocumentNode = getJcr().getRelativeNode(rootNode, document.getWebdavJcrPath());
    assertThat(webdavDocumentNode, notNullValue());
    assertThat(webdavDocumentNode.getPath(), is("/" + document.getWebdavJcrPath()));

    // No user editor has been specified
    assertThat(webdavDocumentNode.canAddMixin(SLV_OWNABLE_MIXIN), is(true));
    assertThat(webdavDocumentNode.hasProperty(SLV_PROPERTY_OWNER), is(false));
    // Single child node must exists
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

  /**
   * Assertion of a JCR webdav content ...
   * @param session the current JCR session.
   * @param document the document to assert into the webdav space.
   * @throws Exception
   */
  protected void assertWebdavDocumentDoesNotExist(Session session, SimpleDocument document)
      throws Exception {
    assertThat(getWebdavDocumentIdNode(session, document), nullValue());
  }

  /**
   * Gets the Node of Webdav document identifier from a Webdav Jcr Path.
   * @param session the current JCR session.
   * @param document the document to assert into the webdav space.
   * @return the aimed {@link Node}.
   * @throws Exception
   */
  protected Node getWebdavDocumentIdNode(Session session, SimpleDocument document)
      throws Exception {
    Pattern pattern = Pattern.compile(".*/" + document.getId());
    Matcher matcher = pattern.matcher(document.getWebdavJcrPath());
    if (!matcher.find()) {
      return null;
    }
    return getJcr().getRelativeNode(session.getRootNode(), matcher.group());
  }
}