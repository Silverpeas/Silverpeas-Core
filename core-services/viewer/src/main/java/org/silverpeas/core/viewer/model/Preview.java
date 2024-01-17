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
package org.silverpeas.core.viewer.model;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;

/**
 * @author Yohann Chastagnier
 */
public interface Preview extends Serializable {

  /**
   * Getting the document identifier.
   * @return a string.
   */
  String getDocumentId();

  /**
   * Getting the document type (not mime-type, but document service type, 'attachment' for example).
   * @return a string.
   */
  String getDocumentType();

  /**
   * Getting the language of the document content.
   * @return a string.
   */
  String getLanguage();

  /**
   * Getting the license of display software product if any
   * @return a string.
   */
  String getDisplayLicenseKey();

  /**
   * Getting URL of the document
   * @return a string.
   */
String getURLAsString();

  /**
   * Getting {@link Path} of the document on the server.
   * @return a {@link Path} instance.
   */
  Path getServerFilePath();

  /**
   * Getting the original file of the document
   * @return a string.
   */
  String getOriginalFileName();

  /**
   * Getting the physical file of the document
   * @return a {@link File} instance.
   */
  File getPhysicalFile();

  /**
   * Getting the width of the document
   * @return a string.
   */
  String getWidth();

  /**
   * Getting the height of the document
   * @return a string.
   */
  String getHeight();
}
