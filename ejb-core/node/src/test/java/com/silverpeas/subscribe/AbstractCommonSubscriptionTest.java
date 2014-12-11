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
package com.silverpeas.subscribe;

import com.silverpeas.jndi.SimpleMemoryContextFactory;
import com.silverpeas.subscribe.constant.SubscriberType;
import com.silverpeas.subscribe.mock.OrganizationControllerMock;
import com.silverpeas.subscribe.service.StubbedResourceSubscriptionService;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public abstract class AbstractCommonSubscriptionTest {

  protected static final String GROUPID_WITH_ONE_USER = "55";
  protected static final String USERID_OF_GROUP_WITH_ONE_USER = "userFromGroupOnly_GroupId_55";
  protected static final String INSTANCE_ID = "kmelia60";

  protected final static SubscriberType[] validSubscriberTypes = SubscriberType.getValidValues()
      .toArray(new SubscriberType[SubscriberType.getValidValues().size()]);

  // Spring context
  private ClassPathXmlApplicationContext context;

  protected OrganizationController getMockedOrganizationController() {
    return organizationController;
  }

  private OrganizationController organizationController;

  @BeforeClass
  public static void setUpClass() throws Exception {
    SimpleMemoryContextFactory.setUpAsInitialContext();
  }

  @AfterClass
  public static void tearDownClass() {
    SimpleMemoryContextFactory.tearDownAsInitialContext();
  }

  @Before
  public void setUp() throws Exception {
    context = new ClassPathXmlApplicationContext("spring-subscription.xml");

    // Beans
    final DataSource dataSource = (DataSource) context.getBean("dataSource");
    organizationController = (OrganizationControllerMock) context.getBean("organizationController");

    // Database
    InitialContext ic = new InitialContext();
    ic.bind(JNDINames.SUBSCRIBE_DATASOURCE, dataSource);
    DatabaseOperation.INSERT
        .execute(new DatabaseConnection(dataSource.getConnection()), getDataSet());
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  protected IDataSet getDataSet() throws DataSetException {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSetBuilder().build(
        this.getClass().getClassLoader()
            .getResourceAsStream("com/silverpeas/subscribe/service/node-actors-test-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    return dataSet;
  }

  @After
  public void tearDown() throws Exception {
    DBUtil.clearTestInstance();
    InitialContext ic = new InitialContext();
    ic.unbind(JNDINames.SUBSCRIBE_DATASOURCE);
    context.close();
  }


  protected void initializeUsersAndGroups() {
    when(organizationController.getAllUsersOfGroup(anyString()))
        .thenAnswer(new Answer<UserDetail[]>() {

          @Override
          public UserDetail[] answer(final InvocationOnMock invocation) throws Throwable {
            String groupId = (String) invocation.getArguments()[0];
            UserDetail user = new UserDetail();
            if (GROUPID_WITH_ONE_USER.equals(groupId)) {
              user.setId(USERID_OF_GROUP_WITH_ONE_USER);
              return new UserDetail[]{user};
            } else {
              return new UserDetail[]{};
            }
          }
        });

    when(organizationController.getAllGroupIdsOfUser(anyString()))
        .thenAnswer(new Answer<String[]>() {

          @Override
          public String[] answer(final InvocationOnMock invocation) throws Throwable {
            String userId = (String) invocation.getArguments()[0];
            if (USERID_OF_GROUP_WITH_ONE_USER.equals(userId)) {
              return new String[]{GROUPID_WITH_ONE_USER};
            } else {
              return new String[]{};
            }
          }
        });
  }

  @SuppressWarnings("unchecked")
  protected <T> T getBeanFromContext(Class<T> clazz) {
    if (clazz.getName().equals(ResourceSubscriptionService.class.getName())) {
      return (T) new StubbedResourceSubscriptionService();
    }
    return context.getBean(clazz);
  }
}