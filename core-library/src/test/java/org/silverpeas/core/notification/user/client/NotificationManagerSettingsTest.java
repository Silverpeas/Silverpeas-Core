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
package org.silverpeas.core.notification.user.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.notification.user.client.constant.NotifChannel;
import org.silverpeas.core.notification.user.delayed.constant.DelayedNotificationFrequency;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.util.SettingBundle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@EnableSilverTestEnv
public class NotificationManagerSettingsTest {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();
  private SettingBundle mockedSettings;

  @BeforeEach
  public void setup() {
    // Settings
    mockedSettings =
        mocker.mockField(NotificationManagerSettings.class, SettingBundle.class,
            "settings");
    setDefaultChannels("");
    setDelayedNotificationFrequencyChoiceList("");
    setDefaultDelayedNotificationFrequencyChoiceList("");
  }

  /*
  TESTS around
  {@link NotificationManagerSettings#getDefaultDelayedNotificationFrequency()}.
   */

  @Test
  public void getDefaultDelayedNotificationFrequencyButNoValue() {
    assertThat(NotificationManagerSettings.getDefaultDelayedNotificationFrequency(),
        is(DelayedNotificationFrequency.NONE));
  }

  @Test
  public void getDefaultDelayedNotificationFrequencyButBadValue() {
    setDefaultDelayedNotificationFrequencyChoiceList("D W");
    assertThat(NotificationManagerSettings.getDefaultDelayedNotificationFrequency(),
        is(DelayedNotificationFrequency.NONE));
  }

  @Test
  public void getDefaultDelayedNotificationFrequencyWithRightValue() {
    setDefaultDelayedNotificationFrequencyChoiceList("W");
    assertThat(NotificationManagerSettings.getDefaultDelayedNotificationFrequency(),
        is(DelayedNotificationFrequency.WEEKLY));
  }

  /*
  TESTS around
  {@link NotificationManagerSettings#getDelayedNotificationFrequencyChoiceList()}.
   */

  @Test
  public void getDelayedNotificationFrequencyChoiceListButNoValue() {
    assertThat(NotificationManagerSettings.getDelayedNotificationFrequencyChoiceList(), empty());
  }

  @Test
  public void getDelayedNotificationFrequencyChoiceListButBadValue() {
    setDelayedNotificationFrequencyChoiceList("   BAD_VALUE   ");
    assertThat(NotificationManagerSettings.getDelayedNotificationFrequencyChoiceList(), empty());
  }

  @Test
  public void getDelayedNotificationFrequencyChoiceListWithOneValidValue() {
    setDelayedNotificationFrequencyChoiceList("   BAD_VALUE  D ");
    assertThat(NotificationManagerSettings.getDelayedNotificationFrequencyChoiceList(),
        contains(DelayedNotificationFrequency.DAILY));
  }

  @Test
  public void getDelayedNotificationFrequencyChoiceListWithTwoValidValue() {
    setDelayedNotificationFrequencyChoiceList(" W  BAD_VALUE  D");
    assertThat(NotificationManagerSettings.getDelayedNotificationFrequencyChoiceList(),
        contains(DelayedNotificationFrequency.DAILY, DelayedNotificationFrequency.WEEKLY));
  }

  @Test
  public void getDelayedNotificationFrequencyChoiceListWithAllFrequencies() {
    setDelayedNotificationFrequencyChoiceList(" W  BAD_VALUE  D *     ");
    assertThat(NotificationManagerSettings.getDelayedNotificationFrequencyChoiceList(),
        contains(DelayedNotificationFrequency.values()));
  }

  /*
  TESTS around
  {@link NotificationManagerSettings#getDefaultChannels()}.
   */

  @Test
  public void getDefaultChannelsWithNoValueDefinedAndMultiChannelIsNotEnabled() {
    assertThat(NotificationManagerSettings.getDefaultChannels(),
        contains(NotifChannel.SMTP));
  }

  @Test
  public void getDefaultChannelsWithBadValueDefinedAndMultiChannelIsNotEnabled() {
    setDefaultChannels("BAD_VALUE");
    assertThat(NotificationManagerSettings.getDefaultChannels(),
        contains(NotifChannel.SMTP));
  }

  @Test
  public void getDefaultChannelsWithBasicServerDefinedAndMultiChannelIsNotEnabled() {
    setDefaultChannels("BASIC_SERVER");
    assertThat(NotificationManagerSettings.getDefaultChannels(),
        contains(NotifChannel.SERVER));
  }

  @Test
  public void getDefaultChannelsWithSeveralDefinedAndMultiChannelIsNotEnabled() {
    setDefaultChannels("  , hijez ,  BASIC_SERVER   BAD_VALUE    BASIC_SERVER   ");
    assertThat(NotificationManagerSettings.getDefaultChannels(),
        contains(NotifChannel.SERVER));
  }

  @Test
  public void getDefaultChannelsWithSeveralDefinedAndMultiChannelIsEnabled() {
    enableMultiChannel();
    setDefaultChannels("  , hijez ,  BASIC_SMTP_MAIL   BAD_VALUE    BASIC_SERVER   ");
    assertThat(NotificationManagerSettings.getDefaultChannels(),
        contains(NotifChannel.SMTP, NotifChannel.SERVER));
  }

  /*
  CURRENT TEST TOOLS
   */

  private void enableMultiChannel() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(true);
  }

  private void setDefaultChannels(String value) {
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn(value);
  }

  private void setDelayedNotificationFrequencyChoiceList(String value) {
    when(mockedSettings.getString("DELAYED_NOTIFICATION_FREQUENCY_CHOICE_LIST", ""))
        .thenReturn(value);
  }

  private void setDefaultDelayedNotificationFrequencyChoiceList(String value) {
    when(mockedSettings.getString("DEFAULT_DELAYED_NOTIFICATION_FREQUENCY", null)).thenReturn(value);
  }
}