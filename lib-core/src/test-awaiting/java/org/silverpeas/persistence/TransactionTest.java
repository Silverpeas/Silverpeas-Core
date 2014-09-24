/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.persistence;

import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.util.DBUtil;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.Test;
import org.silverpeas.persistence.jpa.RepositoryBasedTest;
import org.silverpeas.persistence.repository.OperationContext;
import org.silverpeas.persistence.repository.jpa.JpaEntityServiceTest;
import org.silverpeas.persistence.repository.jpa.model.Person;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author: Yohann Chastagnier
 */
public class TransactionTest extends RepositoryBasedTest {

  private JpaEntityServiceTest jpaEntityServiceTest;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    DBUtil.getInstanceForTest(getDataSource().getConnection());
    jpaEntityServiceTest = getApplicationContext().getBean(JpaEntityServiceTest.class);
  }

  @Override
  public void tearDown() throws Exception {
    try {
      super.tearDown();
    } finally {
      DBUtil.clearTestInstance();
    }
  }

  @Override
  public String[] getApplicationContextPath() {
    return new String[]{"/spring-persistence.xml", "/spring-persistence-embedded-datasource.xml"};
  }

  @Override
  public String getDataSetPath() {
    return "org/silverpeas/persistence/persistence-dataset.xml";
  }

  @Test
  public void transactionSuccess() throws Exception {
    final Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person, notNullValue());

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("test_persons");
    int index = getTableIndexForId(table, person.getId());
    assertThat((String) table.getValue(index, "id"), is("person_1"));
    assertThat((String) table.getValue(index, "id"), is(person.getId()));
    assertThat((String) table.getValue(index, "firstName"), is("Yohann"));
    assertThat((String) table.getValue(index, "firstName"), is(person.getFirstName()));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("1"));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is(person.getLastUpdatedBy()));

    // Modifying person data
    person.setFirstName("UnknownFirstName");

    Transaction transaction = Transaction.getTransaction();
    transaction.perform(new Transaction.Process<Void>() {
      @Override
      public Void execute() {
        jpaEntityServiceTest.save(createOperationContext("26"), person);
        return null;
      }
    });

    table = actualDataSet.getTable("test_persons");
    index = getTableIndexForId(table, person.getId());
    assertThat((String) table.getValue(index, "id"), is("person_1"));
    assertThat((String) table.getValue(index, "firstName"), is("UnknownFirstName"));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("26"));
  }

  @Test
  public void transactionError() throws Exception {
    final Person person = jpaEntityServiceTest.getPersonById("person_1");
    assertThat(person, notNullValue());

    IDataSet actualDataSet = getActualDataSet();
    ITable table = actualDataSet.getTable("test_persons");
    int index = getTableIndexForId(table, person.getId());
    assertThat((String) table.getValue(index, "id"), is("person_1"));
    assertThat((String) table.getValue(index, "id"), is(person.getId()));
    assertThat((String) table.getValue(index, "firstName"), is("Yohann"));
    assertThat((String) table.getValue(index, "firstName"), is(person.getFirstName()));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("1"));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is(person.getLastUpdatedBy()));

    // Modifying person data
    person.setFirstName("UnknownFirstName");

    boolean exceptionThrown = false;
    try {
      Transaction transaction = Transaction.getTransaction();
      transaction.perform(new Transaction.Process<Void>() {
        @Override
        public Void execute() {
          jpaEntityServiceTest.save(createOperationContext("26"), person);
          jpaEntityServiceTest.flush();
          throw new IllegalArgumentException();
        }
      });
    } catch (IllegalArgumentException e) {
      exceptionThrown = true;
    }
    assertThat(exceptionThrown, is(true));

    table = actualDataSet.getTable("test_persons");
    index = getTableIndexForId(table, person.getId());
    assertThat((String) table.getValue(index, "id"), is("person_1"));
    assertThat((String) table.getValue(index, "firstName"), is("Yohann"));
    assertThat((String) table.getValue(index, "lastUpdatedBy"), is("1"));
  }

  /**
   * Create a user.
   * @param userId
   * @return
   */
  private static UserDetail createUser(String userId) {
    UserDetail user = new UserDetail();
    user.setId(userId);
    return user;
  }

  /**
   * Create a user.
   * @param userId
   * @return
   */
  private static OperationContext createOperationContext(String userId) {
    return OperationContext.fromUser(createUser(userId));
  }
}
