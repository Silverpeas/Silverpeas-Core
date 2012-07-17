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

package com.silverpeas.socialnetwork.relationShip;

import java.sql.SQLException;
import java.util.List;

import com.silverpeas.calendar.Date;
import com.silverpeas.socialnetwork.SocialNetworkException;
import com.silverpeas.socialnetwork.model.SocialInformation;
import com.silverpeas.socialnetwork.provider.SocialRelationShipsInterface;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * @author Bensalem Nabil
 */
public class SocialRelationShips implements SocialRelationShipsInterface {

  /**
   * Get list of my socialInformationRelationShip (relationShips) according to number of Item and
   * the first Index
   * @param userId
   * @param numberOfElement
   * @param firstIndex
   * @return List
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsList(String userId, Date begin, Date end)
      throws SilverpeasException {
    try {
      return new RelationShipService().getAllMyRelationShips(userId, begin, end);
    } catch (SQLException ex) {
      throw new SocialNetworkException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }

  /**
   * Get list socialInformationRelationShip (relationShips) of my Contacts according to number of
   * Item and the first Index
   * @param myId
   * @param myContactsIds
   * @param numberOfElement
   * @param firstIndex
   * @return
   * @throws SilverpeasException
   */
  @Override
  public List<SocialInformation> getSocialInformationsListOfMyContacts(String myId,
      List<String> myContactsIds, Date begin, Date end) throws SilverpeasException {
    try {
      return new RelationShipService().getAllRelationShipsOfMyContact(myId, myContactsIds, begin,
          end);
    } catch (SQLException ex) {
      throw new SocialNetworkException("SocialEvent.getSocialInformationsList()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", ex);
    }
  }
}
