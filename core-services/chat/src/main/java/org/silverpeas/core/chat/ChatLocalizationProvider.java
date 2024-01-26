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
package org.silverpeas.core.chat;

import org.silverpeas.kernel.bundle.LocalizationBundle;
import org.silverpeas.kernel.bundle.ResourceLocator;

/**
 * A provider of a localization bundle for the chat service.
 * @author mmoquillon
 */
public class ChatLocalizationProvider {

  private static final String BUNDLE = "org.silverpeas.chat.multilang.chat";

  public static LocalizationBundle getLocalizationBundle() {
    return ResourceLocator.getLocalizationBundle(BUNDLE);
  }

  public static LocalizationBundle getLocalizationBundle(final String language) {
    return ResourceLocator.getLocalizationBundle(BUNDLE, language);
  }

  private ChatLocalizationProvider() {

  }
}
