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
package org.silverpeas.core.security.authentication.verifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.security.authentication.exception.AuthenticationUserMustAcceptTermsOfService;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.util.DateUtil;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Yohann Chastagnier
 * Date: 10/09/13
 */
@EnableSilverTestEnv
class UserMustAcceptTermsOfServiceVerifierTest {

  @Test
  void testUserHasAlreadyAccepted() throws AuthenticationUserMustAcceptTermsOfService {
    UserDetail user = createUser(DateUtil.getNow());
    new UserMustAcceptTermsOfServiceVerifier(user).verify();
  }

  @Test
  void testUserMustAccept() {
    assertThrows(AuthenticationUserMustAcceptTermsOfService.class, () -> {
      UserDetail user = createUser(null);
      new UserMustAcceptTermsOfServiceVerifier(user).verify();
    });
  }

  @Test
  void testNoUser() throws AuthenticationUserMustAcceptTermsOfService {
    new UserMustAcceptTermsOfServiceVerifier(null).verify();
  }

  private UserDetail createUser(Date lastTosAcceptanceDate) {
    UserDetail user = new UserDetail();
    user.setId("0");
    user.setTosAcceptanceDate(lastTosAcceptanceDate);
    return user;
  }
}
