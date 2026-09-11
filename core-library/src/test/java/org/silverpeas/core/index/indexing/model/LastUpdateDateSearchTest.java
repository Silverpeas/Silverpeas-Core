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
package org.silverpeas.core.index.indexing.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.silverpeas.core.index.indexing.IndexFileManager;
import org.silverpeas.core.index.indexing.IndexingLogger;
import org.silverpeas.core.index.indexing.parser.ParserManager;
import org.silverpeas.core.index.search.model.IndexSearcher;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.kernel.test.annotations.TestManagedBeans;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.is;

/**
 * Checks, against a real Lucene index, how the search criteria on dates are applied on the
 * creation and the last update dates stored in the index.
 * <p>
 * Scenario: a content is created at date A and then modified at date B with the term "titi". The
 * search is performed with a date S such as A &lt; S &lt; B.
 * </p>
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans({IndexManager.class, IndexSearcher.class})
class LastUpdateDateSearchTest {

  private static final String COMPONENT_ID = "webPages1";
  private static final String OBJECT_TYPE = "Component";
  private static final String OBJECT_ID = "webPages1";
  private static final LocalDate DATE_A = LocalDate.of(2026, 1, 12);
  private static final LocalDate DATE_B = LocalDate.of(2026, 3, 16);
  private static final LocalDate SEARCH_DATE = LocalDate.of(2026, 2, 1);

  @TestManagedMock
  private ParserManager parserManager;

  @TempDir
  Path indexRoot;

  private IndexManager indexManager;
  private IndexSearcher searcher;

  @BeforeEach
  void setUp() throws Exception {
    // the indexing logger is initialized at Silverpeas startup
    new IndexingLogger().init();
    IndexFileManager.configure(indexRoot.toString() + File.separator);
    indexManager = IndexManager.get();
    searcher = IndexSearcher.get();
  }

  @Test
  void storedUpdateDateIsTheCreationDateWhenNoModificationDateIsSet() throws Exception {
    indexContent(null);

    MatchingIndexEntry entry = searcher.search(COMPONENT_ID, OBJECT_ID, OBJECT_TYPE);

    assertThat(entry.getCreationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
    assertThat(entry.getLastModificationDate(), is(DateUtil.formatAsLuceneDate(DATE_A)));
  }

  @Test
  void modifiedContentIsNotFoundWithModifiedAfterCriterionWhenNoModificationDateIsSet()
      throws Exception {
    indexContent(null);

    assertThat(searcher.search(queryOnTiti()), arrayWithSize(1));

    QueryDescription query = queryOnTiti();
    query.setRequestedUpdatedAfter(SEARCH_DATE);
    assertThat(searcher.search(query), arrayWithSize(0));
  }

  @Test
  void modifiedContentIsFoundWithModifiedAfterCriterionWhenModificationDateIsSet()
      throws Exception {
    indexContent(DATE_B);

    QueryDescription query = queryOnTiti();
    query.setRequestedUpdatedAfter(SEARCH_DATE);
    assertThat(searcher.search(query), arrayWithSize(1));
  }

  @Test
  void publishedAfterCriterionIsOnlyAboutTheCreationDate() throws Exception {
    indexContent(DATE_B);

    QueryDescription query = queryOnTiti();
    query.setRequestedCreatedAfter(SEARCH_DATE);
    assertThat(searcher.search(query), arrayWithSize(0));
  }

  private void indexContent(LocalDate lastUpdateDate) {
    FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(COMPONENT_ID, OBJECT_TYPE, OBJECT_ID));
    indexEntry.setCreationDate(toDate(DATE_A));
    indexEntry.setCreationUser("0");
    if (lastUpdateDate != null) {
      indexEntry.setLastModificationDate(lastUpdateDate);
    }
    indexEntry.setTitle("Web page");
    indexEntry.addTextContent("Un contenu modifié avec le terme titi");
    indexManager.addIndexEntry(indexEntry);
    indexManager.flush();
  }

  private static QueryDescription queryOnTiti() {
    QueryDescription query = new QueryDescription("titi");
    query.addComponent(COMPONENT_ID);
    return query;
  }

  private static Date toDate(LocalDate date) {
    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
