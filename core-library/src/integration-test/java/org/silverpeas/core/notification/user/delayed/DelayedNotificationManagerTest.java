/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.core.notification.user.delayed;

import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationData;
import org.silverpeas.core.notification.user.delayed.model.DelayedNotificationUserSetting;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.WarBuilder4LibCore;
import org.silverpeas.core.test.rule.DbSetupRule;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(Arquillian.class)
public class DelayedNotificationManagerTest {

  private static final int TEST_CASE_1 = 1;
  private static final int TEST_CASE_2 = 2;
  private static final int TEST_CASE_3 = 3;
  private static final String RESOURCE_DATA_ID_1 = "10";
  private static final String RESOURCE_DATA_TYPE = "publication";
  private static final String RESOURCE_DATA_NAME_1 = "Test resource name";
  private static final String RESOURCE_DATA_DESCRIPTION_1 = "Test resource description";
  private static final String RESOURCE_DATA_LOCATION_1 = "Test > Resource > Location";
  private static final String RESOURCE_DATA_URL_1 = "Test resource URL";
  private static final String RESOURCE_DATA_ID_100 = "100";
  private static final String RESOURCE_DATA_NAME_100 = "Test resource name no desc";
  private static final String RESOURCE_DATA_LOCATION_100 = "Test > Resource > Location no desc";
  private static final String RESOURCE_DATA_URL_100 = "Test resource URL no desc";

  @Rule
  public DbSetupRule dbSetupRule = DbSetupRule.createTablesFrom("create-database.sql")
      .loadInitialDataSetFrom("insert-script.sql");

  @Inject
  private DelayedNotification manager;

  @Deployment
  public static Archive<?> createTestArchive() {
    return WarBuilder4LibCore.onWarForTestClass(DelayedNotificationManagerTest.class)
        .addCommonBasicUtilities()
        .addSilverpeasExceptionBases()
        .addAdministrationFeatures()
        .addNotificationFeatures()
        .testFocusedOn((warBuilder) -> {
          warBuilder.addPackages(true, "org.silverpeas.core.notification.user");
          warBuilder.addAsResource("org/silverpeas/core/notification/user/delayed");
        }).build();
  }

  /*
   * Common
   */
  @Test
  public void testGetPossibleFrequencies() throws Exception {
    final Set<DelayedNotificationFrequency> possibleFrequencies = manager.getPossibleFrequencies();
    assertThat(possibleFrequencies, containsInAnyOrder(DelayedNotificationFrequency.values()));
  }

  /*
   * Resource Data
   */
  @Test
  public void testGetExistingResource() throws Exception {

    // Exists already several resources (extraordinary case)
    NotificationResourceData query = buildNotificationResourceData(TEST_CASE_1);
    NotificationResourceData notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, nullValue());

    // Exists one resource
    query = buildNotificationResourceData(TEST_CASE_2);
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, notNullValue());

    // Exists no resource
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setResourceId(-100);
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, nullValue());

    // Exists no resource
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setResourceType("-Type");
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, nullValue());

    // Exists no resource
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setComponentInstanceId("-ComponentInstanceId");
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, nullValue());

    // Exists one resource (searching data indiscriminate)
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setResourceDescription("-Description");
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, notNullValue());

    // Exists one resource (searching data indiscriminate)
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setResourceLocation("-Location");
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, notNullValue());

    // Exists one resource (searching data indiscriminate)
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setResourceName("-Name");
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, notNullValue());

    // Exists one resource (searching data indiscriminate)
    query = buildNotificationResourceData(TEST_CASE_2);
    query.setResourceUrl("-URL");
    notificationResourceData =
        manager.getExistingResource(query.getResourceId(), query.getResourceType(),
        query.getComponentInstanceId());
    assertThat(notificationResourceData, notNullValue());
  }

  private static NotificationResourceData buildNotificationResourceData(final int testCase) {
    NotificationResourceData data = new NotificationResourceData();
    if (testCase == TEST_CASE_1) {
      data =
          buildNotificationResourceData(RESOURCE_DATA_ID_1, RESOURCE_DATA_TYPE,
          RESOURCE_DATA_NAME_1, RESOURCE_DATA_DESCRIPTION_1, RESOURCE_DATA_LOCATION_1,
          RESOURCE_DATA_URL_1);
    } else if (testCase == TEST_CASE_2 || testCase == TEST_CASE_3) {
      data =
          buildNotificationResourceData(RESOURCE_DATA_ID_100, RESOURCE_DATA_TYPE,
          RESOURCE_DATA_NAME_100, null, RESOURCE_DATA_LOCATION_100,
          RESOURCE_DATA_URL_100);
      if (testCase == TEST_CASE_3) {
        data.setResourceDescription(RESOURCE_DATA_DESCRIPTION_1);
      }
    }
    data.setComponentInstanceId("aComponentInstanceId");
    return data;
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
    return data;
  }

  /*
   * Delayed Notification Data
   */
  @Test
  public void testFindAllUsersToBeNotified() throws Exception {
    Set<NotifChannel> ac = getAimedChannelsBase();
    assertThat(manager.findAllUsersToBeNotified(ac).size(), is(9));
    ac = getAimedChannelsAll();
    assertThat(manager.findAllUsersToBeNotified(ac).size(), is(10));
    ac.clear();
    ac.add(NotifChannel.POPUP);
    assertThat(manager.findAllUsersToBeNotified(ac).size(), is(0));
  }

  @Test
  public void testFindUsersToBeNotified_Daily() throws Exception {
    for (final Date date : new Date[]{java.sql.Timestamp.valueOf("2012-05-08 12:45:23.125"),
          java.sql.Timestamp.valueOf("2012-05-09 12:45:23.125"),
          java.sql.Timestamp.valueOf("2012-05-10 12:45:23.125"),
          java.sql.Timestamp.valueOf("2012-05-11 12:45:23.125"),
          java.sql.Timestamp.valueOf("2012-05-12 12:45:23.125"),
          java.sql.Timestamp.valueOf("2012-05-13 12:45:23.125")}) {
      assertFindUsersToBeNotified_Daily(date);
    }
  }

  /**
   * Centralizing assertions
   *
   * @param date
   * @throws Exception
   */
  private void assertFindUsersToBeNotified_Daily(final Date date) throws Exception {
    Set<NotifChannel> ac;

    // DelayedNotificationFrequency.DAILY
    ac = getAimedChannelsBase();
    assertFindUsersToBeNotified(date, ac, DelayedNotificationFrequency.DAILY, 51, 52);

    // DelayedNotificationFrequency.DAILY && Wrong channel
    ac.clear();
    ac.add(NotifChannel.POPUP);
    assertFindUsersToBeNotified(date, ac, DelayedNotificationFrequency.DAILY);

    // DelayedNotificationFrequency.DAILY && Multi channels
    ac = getAimedChannelsBase();
    ac.add(NotifChannel.SILVERMAIL);
    assertFindUsersToBeNotified(date, ac, DelayedNotificationFrequency.DAILY, 51, 52, 54);

    // Others
    for (final DelayedNotificationFrequency defaultDNF : new DelayedNotificationFrequency[]{
          DelayedNotificationFrequency.NONE, DelayedNotificationFrequency.WEEKLY,
          DelayedNotificationFrequency.MONTHLY}) {

      // OK
      ac = getAimedChannelsBase();
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51);

      // Wrong channel
      ac.clear();
      ac.add(NotifChannel.POPUP);
      assertFindUsersToBeNotified(date, ac, defaultDNF);

      // Multi channels
      ac = getAimedChannelsBase();
      ac.add(NotifChannel.SILVERMAIL);
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51);
    }
  }

  @Test
  public void testFindUsersToBeNotified_Weekly() throws Exception {
    // Monday (not first of month)
    final Date date = java.sql.Timestamp.valueOf("2012-05-14 12:45:23.125");

    Set<NotifChannel> ac;

    // DAILY & WEEKLY
    for (final DelayedNotificationFrequency defaultDNF : new DelayedNotificationFrequency[]{
          DelayedNotificationFrequency.DAILY, DelayedNotificationFrequency.WEEKLY}) {

      // OK
      ac = getAimedChannelsBase();
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 52, 54);

      // Wrong channel
      ac.clear();
      ac.add(NotifChannel.POPUP);
      assertFindUsersToBeNotified(date, ac, defaultDNF);

      // Multi channels
      ac = getAimedChannelsBase();
      ac.add(NotifChannel.SILVERMAIL);
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 52, 54);
    }

    // Others
    for (final DelayedNotificationFrequency defaultDNF : new DelayedNotificationFrequency[]{
          DelayedNotificationFrequency.NONE, DelayedNotificationFrequency.MONTHLY}) {

      // OK
      ac = getAimedChannelsBase();
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 54);

      // Wrong channel
      ac.clear();
      ac.add(NotifChannel.POPUP);
      assertFindUsersToBeNotified(date, ac, defaultDNF);

      // Multi channels
      ac = getAimedChannelsBase();
      ac.add(NotifChannel.SILVERMAIL);
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 54);
    }
  }

  @Test
  public void testFindUsersToBeNotified_Monthly() throws Exception {
    // First of month (not a monday)
    final Date date = java.sql.Timestamp.valueOf("2012-05-01 12:45:23.125");

    Set<NotifChannel> ac;

    // DAILY & MONTHLY
    for (final DelayedNotificationFrequency defaultDNF : new DelayedNotificationFrequency[]{
          DelayedNotificationFrequency.DAILY, DelayedNotificationFrequency.MONTHLY}) {

      // OK
      ac = getAimedChannelsBase();
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 52, 53);

      // Wrong channel
      ac.clear();
      ac.add(NotifChannel.POPUP);
      assertFindUsersToBeNotified(date, ac, defaultDNF);

      // Multi channels
      ac = getAimedChannelsBase();
      ac.add(NotifChannel.SILVERMAIL);
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 52, 53, 54);
    }

    // Others
    for (final DelayedNotificationFrequency defaultDNF : new DelayedNotificationFrequency[]{
          DelayedNotificationFrequency.NONE, DelayedNotificationFrequency.WEEKLY}) {

      // OK
      ac = getAimedChannelsBase();
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 53);

      // Wrong channel
      ac.clear();
      ac.add(NotifChannel.POPUP);
      assertFindUsersToBeNotified(date, ac, defaultDNF);

      // Multi channels
      ac = getAimedChannelsBase();
      ac.add(NotifChannel.SILVERMAIL);
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 53);
    }
  }

  @Test
  public void testFindUsersToBeNotified_All() throws Exception {
    // Monday anf first of month
    final Date date = java.sql.Timestamp.valueOf("2012-10-01 12:45:23.125");

    Set<NotifChannel> ac;

    // DAILY & WEEKLY & MONTHLY
    for (final DelayedNotificationFrequency defaultDNF : new DelayedNotificationFrequency[]{
          DelayedNotificationFrequency.DAILY, DelayedNotificationFrequency.WEEKLY,
          DelayedNotificationFrequency.MONTHLY}) {

      // OK
      ac = getAimedChannelsBase();
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 52, 53, 54);

      // Wrong channel
      ac.clear();
      ac.add(NotifChannel.POPUP);
      assertFindUsersToBeNotified(date, ac, defaultDNF);

      // Multi channels
      ac = getAimedChannelsBase();
      ac.add(NotifChannel.SILVERMAIL);
      assertFindUsersToBeNotified(date, ac, defaultDNF, 51, 52, 53, 54);
    }

    // NONE

    // OK
    ac = getAimedChannelsBase();
    assertFindUsersToBeNotified(date, ac, DelayedNotificationFrequency.NONE, 51, 53, 54);

    // Wrong channel
    ac.clear();
    ac.add(NotifChannel.POPUP);
    assertFindUsersToBeNotified(date, ac, DelayedNotificationFrequency.NONE);

    // Multi channels
    ac = getAimedChannelsBase();
    ac.add(NotifChannel.SILVERMAIL);
    assertFindUsersToBeNotified(date, ac, DelayedNotificationFrequency.NONE, 51, 53, 54);
  }

  /**
   * Centralizing assertions
   *
   * @param date
   * @param aimedChannels
   * @param defaultDelayedNotificationFrequency
   * @param expectedUsers
   */
  private void assertFindUsersToBeNotified(final Date date, final Set<NotifChannel> aimedChannels,
      final DelayedNotificationFrequency defaultDelayedNotificationFrequency,
      final Integer... expectedUsers) {
    Integer[] notExpectedUsers = new Integer[]{51, 52, 53, 54, 55, 56};
    final List<Integer> usersToNotify =
        manager.findUsersToBeNotified(date, aimedChannels, defaultDelayedNotificationFrequency);
    usersToNotify.retainAll(Arrays.asList(notExpectedUsers));
    if (expectedUsers != null && expectedUsers.length > 0) {
      notExpectedUsers =
          ArrayUtils.removeElements(notExpectedUsers, expectedUsers);
    }
    assertThat(usersToNotify, notNullValue());
    if (expectedUsers != null && expectedUsers.length > 0) {
      assertThat(usersToNotify.toArray(new Integer[]{}), arrayContainingInAnyOrder(expectedUsers));
    }
    if (notExpectedUsers.length > 0) {
      assertThat(usersToNotify.toArray(new Integer[]{}),
          not(arrayContainingInAnyOrder(notExpectedUsers)));
    }
  }

  private Set<NotifChannel> getAimedChannelsBase() {
    return EnumSet.of(NotifChannel.SMTP);
  }

  private Set<NotifChannel> getAimedChannelsAll() {
    return EnumSet.allOf(NotifChannel.class);
  }

  @Test
  public void testFindDelayedNotificationByUserIdGroupByChannel() throws Exception {
    Map<NotifChannel, List<DelayedNotificationData>> delayedNotificationDataMap =
        manager.findDelayedNotificationByUserIdGroupByChannel(0, getAimedChannelsAll());
    assertThat(delayedNotificationDataMap, notNullValue());
    assertThat(delayedNotificationDataMap.size(), is(0));

    delayedNotificationDataMap =
        manager.findDelayedNotificationByUserIdGroupByChannel(50, getAimedChannelsAll());
    assertThat(delayedNotificationDataMap, notNullValue());
    assertThat(delayedNotificationDataMap.size(), is(2));

    delayedNotificationDataMap =
        manager.findDelayedNotificationByUserIdGroupByChannel(80, getAimedChannelsAll());
    assertThat(delayedNotificationDataMap, notNullValue());
    assertThat(delayedNotificationDataMap.size(), is(1));
  }

  @Test
  public void testSaveDelayedNotification() throws Exception {
    final NotifChannel[] channels = NotifChannel.values();
    final NotifAction[] actions = NotifAction.values();
    int userIdTest = 1000;
    Map<NotifChannel, List<DelayedNotificationData>> delayedNotificationDataMap;
    final NotificationResourceData notificationResourceData =
        buildNotificationResourceData(TEST_CASE_3);
    DelayedNotificationData newDelayedNotificationData;
    DelayedNotificationData delayedNotificationData;
    int count = 0;
    int count2 = 0;
    Date dateBeforeSave;
    for (int i = 0; i < channels.length; i++) {
      userIdTest += i;
      dateBeforeSave = new Date();
      for (final NotifAction action : actions) {
        newDelayedNotificationData = new DelayedNotificationData();
        newDelayedNotificationData.setUserId(userIdTest);
        newDelayedNotificationData.setFromUserId(2 * userIdTest);
        newDelayedNotificationData.setChannel(channels[i]);
        newDelayedNotificationData.setAction(action);
        newDelayedNotificationData.setLanguage("fr");
        if ((count % 3) == 0) {
          newDelayedNotificationData.setResource(notificationResourceData);
        } else {
          newDelayedNotificationData.setResource(buildNotificationResourceData(TEST_CASE_3));
        }
        if ((count % 2) == 0) {
          newDelayedNotificationData.setMessage("message" + count);
        }
        if ((count % 4) == 0) {
          newDelayedNotificationData.setCreationDate(dateBeforeSave);
        }
        assertThat(newDelayedNotificationData.isValid(), is(true));
        manager.saveDelayedNotification(newDelayedNotificationData);
        manager.saveDelayedNotification(newDelayedNotificationData);
        if ((count % 4) == 0) {
          final String idTest = newDelayedNotificationData.getId();
          if (count == 0) {
            newDelayedNotificationData.setId((Long)null);
          }
          newDelayedNotificationData.getResource().setId((Long)null);
          manager.saveDelayedNotification(newDelayedNotificationData);
          assertThat(idTest, is(newDelayedNotificationData.getId()));
        }
        count++;
      }

      delayedNotificationDataMap =
          manager.findDelayedNotificationByUserIdGroupByChannel(userIdTest, getAimedChannelsAll());
      assertThat(delayedNotificationDataMap, notNullValue());
      assertThat(delayedNotificationDataMap.size(), is(1));
      for (final Map.Entry<NotifChannel, List<DelayedNotificationData>> mapEntry :
          delayedNotificationDataMap
          .entrySet()) {
        assertThat(mapEntry.getValue().size(), is(actions.length));
        for (int j = 0; j < actions.length; j++) {
          delayedNotificationData = mapEntry.getValue().get(j);
          assertThat(delayedNotificationData.getId(), is(String.valueOf(1001l + count2)));
          assertThat(delayedNotificationData.getUserId(), is(userIdTest));
          assertThat(delayedNotificationData.getFromUserId(), is(2 * userIdTest));
          assertThat(delayedNotificationData.getChannel(), is(channels[i]));
          assertThat(delayedNotificationData.getAction(), is(actions[j]));
          assertThat(delayedNotificationData.getResource().getId(), is("10"));
          assertThat(delayedNotificationData.getCreationDate(), notNullValue());
          assertThat(delayedNotificationData.getCreationDate(),
              greaterThanOrEqualTo(dateBeforeSave));
          if ((count2 % 2) == 0) {
            assertThat(delayedNotificationData.getMessage(), is("message" + count2));
          } else {
            assertThat(delayedNotificationData.getMessage(), nullValue());
          }
          count2++;
        }
      }
    }
  }

  @Test
  public void testDeleteDelayedNotifications() throws Exception {
    int nbDeletes = manager.deleteDelayedNotifications(Arrays.asList(new Long[]{}));
    assertThat(nbDeletes, is(0));

    nbDeletes =
        manager.deleteDelayedNotifications(Arrays
        .asList(new Long[]{100l, 200l, 300l, 400l, 500l}));
    assertThat(nbDeletes, is(5));
    nbDeletes =
        manager.deleteDelayedNotifications(Arrays
        .asList(new Long[]{100l, 200l, 300l, 400l, 500l}));
    assertThat(nbDeletes, is(0));
  }

  @Test
  public void testDeleteDelayedNotifications2() throws Exception {
    int nbDeletes =
        manager.deleteDelayedNotifications(Arrays.asList(new Long[]{100l, 200l, 300l, 400l, 500l,
          600l}));
    assertThat(nbDeletes, is(6));
    nbDeletes =
        manager.deleteDelayedNotifications(Arrays.asList(new Long[]{100l, 200l, 300l, 400l, 500l,
          600l}));
    assertThat(nbDeletes, is(0));

  }

  /*
   * User settings
   */
  @Test
  public void testFindDelayedNotificationUserSettingByUserId() throws Exception {
    List<DelayedNotificationUserSetting> delayedNotificationUserSettings =
        manager.findDelayedNotificationUserSettingByUserId(0);
    assertThat(delayedNotificationUserSettings, notNullValue());
    assertThat(delayedNotificationUserSettings.size(), is(0));

    delayedNotificationUserSettings = manager.findDelayedNotificationUserSettingByUserId(1);
    assertThat(delayedNotificationUserSettings, notNullValue());
    assertThat(delayedNotificationUserSettings.size(), is(2));

    delayedNotificationUserSettings = manager.findDelayedNotificationUserSettingByUserId(10);
    assertThat(delayedNotificationUserSettings, notNullValue());
    assertThat(delayedNotificationUserSettings.size(), is(1));
  }

  @Test
  public void testGetDelayedNotificationUserSettingByUserIdAndChannelId() throws Exception {
    DelayedNotificationUserSetting delayedNotificationUserSetting =
        manager.getDelayedNotificationUserSettingByUserIdAndChannel(0, NotifChannel.SMTP);
    assertThat(delayedNotificationUserSetting, nullValue());

    delayedNotificationUserSetting =
        manager.getDelayedNotificationUserSettingByUserIdAndChannel(1, NotifChannel.SMTP);
    assertThat(delayedNotificationUserSetting, notNullValue());

    delayedNotificationUserSetting =
        manager.getDelayedNotificationUserSettingByUserIdAndChannel(10, NotifChannel.SILVERMAIL);
    assertThat(delayedNotificationUserSetting, nullValue());

    delayedNotificationUserSetting =
        manager.getDelayedNotificationUserSettingByUserIdAndChannel(20, NotifChannel.SMTP);
    assertThat(delayedNotificationUserSetting, notNullValue());
    assertThat(delayedNotificationUserSetting.getId(), is("50"));
    assertThat(delayedNotificationUserSetting.getFrequency(),
        is(DelayedNotificationFrequency.WEEKLY));
  }

  @Test
  public void testGetDelayedNotificationUserSetting() throws Exception {
    DelayedNotificationUserSetting delayedNotificationUserSetting =
        manager.getDelayedNotificationUserSetting(1);
    assertThat(delayedNotificationUserSetting, nullValue());

    delayedNotificationUserSetting =
        manager.getDelayedNotificationUserSetting(10);
    assertThat(delayedNotificationUserSetting, notNullValue());
    assertThat(DelayedNotificationFrequency.DAILY,
        is(delayedNotificationUserSetting.getFrequency()));
  }

  @Test
  public void testSaveDelayedNotificationUserSetting() throws Exception {

    final DelayedNotificationFrequency[] codes = DelayedNotificationFrequency.values();
    final NotifChannel[] channels = NotifChannel.values();
    int userIdTest = 1000;
    List<DelayedNotificationUserSetting> myDelayedNotificationUserSettings;
    DelayedNotificationUserSetting delayedNotificationUserSetting;
    int count = 0;
    for (final DelayedNotificationFrequency code : codes) {
      for (final NotifChannel channel : channels) {
        userIdTest += count;

        manager.saveDelayedNotificationUserSetting(userIdTest, channel, code);

        myDelayedNotificationUserSettings =
            manager.findDelayedNotificationUserSettingByUserId(userIdTest);
        assertThat(myDelayedNotificationUserSettings, notNullValue());
        assertThat(myDelayedNotificationUserSettings.size(), is(1));
        delayedNotificationUserSetting =
            myDelayedNotificationUserSettings.iterator().next();
        assertThat(delayedNotificationUserSetting.getId(), is(String.valueOf(101 + count)));
        assertThat(delayedNotificationUserSetting.getUserId(), is(userIdTest));
        assertThat(delayedNotificationUserSetting.getChannel(), is(channel));
        assertThat(delayedNotificationUserSetting.getFrequency(),
            is(code));
        count++;
      }
    }
  }

  @Test
  public void testUpdateDelayedNotificationUserSetting() throws Exception {
    final DelayedNotificationUserSetting delayedNotificationUserSettingTest =
        manager.getDelayedNotificationUserSetting(30);
    assertThat(delayedNotificationUserSettingTest.getId(), is("30"));
    assertThat(delayedNotificationUserSettingTest.getUserId(), is(10));
    assertThat(delayedNotificationUserSettingTest.getChannel(), is(NotifChannel.SMTP));
    assertThat(delayedNotificationUserSettingTest.getFrequency(),
        is(DelayedNotificationFrequency.NONE));

    manager.saveDelayedNotificationUserSetting(10, NotifChannel.SMTP,
        DelayedNotificationFrequency.MONTHLY);

    final DelayedNotificationUserSetting myDelayedNotificationUserSettingReloaded =
        manager.getDelayedNotificationUserSetting(30);
    assertThat("DelayedNotificationUserSetting not found in db",
        myDelayedNotificationUserSettingReloaded, notNullValue());
    assertThat("Same", myDelayedNotificationUserSettingReloaded,
        not(sameInstance(delayedNotificationUserSettingTest)));
    assertThat(myDelayedNotificationUserSettingReloaded.getId(), is("30"));
    assertThat(myDelayedNotificationUserSettingReloaded.getUserId(), is(10));
    assertThat(myDelayedNotificationUserSettingReloaded.getChannel(), is(NotifChannel.SMTP));
    assertThat(myDelayedNotificationUserSettingReloaded.getFrequency(),
        is(DelayedNotificationFrequency.MONTHLY));
  }

  @Test
  public void testDeleteDelayedNotificationUserSetting() throws Exception {
    List<DelayedNotificationUserSetting> myDelayedNotificationUserSettings =
        manager.findDelayedNotificationUserSettingByUserId(10);
    assertThat(myDelayedNotificationUserSettings, notNullValue());
    assertThat(myDelayedNotificationUserSettings.size(), is(1));
    manager.deleteDelayedNotificationUserSetting(30);
    myDelayedNotificationUserSettings =
        manager.findDelayedNotificationUserSettingByUserId(10);
    assertThat(myDelayedNotificationUserSettings, notNullValue());
    assertThat(myDelayedNotificationUserSettings.size(), is(0));
  }
}
