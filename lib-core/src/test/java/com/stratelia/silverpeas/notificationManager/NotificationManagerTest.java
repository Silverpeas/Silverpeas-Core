/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.notificationManager;

import com.stratelia.webactiv.util.ResourceLocator;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static com.stratelia.silverpeas.notificationManager.NotificationParameters.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests on the manager of notifications.
 */
public class NotificationManagerTest {

  public NotificationManagerTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
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
    ResourceLocator mockedResourceLocator = mock(ResourceLocator.class);
    when(mockedResourceLocator.getString("multiChannelNotification")).thenReturn("true");
    when(mockedResourceLocator.getString("notif.defaultChannels")).thenReturn(
        "BASIC_SMTP_MAIL   BASIC_SMTP_MAIL   RDFTGT  BASIC_SERVER FFDE    BASIC_SILVERMAIL");
    List<Integer> expectedDefaultChannels = Arrays.asList(ADDRESS_BASIC_SMTP_MAIL,
        ADDRESS_BASIC_SERVER,
        ADDRESS_BASIC_SILVERMAIL);

    NotificationManager notificationManager = new NotificationManager(null);
    notificationManager.setNotificationResources(mockedResourceLocator);
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertTrue(expectedDefaultChannels.containsAll(actualDefaultChannels));
  }

  /**
   * If the multi support channel is not supported, then only one among the different specified
   * default channels is taken into account as default channel.
   */
  @Test
  public void noMultiSupportChannelSettingSetsOnlyOneSpecifiedDefaultChannel() {
    ResourceLocator mockedResourceLocator = mock(ResourceLocator.class);
    when(mockedResourceLocator.getString("multiChannelNotification")).thenReturn("false");
    when(mockedResourceLocator.getString("notif.defaultChannels")).thenReturn(
        "TOTO BASIC_COMMUNICATION_USER   BASIC_SMTP_MAIL   RDFTGT  BASIC_SERVER FFDE    BASIC_SILVERMAIL");
    
    NotificationManager notificationManager = new NotificationManager(null);
    notificationManager.setNotificationResources(mockedResourceLocator);
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals((int)ADDRESS_BASIC_COMMUNICATION_USER, (int)actualDefaultChannels.get(0));
  }
  
  /**
   * No defined default channels means previous behaviour of Silverpeas: the SMTP mail channel is
   * set as default channel.
   */
  @Test
  public void emptyDefaultChannelsSetsSMTPChannelAsDefault() {
    ResourceLocator mockedResourceLocator = mock(ResourceLocator.class);
    when(mockedResourceLocator.getString("multiChannelNotification")).thenReturn("true");
    when(mockedResourceLocator.getString("notif.defaultChannels")).thenReturn("");
    
    NotificationManager notificationManager = new NotificationManager(null);
    notificationManager.setNotificationResources(mockedResourceLocator);
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals((int)ADDRESS_BASIC_SMTP_MAIL, (int)actualDefaultChannels.get(0));
  }
  
  /**
   * No defined default channels means previous behaviour of Silverpeas: the SMTP mail channel is
   * set as default channel.
   */
  @Test
  public void noDefinedDefaultChannelsSetsSMTPChannelAsDefault() {
    ResourceLocator mockedResourceLocator = mock(ResourceLocator.class);
    when(mockedResourceLocator.getString("multiChannelNotification")).thenReturn("true");
    when(mockedResourceLocator.getString("notif.defaultChannels")).thenReturn(null);
    
    NotificationManager notificationManager = new NotificationManager(null);
    notificationManager.setNotificationResources(mockedResourceLocator);
    List<Integer> actualDefaultChannels = notificationManager.getDefaultNotificationAddresses();
    assertEquals(1, actualDefaultChannels.size());
    assertEquals((int)ADDRESS_BASIC_SMTP_MAIL, (int)actualDefaultChannels.get(0));
  }
}
