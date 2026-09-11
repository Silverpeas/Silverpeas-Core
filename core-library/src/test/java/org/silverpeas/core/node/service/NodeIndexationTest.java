/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.node.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.content.wysiwyg.notification.WysiwygEventNotifier;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygManager;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.node.dao.NodeDAO;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.node.notification.NodeEventNotifier;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.SettingBundleStub;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Checks the dates put in the index entry of a folder (a node) when it is indexed again, which is
 * what is done by {@link NodeService#processWysiwyg(NodePK)} once its WYSIWYG content has been
 * modified in a kmelia or toolbox application.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class NodeIndexationTest {

  private static final String COMPONENT_ID = "kmelia1";
  private static final String NODE_ID = "5";
  private static final LocalDate DATE_A = LocalDate.of(2026, 1, 12);
  private static final LocalDate DATE_B = LocalDate.of(2026, 3, 16);

  @RegisterExtension
  static SettingBundleStub nodeSettings = stubNodeSettings();

  @TestManagedMock
  private IndexEngineProxy indexEngineProxy;

  @TestManagedMock
  private AttachmentService attachmentService;

  @TestManagedMock
  private WysiwygEventNotifier wysiwygEventNotifier;

  /**
   * The WYSIWYG content of a node is accessed through the WYSIWYG manager, hence a true instance
   * of it working upon the mocked attachment service above.
   */
  @TestManagedBean
  private WysiwygManager wysiwygManager;

  @TestManagedMock
  private NodeDAO nodeDAO;

  @TestManagedMock
  private NodeDeletion nodeDeletion;

  @TestManagedMock
  private NodeEventNotifier notifier;

  @TestedBean
  private DefaultNodeService service;

  @BeforeEach
  void setUp() {
    nodeSettings.put("indexAuthorName", "false");
  }

  @Test
  void theIndexEntryOfAFolderWithoutWysiwygCarriesItsCreationDateAsLastUpdateDate() {
    folderWysiwygIs(List.of());

    service.createIndex(createdFolder());

    FullIndexEntry indexed = indexedEntry();
    assertThat(indexed.getCreationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationUser(), is("0"));
  }

  @Test
  void theIndexEntryOfAFolderWithANeverModifiedWysiwygCarriesItsCreationDateAsLastUpdateDate() {
    folderWysiwygIs(List.of(createdWysiwyg()));

    service.createIndex(createdFolder());

    FullIndexEntry indexed = indexedEntry();
    assertThat(indexed.getCreationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationUser(), is("0"));
  }

  @Test
  void theIndexEntryOfAFolderWithAModifiedWysiwygCarriesTheLastUpdateOfTheWysiwyg() {
    SimpleDocument wysiwyg = createdWysiwyg();
    // as done by DocumentRepository#updateDocument when the WYSIWYG content is saved
    wysiwyg.setLastUpdateDate(toDate(DATE_B));
    wysiwyg.setUpdatedBy("1");
    folderWysiwygIs(List.of(wysiwyg));

    service.createIndex(createdFolder());

    FullIndexEntry indexed = indexedEntry();
    assertThat(indexed.getCreationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationDate(), is(DateUtil.formatAsLuceneDate(DATE_B)));
    assertThat(indexed.getLastModificationUser(), is("1"));
  }

  private void folderWysiwygIs(List<SimpleDocument> documents) {
    when(attachmentService.listDocumentsByForeignKeyAndType(
        argThat(r -> ("Node_" + NODE_ID).equals(r.getId()) &&
            COMPONENT_ID.equals(r.getInstanceId())), eq(DocumentType.wysiwyg), any()))
        .thenReturn(new SimpleDocumentList<>(documents));
  }

  private static NodeDetail createdFolder() {
    NodeDetail folder = new NodeDetail(new NodePK(NODE_ID, COMPONENT_ID), "Dossier", "", 2, "0");
    folder.setCreatorId("0");
    folder.setCreationDate(toDate(DATE_A));
    return folder;
  }

  private static SimpleDocument createdWysiwyg() {
    SimpleAttachment attachment = SimpleAttachment.builder("fr")
        .setFilename("Node_" + NODE_ID + "wysiwyg_fr.txt")
        .setTitle("")
        .setContentType("text/html")
        .setCreationData("0", toDate(DATE_A))
        .build();
    SimpleDocument wysiwyg =
        new SimpleDocument(new SimpleDocumentPK("uuid-1", COMPONENT_ID), "Node_" + NODE_ID, 0,
            false, attachment);
    wysiwyg.setDocumentType(DocumentType.wysiwyg);
    return wysiwyg;
  }

  private FullIndexEntry indexedEntry() {
    ArgumentCaptor<FullIndexEntry> indexed = ArgumentCaptor.forClass(FullIndexEntry.class);
    verify(indexEngineProxy).add(indexed.capture());
    return indexed.getValue();
  }

  private static SettingBundleStub stubNodeSettings() {
    try {
      // the settings are in a static field of DefaultNodeService
      return new SettingBundleStub(DefaultNodeService.class, "nodeSettings");
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

  private static Date toDate(LocalDate date) {
    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
