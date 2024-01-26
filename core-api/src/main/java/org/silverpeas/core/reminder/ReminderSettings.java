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

package org.silverpeas.core.reminder;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.date.TimeUnit;
import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author silveryocha
 */
public class ReminderSettings {
  /**
   * The relative path of the properties file containing the settings of the reminder services.
   */
  public static final String SETTINGS_PATH = "org.silverpeas.reminder.settings.reminder";

  /**
   * The relative path of the i18n bundle of the reminder component.
   */
  public static final String MESSAGES_PATH = "org.silverpeas.reminder.multilang.reminder";

  /**
   * Hidden constructor.
   */
  private ReminderSettings() {
  }

  /**
   * Gets all the messages for the reminder services and translated in the specified
   * language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings about reminder services.
   * @return the resource with the different service settings.
   */
  public static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }

  /**
   * Gets the list of possible reminders. Each reminder is represented by a {@link Pair} of
   * duration and a time unit (precising the duration indeed).
   * @return list of {@link Pair}
   */
  public static Stream<Pair<Integer, TimeUnit>> getPossibleReminders() {
    return Arrays
        .stream(getSettings().getString("reminder.possible").split(" "))
        .map(r -> {
            final String[] split = r.split("[|]");
            return Pair.of(Integer.parseInt(split[0]), TimeUnit.valueOf(split[1]));
          });
  }

  /**
   * Gets the default reminder to set.
   * @return a {@link Pair} representing the default reminder.
   */
  public static Pair<Integer, TimeUnit> getDefaultReminder() {
    final String[] split = getSettings().getString("reminder.default").split("[|]");
    return Pair.of(Integer.parseInt(split[0]), TimeUnit.valueOf(split[1]));
  }
}
