/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.notification.user.delayed.delegate;

import org.silverpeas.core.notification.user.delayed.DelayedNotificationProvider;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.server.NotificationData;
import org.silverpeas.core.notification.user.server.NotificationServerException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(Arquillian.class)
public class DelayedNotificationDelegateTest {

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
    return WarBuilder4LibCore.onWarForTestClass(DelayedNotificationDelegateTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addNotificationFeatures()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.notification.user");
          warBuilder.addAsResource("org/silverpeas/core/notification/user/delayed");
        }).build();
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
    final List<NotifChannel> channels = new ArrayList<>(Arrays.asList(NotifChannel.values()));
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
    for (final int priority : new int[]{NotificationParameters.ERROR, NotificationParameters.URGENT,
        -1, 3, 7, 9}) {
      dndTest.getNotificationParameters().iMessagePriority = priority;
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad frequency
    dndTest = buildValidDelayedNotificationData();
    for (final int userId : new int[]{10, -100}) {
      dndTest.setUserId(userId);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad language
    dndTest = buildValidDelayedNotificationData();
    for (final String language : new String[]{null, "", "       "}) {
      dndTest.setLanguage(language);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource id
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceId : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceId(resourceId);
      assertNewNotification(dndTest, 1);
    }
    dndTest.getResource().setResourceId((Integer) null);
    assertNewNotification(dndTest, 1);

    // Has to be sent because of a bad resource type
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceType : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceType(resourceType);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource name
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceName : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceName(resourceName);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource location
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceLocation : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceLocation(resourceLocation);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource url
    dndTest = buildValidDelayedNotificationData();
    for (final String resourceUrl : new String[]{null, "", "       "}) {
      dndTest.getResource().setResourceUrl(resourceUrl);
      assertNewNotification(dndTest, 1);
    }

    // Has to be sent because of a bad resource component instance id
    dndTest = buildValidDelayedNotificationData();
    for (final String componentInstanceId : new String[]{null, "", "       "}) {
      dndTest.getResource().setComponentInstanceId(componentInstanceId);
      assertNewNotification(dndTest, 1);
    }
  }

  private DelayedNotificationDelegateStub assertNewNotification(
      final DelayedNotificationData delayedNotificationDataTest, final int nbExpectedResults)
      throws Exception {
    final DelayedNotificationDelegateStub stub = new DelayedNotificationDelegateStub();
    stub.performNewNotificationSending(delayedNotificationDataTest);
    assertThat(stub.sendedList.size(), is(nbExpectedResults));
    return stub;
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
    dndTest.setResource(
        buildNotificationResourceData(RESOURCE_DATA_ID_1, RESOURCE_DATA_TYPE, RESOURCE_DATA_NAME_1,
            RESOURCE_DATA_DESCRIPTION_1, RESOURCE_DATA_LOCATION_1, RESOURCE_DATA_URL_1));
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
    assertDelayedNotifications(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"),
        new int[]{51, 53, 54}, "fr");
    // Checks
    assertDelayedNotifications(0, 53);
    assertDelayedNotifications(1, 55);
  }

  @Test
  public void testDelayedNotifications_3Bis() throws Exception {
    assertDelayedNotifications(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"),
        new int[]{51, 53, 54}, "en");
  }

  @Test
  public void testDelayedNotifications_3Ter() throws Exception {
    assertDelayedNotifications(java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125"),
        new int[]{51, 53, 54}, "de");
  }

  private DelayedNotificationDelegateStub assertDelayedNotifications(final int nbExpectedSendings,
      final Integer userId) throws Exception {
    final DelayedNotificationDelegateStub stub = new DelayedNotificationDelegateStub();
    if (userId != null) {
      stub.forceDelayedNotificationsSending(Collections.singletonList(userId),
          getAimedChannelsBase());
    } else {
      stub.forceDelayedNotificationsSending();
    }
    assertThat(stub.sendedList.size(), is(nbExpectedSendings));
    return stub;
  }

  private DelayedNotificationDelegateStub assertDelayedNotifications(final Date date,
      final int[] userIds, final String language) throws Exception {

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
      try (InputStream expected = DelayedNotificationDelegateTest.class.getClassLoader()
          .getResourceAsStream(
              "org/silverpeas/core/notification/user/delayed/result-synthese-" + userIds[i] +
                  "-" + language + ".txt")) {
        String expectedNotif = IOUtils.toString(expected, Charsets.UTF_8).replaceAll("[\r\n\t]", "");
        String actualNotif = stub.sendedList.get(i).getMessage().replaceAll("[\r\n\t]", "");
        assertThat(actualNotif, is(expectedNotif));
      }
    }
    return stub;
  }

  private Set<NotifChannel> getAimedChannelsBase() {
    return new HashSet<>(Arrays.asList(new NotifChannel[]{NotifChannel.SMTP}));
  }

  /**
   * Stub
   * @author Yohann Chastagnier
   */
  private class DelayedNotificationDelegateStub extends DelayedNotificationDelegate {

    // Récupération des envoyés
    final protected List<NotificationData> sendedList = new ArrayList<>();

    /**
     * Default constructor
     */
    private DelayedNotificationDelegateStub() {
      super();
    }

    @Override
    protected UserDetail getUserDetail(final Integer userId) throws Exception {
      final UserDetail userDetailStub = new UserDetail();
      userDetailStub.setId(userId.toString());
      if (userId >= 0) {
        userDetailStub.setFirstName("User");
        userDetailStub.setLastName("" + userId);
        userDetailStub.seteMail("user" + userId + "@tests.com");
      }
      return userDetailStub;
    }

    @Override
    protected void sendNotification(final NotificationData notificationData)
        throws NotificationServerException {
      sendedList.add(notificationData);
    }
  }
}
