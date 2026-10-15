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

package org.silverpeas.core.contribution.attachment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEventNotifier;
import org.silverpeas.core.contribution.attachment.repository.DocumentRepository;
import org.silverpeas.core.contribution.attachment.util.SimpleDocumentList;
import org.silverpeas.core.contribution.attachment.webdav.WebdavRepository;
import org.silverpeas.core.index.indexing.model.FileDescription;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.indexing.model.TextDescription;
import org.silverpeas.core.jcr.JCRSession;
import org.silverpeas.core.jcr.RepositoryProvider;
import org.silverpeas.core.jcr.SilverpeasRepository;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Checks the enrichment of the index entry of a contribution with the documents attached to it.
 * The documents are those of the given contribution, whatever the component instance the index
 * entry is scoped to; this is the case of the index entry of a publication alias in another
 * component instance than the one hosting the publication.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SimpleDocumentIndexEnrichmentTest {

  private static final String PUB_ID = "10";
  private static final String HOST_INSTANCE = "kmelia1";
  private static final String OTHER_INSTANCE = "kmelia6";
  private static final String LANGUAGE = "fr";

  @TestManagedMock
  private DocumentRepository repository;

  @TestManagedMock
  private WebdavRepository webdavRepository;

  @TestManagedMock
  private AttachmentEventNotifier notifier;

  @TestManagedMock
  private RepositoryProvider repositoryProvider;

  @TestedBean
  private SimpleDocumentService service;

  @BeforeEach
  void setUp() throws RepositoryException {
    SilverpeasRepository jcr = mock(SilverpeasRepository.class);
    when(repositoryProvider.getRepository()).thenReturn(jcr);
    when(jcr.login(any(Credentials.class))).thenReturn(mock(JCRSession.class));
    when(repository.listDocumentsByForeignId(any(), any(), any(), any()))
        .thenReturn(new SimpleDocumentList<>());
    when(repository.listDocumentsByForeignId(any(), eq(HOST_INSTANCE), eq(PUB_ID), eq(LANGUAGE)))
        .thenReturn(new SimpleDocumentList<>(List.of(attachmentOfPublication())));
  }

  @Test
  void theIndexEntryOfAContributionIsEnrichedWithItsOwnDocuments() {
    FullIndexEntry indexEntry = indexEntryScopedTo(HOST_INSTANCE);

    service.updateIndexEntryWithDocuments(indexEntry);

    assertThat(filePaths(indexEntry), contains(attachmentOfPublication().getAttachmentPath()));
    assertThat(textContents(indexEntry), hasItems("Wrappers to the Rescue", "wrappers.doc"));
  }

  @Test
  void theIndexEntryScopedToAnotherInstanceIsEnrichedWithTheDocumentsOfTheGivenContribution() {
    FullIndexEntry indexEntry = indexEntryScopedTo(OTHER_INSTANCE);

    service.updateIndexEntryWithDocuments(indexEntry,
        new ResourceReference(PUB_ID, HOST_INSTANCE));

    assertThat(filePaths(indexEntry), contains(attachmentOfPublication().getAttachmentPath()));
    assertThat(textContents(indexEntry), hasItems("Wrappers to the Rescue", "wrappers.doc"));
  }

  @Test
  void theIndexEntryScopedToAnotherInstanceHasNoDocumentsOfItsOwn() {
    FullIndexEntry indexEntry = indexEntryScopedTo(OTHER_INSTANCE);

    service.updateIndexEntryWithDocuments(indexEntry);

    assertThat(filePaths(indexEntry), is(empty()));
  }

  @Test
  void theIndexEntryOfADocumentItselfIsNeverEnriched() {
    FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(HOST_INSTANCE, "Attachment", "uuid-1"));

    service.updateIndexEntryWithDocuments(indexEntry,
        new ResourceReference(PUB_ID, HOST_INSTANCE));

    assertThat(filePaths(indexEntry), is(empty()));
  }

  private static FullIndexEntry indexEntryScopedTo(String instanceId) {
    FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(instanceId, "Publication", PUB_ID));
    indexEntry.setLang(LANGUAGE);
    return indexEntry;
  }

  private static SimpleDocument attachmentOfPublication() {
    SimpleAttachment attachment = SimpleAttachment.builder(LANGUAGE)
        .setFilename("wrappers.doc")
        .setTitle("Wrappers to the Rescue")
        .setDescription("")
        .setContentType("application/msword")
        .setCreationData("1", new Date())
        .build();
    SimpleDocument document =
        new SimpleDocument(new SimpleDocumentPK("uuid-1", HOST_INSTANCE), PUB_ID, 0, false,
            attachment);
    document.setDocumentType(DocumentType.attachment);
    return document;
  }

  private static List<String> filePaths(FullIndexEntry indexEntry) {
    return indexEntry.getFileContentList().stream()
        .map(FileDescription::getPath)
        .collect(Collectors.toList());
  }

  private static List<String> textContents(FullIndexEntry indexEntry) {
    return indexEntry.getTextContentList().stream()
        .map(TextDescription::getContent)
        .collect(Collectors.toList());
  }
}
