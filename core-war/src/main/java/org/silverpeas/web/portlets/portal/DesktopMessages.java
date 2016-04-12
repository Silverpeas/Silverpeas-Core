/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.portlets.portal;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.ResourceLocator;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * DesktopMessages is used to get the localized messages from DesktopMessages.properties
 */
public class DesktopMessages {
  private static final String RESOURCE_BASE = "org.silverpeas.portlets.multilang.portletsBundle";
  private static final ThreadLocal<DesktopMessages> cache = new ThreadLocal<DesktopMessages>();

  private LocalizationBundle bundle;
  private Locale locale;

  /**
   * This method has to be called at each HTTP Request performed related to portlet management.
   * @param language the language of the user. If no one is passed, default platform language is
   * taken.
   * @return
   */
  public static DesktopMessages init(String language) {
    final String userLanguage =
        (StringUtil.isDefined(language) ? language : I18NHelper.defaultLanguage);
    DesktopMessages desktopMessages = new DesktopMessages();
    desktopMessages.bundle = ResourceLocator.getLocalizationBundle(RESOURCE_BASE, userLanguage);
    desktopMessages.locale = new Locale(userLanguage);
    cache.set(desktopMessages);
    return desktopMessages;
  }

  public static String getLocalizedString(String key) {
    return getLocalizedString(key, null);
  }

  public static String getLocalizedString(String key, Object[] tokens) {
    DesktopMessages desktopMessages = getInstance();
    String msg = desktopMessages.bundle.getString(key);
    if (tokens != null && tokens.length > 0) {
      MessageFormat mf = new MessageFormat("");
      mf.setLocale(desktopMessages.locale);
      mf.applyPattern(msg);
      return mf.format(tokens);
    }
    return msg;
  }

  /**
   * This method retrieves the desktop messages instance.
   * @return
   */
  private static DesktopMessages getInstance() {
    DesktopMessages desktopMessages = cache.get();
    if (desktopMessages == null) {
      // Initialization has not been done, a default one is performed
      desktopMessages = init(null);
    }
    return desktopMessages;
  }
}
