/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.clipboard.service;

import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.clipboard.ClipboardSelection;

import java.util.Collection;

/**
 * A clipboard in Silverpeas to receive the objects cut or copied by users in Silverpeas.
 * @author ehugonnet
 */
public interface Clipboard {

  void add(ClipboardSelection clipObject) throws ClipboardException;

  ClipboardSelection getObject();

  void PasteDone() throws ClipboardException;

  Collection<ClipboardSelection> getSelectedObjects() throws ClipboardException;

  Collection<ClipboardSelection> getObjects() throws ClipboardException;

  int size() throws ClipboardException;

  ClipboardSelection getObject(int index) throws ClipboardException;

  void setSelected(int index, boolean setIt) throws ClipboardException;

  void removeObject(int index) throws ClipboardException;

  void clear();

  void setMultiClipboard() throws ClipboardException;

  void setSingleClipboard() throws ClipboardException;

  int getCount() throws ClipboardException;

  String getMessageError() throws ClipboardException;

  Exception getExceptionError() throws ClipboardException;

  void setMessageError(String messageID, Exception e) throws ClipboardException;

}
