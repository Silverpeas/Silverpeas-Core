/*
 * Copyright (C) 2000 - 2018 Silverpeas
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

package org.silverpeas.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.LoggerExtension;
import org.silverpeas.core.test.extention.LoggerLevel;
import org.silverpeas.core.test.extention.MockedBean;
import org.silverpeas.core.test.extention.TestManagedBean;
import org.silverpeas.core.test.extention.SilverTestEnv;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.Level;

import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test to test some JUnit 5 extensions.
 * @author mmoquillon
 */
@ExtendWith(SilverTestEnv.class)
@ExtendWith(MockitoExtension.class)
@ExtendWith(LoggerExtension.class)
@LoggerLevel(Level.WARNING)
public class PoCJUnit5Test {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  @TestManagedBean
  private UserDetail user = new UserDetail();

  @TestManagedBean
  private UserManager userManager;

  @BeforeEach
  public void setup(@MockedBean final User user) {
    assertThat(user, notNullValue());
    assertThat(userManager, notNullValue());
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  public void test1() {
    assertThat(Logger.getLogger("silverpeas").getLevel(), is(java.util.logging.Level.FINE));
  }

  @Test
  public void test2(@MockedBean final User user) {
    assertThat(user, notNullValue());
  }

  @Test
  public void test3(@Mock UserProvider provider) {
    assertThat(provider, notNullValue());
  }

  @Test
  public void test4() {
    assertThat(Logger.getLogger("silverpeas").getLevel(), is(java.util.logging.Level.WARNING));
  }

  @Test
  public void test5() {
    assertThat(ServiceProvider.getService(UserDetail.class), is(user));
    assertThat(ServiceProvider.getService(UserManager.class), is(userManager));
  }

  @Test
  public void test6() {
    Registration registration = new Registration();
    User registeredUser = mocker.mockField(registration, User.class, "user");
    assertThat(registration, notNullValue());
    assertThat(registration.getRegisteredUser(), is(registeredUser));
  }

  static class Registration {
    private User user;

    public User getRegisteredUser() {
      return user;
    }
  }
}
  