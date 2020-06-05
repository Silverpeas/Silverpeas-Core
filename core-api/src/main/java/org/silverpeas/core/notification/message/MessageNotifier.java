/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.notification.message;

import org.silverpeas.core.util.LocalizationBundle;

import java.text.MessageFormat;
import java.time.temporal.Temporal;

/**
 * This utility class provides tools to display easily some dynamic notifications using the
 * notifier
 * plugin.
 * User: Yohann Chastagnier
 * Date: 23/07/13
 */
public class MessageNotifier {

  /**
   * Gets the localization bundle with the specified base name and for the root locale.
   * @param bundleBaseName the bundle base name.
   * @return a localization bundle.
   */
  public static LocalizationBundle getLocalizationBundle(String bundleBaseName) {
    return MessageManager.getLocalizationBundle(bundleBaseName);
  }

  public static Message addSevere(String message, Object... parameters) {
    return MessageManager.addSevere(format(message, parameters));
  }

  public static Message addError(String message, Object... parameters) {
    return MessageManager.addError(format(message, parameters));
  }

  public static Message addWarning(String message, Object... parameters) {
    return MessageManager.addWarning(format(message, parameters));
  }

  public static Message addSuccess(String message, Object... parameters) {
    return MessageManager.addSuccess(format(message, parameters));
  }

  public static Message addInfo(String message, Object... parameters) {
    return MessageManager.addInfo(format(message, parameters));
  }

  /**
   * Formats the given temporal into an date ISO FORMAT that will be interpreted by the UI tools.
   * @param temporal a temporal to format.
   * @return the date ISO format.
   */
  public static String formatDate(Temporal temporal) {
    return "${" + temporal.toString() + ",date}";
  }

  /**
   * Formats the given temporal into an datetime ISO FORMAT that will be interpreted by the UI
   * tools.
   * @param temporal a temporal to format.
   * @return the datetime ISO format.
   */
  public static String formatDateTime(Temporal temporal) {
    return "${" + temporal.toString() + ",datetime}";
  }

  /**
   * Formats if necessary the given message.
   * @param message the message to format if necessary.
   * @param parameters the parameters to take into account in message formatting.
   * @return the formatted message.
   */
  private static String format(String message, Object... parameters) {
    if (parameters.length != 0) {
      return MessageFormat.format(message, parameters);
    }
    return message;
  }
}
