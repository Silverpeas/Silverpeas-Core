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
package org.silverpeas.core.admin;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.persistence.jdbc.sql.JdbcSqlQuery;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;
import org.silverpeas.core.test.util.SQLRequester;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Tests the isolation of the transaction when the service is not a singleton (so each time an
 * instance of the service is requested, each time is provided a new instance).
 * Please compare tests with those of
 * {@link TransactionIsolationWithOneServiceInstanceIntegrationTest}.
 * @author silveryocha.
 */
@RunWith(Arquillian.class)
public class TransactionIsolationWithMultiServiceInstancesIT
    extends AbstractTransactionIntegrationTest {

  @Rule
  public DbSetupRule dbSetupRule =
      DbSetupRule.createTablesFrom("/org/silverpeas/core/admin/domain/driver/create_table.sql")
          .loadInitialDataSetFrom("test-usersandgroups-dataset.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return configureTestArchive(WarBuilder4LibCore
        .onWarForTestClass(TransactionIsolationWithMultiServiceInstancesIT.class))
        .build();
  }

  /*
  One transaction T1 (REQUIRED).
  Checked exceptions must be set on rollbackOn attribute of Transactional annotation in order to
  be taken into account and finally perform the rollback.
   */

  @Test
  public void testSaveUserWithoutExceptionByDefaultTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    try {
      TransactionTestService test = getTestService();
      test.transactionWithDefaultTransactionManagement(null);
    } catch (Exception ignore) {
    }
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserWithoutExceptionByCheckedTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    try {
      TransactionTestService test = getTestService();
      test.transactionWithCheckedTransactionManagement(null);
    } catch (Exception ignore) {
    }
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserWithRuntimeExceptionByDefaultTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    try {
      TransactionTestService test = getTestService();
      test.transactionWithDefaultTransactionManagement(new NullPointerException());
    } catch (Exception ignore) {
    }
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Test
  public void testSaveUserWithRuntimeExceptionByCheckedTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    try {
      TransactionTestService test = getTestService();
      test.transactionWithCheckedTransactionManagement(new NullPointerException());
    } catch (Exception ignore) {
    }
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Test
  public void testSaveUserWithCheckedExceptionByDefaultTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    try {
      TransactionTestService test = getTestService();
      test.transactionWithDefaultTransactionManagement(new TransactionCheckedException());
    } catch (Exception ignore) {
    }
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserWithCheckedExceptionByCheckedTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    try {
      TransactionTestService test = getTestService();
      test.transactionWithCheckedTransactionManagement(new TransactionCheckedException());
    } catch (Exception ignore) {
    }
    assertThat(getUserTableLines(), hasSize(3));
  }

  /*
  Two transactions, T1 and T2 :
  - T1 (REQUIRED) calls T2 (MANDATORY)
  - T2 does the JOB

  The rollbackOn of T2 is not ignored.
   */

  @Test
  public void testSaveUserByMandatoryDefaultTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndDefaultHandledException(null);
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByMandatoryCheckedTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndCheckedHandledException(null);
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByMandatoryDefaultTransactionManagementAndFinallyThrowRuntimeException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndDefaultHandledException(null);
    }, new NullPointerException());
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Test
  public void testSaveUserByMandatoryCheckedTransactionManagementAndFinallyThrowRuntimeException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndCheckedHandledException(null);
    }, new NullPointerException());
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Test
  public void testSaveUserByMandatoryDefaultTransactionManagementAndFinallyThrowCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndDefaultHandledException(null);
    }, new TransactionCheckedException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByMandatoryCheckedTransactionManagementAndFinallyThrowCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndCheckedHandledException(null);
    }, new TransactionCheckedException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByMandatoryDefaultTransactionManagementWhichThrowsCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndDefaultHandledException(
          new TransactionCheckedException());
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByMandatoryCheckedTransactionManagementWhichThrowsCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithMandatoryTransactionAndCheckedHandledException(
          new TransactionCheckedException());
    }, null);
    assertThat(getUserTableLines(), hasSize(3));
  }

  /*
  Two transactions, T1 and T2 :
  - T1 (REQUIRED) calls T2 (REQUIRED)
  - T2 does the JOB

  The rollbackOn of T2 is not ignored.
   */

  @Test
  public void testSaveUserByRequiredDefaultTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndDefaultHandledException(null);
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiredCheckedTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndCheckedHandledException(null);
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiredDefaultTransactionManagementAndFinallyThrowRuntimeException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndDefaultHandledException(null);
    }, new NullPointerException());
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Test
  public void testSaveUserByRequiredCheckedTransactionManagementAndFinallyThrowRuntimeException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndCheckedHandledException(null);
    }, new NullPointerException());
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Test
  public void testSaveUserByRequiredDefaultTransactionManagementAndFinallyThrowCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndDefaultHandledException(null);
    }, new TransactionCheckedException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiredCheckedTransactionManagementAndFinallyThrowCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndCheckedHandledException(null);
    }, new TransactionCheckedException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiredDefaultTransactionManagementWhichThrowsCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndDefaultHandledException(
          new TransactionCheckedException());
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiredCheckedTransactionManagementWhichThrowsCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiredTransactionAndCheckedHandledException(
          new TransactionCheckedException());
    }, null);
    assertThat(getUserTableLines(), hasSize(3));
  }

  /*
  Two transactions, T1 and T2 :
  - T1 (REQUIRED) calls T2 (REQUIRES_NEW)
  - T2 does the JOB

  The rollbackOn of T2 is not ignored.
   */

  @Test
  public void testSaveUserByRequiresNewDefaultTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndDefaultHandledException(null);
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewCheckedTransactionManagement() throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndCheckedHandledException(null);
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewDefaultTransactionManagementAndFinallyThrowRuntimeException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndDefaultHandledException(null);
    }, new NullPointerException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewCheckedTransactionManagementAndFinallyThrowRuntimeException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndCheckedHandledException(null);
    }, new NullPointerException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewDefaultTransactionManagementAndFinallyThrowCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndDefaultHandledException(null);
    }, new TransactionCheckedException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewCheckedTransactionManagementAndFinallyThrowCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndCheckedHandledException(null);
    }, new TransactionCheckedException());
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewDefaultTransactionManagementWhichThrowsCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndDefaultHandledException(
          new TransactionCheckedException());
    }, null);
    assertThat(getUserTableLines(), hasSize(4));
  }

  @Test
  public void testSaveUserByRequiresNewCheckedTransactionManagementWhichThrowsCheckedException()
      throws Exception {
    assertThat(getUserTableLines(), hasSize(3));
    performInExistingDefaultTransaction(() -> {
      TransactionTestService test = getTestService();
      test.saveNewUserWithRequiresNewTransactionAndCheckedHandledException(
          new TransactionCheckedException());
    }, null);
    assertThat(getUserTableLines(), hasSize(3));
  }

  @Override
  TransactionTestService getTestService() {
    return ServiceProvider.getService(TransactionNotSingletonTestServiceImpl.class);
  }

  /**
   * Returns the list of user lines persisted into st_user table.
   * @return List of lines represented by a map between column name and value.
   */
  private List<SQLRequester.ResultLine> getUserTableLines() throws Exception {
    return dbSetupRule.mapJdbcSqlQueryResultAsListOfMappedValues(
        JdbcSqlQuery.createSelect("* from st_user").addSqlPart("order by id"));
  }
}
