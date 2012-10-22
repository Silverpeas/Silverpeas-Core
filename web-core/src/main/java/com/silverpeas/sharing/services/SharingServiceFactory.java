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
* FLOSS exception. You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.silverpeas.sharing.services;

import javax.inject.Inject;

/**
* A factory of SharingTicketService instances.
* This factory wraps the concrete implementation of the SharingTicketService and the way the
* life-cycle of theses instances are managed.
*/
public class SharingServiceFactory {
  private static final SharingServiceFactory instance = new SharingServiceFactory();
  @Inject
  private SharingTicketService sharingTicketService;

  /**
* Gets a factory of a file sharing service.
* @return an instance of the FileSharingServiceFactory.
*/
  public static SharingServiceFactory getFactory() {
    return instance;
  }

  /**
* Gets an instance of the file sharing service.
* @return a SharingTicketService instance.
*/
  public static SharingTicketService getSharingTicketService() {
    return getFactory().getCurrentSharingTicketService();
  }
  
  
  private SharingTicketService getCurrentSharingTicketService() {
    return sharingTicketService;
  }
  
}