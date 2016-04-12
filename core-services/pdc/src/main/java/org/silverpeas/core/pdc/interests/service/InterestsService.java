/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.pdc.interests.service;

import org.silverpeas.core.pdc.interests.model.Interests;

import java.util.List;

/**
 * Transverse business service on the interests' users.
 */
public interface InterestsService {

  /**
   * @param userID the user identifier
   * @return a list of <code>Interests</code>s by user id provided
   */
  public List<Interests> getInterestsByUserId(int userID);

  /**
   * @param id <code>Interests</code> identifier
   * @return Interests by its id
   */
  public Interests getInterestsById(int id);

  /**
   * @param interests interest center to create
   * @return id of <code>Interests</code> created
   */
  public int createInterests(Interests interests);

  /**
   * perform updates of provided Interests
   * @param interests interest center to update
   */
  public void updateInterests(Interests interests);

  /**
   * @param ids ArrayList of <code>java.lang.Integer</code> - id's of <code>Interests</code>s
   * to be deleted
   * @param userId - current user Id
   */
  public void removeInterestsById(List<Integer> ids, String userId);

  /**
   * @param id an id of <code>Interests</code> to be deleted
   */
  public void removeInterestsById(int id);
}
