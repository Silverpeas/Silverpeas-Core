/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.api;

import org.silverpeas.core.exception.SilverpeasException;

/**
 * Thrown by the workflow engine components.
 */
public class WorkflowException extends SilverpeasException {
  private static final long serialVersionUID = -1912189634294138350L;

  /**
   * Returns the module name (as known by SilverTrace).
   */
  public String getModule() {
    return "workflowEngine";
  }

  /**
   * Set the caller and the error message
   */
  public WorkflowException(String caller, String message) {
    super(caller, SilverpeasException.ERROR, message);
  }

  /**
   * Set the caller, the error message and the nested exception.
   */
  public WorkflowException(String caller, String message,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, nestedException);
  }

  /**
   * Set the caller, infos and the error message
   */
  public WorkflowException(String caller, String message, String infos) {
    super(caller, SilverpeasException.ERROR, message, infos);
  }

  /**
   * Set the caller, the error message, infos and the nested exception.
   */
  public WorkflowException(String caller, String message, String infos,
      Exception nestedException) {
    super(caller, SilverpeasException.ERROR, message, infos, nestedException);
  }

  /**
   * Set the caller, the level and the error message
   */
  public WorkflowException(String caller, int level, String message) {
    super(caller, level, message);
  }

  /**
   * Set the caller, the level, the error message and the nested exception.
   */
  public WorkflowException(String caller, int level, String message,
      Exception nestedException) {
    super(caller, level, message, nestedException);
  }
}
