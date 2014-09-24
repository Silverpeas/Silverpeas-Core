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

import org.silverpeas.util.i18n.I18NHelper;
import com.stratelia.webactiv.util.ResourceLocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: Yohann Chastagnier
 * Date: 07/11/13
 */
public class MessageContainer {
  private final Map<String, ResourceLocator> resourceLocators =
      new HashMap<String, ResourceLocator>();

  private final List<MessageListener> listeners = new ArrayList<MessageListener>();
  private String language = I18NHelper.defaultLanguage;
  private final Set<Message> messages = new LinkedHashSet<Message>();

  /**
   * Hidden construtor because only the Manager is able to instance it.
   */
  MessageContainer() {
    super();
  }

  /**
   * Gets the resource locator from the given property file and given language.
   * @param propertyFileBaseName
   * @param lang
   * @return
   */
  public ResourceLocator getResourceLocator(String propertyFileBaseName, String lang) {
    String cacheKey = propertyFileBaseName + "@" + lang;
    ResourceLocator resourceLocator = resourceLocators.get(cacheKey);
    if (resourceLocator == null) {
      resourceLocator = new ResourceLocator(propertyFileBaseName, lang);
      resourceLocators.put(cacheKey, resourceLocator);
    }
    return resourceLocator;
  }

  /**
   * Adding a message listener
   * @param listener
   */
  public void addListener(MessageListener listener) {
    listeners.add(listener);
  }

  /**
   * Setting a language
   * @param language
   */
  public void setLanguage(final String language) {
    this.language = language;
  }

  /**
   * Getting language
   * @return
   */
  public String getLanguage() {
    for (MessageListener messageListener : listeners) {
      messageListener.beforeGetLanguage(this);
    }
    return language;
  }

  /**
   * Adding a message
   * @param message
   */
  public void addMessage(Message message) {
    for (MessageListener messageListener : listeners) {
      messageListener.beforeAddMessage(this, message);
    }
    messages.add(message);
    for (MessageListener messageListener : listeners) {
      messageListener.afterMessageAdded(this, message);
    }
  }

  /**
   * Getting all messages
   * @return
   */
  public Set<Message> getMessages() {
    return messages;
  }
}
