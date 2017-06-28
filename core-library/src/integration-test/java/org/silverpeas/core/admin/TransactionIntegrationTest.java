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
package org.silverpeas.core.admin;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.util.ServiceProvider;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Tests that the JDBC connection from a same or a graph of managed transactions is always the
 * same but it can be distinct from another transaction. In order to ensure the connection in a
 * stopped transaction isn't reused by another transaction, the transactional operations are
 * executed in different threads.
 * @author mmoquillon
 */
@RunWith(Arquillian.class)
public class TransactionIntegrationTest {

  @Inject
  private UserDAO userDAO;

  @Resource
  private ManagedScheduledExecutorService executor;

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/admin/domain/driver/create_table.sql")
          .loadInitialDataSetFrom("test-usersandgroups-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(TransactionIntegrationTest.class)
        .addAdministrationFeatures()
        .addProcessFeatures()
        .build();
  }

  @Before
  public void before() {
    assertThat(userDAO, notNullValue());
    assertThat(executor, notNullValue());
  }

  @Test
  public void test() throws Exception {
    TransactionIntegrationTest test = ServiceProvider.getService(TransactionIntegrationTest.class);
    Future<Connection> connection1 = executor.submit(() -> test.transaction1());
    Future<Connection> connection2 = executor.submit(() -> test.transaction1());
    assertThat(connection1.get(), not(connection2.get()));
  }

  @Test
  public void testWithoutTransactionalRootMethod() throws Exception {
    UserDetail user;
    try (Connection connection = DBUtil.openConnection()) {
      user = userDAO.getUserById(connection, "1");
      assertThat(user, notNullValue());
    }

    TransactionIntegrationTest test = ServiceProvider.getService(TransactionIntegrationTest.class);
    test.transaction2(user);

    try (Connection connection = DBUtil.openConnection()) {
      user = userDAO.getUserById(connection, "1");
      assertThat(user.getFirstName(), is("MOOZER"));
      assertThat(user.getLastName(), is("KILLER"));
    }
  }

  @Transactional
  public Connection transaction1() throws SQLException {
    Connection connection1;
    UserDetail user;
    try (Connection connection = DBUtil.openConnection()) {
      // assert that the connection is managed
      assertThat(connection.getAutoCommit(), is(false));
      connection1 = getWrappedConnection(connection);
      user = userDAO.getUserById(connection, "1");
      assertThat(user, notNullValue());
    }

    Connection connection2 = transaction2(user);
    assertThat(connection1, is(connection2));
    return connection1;
  }

  @Transactional
  public Connection transaction2(final UserDetail user) throws SQLException {
    try(Connection connection = DBUtil.openConnection()) {
      // assert that the connection is managed
      assertThat(connection.getAutoCommit(), is(false));
      user.setFirstName("MOOZER");
      user.setLastName("KILLER");
      userDAO.updateUser(connection, user);
      return getWrappedConnection(connection);
    }
  }


  private Connection getWrappedConnection(final Connection connection) {
    try {
      Method getUnderlyingConnection = connection.getClass().getMethod("getUnderlyingConnection");
      return (Connection) getUnderlyingConnection.invoke(connection);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

}
