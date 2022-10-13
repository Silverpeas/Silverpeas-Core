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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.contribution.model;

/**
 * An attachment is a document file that is attached to a contribution in Silverpeas. The document
 * file is also a user contribution.
 * @author mmoquillon
 */
public interface Attachment extends Contribution {

  /**
   * Gets the title of the document. The title is either the name set explicitly by the user to the
   * attachment or the title fetched from the document metadata itself or the filename whether the
   * previous data weren't provided.
   * @return the title of the document.
   */
  @Override
  String getTitle();

  /**
   * Gets the content type of this attached document file as a predefined MIME type.
   * @return the MIME type of the document.
   */
  String getContentType();

  /**
   * Gets the path of the icon representing either the attachment itself or its content type.
   * @return the path of the icon representing this attachment.
   */
  String getDisplayIcon();

  /**
   * Gets the name of the document file as stored in the filesystem. The filename can differ from
   * the contribution name that is the title of the document.
   * @return the name of the document file in the filesystem.
   */
  String getFilename();

  /**
   * Gets the size of the document file in the filesystem.
   * @return the size in bytes.
   */
  long getSize();

  /**
   * Gets the absolute path of the document file in the filesystem.
   * @return the path of the attachment in the filesystem.
   */
  String getAttachmentPath();

  /**
   * Is this attachment versioned? A document is versioned if each change is historized and comes to
   * a new minor or major version.
   * @return true if this attachment is versioned, false otherwise.
   */
  boolean isVersioned();

  /**
   * Gets the minor part of the document version. If the attachment isn't versioned, then zero
   * value is returned.
   * @return the minor version part of the document.
   */
  int getMinorVersion();

  /**
   * Gets the major part of the document version. If the attachment isn't versioned, then zero
   * value is returned.
   * @return the major version part of the document.
   */
  int getMajorVersion();

  /**
   * Gets the version of the document. It is a concatenation of the major and of the minor version
   * with a dot as separator.
   * @return the version of the attachment or "0.0" if the document isn't versioned.
   */
  default String getVersion() {
    return getMajorVersion() + "." + getMinorVersion();
  }

}
