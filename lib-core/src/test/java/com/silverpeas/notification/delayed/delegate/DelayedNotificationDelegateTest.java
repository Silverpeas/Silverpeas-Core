/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.notification.delayed.delegate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
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

import com.silverpeas.notification.delayed.DelayedNotificationFactory;
import com.silverpeas.notification.delayed.model.DelayedNotificationData;
import com.silverpeas.notification.model.NotificationResourceData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.constant.NotifAction;
import com.stratelia.silverpeas.notificationManager.constant.NotifChannel;
import com.stratelia.silverpeas.notificationserver.NotificationData;
import com.stratelia.silverpeas.notificationserver.NotificationServerException;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/spring-delayed-notification.xml",
    "/spring-delayed-notification-datasource.xml" })
@TransactionConfiguration(transactionManager = "jpaTransactionManager")
public class DelayedNotificationDelegateTest {

  private static final Integer USER_ID = 1;
  private static final Integer FROM_USER_ID = 1;

  private static final String RESOURCE_DATA_ID_1 = "10";
  private static final String RESOURCE_DATA_TYPE = "publication";
  private static final String RESOURCE_DATA_NAME_1 = "Test resource name";
  private static final String RESOURCE_DATA_DESCRIPTION_1 = "Test resource description";
  private static final String RESOURCE_DATA_LOCATION_1 = "Test > Resource > Location";
  private static final String RESOURCE_DATA_URL_1 = "Test resource URL";

  private static ReplacementDataSet dataSet;

  public DelayedNotificationDelegateTest() {
  }

  @BeforeClass
  public static void prepareDataSet() throws Exception {
    final FlatXmlDataSetBuilder builder = new FlatXmlDataSetBuilder();
    dataSet =
        new ReplacementDataSet(
            builder
                .build(DelayedNotificationDelegateTest.class
                    .getClassLoader()
                    .getResourceAsStream(
                        "com/silverpeas/notification/delayed/delayed-notification-dataset.xml")));
    dataSet.addReplacementObject("[NULL]", null);
  }

  @Inject
  @Named("jpaDataSource")
  private DataSource dataSource;

  @Before
  public void generalSetUp() throws Exception {
    final IDatabaseConnection myConnection = new DatabaseConnection(dataSource.getConnection());
    DatabaseOperation.CLEAN_INSERT.execute(myConnection, dataSet);
    DBUtil.getInstanceForTest(dataSource.getConnection());
  }

  @Test
  public void testNewNotification() throws Exception {

    // Has to be delayed
    DelayedNotificationData dndTest = buildValidDelayedNotificationData();
    assertNewNotification(dndTest, 0);

    // Has to be sent because is flagged to be sent immediately
    dndTest = buildValidDelayedNotificationData();
    dndTest.setSendImmediately(true);
    assertNewNotification(dndTest, 1);

    // Has to be sent because of a bad user id
    dndTest = buildValidDelayedNotificationData();
    dndTest.setUserId((Integer) null);
    assertNewNotification(dndTest, 1);

    // Has to be sent because of a bad from user id
    dndTest = buildValidDelayedNotificationData();
    dndTest.setFromUserId((Integer) null);
    assertNewNotification(dndTest, 1);

    // Has to be sent because of a bad channel
    dndTest = buildValidDelayedNotificationData();
    final List<NotifChannel> channels =
        new ArrayList<NotifChannel>(Arrays.asList(NotifChannel.values()));
    channels.remove(NotifChannel.SMTP);
    channels.add(null);
    for (final NotifChannel channel : channels) {
      dndTest.setChannel(channel);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad action
    dndTest = buildValidDelayedNotificationData();
    dndTest.setAction(null);
    assertNewNotification(dndTest, 1);

    // Has to be sent because of a bad priority
    dndTest = buildValidDelayedNotificationData();
    for (final int priority : new int[] { NotificationParameters.ERROR,
        NotificationParameters.URGENT, -1, 3, 7, 9 }) {
      dndTest.getNotificationParameters().iMessagePriority = priority;
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad frequency
    dndTest = buildValidDelayedNotificationData();
    for (final int userId : new int[] { 10, -100 }) {
      dndTest.setUserId(userId);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad language
    dndTest = buildValidDelayedNotificationData();
    for (final String language : new String[] { null, "", "       " }) {
      dndTest.setLanguage(language);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource id
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceId : new String[] { null, "", "       " }) {
      dndTest.getResource().setResourceId(resourceId);
      assertNewNotification(dndTest, 1);
    }
    dndTest.getResource().setResourceId((Integer) null);
    assertNewNotification(dndTest, 1);

    // Has to be sent because of a bad resource type
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceType : new String[] { null, "", "       " }) {
      dndTest.getResource().setResourceType(resourceType);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource name
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceName : new String[] { null, "", "       " }) {
      dndTest.getResource().setResourceName(resourceName);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource location
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceLocation : new String[] { null, "", "       " }) {
      dndTest.getResource().setResourceLocation(resourceLocation);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource url
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceUrl : new String[] { null, "", "       " }) {
      dndTest.getResource().setResourceUrl(resourceUrl);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource component instance id
    dndTest = buildValidDelayedNotificationData();
    for (final String componentInstanceId : new String[] { null, "", "       " }) {
      dndTest.getResource().setComponentInstanceId(componentInstanceId);
      assertNewNotification(dndTest, 1);
    }
  }

  private DelayedNotificationDelegateMock assertNewNotification(
      final DelayedNotificationData delayedNotificationDataTest, final int nbExpectedResults)
      throws Exception {
    final DelayedNotificationDelegateMock mock = new DelayedNotificationDelegateMock();
    mock.performNewNotificationSending(delayedNotificationDataTest);
    assertThat(mock.sendedList.size(), is(nbExpectedResults));
    return mock;
  }

  private DelayedNotificationData buildValidDelayedNotificationData() {
    final DelayedNotificationData dndTest = new DelayedNotificationData();
    final NotificationParameters notificationParameters = new NotificationParameters();
    dndTest.setNotificationParameters(notificationParameters);

    // User id
    dndTest.setUserId(USER_ID);

    // From user id
    dndTest.setFromUserId(FROM_USER_ID);

    // Channel
    dndTest.setChannel(NotifChannel.SMTP);

    // Action
    dndTest.setAction(NotifAction.CREATE);

    // Language
    dndTest.setLanguage("fr");

    // Resource data
    dndTest.setResource(buildNotificationResourceData(RESOURCE_DATA_ID_1, RESOURCE_DATA_TYPE,
        RESOURCE_DATA_NAME_1, RESOURCE_DATA_DESCRIPTION_1, RESOURCE_DATA_LOCATION_1,
        RESOURCE_DATA_URL_1));
    return dndTest;
  }

  private static NotificationResourceData buildNotificationResourceData(final String resourceId,
      final String resourceType, final String resourceName, final String resourceDescription,
      final String resourceLocation, final String resourceUrl) {
    final NotificationResourceData data = new NotificationResourceData();
    data.setResourceId(resourceId);
    data.setResourceType(resourceType);
    data.setResourceName(resourceName);
    data.setResourceDescription(resourceDescription);
    data.setResourceLocation(resourceLocation);
    data.setResourceUrl(resourceUrl);
    data.setComponentInstanceId("aComponentInstanceId");
    return data;
  }

  @Test
  public void testDelayedNotifications_1() throws Exception {
    // Forcings
    assertDelayedNotifications(1, 53);
    assertDelayedNotifications(1, 55);
  }

  @Test
  public void testDelayedNotifications_2() throws Exception {
    // Forcings
    assertDelayedNotifications(9, null);
    assertDelayedNotifications(0, 53);
    assertDelayedNotifications(0, 55);
  }

  @Test
  public void testDelayedNotifications_3() throws Exception {
    // Weekly
    assertDelayedNotifications(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"), new int[] {
        51, 53, 54 }, "fr");
    // Checks
    assertDelayedNotifications(0, 53);
    assertDelayedNotifications(1, 55);
  }

  @Test
  public void testDelayedNotifications_3Bis() throws Exception {
    assertDelayedNotifications(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"), new int[] {
        51, 53, 54 }, "en");
  }

  private DelayedNotificationDelegateMock assertDelayedNotifications(final int nbExpectedSendings,
      final Integer userId)
      throws Exception {
    final DelayedNotificationDelegateMock mock = new DelayedNotificationDelegateMock();
    if (userId != null) {
      mock.forceDelayedNotificationsSending(Collections.singletonList(userId),
          getAimedChannelsBase());
    } else {
      mock.forceDelayedNotificationsSending();
    }
    assertThat(mock.sendedList.size(), is(nbExpectedSendings));
    return mock;
  }

  private DelayedNotificationDelegateMock assertDelayedNotifications(
      final Date date,
      final int[] userIds,
      final String language)
      throws Exception {

    for (final Integer userId : userIds) {
      final Map<NotifChannel, List<DelayedNotificationData>> dndMap =
          DelayedNotificationFactory.getDelayedNotification().findDelayedNotificationByUserIdGroupByChannel(userId,
              DelayedNotificationFactory.getDelayedNotification().getWiredChannels());
      for (final List<DelayedNotificationData> dndList : dndMap.values()) {
        for (final DelayedNotificationData dnd : dndList) {
          dnd.setLanguage(language);
          DelayedNotificationFactory.getDelayedNotification().saveDelayedNotification(dnd);
        }
      }
    }

    final DelayedNotificationDelegateMock mock = new DelayedNotificationDelegateMock();
    mock.performDelayedNotificationsSending(date, getAimedChannelsBase());
    assertThat(mock.sendedList.size(), is(userIds.length));
    for (int i = 0; i < userIds.length; i++) {
      assertThat(
          mock.sendedList.get(i).getMessage().replaceAll("[\r\n\t]", ""),
          is(IOUtils.toString(
              DelayedNotificationDelegateTest.class.getClassLoader().getResourceAsStream(
                  "com/silverpeas/notification/delayed/result-synthese-" + userIds[i] + "-" + language + ".txt"),
              "UTF-8")
              .replaceAll("[\r\n\t]", "")));
    }
    return mock;
  }

  private Set<NotifChannel> getAimedChannelsBase() {
    return new HashSet<NotifChannel>(Arrays.asList(new NotifChannel[] { NotifChannel.SMTP }));
  }

  /**
   * Mock
   * @author Yohann Chastagnier
   */
  private class DelayedNotificationDelegateMock extends DelayedNotificationDelegate {

    // Récupération des envoyés
    final protected List<NotificationData> sendedList = new ArrayList<NotificationData>();

    /**
     * Default constructor
     */
    private DelayedNotificationDelegateMock() {
      super();
    }

    @Override
    protected UserDetail getUserDetail(final Integer userId) throws Exception {
      final UserDetail userDetailMock = new UserDetail();
      userDetailMock.setId(userId.toString());
      if (userId >= 0) {
        userDetailMock.setFirstName("User");
        userDetailMock.setLastName("" + userId);
        userDetailMock.seteMail("user" + userId + "@tests.com");
      }
      return userDetailMock;
    }

    @Override
    protected void sendNotification(final NotificationData notificationData)
        throws NotificationServerException {
      sendedList.add(notificationData);
    }
  }
}
