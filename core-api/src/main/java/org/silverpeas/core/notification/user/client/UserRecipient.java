/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.notification.user.client;

import org.silverpeas.core.admin.user.model.User;

public class UserRecipient {

  private String userId;

  public UserRecipient(String userId) {
    this.userId = userId;
  }

  public UserRecipient(User userDetail) {
    this.userId = userDetail.getId();
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UserRecipient other = (UserRecipient) obj;
    return this.userId == null ? other.userId == null : this.userId.equals(other.userId);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 89 * hash + (this.userId != null ? this.userId.hashCode() : 0);
    return hash;
  }
}
