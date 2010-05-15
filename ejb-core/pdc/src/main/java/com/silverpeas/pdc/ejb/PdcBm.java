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

package com.silverpeas.pdc.ejb;

import java.util.*;
import javax.ejb.*;
import java.rmi.RemoteException;

import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;

/**
 * Interface declaration
 * @author neysseri
 */
public interface PdcBm extends EJBObject {
  public ArrayList getDaughters(String axisId, String valueId)
      throws RemoteException;

  public ArrayList getSubAxisValues(String axisId, String valueId)
      throws RemoteException;

  public ArrayList findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List componentIds,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws RemoteException;

  public ArrayList findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List componentIds,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive)
      throws RemoteException;

  public int getSilverContentId(String objectId, String componentId)
      throws RemoteException;

  public ArrayList getPositions(int silverContentId, String componentId)
      throws RemoteException;

  public Value getValue(String axisId, String valueId) throws RemoteException;

  public List getSilverContentIds(List docFeatures) throws RemoteException;

  public String getInternalContentId(int silverContentId)
      throws RemoteException;

  public AxisHeader getAxisHeader(String axisId) throws RemoteException;

  public String createDaughterValueWithId(String axisId, Value value) throws RemoteException,
      PdcException;

  public int addPosition(int pubId, ClassifyPosition position, String componentId,
      boolean alertSubscribers) throws RemoteException, PdcException;

}