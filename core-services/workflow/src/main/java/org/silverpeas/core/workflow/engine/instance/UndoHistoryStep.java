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

package org.silverpeas.core.workflow.engine.instance;

/**
 * One implementation of WorkflowEngine The workflow engine main services.
 * @table SB_Workflow_Undo_Step
 * @key-generator MAX
 */
public class UndoHistoryStep {
  /**
   * the unique id
   * @primary-key
   * @field-name id
   * @sql-type integer
   */
  private String id;

  /**
   * the step id that provokes the atomic operation
   * @field-name stepId
   * @sql-type integer
   */
  private String stepId;

  /**
   * the instance id on which the atomic operation has been made
   * @field-name instanceId
   * @sql-type integer
   */
  private String instanceId;

  /**
   * the atomic operation stored in this step
   * @field-name action
   */
  private String action;

  /**
   * parameters of atomic operation concatenated as "param1##param2...paramN"
   * @field-name parameters
   */
  private String parameters;

  /**
   * default constructor
   */
  public UndoHistoryStep() {
  }

  /**
   * Get the unique id
   * @return the unique id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the unique id
   * @param id the unique id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the step id that provokes the atomic operation
   * @return the step id that provokes the atomic operation
   */
  public String getStepId() {
    return stepId;
  }

  /**
   * Set the step id that provokes the atomic operation
   * @param stepId the step id that provokes the atomic operation
   */
  public void setStepId(String stepId) {
    this.stepId = stepId;
  }

  /**
   * Get the instance id on which the atomic operation has been made
   * @return the instance id
   */
  public String getInstanceId() {
    return instanceId;
  }

  /**
   * Set the instance id on which the atomic operation has been made
   * @param instanceId the instance id
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  /**
   * Get the atomic operation stored in this step
   * @return the atomic operation stored in this step
   */
  public String getAction() {
    return action;
  }

  /**
   * Set the atomic operation stored in this step
   * @param action the atomic operation stored in this step
   */
  public void setAction(String action) {
    this.action = action;
  }

  /**
   * Get parameters of atomic operation
   * @return parameters concatenated as "param1##param2...paramN"
   */
  public String getParameters() {
    return parameters;
  }

  /**
   * Set parameters of atomic operation
   * @param parameters parameters concatenated as "param1##param2...paramN"
   */
  public void setParameters(String parameters) {
    this.parameters = parameters;
  }
}