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

package org.silverpeas.core.notification.user.server.channel;

/**
 * @author ehugonnet
 */
public class SilverpeasMessage {
  private long mId;

  public void setId(long value) {
    mId = value;
  }

  public long getId() {
    return mId;
  }

  private long userId = -1;

  public long getUserId() {
    return userId;
  }

  public void setUserId(long value) {
    userId = value;
  }

  private String mUserLogin;

  public void setUserLogin(String value) {
    mUserLogin = value;
  }

  public String getUserLogin() {
    return mUserLogin;
  }

  private String mSenderName;

  public void setSenderName(String value) {
    mSenderName = value;
  }

  public String getSenderName() {
    return mSenderName;
  }

  private String mSubject;

  public void setSubject(String value) {
    mSubject = value;
  }

  public String getSubject() {
    return mSubject;
  }

  private String mBody;

  public void setBody(String value) {
    mBody = value;
  }

  public String getBody() {
    return mBody;
  }
}
