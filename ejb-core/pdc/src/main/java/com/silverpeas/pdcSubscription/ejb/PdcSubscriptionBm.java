/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

/*
 * Aliaksei_Budnikau
 * Date: Oct 24, 2002
 */
package com.silverpeas.pdcSubscription.ejb;

import java.util.List;

import javax.ejb.Local;

import com.silverpeas.pdcSubscription.model.PDCSubscription;

import com.stratelia.silverpeas.classifyEngine.Value;

@Local
public interface PdcSubscriptionBm {

  /**
   * @param userId 
   * @return a list of <code>PDCSubscriptions</code> finded by id provided
   */
  public List<PDCSubscription> getPDCSubscriptionByUserId(int userId);

  public PDCSubscription getPDCSubsriptionById(int id);

  /**
   * @param subscription 
   * @return new autogenerated PDCSubscription id
   */
  public int createPDCSubscription(PDCSubscription subscription);

  public void updatePDCSubscription(PDCSubscription subscription);

  public void removePDCSubscriptionById(int id);

  public void removePDCSubscriptionById(int[] ids);

  /**
   * This method check is any subscription that match criterias provided and sends notification if
   * succeed
   *
   * @param classifyValues Linst of ClassifyValues to be checked
   * @param componentId component where classify event occures
   * @param silverObjectid object that was classified
   */
  public void checkSubscriptions(List<? extends Value> classifyValues, String componentId,
      int silverObjectid);

  /**
   * Implements PDCSubscription check for value deletion. It deletes all references to the path
   * containing this value from PDCSubscription module DB
   *
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   * @param oldPath old path that would be removed soon
   * @param newPath new path. That will be places instead of old for this axis
   * @param pathInfo should contains PdcBm.getFullPath data structure
   */
  public void checkValueOnDelete(int axiId, String axisName, List<String> oldPath,
      List<String> newPath, List<com.stratelia.silverpeas.pdc.model.Value> pathInfo);

  /**
   * Implements PDCSubscription check for axis deletion. It deletes all references to this axis from
   * PDCSubscription module DB
   *
   * @param axisId the axis to be checked
   * @param axisName the name of the axis
   */
  public void checkAxisOnDelete(int axisId, String axisName);
}