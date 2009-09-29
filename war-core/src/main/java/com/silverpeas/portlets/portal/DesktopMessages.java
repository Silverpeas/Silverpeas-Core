/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.portlets.portal;

import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

/**
 * DesktopMessages is used to get the localized messages from
 * DesktopMessages.properties
 */
public class DesktopMessages {

  public DesktopMessages() {
  }

  private static final String RESOURCE_BASE = "com.silverpeas.portlets.multilang.portletsBundle";

  private static ResourceBundle rb;

  public static void init(HttpServletRequest request) {
    rb = PropertyResourceBundle.getBundle(RESOURCE_BASE, request.getLocale());
  }

  public static String getLocalizedString(String key) {
    return rb.getString(key);
  }

  public static String getLocalizedString(String key, Object[] tokens) {
    String msg = getLocalizedString(key);

    if (tokens != null && tokens.length > 0) {
      MessageFormat mf = new MessageFormat("");
      mf.setLocale(rb.getLocale());
      mf.applyPattern(msg);
      return mf.format(tokens);
    } else {
      return msg;
    }
  }
}
