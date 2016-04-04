/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.contribution.contentcontainer.container;

import java.sql.Connection;
import java.util.*;

/**
 * The interface for all the containers (PDC, ..) Every container have to implement this interface
 * and declare it in the containerDescriptor (xml)
 */
public interface ContainerInterface {
  /**
   * Return the parameters for the HTTP call on the classify
   */
  public String getCallParameters(String sComponentId, String sSilverContentId);

  /**
   * Remove all the positions of the given content
   */
  public List<Integer> removePosition(Connection connection, int nSilverContentId)
      throws ContainerManagerException;

  /**
   * Find the search Context for the given SilverContentId
   */
  public ContainerPositionInterface getSilverContentIdSearchContext(int nSilverContentId,
      String sComponentId) throws ContainerManagerException;

  /**
   * Find all the SilverContentId with the given position
   */
  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
      List<String> alComponentId) throws ContainerManagerException;

  public List<Integer> findSilverContentIdByPosition(ContainerPositionInterface containerPosition,
      List<String> alComponentId, String authorId, String afterDate, String beforeDate)
      throws ContainerManagerException;
}