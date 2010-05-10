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

package com.stratelia.silverpeas.pdc.control;

import java.util.List;
import java.util.Collection;
import java.sql.Connection;

import com.stratelia.silverpeas.pdc.model.*;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;

/*
 * CVS Informations
 *
 * $Id: PdcUtilizationBm.java,v 1.3 2002/10/28 16:09:19 neysseri Exp $
 *
 * $Log: PdcUtilizationBm.java,v $
 * Revision 1.3  2002/10/28 16:09:19  neysseri
 * Branch "InterestCenters" merging
 *
 *
 * Revision 1.2  2002/10/17 13:33:21  neysseri
 * Glossary report from VSIC to KMedition
 *
 * Revision 1.1.1.1  2002/08/06 14:47:52  nchaix
 * no message
 *
 * Revision 1.9  2002/04/04 13:10:06  santonio
 * Tient compte de la recherche global (PDC + Classique)
 * Généralisation de certaines méthodes
 *
 * Revision 1.8  2002/03/05 12:51:30  neysseri
 * no message
 *
 * Revision 1.7  2002/03/01 16:31:28  neysseri
 * no message
 *
 * Revision 1.6  2002/02/28 16:06:28  neysseri
 * no message
 *
 * Revision 1.5  2002/02/27 16:42:05  neysseri
 * gestion des transactions
 *
 * Revision 1.4  2002/02/22 12:01:13  santonio
 * no message
 *
 * Revision 1.3  2002/02/21 18:33:07  santonio
 * no message
 *
 * Revision 1.2  2002/02/19 17:16:44  neysseri
 * jindent + javadoc
 *
 */

/**
 * Interface declaration
 * @author
 */
public interface PdcUtilizationBm {

  /**
   * Returns data of an used axis defined by usedAxisId
   * @param usedAxisId - id of the usedAxis
   * @return an UsedAxis
   * @throws PdcException
   */
  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Returns all the axis used by a given Job'Peas instance
   * @param instanceId - the id of the Job'Peas
   * @return a List of UsedAxis
   * @throws PdcException
   */
  public List<UsedAxis> getUsedAxisByInstanceId(String instanceId) throws PdcException;

  /**
   * Returns the distinct axis used by a given Job'Peas instance
   * @param instanceId - the id of the Job'Peas
   * @return a List of AxisHeader
   * @throws PdcException
   */
  public List<AxisHeader> getAxisHeaderUsedByInstanceId(String instanceId)
      throws PdcException;

  public List<AxisHeader> getAxisHeaderUsedByInstanceIds(List<String> instanceIds)
      throws PdcException;

  public List<AxisHeader> getAxisHeaderUsedByInstanceIds(List<String> instanceIds, AxisFilter filter)
      throws PdcException;

  /**
   * Add an UsedAxis
   * @param usedAxis - the UsedAxis to add
   * @return - 0 si, pour une même instance de Job'Peas, il n'existe pas déjà un axe avec comme
   * valeur de base un ascendant ou un descendant - 1 sinon
   * @throws PdcException
   */
  public int addUsedAxis(UsedAxis usedAxis, String treeId) throws PdcException;

  /**
   * Update an UsedAxis
   * @param usedAxis - the UsedAxis to update
   * @return - 0 si, pour une même instance de Job'Peas, il n'existe pas déjà un axe avec comme
   * valeur de base un ascendant ou un descendant - 1 sinon
   * @throws PdcException
   */
  public int updateUsedAxis(UsedAxis usedAxis, String treeId)
      throws PdcException;

  /**
   * Delete an used axis
   * @param usedAxisId - the id of the used axis to delete
   * @throws PdcException
   * @see
   */
  public void deleteUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Delete a collection of used axis
   * @param usedAxisIds - the ids of the used axis to delete
   * @throws PdcException
   * @see
   */
  public void deleteUsedAxis(Collection<String> usedAxisIds) throws PdcException;

  /**
   * Delete used axis based on a particular axis
   * @param axisId - the axis id
   * @throws PdcException
   * @see
   */
  public void deleteUsedAxisByAxisId(Connection con, String axisId)
      throws PdcException;

  public void deleteUsedAxisByMotherValue(Connection con, String valueId,
      String axisId, String treeId) throws PdcException;

  public void updateOrDeleteBaseValue(Connection con, int baseValueToUpdate,
      int newBaseValue, int axisId, String treeId) throws PdcException;

}