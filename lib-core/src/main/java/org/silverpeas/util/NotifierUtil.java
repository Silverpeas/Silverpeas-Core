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
import org.silverpeas.notification.message.Message;
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
   * @see MessageManager#addSevere(String)
   */
  public static Message addSevere(String message) {
    return MessageManager.addSevere(message);
  }

  /**
   * @see MessageManager#addError(String)
   */
  public static Message addError(String message) {
    return MessageManager.addError(message);
  }

  /**
   * @see MessageManager#addWarning(String)
   */
  public static Message addWarning(String message) {
    return MessageManager.addWarning(message);
  }

  /**
   * @see MessageManager#addSuccess(String)
   */
  public static Message addSuccess(String message) {
    return MessageManager.addSuccess(message);
  }

  /**
   * @see MessageManager#addInfo(String)
   */
  public static Message addInfo(String message) {
    return MessageManager.addInfo(message);
  }
}
