/*
 * Copyright (C) 2000 - 2012 Silverpeas
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
package org.silverpeas.attachment.model;

import static org.silverpeas.attachment.model.UnlockOption.*;

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
}
