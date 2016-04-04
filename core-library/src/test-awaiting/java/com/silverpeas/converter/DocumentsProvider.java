/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.converter;

import java.io.File;

/**
 * A provider of documents in several format dedicated to tests.
 */
public final class DocumentsProvider {

  private static final String BASENAME_DOCUMENT = "API_REST_Silverpeas";

  public static DocumentsProvider aDocumentsProvider() {
    return new DocumentsProvider();
  }

  /**
   * Gets a document in ODT format
   * @return the the ODT document
   * @throws Exception if an error occurs while getting the document.
   */
  public File getODTDocument() throws Exception {
    return new File(getClass().getResource(BASENAME_DOCUMENT + ".odt").toURI());
  }

  /**
   * Gets a document in the MS-Word 97/2000/XP format
   * @return the MS-Word document
   * @throws Exception if an error occurs while getting the document.
   */
  public File getMSWordDocument() throws Exception {
    return new File(getClass().getResource(BASENAME_DOCUMENT + ".doc").toURI());
  }

  private DocumentsProvider() {

  }
}
