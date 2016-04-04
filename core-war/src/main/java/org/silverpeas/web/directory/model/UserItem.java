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
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.core.util.StringUtil;

import java.util.Date;

public class UserItem extends AbstractDirectoryItem implements DirectoryUserItem {

  private UserDetail userDetail;
  private UserFull userFull = null;

  public UserItem(UserDetail user) {
    this.userDetail = user;
  }

  @Override
  public String getFirstName() {
    return userDetail.getFirstName();
  }

  @Override
  public String getLastName() {
    return userDetail.getLastName();
  }

  @Override
  public String getAvatar() {
    return userDetail.getAvatar();
  }

  @Override
  public DirectoryItem.ITEM_TYPE getType() {
    return DirectoryItem.ITEM_TYPE.User;
  }

  @Override
  public String getDomainId() {
    return userDetail.getDomainId();
  }

  @Override
  public Date getCreationDate() {
    return userDetail.getCreationDate();
  }

  @Override
  public String getOriginalId() {
    return userDetail.getId();
  }

  public String getAccessLevel() {
    return userDetail.getAccessLevel().code();
  }

  @Override
  public String getMail() {
    return getUserDetail().geteMail();
  }

  @Override
  public String getPhone() {
    if (getUserFull() != null && StringUtil.isDefined(getUserFull().getValue("phone"))) {
      return getUserFull().getValue("phone");
    }
    return null;
  }

  @Override
  public String getFax() {
    if (getUserFull() != null && StringUtil.isDefined(getUserFull().getValue("fax"))) {
      return getUserFull().getValue("fax");
    }
    return null;
  }

  /**
   * Gets the details of the user associated to the item.
   * @return
   */
  public UserDetail getUserDetail() {
    return userDetail;
  }

  /**
   * Gets full data of the user associated to the item.
   * @return
   */
  private UserFull getUserFull(){
    if (userFull == null) {
      userFull = UserFull.getById(getOriginalId());
    }
    return userFull;
  }
}
