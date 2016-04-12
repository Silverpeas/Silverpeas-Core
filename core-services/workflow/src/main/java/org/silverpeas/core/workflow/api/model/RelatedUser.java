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

/**
 * Interface describing a representation of the &lt;relatedUser&gt; element of a Process Model.
 */
public interface RelatedUser {
  /**
   * Get the referred participant
   * @return Participant object
   */
  public Participant getParticipant();

  /**
   * Set the referred participant
   * @param Participant object
   */
  public void setParticipant(Participant participant);

  /**
   * Get the referred item
   */
  public Item getFolderItem();

  /**
   * Set the referred item
   */
  public void setFolderItem(Item item);

  /**
   * Get the relation between user and participant
   * @return relation, if null get the participant himself instead of searching related user
   */
  public String getRelation();

  /**
   * set the relation between user and participant
   */
  public void setRelation(String strRelation);

  /**
   * Get the role to which the related user will be affected
   * @return the role name
   */
  public String getRole();

  /**
   * New method: Set the role the related user will be affected to
   */
  public void setRole(String strRole);
}