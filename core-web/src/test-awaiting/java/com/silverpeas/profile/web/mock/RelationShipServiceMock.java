/*
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.profile.web.mock;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.socialnetwork.relationShip.RelationShip;
import org.silverpeas.core.socialnetwork.relationShip.RelationShipService;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.inject.Named;
import static org.mockito.Mockito.mock;

/**
 * A mock of the RelationShipService service dedicated to tests managed by the IoC container.
 * It is yet a wrapper around the RelationShipService mock to which are delagated the methods
 * invocations. The wrapped mock can be obtained to record behaviours expected by tests.
 */
@Named("relationShipService")
public class RelationShipServiceMock extends RelationShipService {

  private RelationShipService mock;

  public RelationShipServiceMock() {
    mock = mock(RelationShipService.class);
  }

  public RelationShipService getMockedRelationShipService() {
    return mock;
  }

  @Override
  public boolean removeRelationShip(int idUser1, int idUser2) {
    return mock.removeRelationShip(idUser1, idUser2);
  }

  @Override
  public boolean isInRelationShip(int user1Id, int user2Id) throws SQLException {
    return mock.isInRelationShip(user1Id, user2Id);
  }

  @Override
  public RelationShip getRelationShip(int user1Id, int user2Id) throws SQLException {
    return mock.getRelationShip(user1Id, user2Id);
  }

  @Override
  public List<String> getMyContactsIds(int myId) throws SQLException {
    return mock.getMyContactsIds(myId);
  }

  @Override
  public List<SocialInformation> getAllRelationShipsOfMyContact(String myId,
          List<String> myContactsIds, Date begin, Date end) throws SQLException {
    return mock.getAllRelationShipsOfMyContact(myId, myContactsIds, begin, end);
  }

  @Override
  public List<SocialInformation> getAllMyRelationShips(String userId, Date begin, Date end) throws SQLException {
    return mock.getAllMyRelationShips(userId, begin, end);
  }

  @Override
  public List<RelationShip> getAllMyRelationShips(int myId) throws SQLException {
    return mock.getAllMyRelationShips(myId);
  }

  @Override
  public List<String> getAllCommonContactsIds(int user1Id, int user2Id) throws SQLException {
    return mock.getAllCommonContactsIds(user1Id, user2Id);
  }
}
