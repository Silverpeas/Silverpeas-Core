/**
* Copyright (C) 2000 - 2011 Silverpeas
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
* "http://www.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.silverpeas.sharing.security;

import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;

/**
* Node being accessed through some shared object.
*/
public class ShareableNode extends ShareableResource<NodeDetail> {

  public ShareableNode(String token, NodeDetail accessedObject) {
    super(token, accessedObject);
  }

  @Override
  public String getName() {
    if (accessedObject != null) {
      return accessedObject.getName();
    }
    return "";
  }

  @Override
  public String getURL() {
    if (accessedObject != null) {
      return URLManager.getSimpleURL(URLManager.URL_TOPIC, String.valueOf(accessedObject.getId()),
          accessedObject.getNodePK().getInstanceId(), false);
    }
    return "";
  }
}