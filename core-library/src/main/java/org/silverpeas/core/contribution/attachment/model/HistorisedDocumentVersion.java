/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.core.contribution.attachment.model;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class permits to get a historized document behaviour not from the master version but from a
 * frozen version of the document. The history is accorded to the specified version. To obtain the
 * master version, please use {@link #getVersionMaster()} method. To check if the current instance
 * is indexed on master version, please use {@link #isVersionMaster()} method.
 * @author Yohann Chastagnier
 */
public class HistorisedDocumentVersion extends HistorisedDocument {
  private static final long serialVersionUID = -3077658530477641631L;

  /**
   * Default constructor.
   * @param version the simple document version from which the historized document is indexed.
   */
  public HistorisedDocumentVersion(SimpleDocumentVersion version) {
    super(version);
    setVersionMaster(version.getVersionMaster());
    setHistory(new ArrayList<>(version.getVersionMaster().getHistory()));
    Iterator<SimpleDocumentVersion> historyIt = getHistory().iterator();
    while (historyIt.hasNext()) {
      SimpleDocumentVersion currentVersion = historyIt.next();
      if (currentVersion.getVersionIndex() >= getVersionIndex()) {
        historyIt.remove();
      } else {
        break;
      }
    }
  }
}
