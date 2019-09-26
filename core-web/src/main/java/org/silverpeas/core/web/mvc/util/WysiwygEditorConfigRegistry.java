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
package org.silverpeas.core.web.mvc.util;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.web.util.WysiwygEditorConfig;

import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry of configurations of the WYSIWYG editor per Silverpeas components. Each Silverpeas
 * components has its own configuration of the WYSIWYG editor and this registry is a way to
 * gather them in a single place in order to retrieve them later in any layer.
 * @author mmoquillon
 */
@Singleton
public class WysiwygEditorConfigRegistry {

  private Map<String, WysiwygEditorConfig> configsPerComponents = new ConcurrentHashMap<>();

  protected WysiwygEditorConfigRegistry() {
    // default constructor
  }

  public static WysiwygEditorConfigRegistry get() {
    return ServiceProvider.getService(WysiwygEditorConfigRegistry.class);
  }

  /**
   * Register for the specified component a specific configuration of the WYSIWYG editor.
   * @param componentName the name of the component. It can be a personal or a multi-user
   * application.
   * @param config the WYSIWYG editor configuration to register.
   */
  public void register(final String componentName, final WysiwygEditorConfig config) {
    configsPerComponents.put(componentName, config);
  }

  /**
   * Gets the WYSIWYG editor configuration registered for the specified component. It no particular
   * configuration was registered for the given component, then a default configuration is returned.
   * @param componentName the name of the component for which the WYSIWYG editor configuration is
   * asked.
   * @return either the peculiar configuration for the specified component or the default WYSIWYG
   * editor configuration.
   */
  public WysiwygEditorConfig get(final String componentName) {
    final WysiwygEditorConfig config =
        configsPerComponents.computeIfAbsent(componentName, WysiwygEditorConfig::new);
    return config.copy();
  }
}
  