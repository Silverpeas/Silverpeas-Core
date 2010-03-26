/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.Participant;
import com.silverpeas.workflow.api.model.Participants;

/**
 * Class implementing the representation of the &lt;participants&gt; element of a Process Model.
 **/
public class ParticipantsImpl implements Serializable, Participants {
  private List participantList;

  /**
   * Constructor
   */
  public ParticipantsImpl() {
    participantList = new ArrayList();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Participants#addParticipant(com.silverpeas
   * .workflow.api.model.Participant)
   */
  public void addParticipant(Participant participant) {
    participantList.add(participant);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Participants#createParticipant()
   */
  public Participant createParticipant() {
    return new ParticipantImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Participants#getParticipants()
   */
  public Participant[] getParticipants() {
    if (participantList == null)
      return null;

    return (Participant[]) participantList.toArray(new ParticipantImpl[0]);
  }

  /**
   * Get the participant with given name
   * @param name participant name
   * @return wanted participant
   */
  public Participant getParticipant(String name) {
    if (participantList == null)
      return null;

    Participant participant = null;
    for (int r = 0; r < participantList.size(); r++) {
      participant = (Participant) participantList.get(r);
      if (participant.getName().equals(name))
        return participant;
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Participants#iterateParticipant()
   */
  public Iterator iterateParticipant() {
    return participantList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.Participants#removeParticipant(java.lang .String)
   */
  public void removeParticipant(String strParticipantName)
      throws WorkflowException {
    Participant participant = createParticipant();

    participant.setName(strParticipantName);

    if (participantList == null)
      return;

    if (!participantList.remove(participant))
      throw new WorkflowException("ParticipantsImpl.removeParticipant()", //$NON-NLS-1$
          "workflowEngine.EX_PARTICIPANT_NOT_FOUND", // $NON-NLS-1$
          strParticipantName == null ? "<null>" //$NON-NLS-1$
              : strParticipantName);
  }
}