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
package org.silverpeas.core.node.model;

import java.util.Comparator;

/**
 *
 * @author ehugonnet
 */
public class NodeOrderComparator implements Comparator<NodeDetail> {

  @Override
  public int compare(NodeDetail nodeDetail1, NodeDetail nodeDetail2) {
    if(nodeDetail1 == null) {
      if(nodeDetail2 == null) {
        return 0;
      }
      return -1;
    }
    if(nodeDetail2 == null) {
      return 1;
    }
    if(nodeDetail1.getOrder() == nodeDetail2.getOrder()) {
      return nodeDetail1.getName().compareTo(nodeDetail2.getName());
    }
    return nodeDetail1.getOrder() - nodeDetail2.getOrder();
  }

}
