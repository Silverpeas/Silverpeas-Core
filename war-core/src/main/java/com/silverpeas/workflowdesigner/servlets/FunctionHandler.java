/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflowdesigner.servlets;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflowdesigner.control.WorkflowDesignerSessionController;
import com.silverpeas.workflowdesigner.model.WorkflowDesignerException;

/**
 * This interface describes a handler of an atomic function of the Workflow Designer Request Router
 */
public interface FunctionHandler {
  /**
   * Handle the function do the processing and return the URL of the response
   * @param function the name of the function to handle
   * @param workflowDesignerSC the session controller
   * @param request the HTTP request
   * @return the name of the destination JSP, without the path part
   * @throws WorkflowDesignerException when something goes wrong
   * @throws WorkflowException when something goes wrong
   */
  public String getDestination(String function,
      WorkflowDesignerSessionController workflowDesignerSC,
      HttpServletRequest request) throws WorkflowDesignerException,
      WorkflowException;
}
