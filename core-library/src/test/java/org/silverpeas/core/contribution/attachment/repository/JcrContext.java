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
package org.silverpeas.core.contribution.attachment.repository;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.tika.mime.MimeTypes;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.persistence.jcr.JcrRepositoryProvider;
import org.silverpeas.core.persistence.jcr.JcrSession;
import org.silverpeas.core.persistence.jcr.SilverpeasJcrSchemaRegistering;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;

import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.interceptor.Interceptor.Priority.APPLICATION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;

/**
 * This class handle a JCR test.<br/>
 * The repository is created before and deleted after each test.<br/>
 * The registered physical files are also deleted.
 * @author Yohann Chastagnier
 */
public class JcrContext implements TestRule {

  private static final String TEST_REPOSITORY_LOCATION = "test-jcr_";
  public static final String REPOSITORY_IN_MEMORY_XML = "/test-repository-in-memory.xml";

  private JcrTestContext context = null;

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        final Path repositoryPath = Files.createTempDirectory(TEST_REPOSITORY_LOCATION);
        try (final InputStream configStream = JcrContext.class
            .getResourceAsStream(REPOSITORY_IN_MEMORY_XML);) {
          context = new JcrTestContext(configStream, repositoryPath);
          beforeEvaluate(context);
          base.evaluate();
        } finally {
          try {
            afterEvaluate(context);
          } finally {
            FileUtils.deleteQuietly(repositoryPath.toFile());
          }
        }
      }
    };
  }

  protected void beforeEvaluate(JcrTestContext context) throws Exception {
    clearFileSystem();
    StubbedJcrRepositoryProvider jcrRepositoryProvider =
        (StubbedJcrRepositoryProvider) ServiceProvider.getService(JcrRepositoryProvider.class);
    jcrRepositoryProvider.setRepository(context.getRepository());
    ServiceProvider.getService(SilverpeasJcrSchemaRegistering.class).init();
  }

  protected void afterEvaluate(JcrTestContext context) {
    try {
      if (context != null) {
        context.getRepository().shutdown();
      }
    } finally {
      clearFileSystem();
    }
  }

  private void clearFileSystem() {
    File file = new File(FileRepositoryManager.getAbsolutePath(""));
    FileUtils.deleteQuietly(file);
    File index = new File(IndexFileManager.getIndexUpLoadPath());
    FileUtils.deleteQuietly(index);
  }

  protected class JcrTestContext {
    private final InputStream configStream;
    private final Path repositoryLocation;
    private final RepositoryConfig config;
    private final JackrabbitRepository repository;

    public JcrTestContext(final InputStream configStream, final Path repositoryPath)
        throws Exception {
      this.configStream = configStream;
      repositoryLocation = repositoryPath.toAbsolutePath();
      config = RepositoryConfig.create(configStream, repositoryLocation.toString());
      repository = RepositoryImpl.create(config);
    }

    public InputStream getConfigStream() {
      return configStream;
    }

    public Path getRepositoryLocation() {
      return repositoryLocation;
    }

    public RepositoryConfig getConfig() {
      return config;
    }

    public JackrabbitRepository getRepository() {
      return repository;
    }
  }

  public JcrTestContext getContext() {
    return context;
  }

  /*
  REPOSITORY PROVIDER
   */

  @Singleton
  @Alternative
  @Priority(APPLICATION + 10)
  public static class StubbedJcrRepositoryProvider implements JcrRepositoryProvider{

    private Repository repository;

    @Produces
    @Override
    public Repository getRepository() {
      return repository;
    }

    public void setRepository(final Repository repository) {
      this.repository = repository;
    }
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
  public void assertDocumentExists(String uuId) throws Exception {
    SimpleDocument document = getDocumentById(uuId);
    assertThat(document, notNullValue());
  }

  /**
   * Common method to assert a document existence.
   * @param uuId the uuId to retrieve the document.
   */
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
      assertThat(content.toString(org.apache.commons.io.Charsets.UTF_8.name()),
          is(expectedContent));
    }
    return document;
  }

  public SimpleDocument getDocumentById(String uuId, String language) throws Exception {
    try (JcrSession session = openSystemSession()) {
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
    return new SimpleAttachment("test.pdf", "en", "My test document", "This is a test document",
        "This is a test document".getBytes(org.apache.commons.io.Charsets.UTF_8).length,
        MimeTypes.OCTET_STREAM, "0", randomDate(), "18");

  }

  public SimpleAttachment defaultFRContent() {
    return new SimpleAttachment("test.odp", "fr", "Mon document de test",
        "Ceci est un document de test",
        "Ceci est un document de test".getBytes(org.apache.commons.io.Charsets.UTF_8).length,
        MimeTypes.PLAIN_TEXT, "10", randomDate(), "5");
  }

  public SimpleAttachment defaultDEContent() {
    return new SimpleAttachment("test.docx", "de", "Mein Test-Dokument",
        "Dies ist ein Testdokument",
        "Dies ist ein Testdokument".getBytes(org.apache.commons.io.Charsets.UTF_8).length,
        MimeTypes.XML, "10", randomDate(), "2");
  }

  protected Date randomDate() {
    long offset = Timestamp.valueOf("2014-01-01 00:00:00").getTime();
    long end = Timestamp.valueOf("2014-04-30 00:00:00").getTime();
    long diff = end - offset + 1;
    return new Timestamp(offset + (long) (Math.random() * diff));
  }

  /**
   * Creates an Image Master Node into the JCR
   * @param document
   * @param attachment
   * @param content
   * @return
   * @throws Exception
   */
  public SimpleDocument createAttachmentForTest(SimpleDocument document,
      SimpleAttachment attachment, String content) throws Exception {
    document.setAttachment(attachment);
    return createDocumentIntoJcr(document, content);
  }

  /**
   * Creates an Image Master Node into the JCR
   * @param document
   * @param language
   * @param content
   * @return
   * @throws Exception
   */
  public SimpleDocument updateAttachmentForTest(SimpleDocument document, String language,
      String content) throws Exception {
    SimpleDocument documentToUpdate = new SimpleDocument(document);
    documentToUpdate.setLanguage(language);
    return updateDocumentIntoJcr(documentToUpdate, content);
  }

  /**
   * Creates an Image Master Node into the JCR
   * @param document
   * @return
   * @throws Exception
   */
  protected SimpleDocument updateAttachmentForTest(SimpleDocument document) throws Exception {
    SimpleDocument documentToUpdate = new SimpleDocument(document);
    return updateDocumentIntoJcr(documentToUpdate, null);
  }

  /**
   * Creates a master document NODE into the JCR.
   * @param document
   * @return
   * @throws Exception
   */
  private SimpleDocument createDocumentIntoJcr(SimpleDocument document, String content)
      throws Exception {
    try (JcrSession session = openSystemSession()) {
      SimpleDocumentPK createdPk = getDocumentRepository().createDocument(session, document);
      session.save();
      long contentSizeWritten = getDocumentRepository().storeContent(document,
          new ByteArrayInputStream(content.getBytes(org.apache.commons.io.Charsets.UTF_8)));
      assertThat(contentSizeWritten, is((long) content.length()));
      SimpleDocument createdDocument =
          getDocumentRepository().findDocumentById(session, createdPk, document.getLanguage());
      assertThat(createdDocument, notNullValue());
      assertThat(createdDocument.getOrder(), is(document.getOrder()));
      assertThat(createdDocument.getLanguage(), is(document.getLanguage()));
      return createdDocument;
    }
  }

  /**
   * Creates a master document NODE into the JCR.
   * @param document
   * @return
   * @throws Exception
   */
  private SimpleDocument updateDocumentIntoJcr(SimpleDocument document, String content)
      throws Exception {
    assertThat(document.getPk(), notNullValue());
    assertThat(document.getId(), not(isEmptyString()));
    assertThat(document.getInstanceId(), not(isEmptyString()));
    assertThat(document.getOldSilverpeasId(), greaterThan(0L));
    try (JcrSession session = openSystemSession()) {
      Node documentNode = session.getNodeByIdentifier(document.getPk().getId());
      converter.fillNode(document, documentNode);
      session.save();
      if (content != null) {
        long contentSizeWritten = getDocumentRepository().storeContent(document,
            new ByteArrayInputStream(content.getBytes(org.apache.commons.io.Charsets.UTF_8)));
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
   * If no node exists or sevreal node exists, a failed assertion is performed.
   * @param node the parent node.
   * @return the single child node.
   * @throws Exception
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
   * @throws Exception
   */
  public String getBinaryContentAsString(Property property) throws Exception {
    return new String(getBinaryContent(property), Charsets.UTF_8.name());
  }

  /**
   * Gets the {@link Binary} content of a JCR node property.
   * @param property the JCR property that must contains a binary content.
   * @return the binary content.
   * @throws Exception
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
   * @return the binary content.
   * @throws Exception
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
   * Lists all entire pathes that exists from the specified node (recursive treatment).
   * @param parentNode the parent node.
   * @return the list of entire pathes, null if the specified node does not exist anymore.
   * @throws RepositoryException
   */
  public List<String> listPathesFrom(Node parentNode) throws RepositoryException {
    return listPathesFrom(parentNode, -1);
  }

  /**
   * Lists all entire pathes that exists from the specified node (recursive treatment).
   * @param parentNode the parent node.
   * @param deep the maximum number sub-nodes to reach.
   * @return the list of entire pathes, null if the specified node does not exist anymore.
   * @throws RepositoryException
   */
  protected List<String> listPathesFrom(Node parentNode, int deep) throws RepositoryException {
    return listPathesFrom(parentNode, deep, 0);
  }

  /**
   * Lists all entire pathes that exists from the specified node (recursive treatment).
   * @param parentNode the parent node.
   * @param maximumDeep the maximum number of sub-nodes to reach.
   * @param currentDeep the current sub-node position from the initial parent.
   * @return the list of entire pathes, null if the specified node does not exist anymore.
   * @throws RepositoryException
   */
  private List<String> listPathesFrom(Node parentNode, int maximumDeep, int currentDeep)
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
    List<String> pathes = new ArrayList<String>();
    Set<String> uuidNodePerformed = new HashSet<String>();
    boolean willMaximumDeepBeReached = (maximumDeep >= 0 && (currentDeep + 1) >= maximumDeep);
    while (nodeIt.hasNext()) {
      Node node = nodeIt.nextNode();
      if (node.hasNodes()) {
        if (!willMaximumDeepBeReached) {
          pathes.addAll(listPathesFrom(node, maximumDeep, currentDeep + 1));
        } else if (!uuidNodePerformed.contains(node.getIdentifier())) {
          uuidNodePerformed.add(node.getIdentifier());
          pathes.add(node.getPath());
        }
      } else if (!uuidNodePerformed.contains(node.getIdentifier())) {
        uuidNodePerformed.add(node.getIdentifier());
        pathes.add(node.getPath());
      }
    }
    return pathes;
  }
}
