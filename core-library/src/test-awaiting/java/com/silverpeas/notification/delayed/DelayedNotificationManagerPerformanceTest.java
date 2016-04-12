/*
 * Copyright (C) 2000 - 2013 Silverpeas
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.usernotification.delayed;

import com.silverpeas.usernotification.delayed.model.DelayedNotificationData;
import com.silverpeas.usernotification.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
    locations = {"/spring-delayed-notification.xml", "/spring-delayed-notification-datasource.xml"})
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class DelayedNotificationManagerPerformanceTest {
  private static ReplacementDataSet dataSet;

  Logger logger = Logger.getLogger(DelayedNotificationManagerPerformanceTest.class.getName());

  public DelayedNotificationManagerPerformanceTest() {
  }

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    final FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet = new ReplacementDataSet(builder.build(DelayedNotificationManagerPerformanceTest.class.
        getResourceAsStream("delayed-notification-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Inject
  private DelayedNotification manager;
  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @Before
  public void generalSetUp() throws Exception {
    final IDatabaseConnection myConnection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(myConnection, dataSet);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  @Test
  public void testDeleteDelayedNotifications() throws Exception {
    Collection<Long> ids = loadDelayedNorifications(1000);

    long start = System.currentTimeMillis();
    assertThat(manager.deleteDelayedNotifications(ids), is(1000));
    long end = System.currentTimeMillis();

    logger.log(Level.INFO, "Deleting delayed notifications in {0} milliseconds.",
        new BigDecimal(String.valueOf(end - start)));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Collection<Long> loadDelayedNorifications(int size) {
    NotificationResourceData notificationResourceData =
        manager.getExistingResource("100", "publication", "aComponentInstanceId");

    // 10000
    long start = System.currentTimeMillis();
    Collection<Long> result = new ArrayList<Long>(size);
    for (long i = 0; i < size; i++) {
      DelayedNotificationData data = buildDelayedNotificationData(notificationResourceData);
      assertThat(data.getId(), nullValue());
      manager.saveDelayedNotification(data);
      assertThat(data.getId(), notNullValue());
      result.add(data.getId());
    }
    long end = System.currentTimeMillis();

    logger.log(Level.INFO, "Loading database in {0} seconds.",
        new BigDecimal(String.valueOf(end - start))
            .divide(new BigDecimal("1000"), 2, BigDecimal.ROUND_HALF_DOWN));

    return result;
  }

  private static DelayedNotificationData buildDelayedNotificationData(
      NotificationResourceData notificationResourceData) {
    final DelayedNotificationData data = new DelayedNotificationData();
    data.setUserId(26);
    data.setFromUserId(38);
    data.setChannel(NotifChannel.SMTP);
    data.setAction(NotifAction.CREATE);
    data.setResource(notificationResourceData);
    data.setLanguage("fr");
    data.setMessage("Ceci est un message !");
    return data;
  }
}
