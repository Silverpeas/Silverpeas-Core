/**
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

package com.silverpeas.directory.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.contact.model.CompleteContact;

public class DirectoryItemList extends ArrayList<DirectoryItem> {

  public DirectoryItemList() {
    super();
  }
  
  public DirectoryItemList(List<UserDetail> users) {
    for(UserDetail user : users) {
      add(user);
    }
  }
  
  public DirectoryItemList(UserDetail[] users) {
    for(UserDetail user : users) {
      add(user);
    }
  }
  
  public DirectoryItemList(Collection<DirectoryItem> c) {
    super(c);
  }
  
  public void add(UserDetail user) {
    super.add(new UserItem(user));
  }
  
  public void addContacts(List<CompleteContact> contacts) {
    for (CompleteContact contact : contacts) {
      add(contact);
    }
  }
  
  public void add(CompleteContact contact) {
    super.add(new ContactItem(contact));
  }
  
  public DirectoryItemList subList(int fromIndex, int toIndex) {
    return new DirectoryItemList(super.subList(fromIndex, toIndex));
  }
  
  public DirectoryItem getItemByUniqueId(String uniqueId) {
    ListIterator<DirectoryItem> items = listIterator();
    while (items.hasNext()) {
      DirectoryItem item = items.next();
      if (item.getUniqueId().equals(uniqueId)) {
        return item;
      }
    }
    return null;
  }
}
