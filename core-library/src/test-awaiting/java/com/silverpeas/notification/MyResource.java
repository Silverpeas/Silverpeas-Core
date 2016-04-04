/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package com.silverpeas.usernotification;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * It defines a resource in Silverpeas from which notifications can be triggered.
 * It is dedicated to test the serialization of POJO through the notification API.
 */
public class MyResource implements Serializable {
  private static final long serialVersionUID = -8458037386176937030L;

  private String name;
  private String id = UUID.randomUUID().toString();
  private Date creationDate = new Date();
  private Date lastModificationDate = new Date();

  public Date getCreationDate() {
    return new Date(creationDate.getTime());
  }

  public void setCreationDate(Date creationDate) {
    this.creationDate = new Date(creationDate.getTime());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getLastModificationDate() {
    return new Date(lastModificationDate.getTime());
  }

  public void setLastModificationDate(Date lastModificationDate) {
    this.lastModificationDate = new Date(lastModificationDate.getTime());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public MyResource(String name) {
    this.name = name;
  }
}
