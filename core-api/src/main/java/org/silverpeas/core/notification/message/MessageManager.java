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
package org.silverpeas.core.notification.message;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

/**
 * This manager provides tools to register and restitute volatile messages (info, success or error)
 * to the user (on screen).
 * <p/>
 * It works for now with Thread Cache Service, and several steps have to be performed :
 * - use the initialize function in order to render accessible message tools provided
 * - user message tools in treatment when necessary
 * - use the destroy function in order to clear all message data attached to the thread
 * <p/>
 * A typical initialization/destruction in a the service method of a HttpServlet :
 * protected void service(HttpHttpServletResponse response) {
 * <code>
 * MessageManager.initialize();
 * try {
 * ...
 * } finally {
 * MessageManager.destroy();
 * ...
 * }
 * }
 * </code>
 * <p/>
 * A typical use anywhere in treatments :
 * <code>
 * if ([test of functionnal information is not ok]) {
 * MessageMessager.addError(bundle.getMessage("err.label", params));
 * }
 * </code>
 * <p/>
 * User: Yohann Chastagnier
 * Date: 07/11/13
 */
public class MessageManager {

  /**
   * Initialize the manager in order to be used everywhere in treatments.
   */
  public static String initialize() {
    String registeredKey =
        CacheServiceProvider.getApplicationCacheService().add(new MessageContainer());
    CacheServiceProvider.getRequestCacheService().put(MessageManager.class, registeredKey);
    return registeredKey;
  }

  /**
   * Clear out the thread cache the registred key referenced.
   */
  public static void destroy() {
    CacheServiceProvider.getRequestCacheService().remove(MessageManager.class);
  }

  /**
   * Adding a message listener
   * @param listener
   */
  public static void addListener(MessageListener listener) {
    addListener(getRegistredKey(), listener);
  }

  /**
   * Adding a message listener
   * @param listener
   */
  protected static void addListener(String registredKey, MessageListener listener) {
    MessageContainer container = getMessageContainer(registredKey);
    if (container != null) {
      container.addListener(listener);
    }
  }

  /**
   * Setting a language
   * @param language
   */
  public static void setLanguage(final String language) {
    setLanguage(getRegistredKey(), language);
  }

  /**
   * Getting language
   * @return
   */
  public static String getLanguage() {
    return getLanguage(getRegistredKey());
  }

  /**
   * Setting a language
   * @param language
   */
  protected static void setLanguage(String registredKey, String language) {
    MessageContainer container = getMessageContainer(registredKey);
    if (container != null) {
      container.setLanguage(language);
    }
  }

  /**
   * Getting language
   * @return
   */
  protected static String getLanguage(String registredKey) {
    MessageContainer container = getMessageContainer(registredKey);
    if (container != null) {
      return container.getLanguage();
    }
    return DisplayI18NHelper.getDefaultLanguage();
  }

  /**
   * Remove out of the cache the message container referenced by the given key
   */
  public static void clear(String registredKey) {
    CacheServiceProvider.getApplicationCacheService().remove(registredKey);
  }

  /**
   * Get the key that permits to get the MessageContainer registred for the thread.
   */
  public static String getRegistredKey() {
    return CacheServiceProvider.getRequestCacheService().get(MessageManager.class, String.class);
  }

  /**
   * Gets the localization bundle with the given base name and for the root locale.
   * @param bundleBaseName the localization bundle base name.
   * @return a localization bundle.
   */
  public static LocalizationBundle getLocalizationBundle(String bundleBaseName) {
    return getLocalizationBundle(getRegistredKey(), bundleBaseName, null);
  }

  /**
   * Gets from the message container the localization bundle with the specified bundle base name
   * and for the given language.
   * @param messageContainerName the name of the message container.
   * @param bundleBaseName the base name of the localization bundle.
   * @param language the language for which the bundle is asked.
   * @return a localization bundle.
   */
  protected static LocalizationBundle getLocalizationBundle(String messageContainerName,
      String bundleBaseName, String language) {
    MessageContainer container = getMessageContainer(messageContainerName);

    // If null, manager has not been initialized -> ERROR is traced
    if (container == null) {
      SilverLogger.getLogger("notification").error("ResourceLocator : " + bundleBaseName);
      return null;
    }

    return container.getLocalizationBundle(bundleBaseName,
        StringUtil.isDefined(language) ? language : container.getLanguage());
  }


  /**
   * Gets the message container.
   * @return
   */
  public static MessageContainer getMessageContainer(String registredKey) {
    return CacheServiceProvider.getApplicationCacheService()
        .get(registredKey, MessageContainer.class);
  }

  /**
   * Add an error message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addError(String message) {
    return addError(getRegistredKey(), message);
  }

  /**
   * Add an error message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addError(String registredKey, String message) {
    return addMessage(registredKey, new ErrorMessage(message));
  }

  /**
   * Add an severe message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addSevere(String message) {
    return addSevere(getRegistredKey(), message);
  }

  /**
   * Add an severe message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addSevere(String registredKey, String message) {
    return addMessage(registredKey, new SevereMessage(message));
  }

  /**
   * Add an warning message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addWarning(String message) {
    return addWarning(getRegistredKey(), message);
  }

  /**
   * Add an warning message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addWarning(String registredKey, String message) {
    return addMessage(registredKey, new WarningMessage(message));
  }

  /**
   * Add a success message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addSuccess(String message) {
    return addSuccess(getRegistredKey(), message);
  }

  /**
   * Add a success message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addSuccess(String registredKey, String message) {
    return addMessage(registredKey, new SuccessMessage(message));
  }

  /**
   * Add an info message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  public static Message addInfo(String message) {
    return addInfo(getRegistredKey(), message);
  }

  /**
   * Add an info message. If a message already exists, HTML newline is added
   * between the existent message and the given one
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  protected static Message addInfo(String registredKey, String message) {
    return addMessage(registredKey, new InfoMessage(message));
  }

  /**
   * Centralization
   * @param message
   * @return the instance of the created message. Some parameters of this instance can be
   * overridden (the display live time for example).
   */
  private static Message addMessage(String registredKey, Message message) {
    MessageContainer container = getMessageContainer(registredKey);

    // If null, manager has not been initialized -> ERROR is traced
    if (container == null) {
      SilverLogger.getLogger("notification")
          .error("Type : " + message.getType() + ", Message : " + message.getContent());
    } else {
      container.addMessage(message);
    }
    return message;
  }
}
