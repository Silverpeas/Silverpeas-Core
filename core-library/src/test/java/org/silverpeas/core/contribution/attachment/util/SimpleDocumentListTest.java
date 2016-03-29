/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contribution.attachment.util;

import org.junit.Test;
import org.silverpeas.core.contribution.attachment.model.HistorisedDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleAttachment;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

public class SimpleDocumentListTest {

  @Test
  public void removeLanguageFallbacksOnEmptyList() {
    new SimpleDocumentList().setQueryLanguage("en").removeLanguageFallbacks();
  }

  @Test(expected = NullPointerException.class)
  public void removeLanguageFallbacksOnListWithOneElement() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>().setQueryLanguage("en");
    test.add(new SimpleDocument());
    test.removeLanguageFallbacks();
  }

  @Test
  public void removeLanguageFallbacksOnListWithOneElementButNoLanguageSet() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>().setQueryLanguage("");
    test.add(new SimpleDocument());
    test.removeLanguageFallbacks();
    assertThat(test, hasSize(1));
  }

  @Test
  public void removeLanguageFallbacksOnListWithSeveralElementsButNoLanguageSet() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>().setQueryLanguage(null);
    int i = 1;
    // i = number of the month of the last update date...
    test.add(createDocument(i++, "en", createDate("2014-01-01")));
    test.add(createDocument(i++, "fr", createDate("2014-01-02")));
    test.add(createDocument(i++, "de", createDate("2014-01-03")));
    test.add(createDocument(i++, "fr", createDate("2014-01-04")));
    test.add(createDocument(i++, "en", createDate("2014-01-05")));
    test.add(createDocument(i++, "en", createDate("2014-01-06")));
    test.add(createDocument(i++, "fr", createDate("2014-01-07")));
    test.add(createDocument(i++, "en", createDate("2014-01-08")));
    test.add(createDocument(i++, "en", createDate("2014-01-09")));
    test.add(createDocument(i++, "fr", createDate("2014-01-10")));
    test.add(createDocument(i++, "en", createDate("2014-01-11")));
    test.add(createDocument(i, "en", createDate("2014-01-12")));
    List<Integer> ids = extractIds(test);
    assertThat(ids, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
    test.removeLanguageFallbacks();
    ids = extractIds(test);
    assertThat(ids, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
  }

  @Test
  public void removeLanguageFallbacksOnListWithSeveralElements() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>().setQueryLanguage("en");
    int i = 1;
    // i = number of the month of the last update date...
    test.add(createDocument(i++, "en", createDate("2014-01-01")));
    test.add(createDocument(i++, "fr", createDate("2014-01-02")));
    test.add(createDocument(i++, "de", createDate("2014-01-03")));
    test.add(createDocument(i++, "fr", createDate("2014-01-04")));
    test.add(createDocument(i++, "en", createDate("2014-01-05")));
    test.add(createDocument(i++, "en", createDate("2014-01-06")));
    test.add(createDocument(i++, "fr", createDate("2014-01-07")));
    test.add(createDocument(i++, "en", createDate("2014-01-08")));
    test.add(createDocument(i++, "en", createDate("2014-01-09")));
    test.add(createDocument(i++, "fr", createDate("2014-01-10")));
    test.add(createDocument(i++, "en", createDate("2014-01-11")));
    test.add(createDocument(i, "en", createDate("2014-01-12")));
    List<Integer> ids = extractIds(test);
    assertThat(ids, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
    test.removeLanguageFallbacks();
    ids = extractIds(test);
    assertThat(ids, contains(1, 5, 6, 8, 9, 11, 12));
  }

  @Test
  public void orderByLanguageAndLastUpdateOnEmptyList() {
    new SimpleDocumentList().orderByLanguageAndLastUpdate("fr", "en");
  }

  @Test
  public void orderByLanguageAndLastUpdateOnListWithOneElement() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>();
    test.add(new SimpleDocument());
    test.orderByLanguageAndLastUpdate("fr", "en");
  }

  /**
   * This method is to prove that nothing is ordered when it exists only one element in the list.
   * @see #orderByLanguageAndLastUpdateOnListWithOneElement()
   */
  @Test(expected = NullPointerException.class)
  public void orderByLanguageAndLastUpdateOnListWithTwoElements() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>();
    test.add(new SimpleDocument());
    test.add(new SimpleDocument());
    test.orderByLanguageAndLastUpdate("fr", "en");
  }

  /**
   * Default platform language priorities : fr, en, de
   */
  @Test
  public void orderByLanguageAndLastUpdateOnListWithSeveralElements() {
    //noinspection MismatchedQueryAndUpdateOfCollection
    SimpleDocumentList<SimpleDocument> test = new SimpleDocumentList<>();
    int i = 1;
    // i = number of the month of the last update date...
    test.add(createDocument(i++, "en", createDate("2014-01-01")));
    test.add(createDocument(i++, "fr", createDate("2014-01-02")));
    test.add(createDocument(i++, "de", createDate("2014-01-03")));
    test.add(createDocument(i++, "fr", createDate("2014-01-04")));
    test.add(createDocument(i++, "en", createDate("2014-01-05")));
    test.add(createDocument(i++, "en", createDate("2014-01-06")));
    test.add(createDocument(i++, "fr", createDate("2014-01-07")));
    test.add(createDocument(i++, "en", createDate("2014-01-08")));
    test.add(createDocument(i++, "en", createDate("2014-01-09")));
    test.add(createDocument(i++, "fr", createDate("2014-01-10")));
    test.add(createDocument(i++, "en", createDate("2014-01-11")));
    test.add(createDocument(i, "en", createDate("2014-01-12")));
    List<Integer> ids = extractIds(test);
    assertThat(ids, contains(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

    test.orderByLanguageAndLastUpdate();
    ids = extractIds(test);
    assertThat(ids, contains(10, 7, 4, 2, 12, 11, 9, 8, 6, 5, 1, 3));

    test.orderByLanguageAndLastUpdate("de");
    ids = extractIds(test);
    assertThat(ids, contains(3, 10, 7, 4, 2, 12, 11, 9, 8, 6, 5, 1));

    test.orderByLanguageAndLastUpdate("en", "de", "fr");
    ids = extractIds(test);
    assertThat(ids, contains(12, 11, 9, 8, 6, 5, 1, 3, 10, 7, 4, 2));
  }

  private static List<Integer> extractIds(List<SimpleDocument> list) {
    List<Integer> ids = new ArrayList<>(list.size());
    for (SimpleDocument document : list) {
      ids.add(Integer.valueOf(document.getId()));
    }
    return ids;
  }

  /**
   * Creates a date from the given pattern.
   * @param datePattern the pattern of the date to take into account.
   * @return the date according to the specified date pattern.
   */
  private Date createDate(String datePattern) {
    return java.sql.Date.valueOf(datePattern);
  }

  /**
   * Creates a document.
   */
  private SimpleDocument createDocument(final int id, String language, Date lastUpdateDate) {
    final SimpleDocument document;
    if (id % 2 == 0) {
      document = new HistorisedDocument();
    } else {
      document = new SimpleDocument();
    }
    document.setId(String.valueOf(id));
    document.setAttachment(new SimpleAttachment());
    document.setLanguage(language);
    if (id % 5 == 0) {
      document.setUpdated(lastUpdateDate);
    } else {
      document.setCreated(lastUpdateDate);
    }
    return document;
  }
}