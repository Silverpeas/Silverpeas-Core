/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.profile.token;

import static com.silverpeas.util.StringUtil.isDefined;

import org.silverpeas.token.TokenKey;
import org.silverpeas.token.constant.TokenType;

import com.stratelia.webactiv.beans.admin.UserDetail;

/**
 * @author Yohann Chastagnier
 */
public class UserTokenKey implements TokenKey {

  private String userId;

  /**
   * Initializing the token key from a user
   * @param user
   * @return
   */
  public static UserTokenKey from(final UserDetail user) {
    return new UserTokenKey(null, user);
  }

  /**
   * Hidden default constructor
   * @param userId
   * @param user
   */
  private UserTokenKey(final String userId, final UserDetail user) {
    if (isDefined(userId)) {
      this.userId = userId;
    } else if (user != null) {
      this.userId = user.getId();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.TokenKey#isValid()
   */
  @Override
  public boolean isValid() {
    return isDefined(userId);
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.TokenKey#getTokenType()
   */
  @Override
  public TokenType getTokenType() {
    return TokenType.USER;
  }

  /*
   * (non-Javadoc)
   * @see org.silverpeas.token.TokenKey#getResourceId()
   */
  @Override
  public String getResourceId() {
    return userId;
  }
}
