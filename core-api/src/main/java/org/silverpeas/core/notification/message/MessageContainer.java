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
package org.silverpeas.core.notification.message;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * User: Yohann Chastagnier
 * Date: 07/11/13
 */
public class MessageContainer {
  private final List<MessageListener> listeners = new ArrayList<>();
  private String language = DisplayI18NHelper.getDefaultLanguage();
  private final Set<Message> messages = new LinkedHashSet<>();

  /**
   * Hidden construtor because only the Manager is able to instance it.
   */
  MessageContainer() {
    super();
  }

  /**
   * Gets the localization bundle of the given bundle base name and for the given language.
   * @param bundleBaseName the base name of the bundle.
   * @param lang the language for which the bundle is asked.
   * @return a localization bundle.
   */
  public LocalizationBundle getLocalizationBundle(String bundleBaseName, String lang) {
    return ResourceLocator.getLocalizationBundle(bundleBaseName, lang);
  }

  public void addListener(MessageListener listener) {
    listeners.add(listener);
  }

  public void setLanguage(final String language) {
    this.language = language;
  }

  public String getLanguage() {
    for (MessageListener messageListener : listeners) {
      messageListener.beforeGetLanguage(this);
    }
    return language;
  }

  public void addMessage(Message message) {
    for (MessageListener messageListener : listeners) {
      messageListener.beforeAddMessage(this, message);
    }
    messages.add(message);
    for (MessageListener messageListener : listeners) {
      messageListener.afterMessageAdded(this, message);
    }
  }

  public Set<Message> getMessages() {
    return messages;
  }
}
