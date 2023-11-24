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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.directory.model;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.web.directory.control.DirectoryUserFullRequestCache;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.Objects;

public class UserItem extends AbstractDirectoryItem implements DirectoryUserItem {

  private final User user;

  public UserItem(@Nonnull User user) {
    Objects.requireNonNull(user);
    this.user = user;
  }

  @Override
  public String getFirstName() {
    return user.getFirstName();
  }

  @Override
  public String getLastName() {
    return user.getLastName();
  }

  @Override
  public String getAvatar() {
    return user.getAvatar();
  }

  @Override
  public DirectoryItem.ITEM_TYPE getType() {
    return DirectoryItem.ITEM_TYPE.USER;
  }

  @Override
  public String getDomainId() {
    return user.getDomainId();
  }

  @Override
  public Date getCreationDate() {
    return user.getCreationDate();
  }

  @Override
  public String getOriginalId() {
    return user.getId();
  }

  public String getAccessLevel() {
    return user.getAccessLevel().code();
  }

  @Override
  public String getMail() {
    return user.getEmailAddress();
  }

  @Override
  public String getPhone() {
    UserFull userFull = getUserFull();
    if (userFull != null) {
      String phone = userFull.getValue("phone");
      if (StringUtil.isDefined(phone)) {
        return phone;
      }
    }
    return null;
  }

  @Override
  public String getFax() {
    UserFull userFull = getUserFull();
    if (userFull != null) {
      String fax = userFull.getValue("fax");
      if (StringUtil.isDefined(fax)) {
        return fax;
      }
    }
    return null;
  }

  /**
   * Gets the details of the user associated to the item.
   * @return a {@link UserDetail} instance explicitly. If the item has been initialized from
   * {@link User} instance which is not a {@link UserDetail} one, then {@link UserDetail}
   * instance is reached from persistence.
   */
  public UserDetail getUserDetail() {
    return user instanceof UserDetail ? (UserDetail) user :
        UserDetail.getById(user.getId());
  }

  /**
   * Gets full data of the user associated to the item.
   * @return a {@link UserFull} instance.
   */
  public UserFull getUserFull() {
    return DirectoryUserFullRequestCache.get().getUserFull(this);
  }

  /**
   * This implementation to indicates that the equals is explicitly delegated to the overridden
   * class.
   */
  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  /**
   * This implementation to indicates that the hasCode is explicitly delegated to the overridden
   * class.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
