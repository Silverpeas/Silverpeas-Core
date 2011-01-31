/*
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.export;

import com.silverpeas.util.StringUtil;

/**
 * It represents a descriptor about the export of resources into a file. As such it defines the
 * path of the file and the format into which the resources have to be exported. For all export
 * operations that require more information about the export of a given type of resources, a new
 * descriptor class can be defined by subclassing ExportDescriptor.
 */
public class ExportDescriptor {

  /**
   * A specific value for the export format indicating that no explicit format is defined for the
   * export. In general, this specific value means the exporter is defined for one single export
   * format and thus it is useless to specify the export format.
   */
  public static final String NO_FORMAT = "";

  private String filePath = "";
  private String exportFormat = NO_FORMAT;

  /**
   * Constructs a new ExportDescriptor instance by specifying the path of the export destination file.
   * @param filePath the path of the export destination file.
   */
  public ExportDescriptor(String filePath) {
    if (! StringUtil.isDefined(filePath)) {
      throw new IllegalArgumentException("The file path should be set");
    }
    this.filePath = filePath;
  }

  /**
   * Constructs a new ExportDescriptor instance by specifying both the path of the export destination
   * file and the format into which the resources have to be exported.
   * @param filePath the path of the export destination file.
   * @param exportFormat the export format.
   */
  public ExportDescriptor(String filePath, String exportFormat) {
    if (! StringUtil.isDefined(filePath) || ! StringUtil.isDefined(exportFormat)) {
      throw new IllegalArgumentException("The file path or the export format should be set");
    }
    this.filePath = filePath;
    this.exportFormat = exportFormat;
  }

  /**
   * Gets the export format of the resources.
   * If no export format is defined, then NO_FORMAT is returned.
   * @return the export format.
   */
  public String getExportFormat() {
    return exportFormat;
  }

  /**
   * Sets a format into which the resource have to be exported.
   * @param exportFormat the export format to set.
   */
  public void setExportFormat(String exportFormat) {
    if (! StringUtil.isDefined(exportFormat)) {
      throw new IllegalArgumentException("The export format should be set");
    }
    this.exportFormat = exportFormat;
  }

  /**
   * Gets the path of the file into which the resources have to be exported.
   * @return the export destination file path.
   */
  public String getFilePath() {
    return filePath;
  }

  /**
   * Sets an export destination file path.
   * @param filePath the file path to set.
   */
  public void setFilePath(String filePath) {
    if (! StringUtil.isDefined(filePath)) {
      throw new IllegalArgumentException("The file path should be set");
    }
    this.filePath = filePath;
  }


}
