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

package org.silverpeas.core.jcr.impl.oak;

import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import org.apache.jackrabbit.value.BinaryImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.jcr.JCRSession;
import org.silverpeas.core.jcr.RepositoryProvider;
import org.silverpeas.core.jcr.impl.RepositorySettings;
import org.silverpeas.core.jcr.impl.ResourcesCloser;
import org.silverpeas.core.jcr.security.SecurityTest;
import org.silverpeas.kernel.test.annotations.SystemProperty;
import org.silverpeas.kernel.test.annotations.TestManagedBeans;
import org.silverpeas.test.TestUser;

import javax.jcr.Node;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;
import java.io.InputStream;
import java.util.Arrays;

import static de.flapdoodle.embed.mongo.distribution.Version.Main.V6_0;
import static de.flapdoodle.net.Net.localhostIsIPv6;
import static javax.jcr.Property.*;
import static javax.jcr.nodetype.NodeType.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.silverpeas.core.jcr.impl.oak.DocumentStoreTest.JCR_HOME;
import static org.silverpeas.core.jcr.impl.oak.DocumentStoreTest.OAK_CONFIG;

/**
 * Unit test on the initialization of the JCR repository backed by a document storage.
 * @author mmoquillon
 */
@SystemProperty(key = RepositorySettings.JCR_HOME, value = JCR_HOME)
@SystemProperty(key = RepositorySettings.JCR_CONF, value = OAK_CONFIG)
@TestManagedBeans({ResourcesCloser.class, RepositoryProvider.class})
public class DocumentStoreTest extends SecurityTest {

  public static final String JCR_HOME = "/tmp/jcr";
  public static final String OAK_CONFIG = "classpath:/silverpeas-oak-document.properties";

  private static TransitionWalker.ReachedState<RunningMongodProcess> mongo;

  final User user = new TestUser.Builder()
      .setFirstName("Bart")
      .setLastName("Simpson")
      .setId("42")
      .setDomainId("0")
      .build();

  @BeforeAll
  public static void startMongoDB() {
    assertDoesNotThrow(() -> {
      Net localhost = Net.builder()
          .bindIp("localhost")
          .port(27017)
          .isIpv6(localhostIsIPv6())
          .build();
      mongo = Mongod.instance()
          .withNet(Start.to(Net.class).providedBy(() -> localhost))
          .start(V6_0);
      assertThat(mongo.current(), notNullValue());
      assertThat(mongo.current().isAlive(), is(true));
    });
  }

  @AfterAll
  public static void stopMongoDB() {
    mongo.close();
  }

  @Test
  @DisplayName("Create a node into the JCR backed by a document storage")
  void createANode() {
    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        InputStream data = getClass().getResourceAsStream("/silverpeas-oak-segment.properties");
        assertThat(data, notNullValue());

        Node root = session.getRootNode();
        Node expected = root.addNode("GED_1", NT_FOLDER)
            .addNode("files", NT_FOLDER)
            .addNode("myfile_1", NT_FILE)
            .addNode(JCR_CONTENT, NT_RESOURCE);
        expected.setProperty(JCR_MIMETYPE, "plain/text");
        expected.setProperty(JCR_LAST_MODIFIED_BY, user.getId());
        expected.setProperty(JCR_ENCODING, "ISO-8859-1");
        expected.setProperty(JCR_DATA, new BinaryImpl(data));
        session.save();

        Node actual = session.getNodeByIdentifier(expected.getIdentifier());
        assertThat(actual, notNullValue());
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getPath(), is(expected.getPath()));
        assertThat(actual.getProperty(JCR_LAST_MODIFIED_BY).getString(),
            is(user.getId()));
        assertThat(actual.getProperty(JCR_MIMETYPE).getString(),
            is("plain/text"));
        assertThat(actual.getProperty(JCR_ENCODING).getString(),
            is("ISO-8859-1"));
      }
    });
  }

  @Test
  @DisplayName("Create a versioned node into the JCR backed by a document storage")
  void createAVersionedNode() {
    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        InputStream data = getClass().getResourceAsStream("/silverpeas-oak-segment.properties");
        assertThat(data, notNullValue());

        Node root = session.getRootNode();
        Node file = root.addNode("GED_2", NT_FOLDER)
            .addNode("files", NT_FOLDER)
            .addNode("myfile_1", NT_FILE);
        file.addMixin(MIX_VERSIONABLE);

        Node expected = file.addNode(JCR_CONTENT, NT_RESOURCE);
        expected.setProperty(JCR_MIMETYPE, "plain/text");
        expected.setProperty(JCR_LAST_MODIFIED_BY, user.getId());
        expected.setProperty(JCR_ENCODING, "ISO-8859-1");
        expected.setProperty(JCR_DATA, new BinaryImpl(data));

        session.save();

        Node actual = session.getNodeByIdentifier(expected.getIdentifier());
        assertThat(actual, notNullValue());
        assertThat(actual.getName(), is(expected.getName()));
        assertThat(actual.getPath(), is(expected.getPath()));
        assertThat(actual.getProperty(JCR_LAST_MODIFIED_BY).getString(),
            is(user.getId()));
        assertThat(actual.getProperty(JCR_MIMETYPE).getString(),
            is("plain/text"));
        assertThat(actual.getProperty(JCR_ENCODING).getString(),
            is("ISO-8859-1"));
        assertThat(Arrays.stream(actual.getParent().getMixinNodeTypes())
            .anyMatch(m -> m.isNodeType(MIX_VERSIONABLE)), is(true));

        VersionManager versionManager = session.getWorkspace().getVersionManager();
        VersionHistory history = versionManager.getVersionHistory(actual.getParent().getPath());
        assertThat(history, notNullValue());

        Version rootVersion = history.getRootVersion();
        assertThat(rootVersion, notNullValue());
        assertThat(rootVersion.getIdentifier(), notNullValue());

        Version baseVersion = versionManager.getBaseVersion(actual.getParent().getPath());
        assertThat(baseVersion, notNullValue());
        assertThat(baseVersion.getIdentifier(), notNullValue());

        assertThat(rootVersion.getIdentifier(), is(baseVersion.getIdentifier()));

        VersionIterator versionIterator = history.getAllVersions();
        // because versionIterator#getSize() depends on the implementation of the JCR, we cannot
        // be sure of his return value
        if (versionIterator.getSize() == -1L) {
          assertThat(versionIterator.hasNext(), is(true));
          assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));
          assertThat(versionIterator.hasNext(), is(false));
        } else {
          assertThat(versionIterator.getSize(), is(1L));
          assertThat(versionIterator.nextVersion().isSame(rootVersion), is(true));
        }
      }
    });
  }
}
