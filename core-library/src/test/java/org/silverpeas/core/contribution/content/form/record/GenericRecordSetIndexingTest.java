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
package org.silverpeas.core.contribution.content.form.record;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Checks the indexing of the data of a form record into the index entry of the content they
 * belong to.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
class GenericRecordSetIndexingTest {

  private static final String FORM_NAME = "classifieds";
  private static final String RECORD_ID = "12";

  private MockedStatic<GenericRecordSetManager> managerProvider;
  private GenericRecordSetManager manager;
  private IdentifiedRecordTemplate template;

  @BeforeEach
  void setUp() {
    manager = mock(GenericRecordSetManager.class);
    managerProvider = mockStatic(GenericRecordSetManager.class);
    managerProvider.when(GenericRecordSetManager::getInstance).thenReturn(manager);
    template = new IdentifiedRecordTemplate(new GenericRecordTemplate());
    template.setExternalId("classifieds29:" + FORM_NAME);
  }

  @AfterEach
  void closeManagerMock() {
    managerProvider.close();
  }

  @Test
  void theIndexEntryIsMarkedWithTheFormOfTheIndexedRecord() throws Exception {
    when(manager.getLanguagesOfRecord(template, RECORD_ID)).thenReturn(List.of("fr"));
    when(manager.getRecord(template, RECORD_ID, "fr")).thenReturn(template.getEmptyRecord());
    FullIndexEntry indexEntry = newIndexEntry();

    new GenericRecordSet(template).indexRecord(RECORD_ID, FORM_NAME, indexEntry);

    assertThat(indexEntry.getFormNames(), contains(FORM_NAME));
  }

  @Test
  void theIndexEntryIsNotMarkedWithTheFormWhenThereIsNoRecord() throws Exception {
    when(manager.getLanguagesOfRecord(template, RECORD_ID)).thenReturn(List.of("fr"));
    when(manager.getRecord(template, RECORD_ID, "fr")).thenReturn(null);
    FullIndexEntry indexEntry = newIndexEntry();

    new GenericRecordSet(template).indexRecord(RECORD_ID, FORM_NAME, indexEntry);

    assertThat(indexEntry.getFormNames(), empty());
  }

  private static FullIndexEntry newIndexEntry() {
    return new FullIndexEntry(new IndexEntryKey("classifieds29", "Classified", RECORD_ID));
  }
}
