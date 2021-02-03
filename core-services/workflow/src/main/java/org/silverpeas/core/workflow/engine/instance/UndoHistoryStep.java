/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.workflow.engine.instance;

import org.silverpeas.core.persistence.datasource.model.identifier.UniqueIntegerIdentifier;
import org.silverpeas.core.persistence.datasource.model.jpa.BasicJpaEntity;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "sb_workflow_undo_step")
public class UndoHistoryStep extends BasicJpaEntity<UndoHistoryStep, UniqueIntegerIdentifier> {

  /**
   * the step id that provokes the atomic operation
   */
  @Column
  private int stepId;

  /**
   * the instance id on which the atomic operation has been made
   */
  @ManyToOne
  @JoinColumn(name = "instanceid", nullable = false)
  private ProcessInstanceImpl processInstance = null;

  /**
   * the atomic operation stored in this step
   */
  @Column
  private String action;

  /**
   * parameters of atomic operation concatenated as "param1##param2...paramN"
   */
  @Column
  private String parameters;

  /**
   * default constructor
   */
  protected UndoHistoryStep() {
  }

  /**
   * Get the step id that provokes the atomic operation
   * @return the step id that provokes the atomic operation
   */
  public String getStepId() {
    return String.valueOf(stepId);
  }

  /**
   * Set the step id that provokes the atomic operation
   * @param stepId the step id that provokes the atomic operation
   */
  public void setStepId(String stepId) {
    this.stepId = Integer.parseInt(stepId);
  }

  /**
   * Set the instance on which the atomic operation has been made
   * @param instance the instance id
   */
  public void setInstance(ProcessInstance instance) {
    this.processInstance = (ProcessInstanceImpl) instance;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    final UndoHistoryStep that = (UndoHistoryStep) o;

    return stepId == that.stepId;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + stepId;
    return result;
  }
}