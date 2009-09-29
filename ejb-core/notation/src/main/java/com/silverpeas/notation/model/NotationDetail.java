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
package com.silverpeas.notation.model;

import java.io.Serializable;

public class NotationDetail implements Serializable {

  private String instanceId;
  private String elementId;
  private int elementType;
  private int notesCount;
  private float globalNote;
  private int userNote;

  public NotationDetail(NotationPK pk) {
    instanceId = pk.getInstanceId();
    elementId = pk.getId();
    elementType = pk.getType();
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getElementId() {
    return elementId;
  }

  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  public int getElementType() {
    return elementType;
  }

  public void setElementType(int elementType) {
    this.elementType = elementType;
  }

  public int getNotesCount() {
    return notesCount;
  }

  public void setNotesCount(int notesCount) {
    this.notesCount = notesCount;
  }

  public float getGlobalNote() {
    return globalNote;
  }

  public void setGlobalNote(float globalNote) {
    this.globalNote = globalNote;
  }

  public int getUserNote() {
    return userNote;
  }

  public void setUserNote(int userNote) {
    this.userNote = userNote;
  }

  public int getRoundGlobalNote() {
    return Math.round(globalNote);
  }

}
