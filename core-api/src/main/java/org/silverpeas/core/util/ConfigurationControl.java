/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.util;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Defines the frequency for ResourceBundle cache invalidation. The default period is 4 Hours. You
 * can specify a period with the System property <code>silverpeas.refresh.configuration</code> and a
 * period in milliseconds.
 * @author ehugonnet
 */
public class ConfigurationControl extends ResourceBundle.Control {

  public static final long DEFAULT_RELOAD = 14400000L; // 4 Hours
  private static long RELOAD = DEFAULT_RELOAD;
  public static final String REFRESH_CONFIG = "silverpeas.refresh.configuration";

  static {
    String refresh = System.getProperty(REFRESH_CONFIG);
    if (StringUtil.isDefined(refresh) && StringUtil.isLong(refresh)) {
      RELOAD = Long.parseLong(refresh);
    }
  }

  @Override
  public long getTimeToLive(String baseName, Locale locale) {
    return RELOAD;
  }

}
