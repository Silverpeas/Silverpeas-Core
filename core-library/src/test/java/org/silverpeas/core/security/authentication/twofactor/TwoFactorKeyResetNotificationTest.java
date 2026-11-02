/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.security.authentication.twofactor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.silverpeas.core.notification.user.builder.UserNotificationBuilder;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.test.unit.extention.JEETestContext;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.test.extension.EnableSilverTestEnv;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@EnableSilverTestEnv(context = JEETestContext.class)
class TwoFactorKeyResetNotificationTest {
  @Test
  void shouldNotifyOnlyAccountsInTheCommittedReset() {
    var reset = new TwoFactorKeyReset(List.of("42", "43"));
    var notifier = new TwoFactorKeyResetNotifier();
    try (var notifications = mockStatic(UserNotificationHelper.class)) {
      notifier.notifyUsers(reset);
      var builder = ArgumentCaptor.forClass(UserNotificationBuilder.class);
      notifications.verify(() -> UserNotificationHelper.buildAndSend(builder.capture()));
      var captured = (TwoFactorKeyResetNotification) builder.getValue();
      assertEquals(List.of("42", "43"), captured.getUserIdsToNotify());
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"fr", "en", "de"})
  void shouldRenderTheResetExplanationInTheRecipientLanguage(String language) {
    var builder = new NotificationForTest();
    builder.initializeForTest();
    var template = mock(SilverpeasTemplate.class);
    builder.performTemplateData(language, List.of("42"), template);

    String expected = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.social.multilang.socialNetworkBundle", language)
        .getString("myProfile.twoFactor.keyReset.message");
    verify(template).setAttribute("message", expected);
  }

  private static class NotificationForTest extends TwoFactorKeyResetNotification {
    NotificationForTest() {
      super(List.of("42"));
    }

    void initializeForTest() {
      initialize();
    }
  }
}
