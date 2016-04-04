/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.web.directory.model;

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contact.model.CompleteContact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DirectoryItemList extends ArrayList<DirectoryItem> {
  private static final long serialVersionUID = -6341586767047173160L;

  public DirectoryItemList() {
    super();
  }

  public DirectoryItemList(List<UserDetail> users) {
    addUsers(users);
  }

  public DirectoryItemList(UserDetail[] users) {
    addUsers(users);
  }

  public DirectoryItemList(Collection<DirectoryItem> c) {
    super(c);
  }

  public void add(UserDetail user) {
    // Directory list does not take into account users fro which the state is deactivated.
    if (!user.isDeactivatedState()) {
      super.add(new UserItem(user));
    }
  }

  public void addUsers(List<UserDetail> users) {
    for (UserDetail user : users) {
      add(user);
    }
  }

  public void addUsers(UserDetail[] users) {
    for (UserDetail user : users) {
      add(user);
    }
  }

  public boolean contains(final UserDetail userDetail) {
    return super.contains(new UserItem(userDetail));
  }

  public void add(CompleteContact contact) {
    super.add(new ContactItem(contact));
  }

  public void addContacts(List<CompleteContact> contacts) {
    for (CompleteContact contact : contacts) {
      add(contact);
    }
  }

  public boolean contains(final CompleteContact completeContact) {
    return super.contains(new ContactItem(completeContact));
  }

  @Override
  public DirectoryItemList subList(int fromIndex, int toIndex) {
    return new DirectoryItemList(super.subList(fromIndex, toIndex));
  }

  /**
   * Gets an item from the collection from an unique item identifier.
   * @param uniqueId the unique identifier from which an item is searched.
   * @return the item begin the unique identifier if any, null otherwise.
   */
  public DirectoryItem getItemByUniqueId(String uniqueId) {
    for (final DirectoryItem item : this) {
      if (item.getUniqueId().equals(uniqueId)) {
        return item;
      }
    }
    return null;
  }
}
