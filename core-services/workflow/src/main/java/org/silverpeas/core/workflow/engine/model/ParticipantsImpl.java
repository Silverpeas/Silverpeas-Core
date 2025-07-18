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
package org.silverpeas.core.workflow.engine.model;

import org.silverpeas.core.workflow.api.model.Participant;
import org.silverpeas.core.workflow.api.model.Participants;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class implementing the representation of the &lt;participants&gt; element of a Process Model.
 **/
@XmlRootElement(name = "participants")
@XmlAccessorType(XmlAccessType.NONE)
public class ParticipantsImpl implements Serializable, Participants {

  private static final long serialVersionUID = 2184206918365803850L;
  @XmlElement(name = "participant", type = ParticipantImpl.class)
  private List<Participant> participantList;

  /**
   * Constructor
   */
  public ParticipantsImpl() {
    participantList = new ArrayList<>();
  }

  /*
   * (non-Javadoc)
   * @see Participants#addParticipant(com.silverpeas
   * .workflow.api.model.Participant)
   */
  @Override
  public void addParticipant(Participant participant) {
    participantList.add(participant);
  }

  /*
   * (non-Javadoc)
   * @see Participants#createParticipant()
   */
  @Override
  public Participant createParticipant() {
    return new ParticipantImpl();
  }

  /*
   * (non-Javadoc)
   * @see Participants#getParticipants()
   */
  @Override
  public Participant[] getParticipants() {
    if (participantList == null) {
      return new Participant[0];
    }
    return participantList.toArray(new Participant[0]);
  }

  /**
   * Get the participant with given name
   * @param name participant name
   * @return wanted participant
   */
  @Override
  public Participant getParticipant(String name) {
    if (participantList == null) {
      return null;
    }
    for (Participant participant : participantList) {
      if (participant.getName().equals(name)) {
        return participant;
      }
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see Participants#iterateParticipant()
   */
  @Override
  public Iterator<Participant> iterateParticipant() {
    return participantList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see Participants#removeParticipant(java.lang .String)
   */
  @Override
  public void removeParticipant(String strParticipantName) {
    if (participantList == null) {
      return;
    }
    participantList.removeIf(p -> p.getName().equals(strParticipantName));
  }
}