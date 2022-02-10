/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.util.SettingBundle;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the manager of notifications.
 */
@EnableSilverTestEnv
class DefaultNotificationManagerTest {

  @RegisterExtension
  FieldMocker mocker = new FieldMocker();
  private SettingBundle mockedSettings;

  @BeforeEach
  public void setUp() {
    // Settings
    mockedSettings = mocker.mockField(NotificationManagerSettings.class,
        SettingBundle.class, "settings");
  }

  /**
   * Empty test to check the unit test case works fine by default.
   * Useful for checking the setting up of the resources required by tests is ok.
   */
  @Test
  void emptyTest() {
    assertTrue(true);
  }

  /**
   * The load of the parameter on the default channels should set the known specified channels as
   * default. Any unknow channel is not taken into account.
   */
  @Test
  void loadOfDefaultChannelSettingsSetsTheSpecifiedKnownChannelsAsDefault() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(true);
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn(
        "BASIC_SMTP_MAIL   BASIC_SMTP_MAIL   RDFTGT  BASIC_SERVER FFDE    BASIC_SILVERMAIL");
    List<NotifChannel> expectedDefaultChannels = Arrays.asList(NotifChannel.SMTP,
        NotifChannel.SERVER,
        NotifChannel.SILVERMAIL);

    DefaultNotificationManager notificationManager = new DefaultNotificationManager();
    List<NotifChannel> actualDefaultChannels = notificationManager.getDefaultNotificationChannels();
    assertTrue(expectedDefaultChannels.containsAll(actualDefaultChannels));
  }

  /**
   * If the multi support channel is not supported, then only one among the different specified
   * default channels is taken into account as default channel.
   */
  @Test
  void noMultiSupportChannelSettingSetsOnlyOneSpecifiedDefaultChannel() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(false);
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn(
        "TOTO BASIC_SMTP_MAIL   RDFTGT  BASIC_SERVER FFDE    BASIC_SILVERMAIL");

    DefaultNotificationManager notificationManager = new DefaultNotificationManager();
    List<NotifChannel> actualDefaultChannels = notificationManager.getDefaultNotificationChannels();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals(NotifChannel.SMTP, actualDefaultChannels.get(0));
  }

  /**
   * No defined default channels means previous behaviour of Silverpeas: the SMTP mail channel is
   * set as default channel.
   */
  @Test
  void emptyDefaultChannelsSetsSMTPChannelAsDefault() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(true);
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn("");

    DefaultNotificationManager notificationManager = new DefaultNotificationManager();
    List<NotifChannel> actualDefaultChannels = notificationManager.getDefaultNotificationChannels();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals(NotifChannel.SMTP, actualDefaultChannels.get(0));
  }
}
