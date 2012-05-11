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

package com.stratelia.webactiv.util.node.control;

import com.silverpeas.node.notification.NodeNotificationService;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.node.ejb.NodeDAO;
import com.stratelia.webactiv.util.node.ejb.NodeI18NDAO;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Process of deleting a node. As the deletion of a given node is performed by different parts of
 * code in the Silverpeas Node component and each of them in a different context (id est in a single
 * or in a shared transaction), this class is a way to set in a single place the code related to the
 * node deletion whatever the context in which it occurs. It is intended to by used both by the EJB
 * context and by the Silverpeas instanciator context (legacy code that should be later cleaned up).
 */
public class NodeDeletion {

  /**
   * Deletes the specified node and all of its children within the specified data source connection.
   * @param pk the primary key of the father node to delete.
   * @param inConnection the connection to use in the deletion.
   * @param afterDeletion the method to invoke after the deletion of a node.
   */
  public static void deleteNodes(final NodePK pk, final Connection inConnection,
      final AnonymousMethodOnNode afterDeletion) {
    try {
      Collection<NodeDetail> children = NodeDAO.getChildrenDetails(inConnection, pk);
      for (NodeDetail childNode : children) {
        deleteNodes(childNode.getNodePK(), inConnection, afterDeletion);
      }

      deleteNode(pk, inConnection);
      if (afterDeletion != null) {
        afterDeletion.invoke(pk);
      }
    } catch (Exception ex) {
      throw new NodeRuntimeException("NodeBmEJB.removeNode()",
          SilverpeasRuntimeException.ERROR, "node.DELETING_NODE_FAILED",
          "nodeId = " + pk.getId(), ex);
    }
  }

  private static void deleteNode(final NodePK pk, final Connection connection) throws
      SQLException {
    NodeNotificationService notificationService = NodeNotificationService.getService();
    notificationService.notifyOnDeletionOf(pk);

    NodeDAO.deleteRow(connection, pk);
    NodeI18NDAO.removeTranslations(connection, Integer.parseInt(pk.getId()));
    IndexEntryPK indexEntry = new IndexEntryPK(pk.getComponentName(), "Node", pk.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }
}
