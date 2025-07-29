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
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.component;

import org.silverpeas.core.admin.component.model.Tool;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.util.StringUtil;

import javax.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A registry of tools available in Silverpeas.
 * </p>
 * The {@code Tool} available in Silverpeas are defined by a simple property file.
 * @author silveryocha
 */
@Service
@Singleton
public class ToolRegistry implements Initialization {

  private final Map<String, Tool> toolsById = new HashMap<>();

  ToolRegistry() {
  }

  /**
   * Gets an instance of this ToolRegistry registry.
   * @return a ToolRegistry instance.
   */
  public static ToolRegistry get() {
    return ServiceProvider.getService(ToolRegistry.class);
  }

  @Override
  public void init() {
    Stream.of(ResourceLocator.getGeneralSettingBundle().getString("availableToolIds", ""))
        .flatMap(s -> Stream.of(s.split("[ ,;]")))
        .filter(StringUtil::isDefined)
        .forEach(t -> toolsById.put(t , new Tool(t)));
  }

  /**
   * Gets the Tool instance registered under the specified id.
   * @param toolId the id (or the id) of the tool.
   * @return an optional Tool instance if such instance exists under the given id.
   */
  public Optional<Tool> getTool(String toolId) {
    return Optional.ofNullable(toolsById.get(toolId));
  }

  /**
   * Gets all the registered Tool instances indexed by their id.
   * @return a dictionary of the available Tool instances indexed by their id.
   */
  public Map<String, Tool> getAllTools() {
    return Collections.unmodifiableMap(toolsById);
  }
}
