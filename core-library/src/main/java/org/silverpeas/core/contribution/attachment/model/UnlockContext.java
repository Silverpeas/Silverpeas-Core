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
package org.silverpeas.core.contribution.attachment.model;

import static org.silverpeas.core.contribution.attachment.model.UnlockOption.*;

/**
 *
 * @author ehugonnet
 */
public class UnlockContext {

  private int options = 0;
  private String attachmentId;
  private String userId;
  private String comment;
  private String lang;


  public UnlockContext(String attachmentId, String userId, String lang) {
    this.attachmentId = attachmentId;
    this.userId = userId;
    this.lang = lang;
    this.comment = "";
  }

  public UnlockContext(String attachmentId, String userId, String lang, String comment) {
    this.attachmentId = attachmentId;
    this.userId = userId;
    this.lang = lang;
    this.comment = comment;
  }

  public void addOption(UnlockOption option) {
    options = option.addOption(options);
  }

  public void removeOption(UnlockOption option) {
    options = option.removeOption(options);
  }

  public boolean isPublicVersion() {
    return !PRIVATE_VERSION.isSelected(options);
  }

  public boolean isForce() {
    return FORCE.isSelected(options);
  }

  public boolean isUpload() {
    return UPLOAD.isSelected(options);
  }

  public boolean isWebdav() {
    return WEBDAV.isSelected(options);
  }

  public boolean isPrivateVersion() {
    return PRIVATE_VERSION.isSelected(options);
  }

  public String getAttachmentId() {
    return attachmentId;
  }

  public String getUserId() {
    return userId;
  }

  public String getLang() {
    return lang;
  }

  public String getComment() {
    return comment;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 29 * hash + this.options;
    hash = 29 * hash + (this.attachmentId != null ? this.attachmentId.hashCode() : 0);
    hash = 29 * hash + (this.userId != null ? this.userId.hashCode() : 0);
    hash = 29 * hash + (this.comment != null ? this.comment.hashCode() : 0);
    hash = 29 * hash + (this.lang != null ? this.lang.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UnlockContext other = (UnlockContext) obj;
    if (this.options != other.options) {
      return false;
    }
    if ((this.attachmentId == null) ? (other.attachmentId != null)
        : !this.attachmentId.equals(other.attachmentId)) {
      return false;
    }
    if ((this.userId == null) ? (other.userId != null) : !this.userId.equals(other.userId)) {
      return false;
    }
    if ((this.comment == null) ? (other.comment != null) : !this.comment.equals(other.comment)) {
      return false;
    }
    if ((this.lang == null) ? (other.lang != null) : !this.lang.equals(other.lang)) {
      return false;
    }
    return true;
  }
}
