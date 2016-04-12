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

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import java.util.Date;

/**
 * A Question object represents a question asked for the instance
 * @table SB_Workflow_Question
 * @depends ProcessInstanceImpl
 * @key-generator MAX
 */
public class QuestionImpl implements Question {
  /**
   * default constructor used by Castor
   */
  public QuestionImpl() {
  }

  /**
   * a Question object is build from - its content, - the source state name, - the target state name
   * - and the instance where the question was asked
   */
  public QuestionImpl(ProcessInstance processInstance, String questionText,
      String fromState, String targetState, User fromUser, User toUser) {
    this.processInstance = (ProcessInstanceImpl) processInstance;
    this.questionText = questionText;
    this.fromState = fromState;
    this.targetState = targetState;
    this.questionDate = new Date();
    this.fromUserId = fromUser.getUserId();
    this.toUserId = toUser.getUserId();
  }

  /**
   * Get the question id
   */
  public String getId() {
    return id;
  }

  /**
   * Set the question id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Get the process instance where the question was asked
   */
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  /**
   * Set the process instance where the question was asked
   */
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = (ProcessInstanceImpl) processInstance;
  }

  /**
   * Get the state where the question was asked
   */
  public String getFromStateName() {
    return fromState;
  }

  /**
   * Get the state where the question was asked
   */
  public State getFromState() {
    if (processInstance == null)
      return null;

    try {
      return processInstance.getProcessModel().getState(fromState);
    } catch (WorkflowException e) {
      return null;
    }
  }

  /**
   * Set the state where the question was asked
   */
  public void setFromStateName(String fromState) {
    this.fromState = fromState;
  }

  /**
   * Get the destination state for the question
   */
  public String getTargetStateName() {
    return targetState;
  }

  /**
   * Get the destination state for the question
   */
  public State getTargetState() {
    if (processInstance == null)
      return null;

    try {
      return processInstance.getProcessModel().getState(targetState);
    } catch (WorkflowException e) {
      return null;
    }
  }

  /**
   * Set the destination state for the question
   */
  public void setTargetStateName(String targetState) {
    this.targetState = targetState;
  }

  /**
   * Get the question content
   */
  public String getQuestionText() {
    return questionText;
  }

  /**
   * Set the question content
   */
  public void setQuestionText(String questionText) {
    this.questionText = questionText;
  }

  /**
   * Get the response content
   */
  public String getResponseText() {
    return responseText;
  }

  /**
   * Set the response content
   */
  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  /**
   * Answer this question
   */
  public void answer(String responseText) {
    this.responseText = responseText;
    this.responseDate = new Date();
  }

  /**
   * Set the id of user who asked the question
   */
  public void setFromUserId(String fromUserId) {
    this.fromUserId = fromUserId;
  }

  /**
   * Get the id of user who asked the question
   */
  public String getFromUserId() {
    return fromUserId;
  }

  /**
   * Get the user who asked the question
   */
  public User getFromUser() throws WorkflowException {
    return Workflow.getUserManager().getUser(fromUserId);
  }

  /**
   * Set the id of user who received the question
   */
  public void setToUserId(String toUserId) {
    this.toUserId = toUserId;
  }

  /**
   * Get the id of user who received the question
   */
  public String getToUserId() {
    return toUserId;
  }

  /**
   * Get the user who received the question
   */
  public User getToUser() throws WorkflowException {
    return Workflow.getUserManager().getUser(toUserId);
  }

  /**
   * Get the date when question was asked
   */
  public Date getQuestionDate() {
    return questionDate;
  }

  /**
   * Set the date when question was asked
   */
  public void setQuestionDate(Date questionDate) {
    this.questionDate = questionDate;
  }

  /**
   * Get the date when question was asked
   */
  public Date getResponseDate() {
    return responseDate;
  }

  /**
   * Set the date when question was asked
   */
  public void setResponseDate(Date responseDate) {
    this.responseDate = responseDate;
  }

  /**
   * Is a response was sent to this question
   */
  public boolean hasResponse() {
    return (responseText != null);
  }

  /**
   * Has this question been answered and taken in account, if yes, so it's not relevant anymore
   * (return false)
   */
  public boolean isRelevant() {
    return relevant;
  }

  public int isRelevantCastor() {
    if (isRelevant())
      return 1;
    else
      return 0;
  }

  /**
   * Set the relevant status of this question
   * @see isRelevant()
   */
  public void setRelevant(boolean relevant) {
    this.relevant = relevant;
  }

  public void setRelevantCastor(int relevant) {
    this.relevant = (relevant == 1);
  }

  /**
   * the question Id
   * @field-name id
   * @sql-type integer
   * @primary-key
   */
  private String id = null;

  /**
   * the process instance where the question was asked
   * @field-name processInstance
   * @field-type ProcessInstanceImpl
   * @sql-name instanceId
   */
  private ProcessInstanceImpl processInstance = null;

  /**
   * state where the question was asked
   * @field-name fromState
   * @get-method getFromStateName
   * @set-method setFromStateName
   */
  private String fromState = null;

  /**
   * destination state for the question
   * @field-name targetState
   * @get-method getTargetStateName
   * @set-method setTargetStateName
   */
  private String targetState = null;

  /**
   * question content
   * @field-name questionText
   */
  private String questionText = null;

  /**
   * response content
   * @field-name responseText
   */
  private String responseText = null;

  /**
   * date when question was asked
   * @field-name questionDate
   * @sql-type timestamp
   */
  private Date questionDate = null;

  /**
   * date when question was answered
   * @field-name responseDate
   * @sql-type timestamp
   */
  private Date responseDate = null;

  /**
   * Has this response been taken in account, if yes, so it's not relevant anymore (return false)
   * @field-name relevant
   * @get-method isRelevant
   * @set-method setRelevant
   */
  private boolean relevant = true;

  /**
   * The id of user who asked this question
   * @field-name fromUserId
   */
  private String fromUserId = null;

  /**
   * The id of user who received this question
   * @field-name toUserId
   */
  private String toUserId = null;

}