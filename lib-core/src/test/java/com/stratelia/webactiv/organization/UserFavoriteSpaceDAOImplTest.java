/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.stratelia.webactiv.organization;

import com.silverpeas.components.model.AbstractTestDao;
import java.util.List;


import org.junit.Test;

public class UserFavoriteSpaceDAOImplTest extends AbstractTestDao {

  @Test
  public void testGetListUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(1, listUFS.size());
  }

  @Test
  public void testAddUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    UserFavoriteSpaceVO ufsVO = new UserFavoriteSpaceVO(0, 2);
    boolean result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(true, result);

    // Check the new records inside database
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(2, listUFS.size());

    // Check database constraint on existing userid and space id
    ufsVO = new UserFavoriteSpaceVO(10, 10);
    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(false, result);

    // Check default userFavoriteSpaceVO 
    ufsVO = new UserFavoriteSpaceVO();
    assertEquals(-1, ufsVO.getSpaceId());
    assertEquals(-1, ufsVO.getUserId());
    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(false, result);
  }

  @Test
  public void testRemoveUserFavoriteSpace() {
    UserFavoriteSpaceDAOImpl ufsDAO = new UserFavoriteSpaceDAOImpl();
    UserFavoriteSpaceVO ufsVO = new UserFavoriteSpaceVO(0, 2);
    boolean result = ufsDAO.removeUserFavoriteSpace(ufsVO);
    assertEquals(true, result);

    // Check result
    List<UserFavoriteSpaceVO> listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(1, listUFS.size());

    result = ufsDAO.addUserFavoriteSpace(ufsVO);
    assertEquals(true, result);

    // Delete all favorite space of current user
    ufsDAO.removeUserFavoriteSpace(new UserFavoriteSpaceVO(0, -1));
    listUFS = ufsDAO.getListUserFavoriteSpace("0");
    assertEquals(0, listUFS.size());
  }

  @Override
  protected String getDatasetFileName() {
    return "test-favoritespace-dataset.xml";
  }
  
}