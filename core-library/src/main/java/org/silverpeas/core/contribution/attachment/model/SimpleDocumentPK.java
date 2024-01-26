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
package org.silverpeas.core.contribution.attachment.model;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.WAPrimaryKey;
import org.silverpeas.kernel.util.StringUtil;

import java.util.Objects;

/**
 * the unique identifier of a document.
 * @author ehugonnet
 */
public class SimpleDocumentPK extends ResourceReference {

  private static final long serialVersionUID = 5609285040251527744L;
  private long oldSilverpeasId = -1L;

  public SimpleDocumentPK(String id) {
    super(id);
    componentName = "";
    if (StringUtil.isLong(id)) {
      this.oldSilverpeasId = Long.parseLong(id);
    }
  }

  public SimpleDocumentPK(String id, String componentId) {
    super(id, componentId);
    if (StringUtil.isLong(id)) {
      this.oldSilverpeasId = Long.parseLong(id);
    }
  }

  public SimpleDocumentPK(String id, WAPrimaryKey pk) {
    super(id, pk.getInstanceId());
    if (StringUtil.isLong(id)) {
      this.oldSilverpeasId = Long.parseLong(id);
    }
  }

  public long getOldSilverpeasId() {
    return oldSilverpeasId;
  }

  public SimpleDocumentPK setOldSilverpeasId(long oldSilverpeasId) {
    this.oldSilverpeasId = oldSilverpeasId;
    return this;
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
    if (!Objects.equals(this.id, other.id)) {
      return false;
    }
    return Objects.equals(this.componentName, other.componentName);
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
    return "SimpleDocumentPK{id = " + getId() + ", componentName = " +
        getComponentName() + ", oldSilverpeasId=" + oldSilverpeasId + '}';
  }

  public SimpleDocumentPK copy() {
    SimpleDocumentPK pk = new SimpleDocumentPK(getId());
    pk.setOldSilverpeasId(getOldSilverpeasId());
    pk.setSpace(getSpace());
    pk.setComponentName(getComponentName());
    return pk;
  }
}
