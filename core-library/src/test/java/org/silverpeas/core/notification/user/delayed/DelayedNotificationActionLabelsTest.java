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
package org.silverpeas.core.notification.user.delayed;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.kernel.test.UnitTest;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

/**
 * Unit tests on the localized texts required by the synthesis of the delayed notifications.
 * <p>
 * A notification whose sending is delayed is reported to its recipient within a synthesis in which
 * the action the notification is about is rendered from a label of the bundle of the notification
 * manager. Without such a label, the building of the whole synthesis fails. These tests are done
 * on the bundle delivered with Silverpeas, in the configuration module of Silverpeas Core.
 * </p>
 * @author mmoquillon
 */
@UnitTest
class DelayedNotificationActionLabelsTest {

  private static final Path BUNDLES = Path.of("..", "core-configuration", "src", "main", "config",
      "properties", "org", "silverpeas", "notificationManager", "multilang");

  @ParameterizedTest
  @ValueSource(strings = {"", "_fr", "_en", "_de"})
  void eachActionHasItsLabelForTheSynthesisOfTheDelayedNotifications(final String locale)
      throws IOException {
    final Properties bundle = loadBundle(locale);

    final List<NotifAction> actionsWithoutLabel = Arrays.stream(NotifAction.values())
        .filter(a -> bundle.getProperty("resourceAction" + a.name(), "").isBlank())
        .toList();

    assertThat("The actions without label in the bundle '" + locale + "'", actionsWithoutLabel,
        is(empty()));
  }

  private static Properties loadBundle(final String locale) throws IOException {
    final Properties bundle = new Properties();
    final Path path = BUNDLES.resolve("notificationManagerBundle" + locale + ".properties");
    try (Reader reader = Files.newBufferedReader(path, StandardCharsets.ISO_8859_1)) {
      bundle.load(reader);
    }
    return bundle;
  }
}
