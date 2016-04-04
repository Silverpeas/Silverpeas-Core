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

package org.silverpeas.core.web.directory.model;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.admin.user.model.UserDetail;

/**
 * A user as a member of something (silverpeas?).
 * @deprecated use directly UserDetail.
 */
public class Member {

  private UserDetail userDetail = null;

  public boolean isConnected() {
    return userDetail.isConnected();
  }

  public UserDetail getUserDetail() {
    return userDetail;
  }

  public Member(UserDetail ud) {
    userDetail = ud;
  }

  public UserAccessLevel getAccessLevel() {
    return getUserDetail().getAccessLevel();
  }

  public String getMail() {
    return getUserDetail().geteMail();
  }

  public String getFirstName() {
    return getUserDetail().getFirstName();
  }

  public String getId() {
    return getUserDetail().getId();
  }

  public String getLastName() {
    return getUserDetail().getLastName();
  }

  public String getDuration() {
    return getUserDetail().getDurationOfCurrentSession();
  }

  public String getStatus() {
    if (!StringUtil.isDefined(userDetail.getStatus())) {
      return null;
    }
    return userDetail.getStatus();
  }

}