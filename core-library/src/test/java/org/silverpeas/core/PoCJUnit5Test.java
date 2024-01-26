/*
 * Copyright (C) 2000 - 2024 Silverpeas
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

package org.silverpeas.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.UserManager;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.service.UserProvider;
import org.silverpeas.core.test.unit.extention.FieldMocker;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.core.test.unit.extention.RequesterProvider;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.logging.Level;
import org.silverpeas.kernel.test.annotations.TestManagedBean;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestManagedMocks;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import org.silverpeas.kernel.test.extension.LoggerLevel;

import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit test to test some JUnit 5 extensions.
 * @author mmoquillon
 */
@EnableSilverTestEnv(context = JEETestContext.class)
@ExtendWith(MockitoExtension.class)
@LoggerLevel(Level.WARNING)
@TestManagedMocks({OrganizationController.class, Administration.class})
class PoCJUnit5Test {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();

  @TestManagedBean
  private UserDetail user = new UserDetail();

  @TestManagedBean
  private UserManager userManager;

  @TestedBean
  private UserManagerFactory factory;

  @RequesterProvider
  private User getCurrentRequester() {
    return user;
  }

  @BeforeEach
  public void setup(@TestManagedMock final User user) {
    assertThat(user, notNullValue());
    assertThat(userManager, notNullValue());
  }

  @Test
  @LoggerLevel(Level.DEBUG)
  void test1() {
    assertThat(Logger.getLogger("silverpeas").getLevel(), is(java.util.logging.Level.FINE));
  }

  @Test
  void test2(@TestManagedMock final User user) {
    assertThat(user, notNullValue());
  }

  @Test
  void test3(@Mock UserProvider provider) {
    assertThat(provider, notNullValue());
  }

  @Test
  void test4() {
    assertThat(Logger.getLogger("silverpeas").getLevel(), is(java.util.logging.Level.WARNING));
  }

  @Test
  void test5() {
    assertThat(ServiceProvider.getService(UserDetail.class), is(user));
    assertThat(ServiceProvider.getService(UserManager.class), is(userManager));
  }

  @Test
  void test6() {
    Registration registration = new Registration();
    User registeredUser = mocker.mockField(registration, User.class, "user");
    assertThat(registration, notNullValue());
    assertThat(registration.getRegisteredUser(), is(registeredUser));
  }

  @Test
  void test7() {
    assertThat(factory, notNullValue());
    assertThat(factory.getUserManager(), is(userManager));
  }

  @Test
  void test8() {
    assertThat(User.getCurrentRequester(), is(user));
  }

  @Test
  void test9() {
    assertThat(ServiceProvider.getService(OrganizationController.class), notNullValue());
    assertThat(ServiceProvider.getService(Administration.class), notNullValue());
  }

  static class Registration {
    @SuppressWarnings("unused")
    private User user;

    public User getRegisteredUser() {
      return user;
    }
  }

}
  