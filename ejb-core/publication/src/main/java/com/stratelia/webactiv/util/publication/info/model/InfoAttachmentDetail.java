/**
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

public class InfoAttachmentDetail extends InfoItemDetail implements Serializable {

  private static final long serialVersionUID = -6596171390451029798L;
  private String physicalName = null;
  private String logicalName = null;
  private String description = null;
  private String type = null;
  private long size;

  public InfoAttachmentDetail(InfoPK infoPK, String order, String id,
      String physicalName, String logicalName, String description, String type,
      long size) {
    super(infoPK, order, id);
    this.physicalName = physicalName;
    this.logicalName = logicalName;
    this.description = description;
    this.type = type;
    this.size = size;
  }

  public String getPhysicalName() {
    return physicalName;
  }

  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  public String getLogicalName() {
    return logicalName;
  }

  public void setLogicalName(String logicalName) {
    this.logicalName = logicalName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String desc) {
    this.description = desc;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public long getSize() {
    return size;
  }

  public void setSize(long size) {
    this.size = size;
  }

}