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
package org.silverpeas.core.web.calendar.ical;

import org.silverpeas.core.util.Charsets;

/**
 * Simple password encoder. Note, this is not meant to keep your password secure.
 * @author Andras Berkes
 */
public final class PasswordEncoder {

  public static String encodePassword(String password) throws Exception {
    byte[] bytes = StringUtils.encodeString(password, Charsets.UTF_8);
    String base64 = StringUtils.encodeBASE64(bytes);
    StringBuilder buffer = new StringBuilder(base64);
    String seed = Long.toString(System.currentTimeMillis());
    seed = seed.substring(seed.length() - 3);
    String encoded = seed + buffer.reverse().toString();
    return encoded.replace('=', '$');
  }

}
