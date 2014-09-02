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

package com.silverpeas.interestCenter.ejb;

import java.util.List;

import javax.ejb.Local;

import com.silverpeas.interestCenter.model.InterestCenter;

/**
 * InterestCenterBm remote inteface
 */
@Local
public interface InterestCenterBm {

  /**
   * @param userID
   * @return a list of <code>InterestCenter</code>s by user id provided
   */
  public List<InterestCenter> getICByUserID(int userID);

  /**
   * @param icPK <code>InterestCenter</code> id
   * @return InterestCenter by its id
   */
  public InterestCenter getICByID(int icPK);

  /**
   * @param ic
   * @return id of <code>InterestCenter</code> created
   */
  public int createIC(InterestCenter ic);

  /**
   * perform updates of provided InterestCenter
   *
   * @param ic
   */
  public void updateIC(InterestCenter ic);

  /**
   * @param pks ArrayList of <code>java.lang.Integer</code> - id's of <code>InterestCenter</code>s
   * to be deleted
   * @param userId - current user Id
   */
  public void removeICByPK(List<Integer> pks, String userId);

  /**
   * @param pk an id of <code>InterestCenter</code> to be deleted
   */
  public void removeICByPK(int pk);
}
