/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.clipboard.service;

import java.util.Collection;

import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;

/**
 * A clipboard in Silverpeas to receive the objects cut or copied by users in Silverpeas.
 * @author ehugonnet
 */
public interface Clipboard {

  /**
   * Method declaration
   *
   * @param clipObject
   * @throws ClipboardException
   * @see
   */
  public void add(ClipboardSelection clipObject) throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   */
  public ClipboardSelection getObject();

  /**
   * Method declaration
   *
   * @throws ClipboardException
   */
  public void PasteDone() throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   * @throws ClipboardException
   */
  public Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   * @throws ClipboardException
   */
  public Collection<ClipboardSelection> getObjects() throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   * @throws ClipboardException
   * @see
   */
  public int size() throws ClipboardException;

  /**
   * Method declaration
   *
   * @param index
   * @return
   * @throws ClipboardException
   */
  public ClipboardSelection getObject(int index) throws ClipboardException;

  /**
   * Method declaration
   *
   * @param index
   * @param setIt
   * @throws ClipboardException
   */
  public void setSelected(int index, boolean setIt) throws ClipboardException;

  /**
   * Method declaration
   *
   * @param index
   * @throws ClipboardException
   */
  public void removeObject(int index) throws ClipboardException;

  public void clear();

  /**
   * Method declaration
   *
   * @throws ClipboardException
   */
  public void setMultiClipboard() throws ClipboardException;

  /**
   * Method declaration
   *
   * @throws ClipboardException
   */
  public void setSingleClipboard() throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   * @throws ClipboardException
   */
  public int getCount() throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   * @throws ClipboardException
   */
  public String getMessageError() throws ClipboardException;

  /**
   * Method declaration
   *
   * @return
   * @throws ClipboardException
   */
  public Exception getExceptionError() throws ClipboardException;

  /**
   * Method declaration
   *
   * @param messageID
   * @param e
   * @throws ClipboardException
   */
  public void setMessageError(String messageID, Exception e) throws ClipboardException;

}
