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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.stratelia.webactiv.clipboard.control.ejb;

import com.silverpeas.util.clipboard.ClipboardSelection;
import java.rmi.RemoteException;
import java.util.Collection;

/**
 * @author ehugonnet
 */
public interface Clipboard {
  /**
   * Method declaration
   * @param clipObject
   * @throws RemoteException
   * @see
   */
  public void add(ClipboardSelection clipObject) throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public ClipboardSelection getObject() throws RemoteException;

  /**
   * Method declaration
   * @throws RemoteException
   * @see
   */
  public void PasteDone() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getSelectedObjects() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Collection getObjects() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public int size() throws RemoteException;

  /**
   * Method declaration
   * @param index
   * @return
   * @throws RemoteException
   * @see
   */
  public ClipboardSelection getObject(int index) throws RemoteException;

  /**
   * Method declaration
   * @param index
   * @param setIt
   * @throws RemoteException
   * @see
   */
  public void setSelected(int index, boolean setIt) throws RemoteException;

  /**
   * Method declaration
   * @param index
   * @throws RemoteException
   * @see
   */
  public void remove(int index) throws RemoteException;

  /**
   * Method declaration
   * @throws RemoteException
   * @see
   */
  public void clear() throws RemoteException;

  /**
   * Method declaration
   * @throws RemoteException
   * @see
   */
  public void setMultiClipboard() throws RemoteException;

  /**
   * Method declaration
   * @throws RemoteException
   * @see
   */
  public void setSingleClipboard() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public String getName() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Integer getCount() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public String getMessageError() throws RemoteException;

  /**
   * Method declaration
   * @return
   * @throws RemoteException
   * @see
   */
  public Exception getExceptionError() throws RemoteException;

  /**
   * Method declaration
   * @param messageID
   * @param e
   * @throws RemoteException
   * @see
   */
  public void setMessageError(String messageID, Exception e)
      throws RemoteException;
}
