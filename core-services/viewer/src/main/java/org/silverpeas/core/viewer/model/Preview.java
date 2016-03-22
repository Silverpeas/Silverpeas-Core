/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
package org.silverpeas.core.viewer.model;

import java.io.File;
import java.io.Serializable;

/**
 * @author Yohann Chastagnier
 */
public interface Preview extends Serializable {

  /**
   * Getting the license of display software product if any
   * @return
   */
  String getDisplayLicenseKey();

  /**
   * Getting URL of the document
   * @return
   */
  String getURLAsString();

  /**
   * Getting the original file of the document
   * @return
   */
  String getOriginalFileName();

  /**
   * Getting the physical file of the document
   * @return
   */
  File getPhysicalFile();

  /**
   * Getting the width of the document
   * @return
   */
  String getWidth();

  /**
   * Getting the height of the document
   * @return
   */
  String getHeight();
}
