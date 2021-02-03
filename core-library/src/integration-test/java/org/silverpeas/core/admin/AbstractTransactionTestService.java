/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.admin;

import org.silverpeas.core.admin.user.constant.UserAccessLevel;
import org.silverpeas.core.admin.user.constant.UserState;
import org.silverpeas.core.admin.user.dao.UserDAO;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author silveryocha
 */
abstract class AbstractTransactionTestService implements TransactionTestService {

  @Inject
  private UserDAO userDAO;

  @Override
  @Transactional
  public Connection transaction1() throws SQLException, InterruptedException {
    Connection connection1;
    UserDetail user;
    try (Connection connection = DBUtil.openConnection()) {
      Thread.sleep(1000l);
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

  @Override
  @Transactional
  public Connection transaction2(final UserDetail user) throws SQLException {
    try (Connection connection = DBUtil.openConnection()) {
      // assert that the connection is managed
      assertThat(connection.getAutoCommit(), is(false));
      user.setFirstName("MOOZER");
      user.setLastName("KILLER");
      userDAO.updateUser(connection, user);
      return getWrappedConnection(connection);
    }
  }

  @Override
  @Transactional
  public void performInDefaultTransaction(TransactionTestProcess process, Exception exceptionAtEnd)
      throws Exception {
    process.perform();
    if (exceptionAtEnd != null) {
      throw exceptionAtEnd;
    }
  }

  @Override
  @Transactional
  public void transactionWithDefaultTransactionManagement(final Exception exceptionAtEnd)
      throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(rollbackOn = TransactionCheckedException.class)
  public void transactionWithCheckedTransactionManagement(final Exception exceptionAtEnd)
      throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(value = Transactional.TxType.MANDATORY)
  public void saveNewUserWithMandatoryTransactionAndDefaultHandledException(
      final Exception exceptionAtEnd) throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(value = Transactional.TxType.MANDATORY, rollbackOn = TransactionCheckedException
      .class)
  public void saveNewUserWithMandatoryTransactionAndCheckedHandledException(
      final Exception exceptionAtEnd) throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(value = Transactional.TxType.REQUIRED)
  public void saveNewUserWithRequiredTransactionAndDefaultHandledException(
      final Exception exceptionAtEnd) throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = TransactionCheckedException
      .class)
  public void saveNewUserWithRequiredTransactionAndCheckedHandledException(
      final Exception exceptionAtEnd) throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(value = Transactional.TxType.REQUIRES_NEW)
  public void saveNewUserWithRequiresNewTransactionAndDefaultHandledException(
      final Exception exceptionAtEnd) throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  @Override
  @Transactional(value = Transactional.TxType.REQUIRES_NEW, rollbackOn =
      TransactionCheckedException.class)
  public void saveNewUserWithRequiresNewTransactionAndCheckedHandledException(
      final Exception exceptionAtEnd) throws Exception {
    saveNewUserAndThrowExceptionIfRequired(exceptionAtEnd);
  }

  private Connection getWrappedConnection(final Connection connection) {
    try {
      Method getUnderlyingConnection = connection.getClass().getMethod("getUnderlyingConnection");
      return (Connection) getUnderlyingConnection.invoke(connection);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  private void saveNewUserAndThrowExceptionIfRequired(final Exception exceptionAtEnd)
      throws Exception {
    UserDetail newUser = new UserDetail();
    newUser.setDomainId("0");
    newUser.setSpecificId("4");
    newUser.setLastName("NewUser");
    newUser.setLogin("newuser");
    newUser.setAccessLevel(UserAccessLevel.USER);
    newUser.setState(UserState.VALID);
    try (Connection connection = DBUtil.openConnection()) {
      userDAO.saveUser(connection, newUser);
    }
    if (exceptionAtEnd != null) {
      throw exceptionAtEnd;
    }
  }
}
