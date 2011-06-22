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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.webactiv.util.subscribe.control;

import com.stratelia.webactiv.util.node.model.NodePK;
import java.util.Collection;

/**
 * Interface declaration
 * @author
 */
public interface SubscribeBm {

  /**
   * addPublicationInNode() This method has to be called each time a publication is added in a node.
   * This will enable to send email if some users are subscribers of this node.
   * @param userId
   * @param node
   */
  public void addSubscribe(String userId, NodePK node);

  /**
   * Method declaration
   * @param userId
   * @param node
   */
  public void removeSubscribe(String userId, NodePK node);

  /**
   * Method declaration
   * @param userId
   * @see
   */
  public void removeUserSubscribes(String userId);

  /**
   * Method declaration
   * @param node
   * @param path
   * @see
   */
  public void removeNodeSubscribes(NodePK node, String path);

  /**
   * Method declaration
   * @param userId
   * @return
   * @see
   */
  public Collection<NodePK> getUserSubscribePKs(String userId);
  
  /**
   * Method declaration
   * @param userId
   * @param componentName
   * @return
   * @see
   */
  public Collection<NodePK> getUserSubscribePKsByComponent(String userId, String componentName);

  /**
   * Method declaration
   * @param node
   * @return a Collection of userId
   * @see
   */
  public Collection<String> getNodeSubscriberDetails(NodePK node);

  /**
   * Method declaration
   * @param nodePKs
   * @return a Collection of userId
   * @see
   */
  public Collection<String> getNodeSubscriberDetails(Collection<NodePK> nodePKs);
}
