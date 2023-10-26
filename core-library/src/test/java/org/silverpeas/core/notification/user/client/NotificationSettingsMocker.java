package org.silverpeas.core.notification.user.client;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.internal.util.MockUtil;
import org.silverpeas.kernel.bundle.SettingBundle;

import static org.mockito.Mockito.mock;

/**
 * A mocker of the NotificationSettings class. Its goal is to mock the settings backed by the
 * NotificationSettings class in order to be able to change the value of some of the settings
 * for testing purpose.
 * @author mmoquillon
 */
public class NotificationSettingsMocker implements BeforeEachCallback, AfterEachCallback {

  private SettingBundle mockedSettings;
  private SettingBundle mockedIconSettings;

  private static SettingBundle mockSettings() {
    SettingBundle mock = mock(SettingBundle.class);
    NotificationManagerSettings.setSettings(mock);
    return mock;
  }

  private static SettingBundle mockIconSettings() {
    SettingBundle mock = mock(SettingBundle.class);
    NotificationManagerSettings.setIconSettings(mock);
    return mock;
  }

  private static SettingBundle mockSettingsIfNotYet() {
    SettingBundle settings = NotificationManagerSettings.getSettings();
    if (!MockUtil.isMock(settings)) {
      return mockSettings();
    }
    return settings;
  }

  private static void unsetMock() {
    NotificationManagerSettings.setIconSettings(NotificationManagerSettings.DEFAULT_ICON_SETTINGS);
    NotificationManagerSettings.setSettings(NotificationManagerSettings.DEFAULT_SETTINGS);
  }

  public SettingBundle getMockedSettings() {
    return mockedSettings;
  }

  @SuppressWarnings("unused")
  public SettingBundle getMockedIconSettings() {
    return mockedIconSettings;
  }

  @Override
  public void afterEach(ExtensionContext context) {
    unsetMock();
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    mockedSettings = mockSettingsIfNotYet();
    mockedIconSettings = mockIconSettings();
  }
}
  