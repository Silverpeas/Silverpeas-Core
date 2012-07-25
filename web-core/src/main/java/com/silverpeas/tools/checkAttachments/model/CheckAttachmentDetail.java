/**
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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.silverpeas.tools.checkAttachments.model;

public class CheckAttachmentDetail {
  public long getAttachmentId() {
    return attachmentId;
  }

  public void setAttachmentId(long attachmentId) {
    this.attachmentId = attachmentId;
  }

  public String getLogicalName() {
    return logicalName;
  }

  public void setLogicalName(String logicalName) {
    this.logicalName = logicalName;
  }

  public String getPhysicalName() {
    return physicalName;
  }

  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSpaceLabel() {
    return spaceLabel;
  }

  public void setSpaceLabel(String spaceLabel) {
    this.spaceLabel = spaceLabel;
  }

  public String getComponentLabel() {
    return componentLabel;
  }

  public void setComponentLabel(String componentLabel) {
    this.componentLabel = componentLabel;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getPublicationPath() {
    return publicationPath;
  }

  public void setPublicationPath(String publicationPath) {
    this.publicationPath = publicationPath;
  }

  public String getActionsDate() {
    return actionsDate;
  }

  public void setActionsDate(String actionsDate) {
    this.actionsDate = actionsDate;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  private long attachmentId;
  private String logicalName = null;
  private String physicalName = null;
  private long size;
  private String title = null;
  private String spaceLabel = null;
  private String componentLabel = null;
  private String context = null;
  private String publicationPath = null;
  private String actionsDate = null;
  private String status = null;
  private String path = null;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

}