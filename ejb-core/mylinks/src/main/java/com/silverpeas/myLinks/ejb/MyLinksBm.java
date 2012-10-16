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

package com.silverpeas.myLinks.ejb;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.EJBObject;

import com.silverpeas.myLinks.model.LinkDetail;

/**
 * @author
 */
public interface MyLinksBm extends EJBObject {
  public Collection<LinkDetail> getAllLinks(String userId) throws RemoteException;

  public Collection<LinkDetail> getAllLinksByUser(String userId) throws RemoteException;

  public Collection<LinkDetail> getAllLinksByInstance(String instanceId)
      throws RemoteException;

  public Collection<LinkDetail> getAllLinksByObject(String instanceId, String objectId)
      throws RemoteException;

  public void createLink(LinkDetail link) throws RemoteException;

  public LinkDetail getLink(String linkId) throws RemoteException;

  public void deleteLinks(String[] links) throws RemoteException;

  public void updateLink(LinkDetail link) throws RemoteException;
}
