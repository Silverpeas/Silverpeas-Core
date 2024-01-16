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
package org.silverpeas.core.html;

import org.apache.ecs.ElementContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Web plugin consumer registry.<br>
 * It consists to map a {@link SupportedWebPlugins} with a {@link BiConsumer} which consumes an
 * {@link ElementContainer} and the language as string.
 * @author Yohann Chastagnier
 */
public class WebPluginConsumerRegistry {

  private static final Map<SupportedWebPlugins, BiConsumer<ElementContainer, String>> registry =
      new HashMap<>();

  /**
   * Hidden constructor.
   */
  private WebPluginConsumerRegistry() {
  }

  /**
   * Adds a plugin into registry.
   * @param plugin a plugin.
   * @param inclusion the inclusion.
   */
  public static void add(SupportedWebPlugins plugin,
      BiConsumer<ElementContainer, String> inclusion) {
    registry.put(plugin, inclusion);
  }

  /**
   * Gets a plugin into registry.
   * @param plugin a plugin.
   * @return the consumer.
   */
  public static BiConsumer<ElementContainer, String> get(SupportedWebPlugins plugin) {
    return registry.get(plugin);
  }
}
