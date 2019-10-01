/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authorization;

import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.ServiceProvider;

/**
 * This interface extends access controller for a Node resource.
 * @author Yohann Chastagnier
 */
public interface NodeAccessControl extends AccessController<NodePK>{

  static NodeAccessControl get() {
    return ServiceProvider.getService(NodeAccessControl.class);
  }

  /**
   * Is the specified group authorized to access the given node with at least read
   * privileges? The roles of the group on the node aren't taken into account. The group
   * should have at least the user role to access the node unless the node is
   * public.
   * @param groupId the unique identifier of a group.
   * @param nodePK the unique identifier of a node in the data source.
   * @return true if the group can access the given node, false otherwise.
   */
  boolean isGroupAuthorized(final String groupId, final NodePK nodePK);
}
