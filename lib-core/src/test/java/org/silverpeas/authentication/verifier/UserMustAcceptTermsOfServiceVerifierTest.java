/*
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
 * FLOSS exception. You should have recieved a copy of the text describing
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
package org.silverpeas.authentication.verifier;

import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DateUtil;
import org.junit.Test;
import org.silverpeas.authentication.exception.AuthenticationUserMustAcceptTermsOfService;

import java.util.Date;

/**
 * User: Yohann Chastagnier
 * Date: 10/09/13
 */
public class UserMustAcceptTermsOfServiceVerifierTest {

  @Test
  public void testUserHasAlreadyAccepted() throws AuthenticationUserMustAcceptTermsOfService {
    UserDetail user = createUser(DateUtil.getNow());
    new UserMustAcceptTermsOfServiceVerifier(user).verify();
  }

  @Test(expected = AuthenticationUserMustAcceptTermsOfService.class)
  public void testUserMustAccept() throws AuthenticationUserMustAcceptTermsOfService {
    UserDetail user = createUser(null);
    new UserMustAcceptTermsOfServiceVerifier(user).verify();
  }

  @Test
  public void testNoUser() throws AuthenticationUserMustAcceptTermsOfService {
    UserDetail user = createUser(DateUtil.getNow());
    new UserMustAcceptTermsOfServiceVerifier(null).verify();
  }

  /**
   * Create a UserDetail
   * @param lastTosAcceptanceDate
   * @return
   */
  private UserDetail createUser(Date lastTosAcceptanceDate) {
    UserDetail user = new UserDetail();
    user.setId("0");
    user.setTosAcceptanceDate(lastTosAcceptanceDate);
    return user;
  }
}
