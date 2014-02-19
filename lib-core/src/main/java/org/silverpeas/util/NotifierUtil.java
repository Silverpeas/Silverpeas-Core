/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util;

import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.notification.message.MessageManager;

/**
 * This utility class provides tools to display easily some dynamic notifications using the
 * notifier
 * plugin.
 * User: Yohann Chastagnier
 * Date: 23/07/13
 */
public class NotifierUtil {

  /**
   * Gets the resource locator from the given property file and by taking into account of the
   * current known language.
   * @param propertyFileBaseName
   * @return
   */
  public static ResourceLocator getResourceLocator(String propertyFileBaseName) {
    return MessageManager.getResourceLocator(propertyFileBaseName);
  }

  /**
   * Add an severe message.
   * @param message
   */
  public static void addSevere(String message) {
    MessageManager.addSevere(message);
  }

  /**
   * Add an error message.
   * @param message
   */
  public static void addError(String message) {
    MessageManager.addError(message);
  }

  /**
   * Add an warning message.
   * @param message
   */
  public static void addWarning(String message) {
    MessageManager.addWarning(message);
  }

  /**
   * Add a success message.
   * @param message
   */
  public static void addSuccess(String message) {
    MessageManager.addSuccess(message);
  }

  /**
   * Add a success message.
   * @param message
   */
  public static void addInfo(String message) {
    MessageManager.addInfo(message);
  }
}
