/*
 * Copyright (C) 2000 - 2022 Silverpeas
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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.admin.component.ToolRegistry;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p>
 * A tool represents a Personal Application Component. It is an application dedicated to be
 * instantiated for each user in their personal space.
 * <p>
 * The tools available in Silverpeas are loaded by the
 * {@code org.silverpeas.core.admin.component .ToolRegistry} registry, among of them from their XML
 * descriptor. They can be the accessed either by the registry itself or by the Tool class (it
 * delegates the access to the registry). The XML descriptor of a personal application component is
 * defined by the following XSD: <a href="https://www.silverpeas.org/xsd/component.xsd">
 * https://www.silverpeas.org/xsd/component.xsd</a>.
 */
public class Tool extends AbstractSilverpeasComponent {

  protected final String id;

  public Tool(final String id) {
    this.id = id;
  }

  /**
   * Gets the Tool instance with the specified id.
   * @param toolId the unique id (or unique id) of the Tool to return.
   * @return optionally a Tool instance with the given id.
   */
  public static Optional<Tool> getById(String toolId) {
    return ToolRegistry.get().getTool(toolId);
  }

  /**
   * Gets all the available Tool instances.
   * @return a collection of Tool instance.
   */
  public static Collection<Tool> getAll() {
    return ToolRegistry.get().getAllTools().values();
  }

  @Override
  public String getName() {
    return id;
  }

  @Override
  public Map<String, String> getLabel() {
    return Collections.emptyMap();
  }

  @Override
  public boolean isVisible() {
    return false;
  }

  @Override
  public boolean isPersonal() {
    return true;
  }

  @Override
  public Map<String, String> getDescription() {
    return Collections.emptyMap();
  }

  @Override
  public List<Parameter> getParameters() {
    return Collections.emptyList();
  }

  @Override
  public boolean hasParameterDefined(String parameterName) {
    return getIndexedParametersByName().get(parameterName) != null;
  }

  @Override
  public List<GroupOfParameters> getGroupsOfParameters() {
    return Collections.emptyList();
  }
}