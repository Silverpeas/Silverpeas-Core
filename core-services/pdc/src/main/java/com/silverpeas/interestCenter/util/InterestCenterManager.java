/**
 * Copyright (C) 2000 - 2013 Silverpeas
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

package com.silverpeas.interestCenter.util;

import java.util.List;

import com.silverpeas.interestCenter.control.InterestCenterService;
import com.silverpeas.interestCenter.model.InterestCenter;

import org.silverpeas.util.ServiceProvider;

import javax.inject.Inject;

public class InterestCenterManager {

  public static final InterestCenterManager getInstance() {
    return ServiceProvider.getService(InterestCenterManager.class);
  }

  @Inject
  private InterestCenterService interestCenterService;

  protected InterestCenterManager() {

  }

  /**
   * Method getICByUserId returns ArrayList of all InterestCenter objects for user given by userId
   */
  public List<InterestCenter> getICByUserId(int userId)  {
    return interestCenterService.getICByUserID(userId);
  }

  /**
   * Method getICByPK returns Interest Center given by id
   */
  public InterestCenter getICByID(int id)  {
    return interestCenterService.getICByID(id);
  }

  /**
   * Method isICExists returns true if InterstCenter with given name is already exists, false in
   * other case
   */

  public int isICExists(String nameIC, int userId)  {
    List<InterestCenter> icList = getICByUserId(userId);
    for (InterestCenter ic : icList) {
      if (nameIC.equals(ic.getName())) {
        return ic.getId();
      }
    }
    return -1;
  }

  /**
   * Method createIC creates new InterestCenter
   */
  public int createIC(InterestCenter icToCreate)  {
    int id = isICExists(icToCreate.getName(), icToCreate.getOwnerID());
    if (id != -1) {
      interestCenterService.removeICByPK(id);
    }
    return interestCenterService.createIC(icToCreate);
  }

}
