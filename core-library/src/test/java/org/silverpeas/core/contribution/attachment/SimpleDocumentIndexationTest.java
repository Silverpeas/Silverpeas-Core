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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.contribution.attachment.model.DocumentType;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.attachment.notification.AttachmentEventNotifier;
import org.silverpeas.core.contribution.attachment.repository.DocumentRepository;
import org.silverpeas.core.contribution.attachment.webdav.WebdavRepository;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;

/**
 * Checks the dates put in the index entry of a WYSIWYG document (the WYSIWYG content of a
 * webPages application for instance) once this document has been updated.
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class SimpleDocumentIndexationTest {

  private static final String COMPONENT_ID = "webPages1";
  private static final LocalDate DATE_A = LocalDate.of(2026, 1, 12);
  private static final LocalDate DATE_B = LocalDate.of(2026, 3, 16);

  @TestManagedMock
  private IndexEngineProxy indexEngineProxy;

  @TestManagedMock
  private DocumentRepository repository;

  @TestManagedMock
  private WebdavRepository webdavRepository;

  @TestManagedMock
  private AttachmentEventNotifier notifier;

  @TestedBean
  private SimpleDocumentService service;

  @Test
  void theIndexEntryOfAnUpdatedWysiwygCarriesItsLastUpdateDate() {
    SimpleDocument wysiwyg = createdWysiwyg();
    // as done by DocumentRepository#updateDocument when the WYSIWYG content is saved
    wysiwyg.setLastUpdateDate(toDate(DATE_B));

    service.createIndex(wysiwyg);

    FullIndexEntry indexed = indexedEntry();
    assertThat(indexed.getCreationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationDate(), is(DateUtil.formatAsLuceneDate(DATE_B)));
  }

  @Test
  void theIndexEntryOfANeverUpdatedWysiwygCarriesItsCreationDateAsLastUpdateDate() {
    SimpleDocument wysiwyg = createdWysiwyg();

    service.createIndex(wysiwyg);

    FullIndexEntry indexed = indexedEntry();
    assertThat(indexed.getCreationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(indexed.getLastModificationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
  }

  private static SimpleDocument createdWysiwyg() {
    SimpleAttachment attachment = SimpleAttachment.builder("fr")
        .setFilename(COMPONENT_ID + "wysiwyg_fr.txt")
        .setTitle("")
        .setContentType("text/html")
        .setCreationData("0", toDate(DATE_A))
        .build();
    SimpleDocument wysiwyg =
        new SimpleDocument(new SimpleDocumentPK("uuid-1", COMPONENT_ID), COMPONENT_ID, 0, false,
            attachment);
    wysiwyg.setDocumentType(DocumentType.wysiwyg);
    return wysiwyg;
  }

  private FullIndexEntry indexedEntry() {
    ArgumentCaptor<FullIndexEntry> indexed = ArgumentCaptor.forClass(FullIndexEntry.class);
    verify(indexEngineProxy).add(indexed.capture());
    return indexed.getValue();
  }

  private static Date toDate(LocalDate date) {
    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
