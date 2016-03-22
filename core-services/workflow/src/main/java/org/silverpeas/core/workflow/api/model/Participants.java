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

package org.silverpeas.core.workflow.api.model;

import java.util.Iterator;

import org.silverpeas.core.workflow.api.WorkflowException;

/**
 * Interface describing a representation of the &lt;participants&gt; element of a Process Model.
 */
public interface Participants {

  /**
   * Iterate through the Participant objects
   * @return an iterator
   */
  public Iterator<Participant> iterateParticipant();

  /**
   * Create an Participant
   * @return an object implementing Participant
   */
  public Participant createParticipant();

  /**
   * Add an participant to the collection
   * @param participant to be added
   */
  public void addParticipant(Participant participant);

  /**
   * Returns all the Participant elements as an array
   * @return
   */
  public Participant[] getParticipants();

  /**
   * Get the participant definition with given name
   * @param name participant name
   * @return wanted participant definition
   */
  public Participant getParticipant(String name);

  /**
   * Remove an participant from the collection
   * @param strParticipantName the name of the participant to be removed.
   * @throws WorkflowException when the participant could not be deleted.
   */
  public void removeParticipant(String strParticipantName) throws WorkflowException;
}
