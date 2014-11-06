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

import java.util.Date;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.UserFull;

public class UserItem extends AbstractDirectoryItem implements DirectoryUserItem {
  
  private UserDetail userDetail;
  
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
    UserFull uf = UserFull.getById(getOriginalId());
    if (uf != null && StringUtil.isDefined(uf.getValue("phone"))) {
      return uf.getValue("phone");
    }
    return null;
  }
  
  public UserDetail getUserDetail() {
    return userDetail;
  }

}
