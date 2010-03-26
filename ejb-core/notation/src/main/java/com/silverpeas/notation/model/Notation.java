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

/**
 * 
 */
public class Notation implements Serializable {

  public static final int ID_UNDEFINED = -1;

  /**
   * Elements types referenced by notations.
   */
  public static final int TYPE_UNDEFINED = -1;
  public static final int TYPE_PUBLICATION = 0;
  public static final int TYPE_FORUM = 1;
  public static final int TYPE_MESSAGE = 2;

  private int id;
  private String instanceId;
  private String externalId;
  private int externalType;
  private String author;
  private int note;

  public Notation(int id, String instanceId, String externalId,
      int externalType, String author, int note) {
    this.id = id;
    this.instanceId = instanceId;
    this.externalId = externalId;
    this.externalType = externalType;
    this.author = author;
    this.note = note;
  }

  public Notation(NotationPK pk, int note) {
    this.id = ID_UNDEFINED;
    this.instanceId = pk.getInstanceId();
    this.externalId = pk.getId();
    this.externalType = pk.getType();
    this.author = pk.getUserId();
    this.note = note;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public int getExternalType() {
    return externalType;
  }

  public void setExternalType(int externalType) {
    this.externalType = externalType;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public int getNote() {
    return note;
  }

  public void setNote(int note) {
    this.note = note;
  }

}