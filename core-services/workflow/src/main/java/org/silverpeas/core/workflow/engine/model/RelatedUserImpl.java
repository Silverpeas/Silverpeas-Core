/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;

import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Participant;
import org.silverpeas.core.workflow.api.model.RelatedUser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class implementing the representation of the &lt;relatedUser&gt; element of a Process Model.
 **/
@XmlRootElement(name = "relatedUser")
@XmlAccessorType(XmlAccessType.NONE)
public class RelatedUserImpl implements RelatedUser, Serializable {
  private static final long serialVersionUID = -7371460894690406952L;
  @XmlIDREF
  @XmlAttribute
  private ParticipantImpl participant;
  @XmlIDREF
  @XmlAttribute
  private ItemImpl folderItem;
  @XmlAttribute
  private String relation;
  @XmlAttribute
  private String role;

  /**
   * Constructor
   */
  public RelatedUserImpl() {
    super();
  }

  /**
   * Get the referred participant
   */
  public Participant getParticipant() {
    return participant;
  }

  /*
   * (non-Javadoc)
   * @see RelatedUser#setParticipant(com.silverpeas
   * .workflow.api.model.Participant)
   */
  public void setParticipant(Participant participant) {
    this.participant = (ParticipantImpl) participant;
  }

  /**
   * Get the referred item
   */
  public Item getFolderItem() {
    return folderItem;
  }

  /**
   * Set the referred item
   * @param folderItem item to refer
   */
  public void setFolderItem(Item folderItem) {
    this.folderItem = (ItemImpl) folderItem;
  }

  /**
   * Get the relation between user and participant
   */
  public String getRelation() {
    return this.relation;
  }

  /**
   * Set the relation between user and participant
   * @param relation relation as a String
   */
  public void setRelation(String relation) {
    this.relation = relation;
  }

  /**
   * Get the role to which the related user will be affected
   * @return the role name
   */
  public String getRole() {
    return this.role;
  }

  /**
   * Set the role to which the related user will be affected
   * @param role role as a String
   */
  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final RelatedUserImpl that = (RelatedUserImpl) o;

    if (participant != null ? !participant.equals(that.participant) : that.participant != null) {
      return false;
    }
    if (folderItem != null ? !folderItem.equals(that.folderItem) : that.folderItem != null) {
      return false;
    }
    if (relation != null ? !relation.equals(that.relation) : that.relation != null) {
      return false;
    }
    return role != null ? role.equals(that.role) : that.role == null;
  }

  @Override
  public int hashCode() {
    int result = participant != null ? participant.hashCode() : 0;
    result = 31 * result + (folderItem != null ? folderItem.hashCode() : 0);
    result = 31 * result + (relation != null ? relation.hashCode() : 0);
    result = 31 * result + (role != null ? role.hashCode() : 0);
    return result;
  }
}