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
package com.silverpeas.socialNetwork.newsFeed.control;

import com.silverpeas.socialNetwork.relationShip.RelationShipService;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bensalem Nabil
 */
public class NewsFeedService {

  private List<String> myContactsIds = new ArrayList<String>();
  private int myId;
/**
 *
 */
  public NewsFeedService() {
  }
/**
 * get my conact
 * @return List<String>
 */
  public List<String> getMyContactsIds() {
    if(myContactsIds.size()<=0)
    try {
      myContactsIds = new RelationShipService().getMyContactsIds(myId);

    } catch (SQLException ex) {
      SilverTrace.error("newsFeedService",
          "NewsFeedService.getMyContactsIds", "", ex);
    }
    return myContactsIds;
  }
/**
 * get my id
 * @return
 */
  public int getMyId() {
    return myId;
  }
/**
 * set my id
 * @param myId
 */
  public void setMyId(int myId) {
    this.myId = myId;
  }
}
