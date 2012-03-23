/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.profile.web.mock;

import com.silverpeas.socialNetwork.relationShip.RelationShip;
import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Named;

/**
 * A mock of the RelationShipService service dedicated to tests.
 */
@Named("relationShipService")
public class MockedRelationShipService extends RelationShipService {
  
  private List<RelationShip> relationShips = new ArrayList<RelationShip>();
  
  public void setRelationShipsBetween(String userId, final UserDetail ... users) {
    int userId1 = Integer.valueOf(userId);
    for (UserDetail userDetail : users) {
      int userId2 = Integer.valueOf(userDetail.getId());
      relationShips.add(new RelationShip(userId1, userId2, 0, new Date(), userId1));
    }
  }

  @Override
  public List<RelationShip> getAllMyRelationShips(int myId) throws SQLException {
    return relationShips;
  }
  
}
