/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.util;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Embeddable;

/**
 *
 * @author ehugonnet
 */
@Embeddable
public class UuidPk implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String uuid;

  public UuidPk() {
    this.uuid = UUID.randomUUID().toString();
  }

  public UuidPk(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final UuidPk other = (UuidPk) obj;
    if ((this.uuid == null) ? (other.uuid != null) : !this.uuid.equals(other.uuid)) {
      return false;
    }
    return true;
  }
  
}
