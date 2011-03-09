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

package com.stratelia.webactiv.personalization.control.ejb;

import java.rmi.RemoteException;

public interface PersonalizationBmBusinessSkeleton {

  public void setActor(String userId) throws RemoteException;

  public void setLanguages(String languages) throws RemoteException;

  public String getLanguages() throws RemoteException;

  public String getFavoriteLanguage() throws RemoteException;

  public String getFavoriteLook() throws RemoteException;

  public void setFavoriteLook(String look) throws RemoteException;

  public void setPersonalWorkSpace(String spaceId) throws RemoteException;

  public String getPersonalWorkSpace() throws RemoteException;

  public void setThesaurusStatus(boolean thesaurusStatus)
      throws RemoteException;

  public boolean getThesaurusStatus() throws RemoteException;

  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws RemoteException;

  public boolean getDragAndDropStatus() throws RemoteException;

  public void setOnlineEditingStatus(boolean onlineEditingStatus)
      throws RemoteException;

  public boolean getOnlineEditingStatus() throws RemoteException;

  public void setWebdavEditingStatus(boolean webdavEditingStatus)
      throws RemoteException;

  public boolean getWebdavEditingStatus() throws RemoteException;

}