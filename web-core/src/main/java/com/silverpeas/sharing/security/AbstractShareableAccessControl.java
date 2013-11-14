/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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
package com.silverpeas.sharing.security;

import com.silverpeas.sharing.model.Ticket;
import com.silverpeas.sharing.services.SharingServiceFactory;

/**
 * User: Yohann Chastagnier
 * Date: 05/11/13
 */
public abstract class AbstractShareableAccessControl<T extends Ticket, R>
    implements ShareableAccessControl<T, R> {

  protected AbstractShareableAccessControl() {
    super();
  }

  @SuppressWarnings("unchecked")
  @Override
  final public boolean isReadable(final ShareableResource<R> resource) {
    try {
      Ticket ticket =
          SharingServiceFactory.getSharingTicketService().getTicket(resource.getToken());
      return !(ticket == null || !ticket.isValid()) &&
          isReadable((T) ticket, resource.getAccessedObject());
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * @param ticket ticket passed here exists and is valid.
   * @param accessedObject
   * @return
   */
  abstract protected boolean isReadable(T ticket, R accessedObject) throws Exception;
}
