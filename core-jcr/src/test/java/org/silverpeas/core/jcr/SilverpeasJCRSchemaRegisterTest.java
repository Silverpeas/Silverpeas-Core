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
import org.silverpeas.core.test.unit.extention.TestedBean;
import org.silverpeas.test.TestUser;

import javax.jcr.Node;
import java.util.Date;

import static javax.jcr.nodetype.NodeType.NT_FOLDER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.silverpeas.core.jcr.RepositoryProviderTest.JCR_HOME;
import static org.silverpeas.core.jcr.RepositoryProviderTest.OAK_CONFIG;

/**
 * Test the registering of the JCR schema for Silverpeas use.
 * @author mmoquillon
 */
@SystemProperty(key = RepositorySettings.JCR_HOME, value = JCR_HOME)
@SystemProperty(key = RepositorySettings.JCR_CONF, value = OAK_CONFIG)
@TestManagedBeans({ResourcesCloser.class, RepositoryProvider.class})
class SilverpeasJCRSchemaRegisterTest extends SecurityTest {

  @TestedBean
  SilverpeasJCRSchemaRegister schemaRegister;

  final User user = new TestUser.Builder()
      .setFirstName("Bart")
      .setLastName("Simpson")
      .setId("42")
      .setDomainId("0")
      .build();

  @Test
  @DisplayName("Loading the Silverpeas JCR Schema into the JCR should succeed")
  void loadTheSchemaIntoJCR() {
    assertDoesNotThrow(() -> schemaRegister.register());
  }

  @Test
  void createANodeAccordingToTheSchema() throws Exception {
    schemaRegister.register();
    String instanceId = "kmelia42";

    assertDoesNotThrow(() -> {
      try (JCRSession session = JCRSession.openSystemSession()) {
        Node root = session.getRootNode();
        Node componentInstance = root.addNode(instanceId, NT_FOLDER);
        Node nodeType = componentInstance.addNode("attachments", NT_FOLDER);

        Node simpleDoc = nodeType.addNode("simpledoc_42", SilverpeasProperty.SLV_SIMPLE_DOCUMENT);
        JCRUtil.NodeDocProperties docProps = new JCRUtil.NodeDocProperties(instanceId, user)
            .setDisplayable(true)
            .setDownloadable(true)
            .setDate(new Date())
            .setVersionable(true)
            .setForeignKey("12")
            .setOldId("666");
        Node filledSimpleDoc = JCRUtil.fillDocumentNode(session, docProps, simpleDoc);

        Node file = filledSimpleDoc.addNode("file_en", SilverpeasProperty.SLV_SIMPLE_ATTACHMENT);
        JCRUtil.NodeFileProperties fileProps =
            new JCRUtil.NodeFileProperties("SmalltalkForever.pdf", docProps.getDate(), user)
                .setLastModificationDate(docProps.getDate())
                .setLastModifier(user)
                .setFormId("-1")
                .setTitle("Smalltalk Forever")
                .setDescription("All about this wonderful language")
                .setMimeType("application/pdf")
                .setLanguage("en")
                .setSize(378883L);
        JCRUtil.fillFileNode(session, fileProps, file);

        session.save();
      }
    });
  }
}