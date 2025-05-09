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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.notification.user.delayed.delegate;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.delayed.DelayedNotificationProvider;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.integration.rule.DbSetupRule;
import org.silverpeas.core.util.Charsets;

import java.io.InputStream;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Arquillian.class)
public class DelayedNotificationDelegateIT {

  private static final Integer USER_ID = 1;
  private static final Integer FROM_USER_ID = 1;
  private static final String RESOURCE_DATA_ID_1 = "10";
  private static final String RESOURCE_DATA_TYPE = "publication";
  private static final String RESOURCE_DATA_NAME_1 = "Test resource name";
  private static final String RESOURCE_DATA_DESCRIPTION_1 = "Test resource description";
  private static final String RESOURCE_DATA_LOCATION_1 = "Test > Resource > Location";
  private static final String RESOURCE_DATA_URL_1 = "Test resource URL";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("insert-script.sql");

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DelayedNotificationDelegateIT.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addNotificationFeatures()
        .addPublicationTemplateFeatures()
        .testFocusedOn(warBuilder -> {
          warBuilder.addPackages(true, "org.silverpeas.core.notification.user");
          warBuilder.addAsResource("org/silverpeas/core/notification/user/delayed");
        }).build();
  }

  @Test
  public void newNotification() throws Exception {

    // Has to be delayed
    DelayedNotificationData dndTest = buildValidDelayedNotificationData();
    assertNewNotificationIsSent(dndTest, 0);

    // Has to be sent because is flagged to be sent immediately
    dndTest = buildValidDelayedNotificationData();
    dndTest.setSendImmediately(true);
    assertNewNotificationIsSent(dndTest, 1);

    // Has to be sent because of a bad user id
    dndTest = buildValidDelayedNotificationData();
    dndTest.setUserId((Integer) null);
    assertNewNotificationIsSent(dndTest, 1);

    // Has to be sent because of a bad from user id
    dndTest = buildValidDelayedNotificationData();
    dndTest.setFromUserId(null);
    assertNewNotificationIsSent(dndTest, 1);

    // Has to be sent because of a bad channel
    dndTest = buildValidDelayedNotificationData();
    final List<NotifChannel> channels = new ArrayList<>(Arrays.asList(NotifChannel.values()));
    channels.remove(NotifChannel.SMTP);
    channels.add(null); //=> I'm sorry but it is a bug! How we can know to send a message if we
    // don't know the channel through which it can be sent!
    for (final NotifChannel channel : channels) {
      dndTest.setChannel(channel);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad action
    dndTest = buildValidDelayedNotificationData();
    dndTest.setAction(null);
    assertNewNotificationIsSent(dndTest, 1);

    // Has to be sent because of a bad priority
    dndTest = buildValidDelayedNotificationData();
    for (final int priority : new int[]{NotificationParameters.PRIORITY_ERROR,
        NotificationParameters.PRIORITY_URGENT,
        -1, 3, 7, 9}) {
      dndTest.getNotificationParameters().setMessagePriority(priority);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad frequency
    dndTest = buildValidDelayedNotificationData();
    for (final int userId : new int[]{10, -100}) {
      dndTest.setUserId(userId);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad language
    dndTest = buildValidDelayedNotificationData();
    for (final String language : new String[]{null, "", "       "}) {
      dndTest.setLanguage(language);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad resource id
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceId : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceId(resourceId);
      assertNewNotificationIsSent(dndTest, 1);
    }
    dndTest.getResource().setResourceId((Integer) null);
    assertNewNotificationIsSent(dndTest, 1);

    // Has to be sent because of a bad resource type
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceType : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceType(resourceType);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad resource name
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceName : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceName(resourceName);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad resource location
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceLocation : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceLocation(resourceLocation);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad resource url
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceUrl : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceUrl(resourceUrl);
      assertNewNotificationIsSent(dndTest, 1);
    }

    // Has to be sent because of a bad resource component instance id
    dndTest = buildValidDelayedNotificationData();
    for (final String componentInstanceId : new String[]{null, "", "       "}) {
      dndTest.getResource().setComponentInstanceId(componentInstanceId);
      assertNewNotificationIsSent(dndTest, 1);
    }
  }

  private void assertNewNotificationIsSent(
      final DelayedNotificationData delayedNotificationDataTest, final int nbExpectedResults)
      throws Exception {
    final DelayedNotificationDelegateStub stub = new DelayedNotificationDelegateStub();
    stub.performNewNotificationSending(delayedNotificationDataTest);
    assertThat(stub.sendedList.size(), is(nbExpectedResults));
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
    dndTest.setResource(buildNotificationResourceData());
    return dndTest;
  }

  private static NotificationResourceData buildNotificationResourceData() {
    final NotificationResourceData data = new NotificationResourceData();
    data.setResourceId(DelayedNotificationDelegateIT.RESOURCE_DATA_ID_1);
    data.setResourceType(DelayedNotificationDelegateIT.RESOURCE_DATA_TYPE);
    data.setResourceName(DelayedNotificationDelegateIT.RESOURCE_DATA_NAME_1);
    data.setResourceDescription(DelayedNotificationDelegateIT.RESOURCE_DATA_DESCRIPTION_1);
    data.setResourceLocation(DelayedNotificationDelegateIT.RESOURCE_DATA_LOCATION_1);
    data.setResourceUrl(DelayedNotificationDelegateIT.RESOURCE_DATA_URL_1);
    data.setComponentInstanceId("aComponentInstanceId");
    return data;
  }

  @Test
  public void delayedNotifications_1() throws Exception {
    // Forcing
    assertDelayedNotificationsAreForciblySent(1, 53);
    assertDelayedNotificationsAreForciblySent(1, 55);
  }

  @Test
  public void delayedNotifications_2() throws Exception {
    // Forcing
    assertDelayedNotificationsAreForciblySent(9, null);
    assertDelayedNotificationsAreForciblySent(0, 53);
    assertDelayedNotificationsAreForciblySent(0, 55);
  }

  @Test
  public void delayedNotifications_3() throws Exception {
    // Weekly
    assertDelayedNotificationsAreSent(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"),
        new int[]{51, 53, 54}, "fr");
    // Checks
    assertDelayedNotificationsAreForciblySent(0, 53);
    assertDelayedNotificationsAreForciblySent(1, 55);
  }

  @Test
  public void delayedNotifications_3Bis() throws Exception {
    assertDelayedNotificationsAreSent(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"),
        new int[]{51, 53, 54}, "en");
  }

  @Test
  public void delayedNotifications_3Ter() throws Exception {
    assertDelayedNotificationsAreSent(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"),
        new int[]{51, 53, 54}, "de");
  }

  private void assertDelayedNotificationsAreForciblySent(final int expectedSendingNb,
      final Integer userId) throws Exception {
    final DelayedNotificationDelegateStub stub = new DelayedNotificationDelegateStub();
    if (userId != null) {
      stub.forceDelayedNotificationsSending(Collections.singletonList(userId),
          getAimedChannelsBase());
    } else {
      stub.forceDelayedNotificationsSending();
    }
    assertThat(stub.sendedList.size(), is(expectedSendingNb));
  }

  private void assertDelayedNotificationsAreSent(final Date date, final int[] userIds,
      final String language) throws Exception {
    for (final Integer userId : userIds) {
      final Map<NotifChannel, List<DelayedNotificationData>> dndMap =
          DelayedNotificationProvider.getDelayedNotification().
              findDelayedNotificationByUserIdGroupByChannel(userId,
                  DelayedNotificationProvider.getDelayedNotification().getWiredChannels());
      for (final List<DelayedNotificationData> dndList : dndMap.values()) {
        for (final DelayedNotificationData dnd : dndList) {
          dnd.setLanguage(language);
          DelayedNotificationProvider.getDelayedNotification().saveDelayedNotification(dnd);
        }
      }
    }

    final DelayedNotificationDelegateStub stub = new DelayedNotificationDelegateStub();
    stub.performDelayedNotificationsSending(date, getAimedChannelsBase());
    assertThat(stub.sendedList.size(), is(userIds.length));
    for (int i = 0; i < userIds.length; i++) {
      try (InputStream expected = DelayedNotificationDelegateIT.class.getClassLoader()
          .getResourceAsStream(
              "org/silverpeas/core/notification/user/delayed/result-synthese-" + userIds[i] +
                  "-" + language + ".txt")) {
        assertThat(expected, notNullValue());
        String expectedNotif = IOUtils.toString(expected, Charsets.UTF_8).replaceAll("[\r\n\t]",
            "");
        String actualNotif = stub.sendedList.get(i).getMessage().replaceAll("[\r\n\t]", "");
        assertThat(actualNotif, is(expectedNotif));
      }
    }
  }

  private Set<NotifChannel> getAimedChannelsBase() {
    return new HashSet<>(List.of(NotifChannel.SMTP));
  }

  /**
   * Stub
   *
   * @author Yohann Chastagnier
   */
  private static class DelayedNotificationDelegateStub extends DelayedNotificationDelegate {

    // Fetching of the notifications that were sent
    final protected List<NotificationData> sendedList = new ArrayList<>();

    /**
     * Default constructor
     */
    private DelayedNotificationDelegateStub() {
      super();
    }

    @Override
    protected UserDetail getUserDetail(final Integer userId) {
      final UserDetail userDetailStub = new UserDetail();
      userDetailStub.setId(userId.toString());
      if (userId >= 0) {
        userDetailStub.setFirstName("User");
        userDetailStub.setLastName("" + userId);
        userDetailStub.setEmailAddress("user" + userId + "@tests.com");
      }
      return userDetailStub;
    }

    @Override
    protected void sendNotification(final NotificationData notificationData) {
      sendedList.add(notificationData);
    }
  }
}
