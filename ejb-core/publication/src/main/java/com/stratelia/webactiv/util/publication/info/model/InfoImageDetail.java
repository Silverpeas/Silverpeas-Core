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
import java.util.Map;

import com.stratelia.webactiv.util.FileServerUtils;

public class InfoImageDetail extends InfoAttachmentDetail implements Serializable {

  private static final long serialVersionUID = -7862895523231381570L;

  public InfoImageDetail(InfoPK infoPK, String order, String id,
      String physicalName, String logicalName, String description, String type,
      long size) {
    super(infoPK, order, id, physicalName, logicalName, description, type, size);
  }

  public Map<String, String> getMappedUrl() {
    return FileServerUtils.getMappedUrl(getPK().getSpace(), getPK()
        .getComponentName(), getLogicalName(), getPhysicalName(), getType(),
        "images");
  }

  public String getWebURL() {
    return FileServerUtils.getWebUrl(getPK().getSpace(), getPK()
        .getComponentName(), getLogicalName(), getPhysicalName(), getType(),
        "images");
  }

  public String getUrl(String serverNameAndPort) {
    return serverNameAndPort
        + FileServerUtils.getUrl(getPK().getSpace(),
        getPK().getComponentName(), getLogicalName(), getPhysicalName(),
        getType(), "images");
  }

}