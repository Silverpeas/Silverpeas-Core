/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.admin.component.model;

import org.apache.commons.lang3.NotImplementedException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Optional;

import static org.silverpeas.core.SilverpeasExceptionMessages.undefined;
import static org.silverpeas.core.util.StringUtil.isNotDefined;

/**
 * @author silveryocha
 */
public class ToolInstance implements SilverpeasPersonalComponentInstance {
  private static final long serialVersionUID = 5778041904324864493L;

  private final transient Tool tool;

  /**
   * Hidden constructor.
   */
  private ToolInstance(final Tool tool) {
    this.tool = tool;
  }

  /**
   * Gets the tool instance from an instance identifier.
   * @param toolId identifier of a tool.
   * @return optionally an instance of {@link ToolInstance}.
   */
  public static Optional<ToolInstance> from(String toolId) {
    if (isNotDefined(toolId)) {
      String message = undefined("tool instance");
      SilverLogger.getLogger(ToolInstance.class).error(message);
      throw new IllegalArgumentException(message);
    }
    return Tool.getById(toolId).map(ToolInstance::new);
  }

  @Override
  public User getUser() {
    throw new NotImplementedException("this information must not be used from a tool");
  }

  @Override
  public String getId() {
    return getName();
  }

  @Override
  public String getSpaceId() {
    throw new NotImplementedException("this information must not be used from a tool");
  }

  @Override
  public String getName() {
    return tool.getName();
  }

  @Override
  public String getLabel() {
    return tool.getLabel(null);
  }

  @Override
  public String getLabel(final String language) {
    return tool.getLabel(language);
  }

  @Override
  public String getDescription() {
    return tool.getDescription(null);
  }

  @Override
  public String getDescription(final String language) {
    return tool.getDescription(language);
  }

  @Override
  public int getOrderPosition() {
    return 0;
  }
}
