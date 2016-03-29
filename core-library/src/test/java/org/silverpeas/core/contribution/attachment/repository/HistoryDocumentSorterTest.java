/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.contribution.attachment.repository;

import org.junit.Test;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 *
 * @author ehugonnet
 */
public class HistoryDocumentSorterTest {

 /**
   * Test of compare method, of class VersionSimpleDocumentComparator.
   */
  @Test
  public void testSortHistory() {
    SimpleDocument doc1v1 = new SimpleDocument();
    doc1v1.setNodeName("doc1v1");
    doc1v1.setVersionIndex(1);
    SimpleDocument doc1v2 = new SimpleDocument();
    doc1v2.setNodeName("doc1v2");
    doc1v2.setVersionIndex(2);
    SimpleDocument doc2 = new SimpleDocument();
    doc2.setNodeName("doc2");
    SimpleDocument doc3 = new SimpleDocument();
    doc3.setNodeName("doc3");
    doc2.setMajorVersion(1);
    doc2.setMinorVersion(0);
    doc3.setMajorVersion(0);
    doc3.setMinorVersion(0);
    doc1v1.setMinorVersion(1);
    doc1v2.setMinorVersion(1);
    List<SimpleDocument> docs = Arrays.asList(doc1v1, doc2, doc3, doc1v2);
    HistoryDocumentSorter.sortHistory(docs);
    assertThat(docs, contains(doc2, doc1v2, doc1v1, doc3));
    doc1v1.setVersionIndex(3);
    HistoryDocumentSorter.sortHistory(docs);
    assertThat(docs, not(contains(doc2, doc1v2, doc1v1, doc3)));
  }
}
