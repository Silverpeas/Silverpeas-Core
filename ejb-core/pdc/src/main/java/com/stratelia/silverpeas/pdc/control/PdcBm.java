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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.pdc.control;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;

import com.stratelia.silverpeas.containerManager.ContainerManagerException;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;

/*
 * CVS Informations
 * 
 * $Id: PdcBm.java,v 1.11 2008/09/01 07:36:57 neysseri Exp $
 * 
 * $Log: PdcBm.java,v $
 * Revision 1.11  2008/09/01 07:36:57  neysseri
 * no message
 *
 * Revision 1.10.2.3  2008/08/08 12:45:48  neysseri
 * no message
 *
 * Revision 1.10.2.2  2008/06/23 15:11:47  schevance
 * v0.4 debut gestion manager axe mais buggé
 *
 * Revision 1.10.2.1  2008/06/12 15:33:16  schevance
 * V0.2 mais sans les droits : bug sur la barre de progression
 * TODO modifier l'order
 *
 * Revision 1.10  2008/03/13 08:11:01  neysseri
 * no message
 *
 * Revision 1.9  2007/01/29 08:24:16  neysseri
 * Persistence du classement PDC lors d'un copier/coller de publication
 *
 * Revision 1.8  2005/04/14 18:13:21  neysseri
 * no message
 *
 * Revision 1.7  2005/02/28 10:24:44  neysseri
 * no message
 *
 * Revision 1.6  2004/06/22 15:19:25  neysseri
 * nettoyage eclipse
 *
 * Revision 1.5  2004/01/05 09:41:52  neysseri
 * no message
 *
 * Revision 1.4  2003/10/11 02:09:46  neysseri
 * Enhancement for taglib (DIAE Project)
 *
 * Revision 1.3  2002/10/28 16:09:19  neysseri
 * Branch "InterestCenters" merging
 *
 * Revision 1.2  2002/10/17 13:33:21  neysseri
 * Glossary report from VSIC to KMedition
 *
 * Revision 1.1.1.1.10.1  2002/10/28 11:10:10  gshakirin
 * no message
 *
 * Revision 1.29  2002/06/10 13:10:29  nchaix
 * MEC02
 *
 * Revision 1.28.16.1  2002/05/31 08:33:29  neysseri
 * Ajout de la méthode getAxisHeader()
 *
 * Revision 1.28  2002/04/18 14:29:36  neysseri
 * Prise en compte des transactions depuis le pdcPeas sur les fonctions de suppression
 *
 * Revision 1.27  2002/04/09 12:27:51  cbonin
 * Ajout de la methode public List getDaughterValues(String axisId, String valueId)
 *
 * Revision 1.26  2002/04/04 13:10:06  santonio
 * Tient compte de la recherche global (PDC + Classique)
 * Généralisation de certaines méthodes
 *
 * Revision 1.25  2002/03/29 09:41:07  cbonin
 * Ajout de la methode :
 *
 * public Value getRoot(String axisId)
 *
 * Revision 1.24  2002/03/28 09:16:37  cbonin
 * Ajout de la methode :
 * public List getAxisValuesByName(String valueName)
 *
 * Revision 1.23  2002/03/20 13:33:33  santonio
 * no message
 *
 * Revision 1.22  2002/03/14 09:33:27  nchaix
 * no message
 *
 * Revision 1.21  2002/03/08 13:42:25  santonio
 * no message
 *
 * Revision 1.20  2002/03/08 11:52:38  santonio
 * fonctionnalités sur les invariances dans le metier
 *
 * Revision 1.19  2002/03/05 14:58:01  neysseri
 * no message
 *
 * Revision 1.18  2002/03/05 12:51:30  neysseri
 * no message
 *
 * Revision 1.17  2002/03/04 08:34:19  nchaix
 * no message
 *
 * Revision 1.16  2002/03/01 16:31:28  neysseri
 * no message
 *
 * Revision 1.15  2002/02/28 11:09:25  santonio
 * no message
 *
 * Revision 1.14  2002/02/27 19:59:18  neysseri
 * no message
 *
 * Revision 1.13  2002/02/27 16:42:05  neysseri
 * gestion des transactions
 *
 * Revision 1.12  2002/02/26 18:48:32  neysseri
 * no message
 *
 * Revision 1.11  2002/02/22 17:13:17  neysseri
 * no message
 *
 * Revision 1.10  2002/02/22 11:59:59  santonio
 * no message
 *
 * Revision 1.9  2002/02/21 18:38:08  neysseri
 * no message
 *
 * Revision 1.8  2002/02/19 17:16:44  neysseri
 * jindent + javadoc
 *
 */

/**
 * Interface declaration
 * 
 * 
 * @author
 */
public interface PdcBm {

  /**
   * *********************************************************
   */
  /* Methods used by the use case 'definition of the taxinomy' */

  /**
   * *********************************************************
   */

  public List getAxisByType(String type) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public List getAxis() throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param type
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int getNbAxisByType(String type) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int getNbAxis() throws PdcException;

  public int getNbMaxAxis() throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param axisHeader
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int createAxis(AxisHeader axisHeader) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param axisHeader
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int updateAxis(AxisHeader axisHeader) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param axisId
   * 
   * @throws PdcException
   * 
   * @see
   */
  public void deleteAxis(Connection con, String axisId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param axisId
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public Axis getAxisDetail(String axisId) throws PdcException;

  public Axis getAxisDetail(String axisId, AxisFilter filter)
      throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param axisId
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public AxisHeader getAxisHeader(String axisId) throws PdcException;

  public Value getValue(String axisId, String valueId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param valueId
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public Value getAxisValue(String valueId, String treeId) throws PdcException;

  /**
   * Return a list of axis values having the value name in parameter
   * 
   * 
   * @param valueName
   * 
   * @return List
   * 
   * @throws PdcException
   * 
   * @see
   */
  public List getAxisValuesByName(String valueName) throws PdcException;

  /**
   * Return a list of String corresponding to the valueId of the value in
   * parameter
   * 
   * 
   * @param axisId
   * @param valueId
   * 
   * @return List
   * 
   * @throws PdcException
   * 
   * @see
   */
  public List getDaughterValues(String axisId, String valueId)
      throws PdcException;

  /**
   * Return a list of String corresponding to the valueId of the value in
   * parameter
   * 
   * 
   * @param axisId
   * @param valueId
   * 
   * @return List
   * 
   * @throws PdcException
   * 
   * @see
   */
  public List getFilteredAxisValues(String rootId, AxisFilter filter)
      throws PdcException;

  /**
   * Return the Value corresponding to the axis done
   * 
   * 
   * @param axisId
   * 
   * @return Value
   * 
   * @throws PdcException
   * 
   * @see
   */
  public Value getRoot(String axisId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param valueToInsert
   * @param refValue
   * @param axisId
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int insertMotherValue(Value valueToInsert, String refValue,
      String axisId) throws PdcException;

  /**
   * 
   * Déplace une valeur et ses sous-valeurs sous un nouveau père
   * 
   * @param axis
   * @param valueToMove
   * @param newFatherId
   * @return 1 si valeur soeur de même nom
   * @throws PdcException
   */
  public int moveValueToNewFatherId(Axis axis, Value valueToMove,
      String newFatherId, int orderNumber) throws PdcException;

  /**
   * 
   * retourne les droits sur la valeur
   * 
   * @param current
   *          value
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  public List getManagers(String axisId, String valueId) throws PdcException;

  public boolean isUserManager(String userId) throws PdcException;

  /**
   * 
   * retourne les droits hérités sur la valeur
   * 
   * @param current
   *          value
   * @return ArrayList( ArrayList UsersId, ArrayList GroupsId)
   * @throws PdcException
   */
  public List getInheritedManagers(Value value) throws PdcException;

  /**
   * 
   * met à jour les droits sur la valeur
   * 
   * @param ArrayList
   *          ( ArrayList UsersId, ArrayList GroupsId), current value
   * @return
   * @throws PdcException
   */
  public void setManagers(List userIds, List groupIds, String axisId,
      String valueId) throws PdcException;

  /**
   * 
   * supprime tous les droits sur la valeur
   * 
   * @param current
   *          value
   * @return
   * @throws PdcException
   */
  public void razManagers(String axisId, String valueId) throws PdcException;

  public void deleteManager(String userId) throws PdcException;

  public void deleteGroupManager(String groupId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param valueToInsert
   * @param refValue
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int createDaughterValue(Value valueToInsert, String refValue,
      String treeId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param value
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int updateValue(Value value, String treeId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param valueId
   * 
   * @throws PdcException
   * 
   * @see
   */
  public void deleteValueAndSubtree(Connection con, String valueId,
      String axisId, String treeId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param valueId
   * 
   * @throws PdcException
   * 
   * @see
   */
  public String deleteValue(Connection con, String valueId, String axisId,
      String treeId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param valueId
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public List getFullPath(String valueId, String treeId) throws PdcException;

  /**
   * ****************************************************************
   */
  /* Methods used by the use case 'settings of using of the taxinomy' */

  /**
   * ****************************************************************
   */

  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param instanceId
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public List getUsedAxisByInstanceId(String instanceId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param usedAxis
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int addUsedAxis(UsedAxis usedAxis) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param usedAxis
   * 
   * @return
   * 
   * @throws PdcException
   * 
   * @see
   */
  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param usedAxisId
   * 
   * @throws PdcException
   * 
   * @see
   */
  public void deleteUsedAxis(String usedAxisId) throws PdcException;

  /**
   * Method declaration
   * 
   * 
   * @param usedAxisIds
   * 
   * @throws PdcException
   * 
   * @see
   */
  public void deleteUsedAxis(Collection usedAxisIds) throws PdcException;

  public List getUsedAxisToClassify(String instanceId, int silverObjectId)
      throws PdcException;

  public int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId) throws PdcException;

  public int addPosition(int silverObjectId, ClassifyPosition position,
      String sComponentId, boolean alertSubscribers) throws PdcException;

  public int updatePosition(ClassifyPosition position, String instanceId,
      int silverObjectId) throws PdcException;

  public int updatePosition(ClassifyPosition position, String instanceId,
      int silverObjectId, boolean alertSubscribers) throws PdcException;

  public void deletePosition(int positionId, String sComponentId)
      throws PdcException;

  public void copyPositions(int fromObjectId, String fromInstanceId,
      int toObjectId, String toInstanceId) throws PdcException;

  public List getPositions(int silverObjectId, String sComponentId)
      throws PdcException;

  public boolean isClassifyingMandatory(String componentId) throws PdcException;

  /** Search methods */
  public List getPertinentAxis(SearchContext searchContext, String axisType)
      throws PdcException;

  public List getPertinentAxisByInstanceId(SearchContext searchContext,
      String axisType, String instanceId) throws PdcException;

  public List getPertinentAxisByInstanceId(SearchContext searchContext,
      String axisType, String instanceId, AxisFilter filter)
      throws PdcException;

  public List getPertinentAxisByInstanceIds(SearchContext searchContext,
      String axisType, List instanceIds) throws PdcException;

  public List getPertinentAxisByInstanceIds(SearchContext searchContext,
      String axisType, List instanceIds, AxisFilter filter) throws PdcException;

  // public List getFirstLevelAxisValues(SearchContext searchContext, String
  // axisId) throws PdcException;

  public List getFirstLevelAxisValuesByInstanceId(SearchContext searchContext,
      String axisId, String instanceId) throws PdcException;

  public List getFirstLevelAxisValuesByInstanceIds(SearchContext searchContext,
      String axisId, List instanceIds) throws PdcException;

  // recherche globale
  // public List getPertinentDaughterValues(SearchContext searchContext, String
  // axisId, String valueId) throws PdcException;

  // recherche à l'intérieur d'une instance
  public List getPertinentDaughterValuesByInstanceId(
      SearchContext searchContext, String axisId, String valueId,
      String instanceId) throws PdcException;

  public List getPertinentDaughterValuesByInstanceId(
      SearchContext searchContext, String axisId, String valueId,
      String instanceId, AxisFilter filter) throws PdcException;

  public List getPertinentDaughterValuesByInstanceIds(
      SearchContext searchContext, String axisId, String valueId,
      List instanceIds) throws PdcException;

  public List getPertinentDaughterValuesByInstanceIds(
      SearchContext searchContext, String axisId, String valueId,
      List instanceIds, AxisFilter filter) throws PdcException;

  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId,
      String authorId, String afterDate, String beforeDate)
      throws ContainerManagerException;

  /** Find all the SilverContentId with the given position */
  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws ContainerManagerException;

  /** Find all the SilverContentId with the given position */
  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId)
      throws ContainerManagerException;

  public List findSilverContentIdByPosition(
      ContainerPositionInterface containerPosition, List alComponentId,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws ContainerManagerException;

  public List getDaughters(String refValue, String treeId);

  public List getSubAxisValues(String axisId, String valueId);

  public void indexAllAxis() throws PdcException;

}