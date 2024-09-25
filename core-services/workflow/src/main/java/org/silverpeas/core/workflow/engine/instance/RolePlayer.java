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

package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.workflow.api.instance.ProcessInstance;

/**
 * A user playing a given role within a workflow process instance.
 *
 * @author mmoquillon
 */
public interface RolePlayer {

  /**
   * Gets the unique identifier of the user playing a given role.
   * @return the unique identifier of a user.
   */
  String getUserId();

  /**
   * Gets the role the user plays in the process instance.
   * @return the name of the role playing by the user.
   */
  String getRole();

  /**
   * Gets the current state of the user in the process instance.
   * @return the name of the state of the user.
   */
  String getState();

  void setUserId(String userId);

  void setGroupId(String groupId);

  void setUsersRole(String role);

  void setRole(String role);

  void setState(String state);

  void setProcessInstance(ProcessInstanceImpl processInstance);
}
