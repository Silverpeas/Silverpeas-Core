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
package org.silverpeas.attachment.model;

import com.stratelia.webactiv.util.WAPrimaryKey;
import java.io.Serializable;

/**
 *
 * @author ehugonnet
 */
public class SimpleDocumentPK extends WAPrimaryKey implements Serializable {

  private long oldSilverpeasId = -1L;

  public long getOldSilverpeasId() {
    return oldSilverpeasId;
  }

  public SimpleDocumentPK setOldSilverpeasId(long oldSilverpeasId) {
    this.oldSilverpeasId = oldSilverpeasId;
    return this;
  }

  public SimpleDocumentPK(String id) {
    super(id);
  }

  public SimpleDocumentPK(String id, String componentId) {
    super(id, componentId);
  }

  public SimpleDocumentPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleDocumentPK other = (SimpleDocumentPK) obj;
    if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
      return false;
    }
    if ((this.componentName == null) ? (other.componentName != null)
        : !this.componentName.equals(other.componentName)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 71 * hash + (this.id != null ? this.id.hashCode() : 0);
    hash = 71 * hash + (this.componentName != null ? this.componentName.hashCode() : 0);
    hash = 83 * hash + (int) (this.oldSilverpeasId ^ (this.oldSilverpeasId >>> 32));
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder(100);
    buffer.append("SimpleDocumentPK{id = ").append(getId()).append(", componentName = ").
        append(getComponentName()).append(", oldSilverpeasId=").append(oldSilverpeasId).append('}');
    return buffer.toString();
  }
}
