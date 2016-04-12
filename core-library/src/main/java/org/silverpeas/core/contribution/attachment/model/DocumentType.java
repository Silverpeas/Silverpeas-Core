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
package org.silverpeas.core.contribution.attachment.model;

/**
 *
 * @author ehugonnet
 */
public enum DocumentType {

  attachment("attachments"), form("forms"), wysiwyg("wysiwyg"), image("images"), video("video"),
  node("node");
  private String folderName;

  private DocumentType(String folder) {
    this.folderName = folder;
  }

  public String getFolderName() {
    return folderName;
  }

  public static DocumentType fromFolderName(String folder) {
    if (attachment.folderName.equals(folder)) {
      return attachment;
    }
    if (form.folderName.equals(folder)) {
      return form;
    }
    if (wysiwyg.folderName.equals(folder)) {
      return wysiwyg;
    }
    if (image.folderName.equals(folder)) {
      return image;
    }
    if (video.folderName.equals(folder)) {
      return video;
    }
    return attachment;
  }

  public String getName() {
    return this.name();
  }

  public static DocumentType decode(final String name) {
    if (name != null) {
      for (DocumentType documentType : DocumentType.values()) {
        if (documentType.name().equals(name)) {
          return documentType;
        }
      }
    }
    return null;
  }
}
