/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.pdc.service;

import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

/**
 * A service in the PdC engine to filter the PdC's axis per application instance. It defines a
 * constraining view onto the PdC for a given application instance so that each classification of the
 * contributions in the application is done onto this view of the PdC.
 */
public interface PdcUtilizationService {

  /**
   * Returns data of an used axis defined by usedAxisId
   * @param usedAxisId - id of the usedAxis
   * @return an UsedAxis
   * @throws PdcException
   */
  UsedAxis getUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Returns all the axis used by a given Job'Peas instance
   * @param instanceId - the id of the Job'Peas
   * @return a List of UsedAxis
   * @throws PdcException
   */
  List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException;

  List<AxisHeader> getAxisHeaderUsedByInstanceIds(List<String> instanceIds)
      throws PdcException;

  /**
   * Add an UsedAxis
   * @param usedAxis - the UsedAxis to add
   * @param treeId
   * @return - 0 si, pour une même instance de Job'Peas, il n'existe pas déjà un axe avec comme
   * valeur de base un ascendant ou un descendant - 1 sinon
   * @throws PdcException
   */
  int addUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException;

  /**
   * Update an UsedAxis
   * @param usedAxis - the UsedAxis to update
   * @param treeId
   * @return - 0 si, pour une même instance de Job'Peas, il n'existe pas déjà un axe avec comme
   * valeur de base un ascendant ou un descendant - 1 sinon
   * @throws PdcException
   */
  int updateUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException;

  /**
   * Delete an used axis
   * @param usedAxisId - the id of the used axis to delete
   * @throws PdcException
   *
   */
  void deleteUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Delete a collection of used axis
   * @param usedAxisIds - the ids of the used axis to delete
   * @throws PdcException
   *
   */
  void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException;

  /**
   * Delete used axis based on a particular axis
   * @param con
   * @param axisId - the axis id
   * @throws PdcException
   *
   */
  void deleteUsedAxisByAxisId(Connection con, String axisId) throws PdcException;

  void deleteUsedAxisByMotherValue(Connection con, String valueId, String axisId,
      String treeId) throws PdcException;

  void updateOrDeleteBaseValue(Connection con, int baseValueToUpdate, int newBaseValue,
      int axisId, String treeId) throws PdcException;
}