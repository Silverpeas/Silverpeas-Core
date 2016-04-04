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

package org.silverpeas.core.pdc.interests.service;

import java.util.List;

import org.silverpeas.core.pdc.interests.model.Interests;

import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;

public class InterestsManager {

  public static final InterestsManager getInstance() {
    return ServiceProvider.getService(InterestsManager.class);
  }

  @Inject
  private InterestsService interestsService;

  protected InterestsManager() {

  }

  /**
   * Method getInterestsByUserId returns ArrayList of all Interests objects for user given by userId
   */
  public List<Interests> getInterestsByUserId(int userId)  {
    return interestsService.getInterestsByUserId(userId);
  }

  /**
   * Method getInterestsByPK returns Interest Center given by id
   */
  public Interests getInterestsById(int id)  {
    return interestsService.getInterestsById(id);
  }

  /**
   * Method isInterestsExists returns true if interests with the given name is already exists,
   * false in other case
   */

  public int isInterestsExists(String interestsName, int userId)  {
    List<Interests> icList = getInterestsByUserId(userId);
    for (Interests ic : icList) {
      if (interestsName.equals(ic.getName())) {
        return ic.getId();
      }
    }
    return -1;
  }

  /**
   * Method createInterests creates new Interests
   */
  public int createInterests(Interests interests)  {
    int id = isInterestsExists(interests.getName(), interests.getOwnerID());
    if (id != -1) {
      interestsService.removeInterestsById(id);
    }
    return interestsService.createInterests(interests);
  }

}
