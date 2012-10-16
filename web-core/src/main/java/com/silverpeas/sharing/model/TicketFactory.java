/*
 *  Copyright (C) 2000 - 2012 Silverpeas
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 * 
 *  As a special exception to the terms and conditions of version 3.0 of
 *  the GPL, you may redistribute this Program in connection with Free/Libre
 *  Open Source Software ("FLOSS") applications as described in Silverpeas's
 *  FLOSS exception.  You should have recieved a copy of the text describing
 *  the FLOSS exception, and it is also available here:
 *  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 * 
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package com.silverpeas.sharing.model;

import java.util.Date;

/**
 *
 * @author ehugonnet
 */
public class TicketFactory {
  
  public static Ticket aTicket(int sharedObjectId, String componentId, String creatorId,
      Date creationDate, Date endDate, int nbAccessMax, String type) {
    if(Ticket.FILE_TYPE.equalsIgnoreCase(type)) {
      return new SimpleFileTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
              nbAccessMax);
    }
    if(Ticket.VERSION_TYPE.equalsIgnoreCase(type)) {
      return new VersionFileTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
              nbAccessMax);
    }
    if(Ticket.NODE_TYPE.equalsIgnoreCase(type)) {
      return new NodeTicket(sharedObjectId, componentId, creatorId, creationDate, endDate,
              nbAccessMax);
    }
    return null;
  }
  
  public static Ticket continuousTicket(int sharedObjectId, String componentId, String creatorId,
      Date creationDate, String type) {
    if(Ticket.FILE_TYPE.equalsIgnoreCase(type)) {
      return new SimpleFileTicket(sharedObjectId, componentId, creatorId, creationDate, null, -1);
    }
    if(Ticket.VERSION_TYPE.equalsIgnoreCase(type)) {
      return new VersionFileTicket(sharedObjectId, componentId, creatorId, creationDate, null, -1);
    }
    if(Ticket.NODE_TYPE.equalsIgnoreCase(type)) {
      return new NodeTicket(sharedObjectId, componentId, creatorId, creationDate, null, -1);
    }
    return null;
  }
}
