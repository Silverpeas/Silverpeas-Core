/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
package org.silverpeas.core.util;

import org.silverpeas.kernel.cache.model.SimpleCache;

import static org.silverpeas.core.cache.service.CacheAccessorProvider.getThreadCacheAccessor;

/**
 * Registry of the scripts included into the HTML page rendered for the current request. Whatever
 * the way a script is included into the page (a look and feel option, a plugin, the rendering of a
 * form, ...), it has to be included once: a script declaring constants fails when it is evaluated
 * twice in the same document.
 */
public final class ScriptInclusionRegistry {

  private static final String KEY_PREFIX = "$jsPlugin$script$";

  private ScriptInclusionRegistry() {
  }

  /**
   * Registers the inclusion of the specified script into the page being rendered.
   * @param src the URL of the script, without any cache-busting fingerprint.
   * @return true if the script wasn't yet included into the page and then has to be, false if it
   * is already included.
   */
  public static boolean register(final String src) {
    final SimpleCache cache = getThreadCacheAccessor().getCache();
    final String key = KEY_PREFIX + src;
    if (cache.get(key) != null) {
      return false;
    }
    cache.put(key, true);
    return true;
  }
}
