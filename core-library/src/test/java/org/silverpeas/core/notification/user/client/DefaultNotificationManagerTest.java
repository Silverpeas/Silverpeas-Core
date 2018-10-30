/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.silverpeas.core.test.extention.FieldMocker;
import org.silverpeas.core.test.extention.SilverTestEnv;
import org.silverpeas.core.util.SettingBundle;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests on the manager of notifications.
 */
@ExtendWith(SilverTestEnv.class)
public class DefaultNotificationManagerTest {

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
  public void emptyTest() {
    assertTrue(true);
  }

  /**
   * The load of the parameter on the default channels should set the known specified channels as
   * default. Any unknow channel is not taken into account.
   */
  @Test
  public void loadOfDefaultChannelSettingsSetsTheSpecifiedKnownChannelsAsDefault() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(true);
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn(
        "BASIC_SMTP_MAIL   BASIC_SMTP_MAIL   RDFTGT  BASIC_SERVER FFDE    BASIC_SILVERMAIL");
    List<Integer> expectedDefaultChannels = Arrays.asList(
        NotificationParameters.ADDRESS_BASIC_SMTP_MAIL,
        NotificationParameters.ADDRESS_BASIC_SERVER,
        NotificationParameters.ADDRESS_BASIC_SILVERMAIL);

    DefaultNotificationManager notificationManager = new DefaultNotificationManager();
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertTrue(expectedDefaultChannels.containsAll(actualDefaultChannels));
  }

  /**
   * If the multi support channel is not supported, then only one among the different specified
   * default channels is taken into account as default channel.
   */
  @Test
  public void noMultiSupportChannelSettingSetsOnlyOneSpecifiedDefaultChannel() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(false);
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn(
        "TOTO BASIC_COMMUNICATION_USER   BASIC_SMTP_MAIL   RDFTGT  BASIC_SERVER FFDE    " +
            "BASIC_SILVERMAIL");

    DefaultNotificationManager notificationManager = new DefaultNotificationManager();
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals(NotificationParameters.ADDRESS_BASIC_COMMUNICATION_USER, (int)actualDefaultChannels.get(0));
  }

  /**
   * No defined default channels means previous behaviour of Silverpeas: the SMTP mail channel is
   * set as default channel.
   */
  @Test
  public void emptyDefaultChannelsSetsSMTPChannelAsDefault() {
    when(mockedSettings.getBoolean("multiChannelNotification", false)).thenReturn(true);
    when(mockedSettings.getString("notif.defaultChannels", "")).thenReturn("");

    DefaultNotificationManager notificationManager = new DefaultNotificationManager();
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals(NotificationParameters.ADDRESS_BASIC_SMTP_MAIL, (int)actualDefaultChannels.get(0));
  }
}
