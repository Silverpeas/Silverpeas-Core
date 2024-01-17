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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.jcr.impl.ResourcesCloser;
import org.silverpeas.core.jcr.security.SecurityTest;
import org.silverpeas.core.jcr.util.SilverpeasJCRSchemaRegister;
import org.silverpeas.core.jcr.util.SilverpeasProperty;
import org.silverpeas.core.test.unit.extention.SystemProperty;
import org.silverpeas.core.test.unit.extention.TestManagedBeans;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.test.TestUser;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.core.jcr.JCRNodeHandlingTest.JCR_HOME;
import static org.silverpeas.core.jcr.JCRNodeHandlingTest.OAK_CONFIG;
import static org.silverpeas.core.jcr.JCRUtil.*;
import static org.silverpeas.core.jcr.util.SilverpeasProperty.*;

/**
 * Unit test about the handling of nodes in the JCR tree. This unit test is about to explore the JCR
 * backend used behaves correctly when using the JCR API when handling the nodes in the JCR tree.
 * @author mmoquillon
 */
@SystemProperty(key = RepositorySettings.JCR_HOME, value = JCR_HOME)
@SystemProperty(key = RepositorySettings.JCR_CONF, value = OAK_CONFIG)
@TestManagedBeans({ResourcesCloser.class, RepositoryProvider.class})
class JCRNodeHandlingTest extends SecurityTest {

  public static final String JCR_HOME = "/tmp/jcr";
  public static final String OAK_CONFIG = "classpath:/silverpeas-oak-segment.properties";

  static final User user = new TestUser.Builder().setFirstName("Bart")
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
    register.register();
  }

  @BeforeEach
  public void createANode() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final String instanceId = "kmelia42";
      final String type = "attachments";
      final String docId = "simpledoc_1";
      final String basePath = Path.of(instanceId, type).toString();
      Node root = session.getRootNode();
      Node base = !root.hasNode(basePath) ?
          root.addNode(instanceId, NT_FOLDER).addNode(type, NT_FOLDER) :
          root.getNode(basePath);
      if (!base.hasNode(docId)) {
        Node document = base.addNode(docId, SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
        NodeDocProperties docProps = new NodeDocProperties(instanceId, user).setDisplayable(true)
            .setDownloadable(true)
            .setDate(new Date())
            .setVersionable(true)
            .setForeignKey("12")
            .setOldId("666");
        Node expected = fillDocumentNode(session, docProps, document);
        assertThat(expected, notNullValue());
        expectedNodeId = expected.getIdentifier();

        Node file = document.addNode("file_en", SilverpeasProperty.SLV_SIMPLE_ATTACHMENT);
        NodeFileProperties fileProps = new NodeFileProperties("SmalltalkForever.pdf",
            docProps.getDate(), user).setLastModificationDate(docProps.getDate())
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
  void getAnExistingNode() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      Mutable<Node> mutableNode = Mutable.empty();
      assertDoesNotThrow(() -> mutableNode.set(session.getNodeByIdentifier(expectedNodeId)));
      assertThat(mutableNode.isPresent(), is(true));

      Node actual = mutableNode.get();
      assertThat(actual.getName(), is("simpledoc_1"));
      assertThat(actual.getParent().getName(), is("attachments"));
      assertThat(actual.getPath(), is("/kmelia42/attachments/simpledoc_1"));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_OWNER).getString(),
          is(user.getId()));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_INSTANCEID).getString(),
          is("kmelia42"));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_FOREIGN_KEY).getString(),
          is("12"));
      assertThat(actual.getProperty(SilverpeasProperty.SLV_PROPERTY_OLD_ID).getString(), is("666"));
      assertThat(
          actual.getProperty(SilverpeasProperty.SLV_PROPERTY_DISPLAYABLE_AS_CONTENT).getBoolean(),
          is(true));
      assertThat(Arrays.stream(actual.getMixinNodeTypes())
          .anyMatch(m -> m.isNodeType(SilverpeasProperty.SLV_DOWNLOADABLE_MIXIN)), is(true));
      assertThat(isVersioned(actual), is(true));

    }
  }

  @Test
  @DisplayName("Remove an existing node in the JCR")
  void removeAnExistingNode() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node actual = session.getNodeByIdentifier(expectedNodeId);
      actual.remove();
      session.save();

      assertThrows(ItemNotFoundException.class, () -> session.getNodeByIdentifier(expectedNodeId));
    }
  }

  @Test
  @DisplayName("Create a path of nodes and then find one of them")
  void createAPathOfNodes() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final String instanceId = "kmelia42";
      Node attachments = session.getNode("/kmelia42/attachments");
      Node document = attachments.addNode("simpledoc_2", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
      NodeDocProperties properties = new NodeDocProperties(instanceId, user).setVersionable(false);
      Node expected = fillDocumentNode(session, properties, document);
      session.save();

      Node actual = session.getNodeByIdentifier(expected.getIdentifier());
      assertDocumentNodeEquals(expected, actual);
    }
  }

  @Test
  @DisplayName("Create a path of nodes with one of them versioned and then find the versioned one")
  void createAPathOfNodesWithAVersionedOne() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final String instanceId = "kmelia42";
      Node attachments = session.getNode("/kmelia42/attachments");
      Node document = attachments.addNode("simpledoc_3", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
      NodeDocProperties properties = new NodeDocProperties(instanceId, user).setVersionable(true);
      Node expected = fillDocumentNode(session, properties, document);
      session.save();

      // check saved versioned node
      Node actual = session.getNodeByIdentifier(expected.getIdentifier());
      assertThat(isVersioned(actual), is(true));
      assertDocumentNodeEquals(expected, actual);

      // check versions history of the versioned node
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      // the node is well versioned (it has a version history)
      assertDoesNotThrow(() -> versionManager.getVersionHistory(actual.getPath()));
      VersionHistory history = versionManager.getVersionHistory(actual.getPath());

      Version rootVersion = history.getRootVersion();
      assertThat(rootVersion.getIdentifier(), is(notNullValue()));

      Version baseVersion = versionManager.getBaseVersion(actual.getPath());
      assertThat(baseVersion.getIdentifier(), is(notNullValue()));

      assertThat(rootVersion.isSame(baseVersion), is(true));

      VersionIterator versionIterator = history.getAllVersions();
      // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
      // be sure of his return value
      if (versionIterator.getSize() == -1L) {
        assertThat(versionIterator.hasNext(), is(true));
        Version version = versionIterator.nextVersion();
        assertThat(version.isSame(rootVersion), is(true));
        assertThat(versionIterator.hasNext(), is(false));
      } else {
        assertThat(versionIterator.getSize(), is(1L));
        assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));
      }
    }
  }

  @Test
  @DisplayName("Get the history of versions of an existing versioned node")
  void getHistoryOfVersions() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      Node actual = session.getNodeByIdentifier(expectedNodeId);
      assertThat(isVersioned(actual), is(true));

      // check versions history of the expected node
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      // the node is versioned: it should have a version history
      assertDoesNotThrow(() -> versionManager.getVersionHistory(actual.getPath()));
      VersionHistory history = versionManager.getVersionHistory(actual.getPath());

      Version rootVersion = history.getRootVersion();
      assertThat(rootVersion.getIdentifier(), is(notNullValue()));

      Version baseVersion = versionManager.getBaseVersion(actual.getPath());
      assertThat(baseVersion.getIdentifier(), is(notNullValue()));

      // the root version is the base one (id est the latest one)
      assertThat(rootVersion.isSame(baseVersion), is(true));

      VersionIterator versionIterator = history.getAllVersions();
      // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
      // be sure of his return value
      if (versionIterator.getSize() == -1L) {
        assertThat(versionIterator.hasNext(), is(true));
        Version version = versionIterator.nextVersion();
        assertThat(version.isSame(rootVersion), is(true));
        assertThat(versionIterator.hasNext(), is(false));
      } else {
        assertThat(versionIterator.getSize(), is(1L));
        assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));
      }
    }
  }

  @Test
  @DisplayName("Add a new version to an existing versioned node")
  void addANewVersion() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      addANewVersionTo(session, expectedNodeId, 1);

      Node actual = session.getNodeByIdentifier(expectedNodeId);
      assertThat(isVersioned(actual), is(true));

      VersionManager versionManager = session.getWorkspace().getVersionManager();
      VersionHistory history = versionManager.getVersionHistory(actual.getPath());

      Version rootVersion = history.getRootVersion();
      assertThat(rootVersion.getIdentifier(), is(notNullValue()));

      Version baseVersion = versionManager.getBaseVersion(actual.getPath());
      assertThat(baseVersion.getIdentifier(), is(notNullValue()));

      // two versions: the root one and the base one (id est the latest one) aren't the same
      assertThat(rootVersion.isSame(baseVersion), is(false));

      VersionIterator versionIterator = history.getAllLinearVersions();
      // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
      // be sure of his return value
      if (versionIterator.getSize() == -1L) {
        // the first version should be the root one
        assertThat(versionIterator.hasNext(), is(true));
        assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));

        // the last version should be the base one
        assertThat(versionIterator.hasNext(), is(true));
        assertThat(versionIterator.nextVersion().isSame(baseVersion), is(true));

        // no more versions
        assertThat(versionIterator.hasNext(), is(false));
      } else {
        // the first version should be the root one and the last the base one
        assertThat(versionIterator.getSize(), is(2L));
        assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));
        assertThat(versionIterator.nextVersion().isSame(baseVersion), is(true));
      }
    }
  }

  @Test
  @DisplayName("Remove all versions from an existing versioned node")
  void removeAllVersions() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      addANewVersionTo(session, expectedNodeId, 3);

      Node actual = session.getNodeByIdentifier(expectedNodeId);
      assertThat(isVersioned(actual), is(true));

      VersionManager versionManager = session.getWorkspace().getVersionManager();
      VersionHistory history = versionManager.getVersionHistory(actual.getPath());
      Version rootVersion = history.getRootVersion();
      Version baseVersion = versionManager.getBaseVersion(actual.getPath());

      // several versions: the root one and the base one (id est the latest one) aren't the same
      assertThat(rootVersion.isSame(baseVersion), is(false));

      VersionIterator versionIterator = history.getAllLinearVersions();
      while (versionIterator.hasNext()) {
        Version version = versionIterator.nextVersion();

        // we cannot delete the root version
        if (!version.getIdentifier().equals(rootVersion.getIdentifier()) &&
            !version.getIdentifier().equals(baseVersion.getIdentifier())) {
          history.removeVersion(version.getName());
        }
      }

      // check there is only two versions, the root and the base ones
      versionIterator = history.getAllLinearVersions();
      assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));
      assertThat(versionIterator.nextVersion().isSame(baseVersion), is(true));
      assertThat(versionIterator.hasNext(), is(false));
    }
  }

  @Test
  @DisplayName("Remove an existing versioned node with its versions history")
  void removeAVersionedNode() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      addANewVersionTo(session, expectedNodeId, 3);

      Node actual = session.getNodeByIdentifier(expectedNodeId);
      assertThat(isVersioned(actual), is(true));

      String actualPath = actual.getPath();
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      VersionHistory history = versionManager.getVersionHistory(actualPath);
      Version baseVersion = versionManager.getBaseVersion(actualPath);

      actual.remove();
      session.save();

      // several versions: the root one and the base one (id est the latest one) aren't the same
      Version rootVersion = history.getRootVersion();
      assertThat(rootVersion.isSame(baseVersion), is(false));

      // to get all the versions of a removed node: we have to use VersionHistory#getAllVersions().
      // VersionHistory#getAllLinearVersions returns an iterator on an empty collection
      VersionIterator emptyIterator = history.getAllLinearVersions();
      assertThat(emptyIterator.hasNext(), is(false));
      VersionIterator versionIterator = history.getAllVersions();
      assertThat(versionIterator.hasNext(), is(true));
      while (versionIterator.hasNext()) {
        Version version = versionIterator.nextVersion();

        // we cannot delete the root version
        if (!version.getIdentifier().equals(rootVersion.getIdentifier())) {
          history.removeVersion(version.getName());
        }
      }

      session.save();

      // check the node isn't anymore in the JCR and as such it doesn't have anymore versions
      // history
      assertThrows(ItemNotFoundException.class, () -> session.getNodeByIdentifier(expectedNodeId));
      assertThrows(RepositoryException.class, () -> versionManager.getVersionHistory(actualPath));
    }
  }

  @Test
  @DisplayName("Remove the versionable property of an existing versioned node")
  void unsetVersionableMixin() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      addANewVersionTo(session, expectedNodeId, 3);

      Node actual = session.getNodeByIdentifier(expectedNodeId);
      assertThat(isVersioned(actual), is(true));

      String actualPath = actual.getPath();
      VersionManager versionManager = session.getWorkspace().getVersionManager();
      VersionHistory history = versionManager.getVersionHistory(actualPath);
      Version baseVersion = versionManager.getBaseVersion(actualPath);

      versionManager.checkout(actualPath);
      actual.getProperty(SLV_PROPERTY_OWNER).remove();

      actual.removeMixin(MIX_VERSIONABLE);
      actual.setProperty(SLV_PROPERTY_VERSIONED, false);
      actual.setProperty(SLV_PROPERTY_MAJOR, 0);
      actual.setProperty(SLV_PROPERTY_MINOR, 0);

      session.save();

      Version rootVersion = history.getRootVersion();
      // several versions: the root one and the base one (id est the latest one) aren't the same
      assertThat(rootVersion.isSame(baseVersion), is(false));

      VersionIterator versionIterator = history.getAllVersions();
      while (versionIterator.hasNext()) {
        Version version = versionIterator.nextVersion();

        // we cannot delete the root version but the base one yes as nothing references it (the
        // node isn't anymore versioned and as such doesn't refer its possible latest version)
        if (!version.isSame(rootVersion)) {
          history.removeVersion(version.getName());
        }
      }

      session.save();

      // as the node isn't anymore versioned, it is keeps by default checked out and it doesn't
      // require to be checked in (required to create a new version; id est snapshot of the node
      // state)
      assertThat(actual.isCheckedOut(), is(true));
      assertThrows(UnsupportedRepositoryOperationException.class,
          () -> versionManager.checkin(actualPath));

      // check the node exists and it doesn't have any versions history
      assertDoesNotThrow(() -> session.getNodeByIdentifier(expectedNodeId));
      assertThrows(UnsupportedRepositoryOperationException.class,
          () -> versionManager.getVersionHistory(actualPath));
    }
  }

  @Test
  @DisplayName("Sets the versionable property to an existing non versioned node")
  void setVersionableMixin() throws RepositoryException {
    try (JCRSession session = JCRSession.openSystemSession()) {
      final String instanceId = "kmelia42";
      Node attachments = session.getNode("/kmelia42/attachments");
      Node document = attachments.addNode("simpledoc_4", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
      NodeDocProperties properties = new NodeDocProperties(instanceId, user).setVersionable(false);
      Node expected = fillDocumentNode(session, properties, document);
      session.save();

      Node actual = session.getNodeByIdentifier(expected.getIdentifier());
      assertThat(isVersioned(actual), is(false));

      String actualPath = actual.getPath();
      VersionManager versionManager = session.getWorkspace().getVersionManager();

      // node not versioned, hence it is by default checked out
      assertThat(actual.isCheckedOut(), is(true));

      actual.addMixin(MIX_VERSIONABLE);
      actual.setProperty(SLV_PROPERTY_VERSIONED, true);
      actual.setProperty(SLV_PROPERTY_MAJOR, 1);
      actual.setProperty(SLV_PROPERTY_MINOR, 0);

      session.save();
      versionManager.checkin(actualPath);

      VersionHistory history = versionManager.getVersionHistory(actualPath);
      Version rootVersion = history.getRootVersion();
      Version baseVersion = versionManager.getBaseVersion(actualPath);

      // by setting versionable the existing node, the previous state of the node becomes the root
      // version and the new state (with the versionable mixin) is then the base one.
      assertThat(rootVersion.getIdentifier(), is(not(baseVersion.getIdentifier())));

      VersionIterator versionIterator = history.getAllLinearVersions();
      // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
      // be sure of his return value
      if (versionIterator.getSize() == -1L) {
        // the first version should be the root one
        assertThat(versionIterator.hasNext(), is(true));
        assertThat(versionIterator.nextVersion().getIdentifier(), is(rootVersion.getIdentifier()));

        // the last version should be the base one
        assertThat(versionIterator.hasNext(), is(true));
        assertThat(versionIterator.nextVersion().getIdentifier(), is(baseVersion.getIdentifier()));

        // no more versions
        assertThat(versionIterator.hasNext(), is(false));
      } else {
        // the first version should be the root one and the last the base one
        assertThat(versionIterator.getSize(), is(2L));
        assertThat(versionIterator.nextVersion().getIdentifier(), is(rootVersion.getIdentifier()));
        assertThat(versionIterator.nextVersion().getIdentifier(), is(baseVersion.getIdentifier()));
      }
    }
  }

  private boolean isVersioned(final Node node) {
    try {
      return node.getProperty(SilverpeasProperty.SLV_PROPERTY_VERSIONED).getBoolean() &&
          !node.hasProperty(Property.JCR_FROZEN_PRIMARY_TYPE) &&
          Arrays.stream(node.getMixinNodeTypes()).anyMatch(m -> m.isNodeType(MIX_VERSIONABLE));
    } catch (RepositoryException e) {
      return false;
    }
  }

  private void addANewVersionTo(final Session session, final String nodeId, int versionsCount)
      throws RepositoryException {
    Node actual = session.getNodeByIdentifier(nodeId);
    assertThat(actual, is(notNullValue()));
    assertThat(isVersioned(actual), is(true));

    String actualPath = actual.getPath();
    VersionManager versionManager = session.getWorkspace().getVersionManager();
    for (int i = 0; i < versionsCount; i++) {
      versionManager.checkout(actualPath);
      versionManager.checkin(actualPath);
    }

    session.save();
  }
}
