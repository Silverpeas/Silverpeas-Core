/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Lib
 * Open Source Software ("FLOSS") applications as described in Silverpeas
 * FLOSS exception. You should have received a copy of the text describin
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public Licen
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.cmis.walkers;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.silverpeas.cmis.CMISEnvForTests;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.silverpeas.cmis.SilverpeasObjectsTree.LANGUAGE;

/**
 * Unit tests about the content stream of the objects in the CMIS tree.
 * @author mmoquillon
 */
@DisplayName("Test access of content stream of the Silverpeas objects in the CMIS tree through " +
    "the walkers")
class ObjectContentInTreeWalkerTest extends CMISEnvForTests {

  @Test
  @DisplayName("Collaborative space doesn't support content stream")
  void getContentStreamOfSpace() {
    final String spaceId = "WA1";
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisStreamNotSupportedException.class,
        () -> walker.getContentStream(spaceId, "fr", 0, -1));
  }

  @Test
  @DisplayName("Application doesn't support content stream")
  void getContentStreamOfApplication() {
    final String appId = "kmelia1";
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisStreamNotSupportedException.class,
        () -> walker.getContentStream(appId, "fr", 0, -1));
  }

  @Test
  @DisplayName("Node doesn't support content stream")
  void getContentStreamOfNode() {
    final NodePK nodePK = new NodePK("3", "kmelia2");
    final NodeDetail node = nodeService.getDetail(nodePK);
    assertThat(node, notNullValue());

    String folderId = node.getIdentifier()
        .asString();
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisStreamNotSupportedException.class,
        () -> walker.getContentStream(folderId, "fr", 0, -1));
  }

  @Test
  @DisplayName("Publication doesn't support content stream")
  void getContentStreamOfPublication() {
    final PublicationPK pk = new PublicationPK("1", "kmelia1");
    final PublicationDetail publi = publicationService.getDetail(pk);
    assertThat(publi, notNullValue());

    String publiId = publi.getIdentifier()
        .asString();
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    assertThrows(CmisStreamNotSupportedException.class,
        () -> walker.getContentStream(publiId, "fr", 0, -1));
  }

  @Test
  @DisplayName("Document can have a content stream")
  void getContentStreamOfDocument() {
    final SimpleDocumentPK pk = new SimpleDocumentPK("1", "kmelia1");
    final SimpleDocument doc = attachmentService.searchDocumentById(pk, LANGUAGE);
    assertThat(doc, notNullValue());

    String docId = doc.getIdentifier()
        .asString();
    CmisObjectsTreeWalker walker = CmisObjectsTreeWalker.getInstance();
    ContentStream content = walker.getContentStream(docId, "fr", 0, -1);
    assertThat(content, notNullValue());
    assertThat(content.getMimeType(), is(doc.getContentType()));
    assertThat(content.getFileName(), is(doc.getAttachment().getFilename()));
    assertThat(content.getLength(), greaterThanOrEqualTo(102400L));
  }


}
