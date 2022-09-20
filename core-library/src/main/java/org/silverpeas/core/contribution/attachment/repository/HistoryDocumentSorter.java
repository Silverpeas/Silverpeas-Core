/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.contribution.attachment.repository;

import org.silverpeas.core.contribution.attachment.model.SimpleDocument;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * A sorter of versioned documents by their version number.
 * @author ehugonnet
 */
public class HistoryDocumentSorter implements Serializable {
  private static final long serialVersionUID = 4263157996954433938L;

  private static final Comparator<SimpleDocument> comparator = (doc1, doc2) -> {
    if (doc1.getMajorVersion() == doc2.getMajorVersion()) {
      if (doc1.getMinorVersion() == doc2.getMinorVersion()) {
        // This comparison is mandatory because it could exist several history document with
        // the same major and minor version. Indeed, some updates of the property doesn't affect
        // the version, but they add still a new version in version history. For example,
        // updating the order of display of attachments ...
        return doc2.getVersionIndex() - doc1.getVersionIndex();
      }
      return doc2.getMinorVersion() - doc1.getMinorVersion();
    }
    return doc2.getMajorVersion() - doc1.getMajorVersion();
  };

  public static void sortHistory(List<? extends SimpleDocument> docs) {
    docs.sort(comparator);
  }

  private HistoryDocumentSorter() {
  }

}
