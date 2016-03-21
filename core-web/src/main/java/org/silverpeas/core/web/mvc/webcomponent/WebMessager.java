/*
 * Copyright (C) 2000 - 2014 Silverpeas
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
package org.silverpeas.core.web.mvc.webcomponent;

import org.silverpeas.util.LocalizationBundle;
import org.silverpeas.notification.message.Message;
import org.silverpeas.util.NotifierUtil;

/**
 * This utility class provides tools to display easily some dynamic notifications using the
 * notifier plugin.
 * @author Yohann Chastagnier
 */
public final class WebMessager {
  private final static WebMessager webMessager = new WebMessager();

  public static WebMessager getInstance() {
    return webMessager;
  }

  private WebMessager() {
  }

  /**
   * Gets the localization bundle with the specified base name and for the root locale.
   * @param bundleBaseName the bundle base name.
   * @return the asked localization bundle.
   */
  public LocalizationBundle getLocalizationBundle(String bundleBaseName) {
    return NotifierUtil.getLocalizationBundle(bundleBaseName);
  }

  /**
   * @see NotifierUtil#addSevere(String, Object...)
   */
  public Message addSevere(String message, Object... parameters) {
    return NotifierUtil.addSevere(message, parameters);
  }

  /**
   * @see NotifierUtil#addError(String, Object...)
   */
  public Message addError(String message, Object... parameters) {
    return NotifierUtil.addError(message, parameters);
  }

  /**
   * @see NotifierUtil#addWarning(String, Object...)
   */
  public Message addWarning(String message, Object... parameters) {
    return NotifierUtil.addWarning(message, parameters);
  }

  /**
   * @see NotifierUtil#addSuccess(String, Object...)
   */
  public Message addSuccess(String message, Object... parameters) {
    return NotifierUtil.addSuccess(message, parameters);
  }

  /**
   * @see NotifierUtil#addInfo(String, Object...)
   */
  public Message addInfo(String message, Object... parameters) {
    return NotifierUtil.addInfo(message, parameters);
  }
}
