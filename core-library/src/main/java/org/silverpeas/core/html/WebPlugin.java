/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.html;

import org.apache.ecs.ElementContainer;
import org.silverpeas.core.util.ServiceProvider;

/**
 * @author Yohann Chastagnier
 */
public interface WebPlugin {

  static WebPlugin get() {
    return ServiceProvider.getService(WebPlugin.class);
  }

  /**
   * Include a plugin by its name.
   * <p>The method returns a filled element container one time per request.</p>
   * @param plugin the aimed plugin.
   * @param language the aimed language.
   * @return the element container filled with the necessary HTML tags to invoke the plugin, or
   * empty if the initialization script has already been done into the context of the same request.
   */
  ElementContainer getHtml(SupportedWebPlugins plugin, final String language);
}
