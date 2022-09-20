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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.mime.MimeTypes;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.lang.SystemWrapper;
import org.silverpeas.jcr.JCRSession;
import org.silverpeas.jcr.impl.RepositorySettings;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * This class handle a JCR test.<br>
 * The repository is created before and deleted after each test.<br>
 * The registered physical files are also deleted.
 * @author Yohann Chastagnier
 */
public class JcrContext implements TestRule {

  private static final String JCR_HOME = "target/";
  private static final String JCR_CONFIG = "classpath:/silverpeas-oak.properties";

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          init();
          base.evaluate();
        } finally {
          clearJcrRepository();
          clearFileSystem();
        }
      }
    };
  }

  private void init() {
    SystemWrapper systemWrapper = SystemWrapper.get();
    systemWrapper.setProperty(RepositorySettings.JCR_HOME, JCR_HOME);
    systemWrapper.setProperty(RepositorySettings.JCR_CONF, JCR_CONFIG);
  }

  private void clearJcrRepository() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      NodeIterator i =  session.getRootNode().getNodes();
      while(i.hasNext()) {
        Node node = i.nextNode();
        if (!"jcr:system".equals(node.getName())) {
          node.remove();
        }
      }
      session.save();
    }
  }

  private void clearFileSystem() {
    File file = new File(FileRepositoryManager.getAbsolutePath(""));
    FileUtils.deleteQuietly(file);
    File index = new File(IndexFileManager.getIndexUpLoadPath());
    FileUtils.deleteQuietly(index);
  }

  /*
  TOOLS
   */

  private final Date testStartDate = new Date();

  private final DocumentConverter converter = new DocumentConverter();

  private DocumentRepository getDocumentRepository() {
    return ServiceProvider.getService(DocumentRepository.class);
  }

  public Date getTestStartDate() {
    return testStartDate;
  }

  /**
   * Common method to assert a document existence.
   * @param uuId the uuId to retrieve the document.
   */
  @SuppressWarnings("unused")
  public void assertDocumentExists(String uuId) throws Exception {
    SimpleDocument document = getDocumentById(uuId);
    assertThat(document, notNullValue());
  }

  /**
   * Common method to assert a document existence.
   * @param uuId the uuId to retrieve the document.
   */
  @SuppressWarnings("unused")
  public void assertDocumentDoesNotExist(String uuId) throws Exception {
    SimpleDocument document = getDocumentById(uuId);
    assertThat(document, nullValue());
  }

  /**
   * Common method to assert the content for a language of a document.
   * @param uuId the uuId to retrieve the document.
   * @param language the language in which the content must be verified.
   * @param expectedContent if null, the content for the language does not exist.
   * @return the document loaded for assertions.
   */
  public SimpleDocument assertContent(String uuId, String language, String expectedContent)
      throws Exception {
    SimpleDocument document = getDocumentById(uuId, language);
    assertThat(document, notNullValue());
    final File physicalContent;
    if (document.getAttachment() != null) {
      physicalContent = new File(FileRepositoryManager.getAbsolutePath(document.getInstanceId()),
          document.getNodeName() + "/" +
              document.getMajorVersion() + "_" + document.getMinorVersion() + "/" + language + "/" +
              document.getFilename()
      );
    } else {
      physicalContent = new File(FileRepositoryManager.getAbsolutePath(document.getInstanceId()),
          document.getNodeName() + "/" +
              document.getMajorVersion() + "_" + document.getMinorVersion() + "/" + language
      );
    }
    if (expectedContent == null) {
      assertThat(document.getAttachment(), nullValue());
      assertThat(physicalContent.exists(), is(false));
    } else {
      assertThat(document.getAttachment(), notNullValue());
      assertThat(document.getLanguage(), is(language));
      assertThat(physicalContent.exists(), is(true));
      ByteArrayOutputStream content = new ByteArrayOutputStream();
      AttachmentServiceProvider.getAttachmentService()
          .getBinaryContent(content, document.getPk(), language);
      assertThat(content.toString(Charsets.UTF_8),
          is(expectedContent));
    }
    return document;
  }

  public SimpleDocument getDocumentById(String uuId, String language) throws Exception {
    try (JCRSession session = JCRSession.openSystemSession()) {
      SimpleDocument document =
          getDocumentRepository().findDocumentById(session, new SimpleDocumentPK(uuId), language);
      if (StringUtil.isDefined(language) && document != null &&
          !language.equals(document.getLanguage())) {
        document.setAttachment(null);
      }
      return document;
    }
  }

  protected SimpleDocument getDocumentById(String uuId) throws Exception {
    return getDocumentById(uuId, null);
  }

  @SuppressWarnings("SameParameterValue")
  protected SimpleDocument defaultDocument(String instanceId, String foreignId,
      SimpleAttachment file) {
    SimpleDocument document = new SimpleDocument();
    document.setPK(new SimpleDocumentPK("-1", instanceId));
    document.setForeignId(foreignId);
    document.setAttachment(file);
    return document;
  }

  public SimpleDocument defaultDocument(String instanceId, String foreignId) {
    return defaultDocument(instanceId, foreignId, null);
  }

  public SimpleAttachment defaultENContent() {
    return SimpleAttachment.builder("en")
        .setFilename("test.pdf")
        .setTitle("My test document")
        .setDescription("This is a test document")
        .setSize("This is a test document".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.OCTET_STREAM)
        .setCreationData("0", randomDate())
        .setFormId("18")
        .build();
  }

  public SimpleAttachment defaultFRContent() {
    return SimpleAttachment.builder("fr")
        .setFilename("test.odp")
        .setTitle("Mon document de test")
        .setDescription("Ceci est un document de test")
        .setSize("Ceci est un document de test".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.PLAIN_TEXT)
        .setCreationData("10", randomDate())
        .setFormId("5")
        .build();
  }

  @SuppressWarnings("unused")
  public SimpleAttachment defaultDEContent() {
    return SimpleAttachment.builder("de")
        .setFilename("test.docx")
        .setTitle("Mein Test-Dokument")
        .setDescription("Dies ist ein Testdokument")
        .setSize("Dies ist ein Testdokument".getBytes(Charsets.UTF_8).length)
        .setContentType(MimeTypes.XML)
        .setCreationData("10", randomDate())
        .setFormId("2")
        .build();
  }

  protected Date randomDate() {
    long offset = Timestamp.valueOf("2014-01-01 00:00:00").getTime();
    long end = Timestamp.valueOf("2014-04-30 00:00:00").getTime();
    long diff = end - offset + 1;
    return new Timestamp(offset + (long) (Math.random() * diff));
  }

  public SimpleDocument createAttachmentForTest(SimpleDocument document,
      SimpleAttachment attachment, String content) throws Exception {
    document.setAttachment(attachment);
    return createDocumentIntoJcr(document, content);
  }

  public SimpleDocument updateAttachmentForTest(SimpleDocument document, String language,
      String content) throws Exception {
    SimpleDocument documentToUpdate = new SimpleDocument(document);
    documentToUpdate.setLanguage(language);
    return updateDocumentIntoJcr(documentToUpdate, content);
  }

  @SuppressWarnings("unused")
  protected SimpleDocument updateAttachmentForTest(SimpleDocument document) throws Exception {
    SimpleDocument documentToUpdate = new SimpleDocument(document);
    return updateDocumentIntoJcr(documentToUpdate, null);
  }

  private SimpleDocument createDocumentIntoJcr(SimpleDocument document, String content)
      throws Exception {
    try (JCRSession session = JCRSession.openSystemSession()) {
      SimpleDocumentPK createdPk = getDocumentRepository().createDocument(session, document);
      session.save();
      long contentSizeWritten = getDocumentRepository().storeContent(document,
          new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)));
      assertThat(contentSizeWritten, is((long) content.length()));
      SimpleDocument createdDocument =
          getDocumentRepository().findDocumentById(session, createdPk, document.getLanguage());
      assertThat(createdDocument, notNullValue());
      assertThat(createdDocument.getOrder(), is(document.getOrder()));
      assertThat(createdDocument.getLanguage(), is(document.getLanguage()));
      return createdDocument;
    }
  }

  private SimpleDocument updateDocumentIntoJcr(SimpleDocument document, String content)
      throws Exception {
    assertThat(document.getPk(), notNullValue());
    assertThat(document.getId(), not(is(emptyString())));
    assertThat(document.getInstanceId(), not(emptyString()));
    assertThat(document.getOldSilverpeasId(), greaterThan(0L));
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
      converter.fillNode(document, documentNode);
      session.save();
      if (content != null) {
        long contentSizeWritten = getDocumentRepository().storeContent(document,
            new ByteArrayInputStream(content.getBytes(Charsets.UTF_8)));
        assertThat(contentSizeWritten, is((long) content.length()));
      }
      SimpleDocument updatedDocument = getDocumentRepository()
          .findDocumentById(session, document.getPk(), document.getLanguage());
      assertThat(updatedDocument, notNullValue());
      assertThat(updatedDocument.getLanguage(), is(document.getLanguage()));
      return updatedDocument;
    }
  }

  /**
   * Gets the single child node of a node.
   * If no node exists or several node exists, a failed assertion is performed.
   * @param node the parent node.
   * @return the single child node.
   * @throws Exception if an error occurs
   */
  public Node getSingleChildNode(Node node) throws Exception {
    assertThat("getTheOnlyOneChildNode - no child node exists", node.hasNodes(), is(true));
    NodeIterator singleChildNodeIt = node.getNodes();
    Node singleChildNode = singleChildNodeIt.nextNode();
    assertThat("getTheOnlyOneChildNode - several child nodes exists", singleChildNodeIt.hasNext(),
        is(false));
    return singleChildNode;
  }

  /**
   * Gets the {@link Binary} content of a JCR node property as a {@link String}.
   * @param property the JCR property that must contains a binary content.
   * @return the binary content as a {@link String}.
   * @throws Exception if an error occurs
   */
  public String getBinaryContentAsString(Property property) throws Exception {
    return new String(getBinaryContent(property), Charsets.UTF_8);
  }

  /**
   * Gets the {@link Binary} content of a JCR node property.
   * @param property the JCR property that must contains a binary content.
   * @return the binary content.
   * @throws Exception if an error occurs
   */
  protected byte[] getBinaryContent(Property property) throws Exception {
    ByteArrayOutputStream byteContent = new ByteArrayOutputStream();
    Binary content = property.getBinary();
    InputStream in = content.getStream();
    try {
      IOUtils.copy(in, byteContent);
    } finally {
      IOUtils.closeQuietly(in);
      content.dispose();
    }
    return byteContent.toByteArray();
  }

  /**
   * Sets the {@link Binary} content of a JCR node property.
   * @param property the JCR property that must contains a binary content.
   * @throws Exception if an error occurs
   */
  public void setBinaryContent(Property property, byte[] content) throws Exception {
    InputStream in = new ByteArrayInputStream(content);
    try {
      Binary attachmentBinary = property.getSession().getValueFactory().createBinary(in);
      property.setValue(attachmentBinary);
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  /**
   * Gets from the specified parent node the child node that satisfies the given jcrPath.
   * @param parentNode the parent node from which the JCR path is guessed.
   * @param jcrPath the JCR path to find.
   * @return the JCR node instance if it exists, null otherwise.
   */
  public Node getRelativeNode(Node parentNode, String jcrPath) {
    try {
      return parentNode.getNode(jcrPath);
    } catch (RepositoryException re) {
      return null;
    }
  }

  /**
   * Lists all entire paths that exists from the specified node (recursive treatment).
   * @param parentNode the parent node.
   * @return the list of entire paths, null if the specified node does not exist anymore.
   * @throws RepositoryException if an error occurs
   */
  public List<String> listPathsFrom(Node parentNode) throws RepositoryException {
    return listPathsFrom(parentNode, -1);
  }

  /**
   * Lists all entire paths that exists from the specified node (recursive treatment).
   * @param parentNode the parent node.
   * @param deep the maximum number sub-nodes to reach.
   * @return the list of entire paths, null if the specified node does not exist anymore.
   * @throws RepositoryException if an error occurs
   */
  @SuppressWarnings("SameParameterValue")
  protected List<String> listPathsFrom(Node parentNode, int deep) throws RepositoryException {
    return listPathsFrom(parentNode, deep, 0);
  }

  /**
   * Lists all entire paths that exists from the specified node (recursive treatment).
   * @param parentNode the parent node.
   * @param maximumDeep the maximum number of sub-nodes to reach.
   * @param currentDeep the current sub-node position from the initial parent.
   * @return the list of entire paths, null if the specified node does not exist anymore.
   * @throws RepositoryException if an error occurs
   */
  private List<String> listPathsFrom(Node parentNode, int maximumDeep, int currentDeep)
      throws RepositoryException {
    try {
      parentNode.getPath();
    } catch (RepositoryException re) {
      // Node does not exist anymore.
      return null;
    }
    if ((maximumDeep >= 0 && currentDeep >= maximumDeep)) {
      return Collections.emptyList();
    }
    NodeIterator nodeIt = parentNode.getNodes();
    List<String> paths = new ArrayList<>();
    Set<String> uuidNodePerformed = new HashSet<>();
    boolean willMaximumDeepBeReached = (maximumDeep >= 0 && (currentDeep + 1) >= maximumDeep);
    while (nodeIt.hasNext()) {
      Node node = nodeIt.nextNode();
      if (node.hasNodes()) {
        if (!willMaximumDeepBeReached) {
          paths.addAll(Objects.requireNonNull(listPathsFrom(node, maximumDeep, currentDeep + 1)));
        } else if (!uuidNodePerformed.contains(node.getIdentifier())) {
          uuidNodePerformed.add(node.getIdentifier());
          paths.add(node.getPath());
        }
      } else if (!uuidNodePerformed.contains(node.getIdentifier())) {
        uuidNodePerformed.add(node.getIdentifier());
        paths.add(node.getPath());
      }
    }
    return paths;
  }
}
