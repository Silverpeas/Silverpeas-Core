/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.webactiv.util.favorit.control;

import java.util.Collection;

import javax.ejb.*;

import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Interface declaration
 *
 * @author
 * @version %I%, %G%
 */
@Local
public interface FavoritBm {

  /**
   * Method declaration
   *
   * @param userId
   * @param node
   * @
   * @see
   */
  public void addFavoritNode(String userId, NodePK node);

  /**
   * Method declaration
   *
   * @param userId
   * @param node
   * @
   * @see
   */
  public void removeFavoritNode(String userId, NodePK node);

  /**
   * Method declaration
   *
   * @param userId
   * @
   * @see
   */
  public void removeFavoritByUser(String userId);

  /**
   * Method declaration
   *
   * @param node
   * @param path
   * @
   * @see
   */
  public void removeFavoritByNodePath(NodePK node, String path);

  /**
   * Method declaration
   *
   * @param userId
   * @return
   * @
   * @see
   */
  public Collection getFavoritNodePKs(String userId);

  /**
   * Method declaration
   *
   * @param userId
   * @param space
   * @param componentName
   * @return
   * @
   * @see
   */
  public Collection getFavoritNodePKsByComponent(String userId, String componentName);
}
