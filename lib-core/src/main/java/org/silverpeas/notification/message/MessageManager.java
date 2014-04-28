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
package org.silverpeas.notification.message;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import org.silverpeas.cache.service.CacheServiceFactory;

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
    String registredKey =
        CacheServiceFactory.getApplicationCacheService().add(new MessageContainer());
    CacheServiceFactory.getRequestCacheService().put(MessageManager.class, registredKey);
    return registredKey;
  }

  /**
   * Clear out the thread cache the registred key referenced.
   */
  public static void destroy() {
    CacheServiceFactory.getRequestCacheService().remove(MessageManager.class);
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
    return I18NHelper.defaultLanguage;
  }

  /**
   * Remove out of the cache the message container referenced by the given key
   */
  public static void clear(String registredKey) {
    CacheServiceFactory.getApplicationCacheService().remove(registredKey);
  }

  /**
   * Get the key that permits to get the MessageContainer registred for the thread.
   */
  public static String getRegistredKey() {
    return CacheServiceFactory.getRequestCacheService().get(MessageManager.class, String.class);
  }

  /**
   * Gets the resource locator from the given property file and by taking into account of the
   * current known language.
   * @param propertyFileBaseName
   * @return
   */
  public static ResourceLocator getResourceLocator(String propertyFileBaseName) {
    return getResourceLocator(getRegistredKey(), propertyFileBaseName, null);
  }

  /**
   * Gets the resource locator from the given property file and given language.
   * @param propertyFileBaseName
   * @param language
   * @return
   */
  protected static ResourceLocator getResourceLocator(String registredKey,
      String propertyFileBaseName, String language) {
    MessageContainer container = getMessageContainer(registredKey);

    // If null, manager has not been initialized -> ERROR is traced
    if (container == null) {
      SilverTrace.error("notification", "MessageManager.getResourceLocator",
          "MESSAGE_MANAGER.NOT_INITIALIZED", "ResourceLocator : " + propertyFileBaseName);
      return null;
    }

    return container.getResourceLocator(propertyFileBaseName,
        StringUtil.isDefined(language) ? language : container.getLanguage());
  }


  /**
   * Gets the message container.
   * @return
   */
  public static MessageContainer getMessageContainer(String registredKey) {
    return CacheServiceFactory.getApplicationCacheService()
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
      SilverTrace
          .error("notification", "MessageManager.addMessage", "MESSAGE_MANAGER.NOT_INITIALIZED",
              "Type : " + message.getType() + ", Message : " + message.getContent());
    } else {
      container.addMessage(message);
    }
    return message;
  }
}
