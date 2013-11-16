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
package org.silverpeas.attachment.repository;

import org.silverpeas.attachment.model.SimpleDocument;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author ehugonnet
 */
public class HistoryDocumentSorter implements Serializable{
  private static final long serialVersionUID = 4263157996954433938L;

  private static final Comparator<SimpleDocument> comparator = new Comparator<SimpleDocument>() {
    @Override
    public int compare(SimpleDocument doc1, SimpleDocument doc2) {
      if (doc1.getMajorVersion() == doc2.getMajorVersion()) {
        return doc2.getMinorVersion() - doc1.getMinorVersion();
      }
      return doc2.getMajorVersion() - doc1.getMajorVersion();
    }
  };

  public static void sortHistory(List<SimpleDocument> docs) {
    Collections.sort(docs, comparator);
  }

  private HistoryDocumentSorter() {
  }

}
