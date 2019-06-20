/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General License as
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
 * GNU Affero General License for more details.
 *
 * You should have received a copy of the GNU Affero General License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.api.instance;

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

import java.util.Date;

/**
 * A Question object represents a question asked for the instance
 */
public interface Question {
  /**
   * Get the question id
   */
  String getId();

  /**
   * Get the process instance where the question was asked
   */
  ProcessInstance getProcessInstance();

  /**
   * Get the state where the question was asked
   */
  State getFromState();

  /**
   * Get the destination state for the question
   */
  State getTargetState();

  /**
   * Get the question content
   */
  String getQuestionText();

  /**
   * Get the response content
   */
  String getResponseText();

  /**
   * Answer this question
   */
  void answer(String responseText);

  /**
   * Get the user who asked the question
   */
  User getFromUser() throws WorkflowException;

  /**
   * Get the user who received the question
   */
  User getToUser() throws WorkflowException;

  /**
   * Get the date when question was asked
   */
  Date getQuestionDate();

  /**
   * Get the date when question was asked
   */
  Date getResponseDate();

  /**
   * Is a response was sent to this question
   */
  boolean hasResponse();

  /**
   * Has this question been answered and taken in account, if yes, so it's not relevant anymore
   * (return false)
   */
  boolean isRelevant();

}