/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.util;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 * @author Yohann Chastagnier
 */
public enum FileType {

  /*
   * File definitions
   */

  // Image
  BMP, GIF, JPG("jpeg"), PCD, PNG, PSD, TGA, TIF("tiff"),

  // Text
  PDF(),

  // Unknown
  UNKNOWN,

  /*
   * General definitions
   */

  // Images that are handled by applications
  IMAGE_DEFAULT(BMP, GIF, JPG, PCD, PNG, TGA, TIF),

  // Images that are handled by image tool interface
  IMAGE_COMPATIBLE_WITH_IMAGETOOL(BMP, GIF, JPG, PCD, PNG, TGA, TIF);

  static {
    final Set<FileType> filesToPerform = EnumSet.allOf(FileType.class);
    for (final FileType fileType : filesToPerform) {
      fileType.mimeType =
          FileUtil.getMimeType(new StringBuilder("file.").append(fileType.getExtension())
              .toString());
    }
  }

  private static final String GENERAL_DEFINITION_EXTENSION = "*";
  private final Set<String> extensions;
  private String mimeType = "";
  private Set<FileType> fileTypes = new LinkedHashSet<FileType>(0);

  /**
   * File definition
   * @param extension
   */
  private FileType() {
    extensions = new LinkedHashSet<String>();
    extensions.add(name().toLowerCase());
  }

  /**
   * File definition
   * @param extension
   */
  private FileType(final String... otherExtensions) {
    this();
    extensions.addAll(Arrays.asList(otherExtensions));
  }

  /**
   * General definition
   * @param fileTypes
   */
  private FileType(final FileType... fileTypes) {
    this(GENERAL_DEFINITION_EXTENSION);
    this.fileTypes = new HashSet<FileType>(Arrays.asList(fileTypes));
  }

  /**
   * Retrieves the right enum file type from the given file name
   * @param fileName
   * @return
   */
  public static FileType decode(final String fileName) {
    return decode(new File(fileName));
  }

  /**
   * Retrieves the right enum file type from the given file
   * @param file
   * @return
   */
  public static FileType decode(final File file) {
    final String fileExtension = FilenameUtils.getExtension(file.getName());
    for (final FileType fileType : FileType.values()) {
      if (fileType.getExtensions().contains(fileExtension.toLowerCase())) {
        return fileType;
      }
    }
    return UNKNOWN;
  }

  /**
   * @return the first entry of extensions
   */
  public String getExtension() {
    if (extensions.isEmpty()) {
      return "";
    }
    return extensions.iterator().next();
  }

  /**
   * @return the extensions
   */
  public Set<String> getExtensions() {
    return extensions;
  }

  /**
   * @return the mimeType
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Determines if the file is kind of given file type
   * @param fileType
   * @return
   */
  public boolean is(final FileType fileType) {
    return (fileType != null && (this.equals(fileType) || fileType.fileTypes.contains(this)));
  }
}
