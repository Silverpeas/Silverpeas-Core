/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.attachment.webdav;

import com.silverpeas.jcrutil.BasicDaoFactory;
import org.junit.Test;
import org.silverpeas.attachment.AttachmentException;
import org.silverpeas.attachment.WebdavServiceProvider;
import org.silverpeas.attachment.model.SimpleAttachment;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.repository.JcrTest;
import org.silverpeas.attachment.webdav.impl.WebdavDocumentRepository;

import javax.jcr.Node;
import javax.jcr.Session;

import static org.silverpeas.jcr.util.JcrConstants.SLV_OWNABLE_MIXIN;
import static org.silverpeas.jcr.util.JcrConstants.SLV_PROPERTY_OWNER;
import static javax.jcr.Property.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class WebdavServiceTest {

  @Test(expected = AttachmentException.class)
  public void testUpdateDocumentContentFromUnexeitingWebdavDocument() throws Exception {
    new JcrWebdavServiceTest() {
      @Override
      public void run(final WebdavService webdavService) throws Exception {
        SimpleAttachment enDocumentContent = defaultENContent();
        SimpleDocument document = defaultDocument("kmelia26", "foreignId38");
        document = createAttachmentForTest(document, enDocumentContent, "Whaou !");

        webdavService.updateDocumentContent(document);
      }
    }.execute();
  }

  @Test
  public void testUpdateDocumentContent() throws Exception {
    new JcrWebdavServiceTest() {
      @Override
      public void run(final WebdavService webdavService) throws Exception {
        SimpleAttachment enDocumentContent = defaultENContent();
        SimpleDocument document = defaultDocument("kmelia26", "foreignId38");
        document = createAttachmentForTest(document, enDocumentContent, "Whaou !");
        setDocumentIntoWebdav(document);

        assertContent(document.getId(), "fr", null);
        assertContent(document.getId(), "en", "Whaou !");
        assertWebdavContent(document, "Whaou !", document.getWebdavJcrPath());

        setWebdavBinaryContent(document, "Updated webdav content.");

        assertContent(document.getId(), "fr", null);
        assertContent(document.getId(), "en", "Whaou !");
        assertWebdavContent(document, "Updated webdav content.", document.getWebdavJcrPath());

        webdavService.updateDocumentContent(document);

        assertContent(document.getId(), "fr", null);
        assertContent(document.getId(), "en", "Updated webdav content.");
        assertWebdavContent(document, "Updated webdav content.", document.getWebdavJcrPath());
      }
    }.execute();
  }

  @Test
  public void testGetContentEditionLanguage() throws Exception {
    new JcrWebdavServiceTest() {
      @Override
      public void run(final WebdavService webdavService) throws Exception {
        SimpleAttachment frDocumentContent = defaultFRContent();
        SimpleDocument document = defaultDocument("kmelia26", "foreignId38");
        document = createAttachmentForTest(document, frDocumentContent, "FR content");
        document.setAttachment(defaultENContent());
        updateAttachmentForTest(document, "en", "EN content");

        assertThat(webdavService.getContentEditionLanguage(document), nullValue());

        setDocumentIntoWebdav(document);
        assertWebdavContent(document, "EN content", document.getWebdavJcrPath());

        assertThat(webdavService.getContentEditionLanguage(document), is("en"));

        document = assertContent(document.getId(), "fr", "FR content");
        assertThat(webdavService.getContentEditionLanguage(document), is("en"));
      }
    }.execute();
  }

  /**
   * @author: Yohann Chastagnier
   */
  public abstract static class JcrWebdavServiceTest extends JcrTest {

    private WebdavDocumentRepository webdavRepository;
    private WebdavService webdavService;

    @Override
    public void run() throws Exception {
      webdavRepository = (WebdavDocumentRepository) getAppContext().getBean("webdavRepository");
      webdavService = WebdavServiceProvider.getWebdavService();
      Session session = BasicDaoFactory.getSystemSession();
      try {
        run(webdavService);
      } finally {
        BasicDaoFactory.logout(session);
      }
    }

    public abstract void run(WebdavService webdavService) throws Exception;

    /**
     * Assertion of a JCR webdav content ...
     * @param document
     * @param documentContent
     * @param relativeWebdavJcrPath
     * @throws Exception
     */
    protected void assertWebdavContent(SimpleDocument document, String documentContent,
        String relativeWebdavJcrPath) throws Exception {
      Session session = BasicDaoFactory.getSystemSession();
      try {
        Node rootNode = session.getRootNode();
        Node webdavDocumentNode = getRelativeNode(rootNode, document.getWebdavJcrPath());
        assertThat(webdavDocumentNode, notNullValue());
        assertThat(webdavDocumentNode.getPath(), is("/" + document.getWebdavJcrPath()));

        // No user editor has been specified
        assertThat(webdavDocumentNode.canAddMixin(SLV_OWNABLE_MIXIN), is(true));
        assertThat(webdavDocumentNode.hasProperty(SLV_PROPERTY_OWNER), is(false));
        // Single child node must exists
        Node contentFileNode = getSingleChildNode(webdavDocumentNode);
        assertThat(contentFileNode.getPath(), is("/" + relativeWebdavJcrPath + "/jcr:content"));
        assertThat(contentFileNode.getProperty(JCR_MIMETYPE).getString(),
            is(document.getContentType()));
        assertThat(contentFileNode.getProperty(JCR_ENCODING).getString(), is(""));
        assertThat(contentFileNode.getProperty(JCR_LAST_MODIFIED).getDate().getTimeInMillis(),
            greaterThan(getTestStartDate().getTime()));
        // Content
        assertThat(getBinaryContentAsString(contentFileNode.getProperty(JCR_DATA)),
            is(documentContent));
      } finally {
        BasicDaoFactory.logout(session);
      }
    }

    /**
     * Creates a document in document repository and in webdav repository.
     * @param document
     * @throws Exception
     */
    protected void setDocumentIntoWebdav(SimpleDocument document) throws Exception {
      Session session = BasicDaoFactory.getSystemSession();
      try {
        webdavRepository.createAttachmentNode(session, document);
        session.save();
      } finally {
        BasicDaoFactory.logout(session);
      }
    }

    /**
     * Set a content into webdav.
     * @param document
     * @param content
     * @throws Exception
     */
    protected void setWebdavBinaryContent(SimpleDocument document, String content)
        throws Exception {

      Session session = BasicDaoFactory.getSystemSession();
      try {
        Node webdavJcrNode = getRelativeNode(session.getRootNode(), document.getWebdavJcrPath());
        setBinaryContent(webdavJcrNode.getNode(JCR_CONTENT).getProperty(JCR_DATA),
            content.getBytes());
        session.save();
      } finally {
        BasicDaoFactory.logout(session);
      }
    }
  }
}