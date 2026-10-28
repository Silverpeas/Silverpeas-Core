/*
 * Copyright (C) 2000 - 2026 Silverpeas
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

package org.silverpeas.core.security.authentication;

import org.junit.jupiter.api.Test;
import org.silverpeas.core.security.authentication.exception.AuthenticationException;
import org.silverpeas.core.security.authentication.twofactor.TwoFactorAuthenticationService;
import org.silverpeas.core.security.authentication.twofactor.model.TwoFactorAuthentication;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.test.annotations.TestManagedMock;
import org.silverpeas.kernel.test.annotations.TestedBean;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.DriverManager;
import java.util.UUID;
import org.silverpeas.kernel.TestManagedBeanFeeder;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv(context = AuthenticationServiceEnrollmentTest.DatabaseContext.class)
class AuthenticationServiceEnrollmentTest {

  @TestManagedMock
  private TwoFactorAuthenticationService twoFactorService;
  @TestedBean
  private AuthenticationService service;

  @Test
  void shouldResolveEnrollmentOwnerFromLoginAndDomain() throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
         var statement = connection.createStatement();
         var naming = mockStatic(InitialContext.class)) {
      statement.execute("create table ST_User (id int, login varchar(100), domainId int)");
      statement.execute("insert into ST_User values (42, 'alice', 0), (43, 'alice', 1)");
      DataSource dataSource = mock(DataSource.class);
      when(dataSource.getConnection()).thenReturn(connection);
      naming.when(() -> InitialContext.doLookup(anyString())).thenReturn(dataSource);
      var enrollment = TwoFactorAuthentication.builder(42)
          .status(TwoFactorAuthentication.Status.PENDING).build();
      when(twoFactorService.startEnrollment(42)).thenReturn(enrollment);

      assertSame(enrollment, service.startTwoFactorEnrollment("alice", "0"));

      verify(twoFactorService).startEnrollment(42);
    }
  }

  @Test
  void shouldRejectEnrollmentForUnknownUser() throws Exception {
    try (var connection = DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
         var statement = connection.createStatement();
         var naming = mockStatic(InitialContext.class)) {
      statement.execute("create table ST_User (id int, login varchar(100), domainId int)");
      DataSource dataSource = mock(DataSource.class);
      when(dataSource.getConnection()).thenReturn(connection);
      naming.when(() -> InitialContext.doLookup(anyString())).thenReturn(dataSource);

      assertThrows(AuthenticationException.class,
          () -> service.startTwoFactorEnrollment("unknown", "0"));

      verifyNoInteractions(twoFactorService);
    }
  }

  /**
   * Registers the real SQL executor and parameter setters for the H2 queries in these tests.
   */
  public static class DatabaseContext extends JEETestContext {
    @Override
    public void init() {
      super.init();
      Stream.of(
          "org.silverpeas.core.persistence.jdbc.sql.JdbcSqlExecutorProvider",
          "org.silverpeas.core.persistence.jdbc.sql.DefaultJdbcSqlExecutor",
          "org.silverpeas.core.persistence.jdbc.sql.setters.SqlStatementParameterSetter",
          "org.silverpeas.core.persistence.jdbc.sql.setters.SqlIntegerParamSetter",
          "org.silverpeas.core.persistence.jdbc.sql.setters.SqlTextSetter")
          .<Class<?>>map(name -> {
            try {
              return Class.forName(name);
            } catch (ClassNotFoundException e) {
              throw new AssertionError(e);
            }
          }).forEach(DatabaseContext::registerBean);
    }

    private static <T> void registerInterface(TestManagedBeanFeeder feeder, Class<?> type, Class<T> api) {
      feeder.manageBeanForType(type.asSubclass(api), api);
    }

    private static <T> void registerBean(Class<T> type) {
      TestManagedBeanFeeder feeder = new TestManagedBeanFeeder();
      feeder.manageBeanForType(type, type);
      for (Class<?> api : type.getInterfaces()) {
        registerInterface(feeder, type, api);
      }
    }
  }

}
