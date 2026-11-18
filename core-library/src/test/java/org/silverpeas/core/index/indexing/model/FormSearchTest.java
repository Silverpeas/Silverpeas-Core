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
import org.silverpeas.kernel.test.annotations.TestManagedBeans;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

/**
 * Checks, against a real Lucene index, that a search restricted to a given form only finds the
 * contents whose data have been filled with that form.
 * <p>
 * Scenario of the bug #15593: a form is selected in the search by forms, no field of it is
 * filled, and a keyword is given. Only the contents of that form matching the keyword have to be
 * found.
 * </p>
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@TestManagedBeans({IndexManager.class, IndexSearcher.class})
class FormSearchTest {

  private static final String COMPONENT_ID = "kmelia1";
  private static final String KEYWORD = "toyota";

  @TestManagedMock
  private ParserManager parserManager;

  @TempDir
  Path indexRoot;

  private IndexManager indexManager;
  private IndexSearcher searcher;

  @BeforeEach
  void setUp() {
    // the indexing logger is initialized at Silverpeas startup
    new IndexingLogger().init();
    IndexFileManager.configure(indexRoot.toString() + File.separator);
    indexManager = IndexManager.get();
    searcher = IndexSearcher.get();
    indexContent("1", "classifieds");
    indexContent("2", "prospects");
    indexContent("3", "classifieds", "prospects");
    indexContent("4");
  }

  @Test
  void searchWithoutAnyFormFindsAllTheContentsMatchingTheKeyword() throws Exception {
    QueryDescription query = queryOnKeyword();

    assertThat(foundContents(query), containsInAnyOrder("1", "2", "3", "4"));
  }

  @Test
  void searchOnAFormFindsOnlyTheContentsFilledWithThatForm() throws Exception {
    QueryDescription query = queryOnKeyword();
    query.setRequestedFormName("classifieds");

    assertThat(foundContents(query), containsInAnyOrder("1", "3"));
  }

  @Test
  void searchOnAFormWithoutAnyKeywordFindsAllTheContentsFilledWithThatForm() throws Exception {
    QueryDescription query = new QueryDescription();
    query.addComponent(COMPONENT_ID);
    query.setRequestedFormName("prospects");

    assertThat(foundContents(query), containsInAnyOrder("2", "3"));
  }

  @Test
  void searchOnAFormUsedByNoContentFindsNothing() throws Exception {
    QueryDescription query = queryOnKeyword();
    query.setRequestedFormName("unused");

    assertThat(foundContents(query), empty());
  }

  private List<String> foundContents(QueryDescription query) throws Exception {
    return Arrays.stream(searcher.search(query))
        .map(MatchingIndexEntry::getObjectId)
        .toList();
  }

  private void indexContent(String id, String... formNames) {
    FullIndexEntry indexEntry =
        new FullIndexEntry(new IndexEntryKey(COMPONENT_ID, "Publication", id));
    indexEntry.setCreationDate(new Date());
    indexEntry.setCreationUser("0");
    indexEntry.setTitle("Publication " + id);
    indexEntry.addTextContent("Vends une Toyota Auris de 2015");
    Arrays.stream(formNames).forEach(indexEntry::addFormName);
    indexManager.addIndexEntry(indexEntry);
    indexManager.flush();
  }

  private static QueryDescription queryOnKeyword() {
    QueryDescription query = new QueryDescription(KEYWORD);
    query.addComponent(COMPONENT_ID);
    return query;
  }
}
