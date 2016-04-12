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

package com.silverpeas.social.dao;

import com.silverpeas.social.mock.OrganizationControllerMock;
import org.silverpeas.core.socialnetwork.model.ExternalAccount;
import org.silverpeas.core.socialnetwork.model.SocialNetworkID;
import org.silverpeas.core.socialnetwork.service.SocialNetworkService;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.silverpeas.core.admin.user.constant.UserState;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.sql.DataSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

/**
 * @author lbertin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/spring-socialnetwork.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class SocialNetworkServiceLinkedInTest {

  @Inject
  private SocialNetworkService service = null;
  @Inject
  private DataSource dataSource;

  @Inject
  OrganizationControllerMock organizationController;

  public SocialNetworkServiceLinkedInTest() {
  }

  @Before
  public void generalSetUp() throws Exception {
    ReplacementDataSet dataSet = new ReplacementDataSet(new FlatXmlDataSet(
        SocialNetworkServiceLinkedInTest.class.getClassLoader()
            .getResourceAsStream("com/silverpeas/social/dao/socialnetwork-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    doAnswer(new Answer<UserDetail>() {
      @Override
      public UserDetail answer(InvocationOnMock invocation) throws Throwable {
        UserDetail user = new UserDetail();
        String userId = (String) invocation.getArguments()[0];
        if ("11".equals(userId)) {
          user.setState(UserState.VALID);
        } else if ("12".equals(userId)) {
          user.setState(UserState.DELETED);
        } else {
          user = null;
        }
        return user;
      }
    }).when(organizationController.getMock()).getUserDetail(anyString());
  }

  @Test
  @Transactional
  public void testReadByPrimaryKeyUnexistingUser() throws Exception {
    ExternalAccount account = service.getExternalAccount(SocialNetworkID.LINKEDIN, "1233");
    assertThat(account, nullValue());
  }

  @Test
  @Transactional
  public void testReadByPrimaryKeyValidUser() throws Exception {
    ExternalAccount account = service.getExternalAccount(SocialNetworkID.LINKEDIN, "1234");
    assertThat(account.getSilverpeasUserId(), is("11"));
  }

  @Test
  @Transactional
  public void testReadByPrimaryKeyDeletedUser() throws Exception {
    ExternalAccount account = service.getExternalAccount(SocialNetworkID.LINKEDIN, "1235");
    assertThat(account, nullValue());
  }

}
