/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr;

import org.apache.jackrabbit.core.fs.local.FileUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.jcr.security.SecurityTest;
import org.silverpeas.core.jcr.util.SilverpeasProperty;
import org.silverpeas.core.test.extention.SystemProperty;
import org.silverpeas.core.test.extention.TestManagedBeans;
import org.silverpeas.test.TestUser;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;

import static javax.jcr.nodetype.NodeType.MIX_VERSIONABLE;
import static javax.jcr.nodetype.NodeType.NT_FOLDER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.silverpeas.core.jcr.JCRNodeHandlingTest.JCR_HOME;
import static org.silverpeas.core.jcr.JCRNodeHandlingTest.OAK_CONFIG;
import static org.silverpeas.core.jcr.JCRUtil.*;

/**
 * Unit test about the handling of nodes in the JCR tree. This unit test is about to explore the JCR
 * backend used behaves correctly when using the JCR API when handling the nodes in the JCR tree.
 * @author mmoquillon
 */
@SystemProperty(key = RepositorySettings.JCR_HOME, value = JCR_HOME)
@SystemProperty(key = RepositorySettings.JCR_CONF, value = OAK_CONFIG)
@TestManagedBeans({RepositoryProvider.class})
class JCRNodeHandlingTest extends SecurityTest {

  public static final String JCR_HOME = "/tmp/jcr";
  public static final String OAK_CONFIG = "classpath:/silverpeas-oak-segment.properties";

  static final User user = new TestUser.Builder()
      .setFirstName("Bart")
      .setLastName("Simpson")
      .setId("42")
      .setDomainId("0")
      .build();

  private static String expectedNodeId;

  @BeforeAll
  public static void prepareFileStorage() throws IOException {
    Path jcrHome = Path.of(JCR_HOME);
    if (!Files.exists(jcrHome)) {
      Files.createDirectories(jcrHome);
    }
  }

  @AfterAll
  public static void purgeFileStorage() throws IOException {
    Path jcrHome = Path.of(JCR_HOME);
    if (Files.exists(jcrHome)) {
      FileUtil.delete(jcrHome.toFile());
    }
  }

  @BeforeEach
  public void initSilverpeasJCRSchema() throws Exception {
    SilverpeasJCRSchemaRegister register = new SilverpeasJCRSchemaRegister();
    register.init();
  }

  @BeforeEach
  public void createANode() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final String instanceId = "kmelia42";
      Node root = session.getRootNode();
      if (!root.hasNode(instanceId)) {
        Node document = root.addNode(instanceId, NT_FOLDER)
            .addNode("attachments", NT_FOLDER)
            .addNode("simpledoc_1", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
        NodeDocProperties docProps = new NodeDocProperties(instanceId, user)
            .setDisplayable(true)
            .setDownloadable(true)
            .setDate(new Date())
            .setVersionable(true)
            .setForeignKey("12")
            .setOldId("666");
        Node expected = fillDocumentNode(session, docProps, document);
        assertThat(expected, notNullValue());
        expectedNodeId = expected.getIdentifier();

        Node file = document.addNode("file_en", SilverpeasProperty.SLV_SIMPLE_ATTACHMENT);
        NodeFileProperties fileProps =
            new NodeFileProperties("SmalltalkForever.pdf", docProps.getDate(), user)
                .setLastModificationDate(docProps.getDate())
                .setLastModifier(user)
                .setFormId("-1")
                .setTitle("Smalltalk Forever")
                .setDescription("All about this wonderful language")
                .setMimeType("application/pdf")
                .setLanguage("en")
                .setSize(378883L);
        Node expectedFile = fillFileNode(session, fileProps, file);
        assertThat(expectedFile, notNullValue());

        session.save();
      }
    }
  }

  @Test
  @DisplayName("Get an existing node in the JCR")
  void getAnExistingNode() {
    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        Node actual = session.getNodeByIdentifier(expectedNodeId);
        assertThat(actual, notNullValue());
        assertThat(actual.getName(), is("simpledoc_1"));
        assertThat(actual.getParent().getName(), is("attachments"));
        assertThat(actual.getPath(), is("/kmelia42/attachments/simpledoc_1"));
        assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_OWNER).getString(), is(user.getId()));
        assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_INSTANCEID).getString(), is("kmelia42"));
        assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_FOREIGN_KEY).getString(), is("12"));
        assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_OLD_ID).getString(), is("666"));
        assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_DISPLAYABLE_AS_CONTENT).getBoolean(), is(true));
        assertThat(Arrays.stream(actual.getMixinNodeTypes())
            .anyMatch(m -> m.isNodeType(SilverpeasProperty.SLV_DOWNLOADABLE_MIXIN)), is(true));
        assertThat(isVersioned(actual), is(true));
      }
    });
  }

  @Test
  @DisplayName("Create a path of nodes and then find one of them")
  void createAPathOfNodes() {
    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        final String instanceId = "kmelia42";
        Node attachments = session.getNode("/kmelia42/attachments");
        Node document = attachments.addNode("simpledoc_2", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
        NodeDocProperties properties = new NodeDocProperties(instanceId, user)
            .setVersionable(false);
        Node expected = fillDocumentNode(session, properties, document);
        session.save();

        Node actual = session.getNodeByIdentifier(expected.getIdentifier());
        assertThat(actual, notNullValue());
        assertDocumentNodeEquals(expected, actual);
      }
    });
  }

  @Test
  @DisplayName("Create a path of nodes with one of them versioned and then find the versioned one")
  void createAPathOfNodesWithAVersionedOne() {
    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        final String instanceId = "kmelia42";
        Node attachments = session.getNode("/kmelia42/attachments");
        Node document = attachments.addNode("simpledoc_3", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
        NodeDocProperties properties = new NodeDocProperties(instanceId, user)
            .setVersionable(true);
        Node expected = fillDocumentNode(session, properties, document);
        session.save();

        // check saved versioned node
        Node actual = session.getNodeByIdentifier(expected.getIdentifier());
        assertThat(actual, notNullValue());
        assertThat(isVersioned(actual), is(true));
        assertDocumentNodeEquals(expected, actual);

        // check versions history of the versioned node
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        VersionHistory history = versionManager.getVersionHistory(actual.getPath());
        assertThat(history, Matchers.notNullValue());

        Version rootVersion = history.getRootVersion();
        assertThat(rootVersion, Matchers.notNullValue());
        assertThat(rootVersion.getIdentifier(), Matchers.notNullValue());

        Version baseVersion = versionManager.getBaseVersion(actual.getPath());
        assertThat(baseVersion, Matchers.notNullValue());
        assertThat(baseVersion.getIdentifier(), Matchers.notNullValue());

        assertThat(rootVersion.getIdentifier(), Matchers.is(baseVersion.getIdentifier()));

        VersionIterator versionIterator = history.getAllVersions();
        // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
        // be sure of his return value
        if (versionIterator.getSize() == -1L) {
          assertThat(versionIterator.hasNext(), Matchers.is(true));
          Version version = versionIterator.nextVersion();
          assertThat(version, Matchers.notNullValue());
          assertThat(version.getIdentifier(), Matchers.is(rootVersion.getIdentifier()));
          assertThat(versionIterator.hasNext(), Matchers.is(false));
        } else {
          assertThat(versionIterator.getSize(), Matchers.is(1L));
        }
      }
    });
  }

  @Test
  @DisplayName("Get the history of versions of a versioned existing node")
  void getHistoryOfVersions() {
    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        Node actual = session.getNodeByIdentifier(expectedNodeId);
        assertThat(actual, notNullValue());
        assertThat(isVersioned(actual), is(true));

        // check versions history of the expected node
        VersionManager versionManager = session.getWorkspace().getVersionManager();
        VersionHistory history = versionManager.getVersionHistory(actual.getPath());
        assertThat(history, Matchers.notNullValue());

        Version rootVersion = history.getRootVersion();
        assertThat(rootVersion, Matchers.notNullValue());
        assertThat(rootVersion.getIdentifier(), Matchers.notNullValue());

        Version baseVersion = versionManager.getBaseVersion(actual.getPath());
        assertThat(baseVersion, Matchers.notNullValue());
        assertThat(baseVersion.getIdentifier(), Matchers.notNullValue());

        assertThat(rootVersion.getIdentifier(), Matchers.is(baseVersion.getIdentifier()));

        VersionIterator versionIterator = history.getAllVersions();
        // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
        // be sure of his return value
        if (versionIterator.getSize() == -1L) {
          assertThat(versionIterator.hasNext(), Matchers.is(true));
          Version version = versionIterator.nextVersion();
          assertThat(version, Matchers.notNullValue());
          assertThat(version.getIdentifier(), Matchers.is(rootVersion.getIdentifier()));
          assertThat(versionIterator.hasNext(), Matchers.is(false));
        } else {
          assertThat(versionIterator.getSize(), Matchers.is(1L));
        }
      }
    });
  }

  private boolean isVersioned(final Node node) {
    try {
      return node.getProperty(SilverpeasProperty.SLV_PROPERTY_VERSIONED).getBoolean() &&
          !node.hasProperty(Property.JCR_FROZEN_PRIMARY_TYPE) &&
          Arrays.stream(node.getMixinNodeTypes())
              .anyMatch(m -> m.isNodeType(MIX_VERSIONABLE));
    } catch (RepositoryException e) {
      return false;
    }
  }
}
